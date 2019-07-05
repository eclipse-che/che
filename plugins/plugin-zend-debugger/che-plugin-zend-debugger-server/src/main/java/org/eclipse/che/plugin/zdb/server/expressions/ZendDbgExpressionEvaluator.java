/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.expressions;

import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.KIND_ARRAY_MEMBER;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.KIND_OBJECT_MEMBER;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.MOD_PRIVATE;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.MOD_PROTECTED;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet.MOD_PUBLIC;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_ARRAY;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_BOOL;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_FLOAT;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_INT;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_OBJECT;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_RESOURCE;
import static org.eclipse.che.plugin.zdb.server.expressions.IDbgDataType.DataType.PHP_STRING;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.plugin.zdb.server.ZendDebugger;
import org.eclipse.che.plugin.zdb.server.connection.IDbgMessage;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.AssignValueRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.GetVariableValueRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgConnection;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.AssignValueResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetVariableValueResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineResponse;
import org.eclipse.che.plugin.zdb.server.expressions.IDbgDataFacet.Facet;

/**
 * Zend debug expressions manager.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgExpressionEvaluator {

  private static class ValueDecoder {

    public void deserialize(ZendDbgExpression expression, byte[] value) {
      if (value == null) {
        // Expression is illegal.
        value = new byte[] {'N'};
      }
      read(expression, new ValueReader(value));
    }

    private void read(ZendDbgExpression expression, ValueReader reader) {
      char type = reader.readType();
      switch (type) {
        case 'i':
          {
            readIntType(expression, reader);
            break;
          }
        case 'd':
          {
            readFloatType(expression, reader);
            break;
          }
        case 's':
          {
            readSringType(expression, reader);
            break;
          }
        case 'b':
          {
            readBooleanType(expression, reader);
            break;
          }
        case 'r':
          {
            readResourceType(expression, reader);
            break;
          }
        case 'a':
          {
            readArrayType(expression, reader);
            break;
          }
        case 'O':
          {
            readObjectType(expression, reader);
            break;
          }
        default:
          break;
      }
    }

    private void readIntType(ZendDbgExpression expression, ValueReader reader) {
      String value = reader.readToken();
      expression.setExpressionResult(new ZendDbgExpressionResult(value, PHP_INT));
    }

    private void readFloatType(ZendDbgExpression expression, ValueReader reader) {
      String value = reader.readToken();
      expression.setExpressionResult(new ZendDbgExpressionResult(value, PHP_FLOAT));
    }

    private void readSringType(ZendDbgExpression expression, ValueReader reader) {
      String value = reader.readString();
      expression.setExpressionResult(new ZendDbgExpressionResult(value, PHP_STRING));
    }

    private void readBooleanType(ZendDbgExpression expression, ValueReader reader) {
      String value = reader.readToken();
      expression.setExpressionResult(new ZendDbgExpressionResult(value, PHP_BOOL));
    }

    private void readResourceType(ZendDbgExpression expression, ValueReader reader) {
      // Read resource number and move on...
      reader.readInt();
      reader.readInt();
      String value = reader.readToken();
      expression.setExpressionResult(new ZendDbgExpressionResult(value, PHP_RESOURCE));
    }

    private void readArrayType(ZendDbgExpression expression, ValueReader reader) {
      int arrayLength = reader.readInt();
      String arrayDescriptor = "array [" + arrayLength + "]";
      if (reader.isEnd()) {
        expression.setExpressionResult(
            new ZendDbgExpressionResult(arrayDescriptor, PHP_ARRAY, arrayLength));
        return;
      }
      List<IDbgExpression> childExpressions = new ArrayList<>();
      for (int i = 0; i < arrayLength; i++) {
        char type = reader.readType();
        String name;
        if (type == 'i') {
          name = Integer.toString(reader.readInt());
        } else if (type == 's') {
          name = reader.readString();
        } else {
          // Fall back when type is invalid
          return;
        }
        ZendDbgExpression childExpression = expression.createChild(name, KIND_ARRAY_MEMBER);
        childExpressions.add(childExpression);
        read(childExpression, reader);
      }
      expression.setExpressionResult(
          new ZendDbgExpressionResult(arrayDescriptor, PHP_OBJECT, arrayLength, childExpressions));
    }

    private void readObjectType(ZendDbgExpression expression, ValueReader reader) {
      String className = reader.readString();
      int objectLength = reader.readInt();
      if (reader.isEnd()) {
        expression.setExpressionResult(
            new ZendDbgExpressionResult(className, PHP_OBJECT, objectLength));
        return;
      }
      List<IDbgExpression> childExpressions = new ArrayList<>();
      for (int i = 0; i < objectLength; i++) {
        char type = reader.readType();
        String name;
        if (type == 'i') {
          name = Integer.toString(reader.readInt());
        } else if (type == 's') {
          name = reader.readString();
        } else {
          // Fall back when type is invalid
          return;
        }
        ZendDbgExpression childExpression;
        Facet fieldFacet = MOD_PUBLIC;
        if (name.startsWith("*::")) {
          fieldFacet = MOD_PROTECTED;
        } else if (name.contains("::")) {
          fieldFacet = MOD_PRIVATE;
        }
        childExpression = expression.createChild(name, KIND_OBJECT_MEMBER, fieldFacet);
        childExpressions.add(childExpression);
        read(childExpression, reader);
      }
      expression.setExpressionResult(
          new ZendDbgExpressionResult(className, PHP_OBJECT, objectLength, childExpressions));
    }
  }

  private static class ValueReader extends ByteArrayInputStream {

    private ValueReader(byte[] result) {
      super(result);
    }

    char readType() {
      char curr;
      do {
        int temp = super.read();
        if (temp == -1) {
          return ' ';
        }
        curr = (char) temp;
      } while (curr == ';' || curr == ':' || curr == '{' || curr == '}');
      return curr;
    }

    String readToken() {
      StringBuffer buffer = new StringBuffer(6);
      char curr;
      do {
        curr = (char) super.read();
      } while (curr == ';' || curr == ':');
      while (curr != ';' && curr != ':') {
        buffer.append(curr);
        curr = (char) super.read();
      }
      return buffer.toString();
    }

    String readString() {
      int length = readInt();
      while ((char) super.read() != '"') ;
      byte[] bytes = new byte[length];
      read(bytes, 0, length);
      super.read(); // read '"'
      return getText(bytes);
    }

    int readInt() {
      int result = 0;
      char curr;
      boolean isMinus = false;
      do {
        curr = (char) super.read();
        if (curr == '-') {
          isMinus = true;
        }
      } while (!Character.isDigit(curr));
      do {
        result *= 10;
        result += Character.getNumericValue(curr);
        this.mark(1);
      } while (Character.isDigit(curr = (char) super.read()));
      if (isMinus) {
        result *= -1;
      }
      return result;
    }

    boolean isEnd() {
      this.reset();
      char curr = (char) super.read();
      return curr == ';';
    }

    String getText(byte[] buf) {
      try {
        return new String(buf, IDbgMessage.ENCODING);
      } catch (UnsupportedEncodingException e) {
      }
      return new String(buf, Charset.defaultCharset());
    }
  }

  private ZendDbgConnection debugConnection;
  private ValueDecoder valueDecoder;

  public ZendDbgExpressionEvaluator(ZendDbgConnection debugConnection) {
    this.debugConnection = debugConnection;
    this.valueDecoder = new ValueDecoder();
  }

  public void evaluate(ZendDbgExpression expression, int depth) {
    byte[] value = requestEvaluation(expression, depth);
    valueDecoder.deserialize(expression, value);
  }

  public boolean assign(ZendDbgExpression expression, String newValue, int depth) {
    if (!requestAssignment(expression, newValue, depth)) {
      ZendDebugger.LOG.error(
          "Could not assign new value for: " + expression.getExpression() + " variable.");
      return false;
    }
    return true;
  }

  private byte[] requestEvaluation(ZendDbgExpression expression, int depth) {
    String variableOwner = expression.getExpression();
    List<String> variableElementPath = Collections.emptyList();
    List<String> chain = expression.getExpressionChain();
    if (!chain.isEmpty()) {
      variableOwner = chain.get(0);
      variableElementPath = chain.subList(1, chain.size());
    }
    byte[] value = null;
    GetVariableValueResponse response =
        debugConnection.sendRequest(
            new GetVariableValueRequest(variableOwner, depth, variableElementPath));
    if (isOK(response)) {
      value = response.getVariableValue();
    }
    if (value == null) {
      value = new byte[] {'N'};
    }
    return value;
  }

  private boolean requestAssignment(ZendDbgExpression expression, String newValue, int depth) {
    List<String> path = expression.getExpressionChain();
    String variableOwner = path.get(0);
    List<String> variableElementPath = path.subList(1, path.size());
    AssignValueResponse response =
        debugConnection.sendRequest(
            new AssignValueRequest(variableOwner, newValue, depth, variableElementPath));
    return isOK(response);
  }

  private boolean isOK(IDbgEngineResponse response) {
    return response != null && response.getStatus() == 0;
  }
}

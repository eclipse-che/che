/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.variables;

import static zend.com.che.plugin.zdb.server.variables.IDbgDataFacet.Facet.*;
import static zend.com.che.plugin.zdb.server.variables.IDbgDataType.DataType.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import zend.com.che.plugin.zdb.server.connection.IDbgMessage;
import zend.com.che.plugin.zdb.server.connection.ZendDbgConnection;
import zend.com.che.plugin.zdb.server.connection.ZendDbgClientMessages.GetVariableValueRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetVariableValueResponse;
import zend.com.che.plugin.zdb.server.variables.IDbgDataFacet.Facet;

/**
 * Zend debug expressions resolver.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgExpressionResolver {

	private static class ExpressionDecoder {

		public void deserialize(AbstractDbgExpression expression, byte[] value) {
			if (value == null) {
				// the expression is Illeagal.
				value = new byte[] { 'N' };
			}
			build(expression, new ExpressionValueReader(value));
		}

		private void build(AbstractDbgExpression expression, ExpressionValueReader reader) {
			char type = reader.readType();
			switch (type) {
			case 'i': {
				buildIntType(expression, reader);
				break;
			}
			case 'd': {
				buildFloatType(expression, reader);
				break;
			}
			case 's': {
				buildSringType(expression, reader);
				break;
			}
			case 'b': {
				buildBooleanType(expression, reader);
				break;
			}
			case 'r': {
				buildResourceType(expression, reader);
				break;
			}
			case 'a': {
				buildArrayType(expression, reader);
				break;
			}
			case 'O': {
				buildObjectType(expression, reader);
				break;
			}
			default:
				break;
			}
		}

		private void buildIntType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			String value = reader.readToken();
			expression.setDataType(PHP_INT);
			expression.setValue(value);
		}

		private void buildFloatType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			String value = reader.readToken();
			expression.setDataType(PHP_FLOAT);
			expression.setValue(value);
		}

		private void buildSringType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			String value = reader.readString();
			expression.setDataType(PHP_STRING);
			expression.setValue(value);
		}

		private void buildBooleanType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			String value = reader.readToken();
			expression.setDataType(PHP_BOOL);
			expression.setValue(value);
		}

		private void buildResourceType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			// Read resource number and move on...
			reader.readInt();
			reader.readInt();
			String value = reader.readToken();
			expression.setDataType(PHP_RESOURCE);
			expression.setValue(value);
		}

		private void buildArrayType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			int arrayLength = reader.readInt();
			expression.setDataType(PHP_ARRAY);
			expression.setChildrenCount(arrayLength);
			expression.setValue("Array [" + arrayLength + "]");
			if (reader.isLastEnd()) {
				return;
			}
			for (int i = 0; i < arrayLength; i++) {
				char type = reader.readType();
				String name;
				if (type == 'i') {
					name = Integer.toString(reader.readInt());
				} else if (type == 's') {
					name = reader.readString();
				} else {
					// fall back when type is invalid
					expression.setDataType(PHP_NULL);
					expression.setValue(PHP_NULL.getText());
					return;
				}
				AbstractDbgExpression childExpression = expression.createChild(name, KIND_ARRAY_MEMBER);
				build(childExpression, reader);
			}
		}

		private void buildObjectType(AbstractDbgExpression expression, ExpressionValueReader reader) {
			String className = reader.readString();
			int objectLength = reader.readInt();
			expression.setDataType(PHP_OBJECT);
			expression.setChildrenCount(objectLength);
			expression.setValue(className);
			if (reader.isLastEnd()) {
				return;
			}
			for (int i = 0; i < objectLength; i++) {
				char type = reader.readType();
				String name;
				if (type == 'i') {
					name = Integer.toString(reader.readInt());
				} else if (type == 's') {
					name = reader.readString();
				} else {
					// fall back when type is invalid
					expression.setDataType(PHP_NULL);
					expression.setValue(PHP_NULL.getText());
					return;
				}
				AbstractDbgExpression childExpression;
				Facet fieldFacet = MOD_PUBLIC;
				if (name.startsWith("*::")) {
					fieldFacet = MOD_PROTECTED;
				} else if (name.contains("::")) {
					fieldFacet = MOD_PRIVATE;
				}
				childExpression = expression.createChild(name, KIND_OBJECT_MEMBER, fieldFacet);
				build(childExpression, reader);
			}
		}

	}

	private static class ExpressionValueReader extends ByteArrayInputStream {

		private ExpressionValueReader(byte[] result) {
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
			while ((char) super.read() != '"')
				;
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

		boolean isLastEnd() {
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
	private ExpressionDecoder expressionDecoder;

	public ZendDbgExpressionResolver(ZendDbgConnection debugConnection) {
		this.debugConnection = debugConnection;
		this.expressionDecoder = new ExpressionDecoder();
	}

	public void resolve(AbstractDbgExpression expression, int depth) {
		if (expression.getDataType() == PHP_VIRTUAL_CLASS)
			return;
		byte[] value = read(expression, depth);
		expressionDecoder.deserialize(expression, value);
	}

	private byte[] requestExpressionValue(String variable, int depth, List<String> parentPath) {
		GetVariableValueResponse response = debugConnection
				.sendRequest(new GetVariableValueRequest(variable, depth, parentPath));
		if (response != null && response.getStatus() == 0) {
			return response.getVariableValue();
		}
		return null;
	}

	private byte[] read(AbstractDbgExpression expression, int depth) {
		byte[] value = null;
		if (expression instanceof IDbgVariable) {
			IDbgVariable variable = (IDbgVariable) expression;
			List<String> path = variable.getPath();
			String variableOwner = path.get(0);
			List<String> variableElementPath = path.subList(1, path.size());
			value = requestExpressionValue(variableOwner, depth, variableElementPath);
		} else {
			value = requestExpressionValue(expression.getStatement(), depth, Collections.emptyList());
		}
		if (value == null) {
			value = new byte[] { 'N' };
		}
		return value;
	}

}
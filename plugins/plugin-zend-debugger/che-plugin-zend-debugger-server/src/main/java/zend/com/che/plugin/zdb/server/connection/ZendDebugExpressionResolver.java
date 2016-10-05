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
package zend.com.che.plugin.zdb.server.connection;

import static zend.com.che.plugin.zdb.server.connection.IDebugDataFacet.Facet.*;
import static zend.com.che.plugin.zdb.server.connection.IDebugDataType.DataType.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.GetVariableValueRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugResponses.GetVariableValueResponse;

/**
 * Zend debug expressions resolver.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugExpressionResolver {

	private static class ExpressionDecoder {

		public ZendDebugExpressionValue deserialize(IDebugExpression expression, byte[] value) {
			if (value == null) {
				// the expression is Illeagal.
				value = new byte[] { 'N' };
			}
			return build(expression, new ExpressionValueReader(value));
		}

		private ZendDebugExpressionValue build(IDebugExpression expression, ExpressionValueReader reader) {
			char type = reader.readType();
			switch (type) {
			case 'i':
				return buildIntType(reader);
			case 'd':
				return buildFloatType(reader);
			case 's':
				return buildSringType(reader);
			case 'b':
				return buildBooleanType(reader);
			case 'r':
				return buildResourceType(reader);
			case 'a':
				return buildArrayType(expression, reader);
			case 'O':
				return buildObjectType(expression, reader);
			}
			return ZendDebugExpressionValue.NULL_VALUE;
		}

		private ZendDebugExpressionValue buildIntType(ExpressionValueReader reader) {
			String value = reader.readToken();
			return new ZendDebugExpressionValue(PHP_INT, value, null);
		}

		private ZendDebugExpressionValue buildFloatType(ExpressionValueReader reader) {
			String value = reader.readToken();
			return new ZendDebugExpressionValue(PHP_FLOAT, value, null);
		}

		private ZendDebugExpressionValue buildSringType(ExpressionValueReader reader) {
			String value = reader.readString();
			return new ZendDebugExpressionValue(PHP_STRING, value, null);
		}

		private ZendDebugExpressionValue buildBooleanType(ExpressionValueReader reader) {
			String value = reader.readToken();
			return new ZendDebugExpressionValue(PHP_BOOL, value, null);
		}

		private ZendDebugExpressionValue buildResourceType(ExpressionValueReader reader) {
			// Read resource number and move on...
			reader.readInt();
			reader.readInt();
			String value = reader.readToken();
			return new ZendDebugExpressionValue(PHP_RESOURCE, value, null);
		}

		private ZendDebugExpressionValue buildArrayType(IDebugExpression expression, ExpressionValueReader reader) {
			int arrayLength = reader.readInt();
			int originalLength = arrayLength;
			if (reader.isLastEnd()) {
				arrayLength = 0;
			}
			List<IDebugExpression> childNodes = new ArrayList<>(arrayLength);
			for (int i = 0; i < arrayLength; i++) {
				char type = reader.readType();
				String name;
				if (type == 'i') {
					name = Integer.toString(reader.readInt());
				} else if (type == 's') {
					name = reader.readString();
				} else {
					// fall back when type is invalid
					return ZendDebugExpressionValue.NULL_VALUE;
				}
				if (expression == null) {
					childNodes.add(createDefaultVariable(name));
				} else {
					childNodes.add(expression.createChildExpression(name, '[' + name + ']', KIND_ARRAY_MEMBER));
				}
				childNodes.get(i).setValue(build(expression, reader));
			}
			return new ZendDebugExpressionValue(PHP_ARRAY, "Array [" + originalLength + ']', childNodes,
					originalLength);
		}

		private ZendDebugExpressionValue buildObjectType(IDebugExpression expression, ExpressionValueReader reader) {
			String className = reader.readString();
			int objectLength = reader.readInt();
			int originalLength = objectLength;
			if (reader.isLastEnd()) {
				objectLength = 0;
			}
			List<IDebugExpression> childNodes = new ArrayList<>(objectLength);
			for (int i = 0; i < objectLength; i++) {
				char type = reader.readType();
				// System.out.println("type " + type);
				String name;
				if (type == 'i') {
					name = Integer.toString(reader.readInt());
				} else if (type == 's') {
					name = reader.readString();
				} else {
					// fall back when type is invalid
					return ZendDebugExpressionValue.NULL_VALUE;
				}
				if (expression == null) {
					childNodes.add(createDefaultVariable(name));
				} else {
					childNodes.add(expression.createChildExpression(name, "->" + name, KIND_OBJECT_MEMBER)); //$NON-NLS-1$
				}
				childNodes.get(i).setValue(build(expression, reader));
			}
			return new ZendDebugExpressionValue(PHP_OBJECT, className, childNodes, originalLength);
		}

		private IDebugExpression createDefaultVariable(String name) {
			return new ZendDebugExpression('$' + name);
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
			while ((char) super.read() != '"');
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
				return new String(buf, ZendDebugConnection.ENCODING);
			} catch (UnsupportedEncodingException e) {
			}
			return new String(buf, Charset.defaultCharset());
		}

	}

	private ZendDebugConnection debugConnection;
	private ExpressionDecoder expressionDecoder;

	/**
	 * Creates new DefaultExpressionsManager
	 */
	public ZendDebugExpressionResolver(ZendDebugConnection debugConnection) {
		this.debugConnection = debugConnection;
		this.expressionDecoder = new ExpressionDecoder();
	}

	public void resolve(IDebugExpression expression, int depth) {
		if (expression.getValue().getDataType() == PHP_VIRTUAL_CLASS)
			return;
		byte[] value = read(expression, depth);
		ZendDebugExpressionValue expressionValue = expressionDecoder.deserialize(expression, value);
		expression.setValue(expressionValue);
	}

	private byte[] requestVariableValue(String variable, int depth, String[] parentPath) {
		GetVariableValueRequest request = (GetVariableValueRequest) ZendDebugMessageFactory
				.create(IDebugMessageType.REQUEST_GET_VARIABLE_VALUE);
		request.setVar(variable);
		request.setDepth(depth);
		request.setPath(parentPath);
		GetVariableValueResponse response = null;
		response = (GetVariableValueResponse) debugConnection.syncRequest(request);
		if (response == null || response.getStatus() != 0) {
			return null;
		}
		return response.getVarResult();
	}

	private byte[] read(IDebugExpression expression, int depth) {
		String[] path = expression.getPath();
		String[] parentPath = new String[path.length - 1];
		System.arraycopy(path, 1, parentPath, 0, path.length - 1);
		String variable = path[0];
		byte[] value = requestVariableValue(variable, depth, parentPath);
		if (value == null) {
			value = new byte[] { 'N' };
		}
		return value;
	}

}
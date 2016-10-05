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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Set of Zend debug responses.
 * 
 * @author Bartlomiej Laczkowski
 */
public final class ZendDebugResponses {

	private ZendDebugResponses() {
	}

	public static class AddBreakpointResponse extends AbstractDebugResponse {

		private int breakPointID;

		public void setBreakpointID(int breakPointID) {
			this.breakPointID = breakPointID;
		}

		public int getBreakpointID() {
			return breakPointID;
		}

		@Override
		public int getType() {
			return RESPONSE_ADD_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
			out.writeInt(getBreakpointID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
			setBreakpointID(in.readInt());
		}
	}

	public static class AddFilesResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_ADD_FILES;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class AssignValueResponse extends AbstractDebugResponse {

		private String variableValue = null;

		@Override
		public int getType() {
			return RESPONSE_ASSIGN_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}

		public void setVarResult(String varResult) {
			variableValue = varResult;
		}

		public String getVarResult() {
			return variableValue;
		}
	}

	public static class CancelAllBreakpointsResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_CANCEL_ALL_BREAKPOINTS;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class CancelBreakpointResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_CANCEL_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class EvalResponse extends AbstractDebugResponse {

		private String result;

		@Override
		public int getType() {
			return RESPONSE_EVAL;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
			ZendConnectionUtils.writeString(out, getResult());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
			setResult(ZendConnectionUtils.readString(in));
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getResult() {
			return result;
		}
	}

	public static class GetCallStackResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_GET_CALL_STACK;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			// Just read data for now and do nothing with it...
			int depth = in.readInt();
			for (int i = 0; i < depth; i++) {
				ZendConnectionUtils.readString(in);
				in.readInt();
				ZendConnectionUtils.readString(in);
				ZendConnectionUtils.readString(in);
				in.readInt();
				ZendConnectionUtils.readString(in);
				int params = in.readInt();
				for (int j = 0; j < params; j++) {
					ZendConnectionUtils.readEncodedString(in, getTransferEncoding());
					ZendConnectionUtils.readStringAsBytes(in);
				}
			}
		}
	}

	public static class GetCWDResponse extends AbstractDebugResponse {

		private String cwd;

		@Override
		public int getType() {
			return RESPONSE_GET_CWD;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
			ZendConnectionUtils.writeString(out, getCWD());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
			setCWD(ZendConnectionUtils.readString(in));
		}

		public void setCWD(String cwd) {
			this.cwd = cwd;
		}

		public String getCWD() {
			return cwd;
		}
	}

	public static class GetStackVariableValueResponse extends AbstractDebugResponse {

		private byte[] varResult;

		@Override
		public int getType() {
			return RESPONSE_GET_STACK_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
			ZendConnectionUtils.writeStringAsBytes(out, getVarResult());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
			setVarResult(ZendConnectionUtils.readStringAsBytes(in));
		}

		public void setVarResult(byte[] varResult) {
			this.varResult = varResult;
		}

		public byte[] getVarResult() {
			return varResult;
		}
	}

	public static class GetVariableValueResponse extends AbstractDebugResponse {

		private byte[] variableValue = null;

		@Override
		public int getType() {
			return RESPONSE_GET_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
			ZendConnectionUtils.writeStringAsBytes(out, getVarResult());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
			setVarResult(ZendConnectionUtils.readStringAsBytes(in));
		}

		public void setVarResult(byte[] varResult) {
			variableValue = varResult;
		}

		public byte[] getVarResult() {
			return variableValue;
		}
	}

	public static class GoResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_GO;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class PauseDebuggerResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_PAUSE_DEBUGGER;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class SetProtocolResponse extends AbstractDebugResponse {

		private int fProtocolID;

		@Override
		public int getType() {
			return RESPONSE_SET_PROTOCOL;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getProtocolID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setProtocolID(in.readInt());
		}

		public void setProtocolID(int protocolID) {
			fProtocolID = protocolID;
		}

		public int getProtocolID() {
			return fProtocolID;
		}
	}

	public static class StartResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_START;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class StepIntoResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_INTO;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class StepOutResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_OUT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class StepOverResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_OVER;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}
	
	public static class CloseSessionResponse extends AbstractDebugResponse {

		@Override
		public int getType() {
			return RESPONSE_CLOSE_SESSION;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getStatus());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setStatus(in.readInt());
		}
	}

	public static class UnknownMessageResponse extends AbstractDebugResponse {

		private int origMessageType;

		@Override
		public int getType() {
			return RESPONSE_UNKNOWN;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getOriginalMessageType());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setOriginalMessageType(in.readInt());
		}

		public int getOriginalMessageType() {
			return origMessageType;
		}

		private void setOriginalMessageType(int origMessageType) {
			this.origMessageType = origMessageType;
		}
	}

}

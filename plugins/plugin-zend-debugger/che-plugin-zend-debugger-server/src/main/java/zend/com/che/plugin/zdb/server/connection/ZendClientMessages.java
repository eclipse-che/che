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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.AddBreakpointResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.AddFilesResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.AssignValueResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.DeleteAllBreakpointsResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.DeleteBreakpointResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.CloseSessionResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.EvalResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.GetCWDResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.GetCallStackResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.GetStackVariableValueResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.GetVariableValueResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.GoResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.PauseDebuggerResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.SetProtocolResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.StartResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.StepIntoResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.StepOutResponse;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.StepOverResponse;

/**
 * Zend debug client messages container.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendClientMessages {

	public static final int NOTIFICATION_CONTINUE_PROCESS_FILE = 2010;

	public static final int REQUEST_START = 1;
	public static final int REQUEST_PAUSE_DEBUGGER = 2;
	public static final int REQUEST_CLOSE_SESSION = 3;
	public static final int REQUEST_STEP_INTO = 11;
	public static final int REQUEST_STEP_OVER = 12;
	public static final int REQUEST_STEP_OUT = 13;
	public static final int REQUEST_GO = 14;
	public static final int REQUEST_ADD_BREAKPOINT = 21;
	public static final int REQUEST_DELETE_BREAKPOINT = 22;
	public static final int REQUEST_DELETE_ALL_BREAKPOINTS = 23;
	public static final int REQUEST_EVAL = 31;
	public static final int REQUEST_GET_VARIABLE_VALUE = 32;
	public static final int REQUEST_ASSIGN_VALUE = 33;
	public static final int REQUEST_GET_CALL_STACK = 34;
	public static final int REQUEST_GET_STACK_VARIABLE_VALUE = 35;
	public static final int REQUEST_GET_CWD = 36;
	public static final int REQUEST_ADD_FILES = 38;
	public static final int REQUEST_SET_PROTOCOL = 10000;

	private static abstract class AbstractClientRequest<T extends IDebugEngineResponse> extends AbstractMessage
			implements IDebugClientRequest<T> {

		private int id;

		@Override
		public void setID(int id) {
			this.id = id;
		}

		@Override
		public int getID() {
			return this.id;
		}
		
		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

	}

	private ZendClientMessages() {
	}

	// Client notifications

	public static class ContinueProcessFileNotification extends AbstractMessage implements IDebugClientNotification {

		@Override
		public int getType() {
			return NOTIFICATION_CONTINUE_PROCESS_FILE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
		}
	}

	// Client requests

	public static class AddBreakpointRequest extends AbstractClientRequest<AddBreakpointResponse> {

		private int kind;
		private int lifeTime;
		private int lineNumber;
		private String fileName;

		public AddBreakpointRequest(int kind, int lifeTime, int line, String fileName) {
			this.kind = kind;
			this.lifeTime = lifeTime;
			this.lineNumber = line;
			this.fileName = fileName;
		}

		@Override
		public int getType() {
			return REQUEST_ADD_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			out.writeShort(kind);
			out.writeShort(lifeTime);
			ZendConnectionUtils.writeString(out, fileName);
			out.writeInt(lineNumber);
		}
	}

	public static class AddFilesRequest extends AbstractClientRequest<AddFilesResponse> {

		private Set<String> paths;

		public AddFilesRequest(Set<String> paths) {
			this.paths = paths;
		}

		@Override
		public int getType() {
			return REQUEST_ADD_FILES;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			out.writeInt(paths.size());
			for (String path : paths) {
				ZendConnectionUtils.writeString(out, path);
			}
		}
	}

	public static class AssignValueRequest extends AbstractClientRequest<AssignValueResponse> {

		private String var;
		private String value;
		private int depth;
		private List<String> path;

		public AssignValueRequest(String var, String value, int depth, List<String> path) {
			this.var = var;
			this.value = value;
			this.depth = depth;
			this.path = path;
		}

		@Override
		public int getType() {
			return REQUEST_ASSIGN_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			ZendConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
			ZendConnectionUtils.writeEncodedString(out, value, getTransferEncoding());
			out.writeInt(depth);
			out.writeInt(path.size());
			for (String p : path) {
				ZendConnectionUtils.writeString(out, p);
			}
		}
	}

	public static class DeleteAllBreakpointsRequest extends AbstractClientRequest<DeleteAllBreakpointsResponse> {

		@Override
		public int getType() {
			return REQUEST_DELETE_ALL_BREAKPOINTS;
		}
	}

	public static class DeleteBreakpointRequest extends AbstractClientRequest<DeleteBreakpointResponse> {

		private int breakpointId;

		public DeleteBreakpointRequest(int breakpointId) {
			this.breakpointId = breakpointId;
		}

		@Override
		public int getType() {
			return REQUEST_DELETE_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			out.writeInt(breakpointId);
		}
	}

	public static class EvalRequest extends AbstractClientRequest<EvalResponse> {

		private String command;
		
		public EvalRequest(String command) {
			this.command = command;
		}

		@Override
		public int getType() {
			return REQUEST_EVAL;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			ZendConnectionUtils.writeEncodedString(out, command, getTransferEncoding());
		}
	}

	public static class GetCallStackRequest extends AbstractClientRequest<GetCallStackResponse> {

		@Override
		public int getType() {
			return REQUEST_GET_CALL_STACK;
		}
	}

	public static class GetCWDRequest extends AbstractClientRequest<GetCWDResponse> {

		@Override
		public int getType() {
			return REQUEST_GET_CWD;
		}
	}

	public static class GetStackVariableValueRequest extends AbstractClientRequest<GetStackVariableValueResponse> {

		private String var;
		private int depth;
		private int layerDepth;
		private List<String> path;

		public GetStackVariableValueRequest(String var, int depth, int layerDepth, List<String> path) {
			this.var = var;
			this.depth = depth;
			this.layerDepth = layerDepth;
			this.path = path;
		}

		@Override
		public int getType() {
			return REQUEST_GET_STACK_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			out.writeInt(layerDepth);
			ZendConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
			out.writeInt(depth);
			out.writeInt(path.size());
			for (String p : path) {
				ZendConnectionUtils.writeString(out, p);
			}
		}
	}

	public static class GetVariableValueRequest extends AbstractClientRequest<GetVariableValueResponse> {

		private String var;
		private int depth;
		private List<String> path;

		public GetVariableValueRequest(String var, int depth, List<String> path) {
			this.var = var;
			this.depth = depth;
			this.path = path;
		}

		@Override
		public int getType() {
			return REQUEST_GET_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			ZendConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
			out.writeInt(depth);
			out.writeInt(path.size());
			for (String p : path) {
				ZendConnectionUtils.writeString(out, p);
			}
		}
	}

	public static class GoRequest extends AbstractClientRequest<GoResponse> {

		@Override
		public int getType() {
			return REQUEST_GO;
		}
	}

	public static class PauseDebuggerRequest extends AbstractClientRequest<PauseDebuggerResponse> {

		@Override
		public int getType() {
			return REQUEST_PAUSE_DEBUGGER;
		}
	}

	public static class SetProtocolRequest extends AbstractClientRequest<SetProtocolResponse> {

		private int protocolID;

		public SetProtocolRequest(int protocolID) {
			this.protocolID = protocolID;
		}

		@Override
		public int getType() {
			return REQUEST_SET_PROTOCOL;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			super.serialize(out);
			out.writeInt(protocolID);
		}
	}

	public static class StartRequest extends AbstractClientRequest<StartResponse> {

		@Override
		public int getType() {
			return REQUEST_START;
		}
	}

	public static class StepIntoRequest extends AbstractClientRequest<StepIntoResponse> {

		@Override
		public int getType() {
			return REQUEST_STEP_INTO;
		}
	}

	public static class StepOutRequest extends AbstractClientRequest<StepOutResponse> {

		@Override
		public int getType() {
			return REQUEST_STEP_OUT;
		}
	}

	public static class StepOverRequest extends AbstractClientRequest<StepOverResponse> {

		@Override
		public int getType() {
			return REQUEST_STEP_OVER;
		}
	}

	public static class CloseSessionRequest extends AbstractClientRequest<CloseSessionResponse> {

		@Override
		public int getType() {
			return REQUEST_CLOSE_SESSION;
		}
	}

}

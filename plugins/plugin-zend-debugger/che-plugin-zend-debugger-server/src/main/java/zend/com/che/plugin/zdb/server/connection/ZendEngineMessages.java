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
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Zend debug engine messages container.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendEngineMessages {

	public static final int NOTIFICATION_SRIPT_ENDED = 2002;
	public static final int NOTIFICATION_READY = 2003;
	public static final int NOTIFICATION_OUTPUT = 2004;
	public static final int NOTIFICATION_SESSION_STARTED = 2005;
	public static final int NOTIFICATION_PARSING_ERROR = 2006;
	public static final int NOTIFICATION_DEBUGGER_ERROR = 2007;
	public static final int NOTIFICATION_HEADER_OUTPUT = 2008;
	public static final int NOTIFICATION_START_PROCESS_FILE = 2009;
	public static final int NOTIFICATION_INI_ALTERED = 2011;
	public static final int NOTIFICATION_CLOSE_MESSAGE_HANDLER = 0;
	
	public static final int RESPONSE_START = 1001;
	public static final int RESPONSE_PAUSE_DEBUGGER = 1002;
	public static final int RESPONSE_CLOSE_SESSION = 1003;
	public static final int RESPONSE_STEP_INTO = 1011;
	public static final int RESPONSE_STEP_OVER = 1012;
	public static final int RESPONSE_STEP_OUT = 1013;
	public static final int RESPONSE_GO = 1014;
	public static final int RESPONSE_ADD_BREAKPOINT = 1021;
	public static final int RESPONSE_DELETE_BREAKPOINT = 1022;
	public static final int RESPONSE_DELETE_ALL_BREAKPOINTS = 1023;
	public static final int RESPONSE_EVAL = 1031;
	public static final int RESPONSE_GET_VARIABLE_VALUE = 1032;
	public static final int RESPONSE_ASSIGN_VALUE = 1033;
	public static final int RESPONSE_GET_CALL_STACK = 1034;
	public static final int RESPONSE_GET_STACK_VARIABLE_VALUE = 1035;
	public static final int RESPONSE_GET_CWD = 1036;
	public static final int RESPONSE_ADD_FILES = 1038;
	public static final int RESPONSE_SET_PROTOCOL = 11000;
	public static final int RESPONSE_UNKNOWN = 1000;
	
	private static abstract class AbstractEngineResponse extends AbstractMessage implements IDebugEngineResponse {
	
		protected int id;
		protected int status;
	
		@Override
		public int getID() {
			return this.id;
		}
	
		@Override
		public int getStatus() {
			return status;
		}
		
		@Override
		public void deserialize(DataInputStream in) throws IOException {
			id = in.readInt();
			status = in.readInt();
		}
	}

	private ZendEngineMessages() {
	}
	
	// Engine notifications

	public static class DebuggerErrorNotification extends AbstractMessage implements IDebugEngineNotification {

		private int errorLevel = 0;
		private String errorText;

		@Override
		public int getType() {
			return NOTIFICATION_DEBUGGER_ERROR;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			errorLevel = in.readInt();
			errorText = ZendConnectionUtils.readString(in);
		}

		public int getErrorLevel() {
			return errorLevel;
		}

		public String getErrorText() {
			return errorText;
		}
	}

	public static class ScriptEndedNotification extends AbstractMessage implements IDebugEngineNotification {

		private int status;

		@Override
		public int getType() {
			return NOTIFICATION_SRIPT_ENDED;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			status = in.readInt();
		}

		public int getStatus() {
			return status;
		}
	}

	public static class SessionStartedNotification extends AbstractMessage implements IDebugEngineNotification {

		private String fileName = "";
		private String uri = "";
		private String query = "";
		private String additionalOptions = "";
		private int protocolID;

		@Override
		public int getType() {
			return NOTIFICATION_SESSION_STARTED;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			protocolID = in.readInt();
			fileName = ZendConnectionUtils.readString(in);
			uri = ZendConnectionUtils.readString(in);
			query = URLDecoder.decode(ZendConnectionUtils.readString(in), "UTF-8");
			additionalOptions = ZendConnectionUtils.readString(in);
		}

		public String getFileName() {
			return fileName;
		}

		public String getUri() {
			return uri;
		}

		public String getQuery() {
			return query;
		}

		public String getOptions() {
			return additionalOptions;
		}

		public int getServerProtocolID() {
			return protocolID;
		}
	}

	public static class HeaderOutputNotification extends AbstractMessage implements IDebugEngineNotification {

		private String output;

		@Override
		public int getType() {
			return NOTIFICATION_HEADER_OUTPUT;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			output = ZendConnectionUtils.readString(in);
		}

		public String getOutput() {
			return this.output;
		}
	}

	public static class IniAlteredNotification extends AbstractMessage implements IDebugEngineNotification {

		private String name;
		private String oldValue;
		private String newValue;

		@Override
		public int getType() {
			return NOTIFICATION_INI_ALTERED;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			name = ZendConnectionUtils.readString(in);
			oldValue = ZendConnectionUtils.readString(in);
			newValue = ZendConnectionUtils.readString(in);
		}

		public String getName() {
			return name;
		}

		public String getOldValue() {
			return oldValue;
		}

		public String getNewValue() {
			return newValue;
		}
	}

	public static class OutputNotification extends AbstractMessage implements IDebugEngineNotification {

		private String output = null;

		@Override
		public int getType() {
			return NOTIFICATION_OUTPUT;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			output = ZendConnectionUtils.readEncodedString(in, getTransferEncoding());
		}

		public String getOutput() {
			return output;
		}
	}

	public static class ParsingErrorNotification extends AbstractMessage implements IDebugEngineNotification {

		private int errorLevel = 0;
		private String fileName;
		private int lineNumber;
		private String errorText;

		@Override
		public int getType() {
			return NOTIFICATION_PARSING_ERROR;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			errorLevel = in.readInt();
			fileName = ZendConnectionUtils.readString(in);
			lineNumber = in.readInt();
			errorText = ZendConnectionUtils.readString(in);
		}

		public int getErrorLevel() {
			return this.errorLevel;
		}

		public String getErrorText() {
			return this.errorText;
		}

		public String getFileName() {
			return this.fileName;
		}

		public int getLineNumber() {
			return this.lineNumber;
		}
	}

	public static class ReadyNotification extends AbstractMessage implements IDebugEngineNotification {

		private String fileName;
		private int lineNumber;

		@Override
		public int getType() {
			return NOTIFICATION_READY;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			fileName  = ZendConnectionUtils.readString(in);
			lineNumber = in.readInt();
			in.readInt(); // Read the 4 bytes of the watched-list length. this is 0 now.
		}

		public String getFileName() {
			return fileName;
		}

		public int getLineNumber() {
			return lineNumber;
		}
	}

	public static class StartProcessFileNotification extends AbstractMessage implements IDebugEngineNotification {

		private String fileName;

		@Override
		public int getType() {
			return NOTIFICATION_START_PROCESS_FILE;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			fileName = ZendConnectionUtils.readString(in);
		}

		public String getFileName() {
			return fileName;
		}
	}
	
	// Phantom message used to notify that connection was closed
	public static class CloseMessageHandlerNotification extends AbstractMessage implements IDebugEngineNotification {

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			// dummy
		}

		@Override
		public int getType() {
			return NOTIFICATION_CLOSE_MESSAGE_HANDLER;
		}
	}

	// Engine responses

	public static class AddBreakpointResponse extends AbstractEngineResponse {

		private int breakPointID;

		public int getBreakpointID() {
			return breakPointID;
		}

		@Override
		public int getType() {
			return RESPONSE_ADD_BREAKPOINT;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			super.deserialize(in);
			breakPointID = in.readInt();
		}
	}

	public static class AddFilesResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_ADD_FILES;
		}
	}

	public static class AssignValueResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_ASSIGN_VALUE;
		}
	}

	public static class DeleteAllBreakpointsResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_DELETE_ALL_BREAKPOINTS;
		}
	}

	public static class DeleteBreakpointResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_DELETE_BREAKPOINT;
		}
	}

	public static class EvalResponse extends AbstractEngineResponse {

		private String result;

		@Override
		public int getType() {
			return RESPONSE_EVAL;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			super.deserialize(in);
			result = ZendConnectionUtils.readString(in);
		}

		public String getResult() {
			return result;
		}
	}

	public static class GetCallStackResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_GET_CALL_STACK;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			id = in.readInt();
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

	public static class GetCWDResponse extends AbstractEngineResponse {

		private String cwd;

		@Override
		public int getType() {
			return RESPONSE_GET_CWD;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			super.deserialize(in);
			cwd = ZendConnectionUtils.readString(in);
		}

		public String getCWD() {
			return cwd;
		}
	}

	public static class GetStackVariableValueResponse extends AbstractEngineResponse {

		private byte[] varResult;

		@Override
		public int getType() {
			return RESPONSE_GET_STACK_VARIABLE_VALUE;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			super.deserialize(in);
			varResult = ZendConnectionUtils.readStringAsBytes(in);
		}

		public byte[] getVarResult() {
			return varResult;
		}
	}

	public static class GetVariableValueResponse extends AbstractEngineResponse {

		private byte[] variableValue = null;

		@Override
		public int getType() {
			return RESPONSE_GET_VARIABLE_VALUE;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			super.deserialize(in);
			variableValue = ZendConnectionUtils.readStringAsBytes(in);
		}

		public byte[] getVariableValue() {
			return variableValue;
		}
	}

	public static class GoResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_GO;
		}
	}

	public static class PauseDebuggerResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_PAUSE_DEBUGGER;
		}
	}

	public static class SetProtocolResponse extends AbstractEngineResponse {

		private int protocolID;

		@Override
		public int getType() {
			return RESPONSE_SET_PROTOCOL;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			id = in.readInt();
			protocolID = in.readInt();
		}

		public int getProtocolID() {
			return protocolID;
		}
	}

	public static class StartResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_START;
		}
	}

	public static class StepIntoResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_INTO;
		}
	}

	public static class StepOutResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_OUT;
		}
	}

	public static class StepOverResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_STEP_OVER;
		}
	}

	public static class CloseSessionResponse extends AbstractEngineResponse {

		@Override
		public int getType() {
			return RESPONSE_CLOSE_SESSION;
		}
	}

	public static class UnknownMessageResponse extends AbstractEngineResponse {

		private int origint;

		@Override
		public int getType() {
			return RESPONSE_UNKNOWN;
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			id = in.readInt();
			origint = in.readInt();
		}

		public int getOriginalInt() {
			return origint;
		}
	}
	
	public static IDebugEngineMessage create(int type) {
		switch (type) {
		// Engine notifications
		case NOTIFICATION_DEBUGGER_ERROR:
			return new DebuggerErrorNotification();
		case NOTIFICATION_HEADER_OUTPUT:
			return new HeaderOutputNotification();
		case NOTIFICATION_INI_ALTERED:
			return new IniAlteredNotification();
		case NOTIFICATION_OUTPUT:
			return new OutputNotification();
		case NOTIFICATION_PARSING_ERROR:
			return new ParsingErrorNotification();
		case NOTIFICATION_READY:
			return new ReadyNotification();
		case NOTIFICATION_SESSION_STARTED:
			return new SessionStartedNotification();
		case NOTIFICATION_SRIPT_ENDED:
			return new ScriptEndedNotification();
		case NOTIFICATION_START_PROCESS_FILE:
			return new StartProcessFileNotification();
		// Engine responses
		case RESPONSE_ADD_BREAKPOINT:
			return new AddBreakpointResponse();
		case RESPONSE_ADD_FILES:
			return new AddFilesResponse();
		case RESPONSE_ASSIGN_VALUE:
			return new AssignValueResponse();
		case RESPONSE_DELETE_ALL_BREAKPOINTS:
			return new DeleteAllBreakpointsResponse();
		case RESPONSE_DELETE_BREAKPOINT:
			return new DeleteBreakpointResponse();
		case RESPONSE_EVAL:
			return new EvalResponse();
		case RESPONSE_GET_CALL_STACK:
			return new GetCallStackResponse();
		case RESPONSE_GET_CWD:
			return new GetCWDResponse();
		case RESPONSE_GET_STACK_VARIABLE_VALUE:
			return new GetStackVariableValueResponse();
		case RESPONSE_GET_VARIABLE_VALUE:
			return new GetVariableValueResponse();
		case RESPONSE_GO:
			return new GoResponse();
		case RESPONSE_PAUSE_DEBUGGER:
			return new PauseDebuggerResponse();
		case RESPONSE_SET_PROTOCOL:
			return new SetProtocolResponse();
		case RESPONSE_START:
			return new StartResponse();
		case RESPONSE_STEP_INTO:
			return new StepIntoResponse();
		case RESPONSE_STEP_OUT:
			return new StepOutResponse();
		case RESPONSE_STEP_OVER:
			return new StepOverResponse();
		case RESPONSE_CLOSE_SESSION:
			return new CloseSessionResponse();
		}
		return null;
	}

}

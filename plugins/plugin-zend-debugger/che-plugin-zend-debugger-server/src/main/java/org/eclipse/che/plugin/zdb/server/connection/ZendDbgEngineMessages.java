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
package org.eclipse.che.plugin.zdb.server.connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.GetLocalFileContentResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientResponse;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgConnectionUtils;

/**
 * Zend debug engine messages container.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgEngineMessages {

  // Notification types
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
  // Response types
  public static final int RESPONSE_START = 1001;
  public static final int RESPONSE_PAUSE_DEBUGGER = 1002;
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
  // Request types
  public static final int REQUEST_GET_LOCAL_FILE_CONTENT = 10002;

  public interface IDbgEngineMessage extends IDbgMessage {

    /**
     * De-serialize this debug message from an input stream
     *
     * @param in input stream this message is going to be read from
     */
    public void deserialize(DataInputStream in) throws IOException;
  }

  public interface IDbgEngineNotification extends IDbgEngineMessage {}

  public interface IDbgEngineRequest<T extends IDbgClientResponse> extends IDbgEngineMessage {

    /** Return the request id. */
    public int getID();
  }

  public interface IDbgEngineResponse extends IDbgEngineMessage {

    /** Return the response id. */
    public int getID();

    /** Return the response status. */
    public int getStatus();
  }

  private abstract static class AbstractEngineRequest<T extends IDbgClientResponse>
      extends AbstractDbgMessage implements IDbgEngineRequest<T> {

    private int id;

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      id = in.readInt();
    }

    @Override
    public int getID() {
      return id;
    }
  }

  private abstract static class AbstractEngineResponse extends AbstractDbgMessage
      implements IDbgEngineResponse {

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

  private ZendDbgEngineMessages() {}

  // Engine notifications

  public static class DebuggerErrorNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private int errorLevel = 0;
    private String errorText;

    @Override
    public int getType() {
      return NOTIFICATION_DEBUGGER_ERROR;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      errorLevel = in.readInt();
      errorText = ZendDbgConnectionUtils.readString(in);
    }

    public int getErrorLevel() {
      return errorLevel;
    }

    public String getErrorText() {
      return errorText;
    }
  }

  public static class ScriptEndedNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

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

  public static class SessionStartedNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

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
      fileName = ZendDbgConnectionUtils.readString(in);
      uri = ZendDbgConnectionUtils.readString(in);
      query = URLDecoder.decode(ZendDbgConnectionUtils.readString(in), "UTF-8");
      additionalOptions = ZendDbgConnectionUtils.readString(in);
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

  public static class HeaderOutputNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private String output;

    @Override
    public int getType() {
      return NOTIFICATION_HEADER_OUTPUT;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      output = ZendDbgConnectionUtils.readString(in);
    }

    public String getOutput() {
      return this.output;
    }
  }

  public static class IniAlteredNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private String name;
    private String oldValue;
    private String newValue;

    @Override
    public int getType() {
      return NOTIFICATION_INI_ALTERED;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      name = ZendDbgConnectionUtils.readString(in);
      oldValue = ZendDbgConnectionUtils.readString(in);
      newValue = ZendDbgConnectionUtils.readString(in);
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

  public static class OutputNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private String output = null;

    @Override
    public int getType() {
      return NOTIFICATION_OUTPUT;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      output = ZendDbgConnectionUtils.readEncodedString(in, getTransferEncoding());
    }

    public String getOutput() {
      return output;
    }
  }

  public static class ParsingErrorNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

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
      fileName = ZendDbgConnectionUtils.readString(in);
      lineNumber = in.readInt();
      errorText = ZendDbgConnectionUtils.readString(in);
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

  public static class ReadyNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private String fileName;
    private int lineNumber;

    @Override
    public int getType() {
      return NOTIFICATION_READY;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      fileName = ZendDbgConnectionUtils.readString(in);
      lineNumber = in.readInt();
      in.readInt(); // Read the 4 bytes of the watched-list length. this
      // is 0 now.
    }

    public String getFileName() {
      return fileName;
    }

    public int getLineNumber() {
      return lineNumber;
    }
  }

  public static class StartProcessFileNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

    private String fileName;

    @Override
    public int getType() {
      return NOTIFICATION_START_PROCESS_FILE;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      fileName = ZendDbgConnectionUtils.readString(in);
    }

    public String getFileName() {
      return fileName;
    }
  }

  // Phantom message used to notify that connection was closed
  public static class CloseMessageHandlerNotification extends AbstractDbgMessage
      implements IDbgEngineNotification {

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
      result = ZendDbgConnectionUtils.readString(in);
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
        ZendDbgConnectionUtils.readString(in);
        in.readInt();
        ZendDbgConnectionUtils.readString(in);
        ZendDbgConnectionUtils.readString(in);
        in.readInt();
        ZendDbgConnectionUtils.readString(in);
        int params = in.readInt();
        for (int j = 0; j < params; j++) {
          ZendDbgConnectionUtils.readEncodedString(in, getTransferEncoding());
          ZendDbgConnectionUtils.readStringAsBytes(in);
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
      cwd = ZendDbgConnectionUtils.readString(in);
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
      varResult = ZendDbgConnectionUtils.readStringAsBytes(in);
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
      variableValue = ZendDbgConnectionUtils.readStringAsBytes(in);
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

  // Engine requests

  public static class GetLocalFileContentRequest
      extends AbstractEngineRequest<GetLocalFileContentResponse> {

    private String fileName;
    private int size;
    private int checkSum;

    @Override
    public int getType() {
      return REQUEST_GET_LOCAL_FILE_CONTENT;
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
      super.deserialize(in);
      fileName = ZendDbgConnectionUtils.readString(in);
      size = in.readInt();
      checkSum = in.readInt();
    }

    public String getFileName() {
      return fileName;
    }

    public int getSize() {
      return size;
    }

    public int getCheckSum() {
      return checkSum;
    }
  }

  public static IDbgEngineMessage create(int type) {
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
        // Engine requests
      case REQUEST_GET_LOCAL_FILE_CONTENT:
        return new GetLocalFileContentRequest();
    }
    return null;
  }
}

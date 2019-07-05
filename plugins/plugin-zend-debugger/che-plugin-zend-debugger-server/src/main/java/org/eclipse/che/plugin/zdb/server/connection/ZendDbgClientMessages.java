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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.AddBreakpointResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.AddFilesResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.AssignValueResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.DeleteAllBreakpointsResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.DeleteBreakpointResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.EvalResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetCWDResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetCallStackResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetStackVariableValueResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetVariableValueResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GoResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.PauseDebuggerResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.SetProtocolResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.StartResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.StepIntoResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.StepOutResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.StepOverResponse;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgConnectionUtils;

/**
 * Zend debug client messages container.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgClientMessages {

  // Notification types
  public static final int NOTIFICATION_CONTINUE_PROCESS_FILE = 2010;
  public static final int NOTIFICATION_CLOSE_SESSION = 3;
  // Request types
  public static final int REQUEST_START = 1;
  public static final int REQUEST_PAUSE_DEBUGGER = 2;
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
  // Response types
  public static final int RESPONSE_GET_LOCAL_FILE_CONTENT = 11001;

  public interface IDbgClientMessage extends IDbgMessage {

    /**
     * Serializes this debug message to an output stream
     *
     * @param out output stream this message is going to be written to
     */
    public void serialize(DataOutputStream out) throws IOException;
  }

  public interface IDbgClientNotification extends IDbgClientMessage {}

  public interface IDbgClientRequest<T extends IDbgEngineResponse> extends IDbgClientMessage {

    /** Set the client request id. */
    public void setID(int id);

    /** Return the client request id. */
    public int getID();
  }

  public interface IDbgClientResponse extends IDbgClientMessage {

    /** Return the engine response id. */
    public int getID();

    /** Return the engine response status. */
    public int getStatus();
  }

  private abstract static class AbstractClientRequest<T extends IDbgEngineResponse>
      extends AbstractDbgMessage implements IDbgClientRequest<T> {

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

  private abstract static class AbstractClientResponse extends AbstractDbgMessage
      implements IDbgClientResponse {

    protected int id;
    protected int status;

    public AbstractClientResponse(int id) {
      this.id = id;
    }

    @Override
    public int getID() {
      return this.id;
    }

    @Override
    public int getStatus() {
      return status;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
      out.writeShort(getType());
      out.writeInt(getID());
      out.writeInt(getStatus());
    }
  }

  private ZendDbgClientMessages() {}

  // Client notifications

  public static class ContinueProcessFileNotification extends AbstractDbgMessage
      implements IDbgClientNotification {

    @Override
    public int getType() {
      return NOTIFICATION_CONTINUE_PROCESS_FILE;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
      out.writeShort(getType());
    }
  }

  public static class CloseSessionNotification extends AbstractDbgMessage
      implements IDbgClientNotification {

    @Override
    public int getType() {
      return NOTIFICATION_CLOSE_SESSION;
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
      ZendDbgConnectionUtils.writeString(out, fileName);
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
        ZendDbgConnectionUtils.writeString(out, path);
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
      ZendDbgConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
      ZendDbgConnectionUtils.writeEncodedString(out, value, getTransferEncoding());
      out.writeInt(depth);
      out.writeInt(path.size());
      for (String p : path) {
        ZendDbgConnectionUtils.writeString(out, p);
      }
    }
  }

  public static class DeleteAllBreakpointsRequest
      extends AbstractClientRequest<DeleteAllBreakpointsResponse> {

    @Override
    public int getType() {
      return REQUEST_DELETE_ALL_BREAKPOINTS;
    }
  }

  public static class DeleteBreakpointRequest
      extends AbstractClientRequest<DeleteBreakpointResponse> {

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
      ZendDbgConnectionUtils.writeEncodedString(out, command, getTransferEncoding());
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

  public static class GetStackVariableValueRequest
      extends AbstractClientRequest<GetStackVariableValueResponse> {

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
      ZendDbgConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
      out.writeInt(depth);
      out.writeInt(path.size());
      for (String p : path) {
        ZendDbgConnectionUtils.writeString(out, p);
      }
    }
  }

  public static class GetVariableValueRequest
      extends AbstractClientRequest<GetVariableValueResponse> {

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
      ZendDbgConnectionUtils.writeEncodedString(out, var, getTransferEncoding());
      out.writeInt(depth);
      out.writeInt(path.size());
      for (String p : path) {
        ZendDbgConnectionUtils.writeString(out, p);
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

  // Client responses

  public static class GetLocalFileContentResponse extends AbstractClientResponse {

    public static final int STATUS_FAILURE = -1;
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FILES_IDENTICAL = 302;

    private byte content[] = null;

    public GetLocalFileContentResponse(int id, int status, byte[] content) {
      super(id);
      this.status = status;
      this.content = content;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
      super.serialize(out);
      if (content != null) {
        out.write(content.length);
        out.write(content);
      } else {
        out.writeInt(0);
      }
    }

    @Override
    public int getType() {
      return RESPONSE_GET_LOCAL_FILE_CONTENT;
    }
  }
}

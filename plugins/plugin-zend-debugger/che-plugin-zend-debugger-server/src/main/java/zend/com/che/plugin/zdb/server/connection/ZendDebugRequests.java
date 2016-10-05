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
 * Set of Zend debug requests.
 * 
 * @author Bartlomiej Laczkowski
 */
public final class ZendDebugRequests {

	private ZendDebugRequests() {
	}

	public static class AddBreakpointRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_ADD_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			// TODO
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			// TODO
		}
	}

	public static class AddFilesRequest extends AbstractDebugRequest {

		private int counter;
		private String[] paths;

		@Override
		public int getType() {
			return REQUEST_ADD_FILES;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			String[] paths = getPaths();
			out.writeInt(paths.length);
			for (int i = 0; i < paths.length; i++) {
				ZendConnectionUtils.writeString(out, paths[i]);
			}
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			int pathSize = in.readInt();
			if (pathSize > 0) {
				String[] paths = new String[pathSize];
				for (int i = 0; i < pathSize; i++) {
					paths[i] = ZendConnectionUtils.readString(in);
				}
				setPaths(paths);
			}
		}

		public void setPaths(String[] paths) {
			this.paths = new String[paths.length];
			System.arraycopy(paths, 0, this.paths, 0, paths.length);
		}

		public void setCounter(int counter) {
			this.counter = counter;
		}

		public String[] getPaths() {
			return paths;
		}

		public int getCounter() {
			return counter;
		}
	}

	public static class AssignValueRequest extends AbstractDebugRequest {

		private String var;
		private String value;
		private int depth;
		private String[] path;

		@Override
		public int getType() {
			return REQUEST_ASSIGN_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			ZendConnectionUtils.writeEncodedString(out, getVar(), getTransferEncoding());
			ZendConnectionUtils.writeEncodedString(out, getValue(), getTransferEncoding());
			out.writeInt(getDepth());
			String[] path = getPath();
			out.writeInt(path.length);
			for (int i = 0; i < path.length; i++) {
				ZendConnectionUtils.writeString(out, path[i]);
			}
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setVar(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
			setValue(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
			setDepth(in.readInt());
			int pathSize = in.readInt();
			if (pathSize > 0) {
				String[] path = new String[pathSize];
				for (int i = 0; i < pathSize; i++) {
					path[i] = ZendConnectionUtils.readString(in);
				}
				setPath(path);
			}
		}

		public void setVar(String var) {
			this.var = var;
		}

		public String getVar() {
			return var;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public int getDepth() {
			return depth;
		}

		public void setPath(String[] path) {
			if (path == null) {
				this.path = new String[0];
				return;
			}
			this.path = new String[path.length];
			System.arraycopy(path, 0, this.path, 0, path.length);
		}

		public String[] getPath() {
			return path;
		}
	}

	public static class CancelAllBreakpointsRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_CANCEL_ALL_BREAKPOINTS;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class CancelBreakpointRequest extends AbstractDebugRequest {

		private int breakpointId;

		@Override
		public int getType() {
			return REQUEST_CANCEL_BREAKPOINT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getBreakpointID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setBreakpointID(in.readInt());
		}

		public void setBreakpointID(int id) {
			this.breakpointId = id;
		}

		public int getBreakpointID() {
			return this.breakpointId;
		}

	}

	public static class EvalRequest extends AbstractDebugRequest {

		private String command;

		@Override
		public int getType() {
			return REQUEST_EVAL;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			ZendConnectionUtils.writeEncodedString(out, getCommand(), getTransferEncoding());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setCommand(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public String getCommand() {
			return command;
		}
	}

	public static class GetCallStackRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_GET_CALL_STACK;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class GetCWDRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return 36;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class GetStackVariableValueRequest extends AbstractDebugRequest {

		private String var;
		private int depth;
		private int layerDepth;
		private String[] path;

		@Override
		public int getType() {
			return REQUEST_GET_STACK_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			out.writeInt(getLayerDepth());
			ZendConnectionUtils.writeEncodedString(out, getVar(), getTransferEncoding());
			out.writeInt(getDepth());
			String[] path = getPath();
			out.writeInt(path.length);
			for (int i = 0; i < path.length; i++) {
				ZendConnectionUtils.writeString(out, path[i]);
			}
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setLayerDepth(in.readInt());
			setVar(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
			setDepth(in.readInt());
			int pathSize = in.readInt();
			String[] path = new String[pathSize];
			for (int i = 0; i < pathSize; i++) {
				path[i] = ZendConnectionUtils.readString(in);
			}
			setPath(path);
		}

		public void setVar(String var) {
			this.var = var;
		}

		public String getVar() {
			return var;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public int getDepth() {
			return depth;
		}

		public void setPath(String[] path) {
			if (path == null) {
				this.path = new String[0];
				return;
			}
			this.path = new String[path.length];
			System.arraycopy(path, 0, this.path, 0, path.length);
		}

		public String[] getPath() {
			return path;
		}

		public void setLayerDepth(int layerDepth) {
			this.layerDepth = layerDepth;
		}

		public int getLayerDepth() {
			return layerDepth;
		}
	}

	public static class GetVariableValueRequest extends AbstractDebugRequest {

		private String var;
		private int depth;
		private String[] path;

		@Override
		public int getType() {
			return REQUEST_GET_VARIABLE_VALUE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
			ZendConnectionUtils.writeEncodedString(out, getVar(), getTransferEncoding());
			out.writeInt(getDepth());
			String[] path = getPath();
			out.writeInt(path.length);
			for (int i = 0; i < path.length; i++) {
				ZendConnectionUtils.writeString(out, path[i]);
			}
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
			setVar(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
			setDepth(in.readInt());
			int pathSize = in.readInt();
			if (pathSize > 0) {
				String[] path = new String[pathSize];
				for (int i = 0; i < pathSize; i++) {
					path[i] = ZendConnectionUtils.readString(in);
				}
				setPath(path);
			}
		}

		public void setVar(String var) {
			this.var = var;
		}

		public String getVar() {
			return var;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public int getDepth() {
			return depth;
		}

		public void setPath(String[] path) {
			if (path == null) {
				this.path = new String[0];
				return;
			}
			this.path = new String[path.length];
			System.arraycopy(path, 0, this.path, 0, path.length);
		}

		public String[] getPath() {
			return path;
		}
	}

	public static class GoRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_GO;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class PauseDebuggerRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_PAUSE_DEBUGGER;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class SetProtocolRequest extends AbstractDebugRequest {

		private int fProtocolID;

		@Override
		public int getType() {
			return REQUEST_SET_PROTOCOL;
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

	public static class StartRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_START;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class StepIntoRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_STEP_INTO;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class StepOutRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_STEP_OUT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}

	public static class StepOverRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_STEP_OVER;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getID());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setID(in.readInt());
		}
	}
	
	public static class ContinueProcessFileRequest extends AbstractDebugRequest {

		@Override
		public void deserialize(DataInputStream in) throws IOException {
		}

		@Override
		public int getType() {
			return REQUEST_CONTINUE_PROCESS_FILE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
		}
	}
	
	public static class CloseSessionRequest extends AbstractDebugRequest {

		@Override
		public int getType() {
			return REQUEST_CLOSE_SESSION;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
		}
	}

}

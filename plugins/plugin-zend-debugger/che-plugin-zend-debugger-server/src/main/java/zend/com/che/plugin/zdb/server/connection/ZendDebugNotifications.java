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
import java.net.URLDecoder;

/**
 * Set of Zend debug notifications.
 * 
 * @author Bartlomiej Laczkowski
 */
public final class ZendDebugNotifications {

	private ZendDebugNotifications() {
	}

	public static class DebuggerErrorNotification extends AbstractDebugNotification {

		private int errorLevel = 0;
		private String errorText;

		@Override
		public int getType() {
			return NOTIFICATION_DEBUGGER_ERROR;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getErrorLevel());
			ZendConnectionUtils.writeString(out, getErrorText());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setErrorLevel(in.readInt());
			setErrorText(ZendConnectionUtils.readString(in));
		}

		public int getErrorLevel() {
			return this.errorLevel;
		}

		public void setErrorLevel(int errorLevel) {
			this.errorLevel = errorLevel;
		}

		public String getErrorText() {
			return this.errorText;
		}

		public void setErrorText(String errorText) {
			this.errorText = errorText;
		}
	}

	public static class ScriptEndedNotification extends AbstractDebugNotification {

		private int status;

		private int protocolID;

		@Override
		public int getType() {
			return NOTIFICATION_SRIPT_ENDED;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setStatus(in.readInt());
		}

		public void setServerProtocol(int serverProtocolID) {
			this.protocolID = serverProtocolID;
		}

		public int getServerProtocolID() {
			return protocolID;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getStatus() {
			return status;
		}
	}

	public static class SessionStartedNotification extends AbstractDebugNotification {

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
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeString(out, getFileName());
			ZendConnectionUtils.writeString(out, getUri());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setServerProtocol(in.readInt());
			setFileName(ZendConnectionUtils.readString(in));
			setUri(ZendConnectionUtils.readString(in));
			setQuery(URLDecoder.decode(ZendConnectionUtils.readString(in), "UTF-8"));
			setOptions(ZendConnectionUtils.readString(in));
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getUri() {
			return uri;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getOptions() {
			return additionalOptions;
		}

		public void setOptions(String options) {
			this.additionalOptions = options;
		}

		public void setServerProtocol(int serverProtocolID) {
			this.protocolID = serverProtocolID;
		}

		public int getServerProtocolID() {
			return protocolID;
		}
	}

	public static class HeaderOutputNotification extends AbstractDebugNotification {

		private String output;

		@Override
		public int getType() {
			return NOTIFICATION_HEADER_OUTPUT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeString(out, getOutput());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setOutput(ZendConnectionUtils.readString(in));
		}

		public String getOutput() {
			return this.output;
		}

		public void setOutput(String outputText) {
			this.output = outputText;
		}
	}

	public static class IniAlteredNotification extends AbstractDebugNotification {

		private String name;
		private String oldValue;
		private String newValue;

		@Override
		public int getType() {
			return NOTIFICATION_INI_ALTERED;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeString(out, getName());
			ZendConnectionUtils.writeString(out, getOldValue());
			ZendConnectionUtils.writeString(out, getNewValue());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setName(ZendConnectionUtils.readString(in));
			setOldValue(ZendConnectionUtils.readString(in));
			setNewValue(ZendConnectionUtils.readString(in));
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOldValue() {
			return oldValue;
		}

		public void setOldValue(String oldValue) {
			this.oldValue = oldValue;
		}

		public String getNewValue() {
			return newValue;
		}

		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}

	}

	public static class OutputNotification extends AbstractDebugNotification {

		private String output = null;

		@Override
		public int getType() {
			return NOTIFICATION_OUTPUT;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeEncodedString(out, getOutput(), getTransferEncoding());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setOutput(ZendConnectionUtils.readEncodedString(in, getTransferEncoding()));
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String outputText) {
			this.output = outputText;
		}
	}

	public static class ParsingErrorNotification extends AbstractDebugNotification {

		private int errorLevel = 0;
		private String fileName;
		private int lineNumber;
		private String errorText;

		@Override
		public int getType() {
			return NOTIFICATION_PARSING_ERROR;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			out.writeInt(getErrorLevel());
			ZendConnectionUtils.writeString(out, getFileName());
			out.writeInt(getLineNumber());
			ZendConnectionUtils.writeString(out, getErrorText());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setErrorLevel(in.readInt());
			setFileName(ZendConnectionUtils.readString(in));
			setLineNumber(in.readInt());
			setErrorText(ZendConnectionUtils.readString(in));
		}

		public int getErrorLevel() {
			return this.errorLevel;
		}

		public void setErrorLevel(int errorLevel) {
			this.errorLevel = errorLevel;
		}

		public String getErrorText() {
			return this.errorText;
		}

		public void setErrorText(String errorText) {
			this.errorText = errorText;
		}

		public String getFileName() {
			return this.fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public int getLineNumber() {
			return this.lineNumber;
		}

		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}
	}

	public static class ReadyNotification extends AbstractDebugNotification {

		private String fileName;
		private int lineNumber;

		@Override
		public int getType() {
			return NOTIFICATION_READY;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeString(out, getFileName());
			out.writeInt(getLineNumber());
			out.writeInt(0);
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setFileName(ZendConnectionUtils.readString(in));
			setLineNumber(in.readInt());
			in.readInt(); // read the 4 bytes of the watched-list length. this
							// is 0
							// now.
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}

		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
		}

		public int getLineNumber() {
			return lineNumber;
		}
	}
	
	public static class StartProcessFileNotification extends AbstractDebugNotification {

		private String fileName;

		@Override
		public int getType() {
			return NOTIFICATION_START_PROCESS_FILE;
		}

		@Override
		public void serialize(DataOutputStream out) throws IOException {
			out.writeShort(getType());
			ZendConnectionUtils.writeString(out, getFileName());
		}

		@Override
		public void deserialize(DataInputStream in) throws IOException {
			setFileName(ZendConnectionUtils.readString(in));
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}
	}

}

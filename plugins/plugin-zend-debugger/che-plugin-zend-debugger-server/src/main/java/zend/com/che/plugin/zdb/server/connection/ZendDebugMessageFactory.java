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

import static zend.com.che.plugin.zdb.server.connection.IDebugMessageType.*;

/**
 * Zend debug message factory.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugMessageFactory {

	public static IDebugMessage create(int type) {
		switch (type) {
		// Notifications
		case NOTIFICATION_DEBUGGER_ERROR:
			return new ZendDebugNotifications.DebuggerErrorNotification();
		case NOTIFICATION_HEADER_OUTPUT:
			return new ZendDebugNotifications.HeaderOutputNotification();
		case NOTIFICATION_INI_ALTERED:
			return new ZendDebugNotifications.IniAlteredNotification();
		case NOTIFICATION_OUTPUT:
			return new ZendDebugNotifications.OutputNotification();
		case NOTIFICATION_PARSING_ERROR:
			return new ZendDebugNotifications.ParsingErrorNotification();
		case NOTIFICATION_READY:
			return new ZendDebugNotifications.ReadyNotification();
		case NOTIFICATION_SESSION_STARTED:
			return new ZendDebugNotifications.SessionStartedNotification();
		case NOTIFICATION_SRIPT_ENDED:
			return new ZendDebugNotifications.ScriptEndedNotification();
		case NOTIFICATION_START_PROCESS_FILE:
			return new ZendDebugNotifications.StartProcessFileNotification();
		// Requests
		case REQUEST_ADD_BREAKPOINT:
			return new ZendDebugRequests.AddBreakpointRequest();
		case REQUEST_ADD_FILES:
			return new ZendDebugRequests.AddFilesRequest();
		case REQUEST_ASSIGN_VALUE:
			return new ZendDebugRequests.AssignValueRequest();
		case REQUEST_CANCEL_ALL_BREAKPOINTS:
			return new ZendDebugRequests.CancelAllBreakpointsRequest();
		case REQUEST_CANCEL_BREAKPOINT:
			return new ZendDebugRequests.CancelBreakpointRequest();
		case REQUEST_EVAL:
			return new ZendDebugRequests.EvalRequest();
		case REQUEST_GET_CALL_STACK:
			return new ZendDebugRequests.GetCallStackRequest();
		case REQUEST_GET_CWD:
			return new ZendDebugRequests.GetCWDRequest();
		case REQUEST_GET_STACK_VARIABLE_VALUE:
			return new ZendDebugRequests.GetStackVariableValueRequest();
		case REQUEST_GET_VARIABLE_VALUE:
			return new ZendDebugRequests.GetVariableValueRequest();
		case REQUEST_GO:
			return new ZendDebugRequests.GoRequest();
		case REQUEST_PAUSE_DEBUGGER:
			return new ZendDebugRequests.PauseDebuggerRequest();
		case REQUEST_SET_PROTOCOL:
			return new ZendDebugRequests.SetProtocolRequest();
		case REQUEST_START:
			return new ZendDebugRequests.StartRequest();
		case REQUEST_STEP_INTO:
			return new ZendDebugRequests.StepIntoRequest();
		case REQUEST_STEP_OUT:
			return new ZendDebugRequests.StepOutRequest();
		case REQUEST_STEP_OVER:
			return new ZendDebugRequests.StepOverRequest();
		case REQUEST_CONTINUE_PROCESS_FILE:
			return new ZendDebugRequests.ContinueProcessFileRequest();
		case REQUEST_CLOSE_SESSION:
			return new ZendDebugRequests.CloseSessionRequest();
		// Responses
		case RESPONSE_ADD_BREAKPOINT:
			return new ZendDebugResponses.AddBreakpointResponse();
		case RESPONSE_ADD_FILES:
			return new ZendDebugResponses.AddFilesResponse();
		case RESPONSE_ASSIGN_VALUE:
			return new ZendDebugResponses.AssignValueResponse();
		case RESPONSE_CANCEL_ALL_BREAKPOINTS:
			return new ZendDebugResponses.CancelAllBreakpointsResponse();
		case RESPONSE_CANCEL_BREAKPOINT:
			return new ZendDebugResponses.CancelBreakpointResponse();
		case RESPONSE_EVAL:
			return new ZendDebugResponses.EvalResponse();
		case RESPONSE_GET_CALL_STACK:
			return new ZendDebugResponses.GetCallStackResponse();
		case RESPONSE_GET_CWD:
			return new ZendDebugResponses.GetCWDResponse();
		case RESPONSE_GET_STACK_VARIABLE_VALUE:
			return new ZendDebugResponses.GetStackVariableValueResponse();
		case RESPONSE_GET_VARIABLE_VALUE:
			return new ZendDebugResponses.GetVariableValueResponse();
		case RESPONSE_PAUSE_DEBUGGER:
			return new ZendDebugResponses.PauseDebuggerResponse();
		case RESPONSE_SET_PROTOCOL:
			return new ZendDebugResponses.SetProtocolResponse();
		case RESPONSE_START:
			return new ZendDebugResponses.StartResponse();
		case RESPONSE_STEP_INTO:
			return new ZendDebugResponses.StepIntoResponse();
		case RESPONSE_STEP_OUT:
			return new ZendDebugResponses.StepOutResponse();
		case RESPONSE_STEP_OVER:
			return new ZendDebugResponses.StepOverResponse();
		case RESPONSE_CLOSE_SESSION:
			return new ZendDebugResponses.CloseSessionResponse();
		}
		return null;
	}

}

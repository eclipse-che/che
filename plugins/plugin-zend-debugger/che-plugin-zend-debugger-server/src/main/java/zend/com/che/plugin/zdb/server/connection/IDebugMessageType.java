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

/**
 * Zend debug message types.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugMessageType {

	public static final int NOTIFICATION_SRIPT_ENDED = 2002;
	public static final int NOTIFICATION_READY = 2003;
	public static final int NOTIFICATION_OUTPUT = 2004;
	public static final int NOTIFICATION_SESSION_STARTED = 2005;
	public static final int NOTIFICATION_PARSING_ERROR = 2006;
	public static final int NOTIFICATION_DEBUGGER_ERROR = 2007;
	public static final int NOTIFICATION_HEADER_OUTPUT = 2008;
	public static final int NOTIFICATION_START_PROCESS_FILE = 2009;
	public static final int NOTIFICATION_INI_ALTERED = 2011;
	
	public static final int REQUEST_START = 1;
	public static final int REQUEST_PAUSE_DEBUGGER = 2;
	public static final int REQUEST_CLOSE_SESSION = 3;
	public static final int REQUEST_STEP_INTO = 11;
	public static final int REQUEST_STEP_OVER = 12;
	public static final int REQUEST_STEP_OUT = 13;
	public static final int REQUEST_GO = 14;
	public static final int REQUEST_ADD_BREAKPOINT = 21;
	public static final int REQUEST_CANCEL_BREAKPOINT = 22;
	public static final int REQUEST_CANCEL_ALL_BREAKPOINTS = 23;
	public static final int REQUEST_EVAL = 31;
	public static final int REQUEST_GET_VARIABLE_VALUE = 32;
	public static final int REQUEST_ASSIGN_VALUE = 33;
	public static final int REQUEST_GET_CALL_STACK = 34;
	public static final int REQUEST_GET_STACK_VARIABLE_VALUE = 35;
	public static final int REQUEST_GET_CWD = 36;
	public static final int REQUEST_ADD_FILES = 38;
	public static final int REQUEST_CONTINUE_PROCESS_FILE = 2010;
	public static final int REQUEST_SET_PROTOCOL = 10000;
	
	public static final int RESPONSE_START = 1001;
	public static final int RESPONSE_PAUSE_DEBUGGER = 1002;
	public static final int RESPONSE_CLOSE_SESSION = 1003;
	public static final int RESPONSE_STEP_INTO = 1011;
	public static final int RESPONSE_STEP_OVER = 1012;
	public static final int RESPONSE_STEP_OUT = 1013;
	public static final int RESPONSE_GO = 1014;
	public static final int RESPONSE_ADD_BREAKPOINT = 1021;
	public static final int RESPONSE_CANCEL_BREAKPOINT = 1022;
	public static final int RESPONSE_CANCEL_ALL_BREAKPOINTS = 1023;
	public static final int RESPONSE_EVAL = 1031;
	public static final int RESPONSE_GET_VARIABLE_VALUE = 1032;
	public static final int RESPONSE_ASSIGN_VALUE = 1033;
	public static final int RESPONSE_GET_CALL_STACK = 1034;
	public static final int RESPONSE_GET_STACK_VARIABLE_VALUE = 1035;
	public static final int RESPONSE_GET_CWD = 1036;
	public static final int RESPONSE_ADD_FILES = 1038;
	public static final int RESPONSE_SET_PROTOCOL = 11000;
	public static final int RESPONSE_UNKNOWN = 1000;
	
}

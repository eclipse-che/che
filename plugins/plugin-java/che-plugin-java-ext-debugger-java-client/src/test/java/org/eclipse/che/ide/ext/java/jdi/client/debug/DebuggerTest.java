/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.common.collect.ImmutableList;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.jdi.client.BaseTest;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeExtension;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolver;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.java.jdi.shared.JavaDebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.Location;
import org.eclipse.che.ide.ext.java.jdi.shared.StackFrameDump;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Value;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;
import org.eclipse.che.ide.ext.java.jdi.shared.VariablePath;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link JavaDebugger} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmytro Nochevnov
 */
public class DebuggerTest extends BaseTest {
    private static final String DEBUG_INFO  = "debug_info";
    private static final String DEBUGGER_ID = "debugger_id";

    public static final int    LINE_NUMBER = 20;
    public static final String FQN         = "org.test.Test";
    public static final String PATH        = "test/src/main/java/Test.java";

    @Mock
    private JavaDebuggerServiceClientImpl service;
    @Mock
    private DtoFactory                    dtoFactory;
    @Mock
    private LocalStorageProvider          localStorageProvider;
    @Mock
    private MessageBusProvider            messageBusProvider;
    @Mock
    private EventBus                      eventBus;
    @Mock
    private AppContext                    appContext;
    @Mock
    private JavaDebuggerFileHandler       javaDebuggerFileHandler;
    @Mock
    private DebuggerManager               debuggerManager;
    @Mock
    private FileTypeRegistry              fileTypeRegistry;

    @Mock
    private Promise<Void>              promiseVoid;
    @Mock
    private Promise<DebuggerEventList> promiseEventList;
    @Mock
    private PromiseError               promiseError;

    @Mock
    private VirtualFile        file;
    @Mock
    private FqnResolverFactory fqnResolverFactory;
    @Mock
    private LocalStorage       localStorage;
    @Mock
    private DebuggerObserver   observer;
    @Mock
    private Location           location;
    @Mock
    private BreakPoint         jdiBreakpoint;
    @Mock
    private FqnResolver        fgnResolver;

    @Captor
    private ArgumentCaptor<WsAgentStateHandler>              extServerStateHandlerCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>          operationPromiseErrorCaptor;
    @Captor
    private ArgumentCaptor<Operation<Void>>                  operationVoidCaptor;
    @Captor
    private ArgumentCaptor<Breakpoint>                       breakpointCaptor;
    @Captor
    private ArgumentCaptor<Function<Value, String>>          functionValueStringCaptor;
    @Captor
    private ArgumentCaptor<Function<StackFrameDump, String>> functionStackFrameDumpStringCaptor;
    @Captor
    private ArgumentCaptor<Function<JavaDebuggerInfo, Void>> argumentCaptorFunctionJavaDebuggerInfoVoid;
    @Captor
    private ArgumentCaptor<Operation<JavaDebuggerInfo>>      argumentCaptorOperationJavaDebuggerInfo;


    public final Breakpoint TEST_BREAKPOINT = new Breakpoint(Breakpoint.Type.BREAKPOINT, LINE_NUMBER, PATH, file, true);

    private JavaDebugger debugger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        super.setUp();

        doReturn(location).when(dtoFactory).createDto(Location.class);
        doReturn(jdiBreakpoint).when(dtoFactory).createDto(BreakPoint.class);


        doReturn(messageBus).when(messageBusProvider).getMachineMessageBus();

        doReturn(localStorage).when(localStorageProvider).get();
        doReturn(DEBUG_INFO).when(localStorage).getItem(JavaDebugger.LOCAL_STORAGE_DEBUGGER_KEY);
        doReturn(javaDebuggerInfo).when(dtoFactory).createDtoFromJson(DEBUG_INFO, JavaDebuggerInfo.class);

        doReturn(fgnResolver).when(fqnResolverFactory).getResolver(anyString());
        doReturn(FQN).when(fgnResolver).resolveFqn(file);

        doReturn(PATH).when(file).getPath();

        debugger = new JavaDebugger(service, dtoFactory, localStorageProvider, messageBusProvider, eventBus, fqnResolverFactory, appContext,
                                    javaDebuggerFileHandler,
                                    debuggerManager, fileTypeRegistry);
        doReturn(promiseEventList).when(service).checkEvents(DEBUGGER_ID);
        doReturn(promiseEventList).when(promiseEventList).then(any(Operation.class));

        // setup messageBus
        verify(eventBus).addHandler(eq(WsAgentStateEvent.TYPE), extServerStateHandlerCaptor.capture());
        extServerStateHandlerCaptor.getValue().onWsAgentStarted(WsAgentStateEvent.createWsAgentStartedEvent());

        debugger.addObserver(observer);

        FileType fileType = mock(FileType.class);
        doReturn(Collections.singletonList("application/java")).when(fileType).getMimeTypes();
        doReturn(fileType).when(fileTypeRegistry).getFileTypeByFile(eq(file));
    }

    @Test
    public void testAttachDebugger() throws Exception {
        debugger.setJavaDebuggerInfo(null);

        final String host = "some.host";
        final String port = "8000";
        final String vmName = "vm.name";
        final String vmVersion = "vm.version";
        final DebuggerDescriptor expectedDebuggerDescriptor = new DebuggerDescriptor(vmName + " " + vmVersion, host + ":" + port);

        final String javaDebuggerInfoJson = "javaDebuggerInfo";
        doReturn(javaDebuggerInfoJson).when(dtoFactory).toJson(javaDebuggerInfo);

        Map<String, String> connectionProperties = mock(Map.class);
        doReturn(host).when(connectionProperties).get(JavaDebugger.JavaConnectionProperties.HOST.toString());
        doReturn(port).when(connectionProperties).get(JavaDebugger.JavaConnectionProperties.PORT.toString());

        doReturn(vmName).when(javaDebuggerInfo).getVmName();
        doReturn(vmVersion).when(javaDebuggerInfo).getVmVersion();

        Promise<JavaDebuggerInfo> promiseJavaDebuggerInfo = mock(Promise.class);

        doReturn(promiseJavaDebuggerInfo).when(service).connect(connectionProperties);
        doReturn(promiseVoid).when(promiseJavaDebuggerInfo).then((Function<JavaDebuggerInfo, Void>)any());
        doReturn(promiseVoid).when(promiseVoid).catchError((Operation<PromiseError>)any());

        Promise<Void> result = debugger.attachDebugger(connectionProperties);
        assertEquals(promiseVoid, result);

        verify(promiseJavaDebuggerInfo).then(argumentCaptorFunctionJavaDebuggerInfoVoid.capture());
        argumentCaptorFunctionJavaDebuggerInfoVoid.getValue().apply(javaDebuggerInfo);

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        try {
            operationPromiseErrorCaptor.getValue().apply(promiseError);
            fail("Operation Exception expected");
        } catch (OperationException e) {
            verify(promiseError).getMessage();
            verify(promiseError).getCause();
        }

        verify(observer).onDebuggerAttached(expectedDebuggerDescriptor, promiseVoid);

        verify(promiseJavaDebuggerInfo).then(argumentCaptorOperationJavaDebuggerInfo.capture());
        argumentCaptorOperationJavaDebuggerInfo.getValue().apply(javaDebuggerInfo);
        assertTrue(debugger.isConnected());
        verify(localStorage).setItem(eq(JavaDebugger.LOCAL_STORAGE_DEBUGGER_KEY), eq(javaDebuggerInfoJson));
        verify(messageBus).subscribe(eq(JavaRuntimeExtension.EVENTS_CHANNEL + DEBUGGER_ID), any(SubscriptionHandler.class));
        verify(messageBus).subscribe(eq(JavaRuntimeExtension.DISCONNECT_CHANNEL + DEBUGGER_ID), any(SubscriptionHandler.class));
    }

    @Test
    public void testAttachDebuggerWithConnection() throws Exception {
        Map<String, String> connectionProperties = mock(Map.class);

        try {
            debugger.attachDebugger(connectionProperties);
        } catch (UnsatisfiedLinkError e) {
            assertTrue(e.getMessage().contains("JsPromiseError.create"));  // verify calling "Promises.resolve(null)"
            verify(service, never()).connect(any());
            return;
        }

        fail("debugger.attachDebugger() didn't try to obtain rejected promise when there is a connection");
    }

    @Test
    public void testDisconnectDebugger() throws Exception {
        doReturn(promiseVoid).when(service).disconnect(DEBUGGER_ID);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        doReturn(true).when(messageBus).isHandlerSubscribed(any(), any());

        debugger.disconnectDebugger();

        assertFalse(debugger.isConnected());
        verify(localStorage).setItem(eq(JavaDebugger.LOCAL_STORAGE_DEBUGGER_KEY), eq(""));
        verify(messageBus, times(2)).unsubscribe(anyString(), any());

        verify(promiseVoid).then(operationVoidCaptor.capture());
        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());

        operationVoidCaptor.getValue().apply(null);
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
        verify(observer, times(2)).onDebuggerDisconnected();
        verify(debuggerManager, times(2)).setActiveDebugger(eq(null));
    }

    @Test
    public void testDisconnectDebuggerWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);

        try {
            debugger.disconnectDebugger();
        } catch (UnsatisfiedLinkError e) {
            assertTrue(e.getMessage().contains("Promises.resolve"));  // verify calling "Promises.resolve(null)"
            verify(service, never()).disconnect(any());
            return;
        }

        fail("debugger.disconnectDebugger() didn't try to obtain disconnect promiseVoid from the method 'Promises.resolve(null)'");
    }

    @Test
    public void testResume() throws Exception {
        doReturn(promiseVoid).when(service).resume(DEBUGGER_ID);

        debugger.resume();

        verify(observer).onPreResume();

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();

        assertTrue(debugger.isConnected());
    }

    @Test
    public void testResumeWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.resume();
        verify(service, never()).resume(any());
    }

    @Test
    public void testStepInto() throws Exception {
        doReturn(promiseVoid).when(service).stepInto(DEBUGGER_ID);

        debugger.stepInto();

        verify(observer).onPreStepIn();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepIntoWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.stepInto();
        verify(service, never()).stepInto(any());
    }

    @Test
    public void testStepOver() throws Exception {
        doReturn(promiseVoid).when(service).stepOver(DEBUGGER_ID);

        debugger.stepOver();

        verify(observer).onPreStepOver();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepOverWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.stepOver();
        verify(service, never()).stepOver(any());
    }

    @Test
    public void testStepOut() throws Exception {
        doReturn(promiseVoid).when(service).stepOut(DEBUGGER_ID);

        debugger.stepOut();

        verify(observer).onPreStepOut();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepOutWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.stepOut();
        verify(service, never()).stepOut(any());
    }

    @Test
    public void testAddBreakpoint() throws Exception {
        doReturn(promiseVoid).when(service).addBreakpoint(DEBUGGER_ID, jdiBreakpoint);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());
        
        debugger.addBreakpoint(file, LINE_NUMBER);
        
        verify(location).setLineNumber(LINE_NUMBER + 1);
        verify(location).setClassName(FQN);

        verify(jdiBreakpoint).setLocation(location);
        verify(jdiBreakpoint).setEnabled(true);

        verify(promiseVoid).then(operationVoidCaptor.capture());
        operationVoidCaptor.getValue().apply(null);
        verify(observer).onBreakpointAdded(breakpointCaptor.capture());
        assertEquals(breakpointCaptor.getValue(), TEST_BREAKPOINT);
        
        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
    }

    @Test
    public void testAddBreakpointWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.addBreakpoint(file, LINE_NUMBER);

        verify(service, never()).addBreakpoint(any(), any());
        verify(observer).onBreakpointAdded(breakpointCaptor.capture());
        assertEquals(breakpointCaptor.getValue(), TEST_BREAKPOINT);
    }

    @Test
    public void testDeleteBreakpoint() throws Exception {
        doReturn(promiseVoid).when(service).deleteBreakpoint(DEBUGGER_ID, jdiBreakpoint);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        debugger.deleteBreakpoint(file, LINE_NUMBER);

        verify(location).setLineNumber(LINE_NUMBER + 1);
        verify(location).setClassName(FQN);

        verify(jdiBreakpoint).setLocation(location);
        verify(jdiBreakpoint).setEnabled(true);

        verify(promiseVoid).then(operationVoidCaptor.capture());
        operationVoidCaptor.getValue().apply(null);
        verify(observer).onBreakpointDeleted(breakpointCaptor.capture());
        assertEquals(TEST_BREAKPOINT, breakpointCaptor.getValue());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
    }

    @Test
    public void testDeleteBreakpointWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.deleteBreakpoint(file, LINE_NUMBER);

        verify(service, never()).deleteBreakpoint(any(), any());
    }

    @Test
    public void testDeleteAllBreakpoints() throws Exception {
        doReturn(promiseVoid).when(service).deleteAllBreakpoints(DEBUGGER_ID);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        debugger.deleteAllBreakpoints();

        verify(promiseVoid).then(operationVoidCaptor.capture());
        operationVoidCaptor.getValue().apply(null);
        verify(observer).onAllBreakpointsDeleted();

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
    }

    @Test
    public void testDeleteAllBreakpointsWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);
        debugger.deleteAllBreakpoints();

        verify(service, never()).deleteAllBreakpoints(any());
    }

    @Test
    public void testGetValue() throws Exception {
        final String variableJson = "{\"name\":\"var_name\",\"value\":\"var_value\"}";
        final String variablesJson = "[" + variableJson + "]";

        final Variable variable = mock(Variable.class);
        final Value value = mock(Value.class);
        final Promise<Value> promiseValue = mock(Promise.class);

        doReturn(promiseValue).when(service).getValue(DEBUGGER_ID, variable);
        doReturn(promiseValue).when(promiseValue).then((Function<Value, Object>)any());
        doReturn(promiseValue).when(promiseValue).catchError((Operation<PromiseError>)any());

        List<Variable> variables = ImmutableList.of(variable);
        doReturn(variable).when(dtoFactory).createDtoFromJson(variableJson, Variable.class);
        doReturn(variablesJson).when(dtoFactory).toJson(variables);
        doReturn(variables).when(value).getVariables();

        Promise<String> result = debugger.getValue(variableJson);
        assertEquals(promiseValue, result);

        verify(promiseValue).then(functionValueStringCaptor.capture());
        assertEquals(variablesJson, functionValueStringCaptor.getValue().apply(value));

        verify(promiseValue).catchError(operationPromiseErrorCaptor.capture());
        try {
            operationPromiseErrorCaptor.getValue().apply(promiseError);
            fail("Operation Exception expected");
        } catch (OperationException e) {
            verify(promiseError).getMessage();
            verify(promiseError).getCause();
        }
    }

    @Test
    public void testGetValueWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);

        try {
            debugger.getValue(null);
        } catch (UnsatisfiedLinkError e) {
            assertTrue(e.getMessage().contains("JsPromiseError.create"));  // verify calling "Promises.resolve(null)"
            verify(service, never()).getValue(any(), any());
            return;
        }

        fail("debugger.getValue() didn't try to obtain rejected promise when there is no connection");
    }

    @Test
    public void testGetStackFrameDump() throws Exception {
        Promise<StackFrameDump> promiseStackFrameDump = mock(Promise.class);
        StackFrameDump mockStackFrameDump = mock(StackFrameDump.class);
        final String json = "json";
        doReturn(json).when(dtoFactory).toJson(mockStackFrameDump);

        doReturn(promiseStackFrameDump).when(service).getStackFrameDump(DEBUGGER_ID);
        doReturn(promiseStackFrameDump).when(promiseStackFrameDump).then((Function<StackFrameDump, Object>)any());
        doReturn(promiseStackFrameDump).when(promiseStackFrameDump).catchError((Operation<PromiseError>)any());

        Promise<String> result = debugger.getStackFrameDump();
        assertEquals(promiseStackFrameDump, result);

        verify(promiseStackFrameDump).then(functionStackFrameDumpStringCaptor.capture());
        assertEquals(json, functionStackFrameDumpStringCaptor.getValue().apply(mockStackFrameDump));

        verify(promiseStackFrameDump).catchError(operationPromiseErrorCaptor.capture());
        try {
            operationPromiseErrorCaptor.getValue().apply(promiseError);
            fail("Operation Exception expected");
        } catch (OperationException e) {
            verify(promiseError).getMessage();
            verify(promiseError).getCause();
        }
    }

    @Test
    public void testGetStackFrameDumpWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);

        try {
            debugger.getStackFrameDump();
        } catch (UnsatisfiedLinkError e) {
            assertTrue(e.getMessage().contains("JsPromiseError.create"));  // verify calling "Promises.resolve(null)"
            verify(service, never()).getStackFrameDump(any());
            return;
        }

        fail("debugger.getStackFrameDump() didn't try to obtain rejected promise when there is no connection");
    }

    @Test
    public void testEvaluateExpression() throws Exception {
        final String expression = "a = 1";
        Promise<String> promiseString = mock(Promise.class);
        doReturn(promiseString).when(service).evaluateExpression(DEBUGGER_ID, expression);

        Promise<String> result = debugger.evaluateExpression(expression);
        assertEquals(promiseString, result);
    }

    @Test
    public void testEvaluateExpressionWithoutConnection() throws Exception {
        debugger.setJavaDebuggerInfo(null);

        try {
            debugger.evaluateExpression("any");
        } catch (UnsatisfiedLinkError e) {
            assertTrue(e.getMessage().contains("JsPromiseError.create"));  // verify calling "Promises.resolve(null)"
            verify(service, never()).evaluateExpression(any(), any());
            return;
        }

        fail("debugger.evaluateExpression() didn't try to obtain rejected promise when there is no connection");
    }

    @Test
    public void testChangeVariableValue() throws Exception {
        final List<String> path = mock(List.class);
        final String newValue = "new-value";

        VariablePath variablePath = mock(VariablePath.class);
        doReturn(variablePath).when(dtoFactory).createDto(VariablePath.class);

        UpdateVariableRequest updateVariableRequest = mock(UpdateVariableRequest.class);
        doReturn(updateVariableRequest).when(dtoFactory).createDto(UpdateVariableRequest.class);

        doReturn(promiseVoid).when(service).setValue(DEBUGGER_ID, updateVariableRequest);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        debugger.changeVariableValue(path, newValue);

        verify(promiseVoid).then(operationVoidCaptor.capture());
        operationVoidCaptor.getValue().apply(null);
        verify(observer).onValueChanged(path, newValue);

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
    }

}

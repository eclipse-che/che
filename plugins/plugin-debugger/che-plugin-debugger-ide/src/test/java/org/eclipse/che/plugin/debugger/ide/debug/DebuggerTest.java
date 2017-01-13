/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StartActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.debugger.ide.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.creation.MockSettingsImpl;

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
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link AbstractDebugger} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmytro Nochevnov
 */
@RunWith(GwtMockitoTestRunner.class)
public class DebuggerTest extends BaseTest {
    private static final String DEBUG_INFO = "debug_info";
    private static final String SESSION_ID = "debugger_id";

    public static final int    LINE_NUMBER = 20;
    public static final String FQN         = "org.test.Test";
    public static final String PATH        = "test/src/main/java/Test.java";

    @Mock
    private DebuggerServiceClient service;
    @Mock
    private DtoFactory            dtoFactory;
    @Mock
    private LocalStorageProvider  localStorageProvider;
    @Mock
    private MessageBusProvider    messageBusProvider;
    @Mock
    private EventBus              eventBus;
    @Mock
    private ActiveFileHandler     activeFileHandler;
    @Mock
    private DebuggerManager       debuggerManager;
    @Mock
    private NotificationManager   notificationManager;
    @Mock
    private BreakpointManager     breakpointManager;

    @Mock
    private Promise<Void>         promiseVoid;
    @Mock
    private Promise<DebuggerInfo> promiseInfo;
    @Mock
    private PromiseError          promiseError;

    @Mock
    private VirtualFile       file;
    @Mock
    private LocalStorage      localStorage;
    @Mock
    private DebuggerObserver  observer;
    @Mock
    private LocationDto       locationDto;
    @Mock
    private BreakpointDto     breakpointDto;
    @Mock
    private Optional<Project> optional;

    @Captor
    private ArgumentCaptor<WsAgentStateHandler>             extServerStateHandlerCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>         operationPromiseErrorCaptor;
    @Captor
    private ArgumentCaptor<Operation<Void>>                 operationVoidCaptor;
    @Captor
    private ArgumentCaptor<Breakpoint>                      breakpointCaptor;
    @Captor
    private ArgumentCaptor<Function<DebugSessionDto, Void>> argumentCaptorFunctionJavaDebugSessionVoid;
    @Captor
    private ArgumentCaptor<Operation<DebuggerInfo>>         argumentCaptorOperationJavaDebuggerInfo;


    public final Breakpoint TEST_BREAKPOINT = new Breakpoint(Breakpoint.Type.BREAKPOINT, LINE_NUMBER, PATH, file, true);
    public DebuggerDescriptor debuggerDescriptor;

    private AbstractDebugger debugger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        super.setUp();

        debuggerDescriptor = new DebuggerDescriptor(NAME + " " + VERSION, HOST + ":" + PORT);

        doReturn(locationDto).when(dtoFactory).createDto(LocationDto.class);
        doReturn(breakpointDto).when(dtoFactory).createDto(BreakpointDto.class);
        doReturn(locationDto).when(breakpointDto).getLocation();

        doReturn(messageBus).when(messageBusProvider).getMachineMessageBus();

        doReturn(localStorage).when(localStorageProvider).get();
        doReturn(DEBUG_INFO).when(localStorage).getItem(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY);
        doReturn(debugSessionDto).when(dtoFactory).createDtoFromJson(anyString(), eq(DebugSessionDto.class));

        doReturn(Path.valueOf(PATH)).when(file).getLocation();

        debugger = new TestDebugger(service, dtoFactory, localStorageProvider, messageBusProvider, eventBus,
                                    activeFileHandler, debuggerManager, notificationManager, "id");
        doReturn(promiseInfo).when(service).getSessionInfo(SESSION_ID);
        doReturn(promiseInfo).when(promiseInfo).then(any(Operation.class));

        // setup messageBus
        verify(eventBus).addHandler(eq(WsAgentStateEvent.TYPE), extServerStateHandlerCaptor.capture());
        extServerStateHandlerCaptor.getValue().onWsAgentStarted(WsAgentStateEvent.createWsAgentStartedEvent());

        debugger.addObserver(observer);

        FileType fileType = mock(FileType.class);
        doReturn("java").when(fileType).getExtension();
    }

    @Test
    public void testAttachDebugger() throws Exception {
        debugger.setDebugSession(null);

        final String debugSessionJson = "debugSession";
        doReturn(debugSessionJson).when(dtoFactory).toJson(debugSessionDto);
        doReturn(mock(StartActionDto.class)).when(dtoFactory).createDto(StartActionDto.class);

        Map<String, String> connectionProperties = mock(Map.class);
        Promise<DebugSessionDto> promiseDebuggerInfo = mock(Promise.class);

        doReturn(promiseDebuggerInfo).when(service).connect("id", connectionProperties);
        doReturn(promiseVoid).when(promiseDebuggerInfo).then((Function<DebugSessionDto, Void>)any());
        doReturn(promiseVoid).when(promiseVoid).catchError((Operation<PromiseError>)any());

        Promise<Void> result = debugger.connect(connectionProperties);
        assertEquals(promiseVoid, result);

        verify(promiseDebuggerInfo).then(argumentCaptorFunctionJavaDebugSessionVoid.capture());
        argumentCaptorFunctionJavaDebugSessionVoid.getValue().apply(debugSessionDto);

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        try {
            operationPromiseErrorCaptor.getValue().apply(promiseError);
            fail("Operation Exception expected");
        } catch (OperationException e) {
            verify(promiseError).getMessage();
            verify(promiseError).getCause();
        }

        verify(observer).onDebuggerAttached(debuggerDescriptor, promiseVoid);

        assertTrue(debugger.isConnected());
        verify(localStorage).setItem(eq(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY), eq(debugSessionJson));
        verify(messageBus).subscribe(eq("id:events:"), any(SubscriptionHandler.class));
    }

    @Test
    public void testAttachDebuggerWithConnection() throws Exception {
        Map<String, String> connectionProperties = mock(Map.class);

        debugger.connect(connectionProperties);

        verify(service, never()).connect(any(), any());
    }

    @Test
    public void testDisconnectDebugger() throws Exception {
        doReturn(promiseVoid).when(service).disconnect(SESSION_ID);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        doReturn(true).when(messageBus).isHandlerSubscribed(any(), any());

        debugger.disconnect();

        assertFalse(debugger.isConnected());
        verify(localStorage).setItem(eq(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY), eq(""));
        verify(messageBus, times(1)).unsubscribe(anyString(), any());

        verify(promiseVoid).then(operationVoidCaptor.capture());
        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());

        operationVoidCaptor.getValue().apply(null);
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(observer, times(2)).onDebuggerDisconnected();
        verify(debuggerManager, times(2)).setActiveDebugger(eq(null));
    }

    @Test
    public void testDisconnectDebuggerWithoutConnection() throws Exception {
        debugger.setDebugSession(null);

        debugger.disconnect();

        verify(service, never()).disconnect(any());
    }

    @Test
    public void testResume() throws Exception {
        ResumeActionDto resumeAction = mock(ResumeActionDto.class);

        doReturn(promiseVoid).when(service).resume(SESSION_ID, resumeAction);
        doReturn(resumeAction).when(dtoFactory).createDto(ResumeActionDto.class);

        debugger.resume();

        verify(observer).onPreResume();

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();

        assertTrue(debugger.isConnected());
    }

    @Test
    public void testResumeWithoutConnection() throws Exception {
        debugger.setDebugSession(null);
        debugger.resume();
        verify(service, never()).resume(any(), any());
    }

    @Test
    public void testStepInto() throws Exception {
        StepIntoActionDto stepIntoAction = mock(StepIntoActionDto.class);

        doReturn(promiseVoid).when(service).stepInto(SESSION_ID, stepIntoAction);
        doReturn(stepIntoAction).when(dtoFactory).createDto(StepIntoActionDto.class);

        debugger.stepInto();

        verify(observer).onPreStepInto();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepIntoWithoutConnection() throws Exception {
        debugger.setDebugSession(null);
        debugger.stepInto();
        verify(service, never()).stepInto(any(), any());
    }

    @Test
    public void testStepOver() throws Exception {
        StepOverActionDto stepOverAction = mock(StepOverActionDto.class);

        doReturn(promiseVoid).when(service).stepOver(SESSION_ID, stepOverAction);
        doReturn(stepOverAction).when(dtoFactory).createDto(StepOverActionDto.class);

        debugger.stepOver();

        verify(observer).onPreStepOver();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepOverWithoutConnection() throws Exception {
        debugger.setDebugSession(null);
        debugger.stepOver();
        verify(service, never()).stepOver(any(), any());
    }

    @Test
    public void testStepOut() throws Exception {
        StepOutActionDto stepOutAction = mock(StepOutActionDto.class);

        doReturn(promiseVoid).when(service).stepOut(SESSION_ID, stepOutAction);
        doReturn(stepOutAction).when(dtoFactory).createDto(StepOutActionDto.class);

        debugger.stepOut();

        verify(observer).onPreStepOut();

        assertTrue(debugger.isConnected());

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getCause();
    }

    @Test
    public void testStepOutWithoutConnection() throws Exception {
        debugger.setDebugSession(null);
        debugger.stepOut();
        verify(service, never()).stepOut(any(), any());
    }

    @Test
    public void testAddBreakpoint() throws Exception {
        MockSettings mockSettings = new MockSettingsImpl<>().defaultAnswer(RETURNS_SMART_NULLS)
                                                            .extraInterfaces(Resource.class);
        Project project = mock(Project.class);
        when(optional.isPresent()).thenReturn(true);
        when(optional.get()).thenReturn(project);
        when(project.getPath()).thenReturn(PATH);

        VirtualFile virtualFile = mock(VirtualFile.class, mockSettings);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(PATH);
        when(virtualFile.getLocation()).thenReturn(path);
        when(virtualFile.toString()).thenReturn(PATH);

        Resource resource = (Resource)virtualFile;
        when(resource.getRelatedProject()).thenReturn(optional);
        doReturn(promiseVoid).when(service).addBreakpoint(SESSION_ID, breakpointDto);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());
        when(locationDto.withLineNumber(LINE_NUMBER + 1)).thenReturn(locationDto);
        when(locationDto.withResourcePath(PATH)).thenReturn(locationDto);
        when(locationDto.withResourceProjectPath(PATH)).thenReturn(locationDto);
        when(locationDto.withTarget(anyString())).thenReturn(locationDto);
        when(breakpointDto.withLocation(locationDto)).thenReturn(breakpointDto);
        when(breakpointDto.withEnabled(true)).thenReturn(breakpointDto);

        debugger.addBreakpoint(virtualFile, LINE_NUMBER);

        verify(locationDto).withLineNumber(LINE_NUMBER + 1);
        verify(locationDto).withTarget(FQN);
        verify(locationDto).withResourcePath(PATH);
        verify(locationDto).withResourceProjectPath(PATH);

        verify(breakpointDto).withLocation(locationDto);
        verify(breakpointDto).withEnabled(true);

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
        debugger.setDebugSession(null);
        debugger.addBreakpoint(file, LINE_NUMBER);

        verify(service, never()).addBreakpoint(any(), any());
        verify(observer).onBreakpointAdded(breakpointCaptor.capture());
        assertEquals(breakpointCaptor.getValue(), TEST_BREAKPOINT);
    }

    @Test
    public void testDeleteBreakpoint() throws Exception {
        doReturn(promiseVoid).when(service).deleteBreakpoint(SESSION_ID, locationDto);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        debugger.deleteBreakpoint(file, LINE_NUMBER);

        verify(locationDto).setLineNumber(LINE_NUMBER + 1);
        verify(locationDto).setTarget(FQN);

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
        debugger.setDebugSession(null);
        debugger.deleteBreakpoint(file, LINE_NUMBER);

        verify(service, never()).deleteBreakpoint(any(), any());
    }

    @Test
    public void testDeleteAllBreakpoints() throws Exception {
        doReturn(promiseVoid).when(service).deleteAllBreakpoints(SESSION_ID);
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
        debugger.setDebugSession(null);
        debugger.deleteAllBreakpoints();

        verify(service, never()).deleteAllBreakpoints(any());
    }

    @Test
    public void testGetValue() throws Exception {
        final VariableDto variableDto = mock(VariableDto.class);
        final Variable variable = mock(Variable.class);
        final Promise<SimpleValueDto> promiseValue = mock(Promise.class);

        doReturn(variableDto).when(dtoFactory).createDto(VariableDto.class);
        doReturn(mock(VariablePathDto.class)).when(dtoFactory).createDto(VariablePathDto.class);
        doReturn(mock(VariablePathDto.class)).when(variable).getVariablePath();
        doReturn(Collections.emptyList()).when(variable).getVariables();
        doReturn(promiseValue).when(service).getValue(SESSION_ID, variableDto);
        doReturn(promiseValue).when(promiseValue).then((Function<SimpleValueDto, Object>)any());
        doReturn(promiseValue).when(promiseValue).catchError((Operation<PromiseError>)any());

        Promise<SimpleValue> result = debugger.getValue(variable);
        assertEquals(promiseValue, result);
    }

    @Test
    public void testGetValueWithoutConnection() throws Exception {
        debugger.setDebugSession(null);

        debugger.getValue(null);

        verify(service, never()).getValue(any(), any());
    }

    @Test
    public void testGetStackFrameDump() throws Exception {
        Promise<StackFrameDumpDto> promiseStackFrameDump = mock(Promise.class);
        StackFrameDumpDto mockStackFrameDumpDto = mock(StackFrameDumpDto.class);
        final String json = "json";
        doReturn(json).when(dtoFactory).toJson(mockStackFrameDumpDto);

        doReturn(promiseStackFrameDump).when(service).getStackFrameDump(SESSION_ID);
        doReturn(promiseStackFrameDump).when(promiseStackFrameDump).then((Function<StackFrameDumpDto, Object>)any());
        doReturn(promiseStackFrameDump).when(promiseStackFrameDump).catchError((Operation<PromiseError>)any());

        Promise<StackFrameDump> result = debugger.dumpStackFrame();
        assertEquals(promiseStackFrameDump, result);
    }

    @Test
    public void testGetStackFrameDumpWithoutConnection() throws Exception {
        debugger.setDebugSession(null);

        debugger.dumpStackFrame();

        verify(service, never()).getStackFrameDump(any());
    }

    @Test
    public void testEvaluateExpression() throws Exception {
        final String expression = "a = 1";
        Promise<String> promiseString = mock(Promise.class);
        doReturn(promiseString).when(service).evaluate(SESSION_ID, expression);

        Promise<String> result = debugger.evaluate(expression);
        assertEquals(promiseString, result);
    }

    @Test
    public void testEvaluateExpressionWithoutConnection() throws Exception {
        debugger.setDebugSession(null);
        debugger.evaluate("any");
        verify(service, never()).evaluate(any(), any());
    }

    @Test
    public void testChangeVariableValue() throws Exception {
        final List<String> path = mock(List.class);
        final String newValue = "new-value";

        VariablePath variablePath = mock(VariablePathDto.class);
        doReturn(path).when(variablePath).getPath();

        VariableDto variableDto = mock(VariableDto.class);
        doReturn(variableDto).when(dtoFactory).createDto(VariableDto.class);

        Variable variable = mock(Variable.class);
        doReturn(mock(VariablePathDto.class)).when(dtoFactory).createDto(VariablePathDto.class);
        doReturn(variablePath).when(variable).getVariablePath();
        doReturn(newValue).when(variable).getValue();
        doReturn(Collections.emptyList()).when(variable).getVariables();

        doReturn(promiseVoid).when(service).setValue(SESSION_ID, variableDto);
        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        debugger.setValue(variable);

        verify(promiseVoid).then(operationVoidCaptor.capture());
        operationVoidCaptor.getValue().apply(null);
        verify(observer).onValueChanged(path, newValue);

        verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
    }

    private class TestDebugger extends AbstractDebugger {

        public TestDebugger(DebuggerServiceClient service,
                            DtoFactory dtoFactory,
                            LocalStorageProvider localStorageProvider,
                            MessageBusProvider messageBusProvider,
                            EventBus eventBus,
                            ActiveFileHandler activeFileHandler,
                            DebuggerManager debuggerManager,
                            NotificationManager notificationManager,
                            String id) {
            super(service,
                  dtoFactory,
                  localStorageProvider,
                  messageBusProvider,
                  eventBus,
                  activeFileHandler,
                  debuggerManager,
                  notificationManager,
                  breakpointManager,
                  id);
        }

        @Override
        protected String fqnToPath(Location location) {
            return PATH;
        }

        @Nullable
        @Override
        protected String pathToFqn(VirtualFile file) {
            return FQN;
        }

        @Override
        protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
            return debuggerDescriptor;
        }
    }
}

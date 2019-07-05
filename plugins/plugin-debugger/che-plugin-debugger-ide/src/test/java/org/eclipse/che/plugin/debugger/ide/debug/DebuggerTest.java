/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import static junit.framework.TestCase.assertEquals;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.dto.action.ResumeActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepIntoActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOutActionDto;
import org.eclipse.che.api.debug.shared.dto.action.StepOverActionDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.BaseTest;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Testing {@link AbstractDebugger} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmytro Nochevnov
 */
@RunWith(GwtMockitoTestRunner.class)
public class DebuggerTest extends BaseTest {
  @Rule public MockitoRule mrule = MockitoJUnit.rule().silent();

  private static final String DEBUG_INFO = "debug_info";
  private static final String SESSION_ID = "debugger_id";
  private static final long THREAD_ID = 1;
  private static final int FRAME_INDEX = 0;

  public static final String PATH = "test/src/main/java/Test.java";

  @Mock private DebuggerServiceClient service;
  @Mock private DtoFactory dtoFactory;
  @Mock private LocalStorageProvider localStorageProvider;
  @Mock private EventBus eventBus;
  @Mock private DebuggerLocationHandlerManager debuggerLocationHandlerManager;
  @Mock private DebuggerManager debuggerManager;
  @Mock private NotificationManager notificationManager;
  @Mock private BreakpointManager breakpointManager;
  @Mock private AppContext appContext;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private RequestTransmitter transmitter;

  @Mock private RequestHandlerConfigurator configurator;
  @Mock private RequestHandlerManager requestHandlerManager;

  @Mock private Promise<Void> promiseVoid;
  @Mock private Promise<DebuggerInfo> promiseInfo;
  @Mock private PromiseError promiseError;

  @Mock private VirtualFile file;
  @Mock private LocalStorage localStorage;
  @Mock private DebuggerObserver observer;
  @Mock private LocationDto locationDto;
  @Mock private BreakpointDto breakpointDto;
  @Mock private Optional<Project> optional;
  @Mock private WorkspaceImpl workspace;
  @Mock private DebuggerLocalizationConstant constant;
  @Mock private PromiseProvider promiseProvider;
  @Mock private SuspendEventDto suspendEventDto;

  @Captor private ArgumentCaptor<WorkspaceRunningEvent.Handler> workspaceRunningHandlerCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> operationPromiseErrorCaptor;
  @Captor private ArgumentCaptor<Operation<Void>> operationVoidCaptor;
  @Captor private ArgumentCaptor<Breakpoint> breakpointCaptor;

  @Captor
  private ArgumentCaptor<Function<DebugSessionDto, Void>>
      argumentCaptorFunctionJavaDebugSessionVoid;

  public DebuggerDescriptor debuggerDescriptor;

  private AbstractDebugger debugger;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    super.setUp();

    debuggerDescriptor = new DebuggerDescriptor(NAME + " " + VERSION, HOST + ":" + PORT);

    doReturn(STOPPED).when(workspace).getStatus();
    doReturn(workspace).when(appContext).getWorkspace();

    doReturn(locationDto).when(dtoFactory).createDto(LocationDto.class);
    doReturn(breakpointDto).when(dtoFactory).createDto(BreakpointDto.class);
    doReturn(locationDto).when(breakpointDto).getLocation();

    doReturn(localStorage).when(localStorageProvider).get();
    doReturn(DEBUG_INFO)
        .when(localStorage)
        .getItem(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY);
    doReturn(debugSessionDto)
        .when(dtoFactory)
        .createDtoFromJson(anyString(), eq(DebugSessionDto.class));

    doReturn(Path.valueOf(PATH)).when(file).getLocation();

    debugger =
        spy(
            new TestDebugger(
                service,
                transmitter,
                configurator,
                dtoFactory,
                localStorageProvider,
                eventBus,
                debuggerManager,
                notificationManager,
                appContext,
                constant,
                "id",
                debuggerLocationHandlerManager,
                promiseProvider));
    doReturn(promiseInfo).when(service).getSessionInfo(SESSION_ID);
    doReturn(promiseInfo).when(promiseInfo).then(any(Operation.class));
    when(notificationManager.notify(
            any(), any(StatusNotification.Status.class), any(StatusNotification.DisplayMode.class)))
        .thenReturn(mock(StatusNotification.class));

    verify(eventBus)
        .addHandler(eq(WorkspaceRunningEvent.TYPE), workspaceRunningHandlerCaptor.capture());
    workspaceRunningHandlerCaptor.getValue().onWorkspaceRunning(new WorkspaceRunningEvent());

    debugger.addObserver(observer);

    FileType fileType = mock(FileType.class);
    doReturn("java").when(fileType).getExtension();
  }

  @Test
  public void testAttachDebugger() throws Exception {
    doNothing().when(debugger).subscribeToDebuggerEvents();
    doNothing().when(debugger).startCheckingEvents();
    doNothing().when(debugger).startDebugger(any(DebugSessionDto.class));
    debugger.setDebugSession(null);

    final String debugSessionJson = "debugSession";
    doReturn(debugSessionJson).when(dtoFactory).toJson(debugSessionDto);
    Map<String, String> connectionProperties = mock(Map.class);
    Promise<DebugSessionDto> promiseDebuggerInfo = mock(Promise.class);

    doReturn(promiseDebuggerInfo).when(service).connect("id", connectionProperties);
    doReturn(promiseVoid).when(promiseDebuggerInfo).then((Function<DebugSessionDto, Void>) any());
    doReturn(promiseVoid).when(promiseVoid).catchError((Operation<PromiseError>) any());

    Promise<Void> result = debugger.connect(connectionProperties);

    assertEquals(promiseVoid, result);

    verify(promiseDebuggerInfo).then(argumentCaptorFunctionJavaDebugSessionVoid.capture());
    argumentCaptorFunctionJavaDebugSessionVoid.getValue().apply(debugSessionDto);

    verify(observer).onDebuggerAttached(debuggerDescriptor);

    assertTrue(debugger.isConnected());
    verify(localStorage)
        .setItem(eq(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY), eq(debugSessionJson));
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
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());

    debugger.disconnect();

    assertFalse(debugger.isConnected());
    verify(localStorage).setItem(eq(AbstractDebugger.LOCAL_STORAGE_DEBUGGER_SESSION_KEY), eq(""));

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
    debugger.setSuspendEvent(suspendEventDto);

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
    debugger.setSuspendEvent(suspendEventDto);

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
    debugger.setSuspendEvent(suspendEventDto);

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
    MockSettings mockSettings =
        new MockSettingsImpl<>().defaultAnswer(RETURNS_SMART_NULLS).extraInterfaces(Resource.class);
    Project project = mock(Project.class);
    when(optional.isPresent()).thenReturn(true);
    when(optional.get()).thenReturn(project);
    when(project.getPath()).thenReturn(PATH);

    VirtualFile virtualFile = mock(VirtualFile.class, mockSettings);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(PATH);
    when(virtualFile.getLocation()).thenReturn(path);
    when(virtualFile.toString()).thenReturn(PATH);

    Resource resource = (Resource) virtualFile;
    when(resource.getRelatedProject()).thenReturn(optional);
    doReturn(promiseVoid).when(service).addBreakpoint(SESSION_ID, breakpointDto);
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());
    doReturn(breakpointDto).when(debugger).toDto(any(Breakpoint.class));

    debugger.addBreakpoint(breakpointDto);

    verify(service).addBreakpoint(SESSION_ID, breakpointDto);
    verify(promiseVoid).then(operationVoidCaptor.capture());
    operationVoidCaptor.getValue().apply(null);
    verify(observer).onBreakpointAdded(breakpointCaptor.capture());
    assertEquals(breakpointCaptor.getValue(), breakpointDto);

    verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
    operationPromiseErrorCaptor.getValue().apply(promiseError);
    verify(promiseError).getMessage();
  }

  @Test
  public void testDeleteBreakpoint() throws Exception {
    doReturn(promiseVoid).when(service).deleteBreakpoint(SESSION_ID, locationDto);
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());
    doReturn(locationDto).when(debugger).toDto(any(Location.class));

    debugger.deleteBreakpoint(breakpointDto);

    verify(promiseVoid).then(operationVoidCaptor.capture());
    operationVoidCaptor.getValue().apply(null);
    verify(observer).onBreakpointDeleted(breakpointCaptor.capture());
    assertEquals(breakpointDto, breakpointCaptor.getValue());

    verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
    operationPromiseErrorCaptor.getValue().apply(promiseError);
    verify(promiseError).getMessage();
  }

  @Test
  public void testDeleteBreakpointWithoutConnection() throws Exception {
    debugger.setDebugSession(null);
    debugger.deleteBreakpoint(breakpointDto);

    verify(service, never()).deleteBreakpoint(any(), any());
  }

  @Test
  public void testDeleteAllBreakpoints() throws Exception {
    doReturn(promiseVoid).when(service).deleteAllBreakpoints(SESSION_ID);
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());

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

    SimpleValueDto simpleValueDto = mock(SimpleValueDto.class);
    doReturn(simpleValueDto).when(dtoFactory).createDto(SimpleValueDto.class);
    doReturn(simpleValueDto).when(simpleValueDto).withString(nullable(String.class));

    SimpleValue simpleValue = mock(SimpleValue.class);
    doReturn(simpleValue).when(variable).getValue();

    doReturn(simpleValue).when(variable).getValue();
    doReturn(variableDto).when(dtoFactory).createDto(VariableDto.class);
    doReturn(mock(VariablePathDto.class)).when(dtoFactory).createDto(VariablePathDto.class);
    doReturn(mock(VariablePathDto.class)).when(variable).getVariablePath();

    doReturn(promiseValue).when(service).getValue(SESSION_ID, variableDto, THREAD_ID, FRAME_INDEX);
    doReturn(promiseValue).when(promiseValue).then((Function<SimpleValueDto, Object>) any());
    doReturn(promiseValue).when(promiseValue).catchError((Operation<PromiseError>) any());

    Promise<? extends SimpleValue> result = debugger.getValue(variable, THREAD_ID, FRAME_INDEX);
    assertEquals(promiseValue, result);
  }

  @Test
  public void testGetValueWithoutConnection() throws Exception {
    debugger.setDebugSession(null);

    debugger.getValue(null, THREAD_ID, FRAME_INDEX);

    verify(service, never()).getValue(any(), any(), eq(THREAD_ID), eq(FRAME_INDEX));
  }

  @Test
  public void testGetStackFrameDump() throws Exception {
    Promise<StackFrameDumpDto> promiseStackFrameDump = mock(Promise.class);
    StackFrameDumpDto mockStackFrameDumpDto = mock(StackFrameDumpDto.class);
    final String json = "json";
    doReturn(json).when(dtoFactory).toJson(mockStackFrameDumpDto);

    doReturn(promiseStackFrameDump)
        .when(service)
        .getStackFrameDump(SESSION_ID, THREAD_ID, FRAME_INDEX);
    doReturn(promiseStackFrameDump)
        .when(promiseStackFrameDump)
        .then((Function<StackFrameDumpDto, Object>) any());
    doReturn(promiseStackFrameDump)
        .when(promiseStackFrameDump)
        .catchError((Operation<PromiseError>) any());

    Promise<? extends StackFrameDump> result = debugger.getStackFrameDump(THREAD_ID, FRAME_INDEX);
    assertEquals(promiseStackFrameDump, result);
  }

  @Test
  public void testGetStackFrameDumpWithoutConnection() throws Exception {
    debugger.setDebugSession(null);

    debugger.getStackFrameDump(THREAD_ID, FRAME_INDEX);

    verify(service, never()).getStackFrameDump(any(), eq(THREAD_ID), eq(FRAME_INDEX));
  }

  @Test
  public void testEvaluateExpression() throws Exception {
    final String expression = "a = 1";
    Promise<String> promiseString = mock(Promise.class);
    doReturn(promiseString).when(service).evaluate(SESSION_ID, expression, THREAD_ID, FRAME_INDEX);

    Promise<String> result = debugger.evaluate(expression, THREAD_ID, FRAME_INDEX);
    assertEquals(promiseString, result);
  }

  @Test
  public void testEvaluateExpressionWithoutConnection() throws Exception {
    debugger.setDebugSession(null);
    debugger.evaluate("any", THREAD_ID, FRAME_INDEX);
    verify(service, never()).evaluate(any(), any(), eq(THREAD_ID), eq(FRAME_INDEX));
  }

  @Test
  public void testChangeVariableValue() throws Exception {
    final List<String> path = mock(List.class);
    final String newValue = "new-value";

    VariablePath variablePath = mock(VariablePathDto.class);
    doReturn(path).when(variablePath).getPath();

    VariableDto variableDto = mock(VariableDto.class);
    doReturn(variableDto).when(dtoFactory).createDto(VariableDto.class);

    SimpleValueDto simpleValueDto = mock(SimpleValueDto.class);
    doReturn(simpleValueDto).when(dtoFactory).createDto(SimpleValueDto.class);
    doReturn(simpleValueDto).when(simpleValueDto).withString(anyString());
    Variable variable = mock(Variable.class);
    doReturn(mock(VariablePathDto.class)).when(dtoFactory).createDto(VariablePathDto.class);
    doReturn(variablePath).when(variable).getVariablePath();
    SimpleValue simpleValue = mock(SimpleValue.class);
    doReturn(newValue).when(simpleValue).getString();
    doReturn(simpleValue).when(variable).getValue();

    doReturn(promiseVoid).when(service).setValue(SESSION_ID, variableDto, THREAD_ID, FRAME_INDEX);
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());

    debugger.setValue(variable, THREAD_ID, FRAME_INDEX);

    verify(promiseVoid).then(operationVoidCaptor.capture());
    operationVoidCaptor.getValue().apply(null);
    verify(observer).onValueChanged(variable, THREAD_ID, FRAME_INDEX);

    verify(promiseVoid).catchError(operationPromiseErrorCaptor.capture());
    operationPromiseErrorCaptor.getValue().apply(promiseError);
    verify(promiseError).getMessage();
  }

  private class TestDebugger extends AbstractDebugger {

    public TestDebugger(
        DebuggerServiceClient service,
        RequestTransmitter transmitter,
        RequestHandlerConfigurator configurator,
        DtoFactory dtoFactory,
        LocalStorageProvider localStorageProvider,
        EventBus eventBus,
        DebuggerManager debuggerManager,
        NotificationManager notificationManager,
        AppContext appContext,
        DebuggerLocalizationConstant constant,
        String id,
        DebuggerLocationHandlerManager debuggerLocationHandlerManager,
        PromiseProvider promiseProvider) {
      super(
          service,
          transmitter,
          configurator,
          dtoFactory,
          localStorageProvider,
          eventBus,
          debuggerManager,
          notificationManager,
          appContext,
          breakpointManager,
          constant,
          requestHandlerManager,
          debuggerLocationHandlerManager,
          promiseProvider,
          id);
    }

    @Override
    protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
      return debuggerDescriptor;
    }
  }
}

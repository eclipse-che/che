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

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.plugin.debugger.ide.BaseTest;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointContextMenuFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Testing {@link DebuggerPresenter} functionality.
 *
 * @author Dmytro Nochevnov
 * @author Oleksandr Andriienko
 */
public class DebuggerPresenterTest extends BaseTest {

  @Rule public MockitoRule mrule = MockitoJUnit.rule().silent();

  private static final long THREAD_ID = 1;
  private static final int FRAME_INDEX = 0;

  @Mock private DebuggerView view;
  @Mock private DebuggerLocalizationConstant constant;
  @Mock private BreakpointManager breakpointManager;
  @Mock private NotificationManager notificationManager;
  @Mock private DebuggerResources debuggerResources;
  @Mock @DebuggerToolbar private ToolbarPresenter debuggerToolbar;
  @Mock @DebuggerWatchToolBar private ToolbarPresenter watchToolbar;
  @Mock private DebuggerManager debuggerManager;
  @Mock private WorkspaceAgent workspaceAgent;
  @Mock private DebuggerLocationHandlerManager debuggerLocationHandlerManager;
  @Mock private BreakpointContextMenuFactory breakpointContextMenuFactory;

  @Mock private Debugger debugger;
  @Mock private MutableVariable selectedVariable;
  @Mock private List<ThreadState> threadDump;
  @Mock private StackFrameDump stackFrame;

  @Mock private Promise<SimpleValueDto> promiseValue;
  @Mock private Promise<List<ThreadState>> promiseThreadDump;
  @Mock private Promise<StackFrameDump> promiseStackFrame;
  @Mock private Promise<Void> promiseVoid;
  @Mock private EventBus eventBus;

  @Captor private ArgumentCaptor<Operation<Void>> operationVoidCaptor;
  @Captor private ArgumentCaptor<Operation<List<ThreadState>>> operationThreadDumpCaptor;
  @Captor private ArgumentCaptor<Operation<StackFrameDump>> operationStackFrameCaptor;
  @Captor private ArgumentCaptor<Operation<SimpleValueDto>> operationValueCaptor;

  private DebuggerPresenter presenter;

  @Before
  public void setup() {
    when(debuggerManager.getActiveDebugger()).thenReturn(debugger);
    doReturn(true).when(debugger).isSuspended();

    presenter =
        spy(
            new DebuggerPresenter(
                view,
                constant,
                breakpointManager,
                notificationManager,
                debuggerResources,
                debuggerToolbar,
                watchToolbar,
                debuggerManager,
                workspaceAgent,
                debuggerLocationHandlerManager,
                breakpointContextMenuFactory,
                eventBus));

    Mockito.reset(view);
    when(view.getSelectedThreadId()).thenReturn(THREAD_ID);
    when(view.getSelectedFrameIndex()).thenReturn(FRAME_INDEX);
  }

  @Test
  public void shouldSetNestedVariablesWhenNodeIsExpended() throws OperationException {
    SimpleValueDto valueDto = mock(SimpleValueDto.class);
    doReturn(promiseValue)
        .when(debugger)
        .getValue(eq(selectedVariable), eq(THREAD_ID), eq(FRAME_INDEX));
    doReturn(promiseValue).when(promiseValue).then((Operation<SimpleValueDto>) any());

    presenter.onExpandVariable(selectedVariable);

    verify(promiseValue).then(operationValueCaptor.capture());
    operationValueCaptor.getValue().apply(valueDto);

    verify(debugger).getValue(eq(selectedVariable), eq(THREAD_ID), eq(FRAME_INDEX));
    verify(view).expandVariable(any(Variable.class));
  }

  @Test
  public void shouldUpdateStackFrameDumpAndVariablesOnNewSelectedThread() throws Exception {
    doNothing().when(presenter).refreshVariables(THREAD_ID, 0);

    presenter.onSelectedThread(THREAD_ID);

    verify(presenter).refreshView(THREAD_ID);
  }

  @Test
  public void shouldUpdateVariablesOnSelectedFrame() throws Exception {
    doNothing().when(presenter).refreshVariables(THREAD_ID, FRAME_INDEX);

    presenter.onSelectedFrame(FRAME_INDEX);

    verify(presenter).refreshVariables(THREAD_ID, FRAME_INDEX);
  }

  @Test
  public void whenDebuggerStoppedThenPresenterShouldUpdateFramesAndVariables() throws Exception {
    Location executionPoint = mock(Location.class);
    doReturn(THREAD_ID).when(executionPoint).getThreadId();
    doReturn(promiseThreadDump).when(debugger).getThreadDump();
    doReturn(promiseThreadDump).when(promiseThreadDump).then((Operation<List<ThreadState>>) any());

    presenter.onBreakpointStopped(null, executionPoint);

    verify(promiseThreadDump).then(operationThreadDumpCaptor.capture());
    operationThreadDumpCaptor.getValue().apply(threadDump);
    verify(presenter).refreshView();
    verify(view).setThreadDump(eq(threadDump), anyLong());
  }

  @Test
  public void updateVariablesShouldUpdateView() throws Exception {
    doReturn(promiseStackFrame).when(debugger).getStackFrameDump(THREAD_ID, FRAME_INDEX);
    doReturn(promiseStackFrame).when(promiseStackFrame).then((Operation<StackFrameDump>) any());

    presenter.refreshVariables(THREAD_ID, FRAME_INDEX);

    verify(promiseStackFrame).then(operationStackFrameCaptor.capture());
    operationStackFrameCaptor.getValue().apply(stackFrame);
    verify(view).setVariables(stackFrame.getVariables());
  }

  @Test
  public void showDebuggerPanelAndSetVMNameOnDebuggerAttached() throws Exception {
    DebuggerDescriptor debuggerDescriptor = mock(DebuggerDescriptor.class);
    when(debuggerDescriptor.getAddress()).thenReturn("address");
    when(debuggerDescriptor.getInfo()).thenReturn("info");
    doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>) any());
    doNothing().when(presenter).showDebuggerPanel();
    when(notificationManager.notify(
            nullable(String.class),
            nullable(StatusNotification.Status.class),
            nullable(StatusNotification.DisplayMode.class)))
        .thenReturn(mock(StatusNotification.class));

    presenter.onDebuggerAttached(debuggerDescriptor);

    verify(presenter).showDebuggerPanel();
    verify(view).setVMName("info");
  }

  @Test
  public void testOnDebuggerDisconnected() {
    final String address = "";
    String title = "title";
    doReturn(title).when(this.constant).debuggerDisconnectedTitle();
    String description = "description";
    doReturn(description).when(this.constant).debuggerDisconnectedDescription(address);

    presenter.onDebuggerDisconnected();
    notificationManager.notify(eq(title), eq(description), eq(SUCCESS), eq(NOT_EMERGE_MODE));
  }

  @Test
  public void shouldSetNewValueOnValueChanged() throws Exception {
    SimpleValueDto valueDto = mock(SimpleValueDto.class);
    doReturn(promiseValue)
        .when(debugger)
        .getValue(eq(selectedVariable), eq(THREAD_ID), eq(FRAME_INDEX));
    doReturn(promiseValue).when(promiseValue).then((Operation<SimpleValueDto>) any());

    presenter.onValueChanged(selectedVariable, THREAD_ID, FRAME_INDEX);

    verify(promiseValue).then(operationValueCaptor.capture());
    operationValueCaptor.getValue().apply(valueDto);
    verify(debugger).getValue(eq(selectedVariable), eq(THREAD_ID), eq(FRAME_INDEX));
    verify(view).updateVariable(any(Variable.class));
  }

  @Test
  public void shouldClearPanelOnWorkspaceStopped() throws Exception {
    Promise promise = mock(Promise.class);
    when(promise.then(any(Operation.class))).thenReturn(promise);
    when(debugger.evaluate(anyString(), anyLong(), anyInt())).thenReturn(promise);
    WatchExpression watchExpression = mock(WatchExpression.class);
    when(watchExpression.getExpression()).thenReturn("expresion");

    presenter.onAddExpressionBtnClicked(watchExpression);
    presenter.onWorkspaceStopped(mock(WorkspaceStoppedEvent.class));

    verify(view).setVMName(eq(""));
    verify(view).setExecutionPoint(eq(null));
    verify(view).setThreadDump(eq(emptyList()), eq(-1L));
    verify(view).setFrames(eq(emptyList()));
    verify(view).removeAllVariables();
    verify(watchExpression).setResult(eq(""));
    verify(view).updateExpression(eq(watchExpression));
  }
}

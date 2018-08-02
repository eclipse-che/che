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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.part.perspectives.project.ProjectPerspective;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerView.ActiveBreakpointWrapper;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointContextMenuFactory;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The presenter provides debugging applications.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 * @author Oleksandr Andriienko
 */
@Singleton
public class DebuggerPresenter extends BasePresenter
    implements DebuggerView.ActionDelegate,
        DebuggerManagerObserver,
        BreakpointManagerObserver,
        WorkspaceStoppedEvent.Handler {
  private static final String TITLE = "Debug";

  private final DebuggerResources debuggerResources;
  private final ToolbarPresenter debuggerToolbar;
  private final ToolbarPresenter watchToolbar;
  private final BreakpointManager breakpointManager;
  private final NotificationManager notificationManager;
  private final DebuggerLocalizationConstant constant;
  private final DebuggerView view;
  private final DebuggerManager debuggerManager;
  private final WorkspaceAgent workspaceAgent;
  private final DebuggerLocationHandlerManager resourceHandlerManager;
  private final BreakpointContextMenuFactory breakpointContextMenuFactory;

  private List<Variable> variables;
  private List<WatchExpression> watchExpressions;
  private Map<Long, ? extends ThreadState> threadDump;
  private Location executionPoint;
  private DebuggerDescriptor debuggerDescriptor;

  @Inject
  public DebuggerPresenter(
      final DebuggerView view,
      final DebuggerLocalizationConstant constant,
      final BreakpointManager breakpointManager,
      final NotificationManager notificationManager,
      final DebuggerResources debuggerResources,
      final @DebuggerToolbar ToolbarPresenter debuggerToolbar,
      final @DebuggerWatchToolBar ToolbarPresenter watchToolbar,
      final DebuggerManager debuggerManager,
      final WorkspaceAgent workspaceAgent,
      final DebuggerLocationHandlerManager resourceHandlerManager,
      final BreakpointContextMenuFactory breakpointContextMenuFactory,
      final EventBus eventBus) {
    this.view = view;
    this.debuggerResources = debuggerResources;
    this.debuggerToolbar = debuggerToolbar;
    this.watchToolbar = watchToolbar;
    this.debuggerManager = debuggerManager;
    this.workspaceAgent = workspaceAgent;
    this.resourceHandlerManager = resourceHandlerManager;
    this.view.setDelegate(this);
    this.view.setTitle(TITLE);
    this.constant = constant;
    this.breakpointManager = breakpointManager;
    this.breakpointContextMenuFactory = breakpointContextMenuFactory;

    this.notificationManager = notificationManager;
    this.addRule(ProjectPerspective.PROJECT_PERSPECTIVE_ID);

    this.debuggerManager.addObserver(this);
    this.breakpointManager.addObserver(this);

    this.watchExpressions = new ArrayList<>();
    this.threadDump = new HashMap<>();

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);

    clearView();
    refreshBreakpoints();
    addDebuggerPanel();
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public SVGResource getTitleImage() {
    return debuggerResources.debug();
  }

  @Override
  public String getTitleToolTip() {
    return TITLE;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    debuggerToolbar.go(view.getDebuggerToolbarPanel());
    watchToolbar.go(view.getDebuggerWatchToolbarPanel());
  }

  @Override
  public void onExpandVariable(Variable variable) {
    Debugger debugger = debuggerManager.getActiveDebugger();

    if (debugger != null && debugger.isSuspended()) {
      long threadId = view.getSelectedThreadId();
      int frameIndex = view.getSelectedFrameIndex();

      debugger
          .getValue(variable, threadId, frameIndex)
          .then(
              value -> {
                if (isSameSelection(threadId, frameIndex)) {
                  MutableVariable updatedVariable =
                      new MutableVariableImpl(
                          variable.getType(),
                          variable.getName(),
                          value,
                          variable.getVariablePath(),
                          variable.isPrimitive());

                  view.expandVariable(updatedVariable);
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  @Override
  public void onSelectedThread(long threadId) {
    refreshView(threadId);
  }

  @Override
  public void onSelectedFrame(int frameIndex) {
    long threadId = view.getSelectedThreadId();
    refreshVariables(threadId, frameIndex);
    refreshWatchExpressions(threadId, frameIndex);

    ThreadState threadState = threadDump.get(threadId);
    if (threadState != null) {
      List<? extends StackFrameDump> frames = threadState.getFrames();
      if (frames.size() > frameIndex) {

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null && debugger.isSuspended()) {
          debugger
              .getStackFrameLocation(threadId, frameIndex)
              .then(
                  location -> {
                    if (isSameSelection(threadId, frameIndex)) {
                      open(location);
                    }
                  });
        }
      }
    }
  }

  @Override
  public void onAddExpressionBtnClicked(WatchExpression expression) {
    watchExpressions.add(expression);
    view.addExpression(expression);

    evaluateWatchExpression(expression, getSelectedThreadId(), getSelectedFrameIndex());
  }

  @Override
  public void onRemoveExpressionBtnClicked(WatchExpression expression) {
    watchExpressions.remove(expression);
    view.removeExpression(expression);
  }

  @Override
  public void onEditExpressionBtnClicked(WatchExpression expression) {
    expression.setResult("");
    view.updateExpression(expression);

    evaluateWatchExpression(expression, getSelectedThreadId(), getSelectedFrameIndex());
  }

  public long getSelectedThreadId() {
    return view.getSelectedThreadId();
  }

  public int getSelectedFrameIndex() {
    return view.getSelectedFrameIndex();
  }

  private void refreshBreakpoints() {
    List<Breakpoint> breakpoints = new ArrayList<>(breakpointManager.getAll());
    breakpoints.sort(
        (o1, o2) -> {
          Location location1 = o1.getLocation();
          Location location2 = o2.getLocation();
          int compare = location1.getTarget().compareTo(location2.getTarget());
          return (compare == 0 ? location1.getLineNumber() - location2.getLineNumber() : compare);
        });

    view.setBreakpoints(
        breakpoints
            .stream()
            .map(
                breakpoint ->
                    new ActiveBreakpointWrapper(breakpoint, breakpointManager.isActive(breakpoint)))
            .collect(Collectors.toList()));
  }

  protected void refreshVariables(long threadId, int frameIndex) {
    view.removeAllVariables();

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      Promise<? extends StackFrameDump> promise = debugger.getStackFrameDump(threadId, frameIndex);
      promise
          .then(
              stackFrameDump -> {
                if (isSameSelection(threadId, frameIndex) || view.getSelectedThreadId() == -1) {
                  variables = new LinkedList<>();
                  variables.addAll(stackFrameDump.getFields());
                  variables.addAll(stackFrameDump.getVariables());
                  view.setVariables(variables);
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  protected void refreshView() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      debugger
          .getThreadDump()
          .then(
              threadDump -> {
                DebuggerPresenter.this.threadDump =
                    threadDump.stream().collect(Collectors.toMap(ThreadStateDto::getId, ts -> ts));

                if (executionPoint != null) {
                  view.setThreadDump(threadDump, executionPoint.getThreadId());
                  refreshView(executionPoint.getThreadId());
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  protected void refreshView(long threadId) {
    ThreadState threadState = threadDump.get(threadId);
    if (threadState == null) {
      view.setFrames(Collections.emptyList());
      view.setThreadNotSuspendPlaceHolderVisible(false);
      refreshVariables(threadId, 0);
      refreshWatchExpressions(threadId, 0);
      return;
    }

    view.setThreadNotSuspendPlaceHolderVisible(!threadState.isSuspended());

    if (threadState.isSuspended()) {
      List<? extends StackFrameDump> frames = threadState.getFrames();
      view.setFrames(frames);
      refreshVariables(threadId, 0);
      refreshWatchExpressions(threadId, 0);
    } else {
      view.setFrames(Collections.emptyList());
      view.removeAllVariables();
      invalidateExpressions(constant.debuggerThreadNotSuspend());
    }
  }

  private void refreshWatchExpressions(long threadId, int frameIndex) {
    for (WatchExpression expression : watchExpressions) {
      expression.setResult("");
      view.updateExpression(expression);
    }

    for (WatchExpression expression : watchExpressions) {
      evaluateWatchExpression(expression, threadId, frameIndex);
    }
  }

  private void evaluateWatchExpression(WatchExpression expression, long threadId, int frameIndex) {
    Debugger activeDebugger = debuggerManager.getActiveDebugger();
    if (activeDebugger != null && activeDebugger.isSuspended()) {
      debuggerManager
          .getActiveDebugger()
          .evaluate(expression.getExpression(), threadId, frameIndex)
          .then(
              result -> {
                if (isSameSelection(threadId, frameIndex)) {
                  expression.setResult(result);
                  view.updateExpression(expression);
                }
              })
          .catchError(
              error -> {
                if (isSameSelection(threadId, frameIndex)) {
                  expression.setResult(error.getMessage());
                  view.updateExpression(expression);
                }
              });
    }
  }

  public WatchExpression getSelectedWatchExpression() {
    return view.getSelectedExpression();
  }

  public Variable getSelectedVariable() {
    return view.getSelectedVariable();
  }

  public ToolbarPresenter getDebuggerToolbar() {
    return debuggerToolbar;
  }

  public ToolbarPresenter getWatchExpressionToolbar() {
    return watchToolbar;
  }

  @Override
  public void onDebuggerAttached(final DebuggerDescriptor debuggerDescriptor) {
    this.debuggerDescriptor = debuggerDescriptor;
    view.setVMName(debuggerDescriptor.getInfo());
    showDebuggerPanel();
  }

  @Override
  public void onDebuggerDisconnected() {
    String address = debuggerDescriptor != null ? debuggerDescriptor.getAddress() : "";
    String content = constant.debuggerDisconnectedDescription(address);

    notificationManager.notify(
        constant.debuggerDisconnectedTitle(), content, SUCCESS, NOT_EMERGE_MODE);

    clearView();
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointUpdated(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {
    refreshBreakpoints();
  }

  @Override
  public void onAllBreakpointsDeleted() {
    refreshBreakpoints();
  }

  @Override
  public void onPreStepInto() {
    clearExecutionPoint();
  }

  @Override
  public void onPreStepOut() {
    clearExecutionPoint();
  }

  @Override
  public void onPreStepOver() {
    clearExecutionPoint();
  }

  @Override
  public void onPreResume() {
    clearExecutionPoint();
  }

  private void invalidateExpressions(String result) {
    for (WatchExpression expression : watchExpressions) {
      expression.setResult(result);
      view.updateExpression(expression);
    }
  }

  private void open(Location location) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      DebuggerLocationHandler handler = resourceHandlerManager.getOrDefault(location);

      handler.open(
          location,
          new AsyncCallback<VirtualFile>() {
            @Override
            public void onFailure(Throwable caught) {}

            @Override
            public void onSuccess(VirtualFile result) {}
          });
    }
  }

  private void clearExecutionPoint() {
    variables = new ArrayList<>();
    threadDump = new HashMap<>();
    executionPoint = null;
    view.setExecutionPoint(null);
    view.setThreadDump(emptyList(), -1);
    view.setFrames(emptyList());
    view.removeAllVariables();
    view.setThreadNotSuspendPlaceHolderVisible(false);
    invalidateExpressions("");
  }

  private void clearView() {
    clearExecutionPoint();
    debuggerDescriptor = null;
    view.setVMName(null);
    view.setBreakpoints(emptyList());
  }

  private boolean isSameSelection(long threadId, int frameIndex) {
    return view.getSelectedThreadId() == threadId && view.getSelectedFrameIndex() == frameIndex;
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    executionPoint = location;
    view.setExecutionPoint(executionPoint);
    refreshView();
  }

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {
    if (view.getSelectedThreadId() == threadId && view.getSelectedFrameIndex() == frameIndex) {
      Debugger debugger = debuggerManager.getActiveDebugger();
      if (debugger != null && debugger.isSuspended()) {
        Promise<? extends SimpleValue> promise = debugger.getValue(variable, threadId, frameIndex);
        promise
            .then(
                value -> {
                  MutableVariable updatedVariable =
                      variable instanceof MutableVariable
                          ? ((MutableVariable) variable)
                          : new MutableVariableImpl(variable);
                  updatedVariable.setValue(value);
                  view.updateVariable(updatedVariable);
                })
            .catchError(
                error -> {
                  Log.error(DebuggerPresenter.class, error.getCause());
                });
      }
    }
  }

  @Override
  public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {}

  private void addDebuggerPanel() {
    if (partStack == null || !partStack.containsPart(this)) {
      workspaceAgent.openPart(this, PartStackType.INFORMATION);
    }
  }

  public void showDebuggerPanel() {
    partStack.setActivePart(this);
  }

  public void hideDebuggerPanel() {
    partStack.minimize();
  }

  public boolean isDebuggerPanelOpened() {
    return partStack.getActivePart() == this;
  }

  @Override
  public void onBreakpointContextMenu(int clientX, int clientY, Breakpoint breakpoint) {
    Scheduler.get()
        .scheduleDeferred(
            () -> breakpointContextMenuFactory.newContextMenu(breakpoint).show(clientX, clientY));
  }

  @Override
  public void onBreakpointDoubleClick(Breakpoint breakpoint) {
    Location location = breakpoint.getLocation();
    resourceHandlerManager
        .getOrDefault(location)
        .open(
            location,
            new AsyncCallback<VirtualFile>() {
              @Override
              public void onFailure(Throwable caught) {}

              @Override
              public void onSuccess(VirtualFile result) {}
            });
  }

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    clearView();
  }
}

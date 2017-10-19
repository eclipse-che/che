/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.part.perspectives.project.ProjectPerspective;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
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
 */
@Singleton
public class DebuggerPresenter extends BasePresenter
    implements DebuggerView.ActionDelegate, DebuggerManagerObserver, BreakpointManagerObserver {
  private static final String TITLE = "Debug";

  private final DebuggerResources debuggerResources;
  private final ToolbarPresenter debuggerToolbar;
  private final BreakpointManager breakpointManager;
  private final NotificationManager notificationManager;
  private final DebuggerLocalizationConstant constant;
  private final DebuggerView view;
  private final DebuggerManager debuggerManager;
  private final WorkspaceAgent workspaceAgent;
  private final DebuggerResourceHandlerFactory resourceHandlerManager;
  private final BreakpointContextMenuFactory breakpointContextMenuFactory;

  private List<Variable> variables;
  private List<? extends ThreadState> threadDump;
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
      final DebuggerManager debuggerManager,
      final WorkspaceAgent workspaceAgent,
      final DebuggerResourceHandlerFactory resourceHandlerManager,
      final BreakpointContextMenuFactory breakpointContextMenuFactory) {
    this.view = view;
    this.debuggerResources = debuggerResources;
    this.debuggerToolbar = debuggerToolbar;
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

    resetView();
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
  }

  @Override
  public void onExpandVariablesTree(MutableVariable variable) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      Promise<? extends SimpleValue> promise =
          debugger.getValue(variable, view.getSelectedThreadId(), view.getSelectedFrameIndex());

      promise
          .then(
              value -> {
                view.setVariableValue(variable, value);
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  @Override
  public void onSelectedThread(long threadId) {
    updateStackFrameDump(threadId);
    onSelectedFrame(0);
  }

  @Override
  public void onSelectedFrame(int frameIndex) {
    long selectedThreadId = view.getSelectedThreadId();
    updateVariables(selectedThreadId, frameIndex);

    for (ThreadState ts : threadDump) {
      if (ts.getId() == selectedThreadId) {
        StackFrameDump stackFrameDump = ts.getFrames().get(frameIndex);
        open(stackFrameDump.getLocation());
      }
    }
  }

  protected void open(Location location) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      DebuggerResourceHandler handler =
          resourceHandlerManager.getOrDefault(debugger.getDebuggerType());

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

  public long getSelectedThreadId() {
    return view.getSelectedThreadId();
  }

  public int getSelectedFrameIndex() {
    return view.getSelectedFrameIndex();
  }

  protected void updateBreakpoints() {
    view.setBreakpoints(breakpointManager.getBreakpointList());
  }

  protected void updateThreadDump() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      debugger
          .getThreadDump()
          .then(
              threadDump -> {
                DebuggerPresenter.this.threadDump = threadDump;
                if (executionPoint != null) {
                  view.setThreadDump(threadDump, executionPoint.getThreadId());
                  updateStackFrameDump(executionPoint.getThreadId());
                  updateVariables(executionPoint.getThreadId(), 0);
                }
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  protected void updateStackFrameDump(long threadId) {
    for (ThreadState ts : threadDump) {
      if (ts.getId() == threadId) {
        view.setFrames(ts.getFrames());
      }
    }
  }

  protected void updateVariables(long threadId, int frameIndex) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      Promise<? extends StackFrameDump> promise = debugger.getStackFrameDump(threadId, frameIndex);
      promise
          .then(
              stackFrameDump -> {
                if ((threadId == view.getSelectedThreadId()
                        && frameIndex == view.getSelectedFrameIndex())
                    || view.getSelectedThreadId() == -1) {

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

  public Variable getSelectedVariable() {
    return view.getSelectedDebuggerVariable();
  }

  public ToolbarPresenter getDebuggerToolbar() {
    return debuggerToolbar;
  }

  @Override
  public void onDebuggerAttached(
      final DebuggerDescriptor debuggerDescriptor, Promise<Void> connect) {

    final String address = debuggerDescriptor.getAddress();
    final StatusNotification notification =
        notificationManager.notify(constant.debuggerConnectingTitle(address), PROGRESS, FLOAT_MODE);

    connect
        .then(
            aVoid -> {
              DebuggerPresenter.this.debuggerDescriptor = debuggerDescriptor;

              notification.setTitle(constant.debuggerConnectedTitle());
              notification.setContent(constant.debuggerConnectedDescription(address));
              notification.setStatus(SUCCESS);

              view.setVMName(debuggerDescriptor.getInfo());
              showDebuggerPanel();
            })
        .catchError(
            error -> {
              notification.setTitle(
                  constant.failedToConnectToRemoteDebuggerDescription(address, error.getMessage()));
              notification.setStatus(FAIL);
              notification.setDisplayMode(FLOAT_MODE);
            });
  }

  @Override
  public void onDebuggerDisconnected() {
    String address = debuggerDescriptor != null ? debuggerDescriptor.getAddress() : "";
    String content = constant.debuggerDisconnectedDescription(address);

    notificationManager.notify(
        constant.debuggerDisconnectedTitle(), content, SUCCESS, NOT_EMERGE_MODE);

    resetView();
  }

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {
    updateBreakpoints();
  }

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {
    updateBreakpoints();
  }

  @Override
  public void onAllBreakpointsDeleted() {
    updateBreakpoints();
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

  private void clearExecutionPoint() {
    executionPoint = null;
    variables = new ArrayList<>();
    threadDump = new ArrayList<>();
    view.setExecutionPoint(null);
    view.setThreadDump(emptyList(), -1);
    view.setFrames(emptyList());
    view.setVariables(emptyList());
  }

  private void resetView() {
    variables = new ArrayList<>();
    threadDump = new ArrayList<>();
    executionPoint = null;
    debuggerDescriptor = null;
    updateBreakpoints();
    view.setVMName("");
    view.setExecutionPoint(null);
    view.setThreadDump(emptyList(), -1);
    view.setFrames(emptyList());
    view.setVariables(emptyList());
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    executionPoint = location;
    view.setExecutionPoint(executionPoint);
    updateThreadDump();
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
                  view.setVariableValue(variable, value);
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

  public void addDebuggerPanel() {
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
}

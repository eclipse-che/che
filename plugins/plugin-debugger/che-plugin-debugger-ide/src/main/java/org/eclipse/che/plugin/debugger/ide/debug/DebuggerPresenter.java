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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
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
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
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

  private MutableVariable selectedVariable;
  private List<Variable> variables;
  private List<? extends ThreadDump> threadDump;
  private Location executionPoint;
  private long selectedThreadId;
  private int selectedFrameIndex;
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
      final DebuggerResourceHandlerFactory resourceHandlerManager) {
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

    this.notificationManager = notificationManager;
    this.addRule(ProjectPerspective.PROJECT_PERSPECTIVE_ID);

    this.debuggerManager.addObserver(this);
    this.breakpointManager.addObserver(this);

    resetState();
    updateBreakpoints();
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
    view.setBreakpoints(breakpointManager.getBreakpointList());

    container.setWidget(view);
    debuggerToolbar.go(view.getDebuggerToolbarPanel());
  }

  @Override
  public void onExpandVariablesTree() {
    List<? extends Variable> rootVariables = selectedVariable.getVariables();
    if (rootVariables.isEmpty()) {
      Debugger debugger = debuggerManager.getActiveDebugger();
      if (debugger != null) {
        Promise<? extends SimpleValue> promise =
            debugger.getValue(selectedVariable, selectedThreadId, selectedFrameIndex);

        promise
            .then(
                value -> {
                  selectedVariable.setValue(value);
                  view.setVariablesIntoSelectedVariable(value.getVariables());
                  view.updateSelectedVariable();
                })
            .catchError(
                error -> {
                  notificationManager.notify(
                      constant.failedToGetVariableValueTitle(),
                      error.getMessage(),
                      FAIL,
                      FLOAT_MODE);
                });
      }
    }
  }

  @Override
  public void onSelectedVariableElement(@NotNull MutableVariable variable) {
    this.selectedVariable = variable;
  }

  @Override
  public void onSelectedThread(long threadId) {
    selectedThreadId = threadId;
    selectedFrameIndex = 0;

    for (ThreadDump td : threadDump) {
      if (td.getId() == selectedThreadId) {
        view.setFrames(td.getFrames());
      }
    }
    updateStackFrameDump();
  }

  @Override
  public void onSelectedFrame(int frameIndex) {
    if (selectedFrameIndex != frameIndex) {
      updateStackFrameDump();
    }

    selectedFrameIndex = frameIndex;

    for (ThreadDump td : threadDump) {
      if (td.getId() == selectedThreadId) {
        final StackFrameDump stackFrameDump = td.getFrames().get(selectedFrameIndex);

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
          DebuggerResourceHandler handler =
              resourceHandlerManager.getOrDefault(debugger.getDebuggerType());
          handler.open(
              stackFrameDump.getLocation(),
              new AsyncCallback<VirtualFile>() {
                @Override
                public void onFailure(Throwable caught) {}

                @Override
                public void onSuccess(VirtualFile result) {}
              });
        }
      }
    }
  }

  public long getSelectedThreadId() {
    return selectedThreadId;
  }

  public int getSelectedFrameIndex() {
    return selectedFrameIndex;
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

  public boolean isDebuggerPanelPresent() {
    return partStack != null && partStack.containsPart(this);
  }

  private void resetState() {
    variables = new ArrayList<>();
    threadDump = new ArrayList<>();
    selectedThreadId = -1;
    selectedFrameIndex = -1;
    selectedVariable = null;
    executionPoint = null;
    debuggerDescriptor = null;
    view.setVariables(emptyList());
    view.setVMName("");
    view.setExecutionPoint(null);
    view.setThreads(emptyList(), -1);
    view.setFrames(emptyList());
  }

  public void updateView() {
    if (debuggerDescriptor == null) {
      view.setVMName("");
    } else {
      view.setVMName(debuggerDescriptor.getInfo());
    }

    view.setExecutionPoint(executionPoint);

    view.setBreakpoints(breakpointManager.getBreakpointList());
    updateThreadDump();
    updateStackFrameDump();

    showView();
  }

  protected void updateBreakpoints() {
    view.setBreakpoints(breakpointManager.getBreakpointList());

    if (!breakpointManager.getBreakpointList().isEmpty() && !isDebuggerPanelPresent()) {
      showView();
      showDebuggerPanel();
    }
  }

  public void showView() {
    if (partStack == null || !partStack.containsPart(this)) {
      workspaceAgent.openPart(this, PartStackType.INFORMATION);
    }
  }

  protected void updateThreadDump() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      debugger
          .getThreadDump()
          .then(
              threadDump -> {
                DebuggerPresenter.this.threadDump = threadDump;
                view.setThreads(threadDump, selectedThreadId);
                onSelectedThread(selectedThreadId);
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  protected void updateStackFrameDump() {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null && debugger.isSuspended()) {
      Promise<? extends StackFrameDump> promise =
          debugger.getStackFrameDump(selectedThreadId, selectedFrameIndex);
      promise
          .then(
              stackFrameDump -> {
                variables = new ArrayList<>();
                variables.addAll(stackFrameDump.getFields());
                variables.addAll(stackFrameDump.getVariables());

                view.setVariables(variables);
              })
          .catchError(
              error -> {
                Log.error(DebuggerPresenter.class, error.getCause());
              });
    }
  }

  /** @return selected variable in variables tree or null if no selected variables */
  public Variable getSelectedVariable() {
    return selectedVariable;
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

              updateView();
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

    resetState();
    updateView();
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
    resetState();
  }

  @Override
  public void onPreStepOut() {
    resetState();
  }

  @Override
  public void onPreStepOver() {
    resetState();
  }

  @Override
  public void onPreResume() {
    resetState();
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    executionPoint = location;
    selectedThreadId = executionPoint.getThreadId();
    selectedFrameIndex = 0;

    updateView();
  }

  @Override
  public void onValueChanged(List<String> path, String newValue) {
    updateStackFrameDump(); // TODO
  }

  @Override
  public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {}
}

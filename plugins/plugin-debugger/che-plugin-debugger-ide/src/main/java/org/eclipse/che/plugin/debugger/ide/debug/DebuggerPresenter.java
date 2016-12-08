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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
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

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

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
public class DebuggerPresenter extends BasePresenter implements DebuggerView.ActionDelegate,
                                                                DebuggerManagerObserver,
                                                                BreakpointManagerObserver {
    private static final String TITLE = "Debug";

    private final DebuggerResources            debuggerResources;
    private final ToolbarPresenter             debuggerToolbar;
    private final BreakpointManager            breakpointManager;
    private final NotificationManager          notificationManager;
    private final DebuggerLocalizationConstant constant;
    private final DebuggerView                 view;
    private final DebuggerManager              debuggerManager;
    private final WorkspaceAgent               workspaceAgent;

    private MutableVariable    selectedVariable;
    private List<Variable>     variables;
    private DebuggerDescriptor debuggerDescriptor;
    private Location           executionPoint;

    @Inject
    public DebuggerPresenter(final DebuggerView view,
                             final DebuggerLocalizationConstant constant,
                             final BreakpointManager breakpointManager,
                             final NotificationManager notificationManager,
                             final DebuggerResources debuggerResources,
                             final @DebuggerToolbar ToolbarPresenter debuggerToolbar,
                             final DebuggerManager debuggerManager,
                             final WorkspaceAgent workspaceAgent) {
        this.view = view;
        this.debuggerResources = debuggerResources;
        this.debuggerToolbar = debuggerToolbar;
        this.debuggerManager = debuggerManager;
        this.workspaceAgent = workspaceAgent;
        this.view.setDelegate(this);
        this.view.setTitle(TITLE);
        this.constant = constant;
        this.breakpointManager = breakpointManager;
        this.variables = new ArrayList<>();
        this.notificationManager = notificationManager;
        this.addRule(ProjectPerspective.PROJECT_PERSPECTIVE_ID);

        this.debuggerManager.addObserver(this);
        this.breakpointManager.addObserver(this);

        if (!breakpointManager.getBreakpointList().isEmpty()) {
            updateBreakpoints();
        }
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
        view.setVariables(variables);
        container.setWidget(view);
        debuggerToolbar.go(view.getDebuggerToolbarPanel());
    }

    @Override
    public void onExpandVariablesTree() {
        List<? extends Variable> rootVariables = selectedVariable.getVariables();
        if (rootVariables.isEmpty()) {
            Debugger debugger = debuggerManager.getActiveDebugger();
            if (debugger != null) {
                Promise<SimpleValue> promise = debugger.getValue(selectedVariable);

                promise.then(new Operation<SimpleValue>() {
                    @Override
                    public void apply(SimpleValue arg) throws OperationException {
                        selectedVariable.setValue(arg.getValue());
                        view.setVariablesIntoSelectedVariable(arg.getVariables());
                        view.updateSelectedVariable();
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        notificationManager.notify(constant.failedToGetVariableValueTitle(), arg.getMessage(), FAIL, FLOAT_MODE);
                    }
                });
            }
        }
    }

    @Override
    public void onSelectedVariableElement(@NotNull MutableVariable variable) {
        this.selectedVariable = variable;
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

    private void resetStates() {
        variables.clear();
        view.setVariables(variables);
        view.setVMName("");
        view.setExecutionPoint(null);
        selectedVariable = null;
        executionPoint = null;
    }

    public void showAndUpdateView() {
        if (debuggerDescriptor == null) {
            view.setVMName("");
        } else {
            view.setVMName(debuggerDescriptor.getInfo());
        }
        if (executionPoint != null) {
            view.setExecutionPoint(executionPoint);
        }
        view.setBreakpoints(breakpointManager.getBreakpointList());
        updateStackFrameDump();

        showView();
    }

    protected void updateBreakpoints() {
        view.setBreakpoints(breakpointManager.getBreakpointList());

        if (!isDebuggerPanelPresent()) {
            showView();
            showDebuggerPanel();
        }
    }

    public void showView() {
        if (partStack == null || !partStack.containsPart(this)) {
            workspaceAgent.openPart(this, PartStackType.INFORMATION);
        }
    }

    private void updateStackFrameDump() {
        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null && executionPoint != null) {
            Promise<StackFrameDump> promise = debugger.dumpStackFrame();
            promise.then(new Operation<StackFrameDump>() {
                @Override
                public void apply(StackFrameDump arg) throws OperationException {
                    variables = new ArrayList<>();
                    variables.addAll(arg.getFields());
                    variables.addAll(arg.getVariables());

                    view.setVariables(variables);
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(DebuggerPresenter.class, arg.getCause());
                }
            });
        }
    }

    /**
     * @return selected variable in variables tree or null if no selected variables
     */
    public Variable getSelectedVariable() {
        return selectedVariable;
    }

    public ToolbarPresenter getDebuggerToolbar() {
        return debuggerToolbar;
    }

    @Override
    public void onDebuggerAttached(final DebuggerDescriptor debuggerDescriptor, Promise<Void> connect) {
        final String address = debuggerDescriptor.getAddress();
        final StatusNotification notification = notificationManager.notify(constant.debuggerConnectingTitle(address), PROGRESS, FLOAT_MODE);

        connect.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                DebuggerPresenter.this.debuggerDescriptor = debuggerDescriptor;

                notification.setTitle(constant.debuggerConnectedTitle());
                notification.setContent(constant.debuggerConnectedDescription(address));
                notification.setStatus(SUCCESS);

                showAndUpdateView();
                showDebuggerPanel();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notification.setTitle(constant.failedToConnectToRemoteDebuggerDescription(address, arg.getMessage()));
                notification.setStatus(FAIL);
                notification.setDisplayMode(FLOAT_MODE);
            }
        });
    }

    @Override
    public void onDebuggerDisconnected() {
        String address = debuggerDescriptor != null ? debuggerDescriptor.getAddress() : "";
        String content = constant.debuggerDisconnectedDescription(address);
        notificationManager.notify(constant.debuggerDisconnectedTitle(), content, SUCCESS, NOT_EMERGE_MODE);

        executionPoint = null;
        debuggerDescriptor = null;

        resetStates();
        showAndUpdateView();
    }

    @Override
    public void onBreakpointAdded(Breakpoint breakpoint) {
        updateBreakpoints();
    }

    @Override
    public void onBreakpointActivated(String filePath, int lineNumber) { }

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
        resetStates();
    }

    @Override
    public void onPreStepOut() {
        resetStates();
    }

    @Override
    public void onPreStepOver() {
        resetStates();
    }

    @Override
    public void onPreResume() {
        resetStates();
    }

    @Override
    public void onBreakpointStopped(String filePath, String className, int lineNumber) {
        executionPoint = new LocationImpl(className, lineNumber);
        showAndUpdateView();
    }

    @Override
    public void onValueChanged(List<String> path, String newValue) {
        updateStackFrameDump();
    }

    @Override
    public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) { }
}

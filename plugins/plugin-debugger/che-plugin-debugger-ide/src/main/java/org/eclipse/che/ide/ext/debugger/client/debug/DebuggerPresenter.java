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
package org.eclipse.che.ide.ext.debugger.client.debug;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
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
    private final DtoFactory                   dtoFactory;
    private final NotificationManager          notificationManager;
    private final DebuggerLocalizationConstant constant;
    private final DebuggerView                 view;
    private final DebuggerManager              debuggerManager;
    private final WorkspaceAgent               workspaceAgent;

    private DebuggerVariable       selectedVariable;
    private List<DebuggerVariable> variables;
    private DebuggerDescriptor     debuggerDescriptor;
    private Location               executionPoint;

    @Inject
    public DebuggerPresenter(final DebuggerView view,
                             final DebuggerLocalizationConstant constant,
                             final BreakpointManager breakpointManager,
                             final NotificationManager notificationManager,
                             final DebuggerResources debuggerResources,
                             final @DebuggerToolbar ToolbarPresenter debuggerToolbar,
                             final DtoFactory dtoFactory, DebuggerManager debuggerManager,
                             final WorkspaceAgent workspaceAgent) {
        this.view = view;
        this.debuggerResources = debuggerResources;
        this.debuggerToolbar = debuggerToolbar;
        this.dtoFactory = dtoFactory;
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
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Override
    public SVGResource getTitleSVGImage() {
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
        List<DebuggerVariable> rootVariables = selectedVariable.getVariables();
        if (rootVariables.isEmpty()) {
            Debugger debugger = debuggerManager.getActiveDebugger();
            if (debugger != null) {
                Promise<String> promise = debugger.getValue(dtoFactory.toJson(selectedVariable.getVariable()));

                promise.then(new Operation<String>() {
                    @Override
                    public void apply(String arg) throws OperationException {
                        List<Variable> variables = dtoFactory.createListDtoFromJson(arg, Variable.class);
                        List<DebuggerVariable> debuggerVariables = getDebuggerVariables(variables);

                        view.setVariablesIntoSelectedVariable(debuggerVariables);
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

    @NotNull
    private List<DebuggerVariable> getDebuggerVariables(@NotNull List<Variable> variables) {
        List<DebuggerVariable> debuggerVariables = new ArrayList<>(variables.size());

        for (Variable variable : variables) {
            debuggerVariables.add(new DebuggerVariable(variable));
        }

        return debuggerVariables;
    }


    @Override
    public void onSelectedVariableElement(@NotNull DebuggerVariable variable) {
        this.selectedVariable = variable;
    }

    public void showDebuggerPanel() {
        partStack.setActivePart(this);
    }

    public void hideDebuggerPanel() {
        partStack.hidePart(this);
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
            Promise<String> promise = debugger.getStackFrameDump();
            promise.then(new Operation<String>() {
                @Override
                public void apply(String arg) throws OperationException {
                    StackFrameDump dump = dtoFactory.createDtoFromJson(arg, StackFrameDump.class);

                    List<Variable> variables = new ArrayList<>();
                    variables.addAll(dump.getFields());
                    variables.addAll(dump.getLocalVariables());

                    List<DebuggerVariable> debuggerVariables = getDebuggerVariables(variables);

                    view.setVariables(debuggerVariables);
                    DebuggerPresenter.this.variables = debuggerVariables;
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
    public DebuggerVariable getSelectedVariable() {
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
                notification.setTitle(constant.failedToConnectToRemoteDebuggerDescription(address));
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
    public void onPreStepIn() {
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
        executionPoint = dtoFactory.createDto(Location.class);
        executionPoint.withClassName(className);
        executionPoint.withLineNumber(lineNumber);
        showAndUpdateView();
    }

    @Override
    public void onValueChanged(List<String> path, String newValue) {
        updateStackFrameDump();
    }

    @Override
    public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) { }
}

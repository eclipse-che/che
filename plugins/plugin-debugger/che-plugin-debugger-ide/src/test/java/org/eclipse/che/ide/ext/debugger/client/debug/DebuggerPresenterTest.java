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

import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.BaseTest;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link DebuggerPresenter} functionality.
 *
 * @author Dmytro Nochevnov
 */
public class DebuggerPresenterTest extends BaseTest {
    public static final String ERROR_MESSAGE = "error message";

    @Mock
    private DebuggerView                 view;
    @Mock
    private DebuggerLocalizationConstant constant;
    @Mock
    private BreakpointManager            breakpointManager;
    @Mock
    private NotificationManager          notificationManager;
    @Mock
    private DebuggerResources            debuggerResources;
    @Mock
    @DebuggerToolbar
    private ToolbarPresenter             debuggerToolbar;
    @Mock
    private FqnResolverFactory           fqnResolverFactory;
    @Mock
    private DtoFactory                   dtoFactory;
    @Mock
    private DebuggerManager              debuggerManager;
    @Mock
    private WorkspaceAgent               workspaceAgent;
    @Mock
    private FileTypeRegistry             fileTypeRegistry;

    @Mock
    private Debugger         debugger;
    @Mock
    private DebuggerVariable selectedVariable;
    @Mock
    private VirtualFile      file;

    @Mock
    private Promise<String> promiseString;
    @Mock
    private Promise<Void>   promiseVoid;
    @Mock
    private PromiseError    promiseError;


    @Captor
    private ArgumentCaptor<Operation<PromiseError>> operationPromiseErrorCaptor;
    @Captor
    private ArgumentCaptor<Operation<Void>>         operationVoidCaptor;
    @Captor
    private ArgumentCaptor<Operation<String>>       operationStringCaptor;

    private DebuggerPresenter presenter;

    @Before
    public void setup() {
        doReturn(debugger).when(debuggerManager).getActiveDebugger();
        doReturn(ERROR_MESSAGE).when(promiseError).getMessage();

        presenter = spy(new DebuggerPresenter(view, constant, breakpointManager, notificationManager, debuggerResources, debuggerToolbar,
                                              dtoFactory, debuggerManager, workspaceAgent));
        doNothing().when(presenter).showDebuggerPanel();

        presenter.onSelectedVariableElement(selectedVariable);

        FileType fileType = mock(FileType.class);
        doReturn(Collections.singletonList("application/java")).when(fileType).getMimeTypes();
        doReturn(fileType).when(fileTypeRegistry).getFileTypeByFile(eq(file));
    }

    @Test
    public void testGo() {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        List<Breakpoint> breakpoints = new ArrayList<>();

        doReturn(breakpoints).when(breakpointManager).getBreakpointList();
        doReturn(container).when(view).getDebuggerToolbarPanel();

        presenter.go(container);

        verify(view).setBreakpoints(breakpoints);
        verify(view).setVariables(any());
        verify(container).setWidget(view);
        verify(debuggerToolbar).go(container);
    }

    @Test
    public void testOnExpandVariablesTree() throws OperationException {
        final String json = "json";

        List<DebuggerVariable> rootVariables = mock(List.class);
        doReturn(true).when(rootVariables).isEmpty();
        doReturn(rootVariables).when(selectedVariable).getVariables();

        Variable variable = mock(Variable.class);
        doReturn(variable).when(selectedVariable).getVariable();
        doReturn(json).when(dtoFactory).toJson(variable);

        List<Variable> variables = ImmutableList.of(variable);
        doReturn(variables).when(dtoFactory).createListDtoFromJson(json, Variable.class);

        doReturn(promiseString).when(debugger).getValue(json);
        doReturn(promiseString).when(promiseString).then((Operation<String>)any());

        presenter.onExpandVariablesTree();

        verify(promiseString).then(operationStringCaptor.capture());
        operationStringCaptor.getValue().apply(json);
        verify(view).setVariablesIntoSelectedVariable(any());
        verify(view).updateSelectedVariable();

        verify(promiseString).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        notificationManager.notify(any(), eq(ERROR_MESSAGE), eq(FAIL), eq(true));
        verify(constant).failedToGetVariableValueTitle();
    }

    @Test
    public void testShowAndUpdateView() {
        presenter.showAndUpdateView();
        verify(view).setVMName(eq(""));
    }

    @Test
    public void testOnDebuggerAttached() {
        DebuggerDescriptor debuggerDescriptor = mock(DebuggerDescriptor.class);
        final String address = "address";
        doReturn(address).when(debuggerDescriptor).getAddress();

        doReturn(promiseVoid).when(promiseVoid).then((Operation<Void>)any());

        String title = "title";
        doReturn(title).when(this.constant).debuggerConnectingTitle(address);

        presenter.onDebuggerAttached(debuggerDescriptor, promiseVoid);
        notificationManager.notify(eq(address), eq(PROGRESS), eq(true));
    }

    @Test
    public void testOnDebuggerDisconnected() {
        final String address = "";
        String title = "title";
        doReturn(title).when(this.constant).debuggerDisconnectedTitle();
        String description = "description";
        doReturn(description).when(this.constant).debuggerDisconnectedDescription(address);

        presenter.onDebuggerDisconnected();
        notificationManager.notify(eq(title), eq(description), eq(SUCCESS), eq(false));
    }

    @Test
    public void testOnPreIn() {
        presenter.onPreStepIn();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(true), eq(null));
    }

    @Test
    public void testOnPreOut() {
        presenter.onPreStepOut();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(true), eq(null));
    }

    @Test
    public void testOnPreOver() {
        presenter.onPreStepOver();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(true), eq(null));
    }

    @Test
    public void testOnPreResume() {
        presenter.onPreResume();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(true), eq(null));
    }

    @Test
    public void testOnBreakpointAdded() {
        Breakpoint breakpoint = mock(Breakpoint.class);

        List<Breakpoint> breakpoints = new ArrayList<>();
        doReturn(breakpoints).when(breakpointManager).getBreakpointList();

        presenter.onBreakpointAdded(breakpoint);
        verify(presenter).updateBreakpoints();
        verify(breakpointManager, times(2)).getBreakpointList();
        verify(view).setBreakpoints(any());
    }

    @Test
    public void testOnBreakpointDeleted() {
        Breakpoint breakpoint = mock(Breakpoint.class);

        List<Breakpoint> breakpoints = new ArrayList<>();
        doReturn(breakpoints).when(breakpointManager).getBreakpointList();

        presenter.onBreakpointDeleted(breakpoint);
        verify(breakpointManager, times(2)).getBreakpointList();
        verify(view).setBreakpoints(any());
    }

    @Test
    public void testOnAllBreakpointsDeleted() {
        List<Breakpoint> breakpoints = new ArrayList<>();
        doReturn(breakpoints).when(breakpointManager).getBreakpointList();

        presenter.onAllBreakpointsDeleted();
        verify(breakpointManager, times(2)).getBreakpointList();
        verify(view).setBreakpoints(any());
    }

    @Test
    public void testOnBreakpointStopped() {
        String filePath = "filePath";
        String className = "className";
        int lineNumber = 40;

        Location executionPoint = mock(Location.class);
        doReturn(executionPoint).when(dtoFactory).createDto(Location.class);

        doReturn(promiseString).when(debugger).getStackFrameDump();
        doReturn(promiseString).when(promiseString).then((Operation<String>)any());

        presenter.onBreakpointStopped(filePath, className, lineNumber);
        verify(executionPoint).withClassName(eq(className));
        verify(executionPoint).withLineNumber(eq(lineNumber));

        verify(view).setVMName(eq(""));
    }

    @Test
    public void testOnValueChanged() {
        doReturn(promiseString).when(debugger).getStackFrameDump();
        doReturn(promiseString).when(promiseString).then((Operation<String>)any());

        ArrayList<String> path = new ArrayList<>();
        String newValue = "newValue";
        presenter.onValueChanged(path, newValue);
    }
}

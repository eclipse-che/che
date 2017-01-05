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

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.dto.SimpleValueDto;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.plugin.debugger.ide.BaseTest;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
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
    private DtoFactory                   dtoFactory;
    @Mock
    private DebuggerManager              debuggerManager;
    @Mock
    private WorkspaceAgent               workspaceAgent;
    @Mock
    private FileTypeRegistry             fileTypeRegistry;

    @Mock
    private Debugger        debugger;
    @Mock
    private MutableVariable selectedVariable;
    @Mock
    private VirtualFile     file;

    @Mock
    private Promise<String>         promiseString;
    @Mock
    private Promise<SimpleValueDto> promiseValue;
    @Mock
    private Promise<Void>           promiseVoid;
    @Mock
    private PromiseError            promiseError;


    @Captor
    private ArgumentCaptor<Operation<PromiseError>>   operationPromiseErrorCaptor;
    @Captor
    private ArgumentCaptor<Operation<Void>>           operationVoidCaptor;
    @Captor
    private ArgumentCaptor<Operation<String>>         operationStringCaptor;
    @Captor
    private ArgumentCaptor<Operation<SimpleValueDto>> operationValueCaptor;

    private DebuggerPresenter presenter;

    @Before
    public void setup() {
        doReturn(debugger).when(debuggerManager).getActiveDebugger();
        doReturn(ERROR_MESSAGE).when(promiseError).getMessage();

        presenter = spy(new DebuggerPresenter(view, constant, breakpointManager, notificationManager, debuggerResources, debuggerToolbar,
                                              debuggerManager, workspaceAgent));
        doNothing().when(presenter).showDebuggerPanel();

        presenter.onSelectedVariableElement(selectedVariable);

        FileType fileType = mock(FileType.class);
        doReturn("java").when(fileType).getExtension();
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
        SimpleValueDto valueDto = mock(SimpleValueDto.class);

        List<MutableVariable> rootVariables = mock(List.class);
        doReturn(true).when(rootVariables).isEmpty();
        doReturn(rootVariables).when(selectedVariable).getVariables();

        doReturn(promiseValue).when(debugger).getValue(selectedVariable);
        doReturn(promiseValue).when(promiseValue).then((Operation<SimpleValueDto>)any());

        presenter.onExpandVariablesTree();

        verify(promiseValue).then(operationValueCaptor.capture());
        operationValueCaptor.getValue().apply(valueDto);
        verify(view).setVariablesIntoSelectedVariable(any());
        verify(view).updateSelectedVariable();

        verify(promiseValue).catchError(operationPromiseErrorCaptor.capture());
        operationPromiseErrorCaptor.getValue().apply(promiseError);
        notificationManager.notify(any(), eq(ERROR_MESSAGE), eq(FAIL), eq(FLOAT_MODE));
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
        notificationManager.notify(eq(address), eq(PROGRESS), eq(FLOAT_MODE));
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
    public void testOnPreIn() {
        presenter.onPreStepInto();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(null));
    }

    @Test
    public void testOnPreOut() {
        presenter.onPreStepOut();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(null));
    }

    @Test
    public void testOnPreOver() {
        presenter.onPreStepOver();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(null));
    }

    @Test
    public void testOnPreResume() {
        presenter.onPreResume();

        verify(view).setVariables(any());
        verify(view).setVMName(eq(""));
        verify(view).setExecutionPoint(eq(null));
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

        LocationDto executionPoint = mock(LocationDto.class);
        doReturn(executionPoint).when(dtoFactory).createDto(LocationDto.class);

        doReturn(promiseString).when(debugger).dumpStackFrame();
        doReturn(promiseString).when(promiseString).then((Operation<String>)any());

        presenter.onBreakpointStopped(filePath, className, lineNumber);

        verify(presenter).showAndUpdateView();
        verify(view).setExecutionPoint(any(Location.class));
    }

    @Test
    public void testOnValueChanged() {
        doReturn(promiseString).when(debugger).dumpStackFrame();
        doReturn(promiseString).when(promiseString).then((Operation<String>)any());

        ArrayList<String> path = new ArrayList<>();
        String newValue = "newValue";
        presenter.onValueChanged(path, newValue);
    }
}

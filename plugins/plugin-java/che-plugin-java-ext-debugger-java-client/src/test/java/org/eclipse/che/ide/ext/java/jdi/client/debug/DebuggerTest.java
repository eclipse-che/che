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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.event.project.ProjectReadyHandler;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.project.tree.generic.FileNode;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.BaseTest;
import org.eclipse.che.ide.ext.java.jdi.client.debug.changevalue.ChangeValuePresenter;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolver;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.Location;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.VariablePath;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link DebuggerPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public class DebuggerTest extends BaseTest {
    private static final String MIME_TYPE   = "application/java";
    private static final String DEBUG_INFO  = "debug_info";
    private static final String DEBUGGER_ID = "debugger_id";

    @Captor
    private ArgumentCaptor<ProjectReadyHandler> projectActionHandlerArgumentCaptor;
    @Mock
    private DebuggerView                        view;
    @Mock
    private EvaluateExpressionPresenter         evaluateExpressionPresenter;
    @Mock
    private ChangeValuePresenter                changeValuePresenter;
    @Mock
    private BreakpointManager                   gutterManager;
    @Mock
    private FileNode                            file;
    @Mock
    private ItemReference                       fileReference;
    @Mock
    private FqnResolverFactory                  resolverFactory;
    @Mock
    private AsyncCallback<Breakpoint> asyncCallbackBreakpoint;
    @Mock
    private ProjectConfigDto          project;
    @Mock
    private AsyncCallback<Void>       asyncCallbackVoid;
    @Mock
    private AppContext                appContext;
    @Mock
    private CurrentProject            currentProject;
    @Mock
    private EditorAgent               editorAgent;
    @Mock
    private MessageBusProvider        messageBusProvider;
    @Mock
    private WorkspaceDto              workspace;
    @Mock
    private LocalStorageProvider      localStorageProvider;
    @Mock
    private LocalStorage              localStorage;
    @Mock
    private DebuggerInfo              debuggerInfo;
    @Mock
    private DebuggerManager           debuggerManager;
    @Mock
    private Debugger                  debugger;
    @Mock
    private FileTypeRegistry          fileTypeRegistry;

    @Captor
    private ArgumentCaptor<WsAgentStateHandler> extServerStateHandlerCaptor;

    @InjectMocks
    private DebuggerPresenter presenter;

    @Before
    public void setUp() {
        super.setUp();

        when(file.getData()).thenReturn(fileReference);
        when(dtoFactory.createDto(Location.class)).thenReturn(mock(Location.class));
        when(dtoFactory.createDto(BreakPoint.class)).thenReturn(mock(BreakPoint.class));
        when(resolverFactory.getResolver(anyString())).thenReturn(mock(FqnResolver.class));
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getRootProject()).thenReturn(project);
        when(messageBusProvider.getMachineMessageBus()).thenReturn(messageBus);

        when(localStorageProvider.get()).thenReturn(localStorage);
        when(localStorage.getItem(DebuggerPresenter.LOCAL_STORAGE_DEBUGGER_KEY)).thenReturn(DEBUG_INFO);
        when(dtoFactory.createDtoFromJson(DEBUG_INFO, DebuggerInfo.class)).thenReturn(debuggerInfo);

        verify(eventBus).addHandler(eq(WsAgentStateEvent.TYPE), extServerStateHandlerCaptor.capture());
        extServerStateHandlerCaptor.getValue().onWsAgentStarted(WsAgentStateEvent.createWsAgentStartedEvent());

        FileType fileType = mock(FileType.class);
        when(fileType.getMimeTypes()).thenReturn(Collections.singletonList("application/java"));
        when(fileTypeRegistry.getFileTypeByFile(eq(file))).thenReturn(fileType);
    }

    @Test
    public void testDisconnectDebuggerRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).disconnect(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.disconnectDebugger();

        verify(service).disconnect(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(localStorage).setItem(eq(DebuggerPresenter.LOCAL_STORAGE_DEBUGGER_KEY), anyString());
    }

    @Test
    public void testDisconnectDebuggerRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).disconnect(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.disconnectDebugger();

        verify(service).disconnect(anyString(), anyObject());
    }

    @Test
    public void testResumeRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).resume(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                presenter.resume();
                return null;
            }
        }).when(debugger).resume();

        presenter.resume();

        verify(service).resume(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(gutterManager).removeCurrentBreakpoint();
    }

    @Test
    public void testResumeRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).resume(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.resume();

        verify(service).resume(anyString(), anyObject());
    }

    @Test
    public void testStepIntoRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).stepInto(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepInto();

        verify(service).stepInto(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(view).setVariables(anyListOf(DebuggerVariable.class));
        verify(gutterManager).removeCurrentBreakpoint();
    }

    @Test
    public void testStepIntoRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).stepInto(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepInto();

        verify(service).stepInto(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
    }

    @Test
    public void testStepOverRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).stepOver(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepOver();

        verify(service).stepOver(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(gutterManager).removeCurrentBreakpoint();
    }

    @Test
    public void testStepOverRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).stepOver(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepOver();

        verify(service).stepOver(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
    }

    @Test
    public void testStepOutRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).stepOut(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepOut();

        verify(service).stepOut(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(view).setVariables(anyListOf(DebuggerVariable.class));
        verify(gutterManager).removeCurrentBreakpoint();
    }

    @Test
    public void testStepOutRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[1];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).stepOut(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        when(debuggerManager.getDebugger()).thenReturn(debugger);

        presenter.stepOut();

        verify(service).stepOut(anyString(), Matchers.<AsyncRequestCallback<Void>>anyObject());
    }

    @Test
    public void testAddBreakpointRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).addBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        presenter.addBreakpoint(file, anyInt(), asyncCallbackBreakpoint);

        verify(service).addBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(asyncCallbackBreakpoint).onSuccess((Breakpoint)anyObject());
    }

    @Test
    public void testAddBreakpointRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).addBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        presenter.addBreakpoint(file, anyInt(), asyncCallbackBreakpoint);

        verify(service).addBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(asyncCallbackBreakpoint).onFailure((Throwable)anyObject());
    }

    @Test
    public void testRemoveBreakpointRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service).deleteBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        presenter.deleteBreakpoint(file, anyInt(), asyncCallbackVoid);

        verify(service).deleteBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(asyncCallbackVoid).onSuccess((Void)anyObject());
    }

    @Test
    public void testRemoveBreakpointRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                //noinspection unchecked
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[2];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).deleteBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());

        presenter.deleteBreakpoint(file, anyInt(), asyncCallbackVoid);

        verify(service).deleteBreakpoint(anyString(), (BreakPoint)anyObject(), Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(asyncCallbackVoid).onFailure((Throwable)anyObject());
    }

}

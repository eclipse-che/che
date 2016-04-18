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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import com.google.web.bindery.event.shared.Event;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.ResetRequest.ResetType.HARD;
import static org.eclipse.che.api.git.shared.ResetRequest.ResetType.MIXED;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ResetToCommitPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class ResetToCommitPresenterTest extends BaseTest {
    public static final boolean IS_TEXT_FORMATTED = true;
    public static final boolean IS_MIXED          = true;
    public static final String  FILE_PATH         = "/src/testClass.java";

    @Mock
    private ResetToCommitView      view;
    @Mock
    private FileReferenceNode      file;
    @Mock
    private EditorInput            editorInput;
    @Mock
    private EditorPartPresenter    partPresenter;
    @Mock
    private Revision               selectedRevision;
    @InjectMocks
    private ResetToCommitPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();
        when(appContext.getWorkspaceId()).thenReturn("id");

        presenter = new ResetToCommitPresenter(view,
                                               service,
                                               constant,
                                               eventBus,
                                               dialogFactory,
                                               appContext,
                                               notificationManager,
                                               dtoUnmarshallerFactory,
                                               gitOutputConsoleFactory,
                                               consolesPanelPresenter);

        when(view.isMixMode()).thenReturn(IS_MIXED);
        when(selectedRevision.getId()).thenReturn(PROJECT_PATH);
        when(partPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(file);
        when(file.getPath()).thenReturn(FILE_PATH);
    }

    @Test
    public void testShowDialogWhenLogRequestIsSuccessful() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                @SuppressWarnings("NonJREEmulationClassesInClientCode")
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, mock(LogResponse.class));
                return callback;

            }
        }).when(service).log(devMachine, anyObject(), null,anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service)
                .log(eq(devMachine), eq(rootProjectConfig), null, eq(!IS_TEXT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
        verify(view).setRevisions((ArrayList<Revision>)anyObject());
        verify(view).setMixMode(eq(IS_MIXED));
        verify(view).setEnableResetButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWhenLogRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<String> callback = (AsyncRequestCallback<String>)arguments[2];
                @SuppressWarnings("NonJREEmulationClassesInClientCode")
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;

            }
        }).when(service).log(devMachine, anyObject(), null, anyBoolean(), (AsyncRequestCallback<LogResponse>)anyObject());

        presenter.showDialog();

        verify(appContext).getCurrentProject();
        verify(service)
                .log(eq(devMachine), eq(rootProjectConfig), null, eq(!IS_TEXT_FORMATTED), (AsyncRequestCallback<LogResponse>)anyObject());
        verify(constant).logFailed();
        verify(console).printError(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnResetClickedWhenFileIsNotExistInCommitToReset()
            throws Exception {
        // Only in the cases of <code>ResetRequest.ResetType.HARD</code>  or <code>ResetRequest.ResetType.MERGE</code>
        // must change the workdir
        when(view.isMixMode()).thenReturn(false);
        when(view.isHardMode()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[4];
                @SuppressWarnings("NonJREEmulationClassesInClientCode")
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service)
          .reset(devMachine, anyObject(), anyString(), (ResetRequest.ResetType)anyObject(), (List<String>)anyObject(),
                 (AsyncRequestCallback<Void>)anyObject());

        presenter.onRevisionSelected(selectedRevision);
        presenter.onResetClicked();

        verify(view).close();
        verify(selectedRevision).getId();
        verify(appContext).getCurrentProject();
        verify(service).reset(eq(devMachine), anyObject(), eq(PROJECT_PATH), eq(HARD), (List<String>)anyObject(),
                              (AsyncRequestCallback<Void>)anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<OpenProjectEvent>>anyObject());
        verify(console).print(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnResetClickedWhenFileIsChangedInCommitToReset()
            throws Exception {
        // Only in the cases of <code>ResetRequest.ResetType.HARD</code>  or <code>ResetRequest.ResetType.MERGE</code>
        // must change the workdir
        when(view.isMixMode()).thenReturn(false);
        when(view.isHardMode()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[4];
                @SuppressWarnings("NonJREEmulationClassesInClientCode")
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(service)
          .reset(devMachine, anyObject(), anyString(), (ResetRequest.ResetType)anyObject(),
                 (List<String>)anyObject(), (AsyncRequestCallback<Void>)anyObject());

        presenter.onRevisionSelected(selectedRevision);
        presenter.onResetClicked();

        verify(view).close();
        verify(selectedRevision).getId();
        verify(appContext).getCurrentProject();
        verify(service).reset(eq(devMachine), anyObject(), eq(PROJECT_PATH), eq(HARD), (List<String>)anyObject(),
                              (AsyncRequestCallback<Void>)anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<OpenProjectEvent>>anyObject());
        verify(console).print(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnResetClickedWhenResetRequestIsFailed() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[4];
                @SuppressWarnings("NonJREEmulationClassesInClientCode")
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).reset(devMachine, anyObject(), anyString(), (ResetRequest.ResetType)anyObject(), (List<String>)anyObject(),
                               (AsyncRequestCallback<Void>)anyObject());

        presenter.onRevisionSelected(selectedRevision);
        presenter.onResetClicked();

        verify(view).close();
        verify(selectedRevision).getId();
        verify(appContext).getCurrentProject();
        verify(service).reset(eq(devMachine), anyObject(), eq(PROJECT_PATH), eq(MIXED), (java.util.List<String>)anyObject(),
                              (AsyncRequestCallback<Void>)anyObject());
        verify(console).printError(anyString());
        verify(notificationManager).notify(anyString(), rootProjectConfig);
        verify(eventBus, never()).fireEvent((Event<?>)anyObject());
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnRevisionSelected() throws Exception {
        presenter.onRevisionSelected(selectedRevision);

        verify(view).setEnableResetButton(eq(ENABLE_BUTTON));
    }
}

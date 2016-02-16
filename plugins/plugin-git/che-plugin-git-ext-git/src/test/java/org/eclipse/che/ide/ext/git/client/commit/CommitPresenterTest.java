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
package org.eclipse.che.ide.ext.git.client.commit;

import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.eclipse.che.ide.ext.git.client.commit.CommitPresenter.COMMIT_COMMAND_NAME;

/**
 * Testing {@link CommitPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class CommitPresenterTest extends BaseTest {
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<Revision>> asyncRequestCallbackRevisionCaptor;

    public static final boolean ALL_FILE_INCLUDES = true;
    public static final boolean IS_OVERWRITTEN    = true;
    public static final String  COMMIT_TEXT       = "commit text";
    @Mock
    private CommitView               view;
    @Mock
    private Revision                 revision;
    @Mock
    private DateTimeFormatter        dateTimeFormatter;
    @Mock
    private ProjectExplorerPresenter projectExplorer;

    private CommitPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        when(appContext.getWorkspaceId()).thenReturn("id");

        presenter = new CommitPresenter(view,
                                        service,
                                        constant,
                                        notificationManager,
                                        dialogFactory,
                                        dtoUnmarshallerFactory,
                                        appContext,
                                        dateTimeFormatter,
                                        projectExplorer,
                                        gitOutputConsoleFactory,
                                        consolesPanelPresenter);
    }

    @Test
    public void testShowDialog() throws Exception {
        when(view.getMessage()).thenReturn(EMPTY_TEXT);
        presenter.showDialog();

        verify(view).setAmend(eq(!IS_OVERWRITTEN));
        verify(view).setAllFilesInclude(eq(!ALL_FILE_INCLUDES));
        verify(view).focusInMessageField();
        verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
        verify(view).getMessage();
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWithExistingMessage() throws Exception {
        when(view.getMessage()).thenReturn("foo");
        presenter.showDialog();

        verify(view).setAmend(eq(!IS_OVERWRITTEN));
        verify(view).setAllFilesInclude(eq(!ALL_FILE_INCLUDES));
        verify(view).focusInMessageField();
        verify(view).setEnableCommitButton(eq(ENABLE_BUTTON));
        verify(view).getMessage();
        verify(view).showDialog();
    }

    @Test
    public void testOnCommitClickedWhenCommitWSRequestIsSuccessful() throws Exception {
        when(view.getMessage()).thenReturn(COMMIT_TEXT);
        when(view.isAllFilesInclued()).thenReturn(ALL_FILE_INCLUDES);
        when(view.isAmend()).thenReturn(IS_OVERWRITTEN);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Revision> callback = (AsyncRequestCallback<Revision>)arguments[4];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, revision);
                return callback;
            }
        }).when(service).commit(anyString(), anyObject(), anyString(), anyBoolean(), anyBoolean(),
                                (AsyncRequestCallback<Revision>)anyObject());

        presenter.showDialog();
        presenter.onCommitClicked();

        verify(view, times(2)).getMessage();
        verify(view).isAllFilesInclued();
        verify(view).isAmend();
        verify(view).close();
        verify(view).setMessage(eq(EMPTY_TEXT));

        verify(service).commit(anyString(), eq(rootProjectConfig), eq(COMMIT_TEXT), eq(ALL_FILE_INCLUDES), eq(IS_OVERWRITTEN),
                               (AsyncRequestCallback<Revision>)anyObject());
        verify(gitOutputConsoleFactory).create(COMMIT_COMMAND_NAME);
        verify(console).printInfo(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnCommitClickedWhenCommitRequestIsFailed() throws Exception {
        when(view.getMessage()).thenReturn(COMMIT_TEXT);
        when(view.isAllFilesInclued()).thenReturn(ALL_FILE_INCLUDES);
        when(view.isAmend()).thenReturn(IS_OVERWRITTEN);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Revision> callback = (AsyncRequestCallback<Revision>)arguments[4];
                Method onFailure = GwtReflectionUtils.getMethod(callback.getClass(), "onFailure");
                onFailure.invoke(callback, mock(Throwable.class));
                return callback;
            }
        }).when(service).commit(anyString(), anyObject(), anyString(), anyBoolean(), anyBoolean(),
                                (AsyncRequestCallback<Revision>)anyObject());

        presenter.showDialog();
        presenter.onCommitClicked();

        verify(view, times(2)).getMessage();
        verify(view).isAllFilesInclued();
        verify(view).isAmend();
        verify(view).close();
        verify(view, times(0)).setMessage(anyString());

        verify(service).commit(anyString(), eq(rootProjectConfig), eq(COMMIT_TEXT), eq(ALL_FILE_INCLUDES), eq(IS_OVERWRITTEN),
                               (AsyncRequestCallback<Revision>)anyObject());
        verify(constant).commitFailed();
        verify(gitOutputConsoleFactory).create(COMMIT_COMMAND_NAME);
        verify(console).printError(anyString());
        verify(consolesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), rootProjectConfig);
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnValueChangedWhenCommitMessageEmpty() throws Exception {
        when(view.getMessage()).thenReturn(EMPTY_TEXT);

        presenter.onValueChanged();

        verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void testOnValueChanged() throws Exception {
        when(view.getMessage()).thenReturn(COMMIT_TEXT);

        presenter.onValueChanged();

        verify(view).setEnableCommitButton(eq(!DISABLE_BUTTON));
    }
}

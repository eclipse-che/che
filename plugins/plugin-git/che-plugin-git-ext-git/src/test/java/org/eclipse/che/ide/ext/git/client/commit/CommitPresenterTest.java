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
package org.eclipse.che.ide.ext.git.client.commit;

import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Mock;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link CommitPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class CommitPresenterTest extends BaseTest {

    private static final boolean ALL_FILE_INCLUDES = true;
    private static final boolean IS_OVERWRITTEN    = true;
    private static final String  COMMIT_TEXT       = "commit text";

    @Mock
    private CommitView        view;
    @Mock
    private DateTimeFormatter dateTimeFormatter;

    private CommitPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = spy(new CommitPresenter(view,
                                            service,
                                            constant,
                                            notificationManager,
                                            dialogFactory,
                                            appContext,
                                            dateTimeFormatter,
                                            gitOutputConsoleFactory,
                                            processesPanelPresenter));

        when(view.getMessage()).thenReturn(EMPTY_TEXT);

        when(appContext.getResources()).thenReturn(new Resource[]{});
        when(appContext.getDevMachine()).thenReturn(mock(DevMachine.class));
        when(appContext.getRootProject()).thenReturn(mock(Project.class));

        when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
        when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
        when(revisionPromise.then(any(Operation.class))).thenReturn(revisionPromise);
        when(revisionPromise.catchError(any(Operation.class))).thenReturn(revisionPromise);
        when(service.add(any(DevMachine.class), any(Path.class), anyBoolean(), any(Path[].class))).thenReturn(voidPromise);
        when(service.commit(any(DevMachine.class), any(Path.class), anyString(), anyBoolean(), any(Path[].class), anyBoolean()))
                .thenReturn(revisionPromise);
    }

    @Test
    public void testShowDialog() throws Exception {
        presenter.showDialog(project);

        verify(view).setAmend(eq(!IS_OVERWRITTEN));
        verify(view).setAddAllExceptNew(eq(!ALL_FILE_INCLUDES));
        verify(view).focusInMessageField();
        verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
        verify(view).getMessage();
        verify(view).showDialog();
    }

    @Test
    public void testShowDialogWithExistingMessage() throws Exception {
        when(view.getMessage()).thenReturn("foo");
        presenter.showDialog(project);

        verify(view).setAmend(eq(!IS_OVERWRITTEN));
        verify(view).setAddAllExceptNew(eq(!ALL_FILE_INCLUDES));
        verify(view).focusInMessageField();
        verify(view).setEnableCommitButton(eq(ENABLE_BUTTON));
        verify(view).getMessage();
        verify(view).showDialog();
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
    public void shouldPrintSuccessMessageOnCommitSuccess() throws Exception {
        Revision revision = mock(Revision.class);
        GitUser gitUser = mock(GitUser.class);
        when(gitUser.getName()).thenReturn("commiterName");
        when(revision.getId()).thenReturn("commitId");
        when(revision.getCommitter()).thenReturn(gitUser);
        when(constant.commitMessage(eq("commitId"), anyString())).thenReturn("commitMessage");
        when(constant.commitUser(anyString())).thenReturn("commitUser");

        presenter.showDialog(project);
        presenter.doCommit(COMMIT_TEXT, true, true, true);
        verify(revisionPromise).then(revisionCaptor.capture());
        revisionCaptor.getValue().apply(revision);

        verify(console).print("commitMessage commitUser");
        verify(notificationManager).notify("commitMessage commitUser");
    }

    @Test
    public void shouldPrintFailMessageOnCommitFailed() throws Exception {
        when(constant.commitFailed()).thenReturn("commitFailed");

        presenter.showDialog(project);
        presenter.doCommit(COMMIT_TEXT, true, true, true);
        verify(revisionPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);

        verify(console).printError("error");
        verify(notificationManager).notify(eq("commitFailed"), eq("error"), eq(FAIL), eq(FLOAT_MODE));
    }

    @Test
    public void shouldAddAndCommitIfAddSelectedIsSetToTrue() throws Exception {
        when(view.isAddSelectedFiles()).thenReturn(true);
        doNothing().when(presenter).doCommit(anyString(), anyBoolean(), anyBoolean(), anyBoolean());

        presenter.showDialog(project);
        presenter.onCommitClicked();
        verify(voidPromise).then(voidPromiseCaptor.capture());
        voidPromiseCaptor.getValue().apply(null);

        verify(service).add(any(DevMachine.class), any(Path.class), anyBoolean(), any(Path[].class));
        verify(presenter).doCommit(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldNotAddBeforeCommitIfAddSelectedIsSetToFalse() throws Exception {
        when(view.isAddSelectedFiles()).thenReturn(false);
        doNothing().when(presenter).doCommit(anyString(), anyBoolean(), anyBoolean(), anyBoolean());

        presenter.showDialog(project);
        presenter.onCommitClicked();

        verify(service, never()).add(any(DevMachine.class), any(Path.class), anyBoolean(), any(Path[].class));
        verify(presenter).doCommit(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testOnValueChanged() throws Exception {
        when(view.getMessage()).thenReturn(COMMIT_TEXT);

        presenter.onValueChanged();

        verify(view).setEnableCommitButton(eq(!DISABLE_BUTTON));
    }
}

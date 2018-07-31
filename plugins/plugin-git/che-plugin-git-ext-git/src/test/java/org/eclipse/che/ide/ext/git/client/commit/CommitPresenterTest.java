/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.commit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectableChangesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link CommitPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
public class CommitPresenterTest extends BaseTest {

  private static final String COMMIT_TEXT = "commit text";

  @Mock private CommitView view;
  @Mock private DateTimeFormatter dateTimeFormatter;
  @Mock private SelectableChangesPanelPresenter selectableChangesPanelPresenter;
  @Mock private OAuthServiceClient oAuthServiceClient;

  private CommitPresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        spy(
            new CommitPresenter(
                view,
                service,
                selectableChangesPanelPresenter,
                constant,
                notificationManager,
                dialogFactory,
                appContext,
                dateTimeFormatter,
                gitOutputConsoleFactory,
                processesPanelPresenter,
                oAuthServiceClient));

    when(view.getCommitMessage()).thenReturn(EMPTY_TEXT);

    Resource resource = mock(Resource.class);
    when(appContext.getResources()).thenReturn(new Resource[] {});
    when(appContext.getResource()).thenReturn(resource);
    when(resource.getLocation()).thenReturn(Path.valueOf("test/location"));
    when(appContext.getRootProject()).thenReturn(mock(Project.class));

    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
    when(revisionPromise.then(any(Operation.class))).thenReturn(revisionPromise);
    when(revisionPromise.catchError(any(Operation.class))).thenReturn(revisionPromise);
    when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
    when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);
    when(pushPromise.then(any(Operation.class))).thenReturn(pushPromise);
    when(logPromise.then(any(Operation.class))).thenReturn(logPromise);
    when(logPromise.catchError(any(Operation.class))).thenReturn(logPromise);
    when(statusPromise.then(any(Operation.class))).thenReturn(statusPromise);
    when(service.add(any(Path.class), anyBoolean(), any(Path[].class))).thenReturn(voidPromise);
    when(service.commit(any(Path.class), anyString(), anyBoolean(), any(Path[].class)))
        .thenReturn(revisionPromise);
    when(service.diff(
            any(Path.class),
            eq(null),
            any(DiffType.class),
            anyBoolean(),
            anyInt(),
            anyString(),
            anyBoolean()))
        .thenReturn(stringPromise);
    when(service.branchList(any(Path.class), any(BranchListMode.class)))
        .thenReturn(branchListPromise);
    when(service.push(any(Path.class), anyList(), anyString(), anyBoolean(), null))
        .thenReturn(pushPromise);
    when(service.log(any(Path.class), eq(null), anyInt(), anyInt(), anyBoolean()))
        .thenReturn(logPromise);
    when(service.getStatus(any(Path.class), eq(emptyList()))).thenReturn(statusPromise);
  }

  @Test
  public void shouldShowMessageWhenNothingToCommit() throws Exception {
    ConfirmDialog dialog = mock(ConfirmDialog.class);
    when(dialogFactory.createConfirmDialog(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ConfirmCallback.class),
            eq(null)))
        .thenReturn(dialog);

    presenter.showDialog(project);
    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply("");
    verify(logPromise).then(logCaptor.capture());
    logCaptor.getValue().apply(null);

    verify(dialog).show();
  }

  @Test
  public void shouldShowDialog() throws Exception {
    final String diff = "M\tfile";
    final AlteredFiles alteredFiles = new AlteredFiles(project, diff);
    ConfirmDialog dialog = mock(ConfirmDialog.class);
    when(dialogFactory.createConfirmDialog(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(ConfirmCallback.class),
            eq(null)))
        .thenReturn(dialog);

    presenter.showDialog(project);
    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply("M\tfile");
    verify(logPromise).then(logCaptor.capture());
    logCaptor.getValue().apply(null);

    verify(view).setEnableAmendCheckBox(true);
    verify(view).setEnablePushAfterCommitCheckBox(true);
    verify(selectableChangesPanelPresenter).show(eq(alteredFiles));
    verify(view).focusInMessageField();
    verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
    verify(view).getCommitMessage();
    verify(view).showDialog();
  }

  @Test
  public void shouldShowUntrackedFilesOnInitialCommit() throws Exception {
    final String diff = "A\tfile";
    final AlteredFiles alteredFiles = new AlteredFiles(project, diff);
    PromiseError error = mock(PromiseError.class);
    ServerException exception = mock(ServerException.class);
    when(exception.getErrorCode()).thenReturn(ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED);
    when(error.getCause()).thenReturn(exception);
    Status status = mock(Status.class);
    when(status.getUntracked()).thenReturn(singletonList("file"));

    presenter.showDialog(project);
    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply(null);
    verify(logPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(error);
    verify(statusPromise).then(statusPromiseCaptor.capture());
    statusPromiseCaptor.getValue().apply(status);

    verify(view).setEnableAmendCheckBox(false);
    verify(view).setEnablePushAfterCommitCheckBox(false);
    verify(selectableChangesPanelPresenter).show(eq(alteredFiles));
    verify(view).focusInMessageField();
    verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
    verify(view).getCommitMessage();
    verify(view).showDialog();
  }

  @Test
  public void shouldEnableCommitButton() throws Exception {
    when(view.getCommitMessage()).thenReturn("foo");

    presenter.showDialog(project);
    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply("M\tfile");
    verify(logPromise).then(logCaptor.capture());
    logCaptor.getValue().apply(null);

    verify(view).setEnableCommitButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void shouldCloseWhenCancelButtonClicked() throws Exception {
    presenter.onCancelClicked();

    verify(view).close();
  }

  @Test
  public void shouldDisableCommitButtonOnEmptyMessage() throws Exception {
    when(view.getCommitMessage()).thenReturn(EMPTY_TEXT);

    presenter.onValueChanged();

    verify(view).setEnableCommitButton(eq(DISABLE_BUTTON));
  }

  @Test
  public void shouldEnableCommitButtonOnAmendAndNoFilesChecked() throws Exception {
    when(view.getCommitMessage()).thenReturn(COMMIT_TEXT);
    when(view.isAmend()).thenReturn(true);

    presenter.onValueChanged();

    verify(view).setEnableCommitButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void shouldPrintSuccessMessageOnAddToIndexAndCommitSuccess() throws Exception {
    Revision revision = mock(Revision.class);
    GitUser gitUser = mock(GitUser.class);
    when(gitUser.getName()).thenReturn("commiterName");
    when(revision.getId()).thenReturn("commitId");
    when(revision.getCommitter()).thenReturn(gitUser);
    when(constant.commitMessage(eq("commitId"), anyString())).thenReturn("commitMessage");
    when(constant.commitUser(anyString())).thenReturn("commitUser");

    presenter.showDialog(project);
    presenter.onCommitClicked();
    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);
    verify(revisionPromise).then(revisionCaptor.capture());
    revisionCaptor.getValue().apply(revision);

    verify(console).print("commitMessage commitUser");
    verify(notificationManager).notify("commitMessage commitUser");
    verify(view).close();
  }

  @Test
  public void shouldPrintFailMessageOnOnAddToIndexSuccessButCommitFailed() throws Exception {
    when(constant.commitFailed()).thenReturn("commitFailed");

    presenter.showDialog(project);
    presenter.onCommitClicked();
    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);
    verify(revisionPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(console).printError("error");
    verify(notificationManager).notify(eq("commitFailed"), eq("error"), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void shouldShowErrorNotificationOnAddToIndexFailed() throws Exception {
    when(constant.addFailed()).thenReturn("addFailed");

    presenter.showDialog(project);
    presenter.onCommitClicked();
    verify(voidPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(notificationManager).notify(eq("addFailed"), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void shouldShowPushSuccessNotificationIfPushAfterCommitChecked() throws Exception {
    when(constant.pushSuccess(anyString())).thenReturn("pushSuccess");
    when(view.isPushAfterCommit()).thenReturn(true);
    when(view.getRemoteBranch()).thenReturn("origin/master");

    presenter.showDialog(project);
    presenter.onCommitClicked();
    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);
    verify(revisionPromise).then(revisionCaptor.capture());
    revisionCaptor.getValue().apply(mock(Revision.class));
    verify(pushPromise).then(pushPromiseCaptor.capture());
    pushPromiseCaptor.getValue().apply(null);

    verify(notificationManager).notify(eq("pushSuccess"), eq(SUCCESS), eq(FLOAT_MODE));
  }

  @Test
  public void shouldShowPushFailedNotification() throws Exception {
    when(constant.pushFail()).thenReturn("pushFail");
    when(view.isPushAfterCommit()).thenReturn(true);
    when(view.getRemoteBranch()).thenReturn("origin/master");

    presenter.showDialog(project);
    presenter.onCommitClicked();
    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);
    verify(revisionPromise).then(revisionCaptor.capture());
    revisionCaptor.getValue().apply(mock(Revision.class));
    verify(pushPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(notificationManager).notify(eq("pushFail"), eq(FAIL), eq(FLOAT_MODE));
  }
}

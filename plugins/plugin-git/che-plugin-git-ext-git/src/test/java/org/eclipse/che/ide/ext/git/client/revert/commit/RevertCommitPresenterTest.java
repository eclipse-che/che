/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.revert.commit;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.revert.RevertCommitPresenter;
import org.eclipse.che.ide.ext.git.client.revert.RevertCommitView;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class RevertCommitPresenterTest extends BaseTest {

  @Mock private RevertCommitView view;
  @InjectMocks private RevertCommitPresenter presenter;

  @Mock private Promise<RevertResult> revertPromise;
  @Captor private ArgumentCaptor<Operation<RevertResult>> revertCaptor;

  @Override
  public void disarm() {
    super.disarm();

    when(service.log(any(Path.class), any(Path[].class), anyInt(), anyInt(), anyBoolean()))
        .thenReturn(logPromise);
    when(logPromise.then(any(Operation.class))).thenReturn(logPromise);
    when(logPromise.catchError(any(Operation.class))).thenReturn(logPromise);

    when(service.revert(any(Path.class), anyString())).thenReturn(revertPromise);
    when(revertPromise.then(any(Operation.class))).thenReturn(revertPromise);
    when(revertPromise.catchError(any(Operation.class))).thenReturn(revertPromise);
  }

  @Test
  public void shouldGetCommitsAndShowDialog() throws Exception {
    LogResponse response = mock(LogResponse.class);
    List<Revision> revisions = singletonList(mock(Revision.class));
    when(response.getCommits()).thenReturn(revisions);

    presenter.show(project);
    verify(logPromise).then(logCaptor.capture());
    logCaptor.getValue().apply(response);

    verify(view).setRevisions(revisions);
    verify(view).showDialog();
  }

  @Test
  public void shouldShowNotificationOnGetCommitsError() throws Exception {
    when(constant.logFailed()).thenReturn("error");

    presenter.show(project);
    verify(logPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(mock(PromiseError.class));

    verify(notificationManager).notify(eq("error"), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void shouldNotifyOnSuccessfulRevert() throws Exception {
    RevertResult revertResult = mock(RevertResult.class);
    when(revertResult.getNewHead()).thenReturn("1234");
    when(revertResult.getRevertedCommits()).thenReturn(Collections.emptyList());
    when(revertResult.getConflicts()).thenReturn(Collections.emptyMap());

    Revision selectedRevision = mock(Revision.class);
    when(selectedRevision.getId()).thenReturn("1234");

    presenter.show(project);
    presenter.onRevisionSelected(selectedRevision);
    presenter.onRevertClicked();
    verify(revertPromise).then(revertCaptor.capture());
    revertCaptor.getValue().apply(revertResult);

    verify(notificationManager).notify(constant.revertCommitSuccessfully());
  }

  @Test
  public void shouldNotifyOnFailedRevert() throws Exception {
    RevertResult revertResult = mock(RevertResult.class);
    when(revertResult.getNewHead()).thenReturn("1234");
    when(revertResult.getRevertedCommits()).thenReturn(Collections.emptyList());
    when(revertResult.getConflicts()).thenReturn(Collections.emptyMap());

    Revision selectedRevision = mock(Revision.class);
    when(selectedRevision.getId()).thenReturn("1234");

    presenter.show(project);
    presenter.onRevisionSelected(selectedRevision);
    presenter.onRevertClicked();
    verify(revertPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(mock(PromiseError.class));

    verify(notificationManager).notify(eq(constant.revertCommitFailed()), eq(FAIL), eq(FLOAT_MODE));
  }
}

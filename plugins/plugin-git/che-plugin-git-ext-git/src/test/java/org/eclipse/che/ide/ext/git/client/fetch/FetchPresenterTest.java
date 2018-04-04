/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.fetch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link FetchPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class FetchPresenterTest extends BaseTest {
  public static final boolean NO_REMOVE_DELETE_REFS = false;
  public static final boolean FETCH_ALL_BRANCHES = true;
  @Mock private FetchView view;
  @Mock private Branch branch;
  @Mock private BranchSearcher branchSearcher;
  @Mock private OAuthServiceClient oAuthServiceClient;

  private FetchPresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new FetchPresenter(
            dtoFactory,
            view,
            service,
            constant,
            notificationManager,
            branchSearcher,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            oAuthServiceClient);

    when(service.remoteList(any(Path.class), anyString(), anyBoolean()))
        .thenReturn(remoteListPromise);
    when(remoteListPromise.then(any(Operation.class))).thenReturn(remoteListPromise);
    when(remoteListPromise.catchError(any(Operation.class))).thenReturn(remoteListPromise);

    when(service.branchList(any(Path.class), anyObject())).thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);

    when(view.getRepositoryName()).thenReturn(REPOSITORY_NAME);
    when(view.getRepositoryUrl()).thenReturn(REMOTE_URI);
    when(view.getLocalBranch()).thenReturn(LOCAL_BRANCH);
    when(view.getRemoteBranch()).thenReturn(REMOTE_BRANCH);
    when(branch.getName()).thenReturn(REMOTE_BRANCH);
  }

  @Test
  public void testShowDialogWhenBranchListRequestIsSuccessful() throws Exception {
    final List<Remote> remotes = new ArrayList<>();
    remotes.add(mock(Remote.class));
    final List<Branch> branches = new ArrayList<>();
    branches.add(branch);

    presenter.showDialog(project);

    verify(remoteListPromise).then(remoteListCaptor.capture());
    remoteListCaptor.getValue().apply(remotes);

    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(branchListPromise, times(2)).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(view).setEnableFetchButton(eq(ENABLE_BUTTON));
    verify(view).setRepositories(anyObject());
    verify(view).setRemoveDeleteRefs(eq(NO_REMOVE_DELETE_REFS));
    verify(view).setFetchAllBranches(eq(FETCH_ALL_BRANCHES));
    verify(view).showDialog();
    verify(view).setRemoteBranches(anyObject());
    verify(view).setLocalBranches(anyObject());
  }

  @Test
  public void testOnValueChanged() throws Exception {
    when(view.isFetchAllBranches()).thenReturn(FETCH_ALL_BRANCHES);
    presenter.onValueChanged();

    verify(view).setEnableLocalBranchField(eq(DISABLE_FIELD));
    verify(view).setEnableRemoteBranchField(eq(DISABLE_FIELD));

    when(view.isFetchAllBranches()).thenReturn(!FETCH_ALL_BRANCHES);
    presenter.onValueChanged();

    verify(view).setEnableLocalBranchField(eq(ENABLE_FIELD));
    verify(view).setEnableRemoteBranchField(eq(ENABLE_FIELD));
  }

  @Test
  public void testOnCancelClicked() throws Exception {
    presenter.onCancelClicked();

    verify(view).close();
  }

  @Test
  public void shouldRefreshRemoteBranchesWhenRepositoryIsChanged() throws Exception {
    final List<Remote> remotes = new ArrayList<>();
    remotes.add(mock(Remote.class));
    final List<Branch> branches = new ArrayList<>();
    branches.add(branch);
    when(branch.isActive()).thenReturn(ACTIVE_BRANCH);

    presenter.showDialog(project);
    presenter.onRemoteRepositoryChanged();

    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(branchListPromise, times(2)).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(view).setRemoteBranches(anyObject());
    verify(view).setLocalBranches(anyObject());
    verify(view).selectRemoteBranch(anyString());
  }
}

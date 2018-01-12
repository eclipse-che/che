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
package org.eclipse.che.ide.ext.git.client.merge;

import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.MergeResult.MergeStatus.ALREADY_UP_TO_DATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Testing {@link MergePresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class MergePresenterTest extends BaseTest {
  public static final String DISPLAY_NAME = "displayName";

  @Mock private MergeView view;
  @Mock private MergeResult mergeResult;
  @Mock private Reference selectedReference;
  @Mock private VirtualFile file;

  @Mock private Promise<List<Branch>> remoteListBranchPromise;
  @Captor private ArgumentCaptor<Operation<List<Branch>>> remoteListBranchCaptor;
  @Captor protected ArgumentCaptor<Operation<PromiseError>> secondPromiseErrorCaptor;

  private MergePresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new MergePresenter(
            view,
            service,
            constant,
            appContext,
            notificationManager,
            dialogFactory,
            gitOutputConsoleFactory,
            processesPanelPresenter);

    when(mergeResult.getMergeStatus()).thenReturn(ALREADY_UP_TO_DATE);
    when(selectedReference.getDisplayName()).thenReturn(DISPLAY_NAME);

    when(service.branchList(any(Path.class), eq(LIST_LOCAL))).thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);

    when(service.branchList(any(Path.class), eq(LIST_REMOTE))).thenReturn(remoteListBranchPromise);
    when(remoteListBranchPromise.then(any(Operation.class))).thenReturn(remoteListBranchPromise);
    when(remoteListBranchPromise.catchError(any(Operation.class)))
        .thenReturn(remoteListBranchPromise);
  }

  @Test
  public void testShowDialogWhenAllOperationsAreSuccessful() throws Exception {
    final List<Branch> branches = new ArrayList<>();
    branches.add(mock(Branch.class));

    presenter.showDialog(project);

    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(remoteListBranchPromise).then(remoteListBranchCaptor.capture());
    remoteListBranchCaptor.getValue().apply(branches);

    verify(view).setEnableMergeButton(eq(DISABLE_BUTTON));
    verify(view).showDialog();
    verify(view).setRemoteBranches(anyObject());
    verify(view).setLocalBranches(anyObject());
    verify(console, never()).printError(anyString());
  }

  @Test
  public void testOnCancelClicked() throws Exception {
    presenter.onCancelClicked();

    verify(view).close();
  }

  @Test
  public void testDialogWhenListOfBranchesAreEmpty() throws Exception {
    final ArrayList<Reference> emptyReferenceList = new ArrayList<>();
    final List<Branch> emptyBranchList = new ArrayList<>();

    presenter.showDialog(project);

    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(emptyBranchList);

    verify(remoteListBranchPromise).then(remoteListBranchCaptor.capture());
    remoteListBranchCaptor.getValue().apply(emptyBranchList);

    verify(view).showDialog();
    verify(view).setLocalBranches(eq(emptyReferenceList));
    verify(view).setRemoteBranches(eq(emptyReferenceList));
  }
}

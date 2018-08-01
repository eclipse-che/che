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
package org.eclipse.che.ide.ext.git.client.branch;

import static org.eclipse.che.api.git.shared.BranchListMode.LIST_ALL;
import static org.eclipse.che.ide.ext.git.client.patcher.WindowPatcher.RETURNED_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Testing {@link BranchPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class BranchPresenterTest extends BaseTest {

  @Captor private ArgumentCaptor<InputCallback> inputCallbackCaptor;
  @Captor private ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor;

  private static final String BRANCH_NAME = "branchName";
  private static final String REMOTE_BRANCH_NAME = "origin/branchName";
  private static final boolean IS_REMOTE = true;
  private static final boolean IS_ACTIVE = true;
  @Mock private BranchView view;
  @Mock private Branch selectedBranch;
  @Mock private DialogFactory dialogFactory;
  @Mock private DtoFactory dtoFactory;
  @Mock private CheckoutRequest checkoutRequest;

  private BranchPresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new BranchPresenter(
            view,
            dtoFactory,
            service,
            constant,
            notificationManager,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            dialogFactory);

    when(selectedBranch.getDisplayName()).thenReturn(BRANCH_NAME);
    when(selectedBranch.getName()).thenReturn(BRANCH_NAME);
    when(selectedBranch.isRemote()).thenReturn(IS_REMOTE);
    when(selectedBranch.isActive()).thenReturn(IS_ACTIVE);

    when(service.branchList(anyObject(), anyObject())).thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);
    when(view.getFilterValue()).thenReturn("all");
  }

  @Test
  public void testShowBranchesWhenGetBranchesRequestIsSuccessful() throws Exception {
    final List<Branch> branches = Collections.singletonList(selectedBranch);

    when(service.branchList(anyObject(), anyObject())).thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);

    presenter.showBranches(project);

    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    verify(view).showDialogIfClosed();
    verify(view).setBranches(eq(branches));
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString(), any(ProjectConfigDto.class));
    verify(constant, never()).branchesListFailed();
  }

  @Test
  public void shouldShowLocalBranchesWheBranchesFilterIsSetToLocal() throws Exception {
    // given
    final List<Branch> branches = Collections.singletonList(selectedBranch);
    when(service.branchList(anyObject(), eq(BranchListMode.LIST_LOCAL)))
        .thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);
    when(view.getFilterValue()).thenReturn("local");

    // when
    presenter.showBranches(project);
    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    // then
    verify(view).showDialogIfClosed();
    verify(view).setBranches(eq(branches));
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString(), any(ProjectConfigDto.class));
    verify(constant, never()).branchesListFailed();
  }

  @Test
  public void shouldShowRemoteBranchesWheBranchesFilterIsSetToRemote() throws Exception {
    // given
    final List<Branch> branches = Collections.singletonList(selectedBranch);
    when(service.branchList(anyObject(), eq(BranchListMode.LIST_LOCAL)))
        .thenReturn(branchListPromise);
    when(branchListPromise.then(any(Operation.class))).thenReturn(branchListPromise);
    when(branchListPromise.catchError(any(Operation.class))).thenReturn(branchListPromise);
    when(view.getFilterValue()).thenReturn("remote");

    // when
    presenter.showBranches(project);
    verify(branchListPromise).then(branchListCaptor.capture());
    branchListCaptor.getValue().apply(branches);

    // then
    verify(view).showDialogIfClosed();
    verify(view).setBranches(eq(branches));
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString(), any(ProjectConfigDto.class));
    verify(constant, never()).branchesListFailed();
  }

  @Test
  public void testOnCloseClicked() throws Exception {
    presenter.onClose();

    verify(view).closeDialogIfShowing();
  }

  @Test
  public void testOnRenameClickedWhenLocalBranchSelected() throws Exception {
    reset(selectedBranch);

    when(service.branchRename(anyObject(), anyString(), anyString())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);

    when(selectedBranch.getDisplayName()).thenReturn(BRANCH_NAME);
    when(selectedBranch.isRemote()).thenReturn(false);
    InputDialog inputDialog = mock(InputDialog.class);
    when(dialogFactory.createInputDialog(
            anyObject(), anyObject(), anyString(), anyInt(), anyInt(), anyObject(), anyObject()))
        .thenReturn(inputDialog);

    selectBranch();
    presenter.onRenameClicked();

    verify(dialogFactory)
        .createInputDialog(
            eq(null),
            eq(null),
            eq("branchName"),
            eq(0),
            eq("branchName".length()),
            inputCallbackCaptor.capture(),
            eq(null));
    InputCallback inputCallback = inputCallbackCaptor.getValue();
    inputCallback.accepted(RETURNED_MESSAGE);

    verify(selectedBranch, times(2)).getDisplayName();
    verify(dialogFactory, never())
        .createConfirmDialog(anyString(), anyString(), anyObject(), anyObject());
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString());
    verify(constant, never()).branchRenameFailed();
  }

  @Test
  public void testOnRenameClickedWhenRemoteBranchSelectedAndUserConfirmRename() throws Exception {
    reset(selectedBranch);

    when(service.branchRename(anyObject(), anyString(), anyString())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);

    when(selectedBranch.getDisplayName()).thenReturn(REMOTE_BRANCH_NAME);
    when(selectedBranch.isRemote()).thenReturn(true);
    InputDialog inputDialog = mock(InputDialog.class);
    when(dialogFactory.createInputDialog(
            anyString(), anyString(), anyString(), anyInt(), anyInt(), anyObject(), anyObject()))
        .thenReturn(inputDialog);
    ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
    when(dialogFactory.createConfirmDialog(anyString(), anyString(), anyObject(), anyObject()))
        .thenReturn(confirmDialog);

    selectBranch();
    presenter.onRenameClicked();

    verify(dialogFactory)
        .createConfirmDialog(
            anyString(), anyString(), confirmCallbackCaptor.capture(), anyObject());
    ConfirmCallback confirmCallback = confirmCallbackCaptor.getValue();
    confirmCallback.accepted();

    verify(dialogFactory)
        .createInputDialog(
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyInt(),
            inputCallbackCaptor.capture(),
            anyObject());
    InputCallback inputCallback = inputCallbackCaptor.getValue();
    inputCallback.accepted(RETURNED_MESSAGE);

    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);

    verify(selectedBranch, times(2)).getDisplayName();
    verify(service, times(2)).branchList(anyObject(), eq(LIST_ALL));
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString());
    verify(constant, never()).branchRenameFailed();
  }

  /** Select mock branch for testing. */
  private void selectBranch() {
    presenter.showBranches(project);
    presenter.onBranchSelected(selectedBranch);
  }

  @Test
  public void testOnDeleteClickedWhenBranchDeleteRequestIsSuccessful() throws Exception {
    when(service.branchDelete(any(Path.class), anyString(), anyBoolean())).thenReturn(voidPromise);
    when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
    when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);

    selectBranch();
    presenter.onDeleteClicked();

    verify(voidPromise).then(voidPromiseCaptor.capture());
    voidPromiseCaptor.getValue().apply(null);

    verify(selectedBranch).getName();
    verify(service, times(2)).branchList(anyObject(), eq(LIST_ALL));
    verify(constant, never()).branchDeleteFailed();
    verify(console, never()).printError(anyString());
    verify(notificationManager, never()).notify(anyString());
  }

  @Test
  public void testOnCheckoutClickedWhenSelectedNotRemoteBranch() throws Exception {
    when(service.checkout(any(Path.class), any(CheckoutRequest.class))).thenReturn(stringPromise);
    when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
    when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);

    when(selectedBranch.isRemote()).thenReturn(false);
    when(dtoFactory.createDto(CheckoutRequest.class)).thenReturn(checkoutRequest);

    selectBranch();
    presenter.onCheckoutClicked();

    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply(null);

    verify(checkoutRequest).setName(eq(BRANCH_NAME));
    verifyNoMoreInteractions(checkoutRequest);
  }

  @Test
  public void testOnCheckoutClickedWhenSelectedRemoteBranch() throws Exception {
    when(service.checkout(any(Path.class), any(CheckoutRequest.class))).thenReturn(stringPromise);
    when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
    when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);

    when(dtoFactory.createDto(CheckoutRequest.class)).thenReturn(checkoutRequest);

    selectBranch();
    presenter.onCheckoutClicked();

    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply(null);

    verify(checkoutRequest).setTrackBranch(eq(BRANCH_NAME));
    verifyNoMoreInteractions(checkoutRequest);
  }

  @Test
  public void testOnCreateClickedWhenBranchCreateRequestIsSuccessful() throws Exception {
    when(service.branchCreate(any(Path.class), anyString(), anyString())).thenReturn(branchPromise);
    when(branchPromise.then(any(Operation.class))).thenReturn(branchPromise);
    when(branchPromise.catchError(any(Operation.class))).thenReturn(branchPromise);

    InputDialog inputDialog = mock(InputDialog.class);
    when(dialogFactory.createInputDialog(anyString(), anyString(), anyObject(), anyObject()))
        .thenReturn(inputDialog);

    presenter.showBranches(project);
    presenter.onCreateClicked();

    verify(dialogFactory)
        .createInputDialog(anyString(), anyString(), inputCallbackCaptor.capture(), anyObject());
    InputCallback inputCallback = inputCallbackCaptor.getValue();
    inputCallback.accepted(BRANCH_NAME);

    verify(branchPromise).then(branchCaptor.capture());
    branchCaptor.getValue().apply(selectedBranch);

    verify(constant).branchTypeNew();
    verify(service).branchCreate(any(Path.class), anyString(), anyString());
    verify(service, times(2)).branchList(anyObject(), eq(LIST_ALL));
  }

  @Test
  public void checkoutButtonShouldBeEnabled() throws Exception {
    when(selectedBranch.isActive()).thenReturn(false);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableCheckoutButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void checkoutButtonShouldBeDisabled() throws Exception {
    when(selectedBranch.isActive()).thenReturn(true);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableCheckoutButton(eq(DISABLE_BUTTON));
  }

  @Test
  public void renameButtonShouldBeEnabledWhenLocalBranchSelected() throws Exception {
    when(selectedBranch.isRemote()).thenReturn(false);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableRenameButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void renameButtonShouldBeEnabledWhenRemoteBranchSelected() throws Exception {
    when(selectedBranch.isRemote()).thenReturn(true);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableRenameButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void deleteButtonShouldBeEnabled() throws Exception {
    when(selectedBranch.isActive()).thenReturn(false);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableDeleteButton(eq(ENABLE_BUTTON));
  }

  @Test
  public void deleteButtonShouldBeDisabled() throws Exception {
    when(selectedBranch.isActive()).thenReturn(true);

    presenter.onBranchSelected(selectedBranch);

    verify(view).setEnableDeleteButton(eq(DISABLE_BUTTON));
  }

  @Test
  public void checkoutDeleteRenameButtonsShouldBeDisabled() throws Exception {
    presenter.onBranchUnselected();

    verify(view).setEnableCheckoutButton(eq(DISABLE_BUTTON));
    verify(view).setEnableDeleteButton(eq(DISABLE_BUTTON));
    verify(view).setEnableRenameButton(eq(DISABLE_BUTTON));
  }
}

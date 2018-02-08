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
package org.eclipse.che.ide.ext.git.client.compare.branchlist;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_ALL;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changeslist.ChangesListPresenter;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for displaying list of branches for comparing selected with local changes.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BranchListPresenter implements BranchListView.ActionDelegate {
  public static final String BRANCH_LIST_COMMAND_NAME = "Git list of branches";

  private final ComparePresenter comparePresenter;
  private final ChangesListPresenter changesListPresenter;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final BranchListView view;
  private final DialogFactory dialogFactory;
  private final GitServiceClient service;
  private final GitLocalizationConstant locale;
  private final AppContext appContext;
  private final NotificationManager notificationManager;

  private Branch selectedBranch;
  private Project project;
  private Resource selectedItem;

  @Inject
  public BranchListPresenter(
      BranchListView view,
      ComparePresenter comparePresenter,
      ChangesListPresenter changesListPresenter,
      GitServiceClient service,
      GitLocalizationConstant locale,
      AppContext appContext,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter) {
    this.view = view;
    this.comparePresenter = comparePresenter;
    this.changesListPresenter = changesListPresenter;
    this.dialogFactory = dialogFactory;
    this.service = service;
    this.locale = locale;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;

    this.view.setDelegate(this);
  }

  /** Open dialog and shows branches to compare. */
  public void showBranches(Project project, Resource selectedItem) {
    checkState(
        project.getLocation().isPrefixOf(selectedItem.getLocation()),
        "Given selected item is not descendant of given project");

    this.project = project;
    this.selectedItem = selectedItem;

    getBranches();
  }

  @Override
  public void onClose() {
    view.closeDialogIfShowing();
    view.updateSearchFilterLabel("");
    view.clearSearchFilter();
  }

  @Override
  public void onCompareClicked() {

    final String selectedItemPath =
        selectedItem
            .getLocation()
            .removeFirstSegments(project.getLocation().segmentCount())
            .removeTrailingSeparator()
            .toString();

    service
        .diff(
            project.getLocation(),
            selectedItemPath.isEmpty() ? null : singletonList(selectedItemPath),
            NAME_STATUS,
            false,
            0,
            selectedBranch.getName(),
            false)
        .then(
            diff -> {
              if (diff.isEmpty()) {
                dialogFactory
                    .createMessageDialog(
                        locale.compareMessageIdenticalContentTitle(),
                        locale.compareMessageIdenticalContentText(),
                        null)
                    .show();
              } else {
                AlteredFiles alteredFiles = new AlteredFiles(project, diff);
                if (alteredFiles.getFilesQuantity() == 1) {
                  comparePresenter.showCompareWithLatest(
                      alteredFiles, null, selectedBranch.getName());
                } else {
                  changesListPresenter.show(alteredFiles, selectedBranch.getName(), null);
                }
                view.closeDialogIfShowing();
              }
            })
        .catchError(
            error -> {
              notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
            });
  }

  @Override
  public void onBranchUnselected() {
    selectedBranch = null;
    view.setEnableCompareButton(false);
  }

  @Override
  public void onSearchFilterChanged(String filter) {
    view.updateSearchFilterLabel(filter);
  }

  @Override
  public void onBranchSelected(@NotNull Branch branch) {
    selectedBranch = branch;

    view.setEnableCompareButton(true);
  }

  /** Get list of branches from selected project. */
  private void getBranches() {
    service
        .branchList(project.getLocation(), LIST_ALL)
        .then(
            branches -> {
              view.setBranches(branches);
              view.showDialog();
            })
        .catchError(
            error -> {
              final String errorMessage =
                  (error.getMessage() != null) ? error.getMessage() : locale.branchesListFailed();
              GitOutputConsole console = gitOutputConsoleFactory.create(BRANCH_LIST_COMMAND_NAME);
              console.printError(errorMessage);
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(locale.branchesListFailed(), FAIL, NOT_EMERGE_MODE);
            });
  }
}

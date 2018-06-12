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
package org.eclipse.che.ide.ext.git.client.history;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.Constants.DEFAULT_PAGE_SIZE;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changeslist.ChangesListPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for displaying list of revisions for comparing selected with local changes.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class HistoryPresenter implements HistoryView.ActionDelegate {

  private final ComparePresenter comparePresenter;
  private final ChangesListPresenter changesListPresenter;
  private final DialogFactory dialogFactory;
  private final HistoryView view;
  private final GitServiceClient service;
  private final GitLocalizationConstant locale;
  private final AppContext appContext;
  private final NotificationManager notificationManager;

  private Revision selectedRevision;
  private Project project;
  private Path selectedPath;
  private List<Revision> revisions;
  private int skip;

  @Inject
  public HistoryPresenter(
      HistoryView view,
      ComparePresenter comparePresenter,
      ChangesListPresenter changesListPresenter,
      GitServiceClient service,
      GitLocalizationConstant locale,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      AppContext appContext) {
    this.view = view;
    this.comparePresenter = comparePresenter;
    this.changesListPresenter = changesListPresenter;
    this.dialogFactory = dialogFactory;
    this.service = service;
    this.locale = locale;
    this.appContext = appContext;
    this.notificationManager = notificationManager;

    this.view.setDelegate(this);
  }

  /** Open dialog and shows revisions to compare. */
  public void show() {
    this.skip = 0;
    this.revisions = new ArrayList<>();
    this.project = appContext.getRootProject();
    this.selectedPath =
        appContext
            .getResource()
            .getLocation()
            .removeFirstSegments(project.getLocation().segmentCount())
            .removeTrailingSeparator();
    fetchRevisions();
  }

  @Override
  public void onCloseClicked() {
    view.close();
  }

  @Override
  public void onCompareClicked() {
    compare();
  }

  @Override
  public void onRevisionUnselected() {
    selectedRevision = null;
    view.setEnableCompareButton(false);
    view.setDescription(locale.viewCompareRevisionFullDescriptionEmptyMessage());
  }

  @Override
  public void onScrolledToButton() {
    fetchRevisions();
  }

  @Override
  public void onRevisionSelected(@NotNull Revision revision) {
    selectedRevision = revision;

    view.setEnableCompareButton(true);
  }

  @Override
  public void onRevisionDoubleClicked() {
    compare();
  }

  private void fetchRevisions() {
    service
        .log(
            project.getLocation(),
            selectedPath.isEmpty() ? null : new Path[] {selectedPath},
            skip,
            DEFAULT_PAGE_SIZE,
            false)
        .then(
            log -> {
              List<Revision> commits = log.getCommits();
              if (!commits.isEmpty()) {
                skip += commits.size();
                revisions.addAll(commits);
                view.setRevisions(revisions);
                view.showDialog();
              }
            })
        .catchError(
            error -> {
              if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                dialogFactory
                    .createMessageDialog(
                        locale.historyTitle(), locale.initCommitWasNotPerformed(), null)
                    .show();
              } else {
                notificationManager.notify(locale.logFailed(), FAIL, EMERGE_MODE);
              }
            });
  }

  private void compare() {
    final String revisionA =
        revisions.indexOf(selectedRevision) + 1 == revisions.size()
            ? null
            : revisions.get(revisions.indexOf(selectedRevision) + 1).getId();
    final String revisionB = selectedRevision.getId();
    service
        .diff(
            project.getLocation(),
            singletonList(selectedPath.toString()),
            NAME_STATUS,
            true,
            0,
            revisionA,
            revisionB)
        .then(
            diff -> {
              if (diff.isEmpty()) {
                dialogFactory
                    .createMessageDialog(
                        locale.historyTitle(), locale.historyNothingToDisplay(), null)
                    .show();
                return;
              }
              AlteredFiles alteredFiles = new AlteredFiles(project, diff, revisionA, revisionB);
              if (alteredFiles.getFilesQuantity() == 1) {
                comparePresenter.showCompareBetweenRevisions(
                    alteredFiles, null, revisionA, revisionB);
              } else {

                changesListPresenter.show(alteredFiles, revisionA, revisionB);
              }
            })
        .catchError(
            error -> {
              notificationManager.notify(locale.diffFailed(), FAIL, EMERGE_MODE);
            });
  }
}

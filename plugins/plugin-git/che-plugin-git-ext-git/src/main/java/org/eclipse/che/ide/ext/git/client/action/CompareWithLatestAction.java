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
package org.eclipse.che.ide.ext.git.client.action;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changeslist.ChangesListPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Action for comparing with latest repository version
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CompareWithLatestAction extends GitAction {
  private final ComparePresenter comparePresenter;
  private final ChangesListPresenter changesListPresenter;
  private final DialogFactory dialogFactory;
  private final NotificationManager notificationManager;
  private final GitServiceClient service;
  private final GitLocalizationConstant locale;

  private static final String REVISION = "HEAD";

  @Inject
  public CompareWithLatestAction(
      ComparePresenter presenter,
      ChangesListPresenter changesListPresenter,
      AppContext appContext,
      DialogFactory dialogFactory,
      NotificationManager notificationManager,
      GitServiceClient service,
      GitLocalizationConstant constant) {
    super(constant.compareWithLatestTitle(), constant.compareWithLatestTitle(), appContext);
    this.comparePresenter = presenter;
    this.changesListPresenter = changesListPresenter;
    this.dialogFactory = dialogFactory;
    this.notificationManager = notificationManager;
    this.service = service;
    this.locale = constant;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {

    final Project project = appContext.getRootProject();
    final Resource resource = appContext.getResource();

    checkState(project != null, "Null project occurred");
    checkState(
        project.getLocation().isPrefixOf(resource.getLocation()),
        "Given selected item is not descendant of given project");

    final String selectedItemPath =
        resource
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
            REVISION,
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

                  comparePresenter.showCompareWithLatest(alteredFiles, null, REVISION);
                } else {

                  changesListPresenter.show(alteredFiles, REVISION, null);
                }
              }
            })
        .catchError(
            arg -> {
              notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
            });
  }
}

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
package org.eclipse.che.ide.ext.git.client.compare.revisionsList;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for displaying list of revisions for comparing selected with local changes.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RevisionListPresenter implements RevisionListView.ActionDelegate {
    private final ComparePresenter        comparePresenter;
    private final DialogFactory           dialogFactory;
    private final RevisionListView        view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant locale;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;

    private Revision selectedRevision;
    private Project  project;
    private Path     selectedFilePath;

    @Inject
    public RevisionListPresenter(RevisionListView view,
                                 ComparePresenter comparePresenter,
                                 GitServiceClient service,
                                 GitLocalizationConstant locale,
                                 NotificationManager notificationManager,
                                 DialogFactory dialogFactory,
                                 AppContext appContext) {
        this.view = view;
        this.comparePresenter = comparePresenter;
        this.dialogFactory = dialogFactory;
        this.service = service;
        this.locale = locale;
        this.appContext = appContext;
        this.notificationManager = notificationManager;

        this.view.setDelegate(this);
    }

    /** Open dialog and shows revisions to compare. */
    public void showRevisions(Project project, File selectedFile) {
        this.project = project;

        checkState(project.getLocation().isPrefixOf(selectedFile.getLocation()), "Given selected file is not descendant of given project");

        selectedFilePath = selectedFile.getLocation()
                                       .removeFirstSegments(project.getLocation().segmentCount())
                                       .removeTrailingSeparator();
        getRevisions();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCompareClicked() {
        compare();
    }

    /** {@inheritDoc} */
    @Override
    public void onRevisionUnselected() {
        selectedRevision = null;
        view.setEnableCompareButton(false);
        view.setDescription(locale.viewCompareRevisionFullDescriptionEmptyMessage());
    }

    /** {@inheritDoc} */
    @Override
    public void onRevisionSelected(@NotNull Revision revision) {
        selectedRevision = revision;

        view.setEnableCompareButton(true);
    }

    /** {@inheritDoc} */
    @Override
    public void onRevisionDoubleClicked() {
        compare();
    }

    /** Get list of revisions. */
    private void getRevisions() {
        service.log(appContext.getDevMachine(), project.getLocation(), new Path[]{selectedFilePath}, false)
               .then(new Operation<LogResponse>() {
                   @Override
                   public void apply(LogResponse log) throws OperationException {
                       view.setRevisions(log.getCommits());
                       view.showDialog();
                   }
               }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                    dialogFactory.createMessageDialog(locale.compareWithRevisionTitle(),
                                                      locale.initCommitWasNotPerformed(),
                                                      null).show();
                } else {
                    notificationManager.notify(locale.logFailed(), FAIL, NOT_EMERGE_MODE);
                }
            }
        });
    }

    private void compare() {
        service.diff(appContext.getDevMachine(),
                     project.getLocation(),
                     singletonList(selectedFilePath.toString()),
                     NAME_STATUS,
                     false,
                     0,
                     selectedRevision.getId(),
                     false)
               .then(new Operation<String>() {
                   @Override
                   public void apply(final String diff) throws OperationException {
                       if (diff.isEmpty()) {
                           dialogFactory.createMessageDialog(locale.compareMessageIdenticalContentTitle(),
                                                             locale.compareMessageIdenticalContentText(), null).show();
                       } else {
                           project.getFile(diff.substring(2)).then(new Operation<Optional<File>>() {
                               @Override
                               public void apply(Optional<File> file) throws OperationException {
                                   if (file.isPresent()) {
                                       comparePresenter.show(file.get(), defineStatus(diff.substring(0, 1)), selectedRevision.getId());
                                   }
                               }
                           });

                       }
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
                   }
               });
    }
}

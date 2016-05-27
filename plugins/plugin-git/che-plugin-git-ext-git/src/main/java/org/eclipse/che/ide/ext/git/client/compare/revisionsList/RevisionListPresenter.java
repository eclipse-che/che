/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.compare.revisionsList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.api.git.shared.DiffRequest.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for displaying list of revisions for comparing selected with local changes.
 *
 * @author Igor Vinokur
 */
@Singleton
public class RevisionListPresenter implements RevisionListView.ActionDelegate {
    private final ComparePresenter        comparePresenter;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final SelectionAgent          selectionAgent;
    private final DialogFactory           dialogFactory;
    private final RevisionListView        view;
    private final GitServiceClient        gitService;
    private final GitLocalizationConstant locale;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;

    private ProjectConfigDto project;
    private Revision         selectedRevision;
    private String           selectedFile;

    @Inject
    public RevisionListPresenter(RevisionListView view,
                                 ComparePresenter comparePresenter,
                                 GitServiceClient service,
                                 GitLocalizationConstant locale,
                                 AppContext appContext,
                                 NotificationManager notificationManager,
                                 DialogFactory dialogFactory,
                                 DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                 SelectionAgent selectionAgent) {
        this.view = view;
        this.comparePresenter = comparePresenter;
        this.dialogFactory = dialogFactory;
        this.gitService = service;
        this.locale = locale;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.selectionAgent = selectionAgent;
        this.view.setDelegate(this);
    }

    /** Open dialog and shows revisions to compare. */
    public void showRevisions() {
        project = appContext.getCurrentProject().getRootProject();
        String path;

        Selection<?> selection = getExplorerSelection();

        if (selection == null || selection.getHeadElement() == null) {
            path = project.getPath();
        } else {
            path = ((HasStorablePath)selection.getHeadElement()).getStorablePath();
        }

        String pattern = path.replaceFirst(project.getPath(), "");
        selectedFile = (pattern.startsWith("/")) ? pattern.replaceFirst("/", "") : pattern;

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
        gitService.log(appContext.getDevMachine(), project, Collections.singletonList(selectedFile), false,
                       new AsyncRequestCallback<LogResponse>(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class)) {

                           @Override
                           protected void onSuccess(LogResponse result) {
                               view.setRevisions(result.getCommits());
                               view.showDialog();
                           }

                           @Override
                           protected void onFailure(Throwable exception) {
                               if (getErrorCode(exception) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                                   dialogFactory.createMessageDialog(locale.compareWithRevisionTitle(),
                                                                     locale.initCommitWasNotPerformed(),
                                                                     null).show();
                               } else {
                                   notificationManager.notify(locale.logFailed(), FAIL, NOT_EMERGE_MODE);
                               }

                           }
                       });
    }

    private Selection<?> getExplorerSelection() {
        final Selection<?> selection = selectionAgent.getSelection();
        if (selection == null || selection.isEmpty() || selection.getHeadElement() instanceof HasStorablePath) {
            return selection;
        } else {
            return null;
        }
    }

    private void compare() {
        gitService.diff(appContext.getDevMachine(), project, Collections.singletonList(selectedFile), NAME_STATUS, false, 0,
                        selectedRevision.getId(), false, new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                            @Override
                            protected void onSuccess(String result) {
                                if (result.isEmpty()) {
                                    dialogFactory.createMessageDialog(locale.compareMessageIdenticalContentTitle(),
                                                                      locale.compareMessageIdenticalContentText(), new ConfirmCallback() {
                                                                          @Override
                                                                          public void accepted() {
                                                                              //Do nothing
                                                                          }
                                                                      }).show();
                                } else {
                                    comparePresenter.show(result.substring(2),
                                                          defineStatus(result.substring(0, 1)),
                                                          selectedRevision.getId());
                                }
                            }

                            @Override
                            protected void onFailure(Throwable exception) {
                                notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
                            }
                        });
    }
}

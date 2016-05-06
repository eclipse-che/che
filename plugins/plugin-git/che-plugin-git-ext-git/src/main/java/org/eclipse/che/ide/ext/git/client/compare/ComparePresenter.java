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
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.ADDED;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.DELETED;

/**
 * Presenter for comparing current files with files from specified revision or branch.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ComparePresenter implements CompareView.ActionDelegate {

    private final AppContext              appContext;
    private final EventBus                eventBus;
    private final DialogFactory           dialogFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final CompareView             view;
    private final ProjectServiceClient    projectService;
    private final GitServiceClient        gitService;
    private final GitLocalizationConstant locale;
    private final NotificationManager     notificationManager;

    private String item;
    private String newContent;

    @Inject
    public ComparePresenter(AppContext appContext,
                            EventBus eventBus,
                            DialogFactory dialogFactory,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            CompareView view,
                            ProjectServiceClient projectServiceClient,
                            GitServiceClient gitServiceClient,
                            GitLocalizationConstant locale,
                            NotificationManager notificationManager) {
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.view = view;
        this.projectService = projectServiceClient;
        this.gitService = gitServiceClient;
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.view.setDelegate(this);
    }

    /**
     * Show compare window.
     *
     * @param file
     *         file name with its full path
     * @param status
     *         status of the file
     * @param revision
     *         hash of revision or branch
     */
    public void show(final String file, final Status status, final String revision) {
        this.item = file;

        if (status.equals(ADDED)) {
            showCompare(file, "", revision);
        } else if (status.equals(DELETED)) {
            gitService.showFileContent(appContext.getDevMachine(), appContext.getCurrentProject().getRootProject(), file, revision,
                                       new AsyncRequestCallback<ShowFileContentResponse>(
                                               dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class)) {
                                           @Override
                                           protected void onSuccess(final ShowFileContentResponse response) {
                                               view.setTitle(file);
                                               view.show(response.getContent(), "", revision, file);
                                           }

                                           @Override
                                           protected void onFailure(Throwable exception) {
                                               notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                                           }
                                       });
        } else {
            gitService.showFileContent(appContext.getDevMachine(), appContext.getCurrentProject().getRootProject(), file, revision,
                                       new AsyncRequestCallback<ShowFileContentResponse>(
                                               dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class)) {
                                           @Override
                                           protected void onSuccess(final ShowFileContentResponse response) {
                                               showCompare(file, response.getContent(), revision);
                                           }

                                           @Override
                                           protected void onFailure(Throwable exception) {
                                               notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                                           }
                                       });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(final String newContent) {
        if (this.newContent == null || newContent.equals(this.newContent)) {
            view.hide();
            return;
        }

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                final String path = appContext.getCurrentProject().getRootProject().getName() + "/" + item;
                projectService.updateFile(appContext.getDevMachine(), path, newContent, new AsyncRequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        eventBus.fireEvent(new FileContentUpdateEvent("/" + path));
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                    }
                });
                view.hide();
            }
        };

        CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                view.hide();
            }
        };

        dialogFactory.createConfirmDialog(locale.compareSaveTitle(), locale.compareSaveQuestion(), locale.buttonYes(), locale.buttonNo(),
                                          confirmCallback, cancelCallback).show();
    }

    private void showCompare(final String file, final String oldContent, final String revision) {
        String fullItemPath = appContext.getCurrentProject().getRootProject().getName() + "/" + file;

        projectService.getFileContent(appContext.getDevMachine(),
                                      fullItemPath,
                                      new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                          @Override
                                          protected void onSuccess(final String newContent) {
                                              view.setTitle(file);
                                              ComparePresenter.this.newContent = newContent;
                                              view.show(oldContent, newContent, revision, file);
                                          }

                                          @Override
                                          protected void onFailure(Throwable exception) {
                                              notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                                          }
                                      });
    }
}

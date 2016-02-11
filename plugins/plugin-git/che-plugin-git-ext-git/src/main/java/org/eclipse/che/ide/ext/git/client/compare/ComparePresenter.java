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

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for comparing current files with files from specified revision or branch.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ComparePresenter implements CompareView.ActionDelegate {

    private final AppContext             appContext;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final CompareView            view;
    private final ProjectServiceClient   projectService;
    private final GitServiceClient       gitService;
    private final NotificationManager    notificationManager;
    private final String                 workspaceId;

    @Inject
    public ComparePresenter(AppContext appContext,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            CompareView view,
                            ProjectServiceClient projectServiceClient,
                            GitServiceClient gitServiceClient,
                            NotificationManager notificationManager) {
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.view = view;
        this.projectService = projectServiceClient;
        this.gitService = gitServiceClient;
        this.notificationManager = notificationManager;
        this.view.setDelegate(this);
        
        this.workspaceId = appContext.getWorkspaceId();
    }

    /**
     * Show compare window.
     *
     * @param file
     *         file name with its full path
     * @param state
     *         state of the file
     * @param revision
     *         hash of revision or branch
     */
    public void show(final String file, final String state, final String revision) {
        if (state.startsWith("A")) {
            showCompare(file, "", revision);
        } else if (state.startsWith("D")) {
            gitService.showFileContent(workspaceId, appContext.getCurrentProject().getRootProject(), file, revision,
                                       new AsyncRequestCallback<ShowFileContentResponse>(
                                               dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class)) {
                                           @Override
                                           protected void onSuccess(final ShowFileContentResponse response) {
                                               view.setTitle(file);
                                               view.show(response.getContent(), "", revision, file);
                                           }

                                           @Override
                                           protected void onFailure(Throwable exception) {
                                               notificationManager.notify(exception.getMessage(), FAIL, false);
                                           }
                                       });
        } else {
            gitService.showFileContent(workspaceId, appContext.getCurrentProject().getRootProject(), file, revision,
                                       new AsyncRequestCallback<ShowFileContentResponse>(
                                               dtoUnmarshallerFactory.newUnmarshaller(ShowFileContentResponse.class)) {
                                           @Override
                                           protected void onSuccess(final ShowFileContentResponse response) {
                                               showCompare(file, response.getContent(), revision);
                                           }

                                           @Override
                                           protected void onFailure(Throwable exception) {
                                               notificationManager.notify(exception.getMessage(), FAIL, false);
                                           }
                                       });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseButtonClicked() {
        view.hide();
    }

    private void showCompare(final String file, final String oldContent, final String revision) {
        String fullItemPath = appContext.getCurrentProject().getRootProject().getName() + "/" + file;

        projectService.getFileContent(appContext.getWorkspace().getId(),
                                      fullItemPath,
                                      new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                          @Override
                                          protected void onSuccess(final String newContent) {
                                              view.setTitle(file);
                                              view.show(oldContent, newContent, revision, file);
                                          }

                                          @Override
                                          protected void onFailure(Throwable exception) {
                                              notificationManager.notify(exception.getMessage(), FAIL, false);
                                          }
                                      });
    }
}

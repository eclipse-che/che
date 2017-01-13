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
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.resource.Path;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.ADDED;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.DELETED;

/**
 * Presenter for comparing current files with files from specified revision or branch.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ComparePresenter implements CompareView.ActionDelegate {

    private final AppContext              appContext;
    private final EventBus                eventBus;
    private final DialogFactory           dialogFactory;
    private final CompareView             view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant locale;
    private final NotificationManager     notificationManager;

    private File   comparedFile;
    private String revision;
    private String localContent;

    @Inject
    public ComparePresenter(AppContext appContext,
                            EventBus eventBus,
                            DialogFactory dialogFactory,
                            CompareView view,
                            GitServiceClient service,
                            GitLocalizationConstant locale,
                            NotificationManager notificationManager) {
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;
        this.view = view;
        this.service = service;
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
    public void show(final File file, final Status status, final String revision) {
        this.comparedFile = file;
        this.revision = revision;

        if (status.equals(ADDED)) {
            showCompare("");
            return;
        }

        final Optional<Project> project = file.getRelatedProject();

        if (!project.isPresent()) {
            return;
        }

        final Path relPath = file.getLocation().removeFirstSegments(project.get().getLocation().segmentCount());

        if (status.equals(DELETED)) {
            service.showFileContent(appContext.getDevMachine(), project.get().getLocation(), relPath, revision)
                   .then(new Operation<ShowFileContentResponse>() {
                       @Override
                       public void apply(ShowFileContentResponse content) throws OperationException {
                           view.setTitle(file.getLocation().toString());
                           view.show(content.getContent(), "", revision, file.getLocation().toString());
                       }
                   })
                   .catchError(new Operation<PromiseError>() {
                       @Override
                       public void apply(PromiseError error) throws OperationException {
                           notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                       }
                   });
        } else {

            service.showFileContent(appContext.getDevMachine(), project.get().getLocation(), relPath, revision)
                   .then(new Operation<ShowFileContentResponse>() {
                       @Override
                       public void apply(ShowFileContentResponse content) throws OperationException {
                           showCompare(content.getContent());
                       }
                   })
                   .catchError(new Operation<PromiseError>() {
                       @Override
                       public void apply(PromiseError error) throws OperationException {
                           notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                       }
                   });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(final String newContent) {
        if (this.localContent == null || newContent.equals(localContent)) {
            view.hide();
            return;
        }

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                comparedFile.updateContent(newContent).then(new Operation<Void>() {
                    @Override
                    public void apply(Void ignored) throws OperationException {
                        final Container parent = comparedFile.getParent();

                        if (parent != null) {
                            parent.synchronize();
                        }

                        eventBus.fireEvent(new FileContentUpdateEvent(comparedFile.getLocation().toString()));
                        view.hide();
                    }
                });
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

    private void showCompare(final String remoteContent) {
        comparedFile.getContent().then(new Operation<String>() {
            @Override
            public void apply(String local) throws OperationException {
                localContent = local;
                final String path = comparedFile.getLocation().toString();
                view.setTitle(path);
                view.show(remoteContent, localContent, revision, path);
            }
        });
    }
}

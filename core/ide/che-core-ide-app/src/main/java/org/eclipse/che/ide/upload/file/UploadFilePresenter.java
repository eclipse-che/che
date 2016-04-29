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
package org.eclipse.che.ide.upload.file;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.upload.BasicUploadPresenter;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

/**
 * The purpose of this class is upload file
 *
 * @author Roman Nikitenko.
 */
public class UploadFilePresenter extends BasicUploadPresenter implements UploadFileView.ActionDelegate {

    private final UploadFileView           view;
    private final String                   workspaceId;
    private final EventBus                 eventBus;
    private final NotificationManager      notificationManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final CoreLocalizationConstant locale;
    private final AppContext               appContext;

    @Inject
    public UploadFilePresenter(UploadFileView view,
                               AppContext appContext,
                               EventBus eventBus,
                               NotificationManager notificationManager,
                               ProjectExplorerPresenter projectExplorer,
                               CoreLocalizationConstant locale) {
        super(projectExplorer);
        this.appContext = appContext;
        this.workspaceId = appContext.getWorkspace().getId();
        this.eventBus = eventBus;
        this.view = view;
        this.projectExplorer = projectExplorer;
        this.locale = locale;
        this.view.setDelegate(this);
        this.view.setEnabledUploadButton(false);
        this.notificationManager = notificationManager;
    }

    /** Show dialog. */
    public void showDialog() {
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onSubmitComplete(String result) {
        projectExplorer.reloadChildren(getResourceBasedNode());
        if (result != null && !result.isEmpty()) {
            view.closeDialog();
            notificationManager.notify(locale.failedToUploadFiles(), parseMessage(result), StatusNotification.Status.FAIL, FLOAT_MODE);
            return;
        }

        if (view.isOverwriteFileSelected()) {
            String path = ((HasStorablePath)getResourceBasedNode()).getStorablePath() + "/" + view.getFileName();
            eventBus.fireEvent(new FileContentUpdateEvent(path));
        }
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onUploadClicked() {
        view.setEncoding(FormPanel.ENCODING_MULTIPART);
        view.setAction(appContext.getDevMachine().getWsAgentBaseUrl() +"/project/" + workspaceId + "/uploadfile"
                       + ((HasStorablePath)getResourceBasedNode()).getStorablePath());
        view.submit();
    }

    /** {@inheritDoc} */
    @Override
    public void onFileNameChanged() {
        String fileName = view.getFileName();
        boolean enabled = !fileName.isEmpty();
        view.setEnabledUploadButton(enabled);
    }
}

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
package org.eclipse.che.ide.projectimport.local;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.vfs.gwt.client.VfsServiceClient;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 * @author Valeriy Svydenko
 */
public class LocalZipImporterPagePresenter implements LocalZipImporterPageView.ActionDelegate {

    private final AppContext                    appContext;
    private final CoreLocalizationConstant      locale;
    private final LocalZipImporterPageView      view;
    private final DtoFactory                    dtoFactory;
    private final String                        workspaceId;
    private final String                        extPath;
    private final EventBus                      eventBus;
    private final VfsServiceClient              vfsServiceClient;
    private final ProjectServiceClient          projectServiceClient;
    private final DialogFactory                 dialogFactory;
    private final ProjectNotificationSubscriber projectNotificationSubscriber;

    @Inject
    public LocalZipImporterPagePresenter(LocalZipImporterPageView view,
                                         DtoFactory dtoFactory,
                                         CoreLocalizationConstant locale,
                                         AppContext appContext,
                                         @Named("cheExtensionPath") String extPath,
                                         EventBus eventBus,
                                         VfsServiceClient vfsServiceClient,
                                         ProjectServiceClient projectServiceClient,
                                         DialogFactory dialogFactory,
                                         ProjectNotificationSubscriber projectNotificationSubscriber) {
        this.view = view;
        this.locale = locale;
        this.dtoFactory = dtoFactory;
        this.workspaceId = appContext.getWorkspace().getId();
        this.extPath = extPath;
        this.eventBus = eventBus;
        this.vfsServiceClient = vfsServiceClient;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;
        this.dialogFactory = dialogFactory;
        this.projectNotificationSubscriber = projectNotificationSubscriber;
        this.view.setDelegate(this);
    }

    public void show() {
        updateView();
        view.showDialog();
    }

    @Override
    public void projectNameChanged() {
        view.setEnabledImportButton(isCompleted());
    }

    @Override
    public void fileNameChanged() {
        String projectName = extractProjectName(view.getFileName());
        if (!projectName.isEmpty()) {
            view.setProjectName(projectName);
            projectNameChanged();
        }
    }

    @Override
    public void onSubmitComplete(String result) {
        try {
            showProcessing(false);

            result = extractFromHtmlFormat(result);
            if (result.isEmpty()) {
                importFailure(locale.importProjectMessageFailure(extractProjectName(view.getFileName())));
                return;
            }

            ProjectConfigDto projectConfig = dtoFactory.createDtoFromJson(result, ProjectConfigDto.class);
            if (projectConfig == null) {
                importFailure(JsonHelper.parseJsonMessage(result));
                return;
            }
            importSuccess(projectConfig);
        } catch (Exception e) {
            importFailure(result);
        }
    }

    private void importSuccess(ProjectConfigDto projectConfig) {
        view.closeDialog();
        projectNotificationSubscriber.onSuccess();

        eventBus.fireEvent(new CreateProjectEvent(projectConfig));
    }

    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }

    @Override
    public void onImportClicked() {
        // check on VFS because need to check whether the folder with the same name already exists in the root of workspace
        final String projectName = view.getProjectName();
        vfsServiceClient.getItemByPath(appContext.getWorkspaceId(), projectName, new AsyncRequestCallback<Item>() {
            @Override
            protected void onSuccess(Item result) {
                view.setEnabledImportButton(false);
                dialogFactory.createMessageDialog("", locale.createProjectFromTemplateProjectExists(projectName), null).show();
            }

            @Override
            protected void onFailure(Throwable exception) {
                importProject();
            }
        });
    }

    private void importProject() {
        final String projectName = view.getProjectName();
        projectNotificationSubscriber.subscribe(projectName);

        view.setEncoding(FormPanel.ENCODING_MULTIPART);
        view.setAction(extPath + "/project/" + workspaceId + "/upload/zipproject/" + projectName + "?force=false");
        view.submit();
        showProcessing(true);
    }

    private void importFailure(String error) {
        deleteProject(view.getProjectName());
        view.closeDialog();
        projectNotificationSubscriber.onFailure(error);
    }

    /** Updates view from data-object. */
    private void updateView() {
        view.setProjectName("");
        view.setProjectDescription("");
        view.setSkipFirstLevel(true);
    }

    /** Shown the state that the request is processing. */
    private void showProcessing(boolean inProgress) {
        view.setLoaderVisibility(inProgress);
        view.setInputsEnableState(!inProgress);
    }

    private String extractProjectName(@NotNull String zipName) {
        int indexStartProjectName = zipName.lastIndexOf("\\") + 1;
        int indexFinishProjectName = zipName.indexOf(".zip");
        if (indexFinishProjectName != (-1)) {
            return zipName.substring(indexStartProjectName, indexFinishProjectName);
        }
        return "";
    }

    private void deleteProject(final String name) {
        projectServiceClient.delete(appContext.getWorkspace().getId(), name, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                Log.info(LocalZipImporterPagePresenter.class, "Project " + name + " deleted.");
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(LocalZipImporterPagePresenter.class, exception);
            }
        });
    }

    private boolean isProjectNameCorrect() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.hideNameError();
            return true;
        }
        view.showNameError();
        return false;

    }

    private boolean isCompleted() {
        return view.getFileName().contains(".zip") && isProjectNameCorrect();
    }

    private String extractFromHtmlFormat(String text) {
        int beginIndex = -1;
        int lastIndex = -1;

        if (text.contains("<pre")) {
            beginIndex = text.indexOf(">") + 1;
            lastIndex = text.lastIndexOf("</pre");
        }
        return beginIndex != 0 && lastIndex != -1 ? text.substring(beginIndex, lastIndex) : text;
    }
}

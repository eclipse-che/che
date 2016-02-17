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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.importer.AbstractImporter;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.projectimport.ErrorMessageUtils;

import javax.validation.constraints.NotNull;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectImporter extends AbstractImporter {

    private final CoreLocalizationConstant localizationConstant;
    private final EventBus                 eventBus;
    private final ProjectResolver          projectResolver;

    private ProjectConfigDto projectConfig;
    private CompleteCallback callback;

    @Inject
    public ProjectImporter(ProjectServiceClient projectService,
                           CoreLocalizationConstant localizationConstant,
                           ImportProjectNotificationSubscriberFactory subscriberFactory,
                           AppContext appContext,
                           EventBus eventBus,
                           ProjectResolver projectResolver) {
        super(appContext, projectService, subscriberFactory);
        this.localizationConstant = localizationConstant;
        this.projectResolver = projectResolver;
        this.eventBus = eventBus;
    }

    public void checkFolderExistenceAndImport(final CompleteCallback callback, final ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
        this.callback = callback;
        // check on VFS because need to check whether the folder with the same name already exists in the root of workspace
        final String projectName = projectConfig.getName();
//        vfsServiceClient.getItemByPath(workspaceId, projectName, new AsyncRequestCallback<Item>() {
//            @Override
//            protected void onSuccess(Item result) {
//                callback.onFailure(new Exception(localizationConstant.createProjectFromTemplateProjectExists(projectName)));
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                String pathToProject = '/' + projectName;
//
//                startImport(pathToProject, projectName, projectConfig.getSource());
//            }
//        });
    }

    @Override
    protected Promise<Void> importProject(@NotNull String pathToProject,
                                          @NotNull String projectName,
                                          @NotNull SourceStorageDto sourceStorage) {
        final ProjectNotificationSubscriber subscriber = subscriberFactory.createSubscriber();
        subscriber.subscribe(projectName);

        Promise<Void> importPromise = projectService.importProject(workspaceId, pathToProject, false, sourceStorage);

        return importPromise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new CreateProjectEvent(projectConfig));
                projectResolver.resolveProject(callback, projectConfig);

                subscriber.onSuccess();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError exception) throws OperationException {
                subscriber.onFailure(exception.getMessage());
                int errorCode = ErrorMessageUtils.getErrorCode(exception.getCause());
                // no ssh key found code. See org.eclipse.che.git.impl.nativegit.ssh.SshKeyProviderImpl.
                if (errorCode == ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY) {
                    callback.onFailure(new Exception(localizationConstant.importProjectMessageUnableGetSshKey()));
                    return;
                }
                callback.onFailure(new Exception(exception.getCause().getMessage()));
            }
        });
    }
}

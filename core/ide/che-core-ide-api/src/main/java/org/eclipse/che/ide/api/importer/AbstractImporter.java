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
package org.eclipse.che.ide.api.importer;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;

import javax.validation.constraints.NotNull;

/**
 * The general class for all importers. The class contains business logic which allows add and remove projects in list projects
 * which are in importing state. The project is added in special list before import starts and removed from list when import finishes or
 * some exception occurs.
 *
 * @author Dmitry Shnurenko
 */
public abstract class AbstractImporter {

    protected final AppContext appContext;

    protected final ProjectServiceClient                       projectService;
    protected final String                                     workspaceId;
    protected final ImportProjectNotificationSubscriberFactory subscriberFactory;

    protected AbstractImporter(@NotNull AppContext appContext,
                               @NotNull ProjectServiceClient projectService,
                               @NotNull ImportProjectNotificationSubscriberFactory subscriberFactory) {
        this.appContext = appContext;
        this.projectService = projectService;
        this.workspaceId = appContext.getWorkspaceId();
        this.subscriberFactory = subscriberFactory;
    }

    /**
     * Starts project importing. This method should be called when we want mark project as importing.
     *
     * @param pathToProject
     *         path to project which will be imported. Path example '/project_name'
     * @param projectName
     *         name of project
     * @param sourceStorage
     *         information about project location and repository type
     * @return returns instance of Promise
     */
    protected Promise<Void> startImport(@NotNull final String pathToProject,
                                        @NotNull final String projectName,
                                        @NotNull final SourceStorageDto sourceStorage) {
        appContext.addProjectToImporting(pathToProject);

        Promise<Void> importPromise = importProject(pathToProject, projectName, sourceStorage);

        return importPromise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                appContext.removeProjectFromImporting(pathToProject);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                appContext.removeProjectFromImporting(pathToProject);
            }
        });
    }

    /**
     * The method imports projects from location.
     *
     * @param pathToProject
     *         path to project which will be imported. Path example '/project_name'
     * @param projectName
     *         name of project
     * @param sourceStorage
     *         information about project location and repository type
     * @return returns instance of Promise
     */
    protected abstract Promise<Void> importProject(@NotNull String pathToProject,
                                                   @NotNull final String projectName,
                                                   @NotNull final SourceStorageDto sourceStorage);
}

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
package org.eclipse.che.api.project.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImportersService;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.InitBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/**
 * Guice module contains configuration of Project API components.
 *
 * @author gazarenkov
 * @author Artem Zatsarynnyi
 */
public class ProjectApiModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<ProjectImporter> projectImportersMultibinder = Multibinder.newSetBinder(binder(), ProjectImporter.class);
        projectImportersMultibinder.addBinding().to(ZipProjectImporter.class);

        Multibinder.newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

        Multibinder<ProjectHandler> projectHandlersMultibinder = Multibinder.newSetBinder(binder(), ProjectHandler.class);
        projectHandlersMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);
        projectHandlersMultibinder.addBinding().to(InitBaseProjectTypeHandler.class);

        bind(ProjectRegistry.class).asEagerSingleton();
        bind(ProjectService.class);
        bind(ProjectTypeService.class);
        bind(ProjectImportersService.class);

        bind(WorkspaceProjectsSyncer.class).to(WorkspaceHolder.class);
    }
}

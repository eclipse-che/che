/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.impl.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.impl.OnWorkspaceStartProjectInitializer;
import org.eclipse.che.api.project.server.impl.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.impl.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.impl.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.impl.ProjectServiceApi;
import org.eclipse.che.api.project.server.impl.ProjectServiceApiFactory;
import org.eclipse.che.api.project.server.impl.ProjectSynchronizer;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.project.server.impl.RegisteredProjectFactory;
import org.eclipse.che.api.project.server.impl.RootDirCreationHandler;
import org.eclipse.che.api.project.server.impl.ValidatingProjectManager;
import org.eclipse.che.api.project.server.impl.WorkspaceProjectSynchronizer;
import org.eclipse.che.api.project.server.impl.ZipProjectImporter;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.InitBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.type.ProjectQualifier;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeResolver;
import org.eclipse.che.api.project.server.type.ProjectTypes;
import org.eclipse.che.api.project.server.type.ProjectTypesFactory;
import org.eclipse.che.api.project.server.type.SimpleProjectQualifier;
import org.eclipse.che.api.project.server.type.SimpleProjectTypeResolver;

/**
 * Guice module contains configuration of Project API components.
 *
 * @author gazarenkov
 * @author Artem Zatsarynnyi
 * @author Dmitry Kuleshov
 */
public class ProjectApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ProjectService.class);
    bind(ProjectImportersService.class);
    bind(ProjectTypeService.class);

    bind(OnWorkspaceStartProjectInitializer.class);
    bind(ProjectConfigRegistry.class);
    bind(ProjectImporterRegistry.class);
    bind(ProjectHandlerRegistry.class);

    bind(RootDirCreationHandler.class).asEagerSingleton();

    bind(ProjectManager.class).to(ValidatingProjectManager.class);
    bind(ProjectSynchronizer.class).to(WorkspaceProjectSynchronizer.class);
    bind(ProjectQualifier.class).to(SimpleProjectQualifier.class);
    bind(ProjectTypeResolver.class).to(SimpleProjectTypeResolver.class);

    newSetBinder(binder(), ProjectImporter.class).addBinding().to(ZipProjectImporter.class);

    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

    Multibinder<ProjectHandler> projectHandlers = newSetBinder(binder(), ProjectHandler.class);
    projectHandlers.addBinding().to(CreateBaseProjectTypeHandler.class);
    projectHandlers.addBinding().to(InitBaseProjectTypeHandler.class);

    install(
        new FactoryModuleBuilder()
            .implement(ProjectConfig.class, RegisteredProject.class)
            .build(RegisteredProjectFactory.class));

    install(
        new FactoryModuleBuilder()
            .implement(ProjectTypes.class, ProjectTypes.class)
            .build(ProjectTypesFactory.class));

    install(
        new FactoryModuleBuilder()
            .implement(ProjectServiceApi.class, ProjectServiceApi.class)
            .build(ProjectServiceApiFactory.class));
  }
}

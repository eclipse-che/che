/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.SimpleProjectHandlerRegistry;
import org.eclipse.che.api.project.server.impl.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.impl.ProjectInitializer;
import org.eclipse.che.api.project.server.type.ProjectQualifier;
import org.eclipse.che.api.project.server.impl.ProjectSynchronizer;
import org.eclipse.che.api.project.server.type.ProjectTypeResolver;
import org.eclipse.che.api.project.server.impl.SimpleProjectConfigRegistry;
import org.eclipse.che.api.project.server.importer.SimpleProjectImporterRegistry;
import org.eclipse.che.api.project.server.impl.SimpleProjectInitializer;
import org.eclipse.che.api.project.server.type.SimpleProjectQualifier;
import org.eclipse.che.api.project.server.type.SimpleProjectTypeResolver;
import org.eclipse.che.api.project.server.impl.ValidatingProjectManager;
import org.eclipse.che.api.project.server.impl.WorkspaceProjectSynchronizer;
import org.eclipse.che.api.project.server.importer.ZipProjectImporter;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.InitBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

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
    bind(ProjectService.class).asEagerSingleton();
    bind(ProjectTypeService.class).asEagerSingleton();
    bind(ProjectImportersService.class).asEagerSingleton();
    bind(ProjectSynchronizer.class).to(WorkspaceProjectSynchronizer.class);
    bind(ProjectImporterRegistry.class).to(SimpleProjectImporterRegistry.class);
    bind(ProjectConfigRegistry.class).to(SimpleProjectConfigRegistry.class);
    bind(ProjectHandlerRegistry.class).to(SimpleProjectHandlerRegistry.class);
    bind(ProjectInitializer.class).to(SimpleProjectInitializer.class);
    bind(ProjectManager.class).to(ValidatingProjectManager.class);
    bind(ProjectQualifier.class).to(SimpleProjectQualifier.class);
    bind(ProjectTypeResolver.class).to(SimpleProjectTypeResolver.class);

    newSetBinder(binder(), ProjectImporter.class).addBinding().to(ZipProjectImporter.class);
    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

    Multibinder<ProjectHandler> projectHandlers =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlers.addBinding().to(CreateBaseProjectTypeHandler.class);
    projectHandlers.addBinding().to(InitBaseProjectTypeHandler.class);


  }
}

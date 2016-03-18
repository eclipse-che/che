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
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.watcher.WatcherService;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static org.eclipse.che.inject.Matchers.names;

/**
 * Deploys project API components.
 *
 * @author andrew00x
 */
public class BaseProjectModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ProjectImporter.class).addBinding().to(ZipProjectImporter.class);
        Multibinder.newSetBinder(binder(), ValueProviderFactory.class); /* empty binding */
        Multibinder.newSetBinder(binder(), ProjectHandler.class); /* empty binding */

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);

        bind(ProjectService.class);
        bind(ProjectTypeService.class);
        bind(ProjectImportersService.class);
        bind(WatcherService.class);

        ProjectImporterInterceptor projectImporterInterceptor = new ProjectImporterInterceptor();
        requestInjection(projectImporterInterceptor);

        bindInterceptor(Matchers.subclassesOf(ProjectImporter.class), names("importSources"), projectImporterInterceptor);
    }
}

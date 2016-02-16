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

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/**
 * @author gazarenkov
 */
public class ProjectApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProjectService.class);
        bind(ProjectTypeService.class);

        Multibinder.newSetBinder(binder(), ProjectImporter.class).addBinding().to(ZipProjectImporter.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class);

        Multibinder<ProjectTypeDef> projectTypesMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypesMultibinder.addBinding().to(BaseProjectType.class);
    }
}

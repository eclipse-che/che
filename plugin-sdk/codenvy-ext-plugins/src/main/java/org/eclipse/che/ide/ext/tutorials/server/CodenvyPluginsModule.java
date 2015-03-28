/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.server;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Artem Zatsarynnyy */
@DynaModule
public class CodenvyPluginsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ProjectTemplateRegistrar.class).asEagerSingleton();

        Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(ExtensionProjectType.class);
        projectTypeMultibinder.addBinding().to(TutorialProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(CodenvyTutorialGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(CodenvyExtensionGenerator.class);

    }
}

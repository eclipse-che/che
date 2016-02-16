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
package org.eclipse.che.ide.extension.maven.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.ide.ext.java.server.classpath.ClassPathBuilder;
import org.eclipse.che.ide.extension.maven.server.core.MavenClassPathBuilder;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenProjectType;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenTargetFilter;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenValueProviderFactory;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.ArchetypeGenerationStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectCreatedHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectImportedHandler;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/** @author Artem Zatsarynnyi */
public class MavenModule extends AbstractModule {
    @Override
    protected void configure() {
        newSetBinder(binder(), ValueProviderFactory.class).addBinding().to(MavenValueProviderFactory.class);

        newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(MavenProjectType.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(MavenProjectGenerator.class);
//        projectHandlerMultibinder.addBinding().to(AddMavenModuleHandler.class);
//        projectHandlerMultibinder.addBinding().to(RemoveMavenModuleHandler.class);
        projectHandlerMultibinder.addBinding().to(MavenProjectImportedHandler.class);
//        projectHandlerMultibinder.addBinding().to(ProjectHasBecomeMaven.class);
        projectHandlerMultibinder.addBinding().to(MavenProjectCreatedHandler.class);

        newSetBinder(binder(), GeneratorStrategy.class).addBinding().to(ArchetypeGenerationStrategy.class);
        bind(ClassPathBuilder.class).to(MavenClassPathBuilder.class).in(Singleton.class);

        Multibinder<VirtualFileFilter> multibinder = newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));
        multibinder.addBinding().to(MavenTargetFilter.class);
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.server.PomModificationDetector;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenServerNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenTerminalImpl;
import org.eclipse.che.plugin.maven.server.core.project.PomChangeListener;
import org.eclipse.che.plugin.maven.server.projecttype.MavenProjectType;
import org.eclipse.che.plugin.maven.server.projecttype.MavenValueProviderFactory;
import org.eclipse.che.plugin.maven.server.projecttype.handler.ArchetypeGenerationStrategy;
import org.eclipse.che.plugin.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.plugin.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.plugin.maven.server.projecttype.handler.MavenProjectInitHandler;
import org.eclipse.che.plugin.maven.server.projecttype.handler.SimpleGeneratorStrategy;
import org.eclipse.che.plugin.maven.server.rest.MavenServerService;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/** @author Artem Zatsarynnyi */
@DynaModule
public class MavenModule extends AbstractModule {

    @Override
    protected void configure() {
        newSetBinder(binder(), ValueProviderFactory.class).addBinding().to(MavenValueProviderFactory.class);

        //bind maven project type only if maven installed on dev machine
        if(System.getenv("M2_HOME") != null) {
            newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(MavenProjectType.class);
        }

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(MavenProjectGenerator.class);
        projectHandlerMultibinder.addBinding().to(MavenProjectInitHandler.class);

        Multibinder<GeneratorStrategy> generatorStrategyMultibinder = newSetBinder(binder(), GeneratorStrategy.class);
        generatorStrategyMultibinder.addBinding().to(SimpleGeneratorStrategy.class);
        generatorStrategyMultibinder.addBinding().to(ArchetypeGenerationStrategy.class);

        bind(MavenTerminal.class).to(MavenTerminalImpl.class).in(Singleton.class);
        bind(MavenProgressNotifier.class).to(MavenServerNotifier.class).in(Singleton.class);

        bind(MavenServerService.class);

        bind(PomChangeListener.class).asEagerSingleton();
        bind(PomModificationDetector.class).asEagerSingleton();
    }
}

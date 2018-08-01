/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.nio.file.PathMatcher;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.generator.archetype.MavenArchetypeJsonRpcMessenger;
import org.eclipse.che.plugin.maven.lsp.MavenLanguageServerConfig;
import org.eclipse.che.plugin.maven.server.PomModificationDetector;
import org.eclipse.che.plugin.maven.server.core.MavenJsonRpcCommunication;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenServerNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenTerminalImpl;
import org.eclipse.che.plugin.maven.server.core.project.PomChangeListener;
import org.eclipse.che.plugin.maven.server.projecttype.MavenProjectType;
import org.eclipse.che.plugin.maven.server.projecttype.MavenTargetExcludeMatcher;
import org.eclipse.che.plugin.maven.server.projecttype.MavenValueProviderFactory;
import org.eclipse.che.plugin.maven.server.projecttype.handler.ArchetypeGenerationStrategy;
import org.eclipse.che.plugin.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.plugin.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.plugin.maven.server.projecttype.handler.MavenProjectInitHandler;
import org.eclipse.che.plugin.maven.server.projecttype.handler.SimpleGeneratorStrategy;
import org.eclipse.che.plugin.maven.server.rest.MavenServerService;

/** @author Artem Zatsarynnyi */
@DynaModule
public class MavenModule extends AbstractModule {

  @Override
  protected void configure() {
    newSetBinder(binder(), ValueProviderFactory.class)
        .addBinding()
        .to(MavenValueProviderFactory.class);

    // bind maven project type only if maven installed on dev machine
    if (System.getenv("M2_HOME") != null) {
      newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(MavenProjectType.class);
    }

    Multibinder<ProjectHandler> projectHandlerMultibinder =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlerMultibinder.addBinding().to(MavenProjectGenerator.class);
    projectHandlerMultibinder.addBinding().to(MavenProjectInitHandler.class);

    Multibinder<GeneratorStrategy> generatorStrategyMultibinder =
        newSetBinder(binder(), GeneratorStrategy.class);
    generatorStrategyMultibinder.addBinding().to(SimpleGeneratorStrategy.class);
    generatorStrategyMultibinder.addBinding().to(ArchetypeGenerationStrategy.class);

    Multibinder<PathMatcher> excludeMatcher =
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));
    excludeMatcher.addBinding().to(MavenTargetExcludeMatcher.class);

    Multibinder<PathMatcher> fileWatcherExcludes =
        newSetBinder(
            binder(), PathMatcher.class, Names.named("che.user.workspaces.storage.excludes"));
    fileWatcherExcludes.addBinding().to(MavenTargetExcludeMatcher.class);

    bind(MavenTerminal.class).to(MavenTerminalImpl.class).in(Singleton.class);
    bind(MavenProgressNotifier.class).to(MavenServerNotifier.class).in(Singleton.class);

    bind(MavenServerService.class);
    bind(MavenJsonRpcCommunication.class);
    bind(MavenArchetypeJsonRpcMessenger.class);

    bind(PomChangeListener.class).asEagerSingleton();
    bind(PomModificationDetector.class).asEagerSingleton();

    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.maven")
        .to(MavenLanguageServerConfig.class)
        .asEagerSingleton();
  }
}

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
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.util.function.Consumer;
import org.eclipse.che.api.fs.api.FsDtoConverter;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.fs.api.PathResolver;
import org.eclipse.che.api.fs.search.DotCheExcludeMatcher;
import org.eclipse.che.api.fs.search.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.fs.search.LuceneSearcher;
import org.eclipse.che.api.fs.search.MediaTypesExcludeMatcher;
import org.eclipse.che.api.fs.search.Searcher;
import org.eclipse.che.api.fs.watcher.FileTreeWalker;
import org.eclipse.che.api.fs.watcher.FileWatcherByPathMatcher;
import org.eclipse.che.api.fs.watcher.FileWatcherIgnoreFileTracker;
import org.eclipse.che.api.fs.watcher.IndexedFileCreateConsumer;
import org.eclipse.che.api.fs.watcher.IndexedFileDeleteConsumer;
import org.eclipse.che.api.fs.watcher.IndexedFileUpdateConsumer;
import org.eclipse.che.api.fs.watcher.detectors.EditorFileOperationHandler;
import org.eclipse.che.api.fs.watcher.detectors.EditorFileTracker;
import org.eclipse.che.api.fs.watcher.detectors.ProjectTreeTracker;
import org.eclipse.che.api.project.server.api.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.api.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.api.ProjectImporter;
import org.eclipse.che.api.project.server.api.ProjectInitializer;
import org.eclipse.che.api.project.server.api.ProjectManager;
import org.eclipse.che.api.project.server.api.ProjectQualifier;
import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImportersService;
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
    bind(FsManager.class).to(org.eclipse.che.api.fs.impl.FsManager.class);
    bind(FsDtoConverter.class).to(org.eclipse.che.api.fs.impl.FsDtoConverter.class);
    bind(PathResolver.class).to(org.eclipse.che.api.fs.impl.PathResolver.class);
    bind(ProjectConfigRegistry.class)
        .to(org.eclipse.che.api.project.server.impl.ProjectConfigRegistry.class);
    bind(ProjectHandlerRegistry.class)
        .to(org.eclipse.che.api.project.server.impl.ProjectHandlerRegistry.class);
    bind(ProjectInitializer.class)
        .to(org.eclipse.che.api.project.server.impl.ProjectInitializer.class);
    bind(ProjectManager.class)
        .to(org.eclipse.che.api.project.server.impl.SuspendingProjectManager.class);
    bind(ProjectQualifier.class).to(org.eclipse.che.api.project.server.impl.ProjectQualifier.class);
    bind(Searcher.class).to(LuceneSearcher.class);

    Multibinder<ProjectImporter> projectImportersMultibinder =
        newSetBinder(binder(), ProjectImporter.class);
    projectImportersMultibinder.addBinding().to(ZipProjectImporter.class);

    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

    Multibinder<ProjectHandler> projectHandlersMultibinder =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlersMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);
    projectHandlersMultibinder.addBinding().to(InitBaseProjectTypeHandler.class);

    bind(ProjectService.class);
    bind(ProjectTypeService.class);
    bind(ProjectImportersService.class);

    bind(WorkspaceProjectsSyncer.class).to(WorkspaceHolder.class);

    Multibinder<PathMatcher> excludeMatcher =
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));
    excludeMatcher.addBinding().to(MediaTypesExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotCheExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotNumberSignExcludeMatcher.class);

    Multibinder<PathMatcher> fileWatcherExcludes =
        newSetBinder(
            binder(), PathMatcher.class, Names.named("che.user.workspaces.storage.excludes"));
    fileWatcherExcludes.addBinding().to(MediaTypesExcludeMatcher.class);
    fileWatcherExcludes.addBinding().to(DotCheExcludeMatcher.class);
    fileWatcherExcludes.addBinding().to(DotNumberSignExcludeMatcher.class);

    bind(EditorChangesTracker.class).asEagerSingleton();
    bind(EditorWorkingCopyManager.class).asEagerSingleton();
    bind(FileWatcherIgnoreFileTracker.class).asEagerSingleton();

    configureVfsEvent();
    configureTreeWalker();
  }

  private void configureTreeWalker() {
    bind(FileTreeWalker.class).asEagerSingleton();

    Multibinder<Consumer<Path>> directoryUpdateConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.update"));
    Multibinder<Consumer<Path>> directoryCreateConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.create"));
    Multibinder<Consumer<Path>> directoryDeleteConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.delete"));
    Multibinder<PathMatcher> directoryExcludes =
        newSetBinder(
            binder(), new TypeLiteral<PathMatcher>() {}, Names.named("che.fs.directory.excludes"));

    Multibinder<Consumer<Path>> fileUpdateConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.update"));
    Multibinder<Consumer<Path>> fileCreateConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.create"));
    Multibinder<Consumer<Path>> fileDeleteConsumers =
        newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.delete"));
    Multibinder<PathMatcher> fileExcludes =
        newSetBinder(
            binder(), new TypeLiteral<PathMatcher>() {}, Names.named("che.fs.file.excludes"));

    fileCreateConsumers.addBinding().to(IndexedFileCreateConsumer.class);
    fileUpdateConsumers.addBinding().to(IndexedFileUpdateConsumer.class);
    fileDeleteConsumers.addBinding().to(IndexedFileDeleteConsumer.class);

    fileCreateConsumers.addBinding().to(FileWatcherByPathMatcher.class);
    fileDeleteConsumers.addBinding().to(FileWatcherByPathMatcher.class);
    directoryCreateConsumers.addBinding().to(FileWatcherByPathMatcher.class);
    directoryDeleteConsumers.addBinding().to(FileWatcherByPathMatcher.class);
  }

  private void addVfsFilter(Multibinder<PathMatcher> excludeMatcher, String filter) {
    excludeMatcher
        .addBinding()
        .toInstance(
            path -> {
              for (Path pathElement : path) {
                if (pathElement == null || filter.equals(pathElement.toString())) {
                  return true;
                }
              }
              return false;
            });
  }

  private void configureVfsEvent() {
    bind(EditorFileTracker.class).asEagerSingleton();
    bind(EditorFileOperationHandler.class).asEagerSingleton();
    bind(ProjectTreeTracker.class).asEagerSingleton();
  }

  @Provides
  @Singleton
  protected WatchService watchService() {
    try {
      return FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      getLogger(ProjectApiModule.class).error("Error provisioning watch service", e);
      return null;
    }
  }
}

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
import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImportOutputJsonRpcRegistrar;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImportersService;
import org.eclipse.che.api.project.server.matchers.DotCheMatcher;
import org.eclipse.che.api.project.server.matchers.DotNumberSignMatcher;
import org.eclipse.che.api.project.server.matchers.HiddenItemPathMatcher;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.InitBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.event.detectors.EditorFileOperationHandler;
import org.eclipse.che.api.vfs.impl.file.event.detectors.EditorFileTracker;
import org.eclipse.che.api.vfs.impl.file.event.detectors.ProjectTreeTracker;
import org.eclipse.che.api.vfs.search.MediaTypeFilter;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.vfs.watcher.FileTreeWalker;
import org.eclipse.che.api.vfs.watcher.FileWatcherByPathMatcher;
import org.eclipse.che.api.vfs.watcher.FileWatcherIgnoreFileTracker;
import org.eclipse.che.api.vfs.watcher.IndexedFileCreateConsumer;
import org.eclipse.che.api.vfs.watcher.IndexedFileDeleteConsumer;
import org.eclipse.che.api.vfs.watcher.IndexedFileUpdateConsumer;

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
    Multibinder<ProjectImporter> projectImportersMultibinder =
        newSetBinder(binder(), ProjectImporter.class);
    projectImportersMultibinder.addBinding().to(ZipProjectImporter.class);

    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

    Multibinder<ProjectHandler> projectHandlersMultibinder =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlersMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);
    projectHandlersMultibinder.addBinding().to(InitBaseProjectTypeHandler.class);

    bind(ProjectRegistry.class).asEagerSingleton();
    bind(ProjectService.class);
    bind(ProjectTypeService.class);
    bind(ProjectImportersService.class);
    bind(ProjectImportOutputJsonRpcRegistrar.class);

    bind(WorkspaceProjectsSyncer.class).to(WorkspaceHolder.class);

    // configure VFS
    Multibinder<VirtualFileFilter> filtersMultibinder =
        newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));

    filtersMultibinder.addBinding().to(MediaTypeFilter.class);

    Multibinder<PathMatcher> indexExcludesMatcher =
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));
    Multibinder<PathMatcher> fileWatcherExcludes =
        newSetBinder(
            binder(), PathMatcher.class, Names.named("che.user.workspaces.storage.excludes"));

    bind(SearcherProvider.class).to(FSLuceneSearcherProvider.class);
    bind(VirtualFileSystemProvider.class).to(LocalVirtualFileSystemProvider.class);

    bind(FileWatcherNotificationHandler.class).to(DefaultFileWatcherNotificationHandler.class);

    bind(EditorChangesTracker.class).asEagerSingleton();
    bind(EditorWorkingCopyManager.class).asEagerSingleton();
    bind(FileWatcherIgnoreFileTracker.class).asEagerSingleton();

    indexExcludesMatcher.addBinding().to(DotCheMatcher.class);
    indexExcludesMatcher.addBinding().to(DotNumberSignMatcher.class);
    indexExcludesMatcher.addBinding().to(HiddenItemPathMatcher.class);

    fileWatcherExcludes.addBinding().to(DotCheMatcher.class);
    fileWatcherExcludes.addBinding().to(DotNumberSignMatcher.class);

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

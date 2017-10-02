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
package org.eclipse.che.api.watcher.server;

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
import org.eclipse.che.api.project.server.EditorChangesTracker;
import org.eclipse.che.api.project.server.EditorWorkingCopyManager;
import org.eclipse.che.api.search.server.impl.DotCheExcludeMatcher;
import org.eclipse.che.api.search.server.impl.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.search.server.impl.IndexedFileCreateConsumer;
import org.eclipse.che.api.search.server.impl.IndexedFileDeleteConsumer;
import org.eclipse.che.api.search.server.impl.IndexedFileUpdateConsumer;
import org.eclipse.che.api.search.server.impl.MediaTypesExcludeMatcher;
import org.eclipse.che.api.watcher.server.detectors.EditorFileOperationHandler;
import org.eclipse.che.api.watcher.server.detectors.EditorFileTracker;
import org.eclipse.che.api.watcher.server.detectors.ProjectTreeTracker;

public class FileWatcherApiModule extends AbstractModule {

  @Override
  protected void configure() {
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
      getLogger(FileWatcherApiModule.class).error("Error provisioning watch service", e);
      return null;
    }
  }
}

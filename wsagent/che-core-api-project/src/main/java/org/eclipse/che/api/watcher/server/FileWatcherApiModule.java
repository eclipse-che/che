/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import org.eclipse.che.api.search.server.excludes.DotCheExcludeMatcher;
import org.eclipse.che.api.search.server.excludes.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.watcher.server.detectors.EditorFileOperationHandler;
import org.eclipse.che.api.watcher.server.detectors.EditorFileTracker;
import org.eclipse.che.api.watcher.server.detectors.ProjectTreeTracker;
import org.eclipse.che.api.watcher.server.impl.FileTreeWalker;
import org.eclipse.che.api.watcher.server.impl.FileWatcherByPathMatcher;
import org.eclipse.che.api.watcher.server.impl.FileWatcherIgnoreFileTracker;
import org.eclipse.che.api.watcher.server.impl.SimpleFileWatcherManager;

public class FileWatcherApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FileWatcherManager.class).to(SimpleFileWatcherManager.class);
    bind(FileWatcherIgnoreFileTracker.class).asEagerSingleton();

    Multibinder<PathMatcher> fileWatcherExcludes =
        newSetBinder(
            binder(), PathMatcher.class, Names.named("che.user.workspaces.storage.excludes"));
    fileWatcherExcludes.addBinding().to(DotCheExcludeMatcher.class);
    fileWatcherExcludes.addBinding().to(DotNumberSignExcludeMatcher.class);

    configureVfsEvent();
    configureTreeWalker();
    configureFileWatcherManagerPathMatcher();
  }

  private void configureTreeWalker() {
    bind(FileTreeWalker.class).asEagerSingleton();

    newSetBinder(
        binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.update"));
    newSetBinder(
        binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.create"));
    newSetBinder(
        binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.delete"));
    newSetBinder(
        binder(), new TypeLiteral<PathMatcher>() {}, Names.named("che.fs.directory.excludes"));
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.update"));
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.create"));
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.delete"));
    newSetBinder(binder(), new TypeLiteral<PathMatcher>() {}, Names.named("che.fs.file.excludes"));
  }

  private void configureFileWatcherManagerPathMatcher() {
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.create"))
        .addBinding()
        .to(FileWatcherByPathMatcher.class);
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.delete"))
        .addBinding()
        .to(FileWatcherByPathMatcher.class);
    newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.create"))
        .addBinding()
        .to(FileWatcherByPathMatcher.class);
    newSetBinder(
            binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.directory.delete"))
        .addBinding()
        .to(FileWatcherByPathMatcher.class);
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

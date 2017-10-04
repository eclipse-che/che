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
package org.eclipse.che.api.editor.server;

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
import org.eclipse.che.api.editor.server.impl.EditorChangesTracker;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.search.server.excludes.DotCheExcludeMatcher;
import org.eclipse.che.api.search.server.excludes.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.search.server.excludes.MediaTypesExcludeMatcher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.che.api.watcher.server.detectors.EditorFileOperationHandler;
import org.eclipse.che.api.watcher.server.detectors.EditorFileTracker;
import org.eclipse.che.api.watcher.server.detectors.ProjectTreeTracker;
import org.eclipse.che.api.watcher.server.impl.FileTreeWalker;
import org.eclipse.che.api.watcher.server.impl.FileWatcherByPathMatcher;
import org.eclipse.che.api.watcher.server.impl.FileWatcherIgnoreFileTracker;
import org.eclipse.che.api.watcher.server.impl.SimpleFileWatcherManager;

public class EditorApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(EditorChangesTracker.class).asEagerSingleton();
    bind(EditorWorkingCopyManager.class).asEagerSingleton();
  }
}

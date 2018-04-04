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
package org.eclipse.che.api.search.server;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;
import org.eclipse.che.api.search.server.consumers.IndexedFileCreateConsumer;
import org.eclipse.che.api.search.server.consumers.IndexedFileDeleteConsumer;
import org.eclipse.che.api.search.server.consumers.IndexedFileUpdateConsumer;
import org.eclipse.che.api.search.server.excludes.DotCheExcludeMatcher;
import org.eclipse.che.api.search.server.excludes.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.search.server.excludes.MediaTypesExcludeMatcher;
import org.eclipse.che.api.search.server.impl.LuceneSearcher;

public class SearchApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Searcher.class).to(LuceneSearcher.class);

    Multibinder<PathMatcher> excludeMatcher =
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));
    excludeMatcher.addBinding().to(MediaTypesExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotCheExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotNumberSignExcludeMatcher.class);
    excludeMatcher.addBinding().to(HiddenItemPathMatcher.class);

    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.create"))
        .addBinding()
        .to(IndexedFileCreateConsumer.class);
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.update"))
        .addBinding()
        .to(IndexedFileUpdateConsumer.class);
    newSetBinder(binder(), new TypeLiteral<Consumer<Path>>() {}, Names.named("che.fs.file.delete"))
        .addBinding()
        .to(IndexedFileDeleteConsumer.class);
  }
}

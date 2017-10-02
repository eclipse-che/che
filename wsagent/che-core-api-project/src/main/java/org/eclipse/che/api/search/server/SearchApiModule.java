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
package org.eclipse.che.api.search.server;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.nio.file.PathMatcher;
import org.eclipse.che.api.search.server.impl.DotCheExcludeMatcher;
import org.eclipse.che.api.search.server.impl.DotNumberSignExcludeMatcher;
import org.eclipse.che.api.search.server.impl.LuceneSearcher;
import org.eclipse.che.api.search.server.impl.MediaTypesExcludeMatcher;

public class SearchApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Searcher.class).to(LuceneSearcher.class);

    Multibinder<PathMatcher> excludeMatcher =
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));
    excludeMatcher.addBinding().to(MediaTypesExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotCheExcludeMatcher.class);
    excludeMatcher.addBinding().to(DotNumberSignExcludeMatcher.class);
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search.server.consumers;

import java.nio.file.Path;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.search.server.Searcher;

@Singleton
public class IndexedFileCreateConsumer implements Consumer<Path> {

  private final Searcher searcher;

  @Inject
  public IndexedFileCreateConsumer(Searcher searcher) {
    this.searcher = searcher;
  }

  @Override
  public void accept(Path fsPath) {
    searcher.add(fsPath);
  }
}

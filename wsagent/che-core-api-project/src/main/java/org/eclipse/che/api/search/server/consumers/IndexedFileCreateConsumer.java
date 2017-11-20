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
package org.eclipse.che.api.search.server.consumers;

import java.nio.file.Path;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.search.server.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IndexedFileCreateConsumer implements Consumer<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(IndexedFileCreateConsumer.class);

  private final Searcher searcher;

  @Inject
  public IndexedFileCreateConsumer(Searcher searcher) {
    this.searcher = searcher;
  }

  @Override
  public void accept(Path fsPath) {
    try {
      searcher.add(fsPath);
    } catch (ServerException | NotFoundException e) {
      LOG.error("Issue happened during adding created file to index", e);
    }
  }
}

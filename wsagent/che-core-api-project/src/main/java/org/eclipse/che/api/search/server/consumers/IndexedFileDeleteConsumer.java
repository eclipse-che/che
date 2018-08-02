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
package org.eclipse.che.api.search.server.consumers;

import java.nio.file.Path;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.search.server.Searcher;

@Singleton
public class IndexedFileDeleteConsumer implements Consumer<Path> {

  private final Searcher searcher;

  @Inject
  public IndexedFileDeleteConsumer(Searcher searcher) {
    this.searcher = searcher;
  }

  @Override
  public void accept(Path fsPath) {
    searcher.delete(fsPath);
  }
}

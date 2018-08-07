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
package org.eclipse.che.api.languageserver.consumers;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.PathTransformer;

/**
 * Consumer for the file changing operation.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class LanguageServerFileChangeConsumer implements Consumer<Path> {
  private final Map<Consumer<String>, Set<PathMatcher>> consumers = new ConcurrentHashMap<>();

  private PathTransformer pathTransformer;

  @Inject
  public LanguageServerFileChangeConsumer(PathTransformer pathTransformer) {
    this.pathTransformer = pathTransformer;
  }

  @Override
  public void accept(Path path) {
    for (Entry<Consumer<String>, Set<PathMatcher>> entry : consumers.entrySet()) {
      for (PathMatcher matcher : entry.getValue()) {
        if (matcher.matches(path)) {
          entry.getKey().accept(pathTransformer.transform(path));
        }
      }
    }
  }

  /**
   * Adds registered consumer and set of matchers for matching files by path
   *
   * @param create registered consumer
   * @param matcher set of matchers for matching file's path
   */
  public void watch(Consumer<String> create, Set<PathMatcher> matcher) {
    consumers.putIfAbsent(create, matcher);
  }
}

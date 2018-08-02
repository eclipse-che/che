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
package org.eclipse.che.api.languageserver;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;

/**
 * Utility class that simplifies finding language server id.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class FindId {
  private final Registry<Set<Pattern>> patterns;

  @Inject
  FindId(RegistryContainer registryContainer) {
    this.patterns = registryContainer.patternRegistry;
  }

  /**
   * Finds language server ids that corresponds to a specified workspace path
   *
   * @param wsPath absolute workspace path
   * @return set of language server ids
   */
  Set<String> byPath(String wsPath) {
    Set<String> ids = new HashSet<>();

    for (Entry<String, Set<Pattern>> entry : patterns.getAll().entrySet()) {
      String id = entry.getKey();
      Set<Pattern> patterns = entry.getValue();

      for (Pattern pattern : patterns) {
        if (pattern.matcher(wsPath).matches()) {
          ids.add(id);
        }
      }
    }

    return ImmutableSet.copyOf(ids);
  }
}

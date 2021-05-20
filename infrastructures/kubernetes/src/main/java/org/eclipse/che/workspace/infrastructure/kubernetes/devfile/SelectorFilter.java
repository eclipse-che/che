/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.stream.Collectors.toCollection;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/** Helper class to filter Kubernetes objects by a selector. */
public class SelectorFilter {

  /**
   * Uses the {@link #test(ObjectMeta, Map)} method to filter the provided list.
   *
   * @param list the list to filter
   * @param selector the selector to match the metadata of the objects in the list with
   * @return the filtered list
   */
  public static List<HasMetadata> filter(List<HasMetadata> list, Map<String, String> selector) {
    return list.stream()
        .filter(o -> test(o.getMetadata(), selector))
        .collect(toCollection(ArrayList::new));
  }

  /**
   * Returns true is specified object is matched by specified selector, false otherwise.
   *
   * <p>An empty selector is considered to match anything.
   *
   * @param metadata object metadata to check matching
   * @param selector the selector to match the metadata with
   */
  public static boolean test(@Nullable ObjectMeta metadata, Map<String, String> selector) {
    if (selector.isEmpty()) {
      // anything matches if we have nothing to select with
      return true;
    }

    if (metadata == null) {
      return false;
    }

    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      return false;
    }

    return labels.entrySet().containsAll(selector.entrySet());
  }

  private SelectorFilter() {
    throw new AssertionError("Thou shall not instantiate me!");
  }
}

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
package org.eclipse.che.api.devfile.server.convert.tool.kubernetes;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.Map;

/** Helper class to filter Kubernetes objects by a selector. */
public class SelectorFilter {

  /**
   * Returns true is specified object is matched by specified selector, false otherwise
   *
   * @param metadata object metadata to check matching
   * @param selector the selector to match the metadata with
   */
  public static boolean test(ObjectMeta metadata, Map<String, String> selector) {
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

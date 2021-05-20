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
package org.eclipse.che.api.metrics;

import java.util.Arrays;

/** Utility methods for workspace meter binders. */
final class WorkspaceBinders {

  private static final String METRIC_NAME_PREFIX = "che.workspace.";
  private static final String[] STANDARD_TAGS = new String[] {"area", "workspace"};

  private WorkspaceBinders() {}

  /** Produces a name for the workspace metric with the standard prefix. */
  static String workspaceMetric(String name) {
    return METRIC_NAME_PREFIX + name;
  }

  /**
   * Produces a list of tags to add to the metric. The returned array always contains standard tags
   * common to all workspace metrics.
   *
   * @param tags the additional tags to add in addition to the standard tags
   * @return an array representing the tags
   */
  static String[] withStandardTags(String... tags) {
    if (tags.length == 0) {
      return STANDARD_TAGS;
    }

    String[] ret = Arrays.copyOf(STANDARD_TAGS, STANDARD_TAGS.length + tags.length);
    System.arraycopy(tags, 0, ret, STANDARD_TAGS.length, tags.length);
    return ret;
  }
}

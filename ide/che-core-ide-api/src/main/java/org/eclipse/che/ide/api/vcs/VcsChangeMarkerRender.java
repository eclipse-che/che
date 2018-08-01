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
package org.eclipse.che.ide.api.vcs;

import org.eclipse.che.api.git.shared.EditedRegionType;

/** Component that handles VCS change markers. */
public interface VcsChangeMarkerRender {

  /**
   * Add change marker to the gutter on the given lines.
   *
   * @param lineStart the first line number of the marker
   * @param lineEnd the last line number of the marker
   * @param type type of the marker e.g. insertion, modification, deletion
   */
  void addChangeMarker(int lineStart, int lineEnd, EditedRegionType type);

  /** Clear all VCS change markers in the gutter. */
  void clearAllChangeMarkers();
}

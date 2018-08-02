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
package org.eclipse.che.ide.api.editor.texteditor;

import org.eclipse.che.ide.api.editor.text.TextRange;

/** Interface for component that handle marked ranges of text. */
public interface HasTextMarkers {

  /**
   * Add a marker on the text range.
   *
   * @param range the range
   * @param className the CSS class
   * @return an handle to clear the mark.
   */
  MarkerRegistration addMarker(TextRange range, String className);

  /** Registration object to remove a text range marker. */
  interface MarkerRegistration {
    /** Clear the mark. */
    void clearMark();
  }
}

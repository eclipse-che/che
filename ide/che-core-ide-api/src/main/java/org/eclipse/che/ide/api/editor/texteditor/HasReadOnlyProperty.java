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
package org.eclipse.che.ide.api.editor.texteditor;

public interface HasReadOnlyProperty {

  /**
   * Sets the editable state.
   *
   * @param isReadOnly the read only state
   */
  void setReadOnly(final boolean isReadOnly);

  /**
   * Returns whether the shown text can be manipulated.
   *
   * @return the viewer's readOnly state
   */
  boolean isReadOnly();
}

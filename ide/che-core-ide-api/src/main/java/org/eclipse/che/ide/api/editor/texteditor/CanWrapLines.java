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

/**
 * Determined whether the editor implementing this interface can wrap lines.
 *
 * @author Vitaliy Guliy
 */
public interface CanWrapLines {

  /**
   * Determines whether line wrapping is active.
   *
   * @return <b>true</b> when line wrapping is active
   */
  boolean isWrapLines();

  /** Toggles line wrapping mode. */
  void toggleWrapLines();
}

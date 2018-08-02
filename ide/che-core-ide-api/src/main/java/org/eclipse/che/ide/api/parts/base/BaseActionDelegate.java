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
package org.eclipse.che.ide.api.parts.base;

/**
 * Base interface for action delegates, provide method for minimizing part.
 *
 * @author Evgen Vidolob
 */
public interface BaseActionDelegate {

  /** Toggles maximized state of the part. */
  void onToggleMaximize();

  /**
   * Activate Part when clicking the mouse. Is used when the Part contains frames and mouse events
   * are blocked.
   */
  void onActivate();
}

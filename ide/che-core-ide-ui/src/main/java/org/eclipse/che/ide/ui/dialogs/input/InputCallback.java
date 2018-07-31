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
package org.eclipse.che.ide.ui.dialogs.input;

/**
 * Callback called when the user clicks on "OK" in the input dialog.
 *
 * @author Artem Zatsarynnyi
 */
public interface InputCallback {

  /**
   * Action called when the user clicks on OK.
   *
   * @param value the string typed into input dialog
   */
  void accepted(String value);
}

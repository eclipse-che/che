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
 * Interface to the input dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface InputDialog {

  /** Operate the input dialog: show it and manage user actions. */
  void show();

  /**
   * Set the {@link InputValidator} to be called whenever the text changes in the input field in the
   * view.
   *
   * <p>If validator finds the input invalid, the error message is displayed in the dialog's view.
   *
   * @param inputValidator validator to use
   * @return this {@link InputDialog}
   */
  InputDialog withValidator(InputValidator inputValidator);
}

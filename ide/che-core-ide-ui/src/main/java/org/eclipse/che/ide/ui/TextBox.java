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
package org.eclipse.che.ide.ui;

/**
 * An Extension of GWT TextBox. Contains methods for marking the text box containing valid or
 * invalid values.
 *
 * @author Vitaliy Guliy
 */
public class TextBox extends com.google.gwt.user.client.ui.TextBox {

  /** Marks text box containing valid value. */
  public void markValid() {
    getElement().setAttribute("success", "");
    getElement().removeAttribute("error");
  }

  /** Marks text box containing invalid value. */
  public void markInvalid() {
    getElement().removeAttribute("success");
    getElement().setAttribute("error", "");
  }

  /** Removes mark from text box. */
  public void unmark() {
    getElement().removeAttribute("success");
    getElement().removeAttribute("error");
  }
}

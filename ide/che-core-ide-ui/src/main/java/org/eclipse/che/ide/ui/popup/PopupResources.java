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
package org.eclipse.che.ide.ui.popup;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** Resources for the popup window component. */
public interface PopupResources extends ClientBundle {

  /** The CSS resource for the popup window component. */
  @Source({"popup.css", "org/eclipse/che/ide/api/ui/style.css"})
  PopupStyle popupStyle();

  /** The CSS resource interface for the popup window component. */
  interface PopupStyle extends CssResource {

    String popup();

    String header();

    String body();

    String item();

    String icon();

    String label();

    String active();

    String parameter();
  }
}

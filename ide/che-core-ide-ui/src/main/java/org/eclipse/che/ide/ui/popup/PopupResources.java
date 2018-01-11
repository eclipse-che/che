/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
  }
}

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
package org.eclipse.che.ide.menu;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** @author Evgen Vidolob */
public interface MenuResources extends ClientBundle {
  interface Css extends CssResource {

    String menuBar();

    String menuBarTable();

    String menuBarItem();

    String menuBarItemSelected();

    String menuBarItemDisabled();

    String menuBarItemOver();

    String leftPanel();

    String triangleSeparator();

    String rightPanel();

    String commandToolbar();

    String customComponent();

    String panelSeparator();

    String statusPanel();
  }

  @Source({"menu.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css menuCss();
}

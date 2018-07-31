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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/** @author Evgen Vidolob */
public interface ToolbarResources extends ClientBundle {

  interface Css extends CssResource {

    String iconButtonPanel();

    String iconButtonIcon();

    String iconButtonIconInner();

    String popupButtonIconInner();

    String toolbarPanel();

    String iconButtonPanelDown();

    String iconButtonPanelSelectedDown();

    String toolbarDelimiter();

    String toolbarActionGroupPanel();

    String iconButtonPanelSelected();

    String disabled();

    String popupButtonPanel();

    String popupButtonPanelDown();

    String popupButtonIcon();

    String tooltip();

    String tooltipBody();

    String tooltipArrow();

    String caret();

    String leftToolbarPart();

    String centerToolbarPart();

    String rightToolbarPart();
  }

  @Source({"toolbar.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css toolbar();
}

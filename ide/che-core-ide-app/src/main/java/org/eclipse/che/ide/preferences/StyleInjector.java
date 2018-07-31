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
package org.eclipse.che.ide.preferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.ui.window.WindowClientBundle;

/** @author Evgen Vidolob */
@Singleton
public class StyleInjector {
  private Resources resources;
  private WindowClientBundle windowClientBundle;

  @Inject
  public StyleInjector(Resources resources, WindowClientBundle windowClientBundle) {
    this.resources = resources;
    this.windowClientBundle = windowClientBundle;
  }

  public void inject() {
    resources.coreCss().ensureInjected();
    windowClientBundle.getStyle().ensureInjected();
    resources.treeCss().ensureInjected();
    resources.defaultSimpleListCss().ensureInjected();
    resources.partStackCss().ensureInjected();
    resources.dialogBox().ensureInjected();
    resources.clipboardCss().ensureInjected();
    resources.notificationCss().ensureInjected();
    resources.dataGridStyle().ensureInjected();
    resources.cellTableStyle().ensureInjected();
    resources.defaultCategoriesListCss().ensureInjected();
    resources.buttonLoaderCss().ensureInjected();
    resources.menuCss().ensureInjected();

    resources.commandsExplorerCss().ensureInjected();
    resources.commandsPaletteCss().ensureInjected();
    resources.commandToolbarCss().ensureInjected();
    resources.editorCss().ensureInjected();
    resources.commandTypeChooserCss().ensureInjected();
    resources.treeStylesCss().ensureInjected();
  }
}

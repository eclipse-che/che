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

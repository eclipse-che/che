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
package org.eclipse.che.ide.preferences.pages.appearance;

import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.theme.Theme;

/** @author Evgen Vidolob */
public interface AppearanceView extends View<AppearanceView.ActionDelegate> {

  void setThemes(List<Theme> themes, String currentThemeId);

  interface ActionDelegate {

    void themeSelected(String themeId);
  }
}

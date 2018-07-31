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
package org.eclipse.che.ide.api.theme;

import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Manages UI Themes
 *
 * @author Evgen Vidolob
 */
public interface ThemeAgent {

  String PREFERENCE_KEY = "ide.theme";

  /**
   * @param themeId the id of the theme
   * @return theme with theme id or default theme if theme not found
   */
  @NotNull
  Theme getTheme(@NotNull String themeId);

  /** @return default theme */
  Theme getDefault();

  /** @return all known themes */
  List<Theme> getThemes();

  /** @return current theme */
  String getCurrentThemeId();

  /**
   * Sets current theme by theme id. If there is no such theme by given id, then default theme is
   * activated.
   */
  void setTheme(String themeId);
}

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
package org.eclipse.che.ide.api.theme;

import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Manages UI Themes
 *
 * @author Evgen Vidolob
 */
public interface ThemeAgent {

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
}

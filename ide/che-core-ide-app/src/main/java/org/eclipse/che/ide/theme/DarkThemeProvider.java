/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.theme;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeProvider;

/** @author Yevhen Vydolob */
@Singleton
public class DarkThemeProvider implements ThemeProvider {

  public static final String DARK_THEME_ID = "DarkTheme";

  private final PromiseProvider provider;
  private final DarkTheme theme;

  @Inject
  public DarkThemeProvider(PromiseProvider provider, DarkTheme theme) {
    this.provider = provider;
    this.theme = theme;
  }

  @Override
  public String getId() {
    return DARK_THEME_ID;
  }

  @Override
  public String getDescription() {
    return "Dark Theme";
  }

  @Override
  public Promise<Theme> loadTheme() {
    return provider.resolve(theme);
  }
}

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
package org.eclipse.che.ide.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.storage.client.Storage;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;

/**
 * Implementation of ThemeAgent
 *
 * @author Evgen Vidolob
 */
public class ThemeAgentImpl implements ThemeAgent {

  public static final String THEME_STORAGE = "codenvy-theme";
  private final Theme defaultTheme;

  private Map<String, Theme> themes;
  private String currentThemeId;

  @Inject
  public ThemeAgentImpl(DarkTheme darkTheme) {
    defaultTheme = darkTheme;

    themes = new HashMap<>();
  }

  @Inject
  private void registerThemes(Set<Theme> themes) {
    themes.forEach(this::addTheme);
  }

  private void addTheme(@NotNull Theme theme) {
    themes.put(theme.getId(), theme);
  }

  @Override
  public Theme getTheme(@NotNull String themeId) {
    if (themes.containsKey(themeId)) return themes.get(themeId);

    return defaultTheme;
  }

  @Override
  public Theme getDefault() {
    return defaultTheme;
  }

  @Override
  public List<Theme> getThemes() {
    return new ArrayList<>(themes.values());
  }

  @Override
  public void setTheme(String themeId) {
    themeId = themeId != null ? themeId : getCurrentThemeId();
    Theme themeToSet = themeId != null ? getTheme(themeId) : getDefault();
    setCurrentThemeId(themeToSet.getId());

    Document.get().getBody().getStyle().setBackgroundColor(Style.theme.backgroundColor());
  }

  @Override
  public String getCurrentThemeId() {
    if (currentThemeId == null
        && Storage.isLocalStorageSupported()
        && Storage.getLocalStorageIfSupported().getItem(THEME_STORAGE) != null) {
      setCurrentThemeId(Storage.getLocalStorageIfSupported().getItem(THEME_STORAGE));
    }
    return currentThemeId;
  }

  /**
   * Sharing theme ID through "IDE" object makes it readable from native JavaScript. It's needed to
   * display additional menu items in the same style as IDE (style of menu additions must depend on
   * style of IDE).
   */
  public native void setCurrentThemeId(String id) /*-{
        this.@org.eclipse.che.ide.theme.ThemeAgentImpl::currentThemeId = id;
        @org.eclipse.che.ide.api.theme.Style::theme = this.@org.eclipse.che.ide.theme.ThemeAgentImpl::getTheme(Ljava/lang/String;)(id);

        if (typeof(Storage) !== "undefined") {
            localStorage.setItem(@org.eclipse.che.ide.theme.ThemeAgentImpl::THEME_STORAGE, id);
        }

        if ($wnd["IDE"]) {
            $wnd["IDE"].theme = id;
        }
    }-*/;
}

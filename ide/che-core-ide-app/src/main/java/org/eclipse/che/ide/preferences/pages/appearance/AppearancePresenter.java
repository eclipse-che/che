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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.theme.ThemeAgent;

/** @author Evgen Vidolob */
@Singleton
public class AppearancePresenter extends AbstractPreferencePagePresenter
    implements AppearanceView.ActionDelegate {

  public static final String PREF_IDE_THEME = "ide.theme";

  private AppearanceView view;
  private ThemeAgent themeAgent;
  private PreferencesManager preferencesManager;
  private boolean dirty = false;
  private String themeId;

  @Inject
  public AppearancePresenter(
      AppearanceView view,
      CoreLocalizationConstant constant,
      ThemeAgent themeAgent,
      PreferencesManager preferencesManager) {
    super(constant.appearanceTitle(), constant.appearanceCategory());
    this.view = view;
    this.themeAgent = themeAgent;
    this.preferencesManager = preferencesManager;
    view.setDelegate(this);
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    String currentThemeId = preferencesManager.getValue(PREF_IDE_THEME);
    if (currentThemeId == null || currentThemeId.isEmpty()) {
      currentThemeId = themeAgent.getCurrentThemeId();
    }
    view.setThemes(themeAgent.getThemes(), currentThemeId);
  }

  @Override
  public void themeSelected(String themeId) {
    this.themeId = themeId;
    dirty = !themeId.equals(themeAgent.getCurrentThemeId());
    delegate.onDirtyChanged();
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(PREF_IDE_THEME, themeId);
    dirty = false;
  }

  @Override
  public void revertChanges() {
    String currentThemeId = preferencesManager.getValue(PREF_IDE_THEME);
    if (currentThemeId == null || currentThemeId.isEmpty()) {
      currentThemeId = themeAgent.getCurrentThemeId();
    }
    view.setThemes(themeAgent.getThemes(), currentThemeId);

    dirty = false;
  }
}

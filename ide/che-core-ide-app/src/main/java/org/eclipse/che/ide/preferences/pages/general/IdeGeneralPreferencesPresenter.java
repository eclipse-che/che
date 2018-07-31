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
package org.eclipse.che.ide.preferences.pages.general;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.theme.ThemeAgent;

/** @author Evgen Vidolob */
@Singleton
public class IdeGeneralPreferencesPresenter extends AbstractPreferencePagePresenter
    implements IdeGeneralPreferencesView.ActionDelegate {

  public static final String PREF_IDE_GENERAL_TAB_CLOSING = "ide.askBeforeClosingTab";

  private IdeGeneralPreferencesView view;
  private ThemeAgent themeAgent;
  private PreferencesManager preferencesManager;

  private String themeId;
  private boolean isAskBeforeClosingTab;

  @Inject
  public IdeGeneralPreferencesPresenter(
      IdeGeneralPreferencesView view,
      CoreLocalizationConstant constant,
      ThemeAgent themeAgent,
      PreferencesManager preferencesManager) {
    super(constant.generalTitle(), constant.generalCategory());
    this.view = view;
    this.themeAgent = themeAgent;
    this.preferencesManager = preferencesManager;
    view.setDelegate(this);

    themeId = getStoredThemeId();
    isAskBeforeClosingTab = getStoredAskBeforeClosingTab();
  }

  @Override
  public boolean isDirty() {
    return !(themeId.equals(themeAgent.getCurrentThemeId())
        && isAskBeforeClosingTab
            == Boolean.parseBoolean(preferencesManager.getValue(PREF_IDE_GENERAL_TAB_CLOSING)));
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    String currentThemeId = preferencesManager.getValue(ThemeAgent.PREFERENCE_KEY);
    if (currentThemeId == null || currentThemeId.isEmpty()) {
      currentThemeId = themeAgent.getCurrentThemeId();
    }
    view.setThemes(themeAgent.getThemes(), currentThemeId);

    view.setAskBeforeClosingTab(
        Boolean.parseBoolean(preferencesManager.getValue(PREF_IDE_GENERAL_TAB_CLOSING)));
  }

  @Override
  public void themeSelected(String themeId) {
    this.themeId = themeId;
    delegate.onDirtyChanged();
  }

  @Override
  public void onAskBeforeClosingTabChanged(boolean isChecked) {
    this.isAskBeforeClosingTab = isChecked;
    delegate.onDirtyChanged();
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(ThemeAgent.PREFERENCE_KEY, themeId);
    themeAgent.setTheme(themeId);
    preferencesManager.setValue(
        PREF_IDE_GENERAL_TAB_CLOSING, String.valueOf(isAskBeforeClosingTab));
  }

  @Override
  public void revertChanges() {
    String currentThemeId = getStoredThemeId();
    boolean currentAskBeforeClosingTab = getStoredAskBeforeClosingTab();

    view.setThemes(themeAgent.getThemes(), currentThemeId);
    view.setAskBeforeClosingTab(currentAskBeforeClosingTab);

    themeId = currentThemeId;
    isAskBeforeClosingTab = currentAskBeforeClosingTab;
  }

  private boolean getStoredAskBeforeClosingTab() {
    return Boolean.parseBoolean(preferencesManager.getValue(PREF_IDE_GENERAL_TAB_CLOSING));
  }

  private String getStoredThemeId() {
    String currentThemeId = preferencesManager.getValue(ThemeAgent.PREFERENCE_KEY);
    if (currentThemeId == null || currentThemeId.isEmpty()) {
      currentThemeId = themeAgent.getCurrentThemeId();
    }
    return currentThemeId;
  }
}

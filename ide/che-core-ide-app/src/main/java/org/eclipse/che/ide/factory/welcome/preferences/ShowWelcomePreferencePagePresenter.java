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
package org.eclipse.che.ide.factory.welcome.preferences;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/** @author Vitaliy Guliy */
@Singleton
public class ShowWelcomePreferencePagePresenter extends AbstractPreferencePagePresenter
    implements ShowWelcomePreferencePageView.ActionDelegate {

  public static final String SHOW_WELCOME_PREFERENCE_KEY = "plugin-factory.welcome";

  private ShowWelcomePreferencePageView view;
  private PreferencesManager preferencesManager;

  @Inject
  public ShowWelcomePreferencePagePresenter(
      CoreLocalizationConstant localizationConstant,
      ShowWelcomePreferencePageView view,
      PreferencesManager preferencesManager) {
    super(localizationConstant.welcomePreferencesTitle());
    this.view = view;
    this.preferencesManager = preferencesManager;
    view.setDelegate(this);
    view.welcomeField().setValue(true);
  }

  @Override
  public boolean isDirty() {
    String value = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
    if (value == null) {
      return !view.welcomeField().getValue();
    }

    return !view.welcomeField().getValue().equals(Boolean.parseBoolean(value));
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(
        SHOW_WELCOME_PREFERENCE_KEY, view.welcomeField().getValue().toString());
  }

  @Override
  public void revertChanges() {
    view.welcomeField()
        .setValue(Boolean.parseBoolean(preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY)));
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    String value = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
    if (value == null) {
      view.welcomeField().setValue(true);
    } else {
      view.welcomeField()
          .setValue(Boolean.parseBoolean(preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY)));
    }
  }

  @Override
  public void onDirtyChanged() {
    delegate.onDirtyChanged();
  }
}

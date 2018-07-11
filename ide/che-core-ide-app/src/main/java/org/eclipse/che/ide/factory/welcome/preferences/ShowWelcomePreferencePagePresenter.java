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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.valueOf;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

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
      PreferencesManager preferencesManager,
      EventBus eventBus) {
    super(localizationConstant.welcomePreferencesTitle());
    this.view = view;
    this.preferencesManager = preferencesManager;
    view.setDelegate(this);
    view.welcomeField().setValue(true);

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> init());
  }

  private void init() {
    String preference = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
    if (isNullOrEmpty(preference)) {
      preference = "true";
      preferencesManager.setValue(SHOW_WELCOME_PREFERENCE_KEY, preference);
    }
  }

  @Override
  public boolean isDirty() {
    String preference = preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY);
    boolean storedValue = isNullOrEmpty(preference) || parseBoolean(preference);

    return view.welcomeField().getValue() != storedValue;
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(
        SHOW_WELCOME_PREFERENCE_KEY, valueOf(view.welcomeField().getValue()));
  }

  @Override
  public void revertChanges() {
    boolean storedValue = parseBoolean(preferencesManager.getValue(SHOW_WELCOME_PREFERENCE_KEY));

    view.welcomeField().setValue(storedValue);
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

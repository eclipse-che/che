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
package org.eclipse.che.ide.api.preferences.experimental;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/**
 * Abstract preference page presenter for the experimental features. For automatic
 * enablement/disablement of some ActionGroup items use {@link ExperimentalActionGroup}
 */
public abstract class ExperimentalPreferencePresenter extends AbstractPreferencePagePresenter
    implements ExperimentalPreferenceView.ActionDelegate {

  private ExperimentalPreferenceView view;
  private PreferencesManager preferencesManager;
  private boolean dirty = false;
  private boolean enabled;

  public ExperimentalPreferencePresenter(
      ExperimentalPreferenceView view,
      ExperimentalPreferenceLocalizationConstant constant,
      PreferencesManager preferencesManager,
      String title) {
    super(title, constant.experimentalFeaturePreferenceCategory());
    this.view = view;
    this.preferencesManager = preferencesManager;
    enabled = isEnabledInPreferences();
    view.setDelegate(this);
  }

  /**
   * Should return a key to be used for storing enable/disable information in the {@link
   * PreferencesManager}. If {@link ExperimentalActionGroup} is used, then the parameter specified
   * there should be same as this key.
   *
   * @return A key to be used for storing enable/disable information in the {@link
   *     PreferencesManager}
   */
  protected abstract String getExperimentalFeatureEnableKey();

  /** {@inheritDoc} */
  @Override
  public boolean isDirty() {
    return dirty;
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setEnable(enabled);
  }

  /** {@inheritDoc} */
  @Override
  public void enabledChanged(boolean enabled) {
    this.enabled = enabled;
    dirty = (enabled != isEnabledInPreferences());
    delegate.onDirtyChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void storeChanges() {
    preferencesManager.setValue(getExperimentalFeatureEnableKey(), String.valueOf(enabled));
    dirty = false;
  }

  /** {@inheritDoc} */
  @Override
  public void revertChanges() {
    enabled = isEnabledInPreferences();
    view.setEnable(enabled);
    dirty = false;
  }

  private boolean isEnabledInPreferences() {
    return Boolean.valueOf(preferencesManager.getValue(getExperimentalFeatureEnableKey()));
  }
}

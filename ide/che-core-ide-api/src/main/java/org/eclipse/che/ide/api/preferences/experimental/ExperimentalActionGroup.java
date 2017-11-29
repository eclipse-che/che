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

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/** Action group for experimental features items. */
public class ExperimentalActionGroup extends DefaultActionGroup {

  private final PreferencesManager preferencesManager;
  private final String experimentalFeatureEnableKey;

  /**
   * The key parameter is the {@code experimentalFeatureEnableKey} which should be same as that one
   * used in {@link ExperimentalPreferencePresenter#getExperimentalFeatureEnableKey()}
   */
  public ExperimentalActionGroup(
      String shortName,
      boolean popup,
      ActionManager actionManager,
      PreferencesManager preferencesManager,
      String experimentalFeatureEnableKey) {
    super(shortName, popup, actionManager);
    this.preferencesManager = preferencesManager;
    this.experimentalFeatureEnableKey = experimentalFeatureEnableKey;
  }

  public void actionPerformed(ActionEvent event) {
    Presentation presentation = event.getPresentation();
    presentation.setVisible(isEnabled(preferencesManager));
    presentation.setEnabled(isEnabled(preferencesManager));
  }

  public void update(ActionEvent event) {
    Presentation presentation = event.getPresentation();
    presentation.setVisible(isEnabled(preferencesManager));
    presentation.setEnabled(isEnabled(preferencesManager));
  }

  private boolean isEnabled(PreferencesManager preferencesManager) {
    return Boolean.valueOf(preferencesManager.getValue(experimentalFeatureEnableKey));
  }
}

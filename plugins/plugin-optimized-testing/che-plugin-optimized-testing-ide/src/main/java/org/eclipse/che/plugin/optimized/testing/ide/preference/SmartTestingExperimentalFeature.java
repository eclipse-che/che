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
package org.eclipse.che.plugin.optimized.testing.ide.preference;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.preferences.experimental.ExperimentalPreferenceLocalizationConstant;
import org.eclipse.che.ide.api.preferences.experimental.ExperimentalPreferencePresenter;
import org.eclipse.che.ide.api.preferences.experimental.ExperimentalPreferenceView;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestLocalizationConstant;

@Singleton
public class SmartTestingExperimentalFeature extends ExperimentalPreferencePresenter {

  public static final String SMART_TESTING_FEATURE_ENABLE = "smart.testing.feature.enable";

  @Inject
  public SmartTestingExperimentalFeature(
      ExperimentalPreferenceView view,
      ExperimentalPreferenceLocalizationConstant experimentalConstant,
      PreferencesManager preferencesManager,
      OptimizedTestLocalizationConstant constants) {
    super(view, experimentalConstant, preferencesManager, constants.smartTestingTitle());
  }

  @Override
  protected String getExperimentalFeatureEnableKey() {
    return SMART_TESTING_FEATURE_ENABLE;
  }
}

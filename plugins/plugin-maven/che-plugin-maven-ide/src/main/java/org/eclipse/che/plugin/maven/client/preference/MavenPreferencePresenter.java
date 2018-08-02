/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.preference;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;

/**
 * Preference page presenter for Maven plugin.
 *
 * @author Igor Vinokur
 */
@Singleton
public class MavenPreferencePresenter extends AbstractPreferencePagePresenter
    implements MavenPreferenceView.ActionDelegate {

  public static final String PREF_SHOW_ARTIFACT_ID = "maven.artifact.in.project.explorer";

  private final MavenPreferenceView view;
  private final AppContext appContext;
  private final PreferencesManager preferencesManager;

  private boolean showArtifactId;
  private boolean dirty = false;

  @Inject
  public MavenPreferencePresenter(
      MavenPreferenceView view,
      AppContext appContext,
      CoreLocalizationConstant coreLocalizationConstant,
      MavenLocalizationConstant mavenLocalizationConstant,
      PreferencesManager preferencesManager) {
    super(
        mavenLocalizationConstant.mavenPreferencesTitle(),
        coreLocalizationConstant.extensionCategory());
    this.view = view;
    this.appContext = appContext;
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
    view.setSelectedShowArtifactIdCheckBox(getShowArtifactIdPreferenceValue());
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(PREF_SHOW_ARTIFACT_ID, String.valueOf(showArtifactId));
    appContext.getWorkspaceRoot().synchronize();
    dirty = false;
  }

  @Override
  public void revertChanges() {
    view.setSelectedShowArtifactIdCheckBox(getShowArtifactIdPreferenceValue());
    dirty = false;
  }

  @Override
  public void onArtifactIdCheckBoxValueChanged(boolean showArtifactId) {
    this.showArtifactId = showArtifactId;
    dirty = showArtifactId != getShowArtifactIdPreferenceValue();
    delegate.onDirtyChanged();
  }

  private boolean getShowArtifactIdPreferenceValue() {
    return Boolean.valueOf(preferencesManager.getValue(PREF_SHOW_ARTIFACT_ID));
  }
}

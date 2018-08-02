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
package org.eclipse.che.plugin.maven.client.resource;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.plugin.maven.client.preference.MavenPreferencePresenter.PREF_SHOW_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import javax.inject.Inject;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;

/**
 * Intercepts project based resources with maven project type and checks if artifact id is differs
 * from project folder name than interceptor adds {@link PresentableTextMarker} with artifact id in
 * presentable text.
 *
 * @author Vlad Zhukovskiy
 */
public class MavenProjectInterceptor implements ResourceInterceptor {

  private final PreferencesManager preferencesManager;

  @Inject
  public MavenProjectInterceptor(PreferencesManager preferencesManager) {
    this.preferencesManager = preferencesManager;
  }

  /** {@inheritDoc} */
  @Override
  public void intercept(Resource resource) {
    if (!Boolean.valueOf(preferencesManager.getValue(PREF_SHOW_ARTIFACT_ID))) {
      return;
    }
    if (resource.isProject() && ((Project) resource).isTypeOf(MAVEN_ID)) {

      final String artifact = ((Project) resource).getAttribute(ARTIFACT_ID);

      if (!isNullOrEmpty(artifact) && !artifact.equals(resource.getName())) {
        resource.addMarker(new PresentableTextMarker(resource.getName() + " [" + artifact + "]"));
      }
    }
  }
}

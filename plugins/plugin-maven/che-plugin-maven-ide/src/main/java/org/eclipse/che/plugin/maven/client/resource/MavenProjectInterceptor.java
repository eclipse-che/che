/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client.resource;

import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.preferences.pages.appearance.AppearancePresenter.PREF_SHOW_MAVEN_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * Intercepts project based resources with maven project type and checks if artifact id is differs from project folder name than
 * interceptor adds {@link PresentableTextMarker} with artifact id in presentable text.
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
        if (!Boolean.valueOf(preferencesManager.getValue(PREF_SHOW_MAVEN_ARTIFACT_ID))) {
            return;
        }
        if (resource.isProject() && ((Project)resource).isTypeOf(MAVEN_ID)) {

            final String artifact = ((Project)resource).getAttribute(ARTIFACT_ID);

            if (!isNullOrEmpty(artifact) && !artifact.equals(resource.getName())) {
                resource.addMarker(new PresentableTextMarker(resource.getName() + " [" + artifact + "]"));
            }
        }
    }
}

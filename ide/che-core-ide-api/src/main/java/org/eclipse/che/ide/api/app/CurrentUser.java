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
package org.eclipse.che.ide.api.app;

import org.eclipse.che.api.user.shared.dto.ProfileDto;

import com.google.inject.Singleton;
import java.util.Map;

/**
 * Describes current state of user.
 *
 * @author Vitaly Parfonov
 */
@Singleton
public class CurrentUser {

    private ProfileDto          profileDescriptor;
    private Map<String, String> preferences;

    public CurrentUser() {
    }

    public CurrentUser(ProfileDto profileDescriptor) {
        this(profileDescriptor, null);
    }

    public CurrentUser(ProfileDto profileDescriptor, Map<String, String> preferences) {
        this.profileDescriptor = profileDescriptor;
        this.preferences = preferences;
    }

    /**
     * Return current ProfileDescriptor
     *
     * @return
     */
    public ProfileDto getProfile() {
        return profileDescriptor;
    }

    public void setProfile(ProfileDto profileDescriptor) {
        this.profileDescriptor = profileDescriptor;
    }

    /**
     * Return current preferences
     *
     * @return
     */
    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.model.impl;

import org.eclipse.che.api.core.model.user.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for the {@link Profile}.
 *
 * @author Yevhenii Voevodin
 */
public class ProfileImpl implements Profile {

    private String              id;
    private String              email;
    private Map<String, String> attributes;

    public ProfileImpl(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public ProfileImpl(String id, String email, Map<String, String> attributes) {
        this.id = id;
        this.email = email;
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
    }

    public ProfileImpl(Profile profile) {
        this(profile.getUserId(), profile.getEmail(), profile.getAttributes());
    }

    @Override
    public String getUserId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            this.attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProfileImpl)) {
            return false;
        }
        final ProfileImpl that = (ProfileImpl)obj;
        return Objects.equals(id, that.id)
               && Objects.equals(email, that.email)
               && getAttributes().equals(that.getAttributes());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(email);
        hash = 31 * hash + getAttributes().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ProfileImpl{" +
               "id='" + id + '\'' +
               ", email='" + email + '\'' +
               ", attributes=" + attributes +
               '}';
    }
}

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
package org.eclipse.che.ide.context;

import org.eclipse.che.ide.api.app.CurrentUser;

import java.util.Map;

/** Implementation of the {@link CurrentUser}. */
public class CurrentUserImpl implements CurrentUser {

    private String              id;
    private Map<String, String> preferences;

    public CurrentUserImpl() {
    }

    public CurrentUserImpl(String id, Map<String, String> preferences) {
        this.id = id;
        this.preferences = preferences;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }
}

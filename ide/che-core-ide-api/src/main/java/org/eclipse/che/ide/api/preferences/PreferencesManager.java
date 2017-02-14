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
package org.eclipse.che.ide.api.preferences;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Map;


/**
 * Manager for preferences.
 *
 * @author Andrey Plotnikov
 */
public interface PreferencesManager {

    /**
     * Returns preference's value for the given preference's name.
     *
     * @param preference
     *         the preference's name whose associated value will be returned
     * @return preference's value
     */
    @Nullable
    String getValue(String preference);

    /**
     * Associates the preference's value with the preference's name.
     * If preferences previously contained this preference, the old value is replaced by new one.
     *
     * @param preference
     *         preference's name with which the preference's value will be associated
     * @param value
     *         preference's value will be associated with preference's name
     */
    void setValue(String preference, String value);

    /**
     * Persists preferences by passing it to the server side and create notification in case error
     *
     * @return the promise which either updates preference pages state or rejects which an error
     *
     */
    Promise<Void> flushPreferences();

    /**
     * Load preferences from server and create notification in case error
     *
     * @return the promise which either uses preferences or rejects in case an error
     */
    Promise<Map<String, String>> loadPreferences();
}

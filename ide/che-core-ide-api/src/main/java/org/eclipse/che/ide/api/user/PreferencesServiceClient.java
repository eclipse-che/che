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
package org.eclipse.che.ide.api.user;

import org.eclipse.che.api.promises.client.Promise;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * GWT client for preferences service;
 *
 * @author Yevhenii Voevodin
 */
public interface PreferencesServiceClient {

    /**
     * Updates user's preferences using the merge strategy.
     *
     * @param prefsToUpdate
     *         preferences update
     * @return a promise that resolves all the user's preferences, or rejects with an error
     */
    Promise<Map<String, String>> updatePreferences(@NotNull Map<String, String> prefsToUpdate);

    /**
     * Gets user preferences.
     *
     * @return a promise that resolves preferences, or rejects with an error
     */
    Promise<Map<String, String>> getPreferences();
}

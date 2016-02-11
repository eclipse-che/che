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
package org.eclipse.che.api.analytics.client.logger;

import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public interface AnalyticsEventLogger {

    int MAX_PARAMS_NUMBER      = 3;
    int MAX_PARAM_NAME_LENGTH  = 20;
    int MAX_PARAM_VALUE_LENGTH = 100;


    /**
     * Logs a client-side IDE event. Also the current user, workspace and project's information will be logged.
     *
     * @param action
     *         any class to log information from
     * @param actionName
     *         the action name, the name is limited by {@link #MAX_PARAM_VALUE_LENGTH} characters
     * @param additionalParams
     *         any additional parameters to log, not more than {@link #MAX_PARAMS_NUMBER}, every parameter name and its
     *         value are limited by {@link #MAX_PARAM_NAME_LENGTH} and {@link #MAX_PARAM_VALUE_LENGTH} characters
     *         correspondingly
     */
    void log(Object action, String actionName, Map<String, String> additionalParams);

    /**
     * Logs a client-side IDE event.
     *
     * @see #log(Object, String, java.util.Map)
     */
    void log(Object action, String actionName);

    /**
     * Logs a client-side IDE event.
     *
     * @see #log(Object, String, java.util.Map)
     */
    void log(Object action);


}

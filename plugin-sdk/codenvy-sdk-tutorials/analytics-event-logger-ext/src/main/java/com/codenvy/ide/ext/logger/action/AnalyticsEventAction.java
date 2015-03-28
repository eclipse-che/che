/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.logger.action;

/**
 * As usual, importing resources, related to Action API.
 * The 3rd import is required to call a default alert box.
 */

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.codenvy.ide.ext.logger.AnalyticsEventLoggerExtension;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsEventAction extends Action {

    private final AnalyticsEventLogger eventLogger;

    /**
     * Define a constructor and pass over text to be displayed in the dialogue box.
     */
    @Inject
    public AnalyticsEventAction(AnalyticsEventLogger eventLogger) {
        super("Analytics Event");
        this.eventLogger = eventLogger;
    }

    /**
     * Define the action required when calling this method. In our case it'll log an event for the Analytics system.
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        Window.alert("Analytics event will be logged");
        logAnalyticsEvent();
    }

    private void logAnalyticsEvent() {
        /**
         * Preparing additional parameters, must meet the requirements described in javadoc for
         * {@link com.codenvy.ide.api.logger.AnalyticsEventLogger#log(Class, String, java.util.Map)}
         */
        Map<String, String> additionalParameters = new HashMap<>();
        additionalParameters.put("Plugin author", "Codenvy");

        /**
         * Logging the "Analytics Event Action performed" event. If plugin integrated into Codenvy IDE then event will
         * be immediately transferred to Analytics system, otherwise it will be printed on a browser console.
         */
        eventLogger.log(AnalyticsEventLoggerExtension.class, "Analytics Event Action performed", additionalParameters);
    }
}

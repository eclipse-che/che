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
package org.eclipse.che.wsagent.server;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Singleton
public class WsAgentAnalyticsAddresser {

    private static final Logger LOG = LoggerFactory.getLogger(WsAgentAnalyticsAddresser.class);

    public static final String ID = NameGenerator.generate("CHA", 10);

    @ScheduleRate(period = 1, unit = TimeUnit.HOURS)
    void send() {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL("https://install.codenvycorp.com/che/init/workspace?id=" + ID);
            connection = (HttpsURLConnection)url.openConnection();
            connection.getResponseCode();
        } catch (Exception e) {
            LOG.debug("Failed to send agent analytics", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

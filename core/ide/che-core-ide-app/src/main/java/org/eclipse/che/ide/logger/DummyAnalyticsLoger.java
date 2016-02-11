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
package org.eclipse.che.ide.logger;

import java.util.Map;

/**
 * Dummy implementation for AnalyticsEventLogger - do nothing
 *
 * @author Vitalii Parfonov
 */
public class DummyAnalyticsLoger implements AnalyticsEventLoggerExt {


    @Override
    public void logEvent(String event, Map<String, String> additionalParams) {
        //do nothing
    }

    @Override
    public void log(Object action, String actionName, Map<String, String> additionalParams) {
        //do nothing
    }

    @Override
    public void log(Object action, String actionName) {
        //do nothing
    }

    @Override
    public void log(Object action) {
        //do nothing
    }
}

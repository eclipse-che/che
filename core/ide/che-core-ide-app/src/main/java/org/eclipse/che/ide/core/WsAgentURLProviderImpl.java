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
package org.eclipse.che.ide.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.gwt.client.WsAgentUrlProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class WsAgentURLProviderImpl implements WsAgentUrlProvider {

    private final AppContext appContext;

    @Inject
    WsAgentURLProviderImpl(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String get() {
        String wsAgentURL = appContext.getWsAgentURL();

        if (wsAgentURL == null) {
            String errorMessage = "Ws agent URL can not be null";
            Log.error(getClass(), errorMessage);
            throw new IllegalArgumentException(getClass() + errorMessage);
        }

        return wsAgentURL;
    }
}

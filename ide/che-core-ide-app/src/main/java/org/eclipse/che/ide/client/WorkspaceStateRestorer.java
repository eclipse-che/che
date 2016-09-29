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
package org.eclipse.che.ide.client;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.statepersistance.AppStateManager;

/**
 * Restore workspace state, like opened files.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class WorkspaceStateRestorer implements WsAgentComponent {

    private final Provider<AppStateManager> managerProvider;
    private final Provider<AppContext> appContextProvider;

    @Inject
    public WorkspaceStateRestorer(Provider<AppStateManager> managerProvider, Provider<AppContext> appContextProvider) {
        this.managerProvider = managerProvider;
        this.appContextProvider = appContextProvider;
    }

    @Override
    public void start(Callback<WsAgentComponent, Exception> callback) {
        managerProvider.get().restoreWorkspaceState(appContextProvider.get().getWorkspaceId());
        callback.onSuccess(this);
    }
}

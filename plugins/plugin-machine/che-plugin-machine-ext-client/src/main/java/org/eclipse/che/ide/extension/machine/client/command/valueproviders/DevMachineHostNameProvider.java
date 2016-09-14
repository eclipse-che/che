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
package org.eclipse.che.ide.extension.machine.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;

import javax.validation.constraints.NotNull;

/**
 * Provides dev-machine's host name.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DevMachineHostNameProvider implements CommandPropertyValueProvider, WsAgentStateHandler {

    private static final String KEY = "${machine.dev.hostname}";

    private final AppContext           appContext;

    private String value;

    @Inject
    public DevMachineHostNameProvider(EventBus eventBus, AppContext appContext) {
        this.appContext = appContext;
        this.value = "";
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @NotNull
    @Override
    public String getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Promise<String> getValue() {
        return Promises.resolve(value);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        String hostName = appContext.getDevMachine().getRuntimeProperties().get("config.hostname");
        if (hostName != null) {
            value = hostName;
        }
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        value = "";
    }
}

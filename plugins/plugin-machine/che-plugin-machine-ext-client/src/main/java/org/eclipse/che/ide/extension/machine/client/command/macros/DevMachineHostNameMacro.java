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
package org.eclipse.che.ide.extension.machine.client.command.macros;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * Provides dev-machine's host name.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DevMachineHostNameMacro implements Macro, WsAgentStateHandler {

    private static final String KEY = "${machine.dev.hostname}";

    private final AppContext                  appContext;
    private final MachineLocalizationConstant localizationConstants;

    private String value;

    @Inject
    public DevMachineHostNameMacro(EventBus eventBus, AppContext appContext, MachineLocalizationConstant localizationConstants) {
        this.appContext = appContext;
        this.localizationConstants = localizationConstants;
        this.value = "";
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @NotNull
    @Override
    public String getName() {
        return KEY;
    }

    @Override
    public String getDescription() {
        return localizationConstants.macroMachineDevHostnameDescription();
    }

    @NotNull
    @Override
    public Promise<String> expand() {
        return Promises.resolve(value);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        String hostName = appContext.getDevMachine().getProperties().get("config.hostname");
        if (hostName != null) {
            value = hostName;
        }
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        value = "";
    }
}

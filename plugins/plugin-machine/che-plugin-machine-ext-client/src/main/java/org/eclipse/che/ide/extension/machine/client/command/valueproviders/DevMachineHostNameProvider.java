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

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;

import javax.validation.constraints.NotNull;

/**
 * Provides dev-machine's host name.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DevMachineHostNameProvider implements CommandPropertyValueProvider, MachineStateEvent.Handler {

    private static final String KEY = "${machine.dev.hostname}";

    private final AppContext           appContext;
    private final MachineServiceClient machineServiceClient;

    private String value;

    @Inject
    public DevMachineHostNameProvider(EventBus eventBus, AppContext appContext, MachineServiceClient machineServiceClient) {
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.value = "";

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        updateValue();
    }

    @NotNull
    @Override
    public String getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        final MachineDto machine = event.getMachine();
        if (machine.getConfig().isDev()) {
            machineServiceClient.getMachine(machine.getId()).then(new Operation<MachineDto>() {
                @Override
                public void apply(MachineDto machine) throws OperationException {
                    final String hostName = machine.getRuntime().getProperties().get("config.hostname");

                    if (hostName != null) {
                        value = hostName;
                    }
                }
            });
        }
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        final MachineDto machine = event.getMachine();
        if (machine.getConfig().isDev()) {
            value = "";
        }
    }

    private void updateValue() {
        final String devMachineId = appContext.getDevMachineId();
        if (devMachineId == null) {
            return;
        }

        machineServiceClient.getMachine(devMachineId).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto arg) throws OperationException {
                final String hostName = arg.getRuntime().getProperties().get("config.hostname");
                if (hostName != null) {
                    value = hostName;
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                value = "";
            }
        });
    }

}

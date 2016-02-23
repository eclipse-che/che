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

import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedEvent;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedHandler;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStoppedEvent;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStoppedHandler;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.transformEntries;

/**
 * Provide mapping internal port, i.e. ${server.port.8080} to 127.0.0.1:21212.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class ServerPortProvider implements WorkspaceStartedHandler, WorkspaceStoppedHandler {

    public static final String KEY_TEMPLATE = "${server.port.%}";

    private final MachineServiceClient                 machineServiceClient;
    private final CommandPropertyValueProviderRegistry commandPropertyRegistry;
    private final AppContext                           appContext;

    private Collection<CommandPropertyValueProvider> providers;

    private final Operation<MachineDto> registerProviders = new Operation<MachineDto>() {
        @Override
        public void apply(MachineDto machine) throws OperationException {
            providers = getProviders(machine);
            commandPropertyRegistry.register(Sets.newHashSet(providers));
        }
    };

    @Inject
    public ServerPortProvider(EventBus eventBus,
                              MachineServiceClient machineServiceClient,
                              CommandPropertyValueProviderRegistry commandPropertyRegistry,
                              AppContext appContext) {
        this.machineServiceClient = machineServiceClient;
        this.commandPropertyRegistry = commandPropertyRegistry;
        this.appContext = appContext;

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);

        registerProviders();
    }

    private void registerProviders() {
        String devMachineId = appContext.getDevMachineId();
        if (devMachineId != null) {
            machineServiceClient.getMachine(devMachineId).then(registerProviders);
        }
    }

    private Collection<CommandPropertyValueProvider> getProviders(MachineDto machine) {
        EntryTransformer<String, ServerDto, CommandPropertyValueProvider> machineToProvider =
                new EntryTransformer<String, ServerDto, CommandPropertyValueProvider>() {
                    @Override
                    public CommandPropertyValueProvider transformEntry(String internalPort, ServerDto serverConfiguration) {
                        return new AddressProvider(internalPort, serverConfiguration.getAddress());
                    }
                };

        Map<String, CommandPropertyValueProvider> providers = transformEntries(machine.getRuntime().getServers(), machineToProvider);
        Map<String, CommandPropertyValueProvider> providersWithNoTcpAliases = new HashMap<>();
        for (Map.Entry<String, CommandPropertyValueProvider> providerEntry : providers.entrySet()) {
            if (providerEntry.getKey().endsWith("/tcp")) {
                providersWithNoTcpAliases.put(providerEntry.getKey().substring(0, providerEntry.getKey().length() - 4),
                                              providerEntry.getValue());
            }
        }
        providersWithNoTcpAliases.putAll(providers);

        return providersWithNoTcpAliases.values();
    }

    @Override
    public void onWorkspaceStarted(UsersWorkspaceDto workspace) {
        registerProviders();
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        for (CommandPropertyValueProvider provider : providers) {
            commandPropertyRegistry.unregister(provider);
        }

        providers.clear();
    }

    private class AddressProvider implements CommandPropertyValueProvider {

        String variable;
        String address;

        AddressProvider(String internalPort, String address) {
            this.variable = KEY_TEMPLATE.replaceAll("%", internalPort);
            this.address = address;
        }

        @Override
        public String getKey() {
            return variable;
        }

        @Override
        public String getValue() {
            return address;
        }
    }
}

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
package org.eclipse.che.ide.machine.macro;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.machine.CustomCommandPropertyValueProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ServerProtocolMacroProvider}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ServerProtocolMacroProviderTest {

    public static final String WS_AGENT_PORT = Constants.WS_AGENT_PORT; // 4401/tcp
    public static final String PORT          = "1234";
    public static final String ADDRESS       = "127.0.0.1" + ":" + PORT;
    public static final String PROTOCOL      = "protocol";

    @Mock
    private CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry;

    @Mock
    private EventBus eventBus;

    @Mock
    private AppContext appContext;

    @Mock
    private DevMachine devMachine;

    @Mock
    private Machine machine;

    @Mock
    private MachineRuntimeInfo machineRuntimeInfo;

    @Mock
    private Server server;

    private ServerProtocolMacroProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new ServerProtocolMacroProvider(commandPropertyValueProviderRegistry, eventBus, appContext);

        registerProvider();
    }

    @Test
    public void getMacroProviders() throws Exception {
        final Set<CommandPropertyValueProvider> providers = provider.getMacroProviders(devMachine);

        assertEquals(providers.size(), 2);

        final Iterator<CommandPropertyValueProvider> iterator = providers.iterator();

        final CommandPropertyValueProvider provider1 = iterator.next();

        assertTrue(provider1 instanceof CustomCommandPropertyValueProvider);
        assertEquals(provider1.getKey(), ServerProtocolMacroProvider.KEY.replace("%", WS_AGENT_PORT));

        provider1.getValue().then(new Operation<String>() {
            @Override
            public void apply(String address) throws OperationException {
                assertEquals(address, PROTOCOL);
            }
        });

        final CommandPropertyValueProvider provider2 = iterator.next();

        assertTrue(provider2 instanceof CustomCommandPropertyValueProvider);
        assertEquals(provider2.getKey(), ServerProtocolMacroProvider.KEY.replace("%", WS_AGENT_PORT.substring(0, WS_AGENT_PORT.length() - 4)));

        provider2.getValue().then(new Operation<String>() {
            @Override
            public void apply(String address) throws OperationException {
                assertEquals(address, PROTOCOL);
            }
        });
    }

    protected void registerProvider() {
        when(devMachine.getDescriptor()).thenReturn(machine);
        when(machine.getRuntime()).thenReturn(machineRuntimeInfo);
        doReturn(Collections.<String, Server>singletonMap(WS_AGENT_PORT, server)).when(machineRuntimeInfo).getServers();
        when(server.getAddress()).thenReturn(ADDRESS);
        when(server.getProtocol()).thenReturn(PROTOCOL);
    }

}
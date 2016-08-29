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

import com.google.common.annotations.Beta;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base macro provider which belongs to the current server configuration. Provides easy access to the developer machine
 * to allow fetch necessary information to use in custom commands, preview urls, etc.
 *
 * @author Vlad Zhukovskyi
 * @see CommandPropertyValueProviderRegistry
 * @see CommandPropertyValueProvider
 * @see ServerHostNameMacroProvider
 * @see ServerMacroProvider
 * @see ServerPortMacroProvider
 * @see ServerProtocolMacroProvider
 * @since 4.7.0
 */
@Beta
public abstract class AbstractServerMacroProvider implements WsAgentStateHandler {

    private final CommandPropertyValueProviderRegistry providerRegistry;
    private final AppContext                           appContext;

    public AbstractServerMacroProvider(CommandPropertyValueProviderRegistry providerRegistry,
                                       EventBus eventBus,
                                       AppContext appContext) {
        this.providerRegistry = providerRegistry;
        this.appContext = appContext;

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    /**
     * Register macro providers which returns the implementation.
     *
     * @see AbstractServerMacroProvider#getMacroProviders(DevMachine)
     * @since 4.7.0
     */
    private void registerProviders() {
        final DevMachine devMachine = appContext.getDevMachine();

        if (devMachine == null) {
            return;
        }

        final Set<CommandPropertyValueProvider> providers = getMacroProviders(devMachine);
        checkNotNull(providers);

        if (providers.isEmpty()) {
            return;
        }

        providerRegistry.register(providers);
    }

    /**
     * Unregister macro providers which the implementation returns.
     *
     * @see AbstractServerMacroProvider#getMacroProviders(DevMachine)
     * @since 4.7.0
     */
    private void unregisterProviders() {
        final DevMachine devMachine = appContext.getDevMachine();

        if (devMachine == null) {
            return;
        }

        for (CommandPropertyValueProvider provider : getMacroProviders(devMachine)) {
            providerRegistry.unregister(provider);
        }
    }

    /**
     * Returns the macros which implementation provides based on the information from the developer machine.
     *
     * @param devMachine
     *         current developer machine
     * @return set of unique macro providers
     * @see DevMachine
     * @see CommandPropertyValueProvider
     * @since 4.7.0
     */
    public abstract Set<CommandPropertyValueProvider> getMacroProviders(DevMachine devMachine);

    /** {@inheritDoc} */
    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        registerProviders();
    }

    /** {@inheritDoc} */
    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        unregisterProviders();
    }
}

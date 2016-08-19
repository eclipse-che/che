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
package org.eclipse.che.ide.machine;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for {@link CommandPropertyValueProviderRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandPropertyValueProviderRegistryImpl implements CommandPropertyValueProviderRegistry {

    private final Map<String, CommandPropertyValueProvider> valueProviders;

    public CommandPropertyValueProviderRegistryImpl() {
        this.valueProviders = new HashMap<>();
    }

    @Inject(optional = true)
    public void register(Set<CommandPropertyValueProvider> valueProviders) {
        for (CommandPropertyValueProvider provider : valueProviders) {
            final String key = provider.getKey();
            if (this.valueProviders.containsKey(key)) {
                Log.warn(CommandPropertyValueProviderRegistryImpl.class, "Value provider for key " + key + " is already registered.");
            } else {
                this.valueProviders.put(key, provider);
            }
        }
    }

    @Override
    public void unregister(CommandPropertyValueProvider valueProvider) {
        valueProviders.remove(valueProvider.getKey());
    }

    @Override
    public CommandPropertyValueProvider getProvider(String key) {
        return valueProviders.get(key);
    }

    @Override
    public List<CommandPropertyValueProvider> getProviders() {
        return new ArrayList<>(valueProviders.values());
    }

    @Override
    public Set<String> getKeys() {
        return valueProviders.keySet();
    }
}

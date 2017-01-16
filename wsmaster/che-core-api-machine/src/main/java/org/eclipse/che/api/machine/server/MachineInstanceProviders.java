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
package org.eclipse.che.api.machine.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides machines {@link InstanceProvider} implementation by machine type
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineInstanceProviders {
    private final Map<String, InstanceProvider> instanceProviders;

    @Inject
    public MachineInstanceProviders(Set<InstanceProvider> instanceProviders) {
        this.instanceProviders = new HashMap<>(instanceProviders.size());
        for (InstanceProvider provider : instanceProviders) {
            this.instanceProviders.put(provider.getType(), provider);
        }
    }

    /**
     * Returns {@link InstanceProvider} implementation by machine type
     *
     * @param machineType type of machine implementation
     * @return implementation of the machine {code InstanceProvider}
     * @throws NotFoundException if no implementation found for provided machine type
     */
    public InstanceProvider getProvider(String machineType) throws NotFoundException {
        if (hasProvider(machineType)) {
            return instanceProviders.get(machineType);
        }
        throw new NotFoundException(String.format("Can't find machine provider for unsupported machine type '%s'", machineType));
    }
    
    /**
     * Checks if an {@link InstanceProvider} implementation of the given machine type exists
     *
     * @param machineType type of machine implementation
     * @return <code>true</code> if such implementation exists, <code>false</code> otherwise
     */
    public boolean hasProvider(String machineType) {
        return instanceProviders.containsKey(machineType);
    }
    
    /**
     * Returns the machine types of all available {@link InstanceProvider} implementations.
     *
     * @return a collection of machine types
     */
    public Collection<String> getProviderTypes() {
        return Collections.unmodifiableSet(instanceProviders.keySet());
    }
}

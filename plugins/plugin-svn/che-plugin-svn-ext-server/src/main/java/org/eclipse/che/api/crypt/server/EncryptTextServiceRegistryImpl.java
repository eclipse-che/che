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
package org.eclipse.che.api.crypt.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

public class EncryptTextServiceRegistryImpl implements EncryptTextServiceRegistry {

    /**
     * The encryption services.
     */
    private final Map<Integer, EncryptTextService> registry;

    /**
     * Caches the result of the preferred implementation search.
     */
    private EncryptTextService cachedPreferredImpl;

    /**
     * The ordered preferences for text crypto service implementation.
     */
    private final List<Integer> cryptoPreferences;

    @Inject
    public EncryptTextServiceRegistryImpl(final Set<EncryptTextService> encryptTextServices,
                                          final @Named(EncryptTextModule.SCHEME_PREFERENCE_ORDER_NAME) List<Integer> cryptoPreferences) {
        // A reasonable initial capacity is (number of data) / 0.75 + 1
        // And three values are put in the map
        this.registry = new HashMap(5);

        for (final EncryptTextService service : encryptTextServices) {
            register(service.getSchemeVersion(), service);
        }
        this.cryptoPreferences = cryptoPreferences;
    }

    @Override
    public void register(final int schemeVersion, final EncryptTextService service) {
        if (this.registry.get(schemeVersion) != null) {
            throw new RuntimeException("Multiple encryption schemes registered with the same version");
        }
        this.registry.put(schemeVersion, service);
    }

    @Override
    @Nullable
    public EncryptTextService getService(final int schemeVersion) {
        return this.registry.get(schemeVersion);
    }

    @Override
    public EncryptTextService getPreferredService() {
        if (this.cachedPreferredImpl != null) {
            return this.cachedPreferredImpl;
        }
        for (final Integer schemeVersion : this.cryptoPreferences) {
            final EncryptTextService encrypt = this.registry.get(schemeVersion);
            if (encrypt.isActive()) {
                this.cachedPreferredImpl = encrypt;
                return encrypt;
            }
        }
        throw new RuntimeException("no encrypt text service available");
    }

}

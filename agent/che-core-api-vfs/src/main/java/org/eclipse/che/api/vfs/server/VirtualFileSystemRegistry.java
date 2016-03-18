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
package org.eclipse.che.api.vfs.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for virtual file system providers.
 *
 * @author andrew00x
 * @see VirtualFileSystemFactory
 */
@Singleton
public class VirtualFileSystemRegistry {
    private final ConcurrentMap<String, VirtualFileSystemProvider> providers = new ConcurrentHashMap<>();

    public void registerProvider(String vfsId, VirtualFileSystemProvider provider) throws ServerException {
        if (providers.putIfAbsent(id(vfsId), provider) != null) {
            throw new ServerException(String.format("Virtual file system %s already registered. ", vfsId));
        }
    }

    public void unregisterProvider(String vfsId) throws ServerException {
        final VirtualFileSystemProvider removed = providers.remove(id(vfsId));
        if (removed != null) {
            removed.close();
        }
    }

    public VirtualFileSystemProvider getProvider(String vfsId) throws ServerException, NotFoundException {
        String myId = id(vfsId);
        VirtualFileSystemProvider provider = providers.get(myId);
        if (provider == null) {
            VirtualFileSystemProvider newProvider = loadProvider(myId);
            if (newProvider != null) {
                provider = providers.putIfAbsent(myId, newProvider);
                if (provider == null) {
                    provider = newProvider;
                }
            } else {
                throw new ServerException(String.format("Virtual file system %s does not exist.  This is a serious error and likely occurs in an on premises configuration.  " +
                                                        "Contact support for assistance. ", vfsId));
            }
        }
        return provider;
    }

    protected VirtualFileSystemProvider loadProvider(String vfsId) throws ServerException, NotFoundException {
        return null;
    }

    public Collection<VirtualFileSystemProvider> getRegisteredProviders() throws ServerException {
        return Collections.unmodifiableCollection(providers.values());
    }

    private String id(String vfsId) {
        return vfsId == null ? "default" : vfsId;
    }
}

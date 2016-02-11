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
package org.eclipse.che.vfs.impl.fs;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;

import javax.inject.Singleton;

/**
 * Throws {@link NotFoundException} when {@link LocalFileSystemProvider} was not found for given {@code vfsId}.
 * Providers for local infrastructure are registered by {@link LocalFileSystemRegistryPlugin}.
 *
 * @see VirtualFileSystemRegistry
 * @see LocalFileSystemProvider
 * @see LocalFileSystemRegistryPlugin
 *
 * @author Eugene Voevodin
 */
@Singleton
public class LocalVirtualFileSystemRegistry extends VirtualFileSystemRegistry {

    @Override
    protected VirtualFileSystemProvider loadProvider(String vfsId) throws ServerException, NotFoundException {
        throw new NotFoundException("Workspace " + vfsId + " was not found");
    }
}


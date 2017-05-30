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
package org.eclipse.che.api.vfs;

import org.eclipse.che.api.core.ServerException;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractVirtualFileSystemProvider implements VirtualFileSystemProvider {
    protected final AtomicReference<VirtualFileSystem> fileSystemReference = new AtomicReference<>();

    @Override
    public VirtualFileSystem getVirtualFileSystem(boolean create) throws ServerException {
        VirtualFileSystem fileSystem = fileSystemReference.get();
        if (fileSystem == null && create) {
            VirtualFileSystem newFileSystem = createVirtualFileSystem(() -> fileSystemReference.set(null));
            fileSystemReference.compareAndSet(null, newFileSystem);
            fileSystem = fileSystemReference.get();
        }
        return fileSystem;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() throws ServerException {
        return getVirtualFileSystem(true);
    }

    protected abstract VirtualFileSystem createVirtualFileSystem(CloseCallback closeCallback) throws ServerException;

    @Override
    public void close() throws ServerException {
        VirtualFileSystem virtualFileSystem = fileSystemReference.get();
        if (virtualFileSystem != null) {
            virtualFileSystem.close();
        }
        fileSystemReference.set(null);
    }

    public interface CloseCallback {
        void onClose();
    }
}

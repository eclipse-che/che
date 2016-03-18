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
package org.eclipse.che.api.project.server.watcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FilesBuffer;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.File;
import java.io.IOException;

/**
 * @author Dmitry Shnurenko
 */
@Path("/watcher")
@Singleton
public class WatcherService {

    @Inject
    private VirtualFileSystemRegistry virtualFileSystemRegistry;

    @GET
    @Path("/{workspaceId}/register")
    public void registerWatcher(@PathParam("workspaceId") String workspaceId) throws ServerException, NotFoundException, IOException {
        VirtualFileSystem virtualFileSystem = virtualFileSystemRegistry.getProvider(workspaceId).newInstance(null);

        MountPoint mountPoint = virtualFileSystem.getMountPoint();

        VirtualFile root = mountPoint.getRoot();

        File ioFile = root.getIoFile();

        final Watcher watcher = new Watcher(ioFile.toPath(), FilesBuffer.get(), true);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                watcher.processEvents();
            }
        };
        Thread watcherThread = new Thread(runnable, "Thread of file system listener");

        watcherThread.setDaemon(true);

        watcherThread.start();
    }
}

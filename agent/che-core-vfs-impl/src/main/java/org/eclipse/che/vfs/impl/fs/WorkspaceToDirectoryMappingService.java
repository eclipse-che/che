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

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrew00x
 */
@Path("vfs-directory-mapping")
public class WorkspaceToDirectoryMappingService {
    @Inject
    private MappedDirectoryLocalFSMountStrategy mappedDirectoryLocalFSMountStrategy;

    @Inject
    private VirtualFileSystemRegistry virtualFileSystemRegistry;

    @Inject
    @Named("che.user.workspaces.storage")
    String rootDir;

    @POST
    @Path("{ws-id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> setMountPath(@PathParam("ws-id") String workspaceId, @QueryParam("mountPath") String mountPath)
            throws ServerException, IOException, NotFoundException {
//        if (Files.notExists(Paths.get(mountPath))) {
//            Files.createDirectories(Paths.get(mountPath));
//        }
//
//        VirtualFileSystemProvider provider = virtualFileSystemRegistry.getProvider(workspaceId);
//        provider.close();
        mappedDirectoryLocalFSMountStrategy.setMountPath(workspaceId, new File(mountPath));
        return getDirectoryMapping();
    }

    @DELETE
    @Path("{ws-id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> removeMountPath(@PathParam("ws-id") String workspaceId) throws ServerException, NotFoundException {
//        VirtualFileSystemProvider provider = virtualFileSystemRegistry.getProvider(workspaceId);
//        provider.close();
        mappedDirectoryLocalFSMountStrategy.removeMountPath(workspaceId);
        return getDirectoryMapping();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getDirectoryMapping() throws ServerException {
        Map<String, String> directoryMapping;
        if (mappedDirectoryLocalFSMountStrategy.getDirectoryMapping().isEmpty()) {
            directoryMapping = new HashMap<>();
            directoryMapping.put("__default", rootDir);
            return directoryMapping;
        }
        directoryMapping =
                Maps.transformValues(mappedDirectoryLocalFSMountStrategy.getDirectoryMapping(), new Function<File, String>() {
                    @Override
                    public String apply(File input) {
                        return input.getAbsolutePath();
                    }
                });

        return directoryMapping;
    }
}

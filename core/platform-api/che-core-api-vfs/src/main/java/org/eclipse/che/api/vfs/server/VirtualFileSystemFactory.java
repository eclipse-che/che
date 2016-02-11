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
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides access to virtual file systems which have registered providers in VirtualFileSystemRegistry.
 *
 * @author andrew00x
 */
@Path("vfs/{ws-id}")
public class VirtualFileSystemFactory {
    @Inject
    private VirtualFileSystemRegistry registry;
    @Inject
    @Nullable
    private RequestValidator          requestValidator;
    @Context
    private HttpServletRequest        request;
    @Context
    private UriInfo                   uriInfo;
    @PathParam("ws-id")
    private String                    vfsId;

    @Path("v2")
    public VirtualFileSystem getFileSystem() throws ServerException, NotFoundException {
        validateRequest();
        //final String vfsId = (String)EnvironmentContext.getCurrent().getVariable(EnvironmentContext.WORKSPACE_ID);
        VirtualFileSystemProvider provider = registry.getProvider(vfsId);
        return provider.newInstance(uriInfo.getBaseUri());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<VirtualFileSystemInfo> getAvailableFileSystems() throws ServerException {
        validateRequest();
        final Collection<VirtualFileSystemProvider> vfsProviders = registry.getRegisteredProviders();
        final List<VirtualFileSystemInfo> result = new ArrayList<>(vfsProviders.size());
        final URI baseUri = uriInfo.getBaseUri();
        for (VirtualFileSystemProvider p : vfsProviders) {
            VirtualFileSystem fs = p.newInstance(baseUri);
            result.add(fs.getInfo());
        }
        return result;
    }

    private void validateRequest() {
        if (requestValidator != null) {
            requestValidator.validate(request);
        }
    }
}

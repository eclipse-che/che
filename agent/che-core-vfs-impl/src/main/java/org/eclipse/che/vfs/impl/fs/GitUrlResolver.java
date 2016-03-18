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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.nio.file.Paths;

/**
 * Resolves Git URL for public access.
 *
 * @author Vitaly Parfonov
 */
@Singleton
public class GitUrlResolver {
    private final LocalPathResolver pathResolver;
    private final String            mountPath;
    private final String            gitServerUriPrefix;

    @Inject
    public GitUrlResolver(@Named("che.user.workspaces.storage") java.io.File mountRoot, @Named("git.server.uri.prefix") String gitServerUriPrefix, LocalPathResolver pathResolver) {
        this.mountPath = mountRoot.getAbsolutePath();
        this.pathResolver = pathResolver;
        this.gitServerUriPrefix = gitServerUriPrefix;
    }

    public String resolve(UriInfo uriInfo, VirtualFileSystem vfs, String path)
            throws ServerException, NotFoundException, ForbiddenException {

        return resolve(uriInfo.getBaseUri(), ((FSMountPoint)vfs.getMountPoint()).getVirtualFile(path));
    }

    public String resolve(UriInfo uriInfo, VirtualFileImpl virtualFile) {
        return resolve(uriInfo.getBaseUri(), virtualFile);
    }

    public String resolve(URI baseUri, VirtualFileImpl virtualFile) {
        final String localPath = pathResolver.resolve(virtualFile);

        URI uriLocalPath = Paths.get(localPath).toUri();
        String localPathNormalized = uriLocalPath.getPath();

        URI uriMountPath = Paths.get(mountPath).toUri();
        String mountPathNormalized = uriMountPath.getPath();

        StringBuilder result = new StringBuilder();
        result.append(baseUri.getScheme());
        result.append("://");
        result.append(baseUri.getHost());
        int port = baseUri.getPort();
        if (port != 80 && port != 443 && port != -1) {
            result.append(':');
            result.append(port);
        }
        result.append('/');
        result.append(gitServerUriPrefix);
        result.append(localPathNormalized.substring(mountPathNormalized.length() - 1));

        int lastSymbol = result.length() - 1;
        if (result.lastIndexOf("/")  == lastSymbol) {
            result.deleteCharAt(lastSymbol);
        }

        return result.toString();
    }
}

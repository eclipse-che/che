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
package org.eclipse.che.api.git;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Paths;

/**
 * Resolves Git URL for public access.
 *
 * @author Vitaly Parfonov
 */
@Singleton
public class GitUrlResolver {

    private final String mountPath;
    private final String gitServerUriPrefix;

    @Inject
    public GitUrlResolver(@Named("che.user.workspaces.storage") java.io.File mountRoot,
                          @Named("git.server.uri.prefix") String gitServerUriPrefix) {
        this.mountPath = mountRoot.getAbsolutePath();
        this.gitServerUriPrefix = gitServerUriPrefix;
    }

    public String resolve(URI baseUri, String absolutePath) {
        URI uriLocalPath = Paths.get(absolutePath).toUri();
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
        if (result.lastIndexOf("/") == lastSymbol) {
            result.deleteCharAt(lastSymbol);
        }

        return result.toString();
    }
}

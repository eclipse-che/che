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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileSystemImpl;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.util.LinksHelper;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.ACLCapability;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.QueryCapability;
import org.eclipse.che.dto.server.DtoFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of VirtualFileSystem for local filesystem.
 *
 * @author andrew00x
 */
public class LocalFileSystem extends VirtualFileSystemImpl {
    final String vfsId;
    final URI    baseUri;

    public LocalFileSystem(String vfsId,
                           URI baseUri,
                           VirtualFileSystemUserContext userContext,
                           FSMountPoint mountPoint,
                           SearcherProvider searcherProvider,
                           VirtualFileSystemRegistry vfsRegistry) {
        super(vfsId, baseUri, userContext, mountPoint, searcherProvider, vfsRegistry);
        this.vfsId = vfsId;
        this.baseUri = baseUri;
    }

    @Override
    public VirtualFileSystemInfo getInfo() throws ServerException {
        final BasicPermissions[] basicPermissions = BasicPermissions.values();
        final List<String> permissions = new ArrayList<>(basicPermissions.length);
        for (BasicPermissions bp : basicPermissions) {
            permissions.add(bp.value());
        }
        return DtoFactory.getInstance().createDto(VirtualFileSystemInfo.class)
                         .withId(vfsId)
                         .withVersioningSupported(false)
                         .withLockSupported(true)
                         .withAnonymousPrincipal(VirtualFileSystemInfo.ANONYMOUS_PRINCIPAL)
                         .withAnyPrincipal(VirtualFileSystemInfo.ANY_PRINCIPAL)
                         .withPermissions(permissions)
                         .withAclCapability(ACLCapability.MANAGE)
                         .withQueryCapability(searcherProvider == null ? QueryCapability.NONE : QueryCapability.FULLTEXT)
                         .withUrlTemplates(LinksHelper.createUrlTemplates(baseUri, vfsId))
                         .withRoot((Folder)fromVirtualFile(getMountPoint().getRoot(), true, PropertyFilter.ALL_FILTER));
    }
}

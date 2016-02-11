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
package org.eclipse.che.api.vfs.server.impl.memory;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.VirtualFileSystemImpl;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
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

/** @author andrew00x */
public class MemoryFileSystem extends VirtualFileSystemImpl {
    private final String vfsId;
    private final URI    baseUri;

    public MemoryFileSystem(URI baseUri,
                            String vfsId,
                            VirtualFileSystemUserContext userContext,
                            MemoryMountPoint memoryMountPoint,
                            SearcherProvider searcherProvider,
                            VirtualFileSystemRegistry vfsRegistry) {
        super(vfsId, baseUri, userContext, memoryMountPoint, searcherProvider, vfsRegistry);
        this.baseUri = baseUri;
        this.vfsId = vfsId;
    }

    @Override
    public VirtualFileSystemInfo getInfo() throws ServerException {
        final BasicPermissions[] basicPermissions = BasicPermissions.values();
        final List<String> permissions = new ArrayList<>(basicPermissions.length);
        for (BasicPermissions bp : basicPermissions) {
            permissions.add(bp.value());
        }
        final Folder root = (Folder)fromVirtualFile(getMountPoint().getRoot(), true, PropertyFilter.ALL_FILTER);
        return DtoFactory.getInstance().createDto(VirtualFileSystemInfo.class)
                         .withId(vfsId)
                         .withVersioningSupported(false)
                         .withLockSupported(true)
                         .withAnonymousPrincipal(VirtualFileSystemInfo.ANONYMOUS_PRINCIPAL)
                         .withAnyPrincipal(VirtualFileSystemInfo.ANY_PRINCIPAL)
                         .withPermissions(permissions)
                         .withAclCapability(ACLCapability.MANAGE)
                         .withQueryCapability(QueryCapability.FULLTEXT)
                         .withUrlTemplates(LinksHelper.createUrlTemplates(baseUri, vfsId))
                         .withRoot(root);
    }
}

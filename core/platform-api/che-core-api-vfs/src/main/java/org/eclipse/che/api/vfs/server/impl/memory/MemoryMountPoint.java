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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.vfs.server.LazyIterator;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.VirtualFileVisitor;

import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of MountPoint.
 * <p/>
 * NOTE: This implementation is not thread safe.
 *
 * @author andrew00x
 */
public class MemoryMountPoint implements MountPoint {
    private final String                       workspaceId;
    private final EventService                 eventService;
    private final SearcherProvider             searcherProvider;
    private final VirtualFileSystemUserContext userContext;
    private final Map<String, VirtualFile>     entries;
    private final VirtualFile                  root;
    private final SystemPathsFilter            systemFilter;

    public MemoryMountPoint(String workspaceId, EventService eventService, SearcherProvider searcherProvider,
                            VirtualFileSystemUserContext userContext, SystemPathsFilter systemFilter) {
        this.workspaceId = workspaceId;
        this.eventService = eventService;
        this.searcherProvider = searcherProvider;
        this.userContext = userContext;
        entries = new HashMap<>();
        root = new MemoryVirtualFile(this);
        this.systemFilter = systemFilter;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public VirtualFile getRoot() {
        return root;
    }

    public boolean acceptPath(Path path) {
        return systemFilter.accept(workspaceId, path);
    }

    @Override
    public VirtualFile getVirtualFileById(String id) throws NotFoundException, ForbiddenException, ServerException {
        if (id.equals(root.getId())) {
            return getRoot();
        }
        final VirtualFile virtualFile = entries.get(id);
        if (virtualFile == null) {
            throw new NotFoundException(String.format("Object '%s' does not exists. ", id));
        }
        if (!((MemoryVirtualFile)virtualFile).hasPermission(VirtualFileSystemInfo.BasicPermissions.READ.value(), true)) {
            throw new ForbiddenException(String.format("Unable get item '%s'. Operation not permitted. ", id));
        }
        return virtualFile;
    }

    @Override
    public VirtualFile getVirtualFile(String path) throws NotFoundException, ForbiddenException, ServerException {
        if (path == null) {
            throw new IllegalArgumentException("Item path may not be null. ");
        }
        if ("/".equals(path) || path.isEmpty()) {
            return getRoot();
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        VirtualFile virtualFile = getRoot();
        final Path internalPath = Path.fromString(path);
        final String[] elements = internalPath.elements();
        for (int i = 0, length = elements.length; virtualFile != null && i < length; i++) {
            String name = elements[i];
            if (virtualFile.isFolder()) {
                virtualFile = virtualFile.getChild(name);
            }
        }
        if (virtualFile == null) {
            throw new NotFoundException(String.format("Object '%s' does not exists. ", path));
        }

        return virtualFile;
    }

    @Override
    public void reset() {
        entries.clear();
    }

    void putItem(MemoryVirtualFile item) throws ServerException {
        if (item.isFolder()) {
            final Map<String, VirtualFile> flatten = new HashMap<>();
            final ValueHolder<ServerException> errorHolder = new ValueHolder<>();
            item.accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile virtualFile) {
                    try {
                        if (virtualFile.isFolder()) {
                            final LazyIterator<VirtualFile> children = virtualFile.getChildren(VirtualFileFilter.ALL);
                            while (children.hasNext()) {
                                children.next().accept(this);
                            }
                        }
                        flatten.put(virtualFile.getId(), virtualFile);
                    } catch (ServerException e) {
                        errorHolder.set(e);
                    }
                }
            });
            final ServerException error = errorHolder.get();
            if (error != null) {
                throw error;
            }
            entries.putAll(flatten);
        } else {
            entries.put(item.getId(), item);
        }
    }

    void deleteItem(String id) {
        entries.remove(id);
    }

    @Override
    public SearcherProvider getSearcherProvider() {
        return searcherProvider;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    VirtualFileSystemUserContext getUserContext() {
        return userContext;
    }
}

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
package org.eclipse.che.api.vfs.impl.file.event.detectors;

import com.google.common.hash.Hashing;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * Simple registry to keep the list of file that should be tracked by VFS file watching
 * system detector - {@link FileStatusDetector}. For each tracked file stores an MD5
 * hash corresponding to its content.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileTrackingRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(FileTrackingRegistry.class);

    private final Map<String, FileTrackingMetadata> registry = new ConcurrentHashMap<>();

    private final VirtualFileSystemProvider vfsProvider;

    @Inject
    public FileTrackingRegistry(VirtualFileSystemProvider vfsProvider) {
        this.vfsProvider = vfsProvider;
    }

    public void add(String path, String endpoint) {
        final FileTrackingMetadata fileTrackingMetadata = registry.get(path);

        if (fileTrackingMetadata == null) {
            registry.put(path, new FileTrackingMetadata(path, endpoint));
        } else {
            fileTrackingMetadata.addEndpoint(endpoint);
        }
    }

    public void suspend(String endpoint) {
        registry.values()
                .stream()
                .filter(m -> m.getNotSuspendedEndpoints().contains(endpoint))
                .forEach(m -> m.suspend(endpoint));
    }

    public void resume(String endpoint) {
        registry.values()
                .stream()
                .filter(m -> m.getSuspendedEndpoints().contains(endpoint))
                .forEach(m -> m.resume(endpoint));
    }

    public void move(String oldPath, String newPath) {
        final FileTrackingMetadata fileTrackingMetadata = registry.remove(oldPath);

        if (fileTrackingMetadata == null) {
            return;
        }

        registry.put(newPath, fileTrackingMetadata);
    }

    public void copy(String oldPath, String newPath) {
        final FileTrackingMetadata fileTrackingMetadata = registry.get(oldPath);

        if (fileTrackingMetadata == null) {
            return;
        }

        registry.put(newPath, fileTrackingMetadata);
    }

    public void remove(String path, String endpoint) {
        final FileTrackingMetadata fileTrackingMetadata = registry.get(path);

        if (fileTrackingMetadata == null) {
            return;
        }

        fileTrackingMetadata.removeEndpoint(endpoint);

        if (!fileTrackingMetadata.hasEndpoints()) {
            registry.remove(path);
        }
    }

    public void remove(String path) {
        registry.remove(path);
    }

    public String getHashCode(String path) {
        return registry.get(path).getHashCode();
    }

    public boolean updateHash(String path) {
        final String newHash = getHash(path);

        final FileTrackingMetadata fileTrackingMetadata = registry.get(path);
        final String oldHash = fileTrackingMetadata.getHashCode();
        fileTrackingMetadata.setHashCode(newHash);

        return !Objects.equals(oldHash, newHash);
    }

    public boolean contains(String path) {
        return registry.keySet().contains(path);
    }

    public Set<String> getEndpoints(String path) {
        return registry.get(path).getNotSuspendedEndpoints();
    }

    public Set<String> getPaths() {
        return unmodifiableSet(registry.keySet());
    }

    private String getHash(String path) {
        try {
            final VirtualFile file = vfsProvider.getVirtualFileSystem()
                                                .getRoot()
                                                .getChild(Path.of(path));
            String content;
            if (file == null) {
                content = "";
            } else {
                content = file.getContentAsString();
            }

            return Hashing.md5().hashString(content, defaultCharset()).toString();
        } catch (ServerException | ForbiddenException e) {
            LOG.error("Error trying to read {} file and broadcast it", path, e);
        }
        return null;
    }

    private class FileTrackingMetadata {
        private static final boolean ACTIVE     = true;
        private static final boolean NOT_ACTIVE = false;
        private String hashCode;
        private Map<String, Boolean> endpoints = new ConcurrentHashMap<>();

        public FileTrackingMetadata(String path, String endpoint) {
            this.hashCode = getHash(path);
            this.endpoints.put(endpoint, ACTIVE);
        }

        public void suspend(String endpoint) {
            if (endpoints.containsKey(endpoint)) {
                endpoints.put(endpoint, NOT_ACTIVE);
            }
        }

        public void resume(String endpoint) {
            if (endpoints.containsKey(endpoint)) {
                endpoints.put(endpoint, ACTIVE);
            }
        }

        public String getHashCode() {
            return hashCode;
        }

        public void setHashCode(String hashCode) {
            this.hashCode = hashCode;
        }

        public void addEndpoint(String endpoint) {
            endpoints.put(endpoint, ACTIVE);
        }

        public void removeEndpoint(String endpoint) {
            endpoints.remove(endpoint);
        }

        public Set<String> getNotSuspendedEndpoints() {
            return endpoints.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(toSet());
        }

        public Set<String> getSuspendedEndpoints() {
            return endpoints.entrySet().stream().filter(e -> !e.getValue()).map(Entry::getKey).collect(toSet());
        }

        public boolean hasEndpoints() {
            return !endpoints.isEmpty();
        }
    }
}

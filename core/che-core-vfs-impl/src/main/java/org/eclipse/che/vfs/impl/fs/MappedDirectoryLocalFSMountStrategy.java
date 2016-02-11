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
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andrew00x
 */
@Singleton
public class MappedDirectoryLocalFSMountStrategy implements LocalFSMountStrategy {
    private static final Logger                    LOG     = LoggerFactory.getLogger(MappedDirectoryLocalFSMountStrategy.class);

    private final        Map<String, java.io.File> mapping = new ConcurrentHashMap<>();

    private final java.io.File mappingFile;

    public MappedDirectoryLocalFSMountStrategy(Map<String, java.io.File> mapping) {
        this.mappingFile = null;
        this.mapping.putAll(mapping);
    }

    public MappedDirectoryLocalFSMountStrategy() {
        this.mappingFile = null;
    }

    @Inject
    private MappedDirectoryLocalFSMountStrategy(@Named("vfs.local.directory_mapping_file") java.io.File mappingFile) {
        this.mappingFile = mappingFile;
    }

    @PostConstruct
    public void start() {
        if (mappingFile.exists() && mappingFile.isFile()) {
            try {
                loadFromPropertiesFile(mappingFile);
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Unable load directory mapping from file %s. %s", mappingFile, e.getMessage()), e);
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (!mappingFile.exists()) {
            try {
                Files.createFile(mappingFile.toPath());
            } catch (IOException e) {
                throw new IllegalStateException(
                        String.format("Unable create file %s. %s", mappingFile, e.getMessage()), e);
            }
        }
        try {
            saveInPropertiesFile(mappingFile);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Unable save directory mapping in file %s. %s", mappingFile, e.getMessage()), e);
        }
    }

    public void loadFromPropertiesFile(java.io.File propertiesFile) throws IOException {
        final Properties properties = new Properties();
        try (Reader in = new FileReader(propertiesFile)) {
            properties.load(in);
        }
        mapping.clear();
        for (String key : properties.stringPropertyNames()) {
            final String value = properties.getProperty(key);
            if (value != null) {
                mapping.put(key, new java.io.File(value));
            }
        }
    }

    public void saveInPropertiesFile(java.io.File propertiesFile) throws IOException {
        final Properties properties = new Properties();
        for (Map.Entry<String, java.io.File> e : mapping.entrySet()) {
            properties.setProperty(e.getKey(), e.getValue().getAbsolutePath());
        }
        try (Writer out = new FileWriter(propertiesFile)) {
            properties.store(out, null);
        }
    }

    @Override
    public java.io.File getMountPath(String workspaceId) throws ServerException {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new ServerException("Unable get mount path for virtual file system. Workspace id is not set.");
        }
        final java.io.File directory = mapping.get(workspaceId);
        if (directory == null) {
            throw new ServerException(
                    String.format("Unable get mount path for virtual file system. Virtual file system is not configured for workspace %s.",
                            workspaceId));
        }
        return directory;
    }

    @Override
    public java.io.File getMountPath() throws ServerException {
        return getMountPath(EnvironmentContext.getCurrent().getWorkspaceId());
    }

    public void setMountPath(String workspaceId, java.io.File mountPath) {
        mapping.put(workspaceId, mountPath);
    }

    public void removeMountPath(String workspaceId) {
        mapping.remove(workspaceId);
    }

    public Map<String, java.io.File> getDirectoryMapping() {
        return mapping;
    }
}

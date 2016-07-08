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
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.languageserver.server.factory.JsonLanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.factory.LanguageServerFactory;
import org.eclipse.che.plugin.languageserver.shared.model.impl.InitializeResultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry, ServerInitializerObserver {

    private final static Logger LOG                 = LoggerFactory.getLogger(LanguageServerRegistryImpl.class);
    private final static String PROJECT_FOLDER_PATH = "/projects";
    private static final String ALL_PROJECT_MARKER = "*";

    /**
     * Available {@link LanguageServerFactory} by extension.
     */
    private final ConcurrentHashMap<String, List<LanguageServerFactory>> extensionToFactory;

    /**
     * Started {@link LanguageServer} by project.
     */
    private final ConcurrentHashMap<ProjectExtensionKey, LanguageServer> projectToServer;

    /**
     * Started {@link LanguageServer}.
     */
    private final ConcurrentHashMap<LanguageServer, InitializeResult> serverToInitResult;


    private final ProjectManager    projectManager;
    private final ServerInitializer initializer;

    @Inject
    public LanguageServerRegistryImpl(Set<LanguageServerFactory> languageServerFactories,
                                      ProjectManager projectManager,
                                      ServerInitializer initializer) {
        this.projectManager = projectManager;
        this.initializer = initializer;
        this.extensionToFactory = new ConcurrentHashMap<>();
        this.projectToServer = new ConcurrentHashMap<>();
        this.serverToInitResult = new ConcurrentHashMap<>();

        this.initializer.addObserver(this);

        for (LanguageServerFactory factory : languageServerFactories) {
            for (String extension : factory.getLanguageDescription().getFileExtensions()) {
                extensionToFactory.putIfAbsent(extension, new ArrayList<>());
                extensionToFactory.get(extension).add(factory);
            }
        }
    }

    @Override
    public LanguageServer findServer(String uri) {
        String path = URI.create(uri).getPath();
        String extension = extractExtension(path);

        String projectPath;
        try {
            projectPath = findProject(path);
        } catch (ServerException e) {
            return null;
        }

        return findOrInitializeServer(extension, projectPath);
    }

    @Nullable
    protected LanguageServer findOrInitializeServer(String extension, String projectPath) {
        ProjectExtensionKey allProjectKey = new ProjectExtensionKey(ALL_PROJECT_MARKER, extension);
        ProjectExtensionKey projectKey = new ProjectExtensionKey(projectPath, extension);

        for (LanguageServerFactory factory : extensionToFactory.get(extension)) {

            if (!projectToServer.containsKey(projectKey)) {
                synchronized (factory) {

                    if (!projectToServer.containsKey(projectKey)) {
                        LanguageServer server = projectToServer.get(allProjectKey);

                        if (server != null) {
                            projectToServer.put(projectKey, server);
                        } else {
                            server = initializer.initialize(factory, projectPath);
                            projectToServer.put(projectKey, server);

                            // ############################################
                            // TODO: We have to check capabilities here
                            // If server supports multiply projects binding then register server for all projects.
                            // InitializeResult initializeResult = serverToInitResult.get(server);
                            // initializeResult.getCapabilities()
                            // ############################################
                            if (JsonLanguageServerFactory.LANGUAGE_ID.equals(factory.getLanguageDescription().getLanguageId())) {
                                projectToServer.put(allProjectKey, server);
                            }
                        }
                    }
                }
            }

            return projectToServer.get(projectKey);
        }

        return null;
    }


    @Override
    public List<LanguageDescription> getSupportedLanguages() {
        return extensionToFactory.values()
                                 .stream()
                                 .flatMap(Collection::stream)
                                 .map(LanguageServerFactory::getLanguageDescription)
                                 .collect(Collectors.toList());
    }

    @Override
    public List<InitializeResult> getRegisteredLanguages() {
        return serverToInitResult.values().stream().collect(Collectors.toList());
    }

    protected String findProject(String path) throws ServerException {
        FolderEntry root = projectManager.getProjectsRoot();
        if (!path.startsWith(PROJECT_FOLDER_PATH)) {
            return PROJECT_FOLDER_PATH;
        }

        VirtualFileEntry fileEntry = root.getChild(path.substring(PROJECT_FOLDER_PATH.length() + 1));
        return PROJECT_FOLDER_PATH + (fileEntry == null ? "" : fileEntry.getProject());
    }

    private String extractExtension(String path) {
        int extPos = path.lastIndexOf('.');
        return extPos == -1 ? path : path.substring(extPos + 1);
    }


    @Override
    public void onServerInitialized(LanguageServer server, ServerCapabilities capabilities, LanguageDescription languageDescription) {
        InitializeResult initializeResult = new InitializeResultImpl(capabilities, languageDescription);
        serverToInitResult.put(server, initializeResult);
    }

    @PreDestroy
    public void shutdown() {
        for (LanguageServer server : serverToInitResult.keySet()) {
            server.shutdown();
            server.exit();
        }
    }

    /**
     * Is used to register {@link LanguageServer} by project.
     */
    private class ProjectExtensionKey {
        private final String project;
        private final String extension;

        public ProjectExtensionKey(String project, String extension) {
            this.project = project;
            this.extension = extension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProjectExtensionKey)) return false;
            ProjectExtensionKey that = (ProjectExtensionKey)o;
            return Objects.equals(extension, that.extension) &&
                   Objects.equals(project, that.project);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extension, project);
        }
    }
}

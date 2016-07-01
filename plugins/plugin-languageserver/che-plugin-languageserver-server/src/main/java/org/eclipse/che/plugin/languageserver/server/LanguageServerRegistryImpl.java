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
package org.eclipse.che.plugin.languageserver.server;

import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry {

    public static final String CLIENT_NAME = "EclipseChe";

    private final static Logger LOG                 = LoggerFactory.getLogger(LanguageServerRegistryImpl.class);
    private final static int    PROCESS_ID          = getProcessId();
    private final static String PROJECT_FOLDER_PATH = "/projects";

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


    private final PublishDiagnosticsParamsMessenger publishDiagnosticsMessenger;
    private final ProjectManager                    projectManager;

    @Inject
    public LanguageServerRegistryImpl(Set<LanguageServerFactory> languageServerFactories,
                                      PublishDiagnosticsParamsMessenger publishDiagnosticsMessenger,
                                      ProjectManager projectManager) {
        this.projectManager = projectManager;
        this.extensionToFactory = new ConcurrentHashMap<>();
        this.projectToServer = new ConcurrentHashMap<>();
        this.serverToInitResult = new ConcurrentHashMap<>();
        this.publishDiagnosticsMessenger = publishDiagnosticsMessenger;

        for (LanguageServerFactory factory : languageServerFactories) {
            for (String extension : factory.getLanguageDescription().getFileExtensions()) {
                extensionToFactory.putIfAbsent(extension, new ArrayList<>());
                extensionToFactory.get(extension).add(factory);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        for (LanguageServer server : projectToServer.values()) {
            server.shutdown();
            server.exit();
        }
    }

    @Override
    @Nullable
    public LanguageServer findServer(String uri) {
        String path = URI.create(uri).getPath();
        String extension = extractExtension(path);

        String project;
        try {
            project = findProject(path);
        } catch (ServerException e) {
            return null;
        }

        return findOrInitializeServer(extension, project);
    }

    @Nullable
    protected LanguageServer findOrInitializeServer(String extension, String projectPath) {
        for (LanguageServerFactory factory : extensionToFactory.get(extension)) {
            ProjectExtensionKey projectKey = new ProjectExtensionKey(projectPath, extension);

            if (!projectToServer.containsKey(projectKey)) {
                synchronized (factory) {
                    if (!projectToServer.containsKey(projectKey)) {
                        LanguageServer server = initialize(factory, projectPath);
                        projectToServer.put(projectKey, server);
                    }
                }
            }

            return projectToServer.get(projectKey);
        }

        return null;
    }

    protected LanguageServer initialize(LanguageServerFactory factory, String projectPath) {
        String languageId = factory.getLanguageDescription().getLanguageId();
        LOG.info("Initializing Language Server {} on {}", languageId, projectPath);

        InitializeParamsImpl initializeParams = new InitializeParamsImpl();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);
        initializeParams.setClientName(CLIENT_NAME);

        LanguageServer server;
        try {
            server = factory.create(projectPath);
        } catch (LanguageServerException e) {
            LOG.error("Can't initialize Language Server {} on {}. " + e.getMessage(), languageId, projectPath);
            return null;
        }

        server.getTextDocumentService().onPublishDiagnostics(publishDiagnosticsMessenger::onEvent);
        server.getWindowService().onLogMessage(messageParams -> LOG.error(messageParams.getType() + " " + messageParams.getMessage()));

        CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);

        InitializeResult initializeResult;
        try {
            initializeResult = completableFuture.get();
            serverToInitResult.put(server, initializeResult);
        } catch (InterruptedException | ExecutionException e) {
            String errMsg = "Error initialing language server. " + e.getMessage();
            LOG.error(errMsg, e);
        }

        return server;
    }

    @Override
    public List<LanguageDescription> getSupportedLanguages() {
        return extensionToFactory.values()
                                 .stream()
                                 .flatMap(Collection::stream)
                                 .map(LanguageServerFactory::getLanguageDescription)
                                 .collect(Collectors.toList());
    }

    private String findProject(String path) throws ServerException {
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

    private static int getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int prefixEnd = name.indexOf('@');
        if (prefixEnd != -1) {
            String prefix = name.substring(0, prefixEnd);
            try {
                return Integer.parseInt(prefix);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private class ProjectExtensionKey {
        private final String extension;
        private final String project;

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

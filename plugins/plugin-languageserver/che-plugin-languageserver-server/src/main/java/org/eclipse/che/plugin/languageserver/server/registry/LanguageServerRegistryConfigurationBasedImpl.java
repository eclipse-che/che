/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.server.factory.ConfigurationBasedLanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.factory.LanguageServerFactory;
import org.eclipse.che.plugin.languageserver.shared.ProjectExtensionKey;
import org.eclipse.che.plugin.languageserver.shared.lsapi.ServerConfigurationDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.ServerConfigurationUrlsDTO;
import org.eclipse.che.plugin.languageserver.shared.model.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.io.Files.getFileExtension;
import static org.eclipse.che.plugin.languageserver.shared.ProjectExtensionKey.createProjectKey;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class LanguageServerRegistryConfigurationBasedImpl implements LanguageServerRegistry, ServerInitializerObserver {
    public static final String CONFIGURATION_URL = "https://codenvy.com/update/repository/public/download/language-server-configurations";

    public final static String PROJECT_FOLDER_PATH = "/projects";
    private final static Logger LOG                 = LoggerFactory.getLogger(LanguageServerRegistryConfigurationBasedImpl.class);

    /**
     * Available {@link LanguageServerFactory} by extension.
     */
    private final ConcurrentHashMap<String, List<LanguageServerFactory>> extensionToFactory;

    /**
     * Started {@link LanguageServer} by project.
     */
    private final ConcurrentHashMap<ProjectExtensionKey, LanguageServer> projectToServer;

    private final ServerInitializer initializer;
    private final ProjectManager    projectManager;

    @Inject
    public LanguageServerRegistryConfigurationBasedImpl(ServerInitializer initializer, ProjectManager projectManager) {
        this.initializer = initializer;
        this.projectManager = projectManager;
        this.initializer.addObserver(this);

        this.extensionToFactory = new ConcurrentHashMap<>();
        this.projectToServer = new ConcurrentHashMap<>();

        for (LanguageServerFactory factory : prepareFactories()) {
            for (String extension : factory.getLanguageDescription().getFileExtensions()) {
                extensionToFactory.putIfAbsent(extension, new ArrayList<>());
                extensionToFactory.get(extension).add(factory);
            }
        }
    }

    @Override
    public LanguageServer findServer(String fileUri) throws LanguageServerException {
        String path = URI.create(fileUri).getPath();

        String extension = getFileExtension(path);
        String projectPath = extractProjectPath(path);

        return findServer(extension, projectPath);
    }

    @Nullable
    protected LanguageServer findServer(String extension, String projectPath) throws LanguageServerException {
        ProjectExtensionKey projectKey = createProjectKey(projectPath, extension);

        for (LanguageServerFactory factory : extensionToFactory.get(extension)) {
            if (!projectToServer.containsKey(projectKey)) {
                synchronized (factory) {
                    if (!projectToServer.containsKey(projectKey)) {
                        LanguageServer server = initializer.initialize(factory, projectPath);
                        projectToServer.put(projectKey, server);
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
    public Map<ProjectExtensionKey, InitializeResult> getInitializedLanguages() {
        Map<LanguageServer, InitializeResult> initializedServers = initializer.getInitializedServers();
        return projectToServer.entrySet()
                              .stream()
                              .collect(Collectors.toMap(Map.Entry::getKey, e -> initializedServers.get(e.getValue())));
    }


    @Override
    public void onServerInitialized(LanguageServer server,
                                    ServerCapabilities capabilities,
                                    LanguageDescription languageDescription,
                                    String projectPath) {
        for (String ext : languageDescription.getFileExtensions()) {
            projectToServer.put(createProjectKey(projectPath, ext), server);
        }
    }

    protected List<LanguageServerFactory> prepareFactories() {
        List<ServerConfiguration> confs = fetchConfigurations();
        return initializeFactories(confs);
    }

    protected List<ServerConfiguration> fetchConfigurations() {
        DtoFactory dtoFactory = DtoFactory.getInstance();

        String json;
        try {
            json = IOUtils.toString(new URL(CONFIGURATION_URL));
        } catch (IOException e) {
            LOG.error("Can't fetch language servers configurations. " + e.getMessage(), e);
            return Collections.emptyList();
        }

        ServerConfigurationUrlsDTO confUrls = dtoFactory.createDtoFromJson(json, ServerConfigurationUrlsDTO.class);
        List<ServerConfiguration> confs = new ArrayList<>(confUrls.getUrls().size());

        for (String url : confUrls.getUrls()) {
            try {
                json = IOUtils.toString(new URL(url));
            } catch (IOException e) {
                LOG.error("Can't fetch language server configuration " + url + ". " + e.getMessage(), e);
                continue;
            }

            ServerConfigurationDTO conf = dtoFactory.createDtoFromJson(json, ServerConfigurationDTO.class);
            confs.add(conf);
        }

        return confs;
    }

    protected List<LanguageServerFactory> initializeFactories(List<ServerConfiguration> confs) {
        return confs.stream().map(ConfigurationBasedLanguageServerFactory::new).collect(Collectors.toList());
    }

    protected String extractProjectPath(String filePath) throws LanguageServerException {
        FolderEntry root;
        try {
            root = projectManager.getProjectsRoot();
        } catch (ServerException e) {
            throw new LanguageServerException("Project not found for " + filePath, e);
        }

        if (!filePath.startsWith(PROJECT_FOLDER_PATH)) {
            throw new LanguageServerException("Project not found for " + filePath);
        }

        VirtualFileEntry fileEntry;
        try {
            fileEntry = root.getChild(filePath.substring(PROJECT_FOLDER_PATH.length() + 1));
        } catch (ServerException e) {
            throw new LanguageServerException("Project not found for " + filePath, e);
        }

        if (fileEntry == null) {
            throw new LanguageServerException("Project not found for " + filePath);
        }

        return PROJECT_FOLDER_PATH + fileEntry.getProject();
    }
}

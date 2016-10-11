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
package org.eclipse.che.api.languageserver.registry;

import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.ProjectExtensionKey;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.annotation.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.io.Files.getFileExtension;
import static org.eclipse.che.api.languageserver.shared.ProjectExtensionKey.createProjectKey;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry, ServerInitializerObserver {
    public final static String PROJECT_FOLDER_PATH = "/projects";

    /**
     * Available {@link LanguageServerLauncher} by extension.
     */
    private final ConcurrentHashMap<String, List<LanguageServerLauncher>> extensionToLauncher;

    /**
     * Started {@link LanguageServer} by project.
     */
    private final ConcurrentHashMap<ProjectExtensionKey, LanguageServer> projectToServer;

    private final Provider<ProjectManager> projectManagerProvider;
    private final ServerInitializer        initializer;

    @Inject
    public LanguageServerRegistryImpl(Set<LanguageServerLauncher> languageServerLaunchers,
                                      Provider<ProjectManager> projectManagerProvider,
                                      ServerInitializer initializer) {
        this.projectManagerProvider = projectManagerProvider;
        this.initializer = initializer;
        this.extensionToLauncher = new ConcurrentHashMap<>();
        this.projectToServer = new ConcurrentHashMap<>();
        this.initializer.addObserver(this);

        for (LanguageServerLauncher launcher : languageServerLaunchers) {
            for (String extension : launcher.getLanguageDescription().getFileExtensions()) {
                extensionToLauncher.putIfAbsent(extension, new ArrayList<>());
                extensionToLauncher.get(extension).add(launcher);
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

        for (LanguageServerLauncher launcher : extensionToLauncher.get(extension)) {
            if (!projectToServer.containsKey(projectKey)) {
                synchronized (launcher) {
                    if (!projectToServer.containsKey(projectKey)) {
                        LanguageServer server = initializer.initialize(launcher, projectPath);
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
        return extensionToLauncher.values()
                                  .stream()
                                  .flatMap(Collection::stream)
                                  .filter(LanguageServerLauncher::isAbleToLaunch)
                                  .map(LanguageServerLauncher::getLanguageDescription)
                                  .collect(Collectors.toList());
    }

    @Override
    public Map<ProjectExtensionKey, LanguageServerDescription> getInitializedLanguages() {
        Map<LanguageServer, LanguageServerDescription> initializedServers = initializer.getInitializedServers();
        return projectToServer.entrySet()
                              .stream()
                              .collect(Collectors.toMap(Map.Entry::getKey, e -> initializedServers.get(e.getValue())));
    }

    protected String extractProjectPath(String filePath) throws LanguageServerException {
        FolderEntry root;
        try {
            root = projectManagerProvider.get().getProjectsRoot();
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

    @Override
    public void onServerInitialized(LanguageServer server,
                                    ServerCapabilities capabilities,
                                    LanguageDescription languageDescription,
                                    String projectPath) {
        for (String ext : languageDescription.getFileExtensions()) {
            projectToServer.put(createProjectKey(projectPath, ext), server);
        }
    }
}

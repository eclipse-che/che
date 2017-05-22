/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.registry;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.ProjectLangugageKey;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.eclipse.che.api.languageserver.shared.ProjectLangugageKey.createProjectKey;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry, ServerInitializerObserver {
    public final static String                        PROJECT_FOLDER_PATH = "/projects";
    private final List<LanguageDescription>           languages           = new ArrayList<>();
    private final Map<String, LanguageServerLauncher> launchers           = new HashMap<>();

    /**
     * Started {@link LanguageServer} by project.
     */
    private final ConcurrentHashMap<ProjectLangugageKey, LanguageServer> projectToServer;

    private final Provider<ProjectManager> projectManagerProvider;
    private final ServerInitializer        initializer;

    @Inject
    public LanguageServerRegistryImpl(Set<LanguageServerLauncher> languageServerLaunchers, Set<LanguageDescription> languages,
                                      Provider<ProjectManager> projectManagerProvider, ServerInitializer initializer) {
        this.languages.addAll(languages);
        this.launchers.putAll(languageServerLaunchers.stream()
                        .collect(Collectors.toMap(LanguageServerLauncher::getLanguageId, launcher -> launcher)));
        this.projectManagerProvider = projectManagerProvider;
        this.initializer = initializer;
        this.projectToServer = new ConcurrentHashMap<>();
        this.initializer.addObserver(this);
    }

    @Override
    public LanguageServer findServer(String fileUri) throws LanguageServerException {
        String path = URI.create(fileUri).getPath();

        String projectPath = extractProjectPath(path);

        return doFindServer(projectPath, findLanguageId(path));
    }

    private LanguageDescription findLanguageId(String path) {
        for (LanguageDescription language : languages) {
            if (matchesFilenames(language, path) || matchesExtensions(language, path)) {
                return language;
            }
        }
        return null;
    }

    private boolean matchesExtensions(LanguageDescription language, String path) {
        if (language.getFileExtensions() != null) {
            for (String extension : language.getFileExtensions()) {
                if (path.endsWith(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesFilenames(LanguageDescription language, String path) {
        if (language.getFileNames() != null) {
            for (String name : language.getFileNames()) {
                if (name.equals(new File(path).getName())) {
                    return true;
                }
            } 
        }
        return false;
    }

    @Nullable
    protected LanguageServer doFindServer(String projectPath, LanguageDescription language) throws LanguageServerException {
        if (language == null || projectPath == null) {
            return null;
        }
        ProjectLangugageKey projectKey = createProjectKey(projectPath, language.getLanguageId());
        LanguageServerLauncher launcher = launchers.get(language.getLanguageId());

        if (language != null && launcher != null) {
            synchronized (launcher) {
                // we're relying on the fact that the following if condition
                // will
                // not change unless someone else uses the same launcher
                if (!projectToServer.containsKey(projectKey)) {
                    LanguageServer server = initializer.initialize(language, launcher, projectPath);
                    projectToServer.put(projectKey, server);
                }
            }
        }
        return projectToServer.get(projectKey);

    }

    @Override
    public List<LanguageDescription> getSupportedLanguages() {
        return Collections.unmodifiableList(languages);
    }

    @Override
    public Map<ProjectLangugageKey, LanguageServerDescription> getInitializedLanguages() {
        Map<LanguageServer, LanguageServerDescription> initializedServers = initializer.getInitializedServers();
        return projectToServer.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> initializedServers.get(e.getValue())));
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
    public void onServerInitialized(LanguageServer server, ServerCapabilities capabilities, LanguageDescription languageDescription,
                                    String projectPath) {
        for (String ext : languageDescription.getFileExtensions()) {
            projectToServer.put(createProjectKey(projectPath, ext), server);
        }
    }
}

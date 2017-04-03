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

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.typefox.lsapi.services.LanguageServer;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.lang.Pair;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry {
    public final static String PROJECT_FOLDER_PATH = "/projects";

    Set<LanguageServerLauncher> knownLaunchers = new HashSet<>();

    /**
     * Started {@link LanguageServer} by project.
     */
    private final ConcurrentHashMap<Pair<String, LanguageServerLauncher>, LanguageServer> projectToServer;

    private final Provider<ProjectManager> projectManagerProvider;
    private final ServerInitializer initializer;

    @Inject
    public LanguageServerRegistryImpl(Set<LanguageServerLauncher> languageServerLaunchers, Provider<ProjectManager> projectManagerProvider,
                    ServerInitializer initializer) {
        this.projectManagerProvider = projectManagerProvider;
        this.initializer = initializer;
        this.projectToServer = new ConcurrentHashMap<>();

        knownLaunchers= Collections.unmodifiableSet(new HashSet<>(languageServerLaunchers));
    }

    @Override
    public LanguageServer findServer(String fileUri) throws LanguageServerException {
        String path = URI.create(fileUri).getPath();
        String projectPath = extractProjectPath(path);
        LanguageServerLauncher launcher = findResponsibleLaucher(projectPath, path);
        if (launcher != null) {
            Pair<String, LanguageServerLauncher> key = Pair.of(projectPath, launcher);
            synchronized (launcher) {
                    LanguageServer server= projectToServer.get(key);
                    if (server == null) {
                        server = initializer.initialize(launcher, projectPath);
                        projectToServer.put(key, server);
                    }
                    return server;
            }
        }

        return null;
    }
    
    LanguageServerLauncher findResponsibleLaucher(String projectPath, String path) {
        for (LanguageServerLauncher launcher : knownLaunchers) {
            if (matchesPattern(launcher, path)) {
                return launcher;
            }
        }
        for (LanguageServerLauncher launcher : knownLaunchers) {
            if (matchesExtension(launcher, path)) {
                return launcher;
            }
        }

        return null;
    }

    private boolean matchesExtension(LanguageServerLauncher launcher, String path) {
        String fileExtension = Files.getFileExtension(path);
        
        List<String> fileExtensions = launcher.getLanguageDescription().getFileExtensions();
        for (String ext : fileExtensions) {
            if (ext.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPattern(LanguageServerLauncher launcher, String path) {
        List<String> patterns = launcher.getLanguageDescription().getFileNamePatterns();
        if (patterns != null) {
            String name= getFileName(path);
            for (String p : patterns) {
                Pattern pattern = Pattern.compile(p);
                if (pattern.matcher(name).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private LanguageServer doFindServer(LanguageServerLauncher launcher, String projectPath) {
        return projectToServer.get(Pair.of(projectPath, launcher));
    }

    private String getFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0) {
            return path;
        } else if (lastSlash == path.length() - 1) {
            return "";
        } else {
            return path.substring(lastSlash + 1);
        }
    }

    @Override
    public Collection<LanguageDescription> getSupportedLanguages() {
        return knownLaunchers.stream().map(l->l.getLanguageDescription()).collect(Collectors.toSet());
    }

    @Override
    public Map<String, List<LanguageServerDescription>> getInitializedLanguages() {
        Map<String, List<LanguageServerDescription>> result = new HashMap<>();
        Map<LanguageServer, LanguageServerDescription> initializedServers = initializer.getInitializedServers();
        for (Entry<Pair<String, LanguageServerLauncher>, LanguageServer> entry : projectToServer.entrySet()) {
            ArrayList<LanguageServerDescription> list = new ArrayList<>();
            
            result.put(entry.getKey().first, list);
            LanguageServerDescription initializedServer = initializedServers.get(entry.getValue());
            if (initializedServer != null) {
                list.add(initializedServer);
            }
        }
        return result;
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
}

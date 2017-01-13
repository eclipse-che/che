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
package org.eclipse.che.api.local;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.adapters.RecipeTypeAdapter;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * In memory based implementation of {@link WorkspaceDao}.
 *
 * <p>{@link #loadWorkspaces() Loads} & {@link #saveWorkspaces() stores} in memory workspaces
 * to/from filesystem, when component starts/stops.
 *
 * @author Eugene Voevodin
 * @author Dmitry Shnurenko
 * @implNote it is thread-safe, guarded by <i>this</i> instance
 */
@Singleton
public class LocalWorkspaceDaoImpl implements WorkspaceDao {

    public static final String FILENAME = "workspaces.json";

    @VisibleForTesting
    final         Map<String, WorkspaceImpl> workspaces;
    private final LocalStorage               localStorage;

    @Inject
    public LocalWorkspaceDaoImpl(LocalStorageFactory factory, WorkspaceConfigJsonAdapter cfgAdapter) throws IOException {
        final Map<Class<?>, Object> adapters =
                ImmutableMap.of(Recipe.class, new RecipeTypeAdapter(),
                                ProjectConfig.class, new ProjectConfigAdapter(),
                                WorkspaceConfigImpl.class, new WorkspaceConfigDeserializer(cfgAdapter));
        this.localStorage = factory.create(FILENAME, adapters);
        this.workspaces = new HashMap<>();
    }

    @PostConstruct
    public synchronized void loadWorkspaces() {
        workspaces.putAll(localStorage.loadMap(new TypeToken<Map<String, WorkspaceImpl>>() {}));
        for (WorkspaceImpl workspace : workspaces.values()) {
            workspace.setRuntime(null);
        }
    }

    public synchronized void saveWorkspaces() throws IOException {
        localStorage.store(workspaces);
    }

    @Override
    public synchronized WorkspaceImpl create(WorkspaceImpl workspace) throws ConflictException, ServerException {
        requireNonNull(workspace, "Required non-null workspace");
        if (workspaces.containsKey(workspace.getId())) {
            throw new ConflictException("Workspace with id " + workspace.getId() + " already exists");
        }
        if (find(workspace.getConfig().getName(), workspace.getNamespace()).isPresent()) {
            throw new ConflictException(format("Workspace with name %s and owner %s already exists",
                                               workspace.getConfig().getName(),
                                               workspace.getNamespace()));
        }

        workspace.setRuntime(null);
        workspace.setStatus(WorkspaceStatus.STOPPED);
        workspaces.put(workspace.getId(), new WorkspaceImpl(workspace, workspace.getAccount()));
        return workspace;
    }

    @Override
    public synchronized WorkspaceImpl update(WorkspaceImpl workspace) throws NotFoundException,
                                                                             ConflictException,
                                                                             ServerException {
        requireNonNull(workspace, "Required non-null workspace");
        if (!workspaces.containsKey(workspace.getId())) {
            throw new NotFoundException("Workspace with id " + workspace.getId() + " was not found");
        }
        if (find(workspace.getConfig().getName(), workspace.getNamespace()).isPresent()) {
            throw new ConflictException(format("Workspace with name %s and owner %s already exists",
                                               workspace.getConfig().getName(),
                                               workspace.getNamespace()));
        }
        workspace.setStatus(null);
        workspace.setRuntime(null);
        workspaces.put(workspace.getId(), new WorkspaceImpl(workspace, workspace.getAccount()));
        return workspace;
    }

    @Override
    public synchronized void remove(String id) throws ServerException {
        requireNonNull(id, "Required non-null id");
        workspaces.remove(id);
    }

    @Override
    public synchronized WorkspaceImpl get(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        final WorkspaceImpl workspace = workspaces.get(id);
        if (workspace == null) {
            throw new NotFoundException("Workspace with id " + id + " was not found");
        }
        return new WorkspaceImpl(workspace, workspace.getAccount());
    }

    @Override
    public synchronized WorkspaceImpl get(String name, String namespace) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null name");
        requireNonNull(namespace, "Required non-null namespace");
        final Optional<WorkspaceImpl> wsOpt = find(name, namespace);
        if (!wsOpt.isPresent()) {
            throw new NotFoundException(format("Workspace with name %s and owner %s was not found", name, namespace));
        }
        WorkspaceImpl workspace = wsOpt.get();
        return new WorkspaceImpl(workspace, workspace.getAccount());
    }

    @Override
    public synchronized List<WorkspaceImpl> getByNamespace(String namespace) throws ServerException {
        requireNonNull(namespace, "Required non-null namespace");
        return workspaces.values()
                         .stream()
                         .filter(ws -> ws.getNamespace().equals(namespace))
                         .map(ws -> new WorkspaceImpl(ws, ws.getAccount()))
                         .collect(toList());
    }

    @Override
    public List<WorkspaceImpl> getWorkspaces(String userId) throws ServerException {
        return new ArrayList<>(workspaces.values());
    }

    @Override
    public List<WorkspaceImpl> getWorkspaces(boolean isTemporary, int skipCount, int maxItems) throws ServerException {
        Stream<WorkspaceImpl> stream = workspaces.values().stream();
        stream.filter(ws -> ws.isTemporary() == isTemporary);
        stream.skip(skipCount);
        if (maxItems != 0) {
            stream.limit(maxItems);
        }
        return  stream.collect(toList());
    }


    private Optional<WorkspaceImpl> find(String name, String owner) {
        return workspaces.values()
                         .stream()
                         .filter(ws -> ws.getConfig().getName().equals(name) && ws.getNamespace().equals(owner))
                         .findFirst();
    }
}

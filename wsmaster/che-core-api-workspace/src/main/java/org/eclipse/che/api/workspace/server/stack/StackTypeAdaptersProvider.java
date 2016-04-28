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
package org.eclipse.che.api.workspace.server.stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.server.recipe.adapters.GroupAdapter;
import org.eclipse.che.api.machine.server.recipe.adapters.PermissionsAdapter;
import org.eclipse.che.api.machine.server.recipe.adapters.RecipeTypeAdapter;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.workspace.server.stack.adapters.CommandAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.EnvironmentAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.LimitsAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.MachineConfigAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.MachineSourceAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.ProjectConfigAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.StackComponentAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.StackIconAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.StackSourceAdapter;
import org.eclipse.che.api.workspace.server.stack.adapters.WorkspaceConfigAdapter;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.eclipse.che.api.workspace.shared.stack.StackSource;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Provides {@link Gson} type adapters for serialization and deserialization components of the {@link Stack} objects.
 * Usages in the local stack storage.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class StackTypeAdaptersProvider {

    private final Map<Class<?>, Object> adapters;

    private Gson gson;

    @Inject
    public StackTypeAdaptersProvider() {
        adapters = new HashMap<>();
        adapters.put(StackComponent.class, new StackComponentAdapter());
        adapters.put(WorkspaceConfig.class, new WorkspaceConfigAdapter());
        adapters.put(ProjectConfig.class, new ProjectConfigAdapter());
        adapters.put(Environment.class, new EnvironmentAdapter());
        adapters.put(Command.class, new CommandAdapter());
        adapters.put(Recipe.class, new RecipeTypeAdapter());
        adapters.put(Limits.class, new LimitsAdapter());
        adapters.put(MachineSource.class, new MachineSourceAdapter());
        adapters.put(MachineConfig.class, new MachineConfigAdapter());
        adapters.put(StackSource.class, new StackSourceAdapter());
        adapters.put(Permissions.class, new PermissionsAdapter());
        adapters.put(Group.class, new GroupAdapter());
        adapters.put(StackIcon.class, new StackIconAdapter());

        GsonBuilder gsonBuilder = new GsonBuilder();
        for (Map.Entry<Class<?>, Object> adapter : adapters.entrySet()) {
            gsonBuilder.registerTypeAdapter(adapter.getKey(), adapter.getValue());
        }
        gson = gsonBuilder.setPrettyPrinting().create();
    }

    /**
     * Returns {@link Gson} type adapters for serialization and deserialization {@link Stack} objects
     */
    public Map<Class<?>, Object> getTypeAdapters() {
        return unmodifiableMap(adapters);
    }

    /**
     * Returns {@link Gson} for serialization and deserialization {@link Stack} objects
     */
    public Gson getGson() {
        return gson;
    }
}

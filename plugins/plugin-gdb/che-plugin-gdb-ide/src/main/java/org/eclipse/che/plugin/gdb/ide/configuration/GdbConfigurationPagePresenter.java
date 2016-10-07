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
package org.eclipse.che.plugin.gdb.ide.configuration;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.eclipse.che.ide.json.JsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Page allows to edit GDB debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GdbConfigurationPagePresenter implements GdbConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

    public static final String BIN_PATH_CONNECTION_PROPERTY   = "BINARY";
    public static final String DEFAULT_EXECUTABLE_TARGET_NAME = "a.out";

    private final GdbConfigurationPageView view;
    private final AppContext               appContext;
    private final EntityFactory            entityFactory;
    private final RecipeServiceClient      recipeServiceClient;
    private final DtoFactory               dtoFactory;
    private final CurrentProjectPathMacro  currentProjectPathMacro;

    private DebugConfiguration editedConfiguration;
    private String             originHost;
    private int                originPort;
    private String             originBinaryPath;
    private DirtyStateListener listener;

    @Inject
    public GdbConfigurationPagePresenter(GdbConfigurationPageView view,
                                         AppContext appContext,
                                         DtoFactory dtoFactory,
                                         EntityFactory entityFactory,
                                         RecipeServiceClient recipeServiceClient,
                                         CurrentProjectPathMacro currentProjectPathMacro) {
        this.view = view;
        this.appContext = appContext;
        this.entityFactory = entityFactory;
        this.recipeServiceClient = recipeServiceClient;
        this.dtoFactory = dtoFactory;
        this.currentProjectPathMacro = currentProjectPathMacro;

        view.setDelegate(this);
    }

    @Override
    public void resetFrom(DebugConfiguration configuration) {
        editedConfiguration = configuration;

        originHost = configuration.getHost();
        originPort = configuration.getPort();
        originBinaryPath = getBinaryPath(configuration);

        if (originBinaryPath == null) {
            String defaultBinaryPath = getDefaultBinaryPath();
            editedConfiguration.getConnectionProperties().put(BIN_PATH_CONNECTION_PROPERTY, defaultBinaryPath);
            originBinaryPath = defaultBinaryPath;
        }
    }

    private String getBinaryPath(DebugConfiguration debugConfiguration) {
        Map<String, String> connectionProperties = debugConfiguration.getConnectionProperties();
        return connectionProperties.get(BIN_PATH_CONNECTION_PROPERTY);
    }

    private String getDefaultBinaryPath() {
        return currentProjectPathMacro.getName() + "/" + DEFAULT_EXECUTABLE_TARGET_NAME;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setHost(editedConfiguration.getHost());
        view.setPort(editedConfiguration.getPort());
        view.setBinaryPath(getBinaryPath(editedConfiguration));

        boolean devHost = "localhost".equals(editedConfiguration.getHost()) && editedConfiguration.getPort() <= 0;
        view.setDevHost(devHost);
        view.setPortEnableState(!devHost);
        view.setHostEnableState(!devHost);

        List<Machine> machines = getMachines();
        if (!machines.isEmpty()) {
            setHosts(machines);
        }
    }

    private void setHosts(List<Machine> machines) {
        List<Promise<RecipeDescriptor>> recipePromises = new ArrayList<>(machines.size());
        for (Machine machine : machines) {
            String location = machine.getConfig().getSource().getLocation();
            String recipeId = getRecipeId(location);
            recipePromises.add(recipeServiceClient.getRecipe(recipeId));
        }

        @SuppressWarnings("unchecked")
        final Promise<RecipeDescriptor>[] recipePromisesArray = (Promise<RecipeDescriptor>[])recipePromises.toArray();
        setHostsList(recipePromisesArray, machines);
    }

    private List<Machine> getMachines() {
        Workspace workspace = appContext.getWorkspace();
        if (workspace == null || workspace.getRuntime() == null) {
            return emptyList();
        }

        List<? extends Machine> runtimeMachines = workspace.getRuntime().getMachines();
        List<Machine> machines = new ArrayList<>(runtimeMachines.size());
        for (Machine currentMachine : runtimeMachines) {
            if (currentMachine instanceof MachineDto) {
                Machine machine = entityFactory.createMachine((MachineDto)currentMachine);
                machines.add(machine);
            }
        }
        return machines;
    }

    private void setHostsList(final Promise<RecipeDescriptor>[] recipePromises, final List<Machine> machines) {
        Promises.all(recipePromises).then(new Operation<JsArrayMixed>() {
            @Override
            public void apply(JsArrayMixed recipes) throws OperationException {
                Map<String, String> hosts = new HashMap<>();

                for (int i = 0; i < recipes.length(); i++) {
                    String recipeJson = recipes.getObject(i).toString();
                    RecipeDescriptor recipeDescriptor = dtoFactory.createDtoFromJson(recipeJson, RecipeDescriptor.class);

                    String script = recipeDescriptor.getScript();

                    String host;
                    try {
                        Map<String, String> m = JsonHelper.toMap(script);
                        host = m.containsKey("host") ? m.get("host") : "localhost";
                    } catch (Exception e) {
                        host = "localhost";
                    }
                    String description = host + " (" + machines.get(i).getConfig().getName() + ")";
                    hosts.put(host, description);
                }

                view.setHostsList(hosts);
            }
        });
    }

    @Override
    public boolean isDirty() {
        return !originHost.equals(editedConfiguration.getHost())
               || originPort != editedConfiguration.getPort()
               || !originBinaryPath.equals(getBinaryPath(editedConfiguration));
    }

    @Override
    public void setDirtyStateListener(DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onHostChanged() {
        editedConfiguration.setHost(view.getHost());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onPortChanged() {
        editedConfiguration.setPort(view.getPort());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onBinaryPathChanged() {
        final Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();
        connectionProperties.put(BIN_PATH_CONNECTION_PROPERTY, view.getBinaryPath());

        editedConfiguration.setConnectionProperties(connectionProperties);
        listener.onDirtyStateChanged();
    }

    @Override
    public void onDevHostChanged(boolean value) {
        view.setHostEnableState(!value);
        view.setPortEnableState(!value);
        if (value) {
            editedConfiguration.setHost("localhost");
            view.setHost(editedConfiguration.getHost());

            editedConfiguration.setPort(0);
            view.setPort(0);

            listener.onDirtyStateChanged();
        }
    }

    private String getRecipeId(String location) {
        location = location.substring(0, location.lastIndexOf("/"));
        return location.substring(location.lastIndexOf("/") + 1);
    }
}

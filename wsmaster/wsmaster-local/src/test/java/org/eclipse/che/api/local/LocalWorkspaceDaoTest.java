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
package org.eclipse.che.api.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class LocalWorkspaceDaoTest {

    static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    LocalWorkspaceDaoImpl workspaceDao;
    Path                  workspacesPath;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path targetDir = Paths.get(url.toURI()).getParent();
        final Path storageRoot = targetDir.resolve("workspaces");
        workspacesPath = storageRoot.resolve("workspaces.json");
        final WorkspaceConfigJsonAdapter adapter = mock(WorkspaceConfigJsonAdapter.class);
        workspaceDao = new LocalWorkspaceDaoImpl(new LocalStorageFactory(storageRoot.toString()), adapter);
    }

    @Test
    public void testWorkspaceSerialization() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);
        workspaceDao.saveWorkspaces();

        assertEquals(GSON.toJson(singletonMap(workspace.getId(), workspace)), new String(readAllBytes(workspacesPath)));
    }

    @Test
    public void testWorkspaceDeserialization() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        write(workspacesPath, GSON.toJson(singletonMap(workspace.getId(), workspace)).getBytes());

        workspaceDao.loadWorkspaces();

        final WorkspaceImpl result = workspaceDao.get(workspace.getId());
        assertEquals(result, workspace);
    }

    @Test
    public void testOldFormatIsAdaptedWhenWorkspaceIsLoaded() throws Exception {
        final URL rootUrl = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(rootUrl);
        final String path = Paths.get(rootUrl.toURI()).toString();
        final LocalStorageFactory storageFactory = new LocalStorageFactory(path);
        final WorkspaceConfigJsonAdapter workspaceAdapter = new WorkspaceConfigJsonAdapter();
        final LocalWorkspaceDaoImpl workspaceDao = new LocalWorkspaceDaoImpl(storageFactory, workspaceAdapter);

        workspaceDao.loadWorkspaces();

        final WorkspaceImpl test = workspaceDao.get("test");
        final EnvironmentImpl environment = test.getConfig().getEnvironments().get(test.getConfig().getDefaultEnv());
        assertEquals(environment.getRecipe().getType(), "dockerfile");
        assertEquals(environment.getRecipe().getLocation(), "host/api/recipe/recipew7j6ebw6or6rqu2t/script");
        assertEquals(environment.getMachines().size(), 1);
    }

    private static WorkspaceImpl createWorkspace() {
        // environments
        Map<String, EnvironmentImpl> environments = new HashMap<>();

        Map<String, ExtendedMachineImpl> machines;
        Map<String, ServerConf2Impl> servers;
        Map<String, String> properties;
        EnvironmentImpl env;

        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");
        servers.put("ref1", new ServerConf2Impl("port1", "proto1", properties));
        properties = new HashMap<>();
        properties.put("prop3", "value3");
        properties.put("prop4", "value4");
        servers.put("ref2", new ServerConf2Impl("port2", "proto2", properties));
        machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(asList("ws-agent", "someAgent"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop5", "value5");
        properties.put("prop6", "value6");
        servers.put("ref3", new ServerConf2Impl("port3", "proto3", properties));
        properties = new HashMap<>();
        properties.put("prop7", "value7");
        properties.put("prop8", "value8");
        servers.put("ref4", new ServerConf2Impl("port4", "proto4", properties));
        machines = new HashMap<>();
        machines.put("machine2", new ExtendedMachineImpl(asList("ws-agent2", "someAgent2"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment", env);

        env = new EnvironmentImpl();
        servers = new HashMap<>();
        properties = new HashMap<>();
        servers.put("ref11", new ServerConf2Impl("port11", "proto11", properties));
        servers.put("ref12", new ServerConf2Impl("port12", "proto12", null));
        machines = new HashMap<>();
        machines.put("machine11", new ExtendedMachineImpl(emptyList(),
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers.put("ref13", new ServerConf2Impl("port13", "proto13", singletonMap("prop11", "value11")));
        servers.put("ref14", new ServerConf2Impl("port4", null, null));
        servers.put("ref15", new ServerConf2Impl(null, null, null));
        machines.put("machine12", new ExtendedMachineImpl(null,
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        machines.put("machine13", new ExtendedMachineImpl(null,
                                                          null,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment-2", env);

        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl(null, null, null, null));
        env.setMachines(null);

        environments.put("my-environment-3", env);

        // projects
        final ProjectConfigImpl project1 = new ProjectConfigImpl();
        project1.setName("test-project-name");
        project1.setDescription("This is test project");
        project1.setPath("/path/to/project");
        project1.setType("maven");
        project1.setMixins(singletonList("git"));

        final Map<String, List<String>> projectAttrs = new HashMap<>(4);
        projectAttrs.put("project.attribute1", singletonList("value1"));
        projectAttrs.put("project.attribute2", asList("value2", "value3"));
        project1.setAttributes(projectAttrs);

        final Map<String, String> sourceParameters = new HashMap<>(4);
        sourceParameters.put("source-parameter-1", "value1");
        sourceParameters.put("source-parameter-2", "value2");
        project1.setSource(new SourceStorageImpl("sources-type", "sources-location", sourceParameters));

        final List<ProjectConfigImpl> projects = singletonList(project1);

        // commands
        final List<CommandImpl> commands = new ArrayList<>(3);
        commands.add(new CommandImpl("MCI", "mvn clean install", "maven"));
        commands.add(new CommandImpl("bower install", "bower install", "bower"));
        commands.add(new CommandImpl("build without tests", "mvn clean install -Dmaven.test.skip", "maven"));

        // attributes
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("test.attribute1", "test-value1");
        attributes.put("test.attribute2", "test-value2");
        attributes.put("test.attribute3", "test-value3");

        return WorkspaceImpl.builder()
                            .setId(generate("workspace", 16))
                            .setConfig(new WorkspaceConfigImpl("test-workspace-name",
                                                               "This is test workspace",
                                                               null,
                                                               commands,
                                                               projects,
                                                               environments))
                            .setNamespace("user123")
                            .build();
    }
}

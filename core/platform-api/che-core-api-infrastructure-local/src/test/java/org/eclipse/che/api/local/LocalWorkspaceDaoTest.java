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
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
        workspaceDao = new LocalWorkspaceDaoImpl(new LocalStorageFactory(storageRoot.toString()));
    }

    @Test
    public void testWorkspaceSerialization() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);
        workspaceDao.saveWorkspaces();

        assertEquals(GSON.toJson(singletonMap(workspace.getId(), workspace)), new String(readAllBytes(workspacesPath)));
    }

    @Test
    public void testWorkspaceDeserialization() throws Exception {
        final UsersWorkspaceImpl workspace = createWorkspace();
        write(workspacesPath, GSON.toJson(singletonMap(workspace.getId(), workspace)).getBytes());

        workspaceDao.loadWorkspaces();

        final UsersWorkspaceImpl result = workspaceDao.get(workspace.getId());
        assertEquals(result, workspace);
    }

    private static UsersWorkspaceImpl createWorkspace() {
        // environments
        final RecipeImpl recipe = new RecipeImpl();
        recipe.setType("dockerfile");
        recipe.setScript("FROM codenvy/jdk7\nCMD tail -f /dev/null");

        final MachineSourceImpl machineSource = new MachineSourceImpl("recipe", "recipe-url");
        final MachineConfigImpl machineCfg1 = new MachineConfigImpl(true,
                                                                    "dev-machine",
                                                                    "machine-type",
                                                                    machineSource,
                                                                    new LimitsImpl(512));
        final MachineConfigImpl machineCfg2 = new MachineConfigImpl(false,
                                                                    "non-dev-machine",
                                                                    "machine-type-2",
                                                                    machineSource,
                                                                    new LimitsImpl(2048));

        final EnvironmentImpl env1 = new EnvironmentImpl("my-environment", recipe, asList(machineCfg1, machineCfg2));
        final EnvironmentImpl env2 = new EnvironmentImpl("my-environment-2", recipe, singletonList(machineCfg1));

        final List<EnvironmentImpl> environments = new ArrayList<>();
        environments.add(env1);
        environments.add(env2);

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

        return UsersWorkspaceImpl.builder()
                                 .setId(generate("workspace", 16))
                                 .setName("test-workspace-name")
                                 .setDescription("This is test workspace")
                                 .setOwner("user123")
                                 .setAttributes(attributes)
                                 .setCommands(commands)
                                 .setProjects(projects)
                                 .setEnvironments(environments)
                                 .setDefaultEnv(env1.getName())
                                 .build();
    }
}

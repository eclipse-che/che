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
package org.eclipse.che.api.workspace.server;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests of {@link WorkspaceConfigJsonAdapter}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceConfigJsonAdapterTest {

    private static final String INVALID_CONFIGS_DIR_NAME = "invalid_configs";

    private WorkspaceConfigJsonAdapter configAdapter;

    @BeforeMethod
    private void setUp() throws Exception {
        configAdapter = new WorkspaceConfigJsonAdapter();
    }

    @Test
    public void testWorkspaceConfigAdaptationBasedOnDockerfileLocationSource() throws Exception {
        final String content = loadContent("ws_conf_machine_source_dockerfile_location.json");
        final JsonObject wsConfig = new JsonParser().parse(content).getAsJsonObject();
        configAdapter.adaptModifying(wsConfig);

        // The type of environments must be changed from array to map
        assertTrue(wsConfig.has("environments"), "contains environments object");
        assertTrue(wsConfig.get("environments").isJsonObject(), "environments is json object");

        // Environment must be moved out of the environment object
        final JsonObject environmentsObj = wsConfig.get("environments").getAsJsonObject();
        assertTrue(environmentsObj.has("dev-env"), "'dev-env' is present in environments list");
        assertTrue(environmentsObj.get("dev-env").isJsonObject(), "'dev-env' is json object");

        final JsonObject environmentObj = environmentsObj.get("dev-env").getAsJsonObject();
        // 'machineConfigs' -> 'machines'
        assertTrue(environmentObj.has("machines"), "'machines' are present in environment object");
        assertTrue(environmentObj.get("machines").isJsonObject(), "'machines' is json object");
        final JsonObject machinesObj = environmentObj.get("machines").getAsJsonObject();
        assertEquals(machinesObj.entrySet().size(), 1, "machines size");

        // check 'dev' machine
        assertTrue(machinesObj.has("dev"), "'machines' contains machine with name 'dev-machine'");
        assertTrue(machinesObj.get("dev").isJsonObject(), "dev machine is json object");
        final JsonObject devMachineObj = machinesObj.get("dev").getAsJsonObject();
        assertTrue(devMachineObj.has("servers"), "dev machine contains servers field");
        assertTrue(devMachineObj.get("servers").isJsonObject(), "dev machine servers is json object");
        final JsonObject devMachineServersObj = devMachineObj.get("servers").getAsJsonObject();
        assertTrue(devMachineServersObj.has("ref"), "contains servers with reference 'ref'");
        assertTrue(devMachineServersObj.get("ref").isJsonObject(), "server is json object");
        final JsonObject devMachineServerObj = devMachineServersObj.get("ref").getAsJsonObject();
        assertEquals(devMachineServerObj.get("port").getAsString(), "9090/udp");
        assertEquals(devMachineServerObj.get("protocol").getAsString(), "protocol");
        assertTrue(devMachineObj.has("agents"), "dev machine has agents");
        assertTrue(devMachineObj.has("attributes"), "dev machine has attributes");
        assertTrue(devMachineObj.get("attributes").isJsonObject(), "dev machine attributes is json object");
        final JsonObject attributes = devMachineObj.getAsJsonObject("attributes");
        assertTrue(attributes.has("memoryLimitBytes"), "has memory limit");
        assertEquals(attributes.get("memoryLimitBytes").getAsString(), "2147483648");

        // check environment recipe
        assertTrue(environmentObj.has("recipe"), "environment contains recipe");
        assertTrue(environmentObj.get("recipe").isJsonObject(), "environment recipe is json object");
        final JsonObject recipeObj = environmentObj.get("recipe").getAsJsonObject();
        assertEquals(recipeObj.get("type").getAsString(), "dockerfile");
        assertEquals(recipeObj.get("contentType").getAsString(), "text/x-dockerfile");
        assertEquals(recipeObj.get("location").getAsString(), "https://somewhere/Dockerfile");
    }

    @Test(dependsOnMethods = "testWorkspaceConfigAdaptationBasedOnDockerfileLocationSource")
    public void testAdaptionOfWorkspaceConfigWithSourceBasedOnDockerfileContent() throws Exception {
        final String content = loadContent("ws_conf_machine_source_dockerfile_content.json");
        final JsonObject wsConfig = new JsonParser().parse(content).getAsJsonObject();
        configAdapter.adaptModifying(wsConfig);

        // check environment recipe
        final JsonObject recipeObj = wsConfig.getAsJsonObject("environments")
                                             .getAsJsonObject("dev-env")
                                             .getAsJsonObject("recipe");
        assertEquals(recipeObj.get("type").getAsString(), "dockerfile");
        assertEquals(recipeObj.get("contentType").getAsString(), "text/x-dockerfile");
        assertEquals(recipeObj.get("content").getAsString(), "FROM codenvy/ubuntu_jdk8");
    }

    @Test(dependsOnMethods = "testWorkspaceConfigAdaptationBasedOnDockerfileLocationSource")
    public void testAdaptionOfWorkspaceConfigWithSourceBasedOnDockerImage() throws Exception {
        final String content = loadContent("ws_conf_machine_source_dockerimage.json");
        final JsonObject wsConfig = new JsonParser().parse(content).getAsJsonObject();
        configAdapter.adaptModifying(wsConfig);

        // check environment recipe
        final JsonObject recipeObj = wsConfig.getAsJsonObject("environments")
                                             .getAsJsonObject("dev-env")
                                             .getAsJsonObject("recipe");
        assertEquals(recipeObj.get("type").getAsString(), "dockerimage");
        assertEquals(recipeObj.get("location").getAsString(), "codenvy/ubuntu_jdk8");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "invalidConfigs")
    public void testNotValidWorkspaceConfigAdaptations(String filename) throws Exception {
        final String content = loadContent(INVALID_CONFIGS_DIR_NAME + File.separatorChar + filename);

        new WorkspaceConfigJsonAdapter().adaptModifying(new JsonParser().parse(content).getAsJsonObject());
    }

    @DataProvider
    public static Object[][] invalidConfigs() throws Exception {
        final URL dir = Thread.currentThread()
                              .getContextClassLoader()
                              .getResource(INVALID_CONFIGS_DIR_NAME);
        assertNotNull(dir);
        final File[] files = new File(dir.toURI()).listFiles();
        assertNotNull(files);
        final Object[][] result = new Object[files.length][1];
        for (int i = 0; i < files.length; i++) {
            result[i][0] = files[i].getName();
        }
        return result;
    }

    private static String loadContent(String filename) throws IOException {
        try (Reader r = new InputStreamReader(Thread.currentThread()
                                                    .getContextClassLoader()
                                                    .getResourceAsStream(filename))) {
            return CharStreams.toString(r);
        }
    }
}

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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.server.stack.StackJsonAdapter;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.api.workspace.shared.stack.StackSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test for {@link LocalStackDaoImpl}
 *
 * @author Alexander Andrienko
 */
public class LocalStackDaoTest {

    private static final String SVG_ICON = "<svg height=\"50\" width=\"500\">\n" +
                                           "  <ellipse cx=\"210\" cy=\"100\" rx=\"110\" ry=\"60\"\n" +
                                           "  style=\"fill:green;stroke:purple;stroke-width:2\" />\n" +
                                           "</svg>";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                      .create();

    private LocalStackDaoImpl stackDao;
    private Path              storageRoot;
    private Path              stackJsonPath;
    private Path              parentIconFolder;
    private Path              pathToIcon;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path targetDir = Paths.get(url.toURI()).getParent();
        storageRoot = targetDir.resolve("stack");
        stackJsonPath = storageRoot.resolve("stacks.json");
        parentIconFolder = storageRoot.resolve("images").resolve("stackdskhfdskf");
        pathToIcon = parentIconFolder.resolve("java-type.svg");
        StackJsonAdapter adapter = mock(StackJsonAdapter.class);
        stackDao = new LocalStackDaoImpl(new StackLocalStorage(storageRoot.toString(), adapter));
    }

    @AfterMethod
    public void cleanUp() throws IOException {
        FileUtils.deleteDirectory(storageRoot.toFile());
    }

    @Test
    public void createdStackShouldSerializationSuccessfully() throws Exception {
        createStackIcon();
        StackImpl stack = createStack();

        stackDao.create(stack);
        stackDao.saveStacks();

        assertEquals(GSON.toJson(ImmutableMap.of("stackdskhfdskf", stack)), new String(readAllBytes(stackJsonPath)));
        //check icon content
        assertEquals(SVG_ICON, new String(readAllBytes(pathToIcon)));
    }

    @Test
    public void testStackDeserialization() throws Exception {
        createStackIcon();
        StackImpl stack = createStack();
        write(stackJsonPath, GSON.toJson(ImmutableMap.of("stackdskhfdskf", stack)).getBytes());

        stackDao.start();

        Stack result = stackDao.getById("stackdskhfdskf");
        assertEquals(result, stack);
    }

    @Test
    public void testOldFormatIsAdaptedWhenStacksAreLoaded() throws Exception {
        final URL rootUrl = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(rootUrl);
        final String path = Paths.get(rootUrl.toURI()).toString();
        final StackJsonAdapter workspaceAdapter = new StackJsonAdapter(new WorkspaceConfigJsonAdapter());
        final StackLocalStorage storageFactory = new StackLocalStorage(path, workspaceAdapter);
        final LocalStackDaoImpl stackDao = new LocalStackDaoImpl(storageFactory);

        stackDao.start();

        final StackImpl test = stackDao.getById("test");
        final EnvironmentImpl environment = test.getWorkspaceConfig()
                                                .getEnvironments()
                                                .get(test.getWorkspaceConfig().getDefaultEnv());
        assertEquals(environment.getRecipe().getType(), "dockerimage");
        assertEquals(environment.getRecipe().getLocation(), "codenvy/ubuntu_jdk8");
        assertEquals(environment.getMachines().size(), 1);
    }

    private void createStackIcon() throws IOException {
        Files.createDirectories(parentIconFolder);
        Files.write(pathToIcon, SVG_ICON.getBytes());
    }

    private static StackImpl createStack() {
        StackComponentImpl javaComponent = new StackComponentImpl("java", "1.8.45");
        StackComponentImpl mavenComponent = new StackComponentImpl("maven", "3.3.9");

        StackSource stackSource = new StackSourceImpl("image", "codenvy/ubuntu_jdk8");

        Map<String, List<String>> users = new HashMap<>();
        users.put("user", asList("read", "write"));

        StackIcon stackIcon = new StackIcon("java-type.svg", "image/svg+xml", SVG_ICON.getBytes());
        List<StackComponentImpl> components = asList(javaComponent, mavenComponent);
        return StackImpl.builder().setId("stackdskhfdskf")
                        .setName("Java-default")
                        .setDescription("description")
                        .setScope("general")
                        .setCreator("User")
                        .setTags(asList("java", "maven"))
                        .setSource(stackSource)
                        .setComponents(components)
                        .setStackIcon(stackIcon)
                        .build();
    }
}

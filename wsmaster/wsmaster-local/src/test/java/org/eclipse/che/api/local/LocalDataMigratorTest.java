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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.server.stack.StackJsonAdapter;
import org.eclipse.che.commons.lang.IoUtil;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link LocalDataMigrator}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class LocalDataMigratorTest {

    private Path                baseDir;
    private LocalStorageFactory factory;

    @Mock
    private UserDao userDao;

    @Mock
    private ProfileDao profileDao;

    @Mock
    private PreferenceDao preferenceDao;

    @Mock
    private SshDao sshDao;

    @Mock
    private WorkspaceDao workspaceDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private RecipeDao recipeDao;

    @Mock
    private StackDao stackDao;

    @Mock
    private StackJsonAdapter stackJsonAdapter;

    @Mock
    private WorkspaceConfigJsonAdapter workspaceCfgJsonAdapter;

    private LocalDataMigrator dataMigrator;

    @BeforeMethod
    private void setUp() throws Exception {
        baseDir = Files.createTempDirectory(Paths.get("/tmp"), "test");
        factory = new LocalStorageFactory(baseDir.toString());
        dataMigrator = new LocalDataMigrator();
        storeTestData();
        doThrow(new NotFoundException("not-found")).when(userDao).getById(anyString());
        // needed by workspace
        when(userDao.getByName(anyString())).thenReturn(new UserImpl("id", "email", "name"));
        doThrow(new NotFoundException("not-found")).when(profileDao).getById(anyString());
        doReturn(emptyMap()).when(preferenceDao).getPreferences(anyString());
        doThrow(new NotFoundException("not-found")).when(sshDao).get(anyString(), anyString(), anyString());
        doThrow(new NotFoundException("not-found")).when(workspaceDao).get(anyString());
        doThrow(new NotFoundException("not-found")).when(snapshotDao).getSnapshot(anyString());
        doThrow(new NotFoundException("not-found")).when(recipeDao).getById(anyString());
        doThrow(new NotFoundException("not-found")).when(stackDao).getById(anyString());
    }

    @AfterMethod
    private void cleanUp() {
        IoUtil.deleteRecursive(baseDir.toFile());
    }

    @Test(dataProvider = "successfulMigrationAttempts")
    public void shouldMigrateLocalData(String fileName, TestAction verification) throws Exception {
        dataMigrator.performMigration(baseDir.toString(),
                                      userDao,
                                      profileDao,
                                      preferenceDao,
                                      sshDao,
                                      workspaceDao,
                                      snapshotDao,
                                      recipeDao,
                                      stackDao,
                                      stackJsonAdapter,
                                      workspaceCfgJsonAdapter);
        verification.perform();

        assertFalse(Files.exists(baseDir.resolve(fileName)));
        assertTrue(Files.exists(baseDir.resolve(fileName + ".backup")));
    }

    @Test(expectedExceptions = Exception.class, dataProvider = "failOnMigrateAttempts")
    public void shouldFailIfMigrationOfAnyOfEntitiesFailed(TestAction failOnMigrate) throws Exception {
        failOnMigrate.perform();

        dataMigrator.performMigration(baseDir.toString(),
                                      userDao,
                                      profileDao,
                                      preferenceDao,
                                      sshDao,
                                      workspaceDao,
                                      snapshotDao,
                                      recipeDao,
                                      stackDao,
                                      stackJsonAdapter,
                                      workspaceCfgJsonAdapter);
    }

    @DataProvider(name = "successfulMigrationAttempts")
    public Object[][] successfulMigrationAttempts() {
        return new Object[][] {
                {
                        LocalUserDaoImpl.FILENAME,
                        (TestAction)() -> verify(userDao).create(any())
                },
                {
                        LocalProfileDaoImpl.FILENAME,
                        (TestAction)() -> verify(profileDao).create(any())
                },
                {
                        LocalPreferenceDaoImpl.FILENAME,
                        (TestAction)() -> verify(preferenceDao).setPreferences(anyString(), any())
                },
                {
                        LocalSshDaoImpl.FILENAME,
                        (TestAction)() -> verify(sshDao).create(any())
                },
                {
                        LocalWorkspaceDaoImpl.FILENAME,
                        (TestAction)() -> verify(workspaceDao).create(any())
                },
                {
                        LocalSnapshotDaoImpl.FILENAME,
                        (TestAction)() -> verify(snapshotDao).saveSnapshot(any())
                },
                {
                        LocalRecipeDaoImpl.FILENAME,
                        (TestAction)() -> verify(recipeDao).create(any())
                },
                {
                        StackLocalStorage.STACK_STORAGE_FILE,
                        (TestAction)() -> verify(stackDao).create(any())
                }
        };
    }

    @DataProvider(name = "failOnMigrateAttempts")
    public Object[][] failOnMigrateAttempts() {
        return new TestAction[][] {
                {() -> doThrow(new ServerException("fail")).when(userDao).create(any())},
                {() -> doThrow(new ServerException("fail")).when(profileDao).create(any())},
                {() -> doThrow(new ServerException("fail")).when(preferenceDao).setPreferences(anyString(), any())},
                {() -> doThrow(new ServerException("fail")).when(sshDao).create(any())},
                {() -> doThrow(new ServerException("fail")).when(workspaceDao).create(any())},
                {() -> doThrow(new ServerException("fail")).when(snapshotDao).saveSnapshot(any())},
                {() -> doThrow(new ServerException("fail")).when(recipeDao).create(any())},
                {() -> doThrow(new ServerException("fail")).when(stackDao).create(any())}
        };
    }

    private void storeTestData() throws Exception {
        final UserImpl user = new UserImpl("id", "email", "name");
        final ProfileImpl profile = new ProfileImpl(user.getId());
        final Map<String, String> prefs = singletonMap("key", "value");
        final SshPairImpl sshPair = new SshPairImpl(user.getId(), "service", "name", "public", "private");
        final WorkspaceImpl workspace = new WorkspaceImpl("id", user.getAccount(), new WorkspaceConfigImpl("name",
                                                                                                           "description",
                                                                                                           "env",
                                                                                                           emptyList(),
                                                                                                           emptyList(),
                                                                                                           emptyMap()));
        final SnapshotImpl snapshot = new SnapshotImpl();
        snapshot.setId("snapshotId");
        snapshot.setWorkspaceId(workspace.getId());
        final RecipeImpl recipe = new RecipeImpl();
        recipe.setId("id");
        recipe.setCreator(user.getId());
        final StackImpl stack = new StackImpl();
        stack.setId("id");
        stack.setName("name");
        factory.create(LocalUserDaoImpl.FILENAME).store(singletonMap(user.getId(), user));
        factory.create(LocalProfileDaoImpl.FILENAME).store(singletonMap(profile.getUserId(), profile));
        factory.create(LocalPreferenceDaoImpl.FILENAME).store(singletonMap(user.getId(), prefs));
        factory.create(LocalSshDaoImpl.FILENAME, singletonMap(SshPairImpl.class, new SshSerializer()))
               .store(singletonMap(sshPair.getOwner(), singletonList(sshPair)));
        factory.create(LocalWorkspaceDaoImpl.FILENAME, singletonMap(WorkspaceImpl.class, new WorkspaceSerializer()))
               .store(singletonMap(workspace.getId(), workspace));
        factory.create(LocalSnapshotDaoImpl.FILENAME).store(singletonMap(snapshot.getId(), snapshot));
        factory.create(LocalRecipeDaoImpl.FILENAME).store(singletonMap(recipe.getId(), recipe));
        factory.create(StackLocalStorage.STACK_STORAGE_FILE).store(singletonMap(stack.getId(), stack));
    }

    @FunctionalInterface
    private interface TestAction {
        void perform() throws Exception;
    }

    public static class WorkspaceSerializer implements JsonSerializer<WorkspaceImpl> {

        @Override
        public JsonElement serialize(WorkspaceImpl src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement result = new Gson().toJsonTree(src, WorkspaceImpl.class);
            result.getAsJsonObject().addProperty("namespace", src.getNamespace());
            result.getAsJsonObject().remove("account");
            return result;
        }
    }

    public static class SshSerializer implements JsonSerializer<SshPairImpl> {

        @Override
        public JsonElement serialize(SshPairImpl sshPair, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            result.add("service", new JsonPrimitive(sshPair.getService()));
            result.add("name", new JsonPrimitive(sshPair.getName()));
            result.add("privateKey", new JsonPrimitive(sshPair.getPublicKey()));
            result.add("publicKey", new JsonPrimitive(sshPair.getPrivateKey()));
            return result;
        }
    }
}

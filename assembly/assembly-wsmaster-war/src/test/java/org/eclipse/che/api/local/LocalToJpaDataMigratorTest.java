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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.local.storage.stack.StackLocalStorage;
import org.eclipse.che.api.machine.server.jpa.MachineJpaModule;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.server.stack.StackJsonAdapter;
import org.eclipse.che.commons.lang.IoUtil;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.TestObjectsFactory.createPreferences;
import static org.eclipse.che.api.TestObjectsFactory.createProfile;
import static org.eclipse.che.api.TestObjectsFactory.createRecipe;
import static org.eclipse.che.api.TestObjectsFactory.createSnapshot;
import static org.eclipse.che.api.TestObjectsFactory.createSshPair;
import static org.eclipse.che.api.TestObjectsFactory.createStack;
import static org.eclipse.che.api.TestObjectsFactory.createUser;
import static org.eclipse.che.api.TestObjectsFactory.createWorkspace;

/**
 * Tests migration from local json based storage to jpa.
 *
 * @author Yevhenii Voevodin
 */
public class LocalToJpaDataMigratorTest {

    private Injector          injector;
    private LocalDataMigrator migrator;
    private Path              workingDir;

    private UserDao       userDao;
    private ProfileDao    profileDao;
    private PreferenceDao preferenceDao;
    private WorkspaceDao  workspaceDao;
    private SnapshotDao   snapshotDao;
    private SshDao        sshDao;
    private RecipeDao     recipeDao;
    private StackDao      stackDao;

    private LocalStorageFactory        factory;
    private StackJsonAdapter           stackJsonAdapter;
    private WorkspaceConfigJsonAdapter workspaceCfgJsonAdapter;

    @BeforeMethod
    private void setUp() throws Exception {
        workingDir = Files.createTempDirectory(Paths.get("/tmp"), "test");
        factory = new LocalStorageFactory(workingDir.toString());
        injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named("che.database")).to(workingDir.toString());

                bind(JpaInitializer.class).asEagerSingleton();
                bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
                install(new JpaPersistModule("test"));
                install(new UserJpaModule());
                install(new SshJpaModule());
                install(new WorkspaceJpaModule());
                install(new MachineJpaModule());
                bind(StackJsonAdapter.class);
                bind(WorkspaceManager.class).toInstance(Mockito.mock(WorkspaceManager.class));
            }
        });

        userDao = injector.getInstance(UserDao.class);
        preferenceDao = injector.getInstance(PreferenceDao.class);
        profileDao = injector.getInstance(ProfileDao.class);
        sshDao = injector.getInstance(SshDao.class);
        snapshotDao = injector.getInstance(SnapshotDao.class);
        workspaceDao = injector.getInstance(WorkspaceDao.class);
        recipeDao = injector.getInstance(RecipeDao.class);
        stackDao = injector.getInstance(StackDao.class);

        stackJsonAdapter = injector.getInstance(StackJsonAdapter.class);
        workspaceCfgJsonAdapter = injector.getInstance(WorkspaceConfigJsonAdapter.class);

        migrator = new LocalDataMigrator();
        storeTestData();
    }

    @AfterMethod
    public void cleanup() {
        IoUtil.deleteRecursive(workingDir.toFile());
        injector.getInstance(EntityManagerFactory.class).close();
    }

    @Test
    public void shouldSuccessfullyPerformMigration() throws Exception {
        migrator.performMigration(workingDir.toString(),
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

    @Test(expectedExceptions = Exception.class, dataProvider = "failFilenames")
    public void shouldFailIfEntitiesAreInconsistent(String filename) throws Exception {
        Files.delete(workingDir.resolve(filename));
        migrator.performMigration(workingDir.toString(),
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

    @DataProvider(name = "failFilenames")
    private Object[][] failFilenames() {
        return new String[][] {
                {LocalUserDaoImpl.FILENAME},
                {LocalWorkspaceDaoImpl.FILENAME}
        };
    }

    private void storeTestData() throws Exception {
        final UserImpl user = createUser("user123");
        final ProfileImpl profile = createProfile(user.getId());
        final Map<String, String> preferences = createPreferences();
        final SshPairImpl pair = createSshPair(user.getId(), "service", "name");
        final WorkspaceImpl workspace = createWorkspace("id", user.getAccount());
        final SnapshotImpl snapshot = createSnapshot("snapshot123", workspace.getId());
        final RecipeImpl recipe = createRecipe("recipe123");
        final StackImpl stack = createStack("stack123", "stack-name");

        factory.create(LocalUserDaoImpl.FILENAME).store(singletonMap(user.getId(), user));
        factory.create(LocalProfileDaoImpl.FILENAME).store(singletonMap(profile.getUserId(), profile));
        factory.create(LocalPreferenceDaoImpl.FILENAME).store(singletonMap(user.getId(), preferences));
        factory.create(LocalSshDaoImpl.FILENAME, singletonMap(SshPairImpl.class, new SshSerializer()))
               .store(singletonMap(pair.getOwner(), singletonList(pair)));
        factory.create(LocalWorkspaceDaoImpl.FILENAME, singletonMap(WorkspaceImpl.class, new WorkspaceSerializer()))
               .store(singletonMap(workspace.getId(), workspace));
        factory.create(LocalSnapshotDaoImpl.FILENAME).store(singletonMap(snapshot.getId(), snapshot));
        factory.create(LocalRecipeDaoImpl.FILENAME).store(singletonMap(recipe.getId(), recipe));
        factory.create(StackLocalStorage.STACK_STORAGE_FILE).store(singletonMap(stack.getId(), stack));
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

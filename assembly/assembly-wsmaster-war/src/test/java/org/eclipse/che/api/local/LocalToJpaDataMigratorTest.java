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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.core.notification.EventService;
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
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.persistence.internal.jpa.ExceptionFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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

    private LocalUserDaoImpl       localUserDao;
    private LocalProfileDaoImpl    localProfileDao;
    private LocalPreferenceDaoImpl localPreferenceDao;
    private LocalWorkspaceDaoImpl  localWorkspaceDao;
    private LocalSnapshotDaoImpl   localSnapshotDao;
    private LocalSshDaoImpl        localSshDao;
    private LocalRecipeDaoImpl     localRecipeDao;
    private LocalStackDaoImpl      localStackDao;

    @BeforeMethod
    private void setUp() throws Exception {
        workingDir = Files.createTempDirectory(Paths.get("/tmp"), "test");
        injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named("che.conf.storage")).to(workingDir.toString());

                bind(JpaInitializer.class).asEagerSingleton();
                install(new JpaPersistModule("test"));
                install(new UserJpaModule());
                install(new SshJpaModule());
                install(new WorkspaceJpaModule());
                install(new MachineJpaModule());
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
        localUserDao = injector.getInstance(LocalUserDaoImpl.class);
        localProfileDao = injector.getInstance(LocalProfileDaoImpl.class);
        localPreferenceDao = injector.getInstance(LocalPreferenceDaoImpl.class);
        localWorkspaceDao = injector.getInstance(LocalWorkspaceDaoImpl.class);
        localSnapshotDao = injector.getInstance(LocalSnapshotDaoImpl.class);
        localSshDao = injector.getInstance(LocalSshDaoImpl.class);
        localRecipeDao = injector.getInstance(LocalRecipeDaoImpl.class);
        localStackDao = injector.getInstance(LocalStackDaoImpl.class);

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
                                  stackDao);
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
                                  stackDao);
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
        localUserDao.create(user);
        localUserDao.saveUsers();

        final ProfileImpl profile = createProfile(user.getId());
        localProfileDao.create(profile);
        localProfileDao.saveProfiles();

        final Map<String, String> preferences = createPreferences();
        localPreferenceDao.setPreferences(user.getId(), preferences);
        localPreferenceDao.savePreferences();

        final SshPairImpl pair = createSshPair(user.getId(), "service", "name");
        localSshDao.create(pair);
        localSshDao.saveSshPairs();

        final WorkspaceImpl workspace = createWorkspace("id", user.getAccount());
        localWorkspaceDao.create(workspace);
        localWorkspaceDao.saveWorkspaces();

        final SnapshotImpl snapshot = createSnapshot("snapshot123", workspace.getId());
        localSnapshotDao.saveSnapshot(snapshot);
        localSnapshotDao.saveSnapshots();

        final RecipeImpl recipe = createRecipe("recipe123");
        localRecipeDao.create(recipe);
        localRecipeDao.saveRecipes();

        final StackImpl stack = createStack("stack123", "stack-name");
        localStackDao.create(stack);
        localStackDao.saveStacks();
    }
}

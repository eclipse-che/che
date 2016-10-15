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
package org.eclipse.che.api.jdbc.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.api.AccountModule;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.event.CascadeRemovalEvent;
import org.eclipse.che.api.core.jdbc.jpa.event.CascadeRemovalEventSubscriber;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.factory.server.jpa.FactoryJpaModule;
import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao.RemoveFactoriesBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.jpa.MachineJpaModule;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.ssh.server.jpa.JpaSshDao.RemoveSshKeysBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao.RemovePreferencesBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao.RemoveProfileBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao.RemoveSnapshotsBeforeWorkspaceRemovedEventSubscriber;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao.RemoveWorkspaceBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.TestObjectsFactory.createFactory;
import static org.eclipse.che.api.TestObjectsFactory.createPreferences;
import static org.eclipse.che.api.TestObjectsFactory.createProfile;
import static org.eclipse.che.api.TestObjectsFactory.createSnapshot;
import static org.eclipse.che.api.TestObjectsFactory.createSshPair;
import static org.eclipse.che.api.TestObjectsFactory.createUser;
import static org.eclipse.che.api.TestObjectsFactory.createWorkspace;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests top-level entities cascade removals.
 *
 * @author Yevhenii Voevodin
 */
public class JpaEntitiesCascadeRemovalTest {

    private Injector      injector;
    private EventService  eventService;
    private PreferenceDao preferenceDao;
    private UserDao       userDao;
    private ProfileDao    profileDao;
    private WorkspaceDao  workspaceDao;
    private SnapshotDao   snapshotDao;
    private SshDao        sshDao;
    private FactoryDao    factoryDao;

    /** User is a root of dependency tree. */
    private UserImpl user;

    /** Profile depends on user. */
    private ProfileImpl profile;

    /** Preferences depend on user. */
    private Map<String, String> preferences;

    /** Workspaces depend on user. */
    private WorkspaceImpl workspace1;
    private WorkspaceImpl workspace2;

    /** SshPairs depend on user. */
    private SshPairImpl sshPair1;
    private SshPairImpl sshPair2;

    /** Factories depend on user. */
    private FactoryImpl factory1;
    private FactoryImpl factory2;

    /** Snapshots depend on workspace. */
    private SnapshotImpl snapshot1;
    private SnapshotImpl snapshot2;
    private SnapshotImpl snapshot3;
    private SnapshotImpl snapshot4;

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                bind(EventService.class).in(Singleton.class);

                bind(JpaInitializer.class).asEagerSingleton();
                bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
                install(new InitModule(PostConstruct.class));
                install(new JpaPersistModule("test"));
                install(new UserJpaModule());
                install(new AccountModule());
                install(new SshJpaModule());
                install(new WorkspaceJpaModule());
                install(new MachineJpaModule());
                install(new FactoryJpaModule());
                bind(WorkspaceManager.class);
                final WorkspaceRuntimes wR = Mockito.mock(WorkspaceRuntimes.class);
                when(wR.hasRuntime(Mockito.anyString())).thenReturn(false);
                bind(WorkspaceRuntimes.class).toInstance(wR);
                bind(AccountManager.class);
                bind(Boolean.class).annotatedWith(Names.named("workspace.runtime.auto_snapshot")).toInstance(false);
                bind(Boolean.class).annotatedWith(Names.named("workspace.runtime.auto_restore")).toInstance(false);
            }
        });

        eventService = injector.getInstance(EventService.class);
        userDao = injector.getInstance(UserDao.class);
        preferenceDao = injector.getInstance(PreferenceDao.class);
        profileDao = injector.getInstance(ProfileDao.class);
        sshDao = injector.getInstance(SshDao.class);
        snapshotDao = injector.getInstance(SnapshotDao.class);
        workspaceDao = injector.getInstance(WorkspaceDao.class);
        factoryDao = injector.getInstance(FactoryDao.class);
    }

    @AfterMethod
    public void cleanup() {
        injector.getInstance(EntityManagerFactory.class).close();
    }

    @Test
    public void shouldDeleteAllTheEntitiesWhenUserIsDeleted() throws Exception {
        createTestData();

        // Remove the user, all entries must be removed along with the user
        userDao.remove(user.getId());

        // Check all the entities are removed
        assertNull(notFoundToNull(() -> userDao.getById(user.getId())));
        assertNull(notFoundToNull(() -> profileDao.getById(user.getId())));
        assertTrue(preferenceDao.getPreferences(user.getId()).isEmpty());
        assertTrue(sshDao.get(user.getId()).isEmpty());
        assertTrue(workspaceDao.getByNamespace(user.getId()).isEmpty());
        assertTrue(factoryDao.getByAttribute(0, 0, singletonList(Pair.of("creator.userId", user.getId()))).isEmpty());
        assertTrue(snapshotDao.findSnapshots(workspace1.getId()).isEmpty());
        assertTrue(snapshotDao.findSnapshots(workspace2.getId()).isEmpty());
    }

    @Test(dataProvider = "beforeRemoveRollbackActions")
    public void shouldRollbackTransactionWhenFailedToRemoveAnyOfEntries(
            Class<CascadeRemovalEventSubscriber<CascadeRemovalEvent>> subscriberClass,
            Class<CascadeRemovalEvent> eventClass) throws Exception {
        createTestData();
        eventService.unsubscribe(injector.getInstance(subscriberClass), eventClass);

        // Remove the user, all entries must be rolled back after fail
        try {
            userDao.remove(user.getId());
            fail("UserDao#remove had to throw exception");
        } catch (Exception ignored) {
        }

        // Check all the data rolled back
        assertNotNull(userDao.getById(user.getId()));
        assertNotNull(profileDao.getById(user.getId()));
        assertFalse(preferenceDao.getPreferences(user.getId()).isEmpty());
        assertFalse(sshDao.get(user.getId()).isEmpty());
        assertFalse(workspaceDao.getByNamespace(user.getName()).isEmpty());
        assertFalse(factoryDao.getByAttribute(0, 0, singletonList(Pair.of("creator.userId", user.getId()))).isEmpty());
        assertFalse(snapshotDao.findSnapshots(workspace1.getId()).isEmpty());
        assertFalse(snapshotDao.findSnapshots(workspace2.getId()).isEmpty());
        wipeTestData();
    }

    @DataProvider(name = "beforeRemoveRollbackActions")
    public Object[][] beforeRemoveActions() {
        return new Class[][] {
                {RemovePreferencesBeforeUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class},
                {RemoveProfileBeforeUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class},
                {RemoveWorkspaceBeforeAccountRemovedEventSubscriber.class, BeforeAccountRemovedEvent.class},
                {RemoveSnapshotsBeforeWorkspaceRemovedEventSubscriber.class, BeforeWorkspaceRemovedEvent.class},
                {RemoveSshKeysBeforeUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class},
                {RemoveFactoriesBeforeUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class}
        };
    }

    private void createTestData() throws ConflictException, ServerException {
        userDao.create(user = createUser("bobby"));

        profileDao.create(profile = createProfile(user.getId()));

        preferenceDao.setPreferences(user.getId(), preferences = createPreferences());

        workspaceDao.create(workspace1 = createWorkspace("workspace1", user.getAccount()));
        workspaceDao.create(workspace2 = createWorkspace("workspace2", user.getAccount()));

        sshDao.create(sshPair1 = createSshPair(user.getId(), "service", "name1"));
        sshDao.create(sshPair2 = createSshPair(user.getId(), "service", "name2"));

        factoryDao.create(factory1 = createFactory("factory1", user.getId()));
        factoryDao.create(factory2 = createFactory("factory2", user.getId()));

        snapshotDao.saveSnapshot(snapshot1 = createSnapshot("snapshot1", workspace1.getId()));
        snapshotDao.saveSnapshot(snapshot2 = createSnapshot("snapshot2", workspace1.getId()));
        snapshotDao.saveSnapshot(snapshot3 = createSnapshot("snapshot3", workspace2.getId()));
        snapshotDao.saveSnapshot(snapshot4 = createSnapshot("snapshot4", workspace2.getId()));
    }

    private void wipeTestData() throws ConflictException, ServerException, NotFoundException {
        snapshotDao.removeSnapshot(snapshot1.getId());
        snapshotDao.removeSnapshot(snapshot2.getId());
        snapshotDao.removeSnapshot(snapshot3.getId());
        snapshotDao.removeSnapshot(snapshot4.getId());

        factoryDao.remove(factory1.getId());
        factoryDao.remove(factory2.getId());

        sshDao.remove(sshPair1.getOwner(), sshPair1.getService(), sshPair1.getName());
        sshDao.remove(sshPair2.getOwner(), sshPair2.getService(), sshPair2.getName());

        workspaceDao.remove(workspace1.getId());
        workspaceDao.remove(workspace2.getId());

        preferenceDao.remove(user.getId());

        profileDao.remove(user.getId());

        userDao.remove(user.getId());
    }

    private static <T> T notFoundToNull(Callable<T> action) throws Exception {
        try {
            return action.call();
        } catch (NotFoundException x) {
            return null;
        }
    }
}

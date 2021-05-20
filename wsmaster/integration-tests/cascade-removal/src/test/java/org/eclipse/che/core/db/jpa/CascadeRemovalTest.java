/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db.jpa;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createAccount;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createK8sRuntimeState;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createPreferences;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createProfile;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createSshPair;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createUser;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createWorkspaceWithConfig;
import static org.eclipse.che.core.db.jpa.TestObjectsFactory.createWorkspaceWithDevfile;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.api.AccountModule;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.ssh.server.jpa.JpaSshDao.RemoveSshKeysBeforeUserRemovedEventSubscriber;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.jpa.PreferenceEntity;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivity;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.inject.WorkspaceActivityModule;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceLockService;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.devfile.SerializableConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.DevfileConverter;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao.RemoveWorkspaceBeforeAccountRemovedEventSubscriber;
import org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.eclipse.che.commons.observability.NoopExecutorServiceWrapper;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.cascade.event.CascadeEvent;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.BeforeKubernetesRuntimeStateRemovedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeCacheModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeCommandImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.h2.Driver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests top-level entities cascade removals.
 *
 * @author Yevhenii Voevodin
 */
public class CascadeRemovalTest {

  private Injector injector;
  private EventService eventService;
  private AccountDao accountDao;
  private PreferenceDao preferenceDao;
  private UserDao userDao;
  private ProfileDao profileDao;
  private WorkspaceDao workspaceDao;
  private SshDao sshDao;
  private WorkspaceActivityDao workspaceActivityDao;
  private KubernetesRuntimeStateCache k8sRuntimes;
  private KubernetesMachineCache k8sMachines;

  /** Account and User are a root of dependency tree. */
  private AccountImpl account;

  private UserImpl user;

  private UserManager userManager;
  private AccountManager accountManager;

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

  private KubernetesRuntimeState k8sRuntimeState;
  private KubernetesRuntimeState k8sRuntimeState2;

  private H2DBTestServer server;

  @BeforeMethod
  public void setUp() throws Exception {
    server = H2DBTestServer.startDefault();
    injector =
        Guice.createInjector(
            Stage.PRODUCTION,
            new AbstractModule() {
              @Override
              protected void configure() {
                install(
                    new PersistTestModuleBuilder()
                        .setDriver(Driver.class)
                        .runningOn(server)
                        .addEntityClasses(
                            AccountImpl.class,
                            UserImpl.class,
                            ProfileImpl.class,
                            PreferenceEntity.class,
                            WorkspaceImpl.class,
                            WorkspaceConfigImpl.class,
                            WorkspaceActivity.class,
                            ProjectConfigImpl.class,
                            EnvironmentImpl.class,
                            MachineConfigImpl.class,
                            SourceStorageImpl.class,
                            ServerConfigImpl.class,
                            CommandImpl.class,
                            RecipeImpl.class,
                            SshPairImpl.class,
                            VolumeImpl.class,
                            ActionImpl.class,
                            org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl
                                .class,
                            ComponentImpl.class,
                            DevfileImpl.class,
                            EndpointImpl.class,
                            EntrypointImpl.class,
                            EnvImpl.class,
                            ProjectImpl.class,
                            SourceImpl.class,
                            org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl
                                .class,
                            KubernetesRuntimeState.class,
                            KubernetesRuntimeCommandImpl.class,
                            KubernetesMachineImpl.class,
                            KubernetesMachineImpl.MachineId.class,
                            KubernetesServerImpl.class,
                            KubernetesServerImpl.ServerId.class)
                        .addEntityClass(
                            "org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
                        .addClass(SerializableConverter.class)
                        .setExceptionHandler(H2ExceptionHandler.class)
                        .build());
                bind(EventService.class).in(Singleton.class);
                install(new InitModule(PostConstruct.class));
                bind(SchemaInitializer.class)
                    .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
                bind(DBInitializer.class).asEagerSingleton();

                bind(String[].class)
                    .annotatedWith(Names.named("che.auth.reserved_user_names"))
                    .toInstance(new String[0]);

                bind(Long.class)
                    .annotatedWith(Names.named("che.limits.workspace.idle.timeout"))
                    .toInstance(100000L);

                bind(Long.class)
                    .annotatedWith(Names.named("che.limits.workspace.run.timeout"))
                    .toInstance(0L);

                bind(UserManager.class);
                bind(AccountManager.class);

                install(new UserJpaModule());
                install(new AccountModule());
                install(new SshJpaModule());
                install(new WorkspaceJpaModule());
                install(new WorkspaceActivityModule());
                install(new JpaKubernetesRuntimeCacheModule());
                bind(ExecutorServiceWrapper.class).to(NoopExecutorServiceWrapper.class);
                // initialize empty binder
                Multibinder.newSetBinder(binder(), WorkspaceAttributeValidator.class);
                bind(WorkspaceManager.class);

                RuntimeInfrastructure infra = mock(RuntimeInfrastructure.class);
                doReturn(emptySet()).when(infra).getRecipeTypes();
                bind(RuntimeInfrastructure.class).toInstance(infra);

                WorkspaceRuntimes wR =
                    spy(
                        new WorkspaceRuntimes(
                            mock(EventService.class),
                            emptyMap(),
                            infra,
                            mock(WorkspaceSharedPool.class),
                            mock(WorkspaceDao.class),
                            mock(DBInitializer.class),
                            mock(ProbeScheduler.class),
                            new DefaultWorkspaceStatusCache(),
                            new DefaultWorkspaceLockService(),
                            mock(DevfileConverter.class)));
                when(wR.hasRuntime(anyString())).thenReturn(false);
                bind(WorkspaceRuntimes.class).toInstance(wR);
                bind(AccountManager.class);
                bind(WorkspaceSharedPool.class)
                    .toInstance(
                        new WorkspaceSharedPool(
                            "cached", null, null, new NoopExecutorServiceWrapper()));

                MapBinder.newMapBinder(binder(), String.class, ComponentIntegrityValidator.class)
                    .addBinding("kubernetes")
                    .toInstance(mock(ComponentIntegrityValidator.class));
              }
            });

    eventService = injector.getInstance(EventService.class);
    accountDao = injector.getInstance(AccountDao.class);
    userDao = injector.getInstance(UserDao.class);
    userManager = injector.getInstance(UserManager.class);
    accountManager = injector.getInstance(AccountManager.class);
    preferenceDao = injector.getInstance(PreferenceDao.class);
    profileDao = injector.getInstance(ProfileDao.class);
    sshDao = injector.getInstance(SshDao.class);
    workspaceDao = injector.getInstance(WorkspaceDao.class);
    workspaceActivityDao = injector.getInstance(WorkspaceActivityDao.class);
    k8sRuntimes = injector.getInstance(KubernetesRuntimeStateCache.class);
    k8sMachines = injector.getInstance(KubernetesMachineCache.class);
  }

  @AfterMethod
  public void cleanup() {
    injector.getInstance(EntityManagerFactory.class).close();
    server.shutdown();
  }

  @Test
  public void shouldDeleteAllTheEntitiesWhenUserAndAccountIsDeleted() throws Exception {
    createTestData();

    // Remove the user, all entries must be removed along with the user
    accountManager.remove(account.getId());
    userManager.remove(user.getId());

    // Check all the entities are removed
    assertNull(notFoundToNull(() -> userDao.getById(user.getId())));
    assertNull(notFoundToNull(() -> profileDao.getById(user.getId())));
    assertTrue(preferenceDao.getPreferences(user.getId()).isEmpty());
    assertTrue(sshDao.get(user.getId()).isEmpty());
    assertTrue(workspaceDao.getByNamespace(user.getName(), 30, 0).isEmpty());
    assertNull(notFoundToNull(() -> workspaceActivityDao.findActivity(workspace1.getId())));
    assertNull(notFoundToNull(() -> workspaceActivityDao.findActivity(workspace2.getId())));
  }

  @Test(dataProvider = "beforeUserRemoveRollbackActions")
  public void shouldRollbackTransactionWhenFailedToRemoveAnyOfEntriesDuringUserRemoving(
      Class<CascadeEventSubscriber<CascadeEvent>> subscriberClass, Class<CascadeEvent> eventClass)
      throws Exception {
    createTestData();
    eventService.unsubscribe(injector.getInstance(subscriberClass), eventClass);

    // Remove the user, all entries must be rolled back after fail
    try {
      userManager.remove(user.getId());
      fail("UserManager#remove has to throw exception");
    } catch (Exception ignored) {
    }

    // Check all the data rolled back
    assertNotNull(userDao.getById(user.getId()));
    assertNotNull(profileDao.getById(user.getId()));
    assertFalse(preferenceDao.getPreferences(user.getId()).isEmpty());
    assertFalse(sshDao.get(user.getId()).isEmpty());
    wipeTestData();
  }

  @DataProvider(name = "beforeUserRemoveRollbackActions")
  public Object[][] beforeUserRemoveActions() {
    return new Class[][] {
      {RemoveSshKeysBeforeUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class}
    };
  }

  @Test
  public void shouldRollbackTransactionWhenFailedToRemoveMachinesDuringRuntimeRemoving()
      throws Exception {
    createTestData();
    JpaKubernetesMachineCache.RemoveKubernetesMachinesBeforeRuntimesRemoved instance =
        injector.getInstance(
            JpaKubernetesMachineCache.RemoveKubernetesMachinesBeforeRuntimesRemoved.class);
    eventService.unsubscribe(instance, BeforeKubernetesRuntimeStateRemovedEvent.class);
    // Remove the user, all entries must be rolled back after fail
    try {
      k8sRuntimes.remove(k8sRuntimeState.getRuntimeId());
      fail("k8sRuntimes#remove had to throw exception");
    } catch (Exception ignored) {
    }
    try {
      assertNotNull(k8sRuntimes.get(k8sRuntimeState.getRuntimeId()));
    } finally {
      eventService.subscribe(instance, BeforeKubernetesRuntimeStateRemovedEvent.class);
      wipeTestData();
    }
  }

  @Test(dataProvider = "beforeAccountRemoveRollbackActions")
  public void shouldRollbackTransactionWhenFailedToRemoveAnyOfEntriesDuringAccountRemoving(
      Class<CascadeEventSubscriber<CascadeEvent>> subscriberClass, Class<CascadeEvent> eventClass)
      throws Exception {
    createTestData();
    eventService.unsubscribe(injector.getInstance(subscriberClass), eventClass);

    // Remove the user, all entries must be rolled back after fail
    try {
      accountDao.remove(account.getId());
      fail("AccountDao#remove had to throw exception");
    } catch (Exception ignored) {
    }

    // Check all the data rolled back
    assertFalse(workspaceDao.getByNamespace(user.getName(), 30, 0).isEmpty());
    wipeTestData();
  }

  @DataProvider(name = "beforeAccountRemoveRollbackActions")
  public Object[][] beforeAccountRemoveActions() {
    return new Class[][] {
      {RemoveWorkspaceBeforeAccountRemovedEventSubscriber.class, BeforeAccountRemovedEvent.class}
    };
  }

  private void createTestData() throws Exception {
    accountDao.create(account = createAccount("bobby"));

    userDao.create(user = createUser("bobby"));

    profileDao.create(profile = createProfile(user.getId()));

    preferenceDao.setPreferences(user.getId(), preferences = createPreferences());

    workspaceDao.create(workspace1 = createWorkspaceWithConfig("workspace1", account));
    workspaceDao.create(workspace2 = createWorkspaceWithDevfile("workspace2", account));

    workspaceActivityDao.setCreatedTime(workspace1.getId(), System.currentTimeMillis());
    workspaceActivityDao.setCreatedTime(workspace2.getId(), System.currentTimeMillis());
    workspaceActivityDao.setExpirationTime(workspace1.getId(), System.currentTimeMillis());
    workspaceActivityDao.setExpirationTime(workspace2.getId(), System.currentTimeMillis());

    sshDao.create(sshPair1 = createSshPair(user.getId(), "service", "name1"));
    sshDao.create(sshPair2 = createSshPair(user.getId(), "service", "name2"));

    k8sRuntimes.putIfAbsent(k8sRuntimeState = createK8sRuntimeState(workspace1.getId()));
    k8sRuntimes.putIfAbsent(k8sRuntimeState2 = createK8sRuntimeState(workspace2.getId()));

    k8sMachines.put(
        k8sRuntimeState.getRuntimeId(), TestObjectsFactory.createK8sMachine(k8sRuntimeState));
    k8sMachines.put(
        k8sRuntimeState2.getRuntimeId(), TestObjectsFactory.createK8sMachine(k8sRuntimeState2));
  }

  private void wipeTestData() throws Exception {
    sshDao.remove(sshPair1.getOwner(), sshPair1.getService(), sshPair1.getName());
    sshDao.remove(sshPair2.getOwner(), sshPair2.getService(), sshPair2.getName());

    workspaceActivityDao.removeActivity(workspace1.getId());
    workspaceActivityDao.removeActivity(workspace2.getId());

    k8sMachines.remove(k8sRuntimeState.getRuntimeId());
    k8sRuntimes.remove(k8sRuntimeState.getRuntimeId());

    k8sRuntimes.remove(k8sRuntimeState2.getRuntimeId());

    workspaceDao.remove(workspace1.getId());
    workspaceDao.remove(workspace2.getId());

    notFoundToNull(() -> userDao.getById(user.getId()));

    preferenceDao.remove(user.getId());

    profileDao.remove(user.getId());

    userDao.remove(user.getId());

    accountDao.remove(account.getId());
  }

  private static <T> T notFoundToNull(Callable<T> action) throws Exception {
    try {
      return action.call();
    } catch (NotFoundException x) {
      return null;
    }
  }
}

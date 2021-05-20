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
package org.eclipse.che.multiuser.integration.jpa.cascaderemoval;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createAccount;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createFactory;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createFreeResourcesLimit;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createPreferences;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createProfile;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createSignatureKeyPair;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createSshPair;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createUser;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createWorker;
import static org.eclipse.che.multiuser.integration.jpa.cascaderemoval.TestObjectsFactory.createWorkspace;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.DELETE;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.READ;
import static org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain.UPDATE;
import static org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao.RemoveFreeResourcesLimitSubscriber;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.api.AccountModule;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.factory.server.jpa.FactoryJpaModule;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.ssh.server.jpa.SshJpaModule;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceLockService;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.api.workspace.server.WorkspaceLockService;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.WorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.devfile.DevfileModule;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.eclipse.che.commons.observability.NoopExecutorServiceWrapper;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.core.db.cascade.event.CascadeEvent;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.api.permission.server.PermissionCheckerImpl;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.machine.authentication.server.MachineAuthModule;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.eclipse.che.multiuser.organization.api.OrganizationJpaModule;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.listener.RemoveOrganizationOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributor;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.permission.devfile.server.jpa.MultiuserUserDevfileJpaModule;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao.RemoveUserDevfilePermissionsBeforeUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.workspace.server.jpa.MultiuserWorkspaceJpaModule;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceLockKeyProvider;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.ResourcesProvider;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.ResourceType;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests top-level entities cascade removals.
 *
 * @author Yevhenii Voevodin
 */
public class JpaEntitiesCascadeRemovalTest {

  private Injector injector;
  private EventService eventService;
  private PreferenceDao preferenceDao;
  private AccountDao accountDao;
  private AccountManager accountManager;
  private UserDao userDao;
  private UserManager userManager;
  private ProfileDao profileDao;
  private WorkspaceDao workspaceDao;
  private SshDao sshDao;
  private FactoryDao factoryDao;
  private WorkerDao workerDao;
  private UserDevfilePermissionDao userDevfilePermissionDao;
  private UserDevfileDao userDevfileDao;
  private SignatureKeyDao signatureKeyDao;
  private FreeResourcesLimitDao freeResourcesLimitDao;
  private OrganizationManager organizationManager;
  private MemberDao memberDao;
  private OrganizationResourcesDistributor organizationResourcesDistributor;

  /** User is a root of dependency tree. */
  private UserImpl user;

  private UserImpl user2;
  private UserImpl user3;

  private AccountImpl account;
  private AccountImpl organizationalAccount;

  /** Profile depends on user. */
  private ProfileImpl profile;

  /** Preferences depend on user. */
  private Map<String, String> preferences;

  /** Workspaces depend on user. */
  private WorkspaceImpl workspace1;

  private WorkspaceImpl workspace2;

  /** to test workers */
  private WorkspaceImpl workspace3;
  /** to test workspace removing after organization removing */
  private WorkspaceImpl workspace4;

  /** SshPairs depend on user. */
  private SshPairImpl sshPair1;

  private SshPairImpl sshPair2;

  /** Factories depend on user. */
  private FactoryImpl factory1;

  private FactoryImpl factory2;

  /** Organization depends on user via permissions */
  private Organization organization;

  private Organization childOrganization;
  private Organization organization2;

  /** Free resources limit depends on user via personal account */
  private FreeResourcesLimitImpl freeResourcesLimit;

  private FreeResourcesLimitImpl freeResourcesLimit2;

  private UserDevfileImpl devfile;
  private UserDevfilePermissionImpl devfilePermission;

  private H2JpaCleaner h2JpaCleaner;

  @BeforeMethod
  public void setUp() throws Exception {
    injector =
        Guice.createInjector(
            Stage.PRODUCTION,
            new AbstractModule() {
              @Override
              protected void configure() {
                H2DBTestServer server = H2DBTestServer.startDefault();
                install(new JpaPersistModule("main"));
                bind(H2JpaCleaner.class).toInstance(new H2JpaCleaner(server));
                bind(EventService.class).in(Singleton.class);
                bind(SchemaInitializer.class)
                    .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
                bind(DBInitializer.class).asEagerSingleton();
                install(new InitModule(PostConstruct.class));
                install(new UserJpaModule());
                install(new AccountModule());
                install(new SshJpaModule());
                install(new FactoryJpaModule());
                install(new OrganizationJpaModule());
                install(new MultiuserWorkspaceJpaModule());
                install(new MachineAuthModule());
                install(new DevfileModule());
                install(new MultiuserUserDevfileJpaModule());

                bind(ExecutorServiceWrapper.class).to(NoopExecutorServiceWrapper.class);

                bind(FreeResourcesLimitDao.class).to(JpaFreeResourcesLimitDao.class);
                bind(RemoveFreeResourcesLimitSubscriber.class).asEagerSingleton();
                // initialize empty binder
                Multibinder.newSetBinder(binder(), WorkspaceAttributeValidator.class);
                bind(WorkspaceManager.class);
                bind(WorkspaceLockService.class).to(DefaultWorkspaceLockService.class);
                bind(WorkspaceStatusCache.class).to(DefaultWorkspaceStatusCache.class);
                bind(RuntimeInfrastructure.class).toInstance(mock(RuntimeInfrastructure.class));
                MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);
                bind(PermissionsManager.class);
                bind(PermissionChecker.class).to(PermissionCheckerImpl.class);
                bind(AccountManager.class);
                bind(Boolean.class)
                    .annotatedWith(Names.named("che.workspace.auto_snapshot"))
                    .toInstance(false);
                bind(Boolean.class)
                    .annotatedWith(Names.named("che.workspace.auto_restore"))
                    .toInstance(false);
                bind(WorkspaceSharedPool.class)
                    .toInstance(
                        new WorkspaceSharedPool(
                            "cached", null, null, new NoopExecutorServiceWrapper()));

                bind(String[].class)
                    .annotatedWith(Names.named("che.auth.reserved_user_names"))
                    .toInstance(new String[0]);
                bind(RemoveOrganizationOnLastUserRemovedEventSubscriber.class).asEagerSingleton();

                Multibinder.newSetBinder(binder(), ResourceLockKeyProvider.class);
                Multibinder.newSetBinder(binder(), ResourceUsageTracker.class);
                MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class);
                bind(String.class)
                    .annotatedWith(Names.named("che.workspace.plugin_registry_url"))
                    .toInstance("");
                MapBinder.newMapBinder(binder(), String.class, ChePluginsApplier.class);
                Multibinder.newSetBinder(binder(), ResourceType.class)
                    .addBinding()
                    .to(RamResourceType.class);
                Multibinder.newSetBinder(binder(), ResourcesProvider.class)
                    .addBinding()
                    .toInstance(
                        (accountId) ->
                            singletonList(
                                new ProvidedResourcesImpl(
                                    "test",
                                    null,
                                    accountId,
                                    -1L,
                                    -1L,
                                    singletonList(
                                        new ResourceImpl(
                                            RamResourceType.ID, 1024, RamResourceType.UNIT)))));

                bindConstant().annotatedWith(Names.named("che.workspace.probe_pool_size")).to(1);

                // setup bindings for the devfile that would otherwise be read from the config
                bindConstant()
                    .annotatedWith(Names.named("che.workspace.devfile.default_editor"))
                    .to("default/editor/0.0.1");
                bindConstant()
                    .annotatedWith(Names.named("che.websocket.endpoint"))
                    .to("che.websocket.endpoint");
                bind(String.class)
                    .annotatedWith(Names.named("che.workspace.devfile.default_editor.plugins"))
                    .toInstance("default/plugin/0.0.1");
                bind(String.class)
                    .annotatedWith(Names.named("che.workspace.devfile.async.storage.plugin"))
                    .toInstance("");
              }
            });

    eventService = injector.getInstance(EventService.class);
    accountDao = injector.getInstance(AccountDao.class);
    accountManager = injector.getInstance(AccountManager.class);
    userDao = injector.getInstance(UserDao.class);
    userManager = injector.getInstance(UserManager.class);
    preferenceDao = injector.getInstance(PreferenceDao.class);
    profileDao = injector.getInstance(ProfileDao.class);
    sshDao = injector.getInstance(SshDao.class);
    workspaceDao = injector.getInstance(WorkspaceDao.class);
    factoryDao = injector.getInstance(FactoryDao.class);
    workerDao = injector.getInstance(WorkerDao.class);
    userDevfileDao = injector.getInstance(UserDevfileDao.class);
    userDevfilePermissionDao = injector.getInstance(UserDevfilePermissionDao.class);
    signatureKeyDao = injector.getInstance(SignatureKeyDao.class);
    freeResourcesLimitDao = injector.getInstance(FreeResourcesLimitDao.class);
    organizationManager = injector.getInstance(OrganizationManager.class);
    memberDao = injector.getInstance(MemberDao.class);
    organizationResourcesDistributor = injector.getInstance(OrganizationResourcesDistributor.class);

    h2JpaCleaner = injector.getInstance(H2JpaCleaner.class);
  }

  @AfterMethod
  public void cleanup() {
    h2JpaCleaner.clean();
  }

  @Test
  public void shouldDeleteAllTheEntitiesWhenUserIsDeleted() throws Exception {
    createTestData();

    // Remove the user, all entries must be removed along with the user
    accountManager.remove(account.getId());
    userManager.remove(user.getId());
    userManager.remove(user2.getId());

    // Check all the entities are removed
    assertNull(notFoundToNull(() -> userDao.getById(user.getId())));
    assertNull(notFoundToNull(() -> profileDao.getById(user.getId())));
    assertTrue(preferenceDao.getPreferences(user.getId()).isEmpty());
    assertTrue(sshDao.get(user.getId()).isEmpty());
    assertTrue(workspaceDao.getByNamespace(account.getName(), 30, 0).isEmpty());
    assertTrue(userDevfileDao.getByNamespace(account.getName(), 30, 0).isEmpty());
    assertTrue(factoryDao.getByUser(user.getId(), 30, 0).isEmpty());
    // Check workers and parent entity is removed
    assertTrue(workspaceDao.getByNamespace(user2.getId(), 30, 0).isEmpty());
    assertTrue(userDevfileDao.getByNamespace(user2.getId(), 30, 0).isEmpty());
    assertEquals(workerDao.getWorkers(workspace3.getId(), 1, 0).getTotalItemsCount(), 0);
    assertNull(
        notFoundToNull(
            () ->
                userDevfilePermissionDao.getUserDevfilePermission(devfile.getId(), user2.getId())));
    assertFalse(userDevfileDao.getById(devfile.getId()).isPresent());

    // Permissions are removed
    // Non-removed user permissions and stack are present
    // Check existence of organizations
    assertNull(notFoundToNull(() -> organizationManager.getById(organization.getId())));
    assertEquals(memberDao.getMembers(organization.getId(), 1, 0).getTotalItemsCount(), 0);
    // Check workspace is removed along with organization account
    assertNull(notFoundToNull(() -> workspaceDao.get(workspace4.getId())));

    assertNull(notFoundToNull(() -> organizationManager.getById(childOrganization.getId())));
    assertEquals(memberDao.getMembers(childOrganization.getId(), 1, 0).getTotalItemsCount(), 0);

    assertNotNull(notFoundToNull(() -> organizationManager.getById(organization2.getId())));
    assertEquals(memberDao.getMembers(organization2.getId(), 1, 0).getTotalItemsCount(), 1);

    // free resources limit is removed
    assertNull(notFoundToNull(() -> freeResourcesLimitDao.get(user.getId())));
    assertNull(notFoundToNull(() -> freeResourcesLimitDao.get(user2.getId())));

    // machine token keypairs
    assertNull(notFoundToNull(() -> signatureKeyDao.get(workspace1.getId())));
    assertNull(notFoundToNull(() -> signatureKeyDao.get(workspace2.getId())));

    // distributed resources is removed
    assertNull(
        notFoundToNull(() -> organizationResourcesDistributor.get(childOrganization.getId())));

    // cleanup
    memberDao.remove(organization2.getId(), user3.getId());
    organizationManager.remove(organization2.getId());
    userDao.remove(user3.getId());
  }

  @Test(dataProvider = "beforeRemoveRollbackActions")
  public void shouldRollbackTransactionWhenFailedToRemoveAnyOfEntries(
      Class<CascadeEventSubscriber<CascadeEvent>> subscriberClass, Class<CascadeEvent> eventClass)
      throws Exception {
    createTestData();
    eventService.unsubscribe(injector.getInstance(subscriberClass), eventClass);

    // Remove the user, all entries must be rolled back after fail
    try {
      userManager.remove(user2.getId());
      fail("UserManager#remove had to throw exception");
    } catch (Exception ignored) {
    }

    // Check all the data rolled back
    assertNotNull(userDao.getById(user2.getId()));
    assertNotNull(notFoundToNull(() -> freeResourcesLimitDao.get(account.getId())));
    assertNotNull(notFoundToNull(() -> organizationManager.getById(organization.getId())));
    assertNotNull(notFoundToNull(() -> organizationManager.getById(childOrganization.getId())));
    assertNotNull(notFoundToNull(() -> organizationManager.getById(organization2.getId())));
    assertNotNull(notFoundToNull(() -> signatureKeyDao.get(workspace2.getId())));
    assertTrue(userDevfileDao.getById(devfile.getId()).isPresent());
    assertNotNull(
        notFoundToNull(
            () ->
                userDevfilePermissionDao.getUserDevfilePermission(devfile.getId(), user2.getId())));
    assertFalse(
        organizationResourcesDistributor.getResourcesCaps(childOrganization.getId()).isEmpty());
    wipeTestData();
  }

  @DataProvider(name = "beforeRemoveRollbackActions")
  public Object[][] beforeRemoveActions() {
    return new Class[][] {
      {RemoveOrganizationOnLastUserRemovedEventSubscriber.class, BeforeUserRemovedEvent.class},
      {
        RemoveUserDevfilePermissionsBeforeUserRemovedEventSubscriber.class,
        BeforeUserRemovedEvent.class
      }
    };
  }

  private void createTestData()
      throws NotFoundException, ConflictException, ServerException, NoSuchAlgorithmException {
    userDao.create(user = createUser("bobby"));
    accountDao.create(account = createAccount("bobby"));
    // test permissions users
    userDao.create(user2 = createUser("worker"));
    userDao.create(user3 = createUser("stacker"));

    profileDao.create(profile = createProfile(user.getId()));

    preferenceDao.setPreferences(user.getId(), preferences = createPreferences());

    workspaceDao.create(workspace1 = createWorkspace("workspace1", account));
    workspaceDao.create(workspace2 = createWorkspace("workspace2", account));
    workspaceDao.create(workspace3 = createWorkspace("workspace3", account));

    sshDao.create(sshPair1 = createSshPair(user.getId(), "service", "name1"));
    sshDao.create(sshPair2 = createSshPair(user.getId(), "service", "name2"));

    factoryDao.create(factory1 = createFactory("factory1", user.getId()));
    factoryDao.create(factory2 = createFactory("factory2", user.getId()));

    workerDao.store(createWorker(user2.getId(), workspace3.getId()));

    signatureKeyDao.create(createSignatureKeyPair(workspace1.getId()));
    signatureKeyDao.create(createSignatureKeyPair(workspace2.getId()));

    // creator will have all permissions for newly created organization
    prepareCreator(user.getId());
    organization = organizationManager.create(new OrganizationImpl(null, "testOrg", null));
    organizationalAccount = accountDao.getById(organization.getId());
    workspaceDao.create(workspace4 = createWorkspace("workspace4", organizationalAccount));
    organization2 = organizationManager.create(new OrganizationImpl(null, "anotherOrg", null));
    prepareCreator(user2.getId());
    childOrganization =
        organizationManager.create(
            new OrganizationImpl(null, "childTestOrg", organization.getId()));

    memberDao.store(
        new MemberImpl(user2.getId(), organization2.getId(), singletonList(SET_PERMISSIONS)));
    memberDao.store(
        new MemberImpl(user3.getId(), organization2.getId(), singletonList(SET_PERMISSIONS)));

    freeResourcesLimitDao.store(freeResourcesLimit = createFreeResourcesLimit(account.getId()));
    freeResourcesLimitDao.store(
        freeResourcesLimit2 = createFreeResourcesLimit(organization.getId()));

    organizationResourcesDistributor.capResources(
        childOrganization.getId(),
        singletonList(new ResourceImpl(RamResourceType.ID, 1024, RamResourceType.UNIT)));

    userDevfileDao.create(
        devfile = TestObjectsFactory.createUserDevfile("id-dev1", "devfile1", account));
    userDevfilePermissionDao.store(
        devfilePermission =
            new UserDevfilePermissionImpl(
                devfile.getId(), user2.getId(), ImmutableList.of(READ, DELETE, UPDATE)));
  }

  private void prepareCreator(String userId) {
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("userok", userId, "", false));
  }

  private void wipeTestData() throws ConflictException, ServerException, NotFoundException {
    organizationResourcesDistributor.capResources(childOrganization.getId(), emptyList());

    freeResourcesLimitDao.remove(freeResourcesLimit.getAccountId());
    freeResourcesLimitDao.remove(freeResourcesLimit2.getAccountId());

    memberDao.remove(organization.getId(), user.getId());
    memberDao.remove(childOrganization.getId(), user.getId());
    memberDao.remove(organization2.getId(), user.getId());
    memberDao.remove(organization2.getId(), user2.getId());
    memberDao.remove(organization2.getId(), user3.getId());

    organizationManager.remove(childOrganization.getId());
    organizationManager.remove(organization.getId());
    organizationManager.remove(organization2.getId());

    workerDao.removeWorker(workspace3.getId(), user2.getId());

    userDevfilePermissionDao.removeUserDevfilePermission(devfile.getId(), user2.getId());
    userDevfileDao.remove(devfile.getId());

    factoryDao.remove(factory1.getId());
    factoryDao.remove(factory2.getId());

    sshDao.remove(sshPair1.getOwner(), sshPair1.getService(), sshPair1.getName());
    sshDao.remove(sshPair2.getOwner(), sshPair2.getService(), sshPair2.getName());

    signatureKeyDao.remove(workspace1.getId());
    signatureKeyDao.remove(workspace2.getId());

    workspaceDao.remove(workspace1.getId());
    workspaceDao.remove(workspace2.getId());
    workspaceDao.remove(workspace3.getId());
    workspaceDao.remove(workspace4.getId());

    preferenceDao.remove(user3.getId());
    preferenceDao.remove(user2.getId());
    preferenceDao.remove(user.getId());

    profileDao.remove(user3.getId());
    profileDao.remove(user2.getId());
    profileDao.remove(user.getId());

    userDao.remove(user3.getId());
    userDao.remove(user2.getId());
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

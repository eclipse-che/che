/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.jpa.JpaSystemPermissionsDao;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;
import org.eclipse.che.multiuser.api.permission.server.spi.PermissionsDao;
import org.eclipse.che.multiuser.api.permission.server.spi.tck.SystemPermissionsDaoTest;
import org.eclipse.che.multiuser.machine.authentication.server.signature.jpa.JpaSignatureKeyDao;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationDomain;
import org.eclipse.che.multiuser.organization.spi.MemberDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDao;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaMemberDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDao;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaOrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.permission.devfile.server.UserDevfileDomain;
import org.eclipse.che.multiuser.permission.devfile.server.model.UserDevfilePermission;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.UserDevfilePermissionDao;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.WorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaWorkerDao;
import org.eclipse.che.multiuser.permission.workspace.server.spi.tck.WorkerDaoTest;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for running TCKs based on MySQL.
 *
 * @author Mihail Kuznyetsov
 * @author Barry Dresdner
 */
public class MultiuserMySqlTckModule extends TckModule {

  private static final Logger LOG = LoggerFactory.getLogger(MultiuserMySqlTckModule.class);

  @Override
  protected void configure() {

    final Map<String, String> properties = new HashMap<>();
    properties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());

    final String dbUrl = System.getProperty("jdbc.url");
    final String dbUser = System.getProperty("jdbc.user");
    final String dbPassword = System.getProperty("jdbc.password");

    waitConnectionIsEstablished(dbUrl, dbUser, dbPassword);

    properties.put(JDBC_URL, dbUrl);
    properties.put(JDBC_USER, dbUser);
    properties.put(JDBC_PASSWORD, dbPassword);
    properties.put(JDBC_DRIVER, System.getProperty("jdbc.driver"));

    JpaPersistModule main = new JpaPersistModule("main");
    main.properties(properties);
    install(main);
    final com.mysql.cj.jdbc.MysqlDataSource dataSource = new com.mysql.cj.jdbc.MysqlDataSource();
    dataSource.setUser(dbUser);
    dataSource.setPassword(dbPassword);
    dataSource.setUrl(dbUrl);
    bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(dataSource, "che-schema"));
    bind(DBInitializer.class).asEagerSingleton();
    bind(TckResourcesCleaner.class).to(JpaCleaner.class);

    // repositories
    // api-account
    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));
    // api-user
    bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserJpaTckRepository.class);

    // api-workspace
    bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {})
        .toInstance(new JpaTckRepository<>(WorkspaceImpl.class));
    bind(new TypeLiteral<TckRepository<WorkerImpl>>() {})
        .toInstance(new JpaTckRepository<>(WorkerImpl.class));

    // api permission
    bind(new TypeLiteral<TckRepository<SystemPermissionsImpl>>() {})
        .toInstance(new JpaTckRepository<>(SystemPermissionsImpl.class));

    bind(new TypeLiteral<PermissionsDao<SystemPermissionsImpl>>() {})
        .to(JpaSystemPermissionsDao.class);

    bind(new TypeLiteral<AbstractPermissionsDomain<WorkerImpl>>() {})
        .to(WorkerDaoTest.TestDomain.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<SystemPermissionsImpl>>() {})
        .to(SystemPermissionsDaoTest.TestDomain.class);

    // api-organization
    bind(new TypeLiteral<TckRepository<OrganizationImpl>>() {})
        .to(JpaOrganizationImplTckRepository.class);
    bind(new TypeLiteral<TckRepository<MemberImpl>>() {})
        .toInstance(new JpaTckRepository<>(MemberImpl.class));
    bind(new TypeLiteral<TckRepository<OrganizationDistributedResourcesImpl>>() {})
        .toInstance(new JpaTckRepository<>(OrganizationDistributedResourcesImpl.class));

    // api-resource
    bind(new TypeLiteral<TckRepository<FreeResourcesLimitImpl>>() {})
        .toInstance(new JpaTckRepository<>(FreeResourcesLimitImpl.class));

    // machine token keys
    bind(new TypeLiteral<TckRepository<SignatureKeyPairImpl>>() {})
        .toInstance(new JpaTckRepository<>(SignatureKeyPairImpl.class));

    bind(new TypeLiteral<TckRepository<UserDevfileImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserDevfileImpl.class));

    // dao
    bind(OrganizationDao.class).to(JpaOrganizationDao.class);
    bind(OrganizationDistributedResourcesDao.class)
        .to(JpaOrganizationDistributedResourcesDao.class);
    bind(FreeResourcesLimitDao.class).to(JpaFreeResourcesLimitDao.class);

    bind(WorkerDao.class).to(JpaWorkerDao.class);
    bind(MemberDao.class).to(JpaMemberDao.class);
    bind(SignatureKeyDao.class).to(JpaSignatureKeyDao.class);
    bind(new TypeLiteral<PermissionsDao<MemberImpl>>() {}).to(JpaMemberDao.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<MemberImpl>>() {}).to(OrganizationDomain.class);

    bind(UserDevfilePermissionDao.class).to(JpaUserDevfilePermissionDao.class);
    bind(new TypeLiteral<AbstractPermissionsDomain<UserDevfilePermissionImpl>>() {})
        .to(UserDevfileDomain.class);
    bind(new TypeLiteral<TckRepository<UserDevfilePermission>>() {})
        .toInstance(new JpaTckRepository<>(UserDevfilePermission.class));

    // SHA-512 ecnryptor is faster than PBKDF2 so it is better for testing
    bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class).in(Singleton.class);

    // Creates empty multibinder to avoid error during container starting
    Multibinder.newSetBinder(
        binder(), String.class, Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));
  }

  private static void waitConnectionIsEstablished(String dbUrl, String dbUser, String dbPassword) {
    boolean isAvailable = false;
    for (int i = 0; i < 60 && !isAvailable; i++) {
      try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
        isAvailable = true;
      } catch (SQLException x) {
        LOG.warn(
            "An attempt to connect to the database failed with an error: {}",
            x.getLocalizedMessage());
        try {
          TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException interruptedX) {
          throw new RuntimeException(interruptedX.getLocalizedMessage(), interruptedX);
        }
      }
    }
    if (!isAvailable) {
      throw new IllegalStateException("Couldn't initialize connection with a database");
    }
  }

  @Transactional
  public static class UserJpaTckRepository implements TckRepository<UserImpl> {

    @Inject private Provider<EntityManager> managerProvider;

    @Inject private PasswordEncryptor encryptor;

    @Override
    public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
      final EntityManager manager = managerProvider.get();
      entities
          .stream()
          .map(
              user ->
                  new UserImpl(
                      user.getId(),
                      user.getEmail(),
                      user.getName(),
                      user.getPassword() != null ? encryptor.encrypt(user.getPassword()) : null,
                      user.getAliases()))
          .forEach(manager::persist);
    }

    @Override
    public void removeAll() throws TckRepositoryException {
      final EntityManager manager = managerProvider.get();
      manager
          .createQuery("SELECT users FROM Usr users", UserImpl.class)
          .getResultList()
          .forEach(manager::remove);
    }
  }

  /**
   * Organizations require to have own repository because it is important to delete organization in
   * reverse order that they were stored. It allows to resolve problems with removing
   * suborganization before parent organization removing.
   *
   * @author Sergii Leschenko
   */
  public static class JpaOrganizationImplTckRepository extends JpaTckRepository<OrganizationImpl> {
    @Inject protected Provider<EntityManager> managerProvider;

    @Inject protected UnitOfWork uow;

    private final List<OrganizationImpl> createdOrganizations = new ArrayList<>();

    public JpaOrganizationImplTckRepository() {
      super(OrganizationImpl.class);
    }

    @Override
    public void createAll(Collection<? extends OrganizationImpl> entities)
        throws TckRepositoryException {
      super.createAll(entities);
      // It's important to save organization to remove them in the reverse order
      createdOrganizations.addAll(entities);
    }

    @Override
    public void removeAll() throws TckRepositoryException {
      uow.begin();
      final EntityManager manager = managerProvider.get();
      try {
        manager.getTransaction().begin();

        for (int i = createdOrganizations.size() - 1; i > -1; i--) {
          // The query 'DELETE FROM ....' won't be correct as it will ignore orphanRemoval
          // and may also ignore some configuration options, while EntityManager#remove won't
          try {
            final OrganizationImpl organizationToRemove =
                manager
                    .createQuery(
                        "SELECT o FROM Organization o " + "WHERE o.id = :id",
                        OrganizationImpl.class)
                    .setParameter("id", createdOrganizations.get(i).getId())
                    .getSingleResult();
            manager.remove(organizationToRemove);
          } catch (NoResultException ignored) {
            // it is already removed
          }
        }
        createdOrganizations.clear();

        manager.getTransaction().commit();
      } catch (RuntimeException x) {
        if (manager.getTransaction().isActive()) {
          manager.getTransaction().rollback();
        }
        throw new TckRepositoryException(x.getLocalizedMessage(), x);
      } finally {
        uow.end();
      }

      // remove all objects that was created in tests
      super.removeAll();
    }
  }
}

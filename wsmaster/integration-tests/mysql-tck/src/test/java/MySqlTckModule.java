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
import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.ssh.server.jpa.JpaSshDao;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao;
import org.eclipse.che.api.user.server.jpa.JpaUserDao;
import org.eclipse.che.api.user.server.jpa.PreferenceEntity;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivity;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.server.devfile.SerializableConverter;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
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
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.mysql.jpa.eclipselink.MySqlExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.multiuser.machine.authentication.server.signature.jpa.JpaSignatureKeyDao;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeCommandImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for running TCKs based on MySQL.
 *
 * @author Sergii Kabashniuk
 * @author Barry Dresdner
 */
public class MySqlTckModule extends TckModule {

  private static final Logger LOG = LoggerFactory.getLogger(MySqlTckModule.class);

  @Override
  protected void configure() {
    final String dbUrl = System.getProperty("jdbc.url");
    final String dbUser = System.getProperty("jdbc.user");
    final String dbPassword = System.getProperty("jdbc.password");

    waitConnectionIsEstablished(dbUrl, dbUser, dbPassword);

    // jpa
    install(
        new PersistTestModuleBuilder()
            .setDriver(com.mysql.cj.jdbc.Driver.class)
            .setUrl(dbUrl)
            .setUser(dbUser)
            .setPassword(dbPassword)
            .setExceptionHandler(MySqlExceptionHandler.class)
            .addEntityClasses(
                AccountImpl.class,
                UserImpl.class,
                ProfileImpl.class,
                PreferenceEntity.class,
                WorkspaceImpl.class,
                WorkspaceConfigImpl.class,
                ProjectConfigImpl.class,
                EnvironmentImpl.class,
                RecipeImpl.class,
                MachineConfigImpl.class,
                SourceStorageImpl.class,
                ServerConfigImpl.class,
                CommandImpl.class,
                SshPairImpl.class,
                WorkspaceActivity.class,
                VolumeImpl.class,
                SignatureKeyImpl.class,
                SignatureKeyPairImpl.class,
                // devfile
                UserDevfileImpl.class,
                ActionImpl.class,
                org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl.class,
                ComponentImpl.class,
                DevfileImpl.class,
                EndpointImpl.class,
                EntrypointImpl.class,
                EnvImpl.class,
                ProjectImpl.class,
                SourceImpl.class,
                org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl.class,
                // k8s-runtimes
                KubernetesRuntimeState.class,
                KubernetesRuntimeCommandImpl.class,
                KubernetesMachineImpl.class,
                KubernetesMachineImpl.MachineId.class,
                KubernetesServerImpl.class,
                KubernetesServerImpl.ServerId.class)
            .addEntityClass(
                "org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
            .addClass(SerializableConverter.class)
            .build());
    bind(TckResourcesCleaner.class).to(JpaCleaner.class);

    // db initialization
    bind(DBInitializer.class).asEagerSingleton();
    final com.mysql.cj.jdbc.MysqlDataSource dataSource = new com.mysql.cj.jdbc.MysqlDataSource();
    dataSource.setUser(dbUser);
    dataSource.setPassword(dbPassword);
    dataSource.setUrl(dbUrl);
    bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(dataSource, "che-schema"));

    // account
    bind(AccountDao.class).to(JpaAccountDao.class);
    bind(new TypeLiteral<TckRepository<AccountImpl>>() {})
        .toInstance(new JpaTckRepository<>(AccountImpl.class));

    // user
    bind(UserDao.class).to(JpaUserDao.class);
    bind(ProfileDao.class).to(JpaProfileDao.class);
    bind(PreferenceDao.class).to(JpaPreferenceDao.class);
    bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserRepo.class);
    bind(new TypeLiteral<TckRepository<Pair<String, Map<String, String>>>>() {})
        .to(PreferencesRepo.class);
    bind(new TypeLiteral<TckRepository<ProfileImpl>>() {})
        .toInstance(new JpaTckRepository<>(ProfileImpl.class));
    bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class);

    // machine
    bind(new TypeLiteral<TckRepository<RecipeImpl>>() {})
        .toInstance(new JpaTckRepository<>(RecipeImpl.class));

    // ssh
    bind(SshDao.class).to(JpaSshDao.class);
    bind(new TypeLiteral<TckRepository<SshPairImpl>>() {})
        .toInstance(new JpaTckRepository<>(SshPairImpl.class));

    // workspace
    bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
    bind(WorkspaceActivityDao.class).to(JpaWorkspaceActivityDao.class);
    bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new WorkspaceRepository());

    bind(UserDevfileDao.class).to(JpaUserDevfileDao.class);
    bind(new TypeLiteral<TckRepository<UserDevfileImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserDevfileImpl.class));

    // sign keys
    bind(SignatureKeyDao.class).to(JpaSignatureKeyDao.class);
    bind(new TypeLiteral<TckRepository<SignatureKeyPairImpl>>() {})
        .toInstance(new JpaTckRepository<>(SignatureKeyPairImpl.class));

    // k8s runtimes
    bind(new TypeLiteral<TckRepository<KubernetesRuntimeState>>() {})
        .toInstance(new JpaTckRepository<>(KubernetesRuntimeState.class));

    bind(new TypeLiteral<TckRepository<KubernetesMachineImpl>>() {})
        .toInstance(new JpaTckRepository<>(KubernetesMachineImpl.class));

    bind(KubernetesRuntimeStateCache.class).to(JpaKubernetesRuntimeStateCache.class);
    bind(KubernetesMachineCache.class).to(JpaKubernetesMachineCache.class);
    bind(JpaKubernetesRuntimeStateCache.RemoveKubernetesRuntimeBeforeWorkspaceRemoved.class)
        .asEagerSingleton();
    bind(JpaKubernetesMachineCache.RemoveKubernetesMachinesBeforeRuntimesRemoved.class)
        .asEagerSingleton();
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
  static class PreferencesRepo implements TckRepository<Pair<String, Map<String, String>>> {

    @Inject private Provider<EntityManager> managerProvider;

    @Override
    public void createAll(Collection<? extends Pair<String, Map<String, String>>> entities)
        throws TckRepositoryException {
      final EntityManager manager = managerProvider.get();
      for (Pair<String, Map<String, String>> pair : entities) {
        manager.persist(new PreferenceEntity(pair.first, pair.second));
      }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
      final EntityManager manager = managerProvider.get();
      manager
          .createQuery("SELECT preferences FROM Preference preferences", PreferenceEntity.class)
          .getResultList()
          .forEach(manager::remove);
    }
  }

  @Transactional
  static class UserRepo implements TckRepository<UserImpl> {

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
                      encryptor.encrypt(user.getPassword()),
                      user.getAliases()))
          .forEach(manager::persist);
    }

    @Override
    public void removeAll() throws TckRepositoryException {
      managerProvider
          .get()
          .createQuery("SELECT u FROM Usr u", UserImpl.class)
          .getResultList()
          .forEach(managerProvider.get()::remove);
    }
  }

  private static class WorkspaceRepository extends JpaTckRepository<WorkspaceImpl> {
    public WorkspaceRepository() {
      super(WorkspaceImpl.class);
    }

    @Override
    public void createAll(Collection<? extends WorkspaceImpl> entities)
        throws TckRepositoryException {
      for (WorkspaceImpl entity : entities) {
        if (entity.getConfig() != null) {
          entity.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
        }
      }
      super.createAll(entities);
    }
  }
}

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

import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.machine.server.jpa.JpaSnapshotDao;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
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
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;

/**
 * Module for running TCKs based on PostgreSQL.
 *
 * @author Yevhenii Voevodin
 */
public class PostgreSqlTckModule extends TckModule {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlTckModule.class);

    @Override
    protected void configure() {
        final String dbUrl = System.getProperty("jdbc.url");
        final String dbUser = System.getProperty("jdbc.user");
        final String dbPassword = System.getProperty("jdbc.password");

        waitConnectionIsEstablished(dbUrl, dbUser, dbPassword);

        // jpa
        final Map<String, String> properties = new HashMap<>();
        properties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
        properties.put(JDBC_URL, dbUrl);
        properties.put(JDBC_USER, dbUser);
        properties.put(JDBC_PASSWORD, dbPassword);
        properties.put(JDBC_DRIVER, System.getProperty("jdbc.driver"));
        final JpaPersistModule persistenceModule = new JpaPersistModule("test");
        persistenceModule.properties(properties);
        install(persistenceModule);
        bind(TckResourcesCleaner.class).to(JpaCleaner.class);

        // db initialization
        bind(DBInitializer.class).asEagerSingleton();
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setUrl(dbUrl);
        bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(dataSource, "che-schema"));

        // account
        bind(AccountDao.class).to(JpaAccountDao.class);
        bind(new TypeLiteral<TckRepository<AccountImpl>>() {}).toInstance(new JpaTckRepository<>(AccountImpl.class));

        // user
        bind(UserDao.class).to(JpaUserDao.class);
        bind(ProfileDao.class).to(JpaProfileDao.class);
        bind(PreferenceDao.class).to(JpaPreferenceDao.class);
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserRepo.class);
        bind(new TypeLiteral<TckRepository<Pair<String, Map<String, String>>>>() {}).to(PreferencesRepo.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).toInstance(new JpaTckRepository<>(ProfileImpl.class));
        bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class);

        // machine
        bind(RecipeDao.class).to(JpaRecipeDao.class);
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));
        bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {}).toInstance(new JpaTckRepository<>(SnapshotImpl.class));
        bind(new TypeLiteral<TckRepository<Workspace>>() {}).toInstance(new WorkspaceRepoForSnapshots());

        // ssh
        bind(SshDao.class).to(JpaSshDao.class);
        bind(new TypeLiteral<TckRepository<SshPairImpl>>() {}).toInstance(new JpaTckRepository<>(SshPairImpl.class));

        // workspace
        bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
        bind(StackDao.class).to(JpaStackDao.class);
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new JpaTckRepository<>(StackImpl.class));
    }

    private static void waitConnectionIsEstablished(String dbUrl, String dbUser, String dbPassword) {
        boolean isAvailable = false;
        for (int i = 0; i < 60 && !isAvailable; i++) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                isAvailable = true;
            } catch (SQLException x) {
                LOG.warn("An attempt to connect to the database failed with an error: {}", x.getLocalizedMessage());
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

        @Inject
        private Provider<EntityManager> managerProvider;

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
            manager.createQuery("SELECT preferences FROM Preference preferences", PreferenceEntity.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }

    @Transactional
    static class UserRepo implements TckRepository<UserImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Inject
        private PasswordEncryptor encryptor;

        @Override
        public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            entities.stream()
                    .map(user -> new UserImpl(user.getId(),
                                              user.getEmail(),
                                              user.getName(),
                                              encryptor.encrypt(user.getPassword()),
                                              user.getAliases()))
                    .forEach(manager::persist);
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            managerProvider.get()
                           .createQuery("SELECT u FROM Usr u", UserImpl.class)
                           .getResultList()
                           .forEach(managerProvider.get()::remove);
        }
    }

    static class WorkspaceRepoForSnapshots extends JpaTckRepository<Workspace> {
        public WorkspaceRepoForSnapshots() { super(WorkspaceImpl.class); }

        @Override
        public void createAll(Collection<? extends Workspace> entities) throws TckRepositoryException {
            super.createAll(entities.stream()
                                    .map(w -> new WorkspaceImpl(w, new AccountImpl(w.getNamespace(),
                                                                                   w.getNamespace(),
                                                                                   "simple")))
                                    .collect(Collectors.toList()));
        }
    }
}

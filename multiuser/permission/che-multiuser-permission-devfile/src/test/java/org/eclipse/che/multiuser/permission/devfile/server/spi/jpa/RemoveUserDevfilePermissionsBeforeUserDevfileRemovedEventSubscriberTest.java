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
package org.eclipse.che.multiuser.permission.devfile.server.spi.jpa;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.devfile.server.jpa.JpaUserDevfileDao;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.devfile.server.TestObjectGenerator;
import org.eclipse.che.multiuser.permission.devfile.server.model.impl.UserDevfilePermissionImpl;
import org.eclipse.che.multiuser.permission.devfile.server.spi.jpa.JpaUserDevfilePermissionDao.RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for {@link RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber} */
public class RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriberTest {
  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private JpaUserDevfilePermissionDao userDevfilePermissionsDao;
  private JpaUserDevfileDao userDevfileDao;

  private RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber subscriber;

  private UserDevfileImpl userDevfile;
  private UserDevfilePermissionImpl[] userDevfilePermissions;
  private UserImpl[] users;

  @BeforeClass
  public void setupEntities() throws Exception {

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    userDevfile = TestObjectGenerator.createUserDevfile("devfile_id1", generate("name", 6));

    userDevfilePermissions =
        new UserDevfilePermissionImpl[] {
          new UserDevfilePermissionImpl(
              userDevfile.getId(), "user1", Arrays.asList("read", "use", "run")),
          new UserDevfilePermissionImpl(userDevfile.getId(), "user2", Arrays.asList("read", "use"))
        };

    Injector injector = Guice.createInjector(new JpaTckModule());

    manager = injector.getInstance(EntityManager.class);
    userDevfilePermissionsDao = injector.getInstance(JpaUserDevfilePermissionDao.class);
    userDevfileDao = injector.getInstance(JpaUserDevfileDao.class);
    subscriber =
        injector.getInstance(
            RemoveUserDevfilePermissionsBeforeUserDevfileRemovedEventSubscriber.class);
    subscriber.subscribe();
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    manager.persist(userDevfile);
    Stream.of(users).forEach(manager::persist);
    manager.persist(TestObjectGenerator.TEST_ACCOUNT);
    Stream.of(userDevfilePermissions).forEach(manager::persist);
    manager.getTransaction().commit();
    manager.clear();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();

    manager
        .createQuery("SELECT e FROM UserDevfilePermission e", UserDevfilePermissionImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT w FROM UserDevfile w", UserDevfileImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT a FROM Account a", AccountImpl.class)
        .getResultList()
        .forEach(manager::remove);
    manager
        .createQuery("SELECT u FROM Usr u", UserImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager.getTransaction().commit();
  }

  @AfterClass
  public void shutdown() throws Exception {
    subscriber.unsubscribe();
    tckResourcesCleaner.clean();
  }

  @Test
  public void shouldRemoveAllPermissionsWhenUserDevfileIsRemoved() throws Exception {
    userDevfileDao.remove(userDevfile.getId());

    assertEquals(
        userDevfilePermissionsDao
            .getUserDevfilePermission(userDevfile.getId(), 1, 0)
            .getTotalItemsCount(),
        0);
  }

  @Test
  public void shouldRemoveAllPermissionsWhenPageSizeEqualsToOne() throws Exception {
    subscriber.removeUserDevfilePermissions(userDevfile.getId(), 1);

    assertEquals(
        userDevfilePermissionsDao
            .getUserDevfilePermission(userDevfile.getId(), 1, 0)
            .getTotalItemsCount(),
        0);
  }
}

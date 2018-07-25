/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

import static java.util.Arrays.asList;
import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link RemovePermissionsBeforeStackRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemovePermissionsBeforeStackRemovedEventSubscriberTest {
  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private JpaStackDao stackDao;
  private JpaStackPermissionsDao stackPermissionsDao;

  private RemovePermissionsBeforeStackRemovedEventSubscriber subscriber;

  private StackImpl stack;
  private UserImpl[] users;
  private StackPermissionsImpl[] stackPermissions;

  @BeforeClass
  public void setupEntities() throws Exception {
    stack = StackImpl.builder().setId("stack123").setName("defaultStack").build();
    users = new UserImpl[3];
    for (int i = 0; i < 3; i++) {
      users[i] = new UserImpl("user" + i, "user" + i + "@test.com", "username" + i);
    }
    stackPermissions = new StackPermissionsImpl[3];
    for (int i = 0; i < 3; i++) {
      stackPermissions[i] =
          new StackPermissionsImpl(users[i].getId(), stack.getId(), asList("read", "update"));
    }

    Injector injector = Guice.createInjector(new JpaTckModule());

    manager = injector.getInstance(EntityManager.class);
    stackDao = injector.getInstance(JpaStackDao.class);
    stackPermissionsDao = injector.getInstance(JpaStackPermissionsDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);

    subscriber = injector.getInstance(RemovePermissionsBeforeStackRemovedEventSubscriber.class);
    subscriber.subscribe();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    manager.persist(stack);
    Stream.of(users).forEach(manager::persist);
    Stream.of(stackPermissions).forEach(manager::persist);
    manager.getTransaction().commit();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();
    manager
        .createQuery("SELECT usr FROM Usr usr", UserImpl.class)
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
  public void shouldRemoveAllRecipePermissionsWhenRecipeIsRemoved() throws Exception {
    stackDao.remove(stack.getId());

    assertEquals(stackPermissionsDao.getByInstance(stack.getId(), 1, 0).getTotalItemsCount(), 0);
  }

  @Test
  public void shouldRemoveAllRecipePermissionsWhenPageSizeEqualsToOne() throws Exception {
    subscriber.removeStackPermissions(stack.getId(), 1);

    assertEquals(stackPermissionsDao.getByInstance(stack.getId(), 1, 0).getTotalItemsCount(), 0);
  }

  public static class TestDomain extends AbstractPermissionsDomain<StackPermissionsImpl> {
    public TestDomain() {
      super("stack", asList("read", "write", "use", "delete"));
    }

    @Override
    protected StackPermissionsImpl doCreateInstance(
        String userId, String instanceId, List<String> allowedActions) {
      return new StackPermissionsImpl(userId, instanceId, allowedActions);
    }
  }
}

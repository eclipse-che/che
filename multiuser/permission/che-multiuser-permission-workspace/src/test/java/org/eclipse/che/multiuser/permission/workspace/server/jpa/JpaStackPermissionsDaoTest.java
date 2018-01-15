/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.jpa;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.event.BeforeStackRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
public class JpaStackPermissionsDaoTest {
  private TckResourcesCleaner tckResourcesCleaner;

  private EntityManager manager;

  private JpaStackPermissionsDao dao;

  private JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber
      removePermissionsSubscriber;

  private StackPermissionsImpl[] permissions;
  private UserImpl[] users;
  private StackImpl[] stacks;

  @BeforeClass
  public void setupEntities() throws Exception {
    permissions =
        new StackPermissionsImpl[] {
          new StackPermissionsImpl("user1", "stack1", asList("read", "use", "run")),
          new StackPermissionsImpl("user2", "stack1", asList("read", "use")),
          new StackPermissionsImpl("user1", "stack2", asList("read", "run")),
          new StackPermissionsImpl("user2", "stack2", asList("read", "use", "run", "configure"))
        };

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    stacks =
        new StackImpl[] {
          new StackImpl("stack1", "st1", null, null, null, null, null, null, null, null),
          new StackImpl("stack2", "st2", null, null, null, null, null, null, null, null)
        };

    Injector injector = Guice.createInjector(new WorkspaceTckModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(JpaStackPermissionsDao.class);
    removePermissionsSubscriber =
        injector.getInstance(
            JpaStackPermissionsDao.RemovePermissionsBeforeStackRemovedEventSubscriber.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    for (UserImpl user : users) {
      manager.persist(user);
    }

    for (StackImpl stack : stacks) {
      manager.persist(stack);
    }

    for (StackPermissionsImpl stackPermissions : permissions) {
      manager.persist(stackPermissions);
    }
    manager.getTransaction().commit();
    manager.clear();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();

    manager
        .createQuery("SELECT p FROM StackPermissions p", StackPermissionsImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT r FROM Stack r", StackImpl.class)
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
    tckResourcesCleaner.clean();
  }

  @Test
  public void shouldRemoveStackPermissionsWhenStackIsRemoved() throws Exception {
    BeforeStackRemovedEvent event = new BeforeStackRemovedEvent(stacks[0]);
    removePermissionsSubscriber.onEvent(event);
    assertTrue(dao.getByInstance("stack1", 30, 0).isEmpty());
  }

  @Test
  public void shouldStoreStackPublicPermission() throws Exception {
    final StackPermissionsImpl publicPermission =
        new StackPermissionsImpl("*", "stack1", asList("read", "use", "run"));
    dao.store(publicPermission);

    assertTrue(
        dao.getByInstance(publicPermission.getInstanceId(), 30, 0)
            .getItems()
            .contains(new StackPermissionsImpl(publicPermission)));
  }

  @Test
  public void shouldUpdateExistingStackPublicPermissions() throws Exception {
    final StackPermissionsImpl publicPermission =
        new StackPermissionsImpl("*", "stack1", asList("read", "use", "run"));
    dao.store(publicPermission);
    dao.store(publicPermission);

    final Page<StackPermissionsImpl> permissions =
        dao.getByInstance(publicPermission.getInstanceId(), 30, 0);
    assertTrue(permissions.getItems().contains(new StackPermissionsImpl(publicPermission)));
    assertTrue(permissions.getItems().stream().filter(p -> "*".equals(p.getUserId())).count() == 1);
  }

  @Test
  public void shouldRemoveStackPublicPermission() throws Exception {
    final StackPermissionsImpl publicPermission =
        new StackPermissionsImpl("*", "stack1", asList("read", "use", "run"));
    dao.store(publicPermission);
    dao.remove(publicPermission.getUserId(), publicPermission.getInstanceId());

    Page<StackPermissionsImpl> byInstance =
        dao.getByInstance(publicPermission.getInstanceId(), 30, 0);
    assertTrue(byInstance.getItems().stream().filter(p -> "*".equals(p.getUserId())).count() == 0);
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

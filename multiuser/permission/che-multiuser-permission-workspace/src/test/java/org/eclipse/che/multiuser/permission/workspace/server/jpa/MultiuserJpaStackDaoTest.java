/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.MultiuserJpaStackDao;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
public class MultiuserJpaStackDaoTest {

  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private MultiuserJpaStackDao dao;

  private UserImpl[] users;
  private StackImpl[] stacks;

  @BeforeClass
  public void setupEntities() throws Exception {
    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    stacks =
        new StackImpl[] {
          new StackImpl(
              "stack1", "st1", null, null, null, Arrays.asList("tag1", "tag2"), null, null, null),
          new StackImpl("stack2", "st2", null, null, null, null, null, null, null),
          new StackImpl(
              "stack3", "st3", null, null, null, Arrays.asList("tag1", "tag2"), null, null, null),
          new StackImpl("stack4", "st4", null, null, null, null, null, null, null)
        };

    Injector injector = Guice.createInjector(new WorkspaceTckModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(MultiuserJpaStackDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    for (UserImpl user : users) {
      manager.persist(user);
    }

    for (StackImpl recipe : stacks) {
      manager.persist(recipe);
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
  public void shouldFindStackByDirectPermissions() throws Exception {
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[0].getId(), Arrays.asList("read", "use", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[1].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[2].getId(), Arrays.asList("read", "search")));

    List<StackImpl> results = dao.searchStacks(users[0].getId(), null, 0, 0);

    assertEquals(results.size(), 3);
    assertTrue(results.contains(stacks[0]));
    assertTrue(results.contains(stacks[1]));
    assertTrue(results.contains(stacks[2]));
  }

  @Test
  public void shouldFindStackByPublicPermissions() throws Exception {
    manager.persist(
        new StackPermissionsImpl("*", stacks[0].getId(), Arrays.asList("read", "use", "search")));

    List<StackImpl> results = dao.searchStacks(users[0].getId(), null, 0, 0);

    assertEquals(results.size(), 1);
    assertTrue(results.contains(stacks[0]));
  }

  @Test
  public void shouldFindStackByPublicAndDirectPermissions() throws Exception {
    manager.persist(
        new StackPermissionsImpl("*", stacks[0].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[0].getId(), Arrays.asList("read", "search")));

    List<StackImpl> results = dao.searchStacks(users[0].getId(), null, 0, 0);
    assertEquals(results.size(), 1);
    assertTrue(results.contains(stacks[0]));
  }

  @Test
  public void shouldFindRecipeByPermissionsAndTags() throws Exception {
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[0].getId(), Arrays.asList("read", "use", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[1].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[2].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[3].getId(), Arrays.asList("read", "search")));

    List<StackImpl> results =
        dao.searchStacks(users[0].getId(), Collections.singletonList("tag2"), 0, 0);
    assertEquals(results.size(), 2);
    assertTrue(results.contains(stacks[0]));
    assertTrue(results.contains(stacks[2]));
  }

  @Test
  public void shouldNotFindRecipeByNonexistentTags() throws Exception {
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[0].getId(), Arrays.asList("read", "use", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[1].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[2].getId(), Arrays.asList("read", "search")));
    manager.persist(
        new StackPermissionsImpl(
            users[0].getId(), stacks[3].getId(), Arrays.asList("read", "search")));

    List<StackImpl> results =
        dao.searchStacks(users[0].getId(), Collections.singletonList("unexisted_tag2"), 0, 0);

    assertTrue(results.isEmpty());
  }
}

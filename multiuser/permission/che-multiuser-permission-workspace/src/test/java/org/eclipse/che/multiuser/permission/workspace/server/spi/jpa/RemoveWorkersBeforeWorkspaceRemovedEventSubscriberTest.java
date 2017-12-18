/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.spi.jpa;

import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link RemoveWorkersBeforeWorkspaceRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemoveWorkersBeforeWorkspaceRemovedEventSubscriberTest {
  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private JpaWorkerDao workerDao;
  private JpaWorkspaceDao workspaceDao;

  private RemoveWorkersBeforeWorkspaceRemovedEventSubscriber subscriber;

  private WorkspaceImpl workspace;
  private WorkerImpl[] workers;
  private UserImpl[] users;
  private Account account;

  @BeforeClass
  public void setupEntities() throws Exception {
    account = new AccountImpl("account1", "accountName", "test");

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    workspace =
        new WorkspaceImpl(
            "ws1", account, new WorkspaceConfigImpl("", "", "cfg1", null, null, null));

    workers =
        new WorkerImpl[] {
          new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "run")),
          new WorkerImpl("ws1", "user2", Arrays.asList("read", "use"))
        };

    Injector injector = Guice.createInjector(new JpaTckModule());

    manager = injector.getInstance(EntityManager.class);
    workerDao = injector.getInstance(JpaWorkerDao.class);
    workspaceDao = injector.getInstance(JpaWorkspaceDao.class);
    subscriber = injector.getInstance(RemoveWorkersBeforeWorkspaceRemovedEventSubscriber.class);
    subscriber.subscribe();
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    manager.persist(account);
    manager.persist(workspace);
    Stream.of(users).forEach(manager::persist);
    Stream.of(workers).forEach(manager::persist);
    manager.getTransaction().commit();
    manager.clear();
  }

  @AfterMethod
  public void cleanup() {
    manager.getTransaction().begin();

    manager
        .createQuery("SELECT e FROM Worker e", WorkerImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT w FROM Workspace w", WorkspaceImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT u FROM Usr u", UserImpl.class)
        .getResultList()
        .forEach(manager::remove);

    manager
        .createQuery("SELECT a FROM Account a", AccountImpl.class)
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
  public void shouldRemoveAllWorkersWhenWorkspaceIsRemoved() throws Exception {
    workspaceDao.remove(workspace.getId());

    assertEquals(workerDao.getWorkers(workspace.getId(), 1, 0).getTotalItemsCount(), 0);
  }

  @Test
  public void shouldRemoveAllWorkersWhenPageSizeEqualsToOne() throws Exception {
    subscriber.removeWorkers(workspace.getId(), 1);

    assertEquals(workerDao.getWorkers(workspace.getId(), 1, 0).getTotalItemsCount(), 0);
  }
}

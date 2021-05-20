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
package org.eclipse.che.multiuser.permission.workspace.server.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.MultiuserJpaWorkspaceDao;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
public class MultiuserJpaWorkspaceDaoTest {
  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private MultiuserJpaWorkspaceDao dao;

  private AccountImpl account;
  private WorkerImpl[] workers;
  private UserImpl[] users;
  private WorkspaceImpl[] workspaces;

  @BeforeClass
  public void setupEntities() throws Exception {
    workers =
        new WorkerImpl[] {
          new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "search")),
          new WorkerImpl("ws2", "user1", Arrays.asList("read", "search")),
          new WorkerImpl("ws3", "user1", Arrays.asList("none", "run")),
          new WorkerImpl("ws1", "user2", Arrays.asList("read", "use"))
        };

    users =
        new UserImpl[] {
          new UserImpl("user1", "user1@com.com", "usr1"),
          new UserImpl("user2", "user2@com.com", "usr2")
        };

    account = new AccountImpl("account1", "accountName", "test");

    workspaces =
        new WorkspaceImpl[] {
          new WorkspaceImpl(
              "ws1",
              account,
              new WorkspaceConfigImpl("wrksp1", "", "cfg1", null, null, null, null)),
          new WorkspaceImpl(
              "ws2",
              account,
              new WorkspaceConfigImpl("wrksp2", "", "cfg2", null, null, null, null)),
          new WorkspaceImpl(
              "ws3", account, new WorkspaceConfigImpl("wrksp3", "", "cfg3", null, null, null, null))
        };
    Injector injector = Guice.createInjector(new WorkspaceTckModule());
    manager = injector.getInstance(EntityManager.class);
    dao = injector.getInstance(MultiuserJpaWorkspaceDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    manager.getTransaction().begin();
    manager.persist(account);

    for (UserImpl user : users) {
      manager.persist(user);
    }

    for (WorkspaceImpl ws : workspaces) {
      manager.persist(ws);
    }

    for (WorkerImpl worker : workers) {
      manager.persist(worker);
    }
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

  @Test
  public void shouldGetTotalWorkspaceCount() throws ServerException {
    assertEquals(dao.getWorkspacesTotalCount(), 3);
  }

  @AfterClass
  public void shutdown() throws Exception {
    tckResourcesCleaner.clean();
  }

  @Test
  public void shouldFindStackByPermissions() throws Exception {
    List<WorkspaceImpl> results = dao.getWorkspaces(users[0].getId(), 30, 0).getItems();
    assertEquals(results.size(), 2);
    assertTrue(results.contains(workspaces[0]));
    assertTrue(results.contains(workspaces[1]));
  }
}

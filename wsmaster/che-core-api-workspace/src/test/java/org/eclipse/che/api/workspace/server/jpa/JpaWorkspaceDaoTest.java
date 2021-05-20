/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.jpa;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.server.spi.tck.WorkspaceDaoTest.createWorkspaceFromConfig;
import static org.eclipse.che.api.workspace.server.spi.tck.WorkspaceDaoTest.createWorkspaceFromDevfile;
import static org.testng.Assert.assertEquals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests JPA specific use-cases.
 *
 * @author Yevhenii Voevodin
 */
public class JpaWorkspaceDaoTest {

  private TckResourcesCleaner tckResourcesCleaner;
  private EntityManager manager;
  private JpaWorkspaceDao workspaceDao;

  @BeforeMethod
  private void setUpManager() {
    final Injector injector = Guice.createInjector(new WorkspaceTckModule());
    manager = injector.getInstance(EntityManager.class);
    workspaceDao = injector.getInstance(JpaWorkspaceDao.class);
    tckResourcesCleaner = injector.getInstance(TckResourcesCleaner.class);
  }

  @AfterMethod
  private void cleanup() {
    manager.getTransaction().begin();
    final List<Object> entities = new ArrayList<>();
    entities.addAll(manager.createQuery("SELECT w FROM Workspace w").getResultList());
    entities.addAll(manager.createQuery("SELECT a FROM Account a").getResultList());
    for (Object entity : entities) {
      manager.remove(entity);
    }
    manager.getTransaction().commit();
    tckResourcesCleaner.clean();
  }

  @Test
  public void shouldCascadeRemoveObjectsWhenTheyRemovedFromEntity() {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace = createWorkspaceFromConfig("id", account, "name");

    // Persist the account
    manager.getTransaction().begin();
    manager.persist(account);
    manager.getTransaction().commit();
    manager.clear();

    // Persist the workspace
    manager.getTransaction().begin();
    manager.persist(workspace);
    manager.getTransaction().commit();
    manager.clear();

    // Cleanup one to many dependencies
    manager.getTransaction().begin();
    final WorkspaceConfigImpl config = workspace.getConfig();
    config.getProjects().clear();
    config.getCommands().clear();
    config.getEnvironments().clear();
    manager.merge(workspace);
    manager.getTransaction().commit();
    manager.clear();

    // If all the One To Many dependencies are removed then all the embedded objects
    // which depend on those object are also removed, which guaranteed by foreign key constraints
    assertEquals(asLong("SELECT COUNT(p) FROM ProjectConfig p"), 0L, "Project configs");
    assertEquals(asLong("SELECT COUNT(c) FROM Command c"), 0L, "Commands");
    assertEquals(asLong("SELECT COUNT(e) FROM Environment e"), 0L, "Environments");
  }

  @Test(expectedExceptions = DuplicateKeyException.class)
  public void shouldSynchronizeWorkspaceNameWithConfigNameWhenConfigIsUpdated() throws Exception {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace1 = createWorkspaceFromConfig("id", account, "name1");
    final WorkspaceImpl workspace2 = createWorkspaceFromConfig("id2", account, "name2");

    // persist prepared data
    manager.getTransaction().begin();
    manager.persist(account);
    manager.persist(workspace1);
    manager.persist(workspace2);
    manager.getTransaction().commit();

    // make conflict update
    workspace2.getConfig().setName(workspace1.getName());
    manager.getTransaction().begin();
    manager.merge(workspace2);
    manager.getTransaction().commit();
  }

  @Test(expectedExceptions = DuplicateKeyException.class)
  public void shouldSynchronizeWorkspaceNameWithDevfileNameWhenDevfileIsUpdated() throws Exception {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace1 = createWorkspaceFromDevfile("id", account, "name1");
    final WorkspaceImpl workspace2 = createWorkspaceFromDevfile("id2", account, "name2");

    // persist prepared data
    manager.getTransaction().begin();
    manager.persist(account);
    manager.persist(workspace1);
    manager.persist(workspace2);
    manager.getTransaction().commit();

    // make conflict update
    workspace2.getDevfile().setName(workspace1.getDevfile().getName());
    manager.getTransaction().begin();
    manager.merge(workspace2);
    manager.getTransaction().commit();
  }

  @Test
  public void shouldSyncDbAttributesWhileUpdatingWorkspace() throws Exception {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace = createWorkspaceFromConfig("id", account, "name");
    if (workspace.getConfig() != null) {
      workspace.getConfig().getProjects().forEach(ProjectConfigImpl::prePersistAttributes);
    }
    // persist the workspace
    manager.getTransaction().begin();
    manager.persist(account);
    manager.persist(workspace);
    manager.getTransaction().commit();
    manager.clear();

    // put a new attribute
    workspace
        .getConfig()
        .getProjects()
        .get(0)
        .getAttributes()
        .put("new-attr", singletonList("value"));
    WorkspaceImpl result = workspaceDao.update(workspace);

    manager.clear();

    // check it's okay
    assertEquals(result.getConfig().getProjects().get(0).getAttributes().size(), 3);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldNotSaveDevfileWithoutMetadata() {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace = createWorkspaceFromDevfile("id", account, "name");
    workspace.getDevfile().setMetadata(null);

    try {
      // persist the workspace
      manager.getTransaction().begin();
      manager.persist(account);
      manager.persist(workspace);
      manager.getTransaction().commit();
    } finally {
      manager.getTransaction().rollback();
      manager.clear();
    }
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldNotSaveDevfileWithoutMetadataName() {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace = createWorkspaceFromDevfile("id", account, "name");
    workspace.getDevfile().getMetadata().setName(null);

    try {
      // persist the workspace
      manager.getTransaction().begin();
      manager.persist(account);
      manager.persist(workspace);
      manager.getTransaction().commit();
    } finally {
      manager.getTransaction().rollback();
      manager.clear();
    }
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldNotSaveDevfileWithEmptyMetadataName() {
    final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
    final WorkspaceImpl workspace = createWorkspaceFromDevfile("id", account, "name");
    workspace.getDevfile().getMetadata().setName("");

    try {
      // persist the workspace
      manager.getTransaction().begin();
      manager.persist(account);
      manager.persist(workspace);
      manager.getTransaction().commit();
    } finally {
      manager.getTransaction().rollback();
      manager.clear();
    }
  }

  private long asLong(String query) {
    return manager.createQuery(query, Long.class).getSingleResult();
  }
}

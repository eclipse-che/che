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
package org.eclipse.che.api.workspace.server.jpa;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.server.spi.tck.WorkspaceDaoTest.createWorkspace;
import static org.testng.Assert.assertEquals;

/**
 * Tests JPA specific use-cases.
 *
 * @author Yevhenii Voevodin
 */
public class JpaWorkspaceDaoTest {

    private EntityManager   manager;
    private JpaWorkspaceDao workspaceDao;
    private JpaCleaner      cleaner;

    @BeforeMethod
    private void setUpManager() {
        final Injector injector = Guice.createInjector(new WorkspaceTckModule());
        manager = injector.getInstance(EntityManager.class);
        workspaceDao = injector.getInstance(JpaWorkspaceDao.class);
        cleaner = injector.getInstance(H2JpaCleaner.class);
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
        cleaner.clean();
    }

    @Test
    public void shouldCascadeRemoveObjectsWhenTheyRemovedFromEntity() {
        final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
        final WorkspaceImpl workspace = createWorkspace("id", account, "name");

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
        final WorkspaceImpl workspace1 = createWorkspace("id", account, "name1");
        final WorkspaceImpl workspace2 = createWorkspace("id2", account, "name2");

        // persist prepared data
        manager.getTransaction().begin();
        manager.persist(account);
        manager.persist(workspace1);
        manager.persist(workspace2);
        manager.getTransaction().commit();

        // make conflict update
        workspace2.getConfig().setName(workspace1.getConfig().getName());
        manager.getTransaction().begin();
        manager.merge(workspace2);
        manager.getTransaction().commit();
    }

    @Test
    public void shouldSyncDbAttributesWhileUpdatingWorkspace() throws Exception {
        final AccountImpl account = new AccountImpl("accountId", "namespace", "test");
        final WorkspaceImpl workspace = createWorkspace("id", account, "name");

        // persist the workspace
        manager.getTransaction().begin();
        manager.persist(account);
        manager.persist(workspace);
        manager.getTransaction().commit();
        manager.clear();

        // put a new attribute
        workspace.getConfig()
                 .getProjects()
                 .get(0)
                 .getAttributes()
                 .put("new-attr", singletonList("value"));
        workspaceDao.update(workspace);

        manager.clear();

        // check it's okay
        assertEquals(workspaceDao.get(workspace.getId())
                                 .getConfig()
                                 .getProjects()
                                 .get(0)
                                 .getAttributes()
                                 .size(), 3);
    }

    private long asLong(String query) {
        return manager.createQuery(query, Long.class).getSingleResult();
    }
}

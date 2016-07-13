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

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;

import static org.eclipse.che.api.workspace.server.jpa.WorkspaceDaoTest.createWorkspace;
import static org.testng.Assert.assertEquals;

/**
 * Tests JPA specific use-cases.
 *
 * @author Yevhenii Voevodin
 */
public class JpaWorkspaceDaoTest {

    private EntityManager manager;

    @BeforeMethod
    private void setUpManager() {
        manager = Guice.createInjector(new WorkspaceTckModule()).getInstance(EntityManager.class);
    }

    @Test
    public void shouldCascadeRemoveObjectsWhenTheyRemovedFromEntity() {
        final WorkspaceImpl workspace = createWorkspace("id", "namespace", "name");

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

    private long asLong(String query) {
        return manager.createQuery(query, Long.class).getSingleResult();
    }
}

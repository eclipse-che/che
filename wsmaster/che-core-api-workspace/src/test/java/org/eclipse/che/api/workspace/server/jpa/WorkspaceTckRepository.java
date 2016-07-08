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

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import java.util.Collection;

@Transactional
public class WorkspaceTckRepository implements TckRepository<WorkspaceImpl> {

    @Inject
    private Provider<EntityManager> manager;

    @Override
    public void createAll(Collection<? extends WorkspaceImpl> entities) throws TckRepositoryException {
        for (WorkspaceImpl entity : entities) {
            entity.getConfig().getProjects().forEach(ProjectConfigImpl::syncDbAttributes);
            manager.get().persist(entity);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        for (WorkspaceImpl workspace : manager.get().createQuery("SELECT w FROM Workspace w", WorkspaceImpl.class).getResultList()) {
            manager.get().remove(workspace);
        }
    }
}

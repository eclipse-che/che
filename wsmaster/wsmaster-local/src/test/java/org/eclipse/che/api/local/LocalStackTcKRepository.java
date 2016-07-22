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
package org.eclipse.che.api.local;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Yevhenii Voevodin
 */
public class LocalStackTcKRepository implements TckRepository<StackImpl> {

    @Inject
    private LocalStackDaoImpl stackDao;

    @Override
    public void createAll(Collection<? extends StackImpl> entities) throws TckRepositoryException {
        for (StackImpl stack : entities) {
            stackDao.stacks.put(stack.getId(), new StackImpl(stack));
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        stackDao.stacks.clear();
    }
}

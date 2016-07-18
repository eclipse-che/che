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

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Yevhenii Voevodin
 */
public class LocalUserTckRepository implements TckRepository<UserImpl> {

    @Inject
    private LocalUserDaoImpl userDao;

    @Override
    public void createAll(Collection<? extends UserImpl> entities) {
        for (UserImpl user : entities) {
            userDao.users.put(user.getId(), new UserImpl(user));
        }
    }

    @Override
    public void removeAll() {
        userDao.users.clear();
    }
}

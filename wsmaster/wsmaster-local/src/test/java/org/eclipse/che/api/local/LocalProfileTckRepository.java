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

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author Yevhenii Voevodin
 */
public class LocalProfileTckRepository implements TckRepository<ProfileImpl> {

    @Inject
    private LocalProfileDaoImpl profileDao;

    @Override
    public void createAll(Collection<? extends ProfileImpl> profiles) throws TckRepositoryException {
        for (ProfileImpl profile : profiles) {
            profileDao.profiles.put(profile.getUserId(), new ProfileImpl(profile));
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        profileDao.profiles.clear();
    }
}

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

import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * @author Anton Korneta
 */
public class LocalPreferenceTckRepository implements TckRepository<Pair<String, Map<String, String>>> {

    @Inject
    private LocalPreferenceDaoImpl preferenceDao;

    @Override
    public void createAll(Collection<? extends Pair<String, Map<String, String>>> entities) throws TckRepositoryException {
        for (Pair<String, Map<String, String>> entity : entities) {
            preferenceDao.preferences.put(entity.first, entity.second);
        }
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        preferenceDao.preferences.clear();
    }
}

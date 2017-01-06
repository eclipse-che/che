/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mihail Kuznyetsov
 */
public class LocalSshTckRepository implements TckRepository<SshPairImpl> {
    @Inject
    private LocalSshDaoImpl sshDao;

    @Override
    public void createAll(Collection<? extends SshPairImpl> entities) throws TckRepositoryException {
        sshDao.pairs.addAll(entities);
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        sshDao.pairs.clear();
    }
}

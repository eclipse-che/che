/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.jpa;

import com.google.inject.persist.Transactional;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

/** @author Anatolii Bazko */
@Transactional
public class InstallerJpaTckRepository implements TckRepository<InstallerImpl> {

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void createAll(Collection<? extends InstallerImpl> entities)
      throws TckRepositoryException {
    final EntityManager manager = managerProvider.get();
    entities.forEach(manager::persist);
  }

  @Override
  public void removeAll() throws TckRepositoryException {
    managerProvider
        .get()
        .createQuery("SELECT i FROM Inst i", InstallerImpl.class)
        .getResultList()
        .forEach(managerProvider.get()::remove);
  }
}

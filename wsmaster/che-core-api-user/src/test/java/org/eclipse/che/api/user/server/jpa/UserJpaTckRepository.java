/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.jpa;

import com.google.inject.persist.Transactional;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.security.PasswordEncryptor;

@Transactional
public class UserJpaTckRepository implements TckRepository<UserImpl> {

  @Inject private Provider<EntityManager> managerProvider;

  @Inject private PasswordEncryptor encryptor;

  @Override
  public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
    final EntityManager manager = managerProvider.get();
    entities
        .stream()
        .map(
            user ->
                new UserImpl(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    encryptor.encrypt(user.getPassword()),
                    user.getAliases()))
        .forEach(manager::persist);
  }

  @Override
  public void removeAll() throws TckRepositoryException {
    managerProvider
        .get()
        .createQuery("SELECT u FROM Usr u", UserImpl.class)
        .getResultList()
        .forEach(managerProvider.get()::remove);
  }
}

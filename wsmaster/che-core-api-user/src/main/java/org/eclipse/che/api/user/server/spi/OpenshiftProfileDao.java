/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.spi;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;

@Singleton
public class OpenshiftProfileDao implements ProfileDao {

  private final UserDao userDao;

  @Inject
  public OpenshiftProfileDao(UserDao userDao) {
    this.userDao = userDao;
  }

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void update(ProfileImpl profile) throws NotFoundException, ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void remove(String id) throws ServerException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public ProfileImpl getById(String id) throws NotFoundException, ServerException {
    User user = userDao.getById(id);
    return new ProfileImpl(user.getId());
  }
}

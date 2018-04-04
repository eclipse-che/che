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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.core.db.jpa.DuplicateKeyException;
import org.eclipse.che.core.db.jpa.IntegrityConstraintViolationException;

@Singleton
public class JpaProfileDao implements ProfileDao {

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void create(ProfileImpl profile) throws ServerException, ConflictException {
    requireNonNull(profile, "Required non-null profile");
    try {
      doCreate(profile);
    } catch (DuplicateKeyException x) {
      throw new ConflictException(
          format("Profile for user with id '%s' already exists", profile.getUserId()));
    } catch (IntegrityConstraintViolationException x) {
      throw new ConflictException(
          format("User with id '%s' referenced by profile doesn't exist", profile.getUserId()));
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void update(ProfileImpl profile) throws NotFoundException, ServerException {
    requireNonNull(profile, "Required non-null profile");
    try {
      doUpdate(profile);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  public void remove(String id) throws ServerException {
    requireNonNull(id, "Required non-null id");
    try {
      doRemove(id);
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Override
  @Transactional
  public ProfileImpl getById(String userId) throws NotFoundException, ServerException {
    requireNonNull(userId, "Required non-null id");
    try {
      final EntityManager manager = managerProvider.get();
      final ProfileImpl profile = manager.find(ProfileImpl.class, userId);
      if (profile == null) {
        throw new NotFoundException(format("Couldn't find profile for user with id '%s'", userId));
      }
      manager.refresh(profile);
      return profile;
    } catch (RuntimeException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  @Transactional
  protected void doCreate(ProfileImpl profile) {
    EntityManager manager = managerProvider.get();
    manager.persist(profile);
    manager.flush();
  }

  @Transactional
  protected void doUpdate(ProfileImpl profile) throws NotFoundException {
    final EntityManager manager = managerProvider.get();
    if (manager.find(ProfileImpl.class, profile.getUserId()) == null) {
      throw new NotFoundException(
          format(
              "Couldn't update profile, because profile for user with id '%s' doesn't exist",
              profile.getUserId()));
    }
    manager.merge(profile);
    manager.flush();
  }

  @Transactional
  protected void doRemove(String userId) {
    final EntityManager manager = managerProvider.get();
    final ProfileImpl profile = manager.find(ProfileImpl.class, userId);
    if (profile != null) {
      manager.remove(profile);
      manager.flush();
    }
  }
}

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
package org.eclipse.che.multiuser.keycloak.server;

import static java.lang.System.currentTimeMillis;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.multiuser.api.account.personal.PersonalAccountUserManager;

/**
 * Extension of User Manager, providing utility operations related to Keycloak User management, and
 * overriding create/remove operations to be compatible with {@link
 * org.eclipse.che.multiuser.keycloak.server.dao.KeycloakProfileDao}
 *
 * @author Mykhailo Kuznietsov
 */
@Singleton
public class KeycloakUserManager extends PersonalAccountUserManager {

  @Inject
  public KeycloakUserManager(
      UserDao userDao,
      ProfileDao profileDao,
      PreferenceDao preferencesDao,
      AccountManager accountManager,
      EventService eventService,
      @Named("che.auth.reserved_user_names") String[] reservedNames) {
    super(userDao, profileDao, preferencesDao, reservedNames, accountManager, eventService);
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  protected void doCreate(UserImpl user, boolean isTemporary)
      throws ConflictException, ServerException {
    userDao.create(user);
    eventService.publish(new PostUserPersistedEvent(new UserImpl(user))).propagateException();
    preferencesDao.setPreferences(
        user.getId(),
        ImmutableMap.of(
            "temporary", Boolean.toString(isTemporary),
            "codenvy:created", Long.toString(currentTimeMillis())));
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  protected void doRemove(String id) throws ServerException {
    UserImpl user;
    try {
      user = userDao.getById(id);
    } catch (NotFoundException ignored) {
      return;
    }
    preferencesDao.remove(id);
    eventService.publish(new BeforeUserRemovedEvent(user)).propagateException();
    userDao.remove(id);
  }
}

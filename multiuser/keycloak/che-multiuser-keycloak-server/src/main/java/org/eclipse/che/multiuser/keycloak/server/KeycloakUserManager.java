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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
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

  /**
   * Method is used to retrieve user object from Che DB for given user {@code id}, {@code email},
   * and {@code username}. Various actualization operations may be performed:
   *
   * <p>- if user is found in Che DB by the given {@code id}, then it will check, if it's email,
   * matches the {@code email} , and update it in DB if necessary.
   *
   * <p>- if user is not found in Che DB by the given {@code id} , then attempt to create one. But
   * also, it will attempt to get one by {@code email}. If such is found, he will be removed. That
   * way, there will be no conflict with existing user id or email upon recreation. In case of
   * conflict with user name, it may be prepended randomized symbols
   *
   * @param id - user id from
   * @param email - user email
   * @param username - user name
   * @return user object from Che Database, with all needed actualization operations performed on
   *     him
   * @throws ServerException if this exception during user creation, removal, or retrieval
   * @throws ConflictException if this exception occurs during user creation or removal
   */
  public User getOrCreateUser(String id, String email, String username)
      throws ServerException, ConflictException {
    Optional<User> userById = getUserById(id);
    if (!userById.isPresent()) {
      synchronized (this) {
        userById = getUserById(id);
        if (!userById.isPresent()) {
          Optional<User> userByEmail = getUserByEmail(email);
          if (userByEmail.isPresent()) {
            remove(userByEmail.get().getId());
          }
          final UserImpl cheUser = new UserImpl(id, email, username, generate("", 12), emptyList());
          try {
            return create(cheUser, false);
          } catch (ConflictException ex) {
            cheUser.setName(generate(cheUser.getName(), 4));
            return create(cheUser, false);
          }
        }
      }
    }
    return actualizeUserEmail(userById.get(), email);
  }

  /**
   * Performs check that {@code email} matches with the one in local DB, and synchronize them
   * otherwise
   */
  private User actualizeUserEmail(User actualUser, String email) throws ServerException {
    if (isNullOrEmpty(email) || actualUser.getEmail().equals(email)) {
      return actualUser;
    }
    UserImpl update = new UserImpl(actualUser);
    update.setEmail(email);
    try {
      update(update);
    } catch (NotFoundException e) {
      throw new ServerException("Unable to actualize user email. User not found.", e);
    } catch (ConflictException e) {
      throw new ServerException(
          "Unable to actualize user email. Another user with such email exists", e);
    }
    return update;
  }

  private Optional<User> getUserById(String id) throws ServerException {
    try {
      return Optional.of(getById(id));
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }

  private Optional<User> getUserByEmail(String email) throws ServerException {
    try {
      return Optional.of(getByEmail(email));
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }
}

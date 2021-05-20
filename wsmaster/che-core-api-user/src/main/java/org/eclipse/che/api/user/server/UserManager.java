/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.user.server.Constants.ID_LENGTH;
import static org.eclipse.che.api.user.server.Constants.PASSWORD_LENGTH;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.event.UserCreatedEvent;
import org.eclipse.che.api.user.server.event.UserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;

/**
 * Facade for {@link User} and {@link Profile} related operations.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Singleton
public class UserManager {

  public static final String PERSONAL_ACCOUNT = "personal";

  protected final UserDao userDao;
  protected final ProfileDao profileDao;
  protected final PreferenceDao preferencesDao;
  protected final Set<String> reservedNames;
  protected final EventService eventService;

  @Inject
  public UserManager(
      UserDao userDao,
      ProfileDao profileDao,
      PreferenceDao preferencesDao,
      EventService eventService,
      @Named("che.auth.reserved_user_names") String[] reservedNames) {
    this.userDao = userDao;
    this.profileDao = profileDao;
    this.preferencesDao = preferencesDao;
    this.eventService = eventService;
    this.reservedNames = Sets.newHashSet(reservedNames);
  }

  /**
   * Creates new user and his profile.
   *
   * @param newUser created user
   * @throws NullPointerException when {@code newUser} is null
   * @throws ConflictException when user with such name/email/alias already exists
   * @throws ServerException when any other error occurs
   */
  public User create(User newUser, boolean isTemporary) throws ConflictException, ServerException {
    requireNonNull(newUser, "Required non-null user");
    if (reservedNames.contains(newUser.getName().toLowerCase())) {
      throw new ConflictException(String.format("Username '%s' is reserved", newUser.getName()));
    }
    final String userId = newUser.getId() != null ? newUser.getId() : generate("user", ID_LENGTH);
    final UserImpl user =
        new UserImpl(
            userId,
            newUser.getEmail(),
            newUser.getName(),
            firstNonNull(newUser.getPassword(), generate("", PASSWORD_LENGTH)),
            newUser.getAliases());
    doCreate(user, isTemporary);
    eventService.publish(new UserCreatedEvent(user));
    return user;
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  protected void doCreate(UserImpl user, boolean isTemporary)
      throws ConflictException, ServerException {
    userDao.create(user);
    eventService.publish(new PostUserPersistedEvent(new UserImpl(user))).propagateException();
    profileDao.create(new ProfileImpl(user.getId()));
    preferencesDao.setPreferences(
        user.getId(),
        ImmutableMap.of(
            "temporary", Boolean.toString(isTemporary),
            "codenvy:created", Long.toString(currentTimeMillis())));
  }

  /**
   * Updates user by replacing an existing user entity with a new one.
   *
   * @param user user update
   * @throws NullPointerException when {@code user} is null
   * @throws NotFoundException when user with id {@code user.getId()} is not found
   * @throws ConflictException when user's new alias/email/name is not unique
   * @throws ServerException when any other error occurs
   */
  public void update(User user) throws NotFoundException, ServerException, ConflictException {
    requireNonNull(user, "Required non-null user");
    userDao.update(new UserImpl(user));
  }

  /**
   * Finds user by given {@code id}.
   *
   * @param id user identifier
   * @return user instance
   * @throws NullPointerException when {@code id} is null
   * @throws NotFoundException when user doesn't exist
   * @throws ServerException when any other error occurs
   */
  public User getById(String id) throws NotFoundException, ServerException {
    requireNonNull(id, "Required non-null id");
    return userDao.getById(id);
  }

  /**
   * Finds user by given {@code alias}.
   *
   * @param alias user alias
   * @return user instance
   * @throws NullPointerException when {@code alias} is null
   * @throws NotFoundException when user doesn't exist
   * @throws ServerException when any other error occurs
   */
  public User getByAlias(String alias) throws NotFoundException, ServerException {
    requireNonNull(alias, "Required non-null alias");
    return userDao.getByAlias(alias);
  }

  /**
   * Finds user by given {@code name}.
   *
   * @param name user name
   * @return user instance
   * @throws NullPointerException when {@code name} is null
   * @throws NotFoundException when user doesn't exist
   * @throws ServerException when any other error occurs
   */
  public User getByName(String name) throws NotFoundException, ServerException {
    requireNonNull(name, "Required non-null name");
    return userDao.getByName(name);
  }

  /**
   * Finds user by given {@code email}.
   *
   * @param email user email
   * @return user instance
   * @throws NullPointerException when {@code email} is null
   * @throws NotFoundException when user doesn't exist
   * @throws ServerException when any other error occurs
   */
  public User getByEmail(String email) throws NotFoundException, ServerException {
    requireNonNull(email, "Required non-null email");
    return userDao.getByEmail(email);
  }

  /**
   * Finds all users {@code email}.
   *
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return user instance
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   * @throws ServerException when any other error occurs
   */
  public Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException {
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");
    return userDao.getAll(maxItems, skipCount);
  }

  /**
   * Returns all users whose email address contains specified {@code emailPart}.
   *
   * @param emailPart fragment of user's email
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return list of matched users
   * @throws NullPointerException when {@code emailPart} is null
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative or when
   *     {@code skipCount} more than {@value Integer#MAX_VALUE}
   * @throws ServerException when any other error occurs
   */
  public Page<? extends User> getByEmailPart(String emailPart, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(emailPart, "Required non-null email part");
    checkArgument(maxItems >= 0, "The number of items to return can't be negative");
    checkArgument(
        skipCount >= 0 && skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
    return userDao.getByEmailPart(emailPart, maxItems, skipCount);
  }

  /**
   * Returns all users whose name contains specified {@code namePart}.
   *
   * @param namePart fragment of user's name
   * @param maxItems the maximum number of users to return
   * @param skipCount the number of users to skip
   * @return list of matched users
   * @throws NullPointerException when {@code namePart} is null
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative or when
   *     {@code skipCount} more than {@value Integer#MAX_VALUE}
   * @throws ServerException when any other error occurs
   */
  public Page<? extends User> getByNamePart(String namePart, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(namePart, "Required non-null name part");
    checkArgument(maxItems >= 0, "The number of items to return can't be negative");
    checkArgument(
        skipCount >= 0 && skipCount <= Integer.MAX_VALUE,
        "The number of items to skip can't be negative or greater than " + Integer.MAX_VALUE);
    return userDao.getByNamePart(namePart, maxItems, skipCount);
  }

  /**
   * Gets total count of all users
   *
   * @return user count
   * @throws ServerException when any error occurs
   */
  public long getTotalCount() throws ServerException {
    return userDao.getTotalCount();
  }

  /**
   * Removes user by given {@code id}.
   *
   * @param id user identifier
   * @throws NullPointerException when {@code id} is null
   * @throws ConflictException when given user cannot be deleted
   * @throws ServerException when any other error occurs
   */
  public void remove(String id) throws ServerException, ConflictException {
    requireNonNull(id, "Required non-null id");
    doRemove(id);
    eventService.publish(new UserRemovedEvent(id));
  }

  @Transactional(
      rollbackOn = {RuntimeException.class, ServerException.class, ConflictException.class})
  protected void doRemove(String id) throws ConflictException, ServerException {
    UserImpl user;
    try {
      user = userDao.getById(id);
    } catch (NotFoundException ignored) {
      return;
    }
    preferencesDao.remove(id);
    profileDao.remove(id);
    try {
      eventService.publish(new BeforeUserRemovedEvent(user)).propagateException();
    } catch (ServerException e) {
      if (e.getCause() instanceof ConflictException) {
        throw (ConflictException) e.getCause();
      }
      throw e;
    }
    userDao.remove(id);
  }
}

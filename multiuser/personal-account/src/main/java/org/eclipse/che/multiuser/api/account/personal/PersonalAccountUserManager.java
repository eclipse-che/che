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
package org.eclipse.che.multiuser.api.account.personal;

import com.google.inject.persist.Transactional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;

/**
 * Manager that ensures that every user has one and only one personal account. Doesn't contain any
 * logic related to user changing.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class PersonalAccountUserManager extends UserManager {
  public static final String PERSONAL_ACCOUNT = "personal";

  private final AccountManager accountManager;

  @Inject
  public PersonalAccountUserManager(
      UserDao userDao,
      ProfileDao profileDao,
      PreferenceDao preferencesDao,
      @Named("che.auth.reserved_user_names") String[] reservedNames,
      AccountManager accountManager,
      EventService eventService) {
    super(userDao, profileDao, preferencesDao, eventService, reservedNames);
    this.accountManager = accountManager;
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  @Override
  public User create(User newUser, boolean isTemporary) throws ConflictException, ServerException {
    User createdUser = super.create(newUser, isTemporary);

    accountManager.create(
        new AccountImpl(createdUser.getId(), createdUser.getName(), PERSONAL_ACCOUNT));

    return createdUser;
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  @Override
  public void update(User user) throws NotFoundException, ServerException, ConflictException {
    User originalUser = getById(user.getId());

    if (!originalUser.getName().equals(user.getName())) {
      accountManager.update(new AccountImpl(user.getId(), user.getName(), PERSONAL_ACCOUNT));
    }

    super.update(user);
  }

  @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
  @Override
  public void remove(String id) throws ServerException, ConflictException {
    accountManager.remove(id);
    super.remove(id);
  }
}

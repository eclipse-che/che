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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Helps to convert to/from DTOs related to user.
 *
 * @author Anatoliy Bazko
 */
public final class DtoConverter {

  /** Converts {@link User} to {@link UserDto}. */
  public static UserDto asDto(User user) {
    return DtoFactory.getInstance()
        .createDto(UserDto.class)
        .withId(user.getId())
        .withEmail(user.getEmail())
        .withName(user.getName())
        .withAliases(user.getAliases());
  }

  private DtoConverter() {}
}

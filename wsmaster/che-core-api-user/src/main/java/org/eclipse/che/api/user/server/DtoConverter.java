/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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

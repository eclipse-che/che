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
package org.eclipse.che.multiuser.api.permission.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Validates that provided instance parameter is valid
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InstanceParameterValidator {
  private final PermissionsManager permissionsManager;

  @Inject
  public InstanceParameterValidator(PermissionsManager permissionsManager) {
    this.permissionsManager = permissionsManager;
  }

  /**
   * Validates that provided instance parameter is valid for specified domain
   *
   * @param domain the domain of specified {@code instance}
   * @param instance the instance to check
   * @throws BadRequestException if specified {@code domain} is null
   * @throws BadRequestException if specified {@code instance} is not valid
   * @throws NotFoundException if specified {@code domain} is unsupported
   */
  public void validate(String domain, @Nullable String instance)
      throws BadRequestException, NotFoundException {
    checkArgument(domain != null, "Domain id required");
    if (permissionsManager.getDomain(domain).isInstanceRequired() && instance == null) {
      throw new BadRequestException("Specified domain requires non nullable value for instance");
    }
  }

  private void checkArgument(boolean expression, String message) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(message);
    }
  }
}

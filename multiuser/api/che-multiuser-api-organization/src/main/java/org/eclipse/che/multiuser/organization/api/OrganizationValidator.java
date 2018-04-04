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
package org.eclipse.che.multiuser.organization.api;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountValidator;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Utils for organization validation.
 *
 * @author Sergii Leschenko
 */
public class OrganizationValidator {
  @Inject private AccountValidator accountValidator;

  /**
   * Checks whether given organization is valid.
   *
   * @param organization organization to check
   * @throws BadRequestException when organization is not valid
   */
  public void checkOrganization(Organization organization) throws BadRequestException {
    if (organization == null) {
      throw new BadRequestException("Organization required");
    }
    if (isNullOrEmpty(organization.getName())) {
      throw new BadRequestException("Organization name required");
    }
    if (!accountValidator.isValidName(organization.getName())) {
      throw new BadRequestException(
          "Organization name may only contain alphanumeric characters or single hyphens inside");
    }
  }
}

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
package org.eclipse.che.multiuser.resource.api.free;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.shared.dto.FreeResourcesLimitDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;

/**
 * Utils for validation of {@link FreeResourcesLimit}
 *
 * @author Sergii Leschenko
 */
@Singleton
public class FreeResourcesLimitValidator {
  private final ResourceValidator resourceValidator;

  @Inject
  public FreeResourcesLimitValidator(ResourceValidator resourceValidator) {
    this.resourceValidator = resourceValidator;
  }

  /**
   * Validates given {@code freeResourcesLimit}
   *
   * @param freeResourcesLimit resources limit to validate
   * @throws BadRequestException when {@code freeResourcesLimit} is null
   * @throws BadRequestException when any of {@code freeResourcesLimit.getResources} is not valid
   * @see ResourceValidator#validate(ResourceDto)
   */
  public void check(FreeResourcesLimitDto freeResourcesLimit) throws BadRequestException {
    if (freeResourcesLimit == null) {
      throw new BadRequestException("Missed free resources limit description.");
    }
    if (freeResourcesLimit.getAccountId() == null) {
      throw new BadRequestException("Missed account id.");
    }

    Set<String> resourcesToSet = new HashSet<>();
    for (ResourceDto resource : freeResourcesLimit.getResources()) {
      if (!resourcesToSet.add(resource.getType())) {
        throw new BadRequestException(
            format(
                "Free resources limit should contain only one resources with type '%s'.",
                resource.getType()));
      }
      resourceValidator.validate(resource);
    }
  }
}

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

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.multiuser.resource.api.type.ResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;

/**
 * Utils for validation of {@link Resource}.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceValidator {
  private final Map<String, Set<String>> resourcesTypesToUnits;
  private final Map<String, String> resourcesTypesToDefaultUnit;

  @Inject
  public ResourceValidator(Set<ResourceType> supportedResources) {
    this.resourcesTypesToUnits =
        supportedResources
            .stream()
            .collect(toMap(ResourceType::getId, ResourceType::getSupportedUnits));
    this.resourcesTypesToDefaultUnit =
        supportedResources
            .stream()
            .collect(toMap(ResourceType::getId, ResourceType::getDefaultUnit));
  }

  /**
   * Validates given {@code resource}
   *
   * <p>{@link ResourceDto#getUnit()} can be null then {@link ResourceType#getDefaultUnit() default
   * unit} of {@link ResourceDto#getType() specified type} will be set.
   *
   * @param resource resource to validate
   * @throws BadRequestException when {@code resource} is null
   * @throws BadRequestException when {@code resource} has non supported type
   * @throws BadRequestException when {@code resource} has non supported unit
   */
  public void validate(ResourceDto resource) throws BadRequestException {
    if (resource == null) {
      throw new BadRequestException("Missed resource");
    }

    final Set<String> units = resourcesTypesToUnits.get(resource.getType());

    if (units == null) {
      throw new BadRequestException(
          "Specified resources type '" + resource.getType() + "' is not supported");
    }

    if (resource.getUnit() == null) {
      resource.setUnit(resourcesTypesToDefaultUnit.get(resource.getType()));
    } else {
      if (!units.contains(resource.getUnit())) {
        throw new BadRequestException(
            "Specified resources type '"
                + resource.getType()
                + "' support only following units: "
                + units.stream().collect(Collectors.joining(", ")));
      }
    }

    if (resource.getAmount() < -1) {
      throw new BadRequestException(
          "Resources with type '" + resource.getType() + "' has negative amount");
    }
  }
}

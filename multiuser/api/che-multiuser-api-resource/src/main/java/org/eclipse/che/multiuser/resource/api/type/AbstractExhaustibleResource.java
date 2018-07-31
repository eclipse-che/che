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
package org.eclipse.che.multiuser.resource.api.type;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Abstract resource that contains logic for aggregating and deduction for exhaustible resources.
 *
 * @author Sergii Leschenko
 */
public abstract class AbstractExhaustibleResource implements ResourceType {
  @Override
  public Resource aggregate(Resource resourceA, Resource resourceB) {
    checkResource(resourceA);
    checkResource(resourceB);

    if (resourceA.getAmount() == -1 || resourceB.getAmount() == -1) {
      return new ResourceImpl(getId(), -1, getDefaultUnit());
    }

    return new ResourceImpl(
        getId(), resourceA.getAmount() + resourceB.getAmount(), getDefaultUnit());
  }

  @Override
  public Resource deduct(Resource total, Resource deduction) throws NoEnoughResourcesException {
    checkResource(total);
    checkResource(deduction);

    if (total.getAmount() == -1) {
      return total;
    }

    if (deduction.getAmount() == -1) {
      throw new NoEnoughResourcesException(total, deduction, deduction);
    }

    final long resultAmount = total.getAmount() - deduction.getAmount();
    if (resultAmount < 0) {
      throw new NoEnoughResourcesException(
          total, deduction, new ResourceImpl(getId(), -resultAmount, getDefaultUnit()));
    }
    return new ResourceImpl(getId(), resultAmount, getDefaultUnit());
  }

  /**
   * Checks that given resources can be processed by this resource type
   *
   * @param resource resource to check
   * @throws IllegalArgumentException if given resources has unsupported type or unit
   */
  private void checkResource(Resource resource) {
    checkArgument(
        getId().equals(resource.getType()), "Resource should have '" + getId() + "' type");
    checkArgument(
        getSupportedUnits().contains(resource.getUnit()),
        "Resource has unsupported unit '" + resource.getUnit() + "'");
  }
}

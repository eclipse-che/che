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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Describes resource type that control the length of time that a user is idle with their workspace
 * when the system will suspend the workspace by snapshotting it and then stopping it.
 *
 * @author Sergii Leschenko
 */
public class TimeoutResourceType implements ResourceType {
  public static final String ID = "timeout";
  public static final String UNIT = "minute";

  private static final Set<String> SUPPORTED_UNITS = ImmutableSet.of(UNIT);

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDescription() {
    return "Timeout";
  }

  @Override
  public Set<String> getSupportedUnits() {
    return SUPPORTED_UNITS;
  }

  @Override
  public String getDefaultUnit() {
    return UNIT;
  }

  @Override
  public Resource aggregate(Resource resourceA, Resource resourceB) {
    return resourceA.getAmount() > resourceB.getAmount() ? resourceA : resourceB;
  }

  @Override
  public Resource deduct(Resource total, Resource deduction) throws NoEnoughResourcesException {
    return total;
  }
}

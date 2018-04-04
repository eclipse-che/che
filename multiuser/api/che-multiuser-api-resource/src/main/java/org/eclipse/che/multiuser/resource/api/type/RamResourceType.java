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
package org.eclipse.che.multiuser.resource.api.type;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * Describes resource type that control number of RAM which can be used by running workspaces at the
 * same time.
 *
 * @author Sergii Leschenko
 */
public class RamResourceType extends AbstractExhaustibleResource {
  public static final String ID = "RAM";
  public static final String UNIT = "mb";

  private static final Set<String> SUPPORTED_UNITS = ImmutableSet.of(UNIT);

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDescription() {
    return "Number of RAM which can be used by running workspaces at the same time";
  }

  @Override
  public Set<String> getSupportedUnits() {
    return SUPPORTED_UNITS;
  }

  @Override
  public String getDefaultUnit() {
    return UNIT;
  }
}

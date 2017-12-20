/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatolii Bazko */
@DTO
public interface BreakpointConfigurationDto extends BreakpointConfiguration {
  @Override
  boolean isConditionEnabled();

  void setConditionEnabled(boolean conditionEnabled);

  BreakpointConfigurationDto withConditionEnabled(boolean conditionEnabled);

  @Override
  String getCondition();

  void setCondition(String condition);

  BreakpointConfigurationDto withCondition(String condition);

  @Override
  boolean isHitCountEnabled();

  void setHitCountEnabled(boolean hitCountEnabled);

  BreakpointConfigurationDto withHitCountEnabled(boolean hitCountEnabled);

  @Override
  int getHitCount();

  void setHitCount(int hitCount);

  BreakpointConfigurationDto withHitCount(int hitCount);

  @Override
  SuspendPolicy getSuspendPolicy();

  void setSuspendPolicy(SuspendPolicy suspendPolicy);

  BreakpointConfigurationDto withSuspendPolicy(SuspendPolicy suspendPolicy);
}

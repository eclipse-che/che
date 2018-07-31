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

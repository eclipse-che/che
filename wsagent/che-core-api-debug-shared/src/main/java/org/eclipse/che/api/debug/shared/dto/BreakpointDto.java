/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface BreakpointDto extends Breakpoint {
  @Override
  LocationDto getLocation();

  void setLocation(LocationDto location);

  BreakpointDto withLocation(LocationDto location);

  @Override
  boolean isEnabled();

  @Override
  void setEnabled(boolean enabled);

  BreakpointDto withEnabled(boolean enabled);

  @Override
  BreakpointConfigurationDto getBreakpointConfiguration();

  void setBreakpointConfiguration(BreakpointConfigurationDto breakpointConfigurationDto);

  BreakpointDto withBreakpointConfiguration(BreakpointConfigurationDto breakpointConfigurationDto);
}

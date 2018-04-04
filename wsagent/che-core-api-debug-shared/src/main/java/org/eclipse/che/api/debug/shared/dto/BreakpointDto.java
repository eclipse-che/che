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

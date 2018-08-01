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
package org.eclipse.che.api.debug.shared.dto.event;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Event will be generated when breakpoint become active.
 *
 * @author Anatoliy Bazko
 */
@DTO
public interface BreakpointActivatedEventDto extends DebuggerEventDto {
  TYPE getType();

  void setType(TYPE type);

  BreakpointActivatedEventDto withType(TYPE type);

  BreakpointDto getBreakpoint();

  void setBreakpoint(BreakpointDto breakpoint);

  BreakpointActivatedEventDto withBreakpoint(BreakpointDto breakpoint);
}

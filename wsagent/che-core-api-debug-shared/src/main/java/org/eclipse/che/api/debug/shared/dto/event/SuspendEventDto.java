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

import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface SuspendEventDto extends SuspendEvent, DebuggerEventDto {
  @Override
  TYPE getType();

  void setType(TYPE type);

  SuspendEventDto withType(TYPE type);

  @Override
  LocationDto getLocation();

  void setLocation(LocationDto location);

  SuspendEventDto withLocation(LocationDto location);

  @Override
  SuspendPolicy getSuspendPolicy();

  void setSuspendPolicy(SuspendPolicy suspendPolicy);

  SuspendEventDto withSuspendPolicy(SuspendPolicy suspendPolicy);
}

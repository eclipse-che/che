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
package org.eclipse.che.api.system.shared.dto;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for {@link SystemStatusChangedEvent}.
 *
 * @author Yevhenii Voevodin
 */
@DTO
@EventOrigin("system")
public interface SystemStatusChangedEventDto extends SystemEventDto {

  /** Returns new status of the system. */
  SystemStatus getStatus();

  void setStatus(SystemStatus status);

  SystemStatusChangedEventDto withStatus(SystemStatus status);

  /** Returns the previous status of the system. */
  SystemStatus getPrevStatus();

  void setPrevStatus(SystemStatus prevStatus);

  SystemStatusChangedEventDto withPrevStatus(SystemStatus prevStatus);
}

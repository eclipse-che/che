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

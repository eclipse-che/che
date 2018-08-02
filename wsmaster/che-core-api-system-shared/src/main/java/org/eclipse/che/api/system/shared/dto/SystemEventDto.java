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
import org.eclipse.che.api.system.shared.event.EventType;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for {@link SystemEvent}.
 *
 * @author Yevhenii Voevodin
 */
@DTO
@EventOrigin("system")
public interface SystemEventDto extends SystemEvent {

  void setType(EventType type);

  SystemEventDto withType(EventType type);
}

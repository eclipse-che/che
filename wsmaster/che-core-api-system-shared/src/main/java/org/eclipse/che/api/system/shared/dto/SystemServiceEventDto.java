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

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for system service events.
 *
 * @author Yevhenii Voevodin
 */
@DTO
public interface SystemServiceEventDto extends SystemEventDto {

  /** Returns the name of the service described by this event. */
  String getService();

  void setService(String service);

  SystemServiceEventDto withService(String service);
}

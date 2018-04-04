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
package org.eclipse.che.api.system.server;

import org.eclipse.che.api.system.shared.dto.SystemEventDto;
import org.eclipse.che.api.system.shared.dto.SystemServiceEventDto;
import org.eclipse.che.api.system.shared.dto.SystemServiceItemStoppedEventDto;
import org.eclipse.che.api.system.shared.dto.SystemStatusChangedEventDto;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Converts events to corresponding DTOs.
 *
 * @author Yevhenii Voevodin
 */
public final class DtoConverter {

  /** Creates {@link SystemStatusChangedEventDto} from event. */
  public static SystemStatusChangedEventDto asDto(SystemStatusChangedEvent event) {
    SystemStatusChangedEventDto dto = DtoFactory.newDto(SystemStatusChangedEventDto.class);
    dto.setType(event.getType());
    dto.setStatus(event.getStatus());
    dto.setPrevStatus(event.getPrevStatus());
    return dto;
  }

  /** Creates {@link SystemServiceEventDto} from event. */
  public static SystemServiceEventDto asDto(SystemServiceEvent event) {
    SystemServiceEventDto dto = DtoFactory.newDto(SystemServiceEventDto.class);
    dto.setService(event.getServiceName());
    dto.setType(event.getType());
    return dto;
  }

  /** Creates {@link SystemServiceItemStoppedEventDto} from event. */
  public static SystemServiceItemStoppedEventDto asDto(SystemServiceItemStoppedEvent event) {
    SystemServiceItemStoppedEventDto dto =
        DtoFactory.newDto(SystemServiceItemStoppedEventDto.class);
    dto.setService(event.getServiceName());
    dto.setType(event.getType());
    dto.setCurrent(event.getCurrent());
    dto.setTotal(event.getTotal());
    dto.setItem(event.getItem());
    return dto;
  }

  /**
   * Converts given event to the corresponding DTO, if event type is unknown throws {@link
   * IllegalArgumentException}.
   */
  public static SystemEventDto asDto(SystemEvent event) {
    switch (event.getType()) {
      case STATUS_CHANGED:
        return asDto((SystemStatusChangedEvent) event);
      case SERVICE_ITEM_STOPPED:
        return asDto((SystemServiceItemStoppedEvent) event);
      case SERVICE_STOPPED:
      case STOPPING_SERVICE:
        return asDto((SystemServiceEvent) event);
      default:
        throw new IllegalArgumentException(
            "Can't convert event to dto, event type '" + event.getType() + "' is unknown");
    }
  }

  private DtoConverter() {}
}

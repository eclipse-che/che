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
package org.eclipse.che.api.system.server;

import static org.eclipse.che.api.system.shared.SystemStatus.PREPARING_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.RUNNING;
import static org.testng.Assert.assertEquals;

import java.util.EnumSet;
import org.eclipse.che.api.system.shared.dto.SystemServiceEventDto;
import org.eclipse.che.api.system.shared.dto.SystemServiceItemStoppedEventDto;
import org.eclipse.che.api.system.shared.dto.SystemStatusChangedEventDto;
import org.eclipse.che.api.system.shared.event.EventType;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.testng.annotations.Test;

/**
 * Tests {@link DtoConverter}.
 *
 * @author Yevhenii Voevodin
 */
public class DtoConverterTest {

  @Test
  public void convertsSystemStatusChangedEvent() {
    SystemStatusChangedEvent event = new SystemStatusChangedEvent(RUNNING, PREPARING_TO_SHUTDOWN);

    SystemStatusChangedEventDto dto = DtoConverter.asDto(event);

    assertEquals(dto.getType(), EventType.STATUS_CHANGED);
    assertEquals(dto.getPrevStatus(), event.getPrevStatus());
    assertEquals(dto.getStatus(), event.getStatus());
  }

  @Test
  public void convertsSystemServiceStoppingEvent() {
    StoppingSystemServiceEvent event = new StoppingSystemServiceEvent("service1");

    SystemServiceEventDto dto = DtoConverter.asDto(event);

    assertEquals(dto.getType(), EventType.STOPPING_SERVICE);
    assertEquals(dto.getService(), event.getServiceName());
  }

  @Test
  public void convertsSystemServiceStoppedEvent() {
    SystemServiceStoppedEvent event = new SystemServiceStoppedEvent("service1");

    SystemServiceEventDto dto = DtoConverter.asDto(event);

    assertEquals(dto.getType(), EventType.SERVICE_STOPPED);
    assertEquals(dto.getService(), event.getServiceName());
  }

  @Test
  public void convertsSystemServiceItemStoppedEvent() {
    SystemServiceItemStoppedEvent event =
        new SystemServiceItemStoppedEvent("service1", "workspace1", 3, 5);

    SystemServiceItemStoppedEventDto dto = DtoConverter.asDto(event);

    assertEquals(dto.getType(), EventType.SERVICE_ITEM_STOPPED);
    assertEquals(dto.getService(), event.getServiceName());
    assertEquals(dto.getItem(), event.getItem());
    assertEquals(dto.getCurrent(), event.getCurrent());
    assertEquals(dto.getTotal(), event.getTotal());
  }

  @Test
  public void allEventTypesAreHandled() {
    EnumSet<EventType> handled =
        EnumSet.of(
            EventType.STATUS_CHANGED,
            EventType.STOPPING_SERVICE,
            EventType.SUSPENDING_SERVICE,
            EventType.SERVICE_ITEM_STOPPED,
            EventType.SERVICE_ITEM_SUSPENDED,
            EventType.SERVICE_SUSPENDED,
            EventType.SERVICE_STOPPED);
    assertEquals(handled, EnumSet.allOf(EventType.class));
  }
}

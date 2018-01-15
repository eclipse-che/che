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

import static org.eclipse.che.api.system.shared.SystemStatus.PREPARING_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.RUNNING;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.eclipse.che.api.core.util.WebsocketLineConsumer;
import org.eclipse.che.api.system.shared.dto.SystemEventDto;
import org.eclipse.che.api.system.shared.event.SystemEvent;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test {@link SystemEventsWebsocketBroadcaster}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class SystemEventsWebsocketBroadcasterTest {

  @Mock private WebsocketLineConsumer messageCustomer;

  private SystemEventsWebsocketBroadcaster broadcaster;

  @BeforeMethod
  public void setUp() {
    broadcaster = new SystemEventsWebsocketBroadcaster(messageCustomer);
  }

  @Test(dataProvider = "eventToDto")
  public void sendsMessage(SystemEvent event, SystemEventDto dto) throws Exception {
    broadcaster.onEvent(event);

    verify(messageCustomer).writeLine(DtoFactory.getInstance().toJson(dto));
  }

  @Test
  public void sendExceptionsAreLoggedAndNotThrown() throws Exception {
    doThrow(new IOException("exception!")).when(messageCustomer).writeLine(anyString());

    broadcaster.onEvent(new SystemServiceStoppedEvent("service1"));
  }

  @DataProvider(name = "eventToDto")
  private static Object[][] eventToDto() {
    SystemStatusChangedEvent statusChanged =
        new SystemStatusChangedEvent(RUNNING, PREPARING_TO_SHUTDOWN);
    StoppingSystemServiceEvent stoppingService = new StoppingSystemServiceEvent("service1");
    SystemServiceStoppedEvent serviceStopped = new SystemServiceStoppedEvent("service1");
    SystemServiceItemStoppedEvent itemStopped =
        new SystemServiceItemStoppedEvent("service1", "item1", 5, 10);
    return new Object[][] {
      {statusChanged, DtoConverter.asDto(statusChanged)},
      {stoppingService, DtoConverter.asDto(stoppingService)},
      {serviceStopped, DtoConverter.asDto(serviceStopped)},
      {itemStopped, DtoConverter.asDto(itemStopped)}
    };
  }
}

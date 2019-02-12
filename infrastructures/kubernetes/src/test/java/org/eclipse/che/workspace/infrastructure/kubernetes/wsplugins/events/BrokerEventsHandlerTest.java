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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.shared.dto.BrokerStatus.DONE;
import static org.eclipse.che.api.workspace.shared.dto.BrokerStatus.FAILED;
import static org.eclipse.che.api.workspace.shared.dto.BrokerStatus.STARTED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Command;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class BrokerEventsHandlerTest {

  private static String OWNER = "test owner";
  private static String TEST_WORKSPACE = "test workspace";
  private static String TEST_ENV = "test env";

  // not initialized yet
  private static String JSON_OK;
  private static List<ChePlugin> TOOLING_OK;

  static {
    initPluginsList();
  }

  @Mock private EventService eventService;

  @InjectMocks private BrokerEventsHandler handler;

  @Test
  public void sendsLogEventToEventService() {
    String testTime = "test time";
    String testText = "test text";
    BrokerLogEvent event =
        newDto(BrokerLogEvent.class)
            .withTime(testTime)
            .withText(testText)
            .withRuntimeId(
                newDto(RuntimeIdentityDto.class)
                    .withOwnerId(OWNER)
                    .withWorkspaceId(TEST_WORKSPACE)
                    .withEnvName(TEST_ENV));

    handler.handle(event);

    verify(eventService)
        .publish(
            eq(
                newDto(RuntimeLogEvent.class)
                    .withTime(testTime)
                    .withText(testText)
                    .withRuntimeId(
                        newDto(RuntimeIdentityDto.class)
                            .withOwnerId(OWNER)
                            .withWorkspaceId(TEST_WORKSPACE)
                            .withEnvName(TEST_ENV))));
  }

  @Test(dataProvider = "brokerStatusEventToBrokerEventProvider")
  public void sendsBrokerEvent(BrokerStatusChangedEvent originEvent, BrokerEvent derivedEvent) {
    handler.handle(originEvent);

    ArgumentCaptor<BrokerEvent> captor = ArgumentCaptor.forClass(BrokerEvent.class);
    verify(eventService).publish(captor.capture());
    BrokerEvent value = captor.getValue();
    assertEquals(value.getRuntimeId(), derivedEvent.getRuntimeId());
    assertEquals(value.getStatus(), derivedEvent.getStatus());
    assertEquals(value.getTooling(), derivedEvent.getTooling());
    if (derivedEvent.getError() != null) {
      assertTrue(value.getError().startsWith(derivedEvent.getError()));
    } else {
      assertEquals(value.getError(), derivedEvent.getError());
    }
  }

  @DataProvider(name = "brokerStatusEventToBrokerEventProvider")
  public static Object[][] brokerStatusEventToBrokerEventProvider() {
    String ERROR_FROM_BROKER = "error from broker";
    String JSON_EMPTY_ARRAY = "[]";
    String JSON_BROKEN = "broken JSON";
    return new Object[][] {
      {
        // failed with error, broken tooling is ignored
        statusEventFailed().withError(ERROR_FROM_BROKER).withTooling(JSON_BROKEN),
        brokerEventFailed().withError(ERROR_FROM_BROKER)
      },
      {
        // failed with error, correct tooling is ignored
        statusEventFailed().withError(ERROR_FROM_BROKER).withTooling(JSON_OK),
        brokerEventFailed().withError(ERROR_FROM_BROKER)
      },
      {
        // failed with null error, tooling is null - leads to a specific error
        statusEventFailed().withError(null).withTooling(null),
        brokerEventFailed()
            .withError(
                format(
                    BrokerEventsHandler.NO_ERROR_NO_TOOLING_ERROR_TEMPLATE, OWNER, TEST_WORKSPACE))
      },
      {
        // failed with null error, tooling is empty - leads to a specific error
        statusEventFailed().withError(null).withTooling(""),
        brokerEventFailed()
            .withError(
                format(
                    BrokerEventsHandler.NO_ERROR_NO_TOOLING_ERROR_TEMPLATE, OWNER, TEST_WORKSPACE))
      },
      {
        // failed with empty error, tooling is null - leads to a specific error
        statusEventFailed().withError("").withTooling(null),
        brokerEventFailed()
            .withError(
                format(
                    BrokerEventsHandler.NO_ERROR_NO_TOOLING_ERROR_TEMPLATE, OWNER, TEST_WORKSPACE))
      },
      {
        // failed with empty error, tooling is empty - leads to a specific error
        statusEventFailed().withError("").withTooling(""),
        brokerEventFailed()
            .withError(
                format(
                    BrokerEventsHandler.NO_ERROR_NO_TOOLING_ERROR_TEMPLATE, OWNER, TEST_WORKSPACE))
      },
      {
        // done with no error, tolling is parsed
        statusEventDone().withTooling(JSON_OK), brokerEventDone().withTooling(TOOLING_OK)
      },
      {
        // done with no error, tolling is empty array
        statusEventDone().withTooling(JSON_EMPTY_ARRAY), brokerEventDone().withTooling(emptyList())
      },
      {
        // done with no error, tolling is broken - specific error
        statusEventDone().withTooling(JSON_BROKEN),
        brokerEventDone().withError("Parsing Che plugin broker event failed. Error:")
      },
      {
        // failed with no error, tooling is parsed
        statusEventFailed().withError(null).withTooling(JSON_OK),
        brokerEventFailed().withTooling(TOOLING_OK)
      },
      {
        // failed with no error, tooling is broken - specific error
        statusEventFailed().withError(null).withTooling(JSON_BROKEN),
        brokerEventFailed().withError("Parsing Che plugin broker event failed. Error: ")
      },
      {
        // started with no error, tooling is parsed
        statusEventStarted().withError(null).withTooling(JSON_OK),
        brokerEventStarted().withTooling(TOOLING_OK)
      },
      {
        // started with error, tooling is ignored
        statusEventStarted().withError("test error").withTooling(JSON_OK),
        brokerEventStarted().withError("test error").withTooling(null)
      }
    };
  }

  @Test(dataProvider = "incompleteEventProvider")
  public void doesNotSendBrokerEventIfEventIsIncomplete(BrokerStatusChangedEvent event) {
    handler.handle(event);

    verifyZeroInteractions(eventService);
  }

  @DataProvider(name = "incompleteEventProvider")
  public static Object[][] incompleteEventProvider() {
    return new Object[][] {
      {statusDoneEventWithDefaultItems().withStatus(null)},
      {statusDoneEventWithDefaultItems().withRuntimeId(null)},
      {statusDoneEventWithDefaultItems().withRuntimeId(runtimeID().withWorkspaceId(null))},
    };
  }

  private static BrokerStatusChangedEvent statusDoneEventWithDefaultItems() {
    return newDto(BrokerStatusChangedEvent.class)
        .withStatus(DONE)
        .withTooling(JSON_OK)
        .withRuntimeId(runtimeID());
  }

  private static BrokerStatusChangedEvent statusEventDone() {
    return newDto(BrokerStatusChangedEvent.class).withStatus(DONE).withRuntimeId(runtimeID());
  }

  private static BrokerStatusChangedEvent statusEventFailed() {
    return newDto(BrokerStatusChangedEvent.class).withStatus(FAILED).withRuntimeId(runtimeID());
  }

  private static BrokerStatusChangedEvent statusEventStarted() {
    return newDto(BrokerStatusChangedEvent.class).withStatus(STARTED).withRuntimeId(runtimeID());
  }

  private static BrokerEvent brokerEventFailed() {
    return new BrokerEvent().withRuntimeId(runtimeID()).withStatus(FAILED);
  }

  private static BrokerEvent brokerEventDone() {
    return new BrokerEvent().withRuntimeId(runtimeID()).withStatus(DONE);
  }

  private static BrokerEvent brokerEventStarted() {
    return new BrokerEvent().withRuntimeId(runtimeID()).withStatus(STARTED);
  }

  private static RuntimeIdentityDto runtimeID() {
    return newDto(RuntimeIdentityDto.class)
        .withOwnerId(OWNER)
        .withWorkspaceId(TEST_WORKSPACE)
        .withEnvName(TEST_ENV);
  }

  private static void initPluginsList() {
    ChePlugin plugin1 = new ChePlugin();
    plugin1
        .id("ID1")
        .name("Name1")
        .version("V1")
        .workspaceEnv(
            asList(
                new EnvVar().name("env1").value("val1"), new EnvVar().name("env2").value("val2")))
        .endpoints(
            singletonList(
                new ChePluginEndpoint()
                    .attributes(ImmutableMap.of("key1", "val1"))
                    .name("some endpoint")
                    .targetPort(8080)))
        .containers(
            singletonList(
                new CheContainer()
                    .commands(singletonList(new Command().name("name")))
                    .env(singletonList(new EnvVar().name("contEnv").value("val3")))
                    .image("some image")
                    .memoryLimit("some limit")
                    .mountSources(true)
                    .name("container name")
                    .ports(singletonList(new CheContainerPort().exposedPort(8080)))
                    .volumes(singletonList(new Volume().name("vol1").mountPath("/somewhere")))));
    TOOLING_OK = singletonList(plugin1);
    JSON_OK =
        "[{\"name\":\"Name1\",\"id\":\"ID1\",\"version\":\"V1\",\"containers\":[{\"image\":\"some image\",\"name\":\"container name\",\"env\":[{\"name\":\"contEnv\",\"value\":\"val3\"}],\"volumes\":[{\"name\":\"vol1\",\"mountPath\":\"/somewhere\"}],\"ports\":[{\"exposedPort\":8080}],\"editorCommands\":[{\"name\":\"name\",\"command\":[],\"workingDir\":null}],\"memoryLimit\":\"some limit\",\"mountSources\":true}],\"endpoints\":[{\"name\":\"some endpoint\",\"targetPort\":8080,\"attributes\":{\"key1\":\"val1\"},\"public\":false}],\"editors\":[],\"workspaceEnv\":[{\"name\":\"env1\",\"value\":\"val1\"},{\"name\":\"env2\",\"value\":\"val2\"}]}]";
  }
}

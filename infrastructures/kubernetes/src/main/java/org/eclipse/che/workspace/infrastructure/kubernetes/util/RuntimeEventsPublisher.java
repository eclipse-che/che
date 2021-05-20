/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.event.RuntimeAbnormalStoppedEvent;
import org.eclipse.che.api.workspace.server.event.RuntimeAbnormalStoppingEvent;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.event.WatchLogStartedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log.event.WatchLogStoppedEvent;

/** @author Anton Korneta */
@Singleton
public class RuntimeEventsPublisher {

  private final EventService eventService;

  @Inject
  public RuntimeEventsPublisher(EventService eventService) {
    this.eventService = eventService;
  }

  public void sendStartingEvent(String machineName, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(runtimeId))
            .withEventType(MachineStatus.STARTING)
            .withMachineName(machineName));
  }

  public void sendRunningEvent(String machineName, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(runtimeId))
            .withEventType(MachineStatus.RUNNING)
            .withMachineName(machineName));
  }

  public void sendFailedEvent(String machineName, String message, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(runtimeId))
            .withEventType(MachineStatus.FAILED)
            .withMachineName(machineName)
            .withError(message));
  }

  public void sendServerStatusEvent(
      String machineName, String serverName, Server server, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(ServerStatusEvent.class)
            .withIdentity(DtoConverter.asDto(runtimeId))
            .withMachineName(machineName)
            .withServerName(serverName)
            .withStatus(server.getStatus())
            .withServerUrl(server.getUrl()));
  }

  public void sendServerRunningEvent(
      String machineName, String serverName, String serverUrl, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(ServerStatusEvent.class)
            .withIdentity(DtoConverter.asDto(runtimeId))
            .withMachineName(machineName)
            .withServerName(serverName)
            .withStatus(ServerStatus.RUNNING)
            .withServerUrl(serverUrl));
  }

  public void sendMachineLogEvent(
      String machineName, String text, String time, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(RuntimeLogEvent.class)
            .withMachineName(machineName)
            .withRuntimeId(DtoConverter.asDto(runtimeId))
            .withText(text)
            .withTime(time));
  }

  public void sendRuntimeLogEvent(String text, String time, RuntimeIdentity runtimeId) {
    eventService.publish(
        DtoFactory.newDto(RuntimeLogEvent.class)
            .withRuntimeId(DtoConverter.asDto(runtimeId))
            .withText(text)
            .withTime(time));
  }

  public void sendAbnormalStoppedEvent(RuntimeIdentity runtimeId, String reason) {
    eventService.publish(new RuntimeAbnormalStoppedEvent(runtimeId, reason));
  }

  public void sendAbnormalStoppingEvent(RuntimeIdentity runtimeId, String reason) {
    eventService.publish(new RuntimeAbnormalStoppingEvent(runtimeId, reason));
  }

  public void sendWatchLogStartedEvent(String container) {
    eventService.publish(new WatchLogStartedEvent(container));
  }

  public void sendWatchLogStoppedEvent(String container) {
    eventService.publish(new WatchLogStoppedEvent(container));
  }
}

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

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.shared.dto.BrokerStatus;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;

/**
 * Event sent by a plugin broker with results of broker invocation.
 *
 * <p>This class differs from {@link BrokerStatusChangedEvent} it is version of latter with a
 * prettier format. It has workspace tooling in a POJO representation instead of stringified JSON.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @see BrokerStatusChangedEvent
 */
@Beta
public class BrokerEvent {
  private BrokerStatus status;
  private RuntimeIdentity runtimeId;
  private String error;
  private List<ChePlugin> tooling;

  @SuppressWarnings("unused")
  public BrokerEvent() {}

  public BrokerEvent(BrokerStatusChangedEvent resultEvent, List<ChePlugin> tooling) {
    this.error = resultEvent.getError();
    this.status = resultEvent.getStatus();
    this.runtimeId = resultEvent.getRuntimeId();
    this.tooling = tooling;
  }

  public BrokerStatus getStatus() {
    return status;
  }

  public BrokerEvent withStatus(BrokerStatus status) {
    this.status = status;
    return this;
  }

  public RuntimeIdentity getRuntimeId() {
    return runtimeId;
  }

  public BrokerEvent withRuntimeId(RuntimeIdentity runtimeId) {
    this.runtimeId = runtimeId;
    return this;
  }

  public String getError() {
    return error;
  }

  public BrokerEvent withError(String error) {
    this.error = error;
    return this;
  }

  List<ChePlugin> getTooling() {
    return tooling;
  }

  BrokerEvent withTooling(List<ChePlugin> tooling) {
    this.tooling = tooling;
    return this;
  }
}

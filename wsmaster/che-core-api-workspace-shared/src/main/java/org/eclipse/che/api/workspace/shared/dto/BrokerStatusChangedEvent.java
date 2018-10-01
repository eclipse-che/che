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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Event initiated by a plugin broker with the results of the plugin broker invocation for a
 * workspace.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@DTO
public interface BrokerStatusChangedEvent {

  /** Status of execution of a broker process. */
  BrokerStatus getStatus();

  BrokerStatusChangedEvent withStatus(BrokerStatus status);

  /** ID of a workspace this event is related to. */
  String getWorkspaceId();

  BrokerStatusChangedEvent withWorkspaceId(String workspaceId);

  /**
   * Error message that explains the reason of the broker process failure.
   *
   * <p>This method must return non-null value if {@link #getStatus() status} is {@link
   * BrokerStatus#FAILED}.
   */
  String getError();

  BrokerStatusChangedEvent withError(String error);

  /**
   * Stringified workspace tooling in JSON format.
   *
   * <p>Current model of workspace tooling description has fields with names not supported by a DTO
   * framework (dashes in field name, field name not matching POJO getter), so we have to stringify
   * it to pass over Che JSON_RPC framework.
   *
   * <p>This method must return non-null value if {@link #getStatus() status} is {@link
   * BrokerStatus#DONE}.
   */
  String getTooling();

  BrokerStatusChangedEvent withTooling(String tooling);
}

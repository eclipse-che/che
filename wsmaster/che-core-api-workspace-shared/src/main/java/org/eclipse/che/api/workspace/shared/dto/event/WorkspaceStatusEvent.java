/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto.event;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes workspace status changes.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@EventOrigin("workspace")
@DTO
public interface WorkspaceStatusEvent {
  WorkspaceStatus getStatus();

  void setStatus(WorkspaceStatus status);

  WorkspaceStatusEvent withStatus(WorkspaceStatus status);

  /**
   * Returns previous workspace status.
   *
   * @see WorkspaceStatus for more information about certain values
   */
  WorkspaceStatus getPrevStatus();

  void setPrevStatus(WorkspaceStatus status);

  WorkspaceStatusEvent withPrevStatus(WorkspaceStatus status);

  /** The id of the workspace to which this event belongs to . */
  String getWorkspaceId();

  void setWorkspaceId(String machineId);

  WorkspaceStatusEvent withWorkspaceId(String machineId);

  /** Returns an error message value. */
  @Nullable
  String getError();

  void setError(String error);

  WorkspaceStatusEvent withError(String error);

  void setOptions(Map<String, String> options);

  @Nullable
  Map<String, String> getOptions();

  WorkspaceStatusEvent withOptions(Map<String, String> options);

  /** @return whether event cause by some concrete user's request */
  boolean isInitiatedByUser();

  WorkspaceStatusEvent withInitiatedByUser(boolean isInitiatedByUser);
}

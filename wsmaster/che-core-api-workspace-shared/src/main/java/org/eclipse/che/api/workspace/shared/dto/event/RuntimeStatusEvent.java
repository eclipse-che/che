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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Infrastructure specific status changes.
 *
 * @author gazarenkov
 */
@DTO
public interface RuntimeStatusEvent {

  /** @return new status */
  String getStatus();

  RuntimeStatusEvent withStatus(String status);

  /** @return previous status */
  String getPrevStatus();

  RuntimeStatusEvent withPrevStatus(String status);

  /** @return runtime identity */
  RuntimeIdentityDto getIdentity();

  RuntimeStatusEvent withIdentity(RuntimeIdentityDto identity);

  /**
   * Error message/log returned by infrastructure in case if it caused runtime failure Filled only
   * if failed == true
   */
  @Nullable
  String getError();

  RuntimeStatusEvent withError(String error);

  /** @return whether Runtime is not workable anymore */
  boolean isFailed();

  RuntimeStatusEvent withFailed(boolean failed);
}

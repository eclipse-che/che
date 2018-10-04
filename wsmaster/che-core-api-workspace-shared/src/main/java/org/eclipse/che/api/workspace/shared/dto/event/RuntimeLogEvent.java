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
package org.eclipse.che.api.workspace.shared.dto.event;

import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Defines event format for runtime logs.
 *
 * @author Sergii Leshchenko
 */
@DTO
public interface RuntimeLogEvent {

  /** Returns the contents of the log event. */
  String getText();

  void setText(String text);

  RuntimeLogEvent withText(String text);

  /** Returns runtime identity. */
  RuntimeIdentityDto getRuntimeId();

  void setRuntimeId(RuntimeIdentityDto runtimeId);

  RuntimeLogEvent withRuntimeId(RuntimeIdentityDto runtimeId);

  /**
   * Returns the name of the machine that produces the logs.
   *
   * <p>May return null when log is produced by process which doesn't belong to any particular
   * machine.
   */
  @Nullable
  String getMachineName();

  void setMachineName(String machineName);

  RuntimeLogEvent withMachineName(String machineName);

  /** Returns time in format '2017-06-27T17:11:09.306+03:00' */
  String getTime();

  void setTime(String time);

  RuntimeLogEvent withTime(String time);

  /** Returns standard streams, if present otherwise, null will be returned. */
  @Nullable
  String getStream();

  void setStream(String stream);

  RuntimeLogEvent withStream(String stream);
}

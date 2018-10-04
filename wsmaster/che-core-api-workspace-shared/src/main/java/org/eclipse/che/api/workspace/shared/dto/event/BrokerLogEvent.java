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
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface BrokerLogEvent {

  /** Returns the contents of the log event. */
  String getText();

  void setText(String text);

  BrokerLogEvent withText(String text);

  /** Returns runtime identity. */
  RuntimeIdentityDto getRuntimeId();

  void setRuntimeId(RuntimeIdentityDto runtimeId);

  BrokerLogEvent withRuntimeId(RuntimeIdentityDto runtimeId);

  /** Returns time in format '2017-06-27T17:11:09.306+03:00' */
  String getTime();

  void setTime(String time);

  BrokerLogEvent withTime(String time);
}

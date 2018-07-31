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
package org.eclipse.che.agent.exec.shared.dto.event;

import org.eclipse.che.agent.exec.shared.dto.DtoWithPid;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ProcessStartedEventDto extends DtoWithPid {
  ProcessStartedEventDto withPid(int pid);

  String getTime();

  ProcessStartedEventDto withTime(String time);

  int getNativePid();

  ProcessStartedEventDto withNativePid(int nativePid);

  String getName();

  ProcessStartedEventDto withName(String name);

  String getCommandLine();

  ProcessStartedEventDto withCommandLine(String commandLine);
}

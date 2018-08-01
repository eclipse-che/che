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
package org.eclipse.che.agent.exec.shared.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GetProcessesResponseDto extends DtoWithPid {
  GetProcessesResponseDto withPid(int pid);

  String getName();

  GetProcessesResponseDto withName(String name);

  String getCommandLine();

  GetProcessesResponseDto withCommandLine(String commandLine);

  String getType();

  GetProcessesResponseDto withType(String type);

  boolean isAlive();

  GetProcessesResponseDto withAlive(boolean alive);

  int getNativePid();

  GetProcessesResponseDto withNativePid(int nativePid);
}

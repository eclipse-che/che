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
public interface ProcessDiedEventDto extends DtoWithPid {
  ProcessDiedEventDto withPid(int pid);

  String getTime();

  ProcessDiedEventDto withTime(String time);

  int getNativePid();

  ProcessDiedEventDto withNativePid(int nativePid);

  String getName();

  ProcessDiedEventDto withName(String name);

  String getCommandLine();

  ProcessDiedEventDto withCommandLine(String commandLine);
}

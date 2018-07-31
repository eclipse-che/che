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
package org.eclipse.che.api.debug.shared.dto;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.DebugSession;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatoliy Bazko */
@DTO
public interface DebugSessionDto extends DebugSession {
  DebuggerInfoDto getDebuggerInfo();

  void setDebuggerInfo(DebuggerInfoDto debuggerInfo);

  DebugSessionDto withDebuggerInfo(DebuggerInfoDto debuggerInfo);

  String getId();

  void setId(String id);

  DebugSessionDto withId(String id);

  String getType();

  void setType(String type);

  DebugSessionDto withType(String type);

  List<BreakpointDto> getBreakpoints();

  void setBreakpoints(List<BreakpointDto> breakpoints);

  DebugSessionDto withBreakpoints(List<BreakpointDto> breakpoints);
}

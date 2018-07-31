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
package org.eclipse.che.api.debug.shared.dto.action;

import java.util.List;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatoliy Bazko */
@DTO
public interface StartActionDto extends ActionDto, StartAction {
  TYPE getType();

  void setType(TYPE type);

  StartActionDto withType(TYPE type);

  List<BreakpointDto> getBreakpoints();

  void setBreakpoints(List<BreakpointDto> breakpoints);

  StartActionDto withBreakpoints(List<BreakpointDto> breakpoints);
}

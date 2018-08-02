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
package org.eclipse.che.api.debug.shared.dto;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatolii Bazko */
@DTO
public interface MethodDto extends Method {
  @Override
  String getName();

  void setName(String name);

  MethodDto withName(String name);

  @Override
  List<VariableDto> getArguments();

  void setArguments(List<VariableDto> arguments);

  MethodDto withArguments(List<VariableDto> arguments);
}

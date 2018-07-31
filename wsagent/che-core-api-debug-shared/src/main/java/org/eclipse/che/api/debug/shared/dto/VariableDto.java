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

import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface VariableDto extends Variable {
  String getName();

  void setName(String name);

  VariableDto withName(String name);

  SimpleValueDto getValue();

  void setValue(SimpleValueDto value);

  VariableDto withValue(SimpleValueDto value);

  String getType();

  void setType(String type);

  VariableDto withType(String type);

  VariablePathDto getVariablePath();

  void setVariablePath(VariablePathDto variablePath);

  VariableDto withVariablePath(VariablePathDto variablePath);

  boolean isPrimitive();

  void setPrimitive(boolean primitive);

  VariableDto withPrimitive(boolean primitive);
}

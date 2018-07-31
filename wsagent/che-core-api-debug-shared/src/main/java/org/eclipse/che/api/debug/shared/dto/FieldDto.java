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

import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface FieldDto extends Field {
  boolean isIsFinal();

  void setIsFinal(boolean value);

  FieldDto withIsFinal(boolean value);

  boolean isIsStatic();

  void setIsStatic(boolean value);

  FieldDto withIsStatic(boolean value);

  boolean isIsTransient();

  void setIsTransient(boolean value);

  FieldDto withIsTransient(boolean value);

  boolean isIsVolatile();

  void setIsVolatile(boolean value);

  FieldDto withIsVolatile(boolean value);

  String getName();

  void setName(String name);

  FieldDto withName(String name);

  SimpleValueDto getValue();

  void setValue(SimpleValueDto value);

  FieldDto withValue(SimpleValueDto value);

  String getType();

  void setType(String type);

  FieldDto withType(String type);

  VariablePathDto getVariablePath();

  void setVariablePath(VariablePathDto variablePath);

  FieldDto withVariablePath(VariablePathDto variablePath);

  boolean isPrimitive();

  void setPrimitive(boolean primitive);

  FieldDto withPrimitive(boolean primitive);
}

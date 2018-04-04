/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.dto;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface StackFrameDumpDto extends StackFrameDump {
  @Override
  List<FieldDto> getFields();

  void setFields(List<FieldDto> fields);

  StackFrameDumpDto withFields(List<FieldDto> fields);

  @Override
  List<VariableDto> getVariables();

  void setVariables(List<VariableDto> variables);

  StackFrameDumpDto withVariables(List<VariableDto> variables);

  @Override
  LocationDto getLocation();

  void setLocation(LocationDto location);

  StackFrameDumpDto withLocation(LocationDto location);
}

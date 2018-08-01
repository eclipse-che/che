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
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface SimpleValueDto extends SimpleValue {
  @Override
  List<VariableDto> getVariables();

  void setVariables(List<VariableDto> variables);

  SimpleValueDto withVariables(List<VariableDto> variables);

  @Override
  String getString();

  void setString(String value);

  SimpleValueDto withString(String value);
}

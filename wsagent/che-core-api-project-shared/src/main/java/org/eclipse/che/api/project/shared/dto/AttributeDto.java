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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for org.eclipse.che.api.project.shared.AttributeDescription
 *
 * @author andrew00x
 */
@DTO
public interface AttributeDto extends Attribute {

  @Override
  String getName();

  AttributeDto withName(String name);

  @Override
  String getDescription();

  AttributeDto withDescription(String description);

  @Override
  boolean isRequired();

  AttributeDto withRequired(boolean required);

  @Override
  boolean isVariable();

  AttributeDto withVariable(boolean variable);

  @Override
  ValueDto getValue();

  AttributeDto withValue(ValueDto value);
}

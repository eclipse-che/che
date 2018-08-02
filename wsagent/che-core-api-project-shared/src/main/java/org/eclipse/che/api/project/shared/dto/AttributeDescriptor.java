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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for org.eclipse.che.api.project.shared.AttributeDescription
 *
 * @author andrew00x
 */
@DTO
public interface AttributeDescriptor {
  String getName();

  void setName(String name);

  AttributeDescriptor withName(String name);

  String getDescription();

  void setDescription(String description);

  AttributeDescriptor withDescription(String description);

  boolean getRequired();

  void setRequired(boolean required);

  AttributeDescriptor withRequired(boolean required);

  boolean getVariable();

  void setVariable(boolean variable);

  AttributeDescriptor withVariable(boolean variable);

  List<String> getValues();

  void setValues(List<String> values);

  AttributeDescriptor withValues(List<String> values);
}

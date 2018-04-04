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

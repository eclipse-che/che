/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface SmartTestingConfigurationParamDto {

  String getName();

  void setName(String name);

  List<String> getValues();

  void setValues(List<String> values);
}

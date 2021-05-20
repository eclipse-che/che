/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.typescript.dto;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface MyDtoWithSerializableDTO {
  Map<String, Serializable> getPreferences();

  void setPreferences(Map<String, Serializable> preferences);
}

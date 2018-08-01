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
package org.eclipse.che.plugin.yaml.shared;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Interface of DTO for sending schemas
 *
 * @author Joshua Pinkney
 */
@DTO
public interface YamlDTO {
  /**
   * Get the schemas in the DTO
   *
   * @return Map of Schemas in the DTO
   */
  Map<String, String> getSchemas();

  /**
   * Set the schemas in the DTO
   *
   * @param schemas The schemas to set in the DTO
   */
  void setSchemas(Map<String, String> schemas);

  YamlDTO withSchemas(Map<String, String> schemas);
}

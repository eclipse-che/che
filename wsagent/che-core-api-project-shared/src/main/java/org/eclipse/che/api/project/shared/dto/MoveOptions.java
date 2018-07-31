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
package org.eclipse.che.api.project.shared.dto;

import com.google.common.annotations.Beta;
import io.swagger.annotations.ApiModelProperty;
import org.eclipse.che.dto.shared.DTO;

/** @author Ori Libhaber */
@Beta
@DTO
public interface MoveOptions {
  /**
   * Get value of overWrite attribute
   *
   * @return overWrite attribute
   */
  @ApiModelProperty(
    value = "Overwrite if there's a conflict with file names",
    allowableValues = "true, false"
  )
  Boolean getOverWrite();
  /**
   * Set value of overWrite attribute
   *
   * @param overWrite is the value to set to overWrite attribute
   */
  void setOverWrite(Boolean overWrite);
  /**
   * Get value of name attribute
   *
   * @return name attribute
   */
  @ApiModelProperty("New file name")
  String getName();
  /**
   * Set value of name attribute
   *
   * @param name is the value to set to name attribute
   */
  void setName(String name);
}

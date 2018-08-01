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

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface SourceEstimation {

  /** Gets unique id of type of project. */
  @ApiModelProperty(value = "type ID", position = 1)
  String getType();

  SourceEstimation withType(String type);

  /** Gets attributes of this project. */
  @ApiModelProperty(value = "Project attributes", position = 2)
  Map<String, List<String>> getAttributes();

  SourceEstimation withAttributes(Map<String, List<String>> attributes);

  @ApiModelProperty(value = "if matched", position = 3)
  boolean isMatched();

  SourceEstimation withMatched(boolean matched);

  /** Gets resolution - the reason that source code not matches project type requirements. */
  @ApiModelProperty(value = "Resolution", position = 4)
  String getResolution();

  SourceEstimation withResolution(String resolution);
}

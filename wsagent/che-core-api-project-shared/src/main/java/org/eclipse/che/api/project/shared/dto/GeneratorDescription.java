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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for generate project.
 *
 * @author Vladyslav Zhukovskiy
 */
@DTO
@ApiModel(description = "Generate new project")
public interface GeneratorDescription {

  /** Get options needed for generator. */
  @ApiModelProperty("Options needed for generator")
  Map<String, String> getOptions();

  /** Set options needed for generator. */
  void setOptions(Map<String, String> options);

  GeneratorDescription withOptions(Map<String, String> options);
}

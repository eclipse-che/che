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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for update project.
 *
 * @author andrew00x
 */
@DTO
@ApiModel(description = "Update project")
public interface ProjectUpdate {
  /** Get unique ID of type of project. */
  @ApiModelProperty(value = "Unique ID of project's type", required = true)
  @FactoryParameter(obligation = OPTIONAL)
  String getType();

  /** Set unique ID of type of project. */
  void setType(String type);

  ProjectUpdate withType(String type);

  //

  /** Get optional description of project. */
  @ApiModelProperty(value = "Optional description for new project")
  @FactoryParameter(obligation = OPTIONAL)
  String getDescription();

  /** Set optional description of project. */
  void setDescription(String description);

  ProjectUpdate withDescription(String description);

  //

  @ApiModelProperty(value = "Attributes for project")
  @FactoryParameter(obligation = OPTIONAL)
  /** Get attributes of project. */
  Map<String, List<String>> getAttributes();

  /** Set attributes of project. */
  void setAttributes(Map<String, List<String>> attributes);

  ProjectUpdate withAttributes(Map<String, List<String>> attributes);

  //

  @ApiModelProperty(value = "Visibility for project", allowableValues = "public,private")
  @FactoryParameter(obligation = OPTIONAL)
  /** Gets project visibility, e.g. private or public. */
  String getVisibility();

  /** Sets project visibility, e.g. private or public. */
  void setVisibility(String visibility);

  ProjectUpdate withVisibility(String visibility);

  /** Gets project mixin types */
  @ApiModelProperty("Mixing types")
  List<String> getMixins();

  /** Sets permissions of current user on this project. */
  void setMixins(List<String> mixins);

  ProjectUpdate withMixins(List<String> mixins);

  String getRecipe();

  void setRecipe(String recipe);

  ProjectUpdate withRecipe(String recipe);

  String getContentRoot();

  void setContentRoot(String contentRoot);

  ProjectUpdate withContentRoot(String contentRoot);
}

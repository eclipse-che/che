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
package org.eclipse.che.api.workspace.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface RecipeDto extends Recipe {

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getType();

  void setType(String type);

  RecipeDto withType(String type);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getContentType();

  void setContentType(String contentType);

  RecipeDto withContentType(String contentType);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getContent();

  void setContent(String content);

  RecipeDto withContent(String content);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getLocation();

  void setLocation(String location);

  RecipeDto withLocation(String location);
}

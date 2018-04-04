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
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.dto.shared.DTO;

/** @author gazarenkov */
@DTO
public interface ProjectTypeDto extends ProjectType {

  /** Get unique ID of type of project. */
  @Override
  String getId();

  ProjectTypeDto withId(String id);

  /** Get display name of type of project. */
  @Override
  String getDisplayName();

  ProjectTypeDto withDisplayName(String name);

  @Override
  List<AttributeDto> getAttributes();

  ProjectTypeDto withAttributes(List<AttributeDto> attributeDescriptors);

  @Override
  List<String> getParents();

  ProjectTypeDto withParents(List<String> parents);

  @Override
  boolean isPrimaryable();

  ProjectTypeDto withPrimaryable(boolean primaryable);

  @Override
  boolean isMixable();

  ProjectTypeDto withMixable(boolean mixable);

  @Override
  boolean isPersisted();

  ProjectTypeDto withPersisted(boolean persisted);

  /** @return all the ancestors of this project type */
  List<String> getAncestors();

  ProjectTypeDto withAncestors(List<String> ancestors);
}

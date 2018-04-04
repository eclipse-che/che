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
package org.eclipse.che.api.workspace.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.dto.shared.DTO;

/**
 * TODO Type and location are optional in case it is a subproject, that has an empty source.
 *
 * @author Alexander Garagatyi
 */
@DTO
public interface SourceStorageDto extends SourceStorage {
  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getType();

  void setType(String type);

  SourceStorageDto withType(String type);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  String getLocation();

  void setLocation(String location);

  SourceStorageDto withLocation(String location);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getParameters();

  void setParameters(Map<String, String> parameters);

  SourceStorageDto withParameters(Map<String, String> parameters);
}

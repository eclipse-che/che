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
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Class for transfering project importers and general configuration
 *
 * @author Michail Kuznyetsov
 */
@DTO
public interface ProjectImporterData {

  List<ProjectImporterDescriptor> getImporters();

  void setImporters(List<ProjectImporterDescriptor> importers);

  ProjectImporterData withImporters(List<ProjectImporterDescriptor> importers);

  Map<String, String> getConfiguration();

  void setConfiguration(Map<String, String> configuration);

  ProjectImporterData withConfiguration(Map<String, String> configuration);
}

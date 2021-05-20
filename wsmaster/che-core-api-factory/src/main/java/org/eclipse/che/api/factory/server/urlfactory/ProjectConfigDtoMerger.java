/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.urlfactory;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * Merge or add inside a factory the source storage dto
 *
 * @author Florent Benoit
 */
@Singleton
public class ProjectConfigDtoMerger {

  /**
   * Apply the merging of project config dto including source storage dto into the existing factory
   *
   * <p>here are the following rules
   *
   * <ul>
   *   <li>no projects --> add whole project
   *   <li>if projects:
   *       <ul>
   *         <li>if there is only one project: add source if missing
   *         <li>if many projects: do nothing
   *       </ul>
   * </ul>
   *
   * @param factory source factory
   * @param configSupplier supplier which can compute project config on demand
   * @return factory with merged project sources
   */
  public FactoryDto merge(FactoryDto factory, Supplier<ProjectConfigDto> configSupplier) {

    if (factory.getWorkspace() == null) {
      // factory is created with devfile. There is no need to provision projects
      return factory;
    }

    final List<ProjectConfigDto> projects = factory.getWorkspace().getProjects();
    if (projects == null || projects.isEmpty()) {
      factory.getWorkspace().setProjects(singletonList(configSupplier.get()));
      return factory;
    }

    // if we're here, they are projects
    if (projects.size() == 1) {
      ProjectConfigDto projectConfig = projects.get(0);
      if (projectConfig.getSource() == null)
        projectConfig.setSource(configSupplier.get().getSource());
    }

    return factory;
  }
}

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
package org.eclipse.che.plugin.maven.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Contains changes in project model, after updating maven projects
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ProjectsUpdateMessage extends MavenOutputEventDto {

  List<String> getUpdatedProjects();

  void setUpdatedProjects(List<String> updatedProjects);

  List<String> getDeletedProjects();

  void setDeletedProjects(List<String> deletedProjects);
}

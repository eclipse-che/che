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

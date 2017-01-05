/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Contains changes in project model, after updating maven projects
 * @author Evgen Vidolob
 */
@DTO
public interface ProjectsUpdateMessage {

    List<String> getUpdatedProjects();

    void setUpdatedProjects(List<String> updatedProjects);

    List<String> getDeletedProjects();

    void setDeletedProjects(List<String> deletedProjects);
}

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
package org.eclipse.che.ide.api.project;

import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Promise;

import java.util.List;

/**
 * Client for Project Type API.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectTypeServiceClient {

    /**
     * Get information about all registered project types.
     *
     * @param DevMachine devMachine
     * @return a promise that will provide a list of {@link ProjectTypeDto}s, or rejects with an error
     */
    Promise<List<ProjectTypeDto>> getProjectTypes(DevMachine devMachine);

    /**
     * Get information about project type with the specified ID.
     *
     * @param DevMachine devMachine
     * @param id
     *         id of the project type to get
     * @return a promise that resolves to the {@link ProjectTypeDto}, or rejects with an error
     */
    Promise<ProjectTypeDto> getProjectType(DevMachine devMachine, String id);
}

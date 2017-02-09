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
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.dto.shared.DTO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Parameter object for cleanup requests.
 */
@DTO
public interface CleanupRequest {

    /**
     * Sets the list of paths to clean
     * 
     * @param paths the paths
     */
    void setPaths(List<String> paths);

    /**
     * Sets the list of paths to clean
     * 
     * @param paths the paths
     * @return this object
     */
    CleanupRequest withPaths(List<String> paths);

    /**
     * Returns the list of paths to clean.
     * 
     * @return the paths
     */
    List<String> getPaths();

    /**
     * Returns the project path.
     * 
     * @return the project path
     */
    String getProjectPath();

    /**
     * Sets the project path.
     * 
     * @param projectPath the new value
     */
    void setProjectPath(@NotNull final String projectPath);

    /**
     * Sets the project path.
     * 
     * @param projectPath the new value
     * @return this object
     */
    CleanupRequest withProjectPath(@NotNull final String projectPath);
}

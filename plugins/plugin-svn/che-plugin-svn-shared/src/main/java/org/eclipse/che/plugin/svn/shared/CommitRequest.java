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
 * DTO for commit requests.
 */
@DTO
public interface CommitRequest {

    /**
     * @return the project path the request is associated with.
     */
    String getProjectPath();

    /**
     * @param projectPath the project path to set
     */
    void setProjectPath(@NotNull final String projectPath);

    /**
     * @param projectPath the project path to use
     */
    CommitRequest withProjectPath(@NotNull final String projectPath);

    /**
     * @return the paths the request is associated with
     */
    List<String> getPaths();

    /**
     * @param paths the paths to set
     */
    void setPaths(@NotNull final List<String> paths);

    /**
     * @param paths the paths to use
     */
    CommitRequest withPaths(@NotNull final List<String> paths);

    /**
     * @return whether or not keeping locks
     */
    boolean isKeepLocks();

    /**
     * @param keepLocks whether or not to keep locks
     */
    void setKeepLocks(final boolean keepLocks);

    /**
     * @param keepLocks whether or not to keep locks
     */
    CommitRequest withKeepLocks(final boolean keepLocks);

    /**
     * @return whether or not keeping change lists
     */
    boolean isKeepChangeLists();

    /**
     * @param keepChangeLists whether or not to keep change lists
     */
    void setKeepChangeLists(final boolean keepChangeLists);

    /**
     * @param keepChangeLists whether or not to keep change lists
     */
    CommitRequest withKeepChangeLists(final boolean keepChangeLists);

    /**
     * @return the commit message
     */
    String getMessage();

    /**
     * @param message the commit message to use
     */
    void setMessage(@NotNull final String message);

    /**
     * @param message the commit message to use
     */
    CommitRequest withMessage(@NotNull final String message);

}

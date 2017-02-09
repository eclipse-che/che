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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

import javax.validation.constraints.NotNull;
import java.util.List;

@DTO
public interface ShowDiffRequest {

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
    ShowDiffRequest withProjectPath(@NotNull final String projectPath);

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
    ShowDiffRequest withPaths(@NotNull final List<String> paths);

    /**
     * @return the revision the request is associated with
     */
    String getRevision();

    /**
     * @param revision the revision to set
     */
    void setRevision(@NotNull final String revision);

    /**
     * @param revision the revision
     *
     * @return the request
     */
    ShowDiffRequest withRevision(@NotNull final String revision);

    /**
     * @return the depth the request is associated with
     */
    String getDepth();

    /**
     * @param depth the depth to set
     */
    void setDepth(@NotNull final String depth);

    /**
     * @param depth the depth
     *
     * @return the request
     */
    ShowDiffRequest withDepth(@NotNull final String depth);

    /** @return user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(@Nullable final String username);

    /** @return {@link CheckoutRequest} with specified user name for authentication */
    ShowDiffRequest withUsername(@Nullable final String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(@Nullable final String password);

    /** @return {@link CheckoutRequest} with specified password for authentication */
    ShowDiffRequest withPassword(@Nullable final String password);
}

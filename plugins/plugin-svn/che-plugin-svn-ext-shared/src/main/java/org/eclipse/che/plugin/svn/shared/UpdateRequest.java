/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

/**
 * DTO for update requests.
 */
@DTO
public interface UpdateRequest {

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
    UpdateRequest withProjectPath(@NotNull final String projectPath);

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
    UpdateRequest withPaths(@NotNull final List<String> paths);

    /**
     * @return the accept argument
     */
    String getAccept();

    /**
     * @param accept the accept to set
     */
    void setAccept(@NotNull final String accept);

    /**
     * @param accept the accept to use
     *
     * @return the request
     */
    UpdateRequest withAccept(@NotNull final String accept);

    /**
     * @return the depth to checkout
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
    UpdateRequest withDepth(@NotNull final String depth);

    /**
     * @return whether or not to ignore externals
     */
    boolean isIgnoreExternals();

    /**
     * @param ignoreExternals whether or not to ignore externals
     */
    void setIgnoreExternals(@NotNull final boolean ignoreExternals);

    /**
     * @param ignoreExternals whether or not to ignore externals
     *
     * @return the request
     */
    UpdateRequest withIgnoreExternals(@NotNull final boolean ignoreExternals);

    /**
     * @return the revision to update to
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
    UpdateRequest withRevision(@NotNull final String revision);

    /** @return user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(@Nullable final String username);

    /** @return {@link CheckoutRequest} with specified user name for authentication */
    UpdateRequest withUsername(@Nullable final String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(@Nullable final String password);

    /** @return {@link CheckoutRequest} with specified password for authentication */
    UpdateRequest withPassword(@Nullable final String password);

}

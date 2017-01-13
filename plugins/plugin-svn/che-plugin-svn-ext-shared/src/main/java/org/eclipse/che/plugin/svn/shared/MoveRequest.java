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

/**
 * @author Vladyslav Zhukovskyi
 */
@DTO
public interface MoveRequest {
    /**
     * @return the project path the request is associated with.
     */
    String getProjectPath();

    /**
     * @param projectPath
     *         the project path to set
     */
    void setProjectPath(@NotNull final String projectPath);

    /**
     * @param projectPath
     *         the project path to use
     */
    MoveRequest withProjectPath(@NotNull final String projectPath);

    /**
     * Source file or folder path that should be moved.
     *
     * @return source file or folder path to be moved
     */
    List<String> getSource();

    /**
     * @param source
     *         file or folder path to be moved
     */
    void setSource(@NotNull final List<String> source);

    /**
     * @param source
     *         file or folder path to be moved
     */
    MoveRequest withSource(@NotNull final List<String> source);

    /**
     * Destination file or folder path.
     *
     * @return destination file or folder path
     */
    String getDestination();

    /**
     * @param destination
     *         file or folder path
     */
    void setDestination(@NotNull final String destination);

    /**
     * @param destination
     *         file or folder path
     */
    MoveRequest withDestination(@NotNull final String destination);

    /**
     * Operation comment.
     *
     * @return commentary
     */
    String getComment();

    /**
     * @param comment
     *         commentary
     */
    void setComment(String comment);

    /**
     * @param comment
     *         commentary
     */
    MoveRequest withComment(String comment);

    /** @return user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(@Nullable final String username);

    /** @return {@link CheckoutRequest} with specified user name for authentication */
    MoveRequest withUsername(@Nullable final String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(@Nullable final String password);

    /** @return {@link CheckoutRequest} with specified password for authentication */
    MoveRequest withPassword(@Nullable final String password);
}

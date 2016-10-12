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

/**
 * DTO for switch requests.
 *
 * @author Anatoliy Bazko
 */
@DTO
public interface SwitchRequest {

    /**
     * @param projectPath the project path to set
     */
    void setProjectPath(String projectPath);

    String getProjectPath();

    SwitchRequest withProjectPath(String projectPath);

    /**
     * @param location to switch
     */
    void setLocation(String location);

    String getLocation();

    SwitchRequest withLocation(String location);

    /**
     * Sets revision.
     */
    void setRevision(String revision);

    String getRevision();

    SwitchRequest withRevision(final String revision);

    /**
     * Limits operation: 'empty', 'files','immediates', or 'infinity')
     */
    void setDepth(@NotNull final String depth);

    SwitchRequest withDepth(@NotNull final String depth);

    String getDepth();

    /**
     * Set new working copy depth to ARG ('exclude', 'empty', 'files', 'immediates', or 'infinity')
     */
    void setSetDepth(final String setDepth);

    String getSetDepth();

    SwitchRequest withSetDepth(final String setDepth);

    /**
     * Relocates via URL-rewriting.
     */
    void setRelocate(boolean relocate);

    boolean isRelocate();

    SwitchRequest withRelocate(boolean relocate);

    /**
     * Ignores externals definitions.
     */
    void setIgnoreExternals(boolean ignoreExternals);

    boolean isIgnoreExternals();

    SwitchRequest withIgnoreExternals(boolean ignoreExternals);

    /**
     * Allows switching to a node with no common ancestor
     */
    void setIgnoreAncestry(boolean ignoreAncestry);

    boolean isIgnoreAncestry();

    SwitchRequest withIgnoreAncestry(boolean ignoreAncestry);

    /**
     * Forces operation to run.
     */
    void setForce(boolean force);

    boolean isForce();

    SwitchRequest withForce(boolean force);

    /**
     * Specifies automatic conflict resolution action:
     * ('postpone', 'working', 'base', 'mine-conflict', 'theirs-conflict', 'mine-full', 'theirs-full', 'edit', 'launch')
     * (shorthand: 'p', 'mc', 'tc', 'mf', 'tf', 'e', 'l')
     */
    void setAccept(String accept);

    String getAccept();

    SwitchRequest withAccept(String accept);

    /**************************************************************************
     * Credentials
     **************************************************************************/

    String getUsername();

    void setUsername(@Nullable final String username);

    SwitchRequest withUsername(@Nullable final String username);

    String getPassword();

    void setPassword(@Nullable final String password);

    SwitchRequest withPassword(@Nullable final String password);
}

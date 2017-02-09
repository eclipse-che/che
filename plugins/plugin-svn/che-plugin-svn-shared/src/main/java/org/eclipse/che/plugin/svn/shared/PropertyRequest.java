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

/**
 * Base interface for property request.
 *
 * @author Vladyslav Zhukovskyi
 */
@DTO
public interface PropertyRequest {
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
    PropertyRequest withProjectPath(@NotNull final String projectPath);

    /**
     * Property name.
     *
     * @return property name
     */
    String getName();

    /**
     * Property name.
     *
     * @param name
     *         property name
     */
    void setName(String name);

    /**
     * Property name.
     *
     * @param name
     *         property name
     */
    PropertyRequest withName(String name);

    /**
     * Instructs Subversion to limit the scope of an operation to a particular tree depth. ARG is one of empty (only the target itself),
     * files (the target and any immediate file children thereof), immediates (the target and any immediate children thereof), or infinity
     * (the target and all of its descendants—full recursion).
     *
     * @return depth
     */
    Depth getDepth();

    /**
     * Instructs Subversion to limit the scope of an operation to a particular tree depth. ARG is one of empty (only the target itself),
     * files (the target and any immediate file children thereof), immediates (the target and any immediate children thereof), or infinity
     * (the target and all of its descendants—full recursion).
     *
     * @param depth
     *         depth
     */
    void setDepth(Depth depth);

    /**
     * Instructs Subversion to limit the scope of an operation to a particular tree depth. ARG is one of empty (only the target itself),
     * files (the target and any immediate file children thereof), immediates (the target and any immediate children thereof), or infinity
     * (the target and all of its descendants—full recursion).
     *
     * @param depth
     *         depth
     */
    PropertyRequest withDepth(Depth depth);

    /**
     * Forces a particular command or operation to run. Subversion will prevent you from performing some operations in normal usage, but
     * you
     * can pass this option to tell Subversion “I know what I'm doing as well as the possible repercussions of doing it, so let me at 'em.”
     * This option is the programmatic equivalent of doing your own electrical work with the power on—if you don't know what you're doing,
     * you're likely to get a nasty shock.
     *
     * @return force
     */
    boolean isForce();

    /**
     * Forces a particular command or operation to run. Subversion will prevent you from performing some operations in normal usage, but
     * you
     * can pass this option to tell Subversion “I know what I'm doing as well as the possible repercussions of doing it, so let me at 'em.”
     * This option is the programmatic equivalent of doing your own electrical work with the power on—if you don't know what you're doing,
     * you're likely to get a nasty shock.
     *
     * @param force
     *         force
     */
    void setForce(boolean force);

    /**
     * Forces a particular command or operation to run. Subversion will prevent you from performing some operations in normal usage, but
     * you
     * can pass this option to tell Subversion “I know what I'm doing as well as the possible repercussions of doing it, so let me at 'em.”
     * This option is the programmatic equivalent of doing your own electrical work with the power on—if you don't know what you're doing,
     * you're likely to get a nasty shock.
     *
     * @param force
     *         force
     */
    PropertyRequest withForce(boolean force);

    /**
     * This removes properties from files, directories, or revisions.
     *
     * @return path
     */
    String getPath();

    /**
     * This removes properties from files, directories, or revisions.
     *
     * @param path
     *         path
     */
    void setPath(String path);

    /**
     * This removes properties from files, directories, or revisions.
     *
     * @param path
     *         path
     */
    PropertyRequest withPath(String path);
}

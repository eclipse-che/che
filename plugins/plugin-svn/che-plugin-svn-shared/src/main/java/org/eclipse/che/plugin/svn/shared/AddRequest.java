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
 * DTO for add requests.
 */
@DTO
public interface AddRequest {

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
    AddRequest withProjectPath(@NotNull final String projectPath);

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
    AddRequest withPaths(@NotNull final List<String> paths);

    /**
     * @return whether or not to add ignored paths
     */
    boolean isAddIgnored();

    /**
     * @param addIgnored whether or not to add ignored paths
     */
    void setAddIgnored(final boolean addIgnored);

    /**
     * @param addIgnored whether or not to add ignored paths
     */
    AddRequest withAddIgnored(final boolean addIgnored);

    /**
     * @return whether or not to add the intermediary parents
     */
    boolean isAddParents();

    /**
     * @param addParents whether or not to add intermediary parent directories
     */
    void setAddParents(final boolean addParents);

    /**
     * @param addParents whether or not to add intermediary parent directories
     */
    AddRequest withAddParents(final boolean addParents);

    /**
     * @return whether to explicitly use automatic properties
     */
    boolean isAutoProps();

    /**
     * @param authProps whether to explicitly use automatic properties
     */
    void setAutoProps(final boolean authProps);

    /**
     * @param useAutoProps whether to explicitly use automatic properties
     */
    AddRequest withAutoProps(final boolean useAutoProps);

    /**
     * @return whether to explicitly not use automatic properties
     */
    boolean isNotAutoProps();

    /**
     * @param notAutoProps whether to explicitly not use automatic properties
     */
    void setNotAutoProps(final boolean notAutoProps);

    /**
     * @param notAutoProps whether to explicitly not use automatic properties
     */
    AddRequest withNotAutoProps(final boolean notAutoProps);

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
    AddRequest withDepth(@NotNull final String depth);

}

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
 * DTO for status requests.
 */
@DTO
public interface StatusRequest {

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
    StatusRequest withProjectPath(@NotNull final String projectPath);

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
    StatusRequest withPaths(@NotNull final List<String> paths);

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
    StatusRequest withDepth(@NotNull final String depth);

    /**
     * @return whether or not to show ignored paths
     */
    boolean isShowIgnored();

    /**
     * @param showIgnored whether or not to show ignored paths
     */
    void setShowIgnored(final boolean showIgnored);

    /**
     * @param showIgnored whether or not to show ignored paths
     */
    StatusRequest withShowIgnored(final boolean showIgnored);

    /**
     * @return whether or not to show remote updates
     */
    boolean isShowUpdates();

    /**
     * @param showUpdates whether or not to show remote updates
     */
    void setShowUpdates(final boolean showUpdates);

    /**
     * @param showUpdates whether or not to show remote updates
     */
    StatusRequest withShowUpdates(final boolean showUpdates);

    /**
     * @return whether or not to be show unversioned paths
     */
    boolean isShowUnversioned();

    /**
     * @param showUnversioned whether or not to show unversioned paths
     */
    void setShowUnversioned(final boolean showUnversioned);

    /**
     * @param showUnversioned whether or not to show unversioned paths
     */
    StatusRequest withShowUnversioned(final boolean showUnversioned);

    /**
     * @return whether or not to be verbose
     */
    boolean isVerbose();

    /**
     * @param verbose whether or not to be verbose
     */
    void setVerbose(final boolean verbose);

    /**
     * @param verbose whether or not to be verbose
     */
    StatusRequest withVerbose(final boolean verbose);

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
    StatusRequest withIgnoreExternals(@NotNull final boolean ignoreExternals);

    /**
     * @return the change lists to use
     */
    List<String> getChangeLists();

    /**
     * @param changeLists the change lists to use
     */
    void setChangeLists(final List<String> changeLists);

    /**
     * @param changeLists the change lists to use
     *
     * @return the request
     */
    StatusRequest withChangeLists(final List<String> changeLists);

}

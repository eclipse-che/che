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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;

/**
 * Interface for validations of workspace config.
 *
 * @author Alexander Reshetnyak
 */
public interface WorkspaceConfigValidator {

    /**
     * Checks that {@link WorkspaceConfig cfg} contains valid values, if it is not throws {@link BadRequestException}.
     *
     * Validation rules:
     * <ul>
     * <li>{@link WorkspaceConfig#getName()} must not be empty or null</li>
     * <li>{@link WorkspaceConfig#getDefaultEnv()} must not be empty or null</li>
     * <li>{@link WorkspaceConfig#getEnvironments()} must contain {@link WorkspaceConfig#getDefaultEnv() default environment}
     * which is declared in the same configuration</li>
     * <li>{@link Environment#getName()} must not be null</li>
     * <li>{@link Environment#getMachineConfigs()} must contain at least 1 machine(which is dev),
     * also it must contain exactly one dev machine</li>
     * </ul>
     *
     * @throws BadRequestException
     *         when any constrain violation
     */
    void validate(WorkspaceConfig cfg) throws BadRequestException;

    /**
     * Checks that {@link WorkspaceConfig cfg} contains valid values, if it is not throws {@link BadRequestException}.
     *
     * Validation rules:
     * <ul>
     * <li>{@link WorkspaceConfig#getName()} does not check</li>
     * <li>{@link WorkspaceConfig#getDefaultEnv()} must not be empty or null</li>
     * <li>{@link WorkspaceConfig#getEnvironments()} must contain {@link WorkspaceConfig#getDefaultEnv() default environment}
     * which is declared in the same configuration</li>
     * <li>{@link Environment#getName()} must not be null</li>
     * <li>{@link Environment#getMachineConfigs()} must contain at least 1 machine(which is dev),
     * also it must contain exactly one dev machine</li>
     * </ul>
     *
     * @throws BadRequestException
     *         when any constrain violation
     */
    void validateWithoutWorkspaceName(WorkspaceConfig cfg) throws BadRequestException;

    /**
     * Checks workspace name. It must contain valid value, if it is not throws {@link BadRequestException}.
     * @param workspace
     *
     * @throws BadRequestException
     *         when constrain violation
     */
    void validateWorkspaceName(String workspace) throws BadRequestException;
}

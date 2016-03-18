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
package org.eclipse.che.api.workspace.shared.dto.util;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * Special util class which is necessary to generate methods in {@link ProjectConfigDto}
 *
 * @author Dmitry Shnurenko
 */
public final class ProjectConfigUtil {

    private ProjectConfigUtil(){}

    /**
     * Finds modules in project recursively.
     *
     * @param config
     *         project in which we need to find a module
     * @param pathToModule
     *         path to module which we need to find
     * @return an instance {@link ProjectConfigDto} which describes found module or {@code null} if module haven't found
     */
    public static ProjectConfigDto findModule(ProjectConfigDto config, String pathToModule) {
        if (config == null) {
            return null;
        }

        if (pathToModule.equals(config.getPath())) {
            return config;
        }

        for (ProjectConfigDto configDto : config.getModules()) {
            if (pathToModule.equals(configDto.getPath())) {
                return configDto;
            }

            ProjectConfigDto foundConfig = findModule(configDto, pathToModule);

            if (foundConfig != null) {
                return foundConfig;
            }
        }

        return null;
    }
}

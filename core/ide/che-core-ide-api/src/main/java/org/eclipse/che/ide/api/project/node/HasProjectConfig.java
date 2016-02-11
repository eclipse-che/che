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
package org.eclipse.che.ide.api.project.node;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * @author Vlad Zhukovskiy
 * @author Valeriy Svydenko
 */
public interface HasProjectConfig {

    class ProjectConfig implements HasProjectConfig {
        private ProjectConfigDto projectConfig;

        public ProjectConfig(ProjectConfigDto projectConfig) {
            this.projectConfig = projectConfig;
        }

        @Override
        public ProjectConfigDto getProjectConfig() {
            return this.projectConfig;
        }

        @Override
        public void setProjectConfig(ProjectConfigDto projectConfig) {
            this.projectConfig = projectConfig;
        }
    }

    ProjectConfigDto getProjectConfig();

    void setProjectConfig(ProjectConfigDto projectConfig);
}

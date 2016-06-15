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
package org.eclipse.che.ide.api.app;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Describe current state of project.
 *
 * @author Vitaly Parfonov
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
public class CurrentProject {

    private ProjectConfigDto rootProject;
    private ProjectConfigDto configDto;

    /**
     * @return ProjectDescriptor of opened project
     */
    public ProjectConfigDto getProjectConfig() {
        return configDto;
    }

    /**
     * @param projectConfig
     */
    public void setProjectConfig(ProjectConfigDto projectConfig) {
        this.configDto = projectConfig;
    }

    public ProjectConfigDto getRootProject() {
        return rootProject;
    }

    public void setRootProject(ProjectConfigDto rootProject) {
        this.rootProject = rootProject;
    }

    /**
     * Get value of attribute <code>name</code>.
     *
     * @param attributeName
     *         attribute name
     * @return value of attribute with specified name or <code>null</code> if attribute does not exists
     */
    @Nullable
    public String getAttributeValue(String attributeName) {
        List<String> attributeValues = getAttributeValues(attributeName);
        if (attributeValues != null && !attributeValues.isEmpty()) {
            return attributeValues.get(0);
        }
        return null;
    }

    /**
     * Get attribute values.
     *
     * @param attributeName
     *         attribute name
     * @return {@link List} of attribute values or <code>null</code> if attribute does not exists
     * @see #getAttributeValue(String)
     */
    public List<String> getAttributeValues(String attributeName) {
        return configDto.getAttributes().get(attributeName);
    }
}

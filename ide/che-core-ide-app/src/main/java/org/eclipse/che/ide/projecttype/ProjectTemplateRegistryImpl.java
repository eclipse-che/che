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
package org.eclipse.che.ide.projecttype;

import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for {@link ProjectTemplateRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectTemplateRegistryImpl implements ProjectTemplateRegistry {
    private final Map<String, List<ProjectTemplateDescriptor>> templateDescriptors;

    public ProjectTemplateRegistryImpl() {
        templateDescriptors = new HashMap<>();
    }

    @Override
    public void register(@NotNull ProjectTemplateDescriptor descriptor) {
        final String projectTypeId = descriptor.getProjectType();
        List<ProjectTemplateDescriptor> templates = templateDescriptors.get(projectTypeId);
        if (templates == null) {
            templates = new ArrayList<>();
            templates.add(descriptor);
            templateDescriptors.put(projectTypeId, templates);
        }
        templates.add(descriptor);
    }

    @NotNull
    @Override
    public List<ProjectTemplateDescriptor> getTemplateDescriptors(@NotNull String projectTypeId) {
        List<ProjectTemplateDescriptor> templateDescriptors = this.templateDescriptors.get(projectTypeId);
        if (templateDescriptors != null) {
            return templateDescriptors;
        }
        return new ArrayList<>();
    }
}

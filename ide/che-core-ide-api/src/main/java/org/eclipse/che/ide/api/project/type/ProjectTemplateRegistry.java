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
package org.eclipse.che.ide.api.project.type;

import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Registry for {@link ProjectTemplateDescriptor}s.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectTemplateRegistry {
    /**
     * Register the specified {@code descriptor}.
     *
     * @param descriptor
     *         template descriptor to register
     */
    void register(@NotNull ProjectTemplateDescriptor descriptor);

    /** Get all {@link org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor}s for the specified {@code projectTypeId}. */
    @NotNull
    List<ProjectTemplateDescriptor> getTemplateDescriptors(@NotNull String projectTypeId);
}

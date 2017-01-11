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
package org.eclipse.che.ide.api.project;


import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Client for Project Template service.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectTemplateServiceClient {
    /**
     * Get information about all registered project templates for the specified {@code projectTypeId}.
     *
     * @param tags
     *         tags which associated with project templates
     * @param callback
     *         the callback to use for the response
     */
    void getProjectTemplates(@NotNull List<String> tags, @NotNull AsyncRequestCallback<List<ProjectTemplateDescriptor>> callback);

    /**
     * Get information about all registered project templates.
     *
     * @param callback
     *         the callback to use for the response
     */
    void getProjectTemplates(@NotNull AsyncRequestCallback<List<ProjectTemplateDescriptor>> callback);
}

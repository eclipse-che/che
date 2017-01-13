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

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
public class ProjectTypeRegistryImpl implements ProjectTypeRegistry {

    private final List<ProjectTypeDto> types;

    public ProjectTypeRegistryImpl() {
        this.types = new ArrayList<>();
    }

    @Nullable
    @Override
    public ProjectTypeDto getProjectType(@NotNull String id) {
        if (types.isEmpty()) {
            return null;
        }

        for (ProjectTypeDto type : types) {
            if (id.equals(type.getId())) {
                return type;
            }
        }

        return null;
    }

    @Override
    public List<ProjectTypeDto> getProjectTypes() {
        return types;
    }

    @Override
    public void register(ProjectTypeDto projectType) {
        types.add(projectType);
    }

    @Override
    public void registerAll(List<ProjectTypeDto> projectTypesList) {
        types.addAll(projectTypesList);
    }
}

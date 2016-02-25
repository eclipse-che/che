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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
public class ProjectTypeUtils {


    public static ProjectConfigDto ensure(@NotNull ProjectConfigDto projectConfigDto, ProjectTypeRegistry typeRegistry) throws NotFoundException {
        final List<String> ensuredMixins = new ArrayList<>();
        final List<String> persistedAttributes = new ArrayList<>();
        final List<String> mixins = projectConfigDto.getMixins();
        final Map<String, List<String>> attributes = projectConfigDto.getAttributes();
        final Map<String, List<String>> ensuredAttributes = new HashMap<>();
        for (String mixinId : mixins) {
            final ProjectTypeDef mixin = typeRegistry.getProjectType(mixinId);
            if (mixin.isPersisted()) {
                ensuredMixins.add(mixinId);
                final List<Attribute> attributes1 = mixin.getAttributes();
                for (Attribute attribute : attributes1) {
                    persistedAttributes.add(attribute.getId());
                }
            }
        }
        for (String attributeId : attributes.keySet()) {
            if (persistedAttributes.contains(attributeId)) {
                ensuredAttributes.put(attributeId, attributes.get(attributeId));
            }
        }

        ProjectConfigDto ensured = DtoFactory.cloneDto(projectConfigDto);
        ensured.setAttributes(ensuredAttributes);
        ensured.setMixins(ensuredMixins);
        return ensured;
    }
}

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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * Data transfer object (DTO) for creating of project.
 *
 * @author Roman Nikitenko
 */
@DTO
public interface NewProjectConfigDto extends ProjectConfigDto, NewProjectConfig {
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getName();

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getType();

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    SourceStorageDto getSource();

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, String> getOptions();

    NewProjectConfigDto withName(String name);

    NewProjectConfigDto withPath(String path);

    NewProjectConfigDto withDescription(String description);

    NewProjectConfigDto withType(String type);

    NewProjectConfigDto withMixins(List<String> mixins);

    NewProjectConfigDto withAttributes(Map<String, List<String>> attributes);

    NewProjectConfigDto withSource(SourceStorageDto source);

    NewProjectConfigDto withLinks(List<Link> links);

    NewProjectConfigDto withProblems(List<ProjectProblemDto> problems);

    NewProjectConfigDto withOptions(Map<String, String> options);
}

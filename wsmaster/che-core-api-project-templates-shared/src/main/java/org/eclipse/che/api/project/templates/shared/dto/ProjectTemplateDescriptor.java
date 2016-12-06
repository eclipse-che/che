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
package org.eclipse.che.api.project.templates.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/** @author Vitaly Parfonov */
@DTO
public interface ProjectTemplateDescriptor {

    String getName();

    void setName(String name);

    ProjectTemplateDescriptor withName(String name);

    String getPath();

    void setPath(String path);

    ProjectTemplateDescriptor withPath(String path);

    /** Get description of project template. */
    String getDescription();

    /** Set description of project template. */
    void setDescription(String description);

    ProjectTemplateDescriptor withDescription(String description);

    /** Get project type of project template. */
    String getProjectType();

    /** Set project type of project template. */
    void setProjectType(String projectType);

    ProjectTemplateDescriptor withProjectType(String projectType);

    List<String> getMixins();

    void setMixins(List<String> mixins);

    ProjectTemplateDescriptor withMixins(List<String> mixins);

    Map<String, List<String>> getAttributes();

    void setAttributes(Map<String, List<String>> attributes);

    ProjectTemplateDescriptor withAttributes(Map<String, List<String>> attributes);

    List<ProjectProblemDto> getProblems();

    /** @see #getProblems */
    void setProblems(List<ProjectProblemDto> problems);

    ProjectTemplateDescriptor withProblems(List<ProjectProblemDto> problems);

    SourceStorageDto getSource();

    void setSource(SourceStorageDto sources);

    ProjectTemplateDescriptor withSource(SourceStorageDto sources);

    List<Link> getLinks();

    void setLinks(List<Link> links);

    ProjectTemplateDescriptor withLinks(List<Link> links);

    /** Get display name of project template. */
    String getDisplayName();

    /** Set display name of project template. */
    void setDisplayName(String displayName);

    ProjectTemplateDescriptor withDisplayName(String displayName);

    /** Get category of project template. */
    String getCategory();

    /** Set category of project template. */
    void setCategory(String category);

    ProjectTemplateDescriptor withCategory(String category);

    List<CommandDto> getCommands();

    void setCommands(List<CommandDto> commands);

    ProjectTemplateDescriptor withCommands(List<CommandDto> commands);

    List<String> getTags();

    void setTags(List<String> tags);

    ProjectTemplateDescriptor withTags(List<String> tags);

    List<NewProjectConfigDto> getProjects();

    void setProjects(List<NewProjectConfigDto> projects);

    ProjectTemplateDescriptor withProjects(List<NewProjectConfigDto> projects);

    Map<String, String> getOptions();

    void setOptions(Map<String, String> options);

    ProjectTemplateDescriptor withOptions(Map<String, String> options);
}

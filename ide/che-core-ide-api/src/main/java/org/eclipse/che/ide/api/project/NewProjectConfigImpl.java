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
package org.eclipse.che.ide.api.project;

import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.api.workspace.shared.dto.NewProjectConfigDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link NewProjectConfig} for creating project
 *
 * @author Roman Nikitenko
 */
public class NewProjectConfigImpl implements NewProjectConfig {
    private String                    name;
    private String                    path;
    private String                    description;
    private String                    type;
    private SourceStorage             sourceStorage;
    private List<String>              mixins;
    private Map<String, List<String>> attributes;
    private Map<String, String>       options;

    /** Constructor for creating project import configuration */
    public NewProjectConfigImpl(String name,
                                String path,
                                String description,
                                String type,
                                SourceStorage sourceStorage) {
        this(name, path, description, type, sourceStorage, null, null, null);
    }

    /** Constructor for creating project generator configuration */
    public NewProjectConfigImpl(String name,
                                String path,
                                String description,
                                String type,
                                Map<String, List<String>> attributes,
                                Map<String, String> options) {
        this(name, path, description, type, null, null, attributes, options);
    }

    /** Constructor for creating configuration from project template descriptor */
    public NewProjectConfigImpl(ProjectTemplateDescriptor descriptor) {
        this(descriptor.getName(),
             descriptor.getPath(),
             descriptor.getDescription(),
             descriptor.getProjectType(),
             descriptor.getSource(),
             descriptor.getMixins(),
             descriptor.getAttributes(),
             descriptor.getOptions());
    }

    /** Constructor for creating configuration from DTO object */
    public NewProjectConfigImpl(NewProjectConfigDto dto) {
        this(dto.getName(),
             dto.getPath(),
             dto.getDescription(),
             dto.getType(),
             dto.getSource(),
             dto.getMixins(),
             dto.getAttributes(),
             dto.getOptions());
    }

    public NewProjectConfigImpl(String name,
                                String path,
                                String description,
                                String type,
                                SourceStorage sourceStorage,
                                List<String> mixins,
                                Map<String, List<String>> attributes,
                                Map<String, String> options) {
        this.name = name;
        this.path = path;
        this.description = description;
        this.type = type;
        this.sourceStorage = sourceStorage;
        this.mixins = mixins;
        this.attributes = attributes != null ? attributes : new HashMap<String, List<String>>();
        this.options = options != null ? options : new HashMap<String, String>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<String> getMixins() {
        return mixins != null ? mixins : new ArrayList<String>();
    }

    @Override
    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return attributes != null ? attributes : new HashMap<String, List<String>>();
    }

    @Override
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, String> getOptions() {
        return options != null ? options : new HashMap<String, String>();
    }

    @Override
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public SourceStorage getSource() {
        return sourceStorage;
    }
}

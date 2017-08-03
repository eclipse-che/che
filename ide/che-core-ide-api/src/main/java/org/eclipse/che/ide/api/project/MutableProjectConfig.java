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

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.machine.shared.dto.CommandDto;

import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Vlad Zhukovskiy
 */
@Beta
public class MutableProjectConfig implements ProjectConfig {

    private String                    name;
    private String                    path;
    private String                    description;
    private String                    type;
    private List<String>              mixins;
    private Map<String, List<String>> attributes;
    private MutableSourceStorage      sourceStorage;
    private Map<String, String>       options;
    private List<NewProjectConfig>    projects;
    private List<CommandDto>          commands;

    public MutableProjectConfig(ProjectConfig source) {
        name = source.getName();
        path = source.getPath();
        description = source.getDescription();
        type = source.getType();
        mixins = newArrayList(source.getMixins());
        attributes = newHashMap(source.getAttributes());
        sourceStorage = new MutableSourceStorage(source.getSource());
    }

    public MutableProjectConfig() {
    }

    @Override
    public String getName() {
        return firstNonNull(name, "");
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPath() {
        return firstNonNull(path, "");
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDescription() {
        return firstNonNull(description, "");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return firstNonNull(type, "");
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<String> getMixins() {
        return firstNonNull(mixins, newArrayList());
    }

    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return firstNonNull(attributes, newHashMap());
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public MutableSourceStorage getSource() {
        return firstNonNull(sourceStorage, new MutableSourceStorage());
    }

    public void setSource(SourceStorage sourceStorage) {
        this.sourceStorage = new MutableSourceStorage(sourceStorage);
    }

    public Map<String, String> getOptions() {
        return firstNonNull(options, newHashMap());
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public List<CommandDto> getCommands() {
        return firstNonNull(commands, newArrayList());
    }

    public void setCommands(List<CommandDto> commands) {
        this.commands = commands;
    }

    /**
     * Returns the list of configurations to creating projects
     *
     * @return the list of {@link NewProjectConfig} to creating projects
     */
    public List<NewProjectConfig> getProjects() {
        return firstNonNull(projects, newArrayList());
    }

    /**
     * Sets the list of configurations to creating projects
     *
     * @param projects
     *         the list of {@link NewProjectConfig} to creating projects
     */
    public void setProjects(List<NewProjectConfig> projects) {
        this.projects = projects;
    }

    public class MutableSourceStorage implements SourceStorage {
        private String              type;
        private String              location;
        private Map<String, String> parameters;

        public MutableSourceStorage(SourceStorage source) {
            type = source.getType();
            location = source.getLocation();
            parameters = source.getParameters();
        }

        public MutableSourceStorage() {
        }

        @Override
        public String getType() {
            return firstNonNull(type, "");
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getLocation() {
            return firstNonNull(location, "");
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public Map<String, String> getParameters() {
            return firstNonNull(parameters, newHashMap());
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
    }
}

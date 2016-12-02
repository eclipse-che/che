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

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;

import java.util.List;
import java.util.Map;

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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<String> getMixins() {
        if (mixins == null) {
            mixins = newArrayList();
        }

        return mixins;
    }

    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        if (attributes == null) {
            attributes = newHashMap();
        }

        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public MutableSourceStorage getSource() {
        if (sourceStorage == null) {
            sourceStorage = new MutableSourceStorage();
        }

        return sourceStorage;
    }

    public void setSource(SourceStorage sourceStorage) {
        this.sourceStorage = new MutableSourceStorage(sourceStorage);
    }

    public Map<String, String> getOptions() {
        if (options == null) {
            options = newHashMap();
        }
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    /**
     * Returns the list of configurations to creating projects
     *
     * @return the list of {@link NewProjectConfig} to creating projects
     */
    public List<NewProjectConfig> getProjects() {
        if (projects == null) {
            return newArrayList();
        }
        return projects;
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
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public Map<String, String> getParameters() {
            if (parameters == null) {
                parameters = newHashMap();
            }

            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
    }
}

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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.vfs.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Implementation of {@link NewProjectConfig} for creating project
 *
 * @author gazarenkov
 */
public class NewProjectConfigImpl implements NewProjectConfig {

    private String                    path;
    private String                    type;
    private List<String>              mixins;
    private String                    name;
    private String                    description;
    private Map<String, List<String>> attributes;
    private Map<String, String>       options;
    private SourceStorage             origin;

    /**
     * Full qualified constructor
     *
     * @param path
     *         project path
     * @param type
     *         project type
     * @param mixins
     *         mixin project types
     * @param name
     *         project name
     * @param description
     *         project description
     * @param attributes
     *         project attributes
     * @param options
     *         options for generator for creating project
     * @param origin
     *         source configuration
     */
    public NewProjectConfigImpl(String path,
                                String type,
                                List<String> mixins,
                                String name,
                                String description,
                                Map<String, List<String>> attributes,
                                Map<String, String> options,
                                SourceStorage origin) {
        this.path = path;
        this.type = (type == null) ? BaseProjectType.ID : type;
        this.mixins = (mixins == null) ? new ArrayList<>() : mixins;
        this.name = name;
        this.description = description;
        this.attributes = (attributes == null) ? newHashMap() : attributes;
        this.options = (options == null) ? newHashMap() : options;
        this.origin = origin;
    }

    /**
     * Constructor for project import
     *
     * @param path
     *         project path
     * @param name
     *         project name
     * @param type
     *         project type
     * @param origin
     *         source configuration
     */
    public NewProjectConfigImpl(String path, String name, String type, SourceStorage origin) {
        this(path, type, null, name, null, null, null, origin);
    }

    /**
     * Constructor for reinit
     *
     * @param path
     */
    public NewProjectConfigImpl(Path path) {
        this(path.toString(), null, null, path.getName(), null, null, null, null);
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
        return mixins != null ? mixins : newArrayList();
    }

    @Override
    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return attributes != null ? attributes : newHashMap();
    }

    @Override
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SourceStorage getSource() {
        return origin;
    }

    @Override
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public Map<String, String> getOptions() {
        return options != null ? options : newHashMap();
    }
}

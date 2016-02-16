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
package org.eclipse.che.api.workspace.server.model.impl;


import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

/**
 * Data object for {@link ProjectConfig}.
 *
 * @author Eugene Voevodin
 * @author Dmitry Shnurenko
 */
public class ProjectConfigImpl implements ProjectConfig {

    private String                    name;
    private String                    path;
    private String                    description;
    private String                    type;
    private List<String>              mixins;
    private Map<String, List<String>> attributes;
    private List<ProjectConfig>       modules;
    private SourceStorageImpl         storage;
    //private String                    contentRoot;

    public ProjectConfigImpl() {
    }

    public ProjectConfigImpl(ProjectConfig projectConfig) {
        name = projectConfig.getName();
        path = projectConfig.getPath();
        description = projectConfig.getDescription();
        type = projectConfig.getType();
        mixins = new ArrayList<>(projectConfig.getMixins());
//        modules = new ArrayList<>(projectConfig.getModules() != null ? projectConfig.getModules() : Collections.<ProjectConfig>emptyList());
        attributes = projectConfig.getAttributes()
                                  .entrySet()
                                  .stream()
                                  .collect(toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));

        SourceStorage sourceStorage = projectConfig.getSource();

        if (sourceStorage != null) {
            storage = new SourceStorageImpl(sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
        }

//        contentRoot = projectConfig.getContentRoot();
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
            mixins = new ArrayList<>();
        }
        return mixins;
    }

    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

//    @Override
//    public List<ProjectConfig> getModules() {
//        if (modules == null) {
//            modules = new ArrayList<>();
//        }
//        return modules;
//    }

//    public void setModules(List<ProjectConfig> modules) {
//        this.modules = modules;
//    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SourceStorage getSource() {
        return storage;
    }

//    @Override
//    public String getContentRoot() {
//        return contentRoot;
//    }
//
//    public void setContentRoot(String contentRoot) {
//        this.contentRoot = contentRoot;
//    }

    public void setSource(SourceStorageImpl sourceStorage) {
        this.storage = sourceStorage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectConfigImpl)) return false;
        final ProjectConfigImpl other = (ProjectConfigImpl)o;
        return Objects.equals(name, other.name)
               && Objects.equals(path, other.path)
               && Objects.equals(description, other.description)
               && Objects.equals(type, other.type)
               && getMixins().equals(other.getMixins())
               && getAttributes().equals(other.getAttributes())
//               && getModules().equals(other.getModules())
               && Objects.equals(storage, other.getSource());
               //&& Objects.equals(contentRoot, other.getContentRoot());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(path);
        hash = hash * 31 + Objects.hashCode(description);
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + getMixins().hashCode();
        hash = hash * 31 + getAttributes().hashCode();
//        hash = hash * 31 + getModules().hashCode();
        hash = hash * 31 + Objects.hashCode(storage);
        //hash = hash * 31 + Objects.hashCode(contentRoot);
        return hash;
    }

    @Override
    public String toString() {
        return "ProjectConfigImpl{" +
               "name='" + name + '\'' +
               ", path='" + path + '\'' +
               ", description='" + description + '\'' +
               ", type='" + type + '\'' +
               ", mixins=" + mixins +
               ", attributes=" + attributes +
               ", modules=" + modules +
               ", storage=" + storage +
//               ", contentRoot='" + contentRoot + '\'' +
               '}';
    }
}

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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public class ProjectTypes {

    private final Project        project;
    private final ProjectManager manager;
    private final ProjectTypeDef primary;
    private final Map<String, ProjectTypeDef> mixins = new HashMap<>();
    private final Map<String, ProjectTypeDef> all    = new HashMap<>();

    public ProjectTypes(Project project, ProjectConfig projectConfig, ProjectManager manager) throws ProjectTypeConstraintException,
                                                                                                     NotFoundException {
        this.project = project;
        this.manager = manager;

        String projectType = projectConfig.getType();

        if (projectType == null) {
            throw new ProjectTypeConstraintException("No primary type defined for " + project.getWorkspace() + " : " + project.getPath());
        }

        primary = manager.getProjectTypeRegistry().getProjectType(projectType);

        if (!primary.isPrimaryable()) {
            throw new ProjectTypeConstraintException("Project type " + primary.getId() + " is not allowable to be primary type");
        }

        all.put(primary.getId(), primary);

        List<String> mixinsFromConfig = projectConfig.getMixins();

        if (mixinsFromConfig == null) {
            mixinsFromConfig = new ArrayList<>();
        }

        // temporary storage to detect duplicated attributes
        HashMap<String, Attribute> tmpAttrs = new HashMap<>();
        for (Attribute attr : primary.getAttributes()) {
            tmpAttrs.put(attr.getName(), attr);
        }

        for (String mixinFromConfig : mixinsFromConfig) {
            if (!mixinFromConfig.equals(primary.getId())) {
                ProjectTypeDef mixin = manager.getProjectTypeRegistry().getProjectType(mixinFromConfig);
                if (mixin == null) {
                    throw new ProjectTypeConstraintException("No project type registered for " + mixinFromConfig);
                }
                if (!mixin.isMixable()) {
                    throw new ProjectTypeConstraintException("Project type " + mixin + " is not allowable to be mixin");
                }

                // detect duplicated attributes
                for (Attribute attr : mixin.getAttributes()) {
                    if (tmpAttrs.containsKey(attr.getName())) {
                        throw new ProjectTypeConstraintException(
                                "Attribute name conflict. Duplicated attributes detected " + project.getPath() +
                                " Attribute " + attr.getName() + " declared in " + mixin.getId() + " already declared in " +
                                tmpAttrs.get(attr.getName()).getProjectType()
                        );
                    }

                    tmpAttrs.put(attr.getName(), attr);
                }

                // Silently remove repeated items from mixins if any
                mixins.put(mixinFromConfig, mixin);
                all.put(mixinFromConfig, mixin);
            }
        }
    }

    public ProjectTypeDef getPrimary() {
        return primary;
    }

    public Map<String, ProjectTypeDef> getMixins() {
        return mixins;
    }

    public Map<String, ProjectTypeDef> getAll() {
        return all;
    }

    void removeTransient() {
        HashSet<String> toRemove = new HashSet<>();
        for (ProjectTypeDef mt : all.values()) {
            if (!mt.isPersisted())
                toRemove.add(mt.getId());
        }

        for (String id : toRemove) {
            all.remove(id);
            mixins.remove(id);
        }
    }

    void addTransient() throws ServerException, NotFoundException {
        List<SourceEstimation> estimations;
        try {
            estimations = manager.resolveSources(project.getWorkspace(), project.getPath(), true);
        } catch (Exception e) {
            throw new ServerException(e);
        }
        for (SourceEstimation est : estimations) {
            ProjectTypeDef type = manager.getProjectTypeRegistry().getProjectType(est.getType());

            // NOTE: Only mixable types allowed
            if (type.isMixable()) {
                all.put(type.getId(), type);
                mixins.put(type.getId(), type);
            }
        }
    }

    List<String> mixinIds() {
        return new ArrayList<>(mixins.keySet());
    }
}

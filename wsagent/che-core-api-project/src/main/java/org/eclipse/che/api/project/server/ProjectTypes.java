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
import org.eclipse.che.api.project.server.RegisteredProject.Problem;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ValueStorageException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gazarenkov
 */
public class ProjectTypes {

    private final String                      projectPath;
    private final ProjectTypeRegistry         projectTypeRegistry;
    private ProjectTypeDef                    primary;
    private final Map<String, ProjectTypeDef> mixins;
    private final Map<String, ProjectTypeDef> all;
    private final Map<String, Attribute>      attributeDefs;

    ProjectTypes(String projectPath,
                        String type,
                        List<String> mixinTypes,
                        ProjectTypeRegistry projectTypeRegistry,
                        List<Problem>      problems) throws ProjectTypeConstraintException, NotFoundException {
        mixins = new HashMap<>();
        all = new HashMap<>();
        attributeDefs = new HashMap<>();

        this.projectTypeRegistry = projectTypeRegistry;
        this.projectPath = projectPath;

        ProjectTypeDef tmpPrimary;
        if (type == null) {
            problems.add(new Problem(12, "No primary type defined for " + projectPath + " Base Project Type assigned."));
            tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
        } else {
            try {
                tmpPrimary = projectTypeRegistry.getProjectType(type);
            } catch (NotFoundException e) {
                problems.add(new Problem(12, "Primary type " + type + " defined for " + projectPath + " is not registered. Base Project Type assigned."));
                tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
            }

            if (!tmpPrimary.isPrimaryable()) {
                problems.add(new Problem(12, "Project type " + tmpPrimary.getId() + " is not allowable to be primary type. Base Project Type assigned."));
                tmpPrimary = ProjectTypeRegistry.BASE_TYPE;
            }
        }

        this.primary = tmpPrimary;
        all.put(primary.getId(), primary);

        List<String> mixinsFromConfig = mixinTypes;

        if (mixinsFromConfig == null) {
            mixinsFromConfig = new ArrayList<>();
        }

        for (Attribute attr : primary.getAttributes()) {
            attributeDefs.put(attr.getName(), attr);
        }

        for (String mixinFromConfig : mixinsFromConfig) {
            if (mixinFromConfig.equals(primary.getId())) {
                continue;
            }

            final ProjectTypeDef mixin;
            try {
                mixin = projectTypeRegistry.getProjectType(mixinFromConfig);
            } catch (NotFoundException e) {
                problems.add(new Problem(12, "Project type " + mixinFromConfig + " is not registered. Skipped."));
                continue;
            }

            if (!mixin.isMixable()) {
                problems.add(new Problem(12, "Project type " + mixin + " is not allowable to be mixin. It not mixable. Skipped."));
                continue;
            }

            if (!mixin.isPersisted()) {
                continue;
            }

            // detect duplicated attributes
            for (Attribute attr : mixin.getAttributes()) {
                if (attributeDefs.containsKey(attr.getName())) {

                    problems.add(new Problem(13, "Attribute name conflict. Duplicated attributes detected " + projectPath +
                                                 " Attribute " + attr.getName() + " declared in " + mixin.getId() + " already declared in " +
                                                 attributeDefs.get(attr.getName()).getProjectType()+" Skipped."));
                    continue;
                }
                attributeDefs.put(attr.getName(), attr);
            }

            // Silently remove repeated items from mixins if any
            mixins.put(mixinFromConfig, mixin);
            all.put(mixinFromConfig, mixin);
        }
    }

    public Map<String, Attribute> getAttributeDefs() {
        return attributeDefs;
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

    /**
     * Reset project types and atrributes after initialization
     * in case when some attributes are not valid
     * (for instance required attributes are not initialized)
     *
     * @param attributesToDel - invalid attributes
     */
    void reset(Set<Attribute> attributesToDel) {

        Set<String> ptsToDel = new HashSet<>();
        for(Attribute attr : attributesToDel) {
            ptsToDel.add(attr.getProjectType());
        }

        Set <String>attrNamesToDel = new HashSet<>();
        for(String pt : ptsToDel) {
            ProjectTypeDef typeDef = all.get(pt);
            for(Attribute attrDef: typeDef.getAttributes()) {
                attrNamesToDel.add(attrDef.getName());
            }
        }

        // remove project types
        for(String typeId : ptsToDel) {
            this.all.remove(typeId);
            if(this.primary.getId().equals(typeId)) {
                this.primary = ProjectTypeRegistry.BASE_TYPE;
                this.all.put(ProjectTypeRegistry.BASE_TYPE.getId(), ProjectTypeRegistry.BASE_TYPE);
            } else {
                mixins.remove(typeId);
            }
        }

        // remove attributes
        for(String attr : attrNamesToDel) {
            this.attributeDefs.remove(attr);
        }
    }

    void addTransient(FolderEntry projectFolder) throws ServerException,
                                                        NotFoundException,
                                                        ProjectTypeConstraintException,
                                                        ValueStorageException {
        for (ProjectTypeDef pt : projectTypeRegistry.getProjectTypes()) {
            // NOTE: Only mixable types allowed
            if (pt.isMixable() && !pt.isPersisted() && pt.resolveSources(projectFolder).matched()) {
                all.put(pt.getId(), pt);
                mixins.put(pt.getId(), pt);
                for (Attribute attr : pt.getAttributes()) {
                    if (attributeDefs.containsKey(attr.getName())) {
                        throw new ProjectTypeConstraintException(
                                "Attribute name conflict. Duplicated attributes detected " + projectPath +
                                " Attribute " + attr.getName() + " declared in " + pt.getId() + " already declared in " +
                                attributeDefs.get(attr.getName()).getProjectType()
                        );
                    }

                    attributeDefs.put(attr.getName(), attr);
                }
            }
        }
    }
}

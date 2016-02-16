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
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author gazarenkov
 */
public class ProjectTypes {

    //private final ProjectConfig  projectConfig;

    private final String projectPath;
    private final ProjectTypeRegistry projectTypeRegistry;
    private final ProjectTypeDef primary;
    private final Map<String, ProjectTypeDef> mixins        = new HashMap<>();
    private final Map<String, ProjectTypeDef> all           = new HashMap<>();
    private final Map<String, Attribute>      attributeDefs = new HashMap<>();

    public ProjectTypes(String projectPath, String type, List<String> mixinTypes, ProjectTypeRegistry projectTypeRegistry) throws
                                                                                                                           ProjectTypeConstraintException,
                                                                                                                           NotFoundException {
        this.projectTypeRegistry = projectTypeRegistry;
        this.projectPath = projectPath;


        if (type == null) {
            throw new ProjectTypeConstraintException("No primary type defined for " + projectPath);
        }

        primary = projectTypeRegistry.getProjectType(type);

        if (!primary.isPrimaryable()) {
            throw new ProjectTypeConstraintException("Project type " + primary.getId() + " is not allowable to be primary type");
        }

        all.put(primary.getId(), primary);

        List<String> mixinsFromConfig = mixinTypes;
                //projectConfig.getMixins();

        if (mixinsFromConfig == null) {
            mixinsFromConfig = new ArrayList<>();
        }

        // temporary storage to detect duplicated attributes
        //HashMap<String, Attribute> tmpAttrs = new HashMap<>();

        for (Attribute attr : primary.getAttributes()) {
            attributeDefs.put(attr.getName(), attr);
        }

        for (String mixinFromConfig : mixinsFromConfig) {
            if (!mixinFromConfig.equals(primary.getId())) {
                ProjectTypeDef mixin = projectTypeRegistry.getProjectType(mixinFromConfig);
//                if (mixin == null) {
//                    throw new ProjectTypeConstraintException("No project type registered for " + mixinFromConfig);
//                }
                if (!mixin.isMixable()) {
                    throw new ProjectTypeConstraintException("Project type " + mixin + " is not allowable to be mixin");
                }

                // detect duplicated attributes
                for (Attribute attr : mixin.getAttributes()) {
                    if (attributeDefs.containsKey(attr.getName())) {
                        throw new ProjectTypeConstraintException(
                                "Attribute name conflict. Duplicated attributes detected " + projectPath +
                                " Attribute " + attr.getName() + " declared in " + mixin.getId() + " already declared in " +
                                attributeDefs.get(attr.getName()).getProjectType()
                        );
                    }

                    attributeDefs.put(attr.getName(), attr);
                }

                // Silently remove repeated items from mixins if any
                mixins.put(mixinFromConfig, mixin);
                all.put(mixinFromConfig, mixin);
            }
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


    void addTransient(FolderEntry projectFolder) throws ServerException, NotFoundException, ProjectTypeConstraintException {


        for (ProjectTypeDef pt : projectTypeRegistry.getProjectTypes()) {
            // NOTE: Only mixable types allowed
            if (pt.isMixable() && !pt.isPersisted() &&
                pt.getResolverFactory().newInstance(projectFolder).resolve()) {

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


//        List<SourceEstimation> estimations;
//        try {
//            estimations = manager.resolveSources(projectConfig.getPath(), true);
//        } catch (Exception e) {
//            throw new ServerException(e);
//        }
//        for (SourceEstimation est : estimations) {
//            ProjectTypeDef type = projectTypeRegistry.getProjectType(est.getType());
//
//            // NOTE: Only mixable types allowed
//            if (type.isMixable()) {
//                all.putProject(type.getId(), type);
//                mixins.putProject(type.getId(), type);
//
//                for (Attribute attr : type.getAttributes()) {
//                    if (attributeDefs.containsKey(attr.getName())) {
//                        throw new ProjectTypeConstraintException(
//                                "Attribute name conflict. Duplicated attributes detected " + projectConfig.getPath() +
//                                " Attribute " + attr.getName() + " declared in " + type.getId() + " already declared in " +
//                                attributeDefs.get(attr.getName()).getProjectType()
//                        );
//                    }
//
//                    attributeDefs.putProject(attr.getName(), attr);
//                }
//
//            }
//        }
//    }

//    List<String> mixinIds() {
//        return new ArrayList<>(mixins.keySet());
//    }
}

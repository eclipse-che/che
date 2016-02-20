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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.Variable;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class contains business logic which allows filter project and module attributes and add one or other type of attributes to project.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class AttributeFilter {

    private final ProjectManager projectManager;

    @Inject
    public AttributeFilter(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    /**
     * Adds persistent attributes to project. Persistent are attributes which are stored in project in workspace. The method analyzes
     * attributes from project and internal modules recursive.
     *
     * @param projectConfig
     *         project configuration which contains information about project and internal modules
     * @param projectFolder
     *         folder in which project or module is located
     * @throws ServerException
     *         error occurs during we get project from workspace
     * @throws ForbiddenException
     *         if user which perform operation doesn't have access to the requested project
     * @throws ProjectTypeConstraintException
     *         if we create project types without required parameters
     * @throws ValueStorageException
     *         when we have exception during getting values from value factory in attribute
     */
    public void addPersistedAttributesToProject(ProjectConfigDto projectConfig,
                                                FolderEntry projectFolder) throws ServerException,
                                                                                  ForbiddenException,
                                                                                  ProjectTypeConstraintException,
                                                                                  ValueStorageException,
                                                                                  NotFoundException {
        addAttributesToProject(projectConfig, projectFolder, AttributeType.PERSISTENT);
    }

    /**
     * Adds runtime attributes to project. Runtime are attributes which are calculated in runtime during getting or updating project.
     * The method analyzes attributes from project and internal modules recursive.
     *
     * @param projectConfig
     *         project configuration which contains information about project and internal modules
     * @param projectFolder
     *         folder in which project or module is located
     * @throws ServerException
     *         error occurs during we get project from workspace
     * @throws ForbiddenException
     *         if user which perform operation doesn't have access to the requested project
     * @throws ProjectTypeConstraintException
     *         if we create project types without required parameters
     * @throws ValueStorageException
     *         when we have exception during getting values from value factory in attribute
     */
    public void addRuntimeAttributesToProject(ProjectConfigDto projectConfig,
                                              FolderEntry projectFolder) throws ProjectTypeConstraintException,
                                                                                ForbiddenException,
                                                                                ValueStorageException,
                                                                                ServerException,
                                                                                NotFoundException {

        addAttributesToProject(projectConfig, projectFolder, AttributeType.RUNTIME);
    }


    private void addAttributesToProject(ProjectConfigDto projectConfig,
                                        FolderEntry parentFolder,
                                        AttributeType attributeType) throws ServerException,
                                                                            ForbiddenException,
                                                                            ProjectTypeConstraintException,
                                                                            ValueStorageException,
                                                                            NotFoundException {

        ProjectTypes projectTypes = getProjectTypes(parentFolder, projectConfig);

        ProjectTypeDef primaryType = projectTypes.getPrimary();

        projectConfig.setType(primaryType == null ? "blank" : primaryType.getId());
        projectConfig.setMixins(projectTypes.mixinIds());

        for (ProjectTypeDef projectType : projectTypes.getAll().values()) {
            Map<String, List<String>> attributes = AttributeType.PERSISTENT.equals(attributeType) ? getPersistedAttributes(projectType,
                                                                                                                           projectConfig)
                                                                                                  : getRuntimeAttributes(projectType,
                                                                                                                         projectConfig,
                                                                                                                         parentFolder);
            projectConfig.getAttributes().putAll(attributes);
        }

        for (ProjectConfigDto configDto : projectConfig.getModules()) {
            FolderEntry module = findModuleByPath(parentFolder, configDto.getPath());

            addAttributesToProject(configDto, module, attributeType);
        }
    }

    private FolderEntry findModuleByPath(FolderEntry parent, String path) throws ServerException, ForbiddenException {
        if (!path.contains("/")) {
            return parent;
        }

        //module which is located in module or project
        FolderEntry module = (FolderEntry)parent.getChild(path.substring(path.lastIndexOf("/")));

        if (module == null) {
            //module which is located in simple folder
            module = (FolderEntry)parent.getChild(definePathToModule(path));
        }

        return module == null ? parent : module;
    }

    private String definePathToModule(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path.substring(path.indexOf("/"));
    }

    private ProjectTypes getProjectTypes(FolderEntry module, ProjectConfig moduleConfig) throws ProjectTypeConstraintException,
                                                                                                ServerException,
                                                                                                NotFoundException {
        Project project = new Project(module, projectManager);

        ProjectTypes types = new ProjectTypes(project, moduleConfig, projectManager);
        types.addTransient();

        return types;
    }

    private Map<String, List<String>> getPersistedAttributes(ProjectTypeDef projectType,
                                                             ProjectConfig projectConfig) throws ProjectTypeConstraintException,
                                                                                                 ValueStorageException {
        Map<String, List<String>> persistentAttributes = new HashMap<>();

        for (Attribute attribute : projectType.getAttributes()) {
            if (!attribute.isVariable()) {
                persistentAttributes.put(attribute.getName(), attribute.getValue().getList());

                continue;
            }

            Variable variable = (Variable)attribute;
            ValueProviderFactory factory = variable.getValueProviderFactory();

            if (factory == null) {
                List<String> value = projectConfig.getAttributes().get(attribute.getName());

                persistentAttributes.put(variable.getName(), value);
            }
        }

        return persistentAttributes;
    }

    private Map<String, List<String>> getRuntimeAttributes(ProjectTypeDef projectType,
                                                           ProjectConfig projectConfig,
                                                           FolderEntry projectFolder) throws ProjectTypeConstraintException,
                                                                                             ValueStorageException {
        Map<String, List<String>> runtimeAttributes = new HashMap<>();

        for (Attribute attribute : projectType.getAttributes()) {
            if (attribute.isVariable()) {
                Variable variable = (Variable)attribute;
                ValueProviderFactory factory = variable.getValueProviderFactory();

                List<String> value;
                if (factory != null) {
                    value = factory.newInstance(projectFolder).getValues(variable.getName());

                    if (value == null) {
                        throw new ProjectTypeConstraintException("Value Provider must not produce NULL value of variable " +
                                                                 variable.getId());
                    }
                } else {
                    value = projectConfig.getAttributes().get(attribute.getName());
                }

                if ((value == null || value.isEmpty()) && variable.isRequired()) {
                    throw new ProjectTypeConstraintException("No Value for provider defined for required variable " + variable.getId());
                } else {
                    runtimeAttributes.put(variable.getName(), value);
                }
            }
        }

        return runtimeAttributes;
    }

    private enum AttributeType {
        PERSISTENT, RUNTIME
    }
}

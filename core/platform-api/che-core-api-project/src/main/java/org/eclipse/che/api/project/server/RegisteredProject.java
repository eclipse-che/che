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
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.server.type.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal Project implementation
 * It is supposed that it is object always consistent
 *
 * @author gazarenkov
 */
public class RegisteredProject implements ProjectConfig {

    private final List<Problem> problems = new ArrayList<>();
    private final FolderEntry   folder;
    private final ProjectConfig config;
    private final ProjectTypes  types;
    private final Map<String, Value> attributes = new HashMap<>();
    private final ProjectTypeRegistry projectTypeRegistry;
    private       boolean             updated;
    private       boolean             persisted;

    /**
     * Either root folder or config can be null, in this case Project is configured with problem
     *
     * @param folder
     *         - root local folder or null
     * @param config
     *         - project configuration in workspace
     * @param updated
     *         - if this object was updated, i.e. no more synchronized with workspace master
     * @param persisted
     *         - if this project was detected, initialized when "parent" project initialized
     * @param projectTypeRegistry
     */
    RegisteredProject(FolderEntry folder,
                      ProjectConfig config,
                      boolean updated,
                      boolean persisted,
                      ProjectTypeRegistry projectTypeRegistry) throws NotFoundException,
                                                                      ProjectTypeConstraintException,
                                                                      ServerException,
                                                                      ValueStorageException {
        this.folder = folder;
        this.config = (config == null) ? new NewProjectConfig(folder.getPath()) : config;
        this.updated = updated;
        this.persisted = persisted;
        this.projectTypeRegistry = projectTypeRegistry;

        if (folder == null || folder.isFile())
            problems.add(new Problem(10, "No project folder on file system " + this.config.getPath()));

        if (config == null)
            problems.add(new Problem(11, "No project configured in workspace " + this.config.getPath()));

        this.types = new ProjectTypes(this.config.getPath(), this.config.getType(), this.config.getMixins(), projectTypeRegistry);

        // init transient (implicit, like git) project types.
        // TODO should we do that in constructor?
        types.addTransient(folder);

        // initialize attributes
        initAttributes();


    }

    private void initAttributes()
            throws ValueStorageException, ProjectTypeConstraintException, ServerException, NotFoundException {

        // we take only defined attributes, others ignored
        for (Map.Entry<String, Attribute> entry : types.getAttributeDefs().entrySet()) {

            Attribute definition = entry.getValue();
            String name = entry.getKey();
            AttributeValue value = new AttributeValue(config.getAttributes().get(name));

            if (!definition.isVariable()) {
                // constant, value always assumed as stated in definition
                this.attributes.put(name, definition.getValue());

            } else {
                // variable
                final Variable variable = (Variable)definition;
                final ValueProviderFactory valueProviderFactory = variable.getValueProviderFactory();

                if (valueProviderFactory != null) {

                    // read-only. TODO the constants instead?
                    if(folder != null)
                        value = new AttributeValue(valueProviderFactory.newInstance(folder).getValues(name));
                    else
                        continue;

                }

                if (value.isEmpty() && variable.isRequired()) {
                    throw new ProjectTypeConstraintException("Value for required attribute is not initialized " + variable.getId());
                }

                if(!value.isEmpty())
                    this.attributes.put(name, value);

            }

        }

    }


    public ProjectTypeDef getProjectType() {
        return types.getPrimary();
    }

    public Map<String, ProjectTypeDef> getMixinTypes() {
        return types.getMixins();
    }

    public Map<String, ProjectTypeDef> getTypes() {
        return types.getAll();
    }

    public Map<String, Value> getAttributeEntries() {
        return attributes;
    }


    public boolean isSynced() {
        return !this.updated;
    }

    public void setSync() {
        this.updated = false;
    }

    public boolean isDetected() {
        return !this.persisted;
    }

    /**
     * @return root folder or null
     */
    public FolderEntry getBaseFolder() {
        return folder;
    }

    /**
     * @return problems in case if root or config is null (project is not synced)
     */
    public List<Problem> getProblems() {
        return problems;
    }


    public Map<String, List <String>> getPersistableAttributes() {
        Map<String, List <String>> attrs = new HashMap<>();
        for(HashMap.Entry<String, Value> entry : getAttributeEntries().entrySet()) {

            Attribute def = types.getAttributeDefs().get(entry.getKey());
            // not provided, not constants
            if(def != null &&
               ((def.isVariable() && ((Variable) def).getValueProviderFactory() == null)))
 //              || !def.isVariable()))
                attrs.put(entry.getKey(), entry.getValue().getList());
        }
        return attrs;
    }

    /* ------------------------------------------- */
    /* Implementation of ProjectConfig interface   */
    /* ------------------------------------------- */

    @Override
    public String getPath() {
        return ProjectRegistry.absolutizePath(config.getPath());
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getDescription() {
        return config.getDescription();
    }

    @Override
    public SourceStorage getSource() {
        return config.getSource();
    }

    @Override
    public String getType() {
        return types.getPrimary().getId();
    }

    @Override
    public List<String> getMixins() {
        List <String> mixins = new ArrayList<>();

        for(ProjectTypeDef mixin : types.getMixins().values()) {
            if(mixin.isPersisted())
                mixins.add(mixin.getId());
        }

        return mixins;
    }

    @Override
    public Map<String, List<String>> getAttributes() {

        Map<String, List<String>> attrs = new HashMap<>();
        for(Map.Entry<String, Value> entry : getAttributeEntries().entrySet())
             attrs.put(entry.getKey(), entry.getValue().getList());

        //return getPersistableAttributes();
        return attrs;

    }


    /* ----------------------------------- */

    public class Problem {
        private Problem(int code, String message) {
            this.code = code;
            this.message = message;
        }

        int    code;
        String message;
    }



}

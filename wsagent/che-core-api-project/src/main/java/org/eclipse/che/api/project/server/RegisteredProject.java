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
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.server.type.Variable;
import org.eclipse.che.api.vfs.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Internal Project implementation.
 * It is supposed that it is object always consistent.
 *
 * @author gazarenkov
 */
public class RegisteredProject implements ProjectConfig {

    private final List<Problem>      problems;
    private final Map<String, Value> attributes;

    private final FolderEntry   folder;
    private final ProjectConfig config;
    private       boolean       updated;
    private       boolean       detected;
    private final ProjectTypes  types;

    /**
     * Either root folder or config can be null, in this case Project is configured with problem.
     *
     * @param folder
     *         root local folder or null
     * @param config
     *         project configuration in workspace
     * @param updated
     *         if this object was updated, i.e. no more synchronized with workspace master
     * @param detected
     *         if this project was detected, initialized when "parent" project initialized
     * @param projectTypeRegistry
     *         project type registry
     */
    RegisteredProject(FolderEntry folder,
                      ProjectConfig config,
                      boolean updated,
                      boolean detected,
                      ProjectTypeRegistry projectTypeRegistry) throws NotFoundException,
                                                                      ProjectTypeConstraintException,
                                                                      ServerException,
                                                                      ValueStorageException {
        problems = new ArrayList<>();
        attributes = new HashMap<>();

        Path path;
        if (folder != null) {
            path = folder.getPath();
        } else if (config != null) {
            path = Path.of(config.getPath());
        } else {
            throw new ServerException("Invalid Project Configuration. Path undefined.");
        }

        this.folder = folder;
        this.config = (config == null) ? new NewProjectConfig(path) : config;
        this.updated = updated;
        this.detected = detected;

        if (folder == null || folder.isFile()) {
            problems.add(new Problem(10, "No project folder on file system " + this.config.getPath()));
        }

        if (config == null) {
            problems.add(new Problem(11, "No project configured in workspace " + this.config.getPath()));
        }

        // 1. init project types
        this.types = new ProjectTypes(this.config.getPath(), this.config.getType(), this.config.getMixins(), projectTypeRegistry, problems);

        // 2. init transient (implicit, like git) project types.
        types.addTransient(folder);

        // 3. initialize attributes
        initAttributes();
    }


    /**
     * Initialize project attributes.
     *
     * @throws ValueStorageException
     * @throws ProjectTypeConstraintException
     * @throws ServerException
     * @throws NotFoundException
     */
    private void initAttributes() throws ValueStorageException, ProjectTypeConstraintException, ServerException, NotFoundException {

        Set<Attribute> invalidAttributes = new HashSet<>();

        // we take only defined attributes, others ignored
        for (Map.Entry<String, Attribute> entry : types.getAttributeDefs().entrySet()) {
            final Attribute definition = entry.getValue();
            final String name = entry.getKey();
            AttributeValue value = new AttributeValue(config.getAttributes().get(name));

            if (!definition.isVariable()) {
                // constant, value always assumed as stated in definition
                attributes.put(name, definition.getValue());
            } else {
                // variable
                final Variable variable = (Variable)definition;

                // value provided
                if (variable.isValueProvided()) {

                    final ValueProvider valueProvider = variable.getValueProviderFactory().newInstance(folder);

                    if (folder != null) {

                        if (!valueProvider.isSettable() || value.isEmpty()) {
                            // get provided value
                            value = new AttributeValue(valueProvider.getValues(name));
                        } else {
                            // set provided (not empty) value
                            valueProvider.setValues(name, value.getList());
                        }

                    } else {
                        continue;
                    }
                }

                if (value.isEmpty() && variable.isRequired()) {
                    this.problems.add(new Problem(13, "Value for required attribute is not initialized " + variable.getId()));
                    invalidAttributes.add(variable);
                    //throw new ProjectTypeConstraintException("Value for required attribute is not initialized " + variable.getId());
                }

                if (!value.isEmpty()) {
                    this.attributes.put(name, value);
                }
            }
        }

        types.reset(invalidAttributes);

    }

    /**
     * @return primary project type
     */
    public ProjectTypeDef getProjectType() {
        return types.getPrimary();
    }

    /**
     * @return mixin project types
     */
    public Map<String, ProjectTypeDef> getMixinTypes() {
        return types.getMixins();
    }

    /**
     * @return all project types (primary + mixins, convenient method)
     */
    public Map<String, ProjectTypeDef> getTypes() {
        return types.getAll();
    }

    /**
     * @return attributes as name / Value Map
     */
    public Map<String, Value> getAttributeEntries() {
        return attributes;
    }

    /**
     * @return whether this project is synchronized with Workspace storage
     * On the other words this project is not updated
     */
    public boolean isSynced() {
        return !this.updated;
    }

    /**
     * should be called after synchronization with Workspace storage
     */
    public void setSync() {
        this.updated = false;
    }

    /**
     * @return whether this project is detected using Project Type resolver
     * If so it should not be persisted to Workspace storage
     */
    public boolean isDetected() {
        return detected;
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

    /**
     * @return list of Problems as a String
     */
    public String getProblemsStr() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for( RegisteredProject.Problem prb : problems ) {
            builder.append("[").append(i++).append("] : ").append(prb.message).append("\n");
        }
        return builder.toString();
    }

    /**
     * @return non provided attributes, those attributes can be persisted to Workspace storage
     */
    public Map<String, List<String>> getPersistableAttributes() {
        Map<String, List<String>> attrs = new HashMap<>();
        for (HashMap.Entry<String, Value> entry : getAttributeEntries().entrySet()) {
            Attribute def = types.getAttributeDefs().get(entry.getKey());
            // not provided, not constants
            if (def != null &&
                ((def.isVariable() && ((Variable)def).getValueProviderFactory() == null)))
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
        return types.getMixins().values()
                    .stream()
                    .filter(ProjectTypeDef::isPersisted)
                    .map(ProjectTypeDef::getId)
                    .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = new HashMap<>();
        for (Map.Entry<String, Value> entry : getAttributeEntries().entrySet()) {
            attrs.put(entry.getKey(), entry.getValue().getList());
        }
        return attrs;
    }

    public static class Problem {
        Problem(int code, String message) {
            this.code = code;
            this.message = message;
        }

        int    code;
        String message;
    }
}

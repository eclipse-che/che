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
package org.eclipse.che.api.workspace.shared.stack;

import org.eclipse.che.commons.annotation.Nullable;

import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;

import java.util.List;

/**
 * Defines the interface for managing stack of technologies.
 *
 * <p>Stack is the recipe/image/snapshot which declares workspace
 * environment with certain components (technologies) and provides additional
 * meta information for it.</p>
 *
 * @author Alexander Andrienko
 */
public interface Stack {

    /**
     * Return the unique stack identifier. (e.g. "stack123").
     */
    String getId();

    /**
     * Return the unique stack name. (e.g. "Ruby on Rails").
     */
    String getName();

    /**
     * Return identifier of user who is the stack creator.
     */
    String getCreator();

    /**
     * Returns the stack description, short information about the stack.
     */
    @Nullable
    String getDescription();

    /**
     * Return the scope of the stack.
     *
     * <p>There are two types of the scope:
     * <ul>
     * <li>"general" - when the stack defines common technology (e.g.: the stack "Java")</li>
     * <li>"advanced" - when the stack defines detailed(concrete) technology implementation (e.g: the stack "Java only with JRE")</li>
     * </ul>
     *
     */
    String getScope();

    /**
     * Return list technology tags. Tag links the stack with list Project Templates.
     */
    List<String> getTags();

    /**
     * Return the {@link WorkspaceConfig} for creation workspace.
     * This workspaceConfig can be used for store machine source, list predefined commands, projects etc.
     */
    @Nullable
    WorkspaceConfig getWorkspaceConfig();

    /**
     *  Return the source for the stack.
     *  (e.g. "type:recipe, origin: recipeLink", "type:script, origin:recipeScript")
     *  @see StackSource
     */
    @Nullable
    StackSource getSource();

    /**
     * Return the list of the components that stack consist of.
     * @see StackComponent
     *
     * Example:
     * [{"name": "java", "version" : "1.8.45"}, {"name" : "maven", "version" : "3.3.1"}]
     */
    List<? extends StackComponent> getComponents();
}

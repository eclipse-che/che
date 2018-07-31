/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.stack;

import java.util.List;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines the interface for managing stack of technologies.
 *
 * <p>Stack is the recipe/image/snapshot which declares workspace environment with certain
 * components (technologies) and provides additional meta information for it.
 *
 * @author Alexander Andrienko
 */
public interface Stack {

  /** Return the unique stack identifier. (e.g. "stack123"). */
  String getId();

  /** Return the unique stack name. (e.g. "Ruby on Rails"). */
  String getName();

  /** Return identifier of user who is the stack creator. */
  String getCreator();

  /** Returns the stack description, short information about the stack. */
  @Nullable
  String getDescription();

  /**
   * Return the scope of the stack.
   *
   * <p>There are two types of the scope:
   *
   * <ul>
   *   <li>"general" - when the stack defines common technology (e.g.: the stack "Java")
   *   <li>"advanced" - when the stack defines detailed(concrete) technology implementation (e.g:
   *       the stack "Java only with JRE")
   * </ul>
   */
  String getScope();

  /** Return list technology tags. Tag links the stack with list Project Templates. */
  List<String> getTags();

  /**
   * Return the {@link WorkspaceConfig} for creation workspace. This workspaceConfig can be used for
   * store machine source, list predefined commands, projects etc.
   */
  @Nullable
  WorkspaceConfig getWorkspaceConfig();

  /**
   * Return the list of the components that stack consist of.
   *
   * @see StackComponent
   *     <p>Example: [{"name": "java", "version" : "1.8.45"}, {"name" : "maven", "version" :
   *     "3.3.1"}]
   */
  List<? extends StackComponent> getComponents();
}

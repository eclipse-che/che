/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.app;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.ide.api.factory.model.FactoryImpl;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.resource.Path;

/**
 * Represents current context of the IDE application.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 * @author Yevhenii Voevodin
 */
public interface AppContext {

  /**
   * Returns the workspace root container, which is holder of registered projects.
   *
   * @return the workspace root
   * @since 4.4.0
   */
  @Beta
  Container getWorkspaceRoot();

  /**
   * Returns the registered projects in current workspace. If no projects were registered before,
   * then empty array is returned.
   *
   * @return the registered projects
   * @see Container#newProject()
   * @since 4.4.0
   */
  @Beta
  Project[] getProjects();

  /**
   * Returns the resource which is in current context. By current context means, that resource may
   * be in use in specified part if IDE. For example, project part may provide resource which is
   * under selection at this moment, editor may provide resource which is open, full text search may
   * provide resource which is under selection.
   *
   * <p>If specified part provides more than one resource, then last selected resource is returned.
   *
   * <p>May return {@code null} if there is no resource in context.
   *
   * @return the resource in context
   * @see Resource
   * @see #getResources()
   * @since 4.4.0
   */
  @Beta
  Resource getResource();

  /**
   * Returns the resources which are in current context. By current context means, that resources
   * may be in use in specified part if IDE. For example, project part may provide resources which
   * are under selection at this moment, editor may provide resource which is open, full text search
   * may provide resources which are under selection.
   *
   * <p>If specified part provides more than one resource, then all selected resources are returned.
   *
   * <p>May return {@code null} if there is no resources in context.
   *
   * @return the resource in context
   * @see Resource
   * @see #getResource()
   * @since 4.4.0
   */
  @Beta
  Resource[] getResources();

  /**
   * Returns the root project which is in context. To find out specified sub-project in context,
   * method {@link #getResource()} should be called. Resource is bound to own project and to get
   * {@link Project} instance from {@link Resource}, method {@link Resource#getRelatedProject()}
   * should be called.
   *
   * <p>May return {@code null} if there is no project in context.
   *
   * @return the root project or {@code null}
   * @see Project
   * @since 4.4.0
   */
  @Beta
  Project getRootProject();

  /**
   * Returns the path where projects are stored on file system.
   *
   * @return the path to projects root.
   * @since 4.2.0
   */
  Path getProjectsRoot();

  /**
   * Returns list of start-up actions with parameters that comes form URL during IDE initialization.
   *
   * @return the list of actions
   * @see StartUpAction
   */
  List<StartUpAction> getStartAppActions();

  /**
   * Returns the current user.
   *
   * @return current user
   */
  CurrentUser getCurrentUser();

  /**
   * Returns list of projects paths which are in importing state.
   *
   * @return list of project paths
   */
  List<String> getImportingProjects();

  /**
   * Adds project path to list of projects which are in importing state.
   *
   * @param pathToProject project path
   */
  void addProjectToImporting(String pathToProject);

  /**
   * Removes project path to list of projects which are in importing state.
   *
   * @param pathToProject project path
   */
  void removeProjectFromImporting(String pathToProject);

  /**
   * Returns {@link FactoryImpl} instance which id was set on startup, or {@code null} if no factory
   * was specified.
   *
   * @return loaded factory or {@code null}
   */
  FactoryImpl getFactory();

  String getWorkspaceId();

  /** Returns the current workspace. */
  WorkspaceImpl getWorkspace();

  /** Returns URL of Che Master API endpoint. */
  String getMasterApiEndpoint();

  /**
   * Returns URL of ws-agent server API endpoint.
   *
   * @throws RuntimeException if ws-agent server doesn't exist. Normally it may happen when
   *     workspace is stopped.
   */
  String getWsAgentServerApiEndpoint();

  /**
   * Returns web application identifier. Most obvious use - to distinguish web applications on
   * server side (e.g. connected via websocket)
   *
   * @return identifier
   */
  Optional<String> getApplicationId();

  /**
   * Sets web application identifier. Most obvious use - to distinguish web applications on server
   * side (e.g. connected via websocket)
   */
  void setApplicationWebsocketId(String id);

  /**
   * Returns context properties, key-value storage that allows to store data in the context for
   * plugins and extensions.
   *
   * @return a modifiable properties map
   * @since 5.11.0
   */
  Map<String, String> getProperties();
}

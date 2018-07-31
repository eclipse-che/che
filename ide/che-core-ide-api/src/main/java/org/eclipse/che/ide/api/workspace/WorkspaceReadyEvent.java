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
package org.eclipse.che.ide.api.workspace;

import com.google.common.annotations.Beta;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.ide.api.resources.Project;

/**
 * Workspace projects loaded events describe situation when new workspace configuration has been
 * received and workspace context have to be initialized with remote projects.
 *
 * <p>This event is intended to be fired when workspace has started and configured and projects has
 * been successfully loaded.
 *
 * <p>By design this event is intended to initialize third party components with initial projects
 * configuration.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public class WorkspaceReadyEvent extends GwtEvent<WorkspaceReadyEvent.WorkspaceReadyHandler> {

  /**
   * A workspace change listener is notified of projects loading.
   *
   * <p>Third party components may implement this interface to handle workspace projects loading
   * event.
   */
  public interface WorkspaceReadyHandler extends EventHandler {
    /**
     * Notifies the listener that some workspace configuration changes are happening. The supplied
     * event dives details.
     *
     * @param event instance of {@link WorkspaceReadyEvent}
     * @see WorkspaceReadyEvent
     * @since 4.4.0
     */
    void onWorkspaceReady(WorkspaceReadyEvent event);
  }

  private static Type<WorkspaceReadyHandler> TYPE;

  public static Type<WorkspaceReadyHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private final Project[] projects;

  public WorkspaceReadyEvent(Project[] projects) {
    this.projects = projects;
  }

  /**
   * Returns the new workspace configuration.
   *
   * @return the new workspace configuration
   * @see WorkspaceConfig
   * @since 4.4.0
   */
  public Project[] getProjects() {
    return projects;
  }

  /** {@inheritDoc} */
  @Override
  public Type<WorkspaceReadyHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(WorkspaceReadyHandler handler) {
    handler.onWorkspaceReady(this);
  }
}

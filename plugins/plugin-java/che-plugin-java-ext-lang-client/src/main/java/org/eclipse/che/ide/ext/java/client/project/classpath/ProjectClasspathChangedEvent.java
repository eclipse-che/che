/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** @author V. Rubezhny */
public class ProjectClasspathChangedEvent
    extends GwtEvent<ProjectClasspathChangedEvent.ProjectClasspathChangedHandler> {

  private String projectPath;

  public interface ProjectClasspathChangedHandler extends EventHandler {
    void onProjectClasspathChanged(ProjectClasspathChangedEvent event);
  }

  public static final Type<ProjectClasspathChangedHandler> TYPE = new Type<>();

  public ProjectClasspathChangedEvent(String projectPath) {
    this.projectPath = projectPath;
  }

  public String getProject() {
    return projectPath;
  }

  @Override
  public Type<ProjectClasspathChangedHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(ProjectClasspathChangedHandler handler) {
    handler.onProjectClasspathChanged(this);
  }
}

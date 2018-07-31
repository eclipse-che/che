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
package org.eclipse.che.ide.command.editor.page.project;

import java.util.Map;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Project;

/**
 * The view for {@link ProjectsPage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectsPageView extends View<ProjectsPageView.ActionDelegate> {

  /** Sets the applicable projects. */
  void setProjects(Map<Project, Boolean> projects);

  /** The action delegate for this view. */
  interface ActionDelegate {

    /** Called when applicable project has been changed. */
    void onApplicableProjectChanged(Project project, boolean applicable);
  }
}

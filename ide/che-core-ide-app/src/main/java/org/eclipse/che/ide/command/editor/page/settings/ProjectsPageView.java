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
package org.eclipse.che.ide.command.editor.page.settings;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Project;

import java.util.Map;

/**
 * The view of {@link ProjectsPage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectsPageView extends View<ProjectsPageView.ActionDelegate> {

    void setProjects(Map<Project, Boolean> projects);

    /** The action delegate for this view. */
    interface ActionDelegate {

        void onApplicableProjectChanged(Project project, boolean value);
    }
}

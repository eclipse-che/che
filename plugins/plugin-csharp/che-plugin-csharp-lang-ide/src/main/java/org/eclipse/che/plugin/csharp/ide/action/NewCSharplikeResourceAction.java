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
package org.eclipse.che.plugin.csharp.ide.action;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.csharp.shared.Constants;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Base class for ne C# resource
 *
 * Show/hide action depend on project type
 *
 * @author Anatolii Bazko
 */
public abstract class NewCSharplikeResourceAction extends AbstractNewResourceAction {

    protected final AppContext appContext;

    /**
     * Creates new action.
     *
     * @param title
     *         action's title
     * @param description
     *         action's description
     * @param svgIcon
     */
    public NewCSharplikeResourceAction(String title, String description, @Nullable SVGResource svgIcon, AppContext appContext) {
        super(title, description, svgIcon);
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        CurrentProject project = appContext.getCurrentProject();
        if (project == null || !(Constants.CSHARP_PROJECT_TYPE_ID.equals(project.getRootProject().getType()))) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Selection<?> selection = projectExplorer.getSelection();
        if (selection == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setEnabledAndVisible(true);
    }
}

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
package org.eclipse.che.plugin.jsonexample.ide.action;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
import org.eclipse.che.plugin.jsonexample.shared.Constants;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * JSON Example project specific action.
 */
public abstract class JsonExampleProjectAction extends AbstractPerspectiveAction {

    private AppContext appContext;

    /**
     * Constructor.
     *
     * @param appContext
     *         the IDE application context
     * @param text
     *         the text of the action
     * @param description
     *         the description of the action
     * @param svgResource
     *         the icon of the resource
     */
    public JsonExampleProjectAction(AppContext appContext,
                                    @NotNull String text,
                                    @NotNull String description,
                                    @Nullable SVGResource svgResource) {
        super(Collections.singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID),
              text,
              description,
              null,
              svgResource);
        this.appContext = appContext;
    }

    private static boolean isJsonExampleProjectType(CurrentProject currentProject) {
        return currentProject != null
               && Constants.JSON_EXAMPLE_PROJECT_TYPE_ID.equals(currentProject.getProjectConfig().getType());
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        CurrentProject currentProject = appContext.getCurrentProject();
        event.getPresentation()
             .setEnabledAndVisible(isJsonExampleProjectType(currentProject));
    }
}

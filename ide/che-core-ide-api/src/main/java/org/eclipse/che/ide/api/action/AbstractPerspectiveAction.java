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
package org.eclipse.che.ide.api.action;

import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The class contains general business logic for all actions displaying of which depend on current perspective.All actions must
 * extend this class if their displaying depend on changing of perspective.
 *
 * @author Dmitry Shnurenko
 */
public abstract class AbstractPerspectiveAction extends Action {


    @Inject
    private AppContext appContext;

    /**
     * A list of perspectives in which the action is enabled.
     * Null or empty list means the action is enabled everywhere.
     */
    private final List<String> perspectives;

    /**
     * Creates a new action with the specified text.
     *
     * @param perspectives
     *         list of perspective action IDs
     * @param text
     *         Serves as a tooltip when the presentation is a button and the name of the
     *         menu item when the presentation is a menu item.
     */
    public AbstractPerspectiveAction(@Nullable List<String> perspectives,
                                     @NotNull String text) {
        super(text);
        this.perspectives = perspectives;
    }

    /**
     * Constructs a new action with the specified text, description.
     *
     * @param perspectives
     *         list of perspective action IDs
     * @param text
     *         Serves as a tooltip when the presentation is a button and the name of the
     *         menu item when the presentation is a menu item
     * @param description
     *         Describes current action, this description will appear on
     *         the status bar when presentation has focus
     */
    public AbstractPerspectiveAction(@Nullable List<String> perspectives,
                                     @NotNull String text,
                                     @NotNull String description) {
        super(text, description, null, null);
        this.perspectives = perspectives;
    }

    public AbstractPerspectiveAction(@Nullable List<String> perspectives,
                                     @NotNull String text,
                                     @NotNull String description,
                                     @Nullable ImageResource imageResource,
                                     @Nullable SVGResource svgResource) {
        super(text, description, imageResource, svgResource);
        this.perspectives = perspectives;
    }

    public AbstractPerspectiveAction(@Nullable List<String> perspectives,
                                     @NotNull String text,
                                     @NotNull String description,
                                     @Nullable ImageResource imageResource,
                                     @Nullable SVGResource svgResource,
                                     @Nullable String htmlResource) {
        super(text, description, imageResource, svgResource, htmlResource);
        this.perspectives = perspectives;
    }

    /** {@inheritDoc} */
    @Override
    public final void update(@NotNull ActionEvent event) {
        PerspectiveManager manager = event.getPerspectiveManager();

        Presentation presentation = event.getPresentation();

        boolean isWorkspaceRunning = false;

        if (appContext != null) {
            Workspace workspace = appContext.getWorkspace();
            isWorkspaceRunning = workspace != null && WorkspaceStatus.RUNNING.equals(workspace.getStatus());
        }

        boolean inPerspective = perspectives == null || perspectives.isEmpty() ? true : perspectives.contains(manager.getPerspectiveId());

        presentation.setEnabledAndVisible(inPerspective && isWorkspaceRunning);

        if (inPerspective && isWorkspaceRunning) {
            updateInPerspective(event);
        }
    }

    /**
     * Updates displaying of action within current perspective.
     *
     * @param event
     *         update action
     */
    public abstract void updateInPerspective(@NotNull ActionEvent event);
}

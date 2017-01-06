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
package org.eclipse.che.ide.api.action;

import org.eclipse.che.ide.api.parts.PerspectiveManager;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.Map;

/**
 * Container for the information necessary to execute or update an {@link Action}.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class ActionEvent {
    private final ActionManager       actionManager;
    private final Presentation        presentation;
    private final PerspectiveManager  perspectiveManager;
    private final Map<String, String> parameters;

    /**
     * Create new action event.
     *
     * @param presentation
     *         the presentation which represents the action in the place from where it is invoked or updated
     * @param actionManager
     *         the manager for actions
     * @param perspectiveManager
     *         perspective manager which contains information about current perspective
     */
    public ActionEvent(@NotNull Presentation presentation,
                       @NotNull ActionManager actionManager,
                       @NotNull PerspectiveManager perspectiveManager) {
        this(presentation, actionManager, perspectiveManager, null);
    }

    /**
     * Create new action event.
     *
     * @param presentation
     *         the presentation which represents the action in the place from where it is invoked or updated
     * @param actionManager
     *         the manager for actions
     * @param perspectiveManager
     *         perspective manager which contains information about current perspective
     * @param parameters
     *         the parameters with which the action is invoked or updated
     */
    public ActionEvent(@NotNull Presentation presentation,
                       @NotNull ActionManager actionManager,
                       @NotNull PerspectiveManager perspectiveManager,
                       @Nullable Map<String, String> parameters) {
        this.actionManager = actionManager;
        this.presentation = presentation;
        this.perspectiveManager = perspectiveManager;
        this.parameters = parameters;
    }

    /**
     * Returns the presentation which represents the action in the place from where it is invoked or updated.
     *
     * @return the presentation instance
     */
    public Presentation getPresentation() {
        return presentation;
    }

    public PerspectiveManager getPerspectiveManager() {
        return perspectiveManager;
    }

    /**
     * Returns the parameters with which the action is invoked or updated.
     *
     * @return action's parameters
     */
    @Nullable
    public Map<String, String> getParameters() {
        return parameters;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }
}

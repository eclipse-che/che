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
 * Container for the information necessary to execute {@link Action} and cancel closing of application.
 *
 * @author Sergii Leschenko
 */
public class AppCloseActionEvent extends ActionEvent {
    private String cancelMessage;

    public AppCloseActionEvent(@NotNull Presentation presentation,
                               @NotNull ActionManager actionManager,
                               @NotNull PerspectiveManager perspectiveManager) {

        super(presentation, actionManager, perspectiveManager);
    }

    public AppCloseActionEvent(@NotNull Presentation presentation,
                               @NotNull ActionManager actionManager,
                               @NotNull PerspectiveManager perspectiveManager,
                               @Nullable Map<String, String> parameters) {

        super(presentation, actionManager, perspectiveManager, parameters);
    }

    /**
     * @return message that should be displayed in confirmation window on closing of application
     * or {@code null} if confirmation window should not be displayed
     */
    @Nullable
    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * Sets message that will be displayed in confirmation window on closing of application
     */
    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
}

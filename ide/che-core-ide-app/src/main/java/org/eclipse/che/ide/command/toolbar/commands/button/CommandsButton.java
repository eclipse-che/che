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
package org.eclipse.che.ide.command.toolbar.commands.button;

import com.google.gwt.safehtml.shared.SafeHtml;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;
import org.eclipse.che.ide.ui.menubutton.PopupActionHandler;
import org.eclipse.che.ide.ui.menubutton.PopupItemDataProvider;

/** {@link MenuPopupButton} for displaying commands of some {@link CommandGoal}. */
public class CommandsButton extends MenuPopupButton {

    private final CommandGoal goal;

    public CommandsButton(CommandGoal goal,
                          SafeHtml icon,
                          PopupItemDataProvider dataProvider,
                          PopupActionHandler actionHandler) {
        super(icon, dataProvider, actionHandler);
        this.goal = goal;
    }

    public PopupItemDataProvider getPopupItemDataProvider() {
        return dataProvider;
    }

    public CommandGoal getGoal() {
        return goal;
    }
}

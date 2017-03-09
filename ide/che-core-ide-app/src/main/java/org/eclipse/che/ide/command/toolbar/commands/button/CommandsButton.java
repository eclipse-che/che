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

import elemental.dom.Element;

import com.google.gwt.safehtml.shared.SafeHtml;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;
import org.eclipse.che.ide.ui.menubutton.SelectionHandler;
import org.eclipse.che.ide.ui.menubutton.MenuPopupItemDataProvider;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/** {@link MenuPopupButton} for displaying commands belong to the same {@link CommandGoal}. */
public class CommandsButton extends MenuPopupButton {

    private final CommandGoal goal;

    private Tooltip tooltip;
    private String  tooltipText;

    public CommandsButton(CommandGoal goal,
                          SafeHtml icon,
                          MenuPopupItemDataProvider dataProvider,
                          SelectionHandler actionHandler) {
        super(icon, dataProvider, actionHandler);

        this.goal = goal;
    }

    public MenuPopupItemDataProvider getPopupItemDataProvider() {
        return dataProvider;
    }

    public CommandGoal getGoal() {
        return goal;
    }

    public void setTooltip(String newTooltipText) {
        if (newTooltipText.equals(tooltipText)) {
            return;
        }

        tooltipText = newTooltipText;

        if (tooltip != null) {
            tooltip.destroy();
        }

        tooltip = Tooltip.create((Element)getElement(), BOTTOM, MIDDLE, newTooltipText);
    }
}

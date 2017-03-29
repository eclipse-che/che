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
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menubutton.MenuPopupButton;
import org.eclipse.che.ide.ui.menubutton.MenuPopupItemDataProvider;
import org.eclipse.che.ide.ui.menubutton.PopupItem;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/** {@link MenuPopupButton} for displaying commands belong to the same {@link CommandGoal}. */
public class GoalButton extends MenuPopupButton {

    private final CommandGoal     goal;
    private final ToolbarMessages messages;

    private Tooltip tooltip;
    private String  tooltipText;

    GoalButton(CommandGoal goal,
               SafeHtml icon,
               MenuPopupItemDataProvider dataProvider,
               ToolbarMessages messages) {
        super(icon, dataProvider);

        this.goal = goal;
        this.messages = messages;
    }

    public GoalButtonDataProvider getPopupItemDataProvider() {
        return (GoalButtonDataProvider)dataProvider;
    }

    public CommandGoal getGoal() {
        return goal;
    }

    /** Updates button's tooltip depending on it's state (what child elements it contains). */
    public void updateTooltip() {
        final PopupItem defaultItem = dataProvider.getDefaultItem();

        if (defaultItem != null) {
            setTooltip(messages.goalButtonTooltipExecutePrompt(defaultItem.getName()));
        } else if (getPopupItemDataProvider().hasGuideOnly()) {
            setTooltip(messages.goalButtonTooltipNoCommand(goal.getId()));
        } else {
            setTooltip(messages.goalButtonTooltipChooseCommand(goal.getId()));
        }
    }

    private void setTooltip(String newTooltipText) {
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

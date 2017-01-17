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
package org.eclipse.che.ide.command.execute;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.BaseCommandGoal;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Action group that represents command goal.
 *
 * @author Artem Zatsarynnyi
 */
class GoalPopUpGroup extends DefaultActionGroup {

    private final CommandGoal  commandGoal;
    private final IconRegistry iconRegistry;

    @Inject
    GoalPopUpGroup(@Assisted String goalId,
                   ActionManager actionManager,
                   PredefinedCommandGoalRegistry goalRegistry,
                   IconRegistry iconRegistry) {
        super(actionManager);

        this.iconRegistry = iconRegistry;

        if (isNullOrEmpty(goalId)) {
            commandGoal = goalRegistry.getDefaultGoal();
        } else {
            commandGoal = goalRegistry.getGoalById(goalId)
                                      .or(new BaseCommandGoal(goalId, goalId));
        }

        setPopup(true);

        // set icon
        final SVGResource commandTypeIcon = getCommandGoalIcon();
        if (commandTypeIcon != null) {
            getTemplatePresentation().setSVGResource(commandTypeIcon);
        }
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setText(commandGoal.getDisplayName() + " (" + getChildrenCount() + ")");
    }

    private SVGResource getCommandGoalIcon() {
        final String goalId = commandGoal.getId();

        final Icon icon = iconRegistry.getIconIfExist("command.goal." + goalId);

        if (icon != null) {
            final SVGImage svgImage = icon.getSVGImage();

            if (svgImage != null) {
                return icon.getSVGResource();
            }
        }

        return null;
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.action;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.tutorial.action.action.ChangeItemAction;
import com.codenvy.ide.tutorial.action.action.EnableAction;
import com.codenvy.ide.tutorial.action.action.VisibleAction;
import com.codenvy.ide.tutorial.action.part.TutorialHowToPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;

/** Extension used to demonstrate the Action feature. */
@Singleton
@Extension(title = "Action tutorial", version = "1.0.0")
public class ActionTutorialExtension {
    public static boolean SHOW_ITEM = true;

    @Inject
    public ActionTutorialExtension(ActionManager actionManager, ActionTutorialResources resources, ChangeItemAction changeItemAction,
                                   VisibleAction visibleAction, EnableAction enableAction, WorkspaceAgent workspaceAgent,
                                   TutorialHowToPresenter howToPresenter) {
        workspaceAgent.openPart(howToPresenter, PartStackType.EDITING);

        // Get main groups of Main menu, Toolbar and Context menu
        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        DefaultActionGroup toolbar = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
        DefaultActionGroup contextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);

        // Create main group
        DefaultActionGroup actionGroup = new DefaultActionGroup("Actions", false, actionManager);
        actionManager.registerAction("actionsGroup", actionGroup);

        // Create drop down group
        DefaultActionGroup popupGroup = new DefaultActionGroup("Drop down group", true, actionManager);
        popupGroup.getTemplatePresentation().setIcon(resources.item());
        actionManager.registerAction("dropDownGroup", popupGroup);
        actionGroup.add(popupGroup);

        // Add separator
        actionGroup.addSeparator();

        // Create general(not drop down) group
        DefaultActionGroup notPopupGroup = new DefaultActionGroup("General group", false, actionManager);
        actionManager.registerAction("generalGroup", notPopupGroup);
        actionGroup.add(notPopupGroup);

        popupGroup.add(changeItemAction);
        popupGroup.add(visibleAction);
        popupGroup.add(enableAction);

        notPopupGroup.add(changeItemAction);
        notPopupGroup.add(visibleAction);
        notPopupGroup.add(enableAction);

        // Add actions in MainMenu, Toolbar and Context menu
        mainMenu.add(actionGroup);
        toolbar.add(actionGroup);
        contextMenu.add(actionGroup);
    }
}
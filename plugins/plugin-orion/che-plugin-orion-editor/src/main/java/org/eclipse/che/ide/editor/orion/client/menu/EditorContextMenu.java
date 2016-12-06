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
package org.eclipse.che.ide.editor.orion.client.menu;

import com.google.inject.Provider;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.ContextMenu;

import javax.inject.Inject;

/**
 * Editor context menu, shows {@link IdeActions#GROUP_EDITOR_CONTEXT_MENU} action group.
 */
public class EditorContextMenu extends ContextMenu {

    @Inject
    public EditorContextMenu(ActionManager actionManager,
                             KeyBindingAgent keyBindingAgent,
                             Provider<PerspectiveManager> managerProvider) {
        super(actionManager, keyBindingAgent, managerProvider);
    }


    @Override
    protected String getGroupMenu() {
        return IdeActions.GROUP_EDITOR_CONTEXT_MENU;
    }
}

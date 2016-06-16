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
package org.eclipse.che.ide.part.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.ContextMenu;
import org.eclipse.che.ide.part.widgets.editortab.EditorTab;

import static org.eclipse.che.ide.part.editor.actions.EditorAbstractAction.CURRENT_FILE_PROP;
import static org.eclipse.che.ide.part.editor.actions.PinEditorTabAction.PROP_PIN;

/**
 * Editor tab context menu.
 * Perform injecting client property into action when last ones performs.
 * So we may obtain file on which context menu has been shown and editor tab pin state.
 *
 * @author Vlad Zhukovskiy
 */
public class EditorTabContextMenu extends ContextMenu {

    private final EditorTab editorTab;

    @Inject
    public EditorTabContextMenu(@Assisted EditorTab editorTab,
                                ActionManager actionManager,
                                KeyBindingAgent keyBindingAgent,
                                Provider<PerspectiveManager> managerProvider) {
        super(actionManager, keyBindingAgent, managerProvider);
        this.editorTab = editorTab;
    }

    /** {@inheritDoc} */
    @Override
    protected String getGroupMenu() {
        return IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
    }

    /** {@inheritDoc} */
    @Override
    public void onActionSelected(Action action) {
        super.onActionSelected(action);

        //pass into action file property and editor tab pin state
        presentationFactory.getPresentation(action).putClientProperty(CURRENT_FILE_PROP, editorTab.getFile());
        presentationFactory.getPresentation(action).putClientProperty(PROP_PIN, editorTab.isPinned());
    }
}

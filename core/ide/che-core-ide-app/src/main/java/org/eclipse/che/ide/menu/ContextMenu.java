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
package org.eclipse.che.ide.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.CloseMenuHandler;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.eclipse.che.ide.ui.toolbar.PopupMenu;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;

/**
 * Manages the Content menu.
 * Call <b>show</b> method to show menu and <b>hide</b> to hide it.
 * Also this manager filters the list of actions and displays only actions that are belong to Main MachineContext menu group.
 *
 * @author Vitaliy Guliy
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ContextMenu implements CloseMenuHandler, ActionSelectedHandler {

    private final ActionManager                actionManager;
    private final KeyBindingAgent              keyBindingAgent;
    private final DefaultActionGroup           actions;
    private final Provider<PerspectiveManager> managerProvider;

    private PopupMenu     popupMenu;
    private MenuLockLayer lockLayer;

    protected final PresentationFactory          presentationFactory;

    @Inject
    public ContextMenu(ActionManager actionManager, KeyBindingAgent keyBindingAgent, Provider<PerspectiveManager> managerProvider) {
        this.actionManager = actionManager;
        this.keyBindingAgent = keyBindingAgent;
        this.managerProvider = managerProvider;

        presentationFactory = new PresentationFactory();
        actions = new DefaultActionGroup(actionManager);

        blockBrowserMenu();
    }

    /**
     * Add a handler to block browser content menu.
     */
    private void blockBrowserMenu() {
        com.google.gwt.user.client.Event.sinkEvents(RootPanel.getBodyElement(), com.google.gwt.user.client.Event.ONCONTEXTMENU);
        DOM.setEventListener(RootPanel.getBodyElement(), new com.google.gwt.user.client.EventListener() {
            @Override
            public void onBrowserEvent(com.google.gwt.user.client.Event event) {
                if (com.google.gwt.user.client.Event.ONCONTEXTMENU == event.getTypeInt()) {
                    event.stopPropagation();
                    event.preventDefault();
                    return;
                }
            }
        });
    }

    /**
     * Shows a content menu and moves it to specified position.
     *
     * @param x
     * @param y
     */
    public void show(int x, int y) {
        hide();
        updateActions();

        lockLayer = new MenuLockLayer(this);
        popupMenu = new PopupMenu(actions,
                                  actionManager,
                                  managerProvider,
                                  presentationFactory,
                                  lockLayer,
                                  this,
                                  keyBindingAgent,
                                  "contextMenu");
        lockLayer.add(popupMenu);

        popupMenu.getElement().getStyle().setTop(y, com.google.gwt.dom.client.Style.Unit.PX);
        popupMenu.getElement().getStyle().setLeft(x, com.google.gwt.dom.client.Style.Unit.PX);
    }

    /**
     * Updates the list of visible actions.
     */
    private void updateActions() {
        actions.removeAll();

        final ActionGroup mainActionGroup = (ActionGroup)actionManager.getAction(getGroupMenu());
        if (mainActionGroup == null) {
            return;
        }

        final Action[] children = mainActionGroup.getChildren(null);
        for (final Action action : children) {
            final Presentation presentation = presentationFactory.getPresentation(action);
            final ActionEvent e = new ActionEvent(presentation, actionManager, managerProvider.get());

            action.update(e);
            if (presentation.isVisible()) {
                actions.add(action);
            }
        }
    }

    protected String getGroupMenu() {
        return IdeActions.GROUP_MAIN_CONTEXT_MENU;
    }

    @Override
    public void onActionSelected(Action action) {
        hide();
    }

    @Override
    public void onCloseMenu() {
        hide();
    }

    /**
     * Hides opened content menu.
     */
    public void hide() {
        if (popupMenu != null) {
            popupMenu.removeFromParent();
            popupMenu = null;
        }

        if (lockLayer != null) {
            lockLayer.removeFromParent();
            lockLayer = null;
        }
    }

}

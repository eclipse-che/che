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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.CloseMenuHandler;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.eclipse.che.ide.ui.toolbar.PopupMenu;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;

import static com.google.gwt.dom.client.Style.Unit.PX;

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
    private final Provider<PerspectiveManager> managerProvider;

    private PopupMenu     popupMenu;
    private MenuLockLayer lockLayer;

    protected final PresentationFactory presentationFactory;

    @Inject
    public ContextMenu(ActionManager actionManager, KeyBindingAgent keyBindingAgent, Provider<PerspectiveManager> managerProvider) {
        this.actionManager = actionManager;
        this.keyBindingAgent = keyBindingAgent;
        this.managerProvider = managerProvider;

        presentationFactory = new PresentationFactory();

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
                }
            }
        });
    }

    /**
     * Shows a content menu and moves it to specified position.
     *
     * @param x
     *         x coordinate
     * @param y
     *         y coordinate
     */
    public void show(int x, int y) {
        hide();
        ActionGroup actions = updateActions();

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

        calculatePosition(popupMenu, x, y);
    }

    private void calculatePosition(PopupMenu popupMenu, int x, int y) {
        int windowHeight = Window.getClientHeight();
        int popupHeight = popupMenu.getOffsetHeight();

        boolean isMenuWithinWindow = popupHeight + y < windowHeight;

        popupMenu.getElement().getStyle().setTop(isMenuWithinWindow ? y : y - popupHeight, PX);
        popupMenu.getElement().getStyle().setLeft(x, PX);
    }

    /**
     * Updates the list of visible actions.
     */
    protected ActionGroup updateActions() {


        final ActionGroup mainActionGroup = (ActionGroup)actionManager.getAction(getGroupMenu());
        if (mainActionGroup == null) {
            return new DefaultActionGroup(actionManager);
        }

        return mainActionGroup;
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

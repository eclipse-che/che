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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.eclipse.che.ide.ui.toolbar.PopupMenu;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownHeaderWidgetImpl.Resources;

import javax.validation.constraints.NotNull;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Class describes the popup window which contains all elements of list.
 *
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
public class DropDownListMenu implements ActionSelectedHandler {

    private final Resources                    resources;
    private final ActionManager                actionManager;
    private final KeyBindingAgent              keyBindingAgent;
    private final PresentationFactory          presentationFactory;
    private final DefaultActionGroup           actions;
    private final Provider<PerspectiveManager> managerProvider;

    private PopupMenu     popupMenu;
    private MenuLockLayer lockLayer;

    @Inject
    public DropDownListMenu(Resources resources, ActionManager actionManager, KeyBindingAgent keyBindingAgent,
                            Provider<PerspectiveManager> managerProvider) {
        this.resources = resources;
        this.actionManager = actionManager;
        this.keyBindingAgent = keyBindingAgent;
        this.managerProvider = managerProvider;

        presentationFactory = new PresentationFactory();
        actions = new DefaultActionGroup(actionManager);
    }

    /** {@inheritDoc} */
    @Override
    public void onActionSelected(Action action) {
        hide();
    }

    /**
     * Shows a content menu and moves it to specified position.
     *
     * @param x
     *         horizontal position
     * @param y
     *         vertical position
     * @param itemIdPrefix
     *         list identifier
     */
    public void show(final int x, final int y, @NotNull String itemIdPrefix) {
        hide();
        updateActions(itemIdPrefix);

        lockLayer = new MenuLockLayer();
        popupMenu = new PopupMenu(actions,
                                  actionManager,
                                  managerProvider,
                                  presentationFactory,
                                  lockLayer,
                                  this,
                                  keyBindingAgent,
                                  itemIdPrefix);
        popupMenu.addStyleName(resources.dropdownListCss().dropDownListMenu());
        popupMenu.getElement().getStyle().setOpacity(0);
        lockLayer.add(popupMenu);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                int left = x - popupMenu.getOffsetWidth();
                if (left < 0) {
                    left = 0;
                }

                popupMenu.getElement().getStyle().setTop(y, PX);
                popupMenu.getElement().getStyle().setLeft(left, PX);
                popupMenu.getElement().getStyle().setProperty("transition", "opacity 0.2s");
                popupMenu.getElement().getStyle().setOpacity(1);
            }
        });
    }

    /**
     * Updates the list of visible actions.
     *
     * @param listId
     *         identifier of action group which contains elements of list
     */
    private void updateActions(@NotNull String listId) {
        actions.removeAll();

        ActionGroup mainActionGroup = (ActionGroup)actionManager.getAction(listId);
        if (mainActionGroup == null) {
            return;
        }

        Action[] children = mainActionGroup.getChildren(null);
        for (Action action : children) {
            Presentation presentation = presentationFactory.getPresentation(action);
            ActionEvent e = new ActionEvent(presentation, actionManager, managerProvider.get());

            action.update(e);
            if (presentation.isVisible()) {
                actions.add(action);
            }
        }
    }

    /** Hides opened content menu. */
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

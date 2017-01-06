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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.eclipse.che.ide.ui.toolbar.PopupMenu;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/**
 * Class provides general view representation for header of drop down list.
 *
 * @author Valeriy Svydenko
 * @author Oleksii Orel
 * @author Vitaliy Guliy
 */
public class DropDownWidgetImpl extends Composite implements ActionSelectedHandler, ClickHandler, DropDownWidget {

    interface DropDownWidgetImplUiBinder extends UiBinder<Widget, DropDownWidgetImpl> {
    }

    private static final DropDownWidgetImplUiBinder UI_BINDER = GWT.create(DropDownWidgetImplUiBinder.class);

    @UiField
    FlowPanel marker;

    @UiField
    Label     selectedElementName;

    @UiField
    FlowPanel selectedElement;

    @UiField
    FlowPanel listHeader;

    private final String                       actionGroupId;

    private final Resources                    resources;
    private final ActionManager                actionManager;
    private final KeyBindingAgent              keyBindingAgent;
    private final PresentationFactory          presentationFactory;
    private final DefaultActionGroup           actions;
    private final Provider<PerspectiveManager> managerProvider;

    private String        selectedId;
    private String        selectedName;
    private PopupMenu     popupMenu;
    private MenuLockLayer lockLayer;
    private Tooltip       tooltip;


    @AssistedInject
    public DropDownWidgetImpl(Resources resources,
                              ActionManager actionManager,
                              KeyBindingAgent keyBindingAgent,
                              Provider<PerspectiveManager> managerProvider,
                              @NotNull @Assisted String actionGroupId) {
        this.resources = resources;
        this.actionGroupId = actionGroupId;

        initWidget(UI_BINDER.createAndBindUi(this));

        resources.dropdownListCss().ensureInjected();

        listHeader.setStyleName(resources.dropdownListCss().menuElement());

        marker.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        marker.addStyleName(resources.dropdownListCss().expandedImage());

        addDomHandler(this, ClickEvent.getType());

        this.actionManager = actionManager;
        this.keyBindingAgent = keyBindingAgent;
        this.managerProvider = managerProvider;

        presentationFactory = new PresentationFactory();
        actions = new DefaultActionGroup(actionManager);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                updatePopup();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void selectElement(String id, String name) {
        selectedId = id;
        selectedName = name;
        selectedElementName.setText(name == null ? "---" : name);
        tooltip = Tooltip.create((elemental.dom.Element)listHeader.getElement(), BOTTOM, MIDDLE, name);
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getSelectedName() {
        return selectedName;
    }

    public String getSelectedId() {
        return selectedId;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        int left = getAbsoluteLeft();
        int top = getAbsoluteTop() + listHeader.getOffsetHeight();
        int width = listHeader.getOffsetWidth();
        show(left, top, width);
    }

    /** {@inheritDoc} */
    @Override
    public void updatePopup() {
        if (popupMenu == null || !popupMenu.isAttached()) {
            return;
        }
        this.hide();
        int top = getAbsoluteTop() + listHeader.getOffsetHeight();
        show(getAbsoluteLeft(), top, listHeader.getOffsetWidth());
    }

    /** {@inheritDoc} */
    @Override
    public Action createAction(String id, String name) {
        return new SimpleListElementAction(id, name);
    }

    /** {@inheritDoc} */
    @Override
    public void onActionSelected(Action action) {
        this.hide();
    }

    /**
     * Shows a content menu and moves it to specified position.
     *
     * @param left
     *         horizontal position
     * @param top
     *         vertical position
     * @param width
     *         header width
     */
    private void show(int left, int top, int width) {
        hide();
        updateActions();

        lockLayer = new MenuLockLayer();
        popupMenu = new PopupMenu(actions,
                                  actionManager,
                                  managerProvider,
                                  presentationFactory,
                                  lockLayer,
                                  this,
                                  keyBindingAgent,
                                  actionGroupId, true);
        popupMenu.addStyleName(resources.dropdownListCss().dropDownListMenu());
        popupMenu.getElement().getStyle().setTop(top, PX);
        popupMenu.getElement().getStyle().setLeft(left, PX);
        popupMenu.getElement().getStyle().setWidth(width, PX);

        lockLayer.add(popupMenu);
    }

    /**
     * Refresh the list of visible actions.
     */
    private void updateActions() {
        actions.removeAll();

        ActionGroup mainActionGroup = (ActionGroup)actionManager.getAction(actionGroupId);
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
    private void hide() {
        if (popupMenu != null) {
            popupMenu.removeFromParent();
            popupMenu = null;
        }

        if (lockLayer != null) {
            lockLayer.removeFromParent();
            lockLayer = null;
        }
    }

    /**
     * The action which describes simple element of the custom drop down list.
     */
    private class SimpleListElementAction extends Action {
        private final String id;
        private final String name;

        public SimpleListElementAction(String id, String name) {
            super(name);
            this.id = id;
            this.name = name;
        }

        /** {@inheritDoc} */
        @Override
        public void actionPerformed(ActionEvent e) {
            selectElement(id, name);
        }

        /** @return the id of the element */
        @NotNull
        public String getId() {
            return id;
        }

        /** @return the title of the element */
        @NotNull
        public String getName() {
            return name;
        }
    }

    /** Item style selectors for a categories list item. */
    public interface DropdownCss extends CssResource {
        String expandedImage();

        String menuElement();

        String dropDownListMenu();
    }

    public interface Resources extends ClientBundle {
        @Source({"DropdownList.css", "org/eclipse/che/ide/api/ui/style.css"})
        DropdownCss dropdownListCss();

        @Source("expansionIcon.svg")
        SVGResource expansionImage();
    }

}

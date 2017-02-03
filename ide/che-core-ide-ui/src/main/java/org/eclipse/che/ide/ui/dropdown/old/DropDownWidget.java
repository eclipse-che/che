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
package org.eclipse.che.ide.ui.dropdown.old;

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
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.toolbar.MenuLockLayer;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * Class provides general view representation for header of drop down list.
 *
 * @author Valeriy Svydenko
 * @author Oleksii Orel
 * @author Vitaliy Guliy
 */
public class DropDownWidget<T> extends Composite implements ClickHandler {

    interface DropDownWidgetImplUiBinder extends UiBinder<Widget, DropDownWidget> {
    }

    private static final DropDownWidgetImplUiBinder UI_BINDER = GWT.create(DropDownWidgetImplUiBinder.class);

    @UiField
    FlowPanel marker;

    @UiField
    FlowPanel selectedElementName;

    @UiField
    FlowPanel selectedElement;

    @UiField
    FlowPanel listHeader;

    private final Resources resources;
    private ItemRenderer<T> renderer;
    private ItemSelectionHandler<T> itemSelectionHandler;

    private MenuLockLayer lockLayer;
    private Tooltip tooltip;

    private List<T> data;


    public DropDownWidget(Resources resources, ItemRenderer<T> renderer, ItemSelectionHandler<T> itemSelectionHandler) {
        this.resources = resources;
        this.renderer = renderer;
        this.itemSelectionHandler = itemSelectionHandler;

        initWidget(UI_BINDER.createAndBindUi(this));

        resources.dropdownListCss().ensureInjected();

        listHeader.setStyleName(resources.dropdownListCss().menuElement());

        marker.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        marker.addStyleName(resources.dropdownListCss().expandedImage());

        addDomHandler(this, ClickEvent.getType());


        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                updatePopup();
            }
        });
    }

    public void setData(List<T> data) {
        this.data = data;
        setSelectedItem(0);
    }

    public void setSelectedItem(int index) {
        if (data != null && data.size() > index) {
            selectedElementName.clear();
            T item = data.get(index);
            selectedElementName.getElement().appendChild((com.google.gwt.dom.client.Element) renderer.render(item));
            itemSelectionHandler.onItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(ClickEvent event) {
        int left = getAbsoluteLeft();
        int top = getAbsoluteTop() + listHeader.getOffsetHeight();
        int width = listHeader.getOffsetWidth();
        show(left, top, width);
    }

    public void updatePopup() {
//        if (popupMenu == null || !popupMenu.isAttached()) {
//            return;
//        }
        this.hide();
        int top = getAbsoluteTop() + listHeader.getOffsetHeight();
        show(getAbsoluteLeft(), top, listHeader.getOffsetWidth());
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
//        updateActions();

        lockLayer = new MenuLockLayer();
        //TODO
//        popupMenu = new PopupMenu(actions,
//                                  actionManager,
//                                  managerProvider,
//                                  presentationFactory,
//                                  lockLayer,
//                                  this,
//                                  keyBindingAgent,
//                                  actionGroupId, true);
//        popupMenu.addStyleName(resources.dropdownListCss().dropDownListMenu());
//        popupMenu.getElement().getStyle().setTop(top, PX);
//        popupMenu.getElement().getStyle().setLeft(left, PX);
//        popupMenu.getElement().getStyle().setWidth(width, PX);
//
//        lockLayer.add(popupMenu);
    }

    /**
     * Hides opened content menu.
     */
    private void hide() {
//        if (popupMenu != null) {
//            popupMenu.removeFromParent();
//            popupMenu = null;
//        }

        if (lockLayer != null) {
            lockLayer.removeFromParent();
            lockLayer = null;
        }
    }


    /**
     * Item style selectors for a categories list item.
     */
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

    public interface ItemRenderer<T> {
        Element render(T item);
    }

    public interface ItemSelectionHandler<T> {
        void onItemSelected(T item);
    }

}

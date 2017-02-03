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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ui.dropdown.old.DropDownWidget;

import java.util.HashMap;
import java.util.Map;

import static com.google.gwt.user.client.ui.PopupPanel.AnimationType.ROLL_DOWN;

/**
 * Drop down list widget.
 */
public class DropDownList extends Composite {

    private static final DropDownListUiBinder     UI_BINDER = GWT.create(DropDownListUiBinder.class);
    private static final DropDownWidget.Resources resources = GWT.create(DropDownWidget.Resources.class);

    private final FlowPanel             contentPanel;
    private final PopupPanel            dropDownPanel;
    private final Map<ListItem, Widget> itemsWidgets;

    @UiField
    FlowPanel listHeader;
    @UiField
    FlowPanel selectedElementName;
    @UiField
    FlowPanel dropButton;

    /** Create new drop down widget. */
    public DropDownList() {
        itemsWidgets = new HashMap<>();

        initWidget(UI_BINDER.createAndBindUi(this));

        listHeader.setStyleName(resources.dropdownListCss().menuElement());

        dropButton.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        dropButton.addStyleName(resources.dropdownListCss().expandedImage());

        dropDownPanel = new PopupPanel(true);
        dropDownPanel.setAnimationEnabled(true);
        dropDownPanel.setAnimationType(ROLL_DOWN);
        dropDownPanel.setWidth("350px");
        dropDownPanel.setHeight("150px");

        contentPanel = new FlowPanel();
        dropDownPanel.add(new ScrollPanel(contentPanel));

        attachEventHandlers();
    }

    private void attachEventHandlers() {
        dropButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (itemsWidgets.size() > 0) {
                    showDropDownList();
                }
            }
        }, ClickEvent.getType());
    }

    private void showDropDownList() {
        dropDownPanel.showRelativeTo(this);
        dropDownPanel.show();
    }

    /** Add item with the appropriate renderer. */
    public void addItem(final ListItem item, final ItemRenderer renderer) {
        final Widget widget = renderer.render(item);

        itemsWidgets.put(item, widget);

        widget.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // set the chosen item to the header
                selectedElementName.clear();
                selectedElementName.add(renderer.render(item));

                dropDownPanel.hide();
            }
        }, ClickEvent.getType());

        contentPanel.add(widget);
    }

    public void clear() {
        itemsWidgets.clear();
        contentPanel.clear();
    }

    interface DropDownListUiBinder extends UiBinder<Widget, DropDownList> {
    }
}

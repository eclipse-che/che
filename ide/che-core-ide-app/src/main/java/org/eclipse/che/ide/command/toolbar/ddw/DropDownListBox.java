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
package org.eclipse.che.ide.command.toolbar.ddw;

import elemental.dom.Element;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.ui.dropdown.DropDownWidget;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DropDownListBox<T> extends Composite {

    private static final DropDownListBoxUiBinder  UI_BINDER = GWT.create(DropDownListBoxUiBinder.class);
    private static final DropDownWidget.Resources resources = GWT.create(DropDownWidget.Resources.class);

    private final List<T>     items;
    private final Renderer<T> renderer;
    private final PopupPanel  panel;

    @UiField
    FlowPanel listHeader;
    @UiField
    FlowPanel marker;

    public DropDownListBox(Renderer<T> renderer) {
        this.renderer = renderer;

        items = new ArrayList<>();

        panel = new PopupPanel();
        panel.setAnimationEnabled(true);
        panel.setAnimationType(PopupPanel.AnimationType.ROLL_DOWN);

        initWidget(UI_BINDER.createAndBindUi(this));

        listHeader.setStyleName(resources.dropdownListCss().menuElement());

        marker.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        marker.addStyleName(resources.dropdownListCss().expandedImage());

        marker.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.add(new Label("test"));
                panel.show();
                panel.center();
                panel.setWidth("100px");
                panel.setHeight("100px");
            }
        }, ClickEvent.getType());
    }

    public void addItem(T item) {
        items.add(item);

        final Element element = renderer.render(item);
    }

    interface DropDownListBoxUiBinder extends UiBinder<Widget, DropDownListBox> {
    }

    public interface Renderer<T> {
        Element render(T item);
    }
}

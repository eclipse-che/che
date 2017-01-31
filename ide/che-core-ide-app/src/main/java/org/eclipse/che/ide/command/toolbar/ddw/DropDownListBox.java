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
import elemental.html.DivElement;
import elemental.html.SpanElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DropDownListBox<T> extends Composite {

    private static final DropDownListBoxUiBinder  UI_BINDER = GWT.create(DropDownListBoxUiBinder.class);
    private static final DropDownWidget.Resources resources = GWT.create(DropDownWidget.Resources.class);
    private static final Resources                res       = GWT.create(Resources.class);

    private final List<T>                         items;
    private final Renderer<T>                     renderer;
    private final PopupPanel                      panel;
    private final SimpleList<T>                   list;
    private final SimpleList.ListItemRenderer<T>  itemRenderer;
    private final SimpleList.ListEventDelegate<T> eventDelegate;

    @UiField
    FlowPanel listHeader;
    @UiField
    FlowPanel marker;

    public DropDownListBox(Renderer<T> renderer) {
        this.renderer = renderer;

        items = new ArrayList<>();

        panel = new PopupPanel(true);
        panel.setAnimationEnabled(true);
        panel.setAnimationType(PopupPanel.AnimationType.ROLL_DOWN);

        initWidget(UI_BINDER.createAndBindUi(this));

        listHeader.setStyleName(resources.dropdownListCss().menuElement());

        marker.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        marker.addStyleName(resources.dropdownListCss().expandedImage());

        marker.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.showRelativeTo(DropDownListBox.this);
                panel.show();
                panel.setWidth("294px");
                panel.setHeight("100px");
            }
        }, ClickEvent.getType());

        itemRenderer = new SimpleList.ListItemRenderer<T>() {
            @Override
            public void render(Element listItemBase, T itemData) {
                final SpanElement element = Elements.createSpanElement();
                element.setInnerText("test");

                listItemBase.appendChild(element);
            }
        };
        eventDelegate = new SimpleList.ListEventDelegate<T>() {
            @Override
            public void onListItemClicked(Element listItemBase, T itemData) {

            }

            @Override
            public void onListItemDoubleClicked(Element listItemBase, T itemData) {

            }
        };

        DivElement listView = Elements.createDivElement();
        panel.getElement().appendChild(((com.google.gwt.dom.client.Element)listView));

        list = SimpleList.create((SimpleList.View)panel.getElement().cast(),
                                 (Element)panel.getElement(),
                                 listView,
                                 res.defaultSimpleListCss(),
                                 itemRenderer,
                                 eventDelegate);

//        panel.add(list);
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

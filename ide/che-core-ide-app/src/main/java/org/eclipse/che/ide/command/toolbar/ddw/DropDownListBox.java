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
import elemental.dom.Node;
import elemental.html.ButtonElement;
import elemental.html.DivElement;
import elemental.html.TableCellElement;
import elemental.html.TableElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.user.client.ui.PopupPanel.AnimationType.ROLL_DOWN;

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
        panel.setAnimationType(ROLL_DOWN);

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
                Element label1 = Elements.createSpanElement();
                label1.setInnerText("label");
                Element icon = Elements.createSpanElement();
                icon.setInnerText("icon");

                TableCellElement label = Elements.createTDElement();
                TableCellElement path = Elements.createTDElement();
                ButtonElement buttonElement = Elements.createButtonElement();

                buttonElement.setName("name");
                buttonElement.setValue("value");

                label.setInnerHTML("label");
                path.setInnerHTML("(" + itemData.toString() + ")");

                final Button button = new Button("run");
                button.addHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
//                        event.preventDefault();
//                        event.stopPropagation();
                        Window.alert("onButtonClicked");
                    }
                }, ClickEvent.getType());

                listItemBase.appendChild(label1);
                listItemBase.appendChild(icon);
                listItemBase.appendChild((Node)button.getElement());
            }

            @Override
            public Element createElement() {
                return Elements.createTRElement();
            }
        };
        eventDelegate = new SimpleList.ListEventDelegate<T>() {
            @Override
            public void onListItemClicked(Element listItemBase, T itemData) {
                Window.alert("onListItemClicked");
            }

            @Override
            public void onListItemDoubleClicked(Element listItemBase, T itemData) {

            }
        };

        DivElement tableElement = Elements.createDivElement();

        final DivElement container = Elements.createDivElement();
        container.appendChild(tableElement);

        final DivElement box = Elements.createDivElement();
        box.appendChild(container);

        final TableElement itemHolder = Elements.createTableElement();

        final HTML suggestionsContainer = new HTML();

        panel.getElement().appendChild((com.google.gwt.dom.client.Element)box);

        suggestionsContainer.getElement().setInnerHTML("");
        suggestionsContainer.getElement().appendChild(((com.google.gwt.dom.client.Element)itemHolder));

        list = SimpleList.create((SimpleList.View)box,
                                 container,
                                 tableElement,
                                 res.defaultSimpleListCss(),
                                 itemRenderer,
                                 eventDelegate);
    }

    public void addItems(List<T> items) {
        this.list.render(items);

//        items.add(item);

//        final Element element = renderer.render(item);
    }

    interface DropDownListBoxUiBinder extends UiBinder<Widget, DropDownListBox> {
    }

    public interface Renderer<T> {
        Element render(T item);
    }
}

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
package org.eclipse.che.ide.ui.menubutton;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.util.Pair;

import java.util.List;

/**
 * Popup list for {@link MenuPopupButton}
 */
class PopupItemList extends PopupPanel {

    private final PopupItemDataProvider dataProvider;
    private final PopupActionHandler actionHandler;
    private final MenuPopupButton.Resources resources;

    private PopupItemList childList;

    private ItemWidget overItem;

    private FlowPanel content;

    public PopupItemList(List<PopupItem> children,
                         PopupItemDataProvider dataProvider,
                         PopupActionHandler actionHandler,
                         MenuPopupButton.Resources resources,
                         @Nullable String title) {
        super(true, false);
        addStyleName(resources.css().popupPanel());
        this.dataProvider = dataProvider;
        this.actionHandler = actionHandler;
        this.resources = resources;
        content = new FlowPanel();
        add(content);
        setAnimationEnabled(true);
        setAnimationType(AnimationType.ROLL_DOWN);
        if (title != null) {
            Label label = new Label(title);
            label.setStyleName(resources.css().label());
            content.add(label);
        }
        for (PopupItem child : children) {
            content.add(new ItemWidget(child));
        }

        addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (childList != null && childList.isShowing()) {
                    childList.hide(false);
                }
            }
        });

    }


    private class ItemWidget extends FlowPanel {

        private final PopupItem item;

        private Element itemLabel;

        public ItemWidget(final PopupItem item) {
            this.item = item;
            itemLabel = Document.get().createDivElement();
            itemLabel.setInnerText(item.getName());
            itemLabel.getStyle().setFloat(Style.Float.LEFT);
            getElement().appendChild(itemLabel);
            addStyleName(resources.css().popupItem());
            if (dataProvider.isGroup(item)) {
                DivElement arrow = Document.get().createDivElement();
                arrow.setInnerHTML(FontAwesome.PLAY);
                arrow.addClassName(resources.css().arrow());
                getElement().appendChild(arrow);
            }

            this.addDomHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    if (overItem != null) {
                        overItem.removeStyleName(resources.css().popupItemOver());
                    }
                    overItem = ItemWidget.this;
                    addStyleName(resources.css().popupItemOver());
                    if (childList != null) {
                        childList.hide();
                    }

                    if (dataProvider.isGroup(item)) {
                        Pair<List<PopupItem>, String> children = dataProvider.getChildren(item);
                        createChildPopup(children);
                    }
                }
            }, MouseOverEvent.getType());

            this.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (dataProvider.isGroup(item)) {
                        return;
                    }
                    hide(true);
                    actionHandler.onItemSelected(item);
                }
            }, ClickEvent.getType());
        }

        private void createChildPopup(Pair<List<PopupItem>, String> children) {
            childList = new PopupItemList(children.first, dataProvider, actionHandler, resources, "Execute on:");
            childList.setPopupPosition(getAbsoluteLeft() + getOffsetWidth(), getAbsoluteTop());
            childList.show();
            childList.setAutoHideEnabled(false);
            childList.setAnimationEnabled(false);
            childList.addCloseHandler(new CloseHandler<PopupPanel>() {
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    if (event.isAutoClosed()) {
                        hide();
                    }
                }
            });
        }
    }

}

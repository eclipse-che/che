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
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.util.Pair;

import java.util.List;
import java.util.Optional;

/** Popup list for {@link MenuPopupButton}. */
class PopupItemList extends PopupPanel {

    private final MenuPopupItemDataProvider dataProvider;
    private final MenuPopupButton.Resources resources;

    private ActionHandler actionHandler;
    private PopupItemList childList;

    private ItemWidget overItem;

    PopupItemList(List<PopupItem> children,
                  MenuPopupItemDataProvider dataProvider,
                  MenuPopupButton.Resources resources,
                  @Nullable String title) {
        super(true, false);

        this.dataProvider = dataProvider;
        this.resources = resources;

        setAnimationEnabled(true);
        setAnimationType(AnimationType.ROLL_DOWN);
        addStyleName(resources.css().popupPanel());

        final FlowPanel content = new FlowPanel();
        add(content);

        if (title != null) {
            Label label = new Label(title);
            label.setStyleName(resources.css().label());
            content.add(label);
        }

        children.forEach(child -> content.add(new ItemWidget(child)));

        addCloseHandler(event -> {
            if (childList != null && childList.isShowing()) {
                childList.hide(false);
            }
        });
    }

    Optional<ActionHandler> getActionHandler() {
        return Optional.ofNullable(actionHandler);
    }

    void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    private class ItemWidget extends FlowPanel {

        private final PopupItem item;

        ItemWidget(PopupItem item) {
            this.item = item;

            addStyleName(resources.css().popupItem());

            final Element itemLabel = Document.get().createDivElement();
            itemLabel.setInnerText(item.getName());
            itemLabel.getStyle().setFloat(Style.Float.LEFT);

            getElement().appendChild(itemLabel);

            if (dataProvider.isGroup(item)) {
                DivElement arrow = Document.get().createDivElement();
                arrow.setInnerHTML(FontAwesome.PLAY);
                arrow.addClassName(resources.css().arrow());
                getElement().appendChild(arrow);
            }

            attachEventHandlers();
        }

        private void attachEventHandlers() {
            addDomHandler(event -> {
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
            }, MouseOverEvent.getType());

            addDomHandler(event -> {
                if (dataProvider.isGroup(item)) {
                    return;
                }

                hide(true);

                getActionHandler().ifPresent(actionHandler -> actionHandler.onAction(item));
            }, ClickEvent.getType());
        }

        private void createChildPopup(Pair<List<PopupItem>, String> children) {
            childList = new PopupItemList(children.first, dataProvider, resources, "Execute on:");
            getActionHandler().ifPresent(actionHandler -> childList.setActionHandler(actionHandler));
            childList.setPopupPosition(getAbsoluteLeft() + getOffsetWidth(), getAbsoluteTop());
            childList.show();
            childList.setAutoHideEnabled(false);
            childList.setAnimationEnabled(false);
            childList.addCloseHandler(event -> {
                if (event.isAutoClosed()) {
                    hide();
                }
            });
        }
    }
}

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
package org.eclipse.che.ide.command.toolbar.button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;

/**
 * Button, on long click (~1 sec) open popup menu
 */
public class MenuPopupButton extends ButtonBase {

    private static final Resources resources;

    static {
        resources = GWT.create(Resources.class);
        resources.css().ensureInjected();
    }

    private FlowPanel marker = new FlowPanel();

    private final Timer timer = new Timer() {
        @Override
        public void run() {
            longClick();
        }
    };
    private final PopupItemDataProvider dataProvider;
    private final PopupActionHandler actionHandler;
    private final DropDownWidget.Resources dropResources;
    private PopupItemList popupItemList;

    public MenuPopupButton(final SafeHtml content,
                           final PopupItemDataProvider dataProvider,
                           final PopupActionHandler actionHandler,
                           DropDownWidget.Resources dropResources) {
        super(Document.get().createDivElement());
        this.dataProvider = dataProvider;
        this.actionHandler = actionHandler;
        this.dropResources = dropResources;

        setSize("32px", "20px");
        getElement().setInnerSafeHtml(content);
        addStyleName(resources.css().button());

        addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                addStyleName(resources.css().mouseOver());

            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                removeStyleName(resources.css().mouseOver());
                timer.cancel();
            }
        }, MouseOutEvent.getType());

        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                    timer.schedule(1000);
                } else {
                    timer.cancel();
                }
            }
        }, MouseDownEvent.getType());
        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                timer.cancel();
            }
        }, MouseUpEvent.getType());

        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (popupItemList != null && popupItemList.isShowing()) {
                    return;
                }
                PopupItem defaultItem = dataProvider.getDefaultItem();
                if (defaultItem != null) {
                    actionHandler.onItemSelected(defaultItem);
                } else {
                    longClick();
                }
            }
        });

        dataProvider.setItemDataChangedHandler(new PopupItemDataProvider.ItemDataChangeHandler() {
            @Override
            public void onItemDataChanged() {
                updateButton();
            }
        });
    }

    private void updateButton() {
        if (dataProvider.getItems().isEmpty()) {
            marker.removeFromParent();
        } else {
            marker = new FlowPanel();
            marker.getElement().appendChild(dropResources.expansionImage().getSvg().getElement());
            marker.addStyleName(resources.css().expandedImage());
            getElement().appendChild(marker.getElement());
        }
    }

    private void longClick() {
        popupItemList = new PopupItemList(dataProvider.getItems(), dataProvider, actionHandler, resources, null);
        popupItemList.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
        popupItemList.show();
    }

    public interface Resources extends ClientBundle {
        @Source({"button.css", "org/eclipse/che/ide/api/ui/style.css"})
        Css css();
    }

    public interface Css extends CssResource {
        String button();

        String mouseOver();

        String popupPanel();

        String expandedImage();

        String popupItem();

        String popupItemOver();

        String arrow();

        String label();

    }
}

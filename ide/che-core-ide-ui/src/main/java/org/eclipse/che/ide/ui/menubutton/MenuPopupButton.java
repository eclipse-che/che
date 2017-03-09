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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;

import org.vectomatic.dom.svg.ui.SVGResource;

/** Button with popup menu opened by long click. */
public class MenuPopupButton extends ButtonBase {

    private static final Resources RESOURCES;

    protected final MenuPopupItemDataProvider dataProvider;
    protected final SelectionHandler          actionHandler;

    private final Timer showMenuTimer;

    private FlowPanel marker = new FlowPanel();
    private PopupItemList popupItemList;

    public MenuPopupButton(SafeHtml content, MenuPopupItemDataProvider dataProvider, SelectionHandler actionHandler) {
        super(Document.get().createDivElement());

        this.dataProvider = dataProvider;
        this.actionHandler = actionHandler;

        getElement().setInnerSafeHtml(content);

        showMenuTimer = new Timer() {
            @Override
            public void run() {
                showMenu();
            }
        };

        addStyleName(RESOURCES.css().button());
        attachEventHandlers();

        dataProvider.setItemDataChangedHandler(this::updateButton);
    }

    private void attachEventHandlers() {
        addDomHandler(event -> showMenuTimer.cancel(), MouseOutEvent.getType());

        addDomHandler(event -> {
            if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                showMenuTimer.schedule(1000);
            } else {
                showMenuTimer.cancel();
            }
        }, MouseDownEvent.getType());

        addDomHandler(event -> showMenuTimer.cancel(), MouseUpEvent.getType());

        addClickHandler(event -> {
            if (popupItemList != null && popupItemList.isShowing()) {
                return;
            }

            final PopupItem defaultItem = dataProvider.getDefaultItem();

            if (defaultItem != null) {
                actionHandler.onItemSelected(defaultItem);
            } else {
                showMenu();
            }
        });
    }

    /** Shows or hides 'Open Menu' button. */
    private void updateButton() {
        if (dataProvider.getItems().isEmpty()) {
            marker.removeFromParent();
        } else {
            marker = new FlowPanel();
            marker.getElement().appendChild(RESOURCES.expansionImage().getSvg().getElement());
            marker.addStyleName(RESOURCES.css().expandedImage());
            getElement().appendChild(marker.getElement());
        }
    }

    private void showMenu() {
        popupItemList = new PopupItemList(dataProvider.getItems(), dataProvider, actionHandler, RESOURCES, null);
        popupItemList.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
        popupItemList.show();
    }

    public interface Resources extends ClientBundle {

        @Source("expansionIcon.svg")
        SVGResource expansionImage();

        @Source({"button.css", "org/eclipse/che/ide/api/ui/style.css"})
        Css css();
    }

    public interface Css extends CssResource {

        String button();

        String popupPanel();

        String expandedImage();

        String popupItem();

        String popupItemOver();

        String arrow();

        String label();
    }

    static {
        RESOURCES = GWT.create(Resources.class);
        RESOURCES.css().ensureInjected();
    }
}

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

import java.util.Optional;

/** Button with popup menu opened by long click. */
public class MenuPopupButton extends ButtonBase {

    private static final Resources RESOURCES;

    protected final MenuPopupItemDataProvider dataProvider;
    private final   Timer                     showMenuTimer;

    private ActionHandler actionHandler;

    private FlowPanel marker = new FlowPanel();
    private PopupItemList popupItemList;

    public MenuPopupButton(SafeHtml content, MenuPopupItemDataProvider dataProvider) {
        super(Document.get().createDivElement());

        this.dataProvider = dataProvider;

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
                getActionHandler().ifPresent(actionHandler -> actionHandler.onAction(defaultItem));
            } else {
                showMenu();
            }
        });
    }

    public Optional<ActionHandler> getActionHandler() {
        return Optional.ofNullable(actionHandler);
    }

    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
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
        popupItemList = new PopupItemList(dataProvider.getItems(), dataProvider, RESOURCES, null);
        getActionHandler().ifPresent(actionHandler -> popupItemList.setActionHandler(actionHandler));
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

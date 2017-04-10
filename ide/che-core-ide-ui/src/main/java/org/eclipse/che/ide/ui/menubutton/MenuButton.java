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

import java.util.List;
import java.util.Optional;

/** Button with popup menu. */
public class MenuButton extends ButtonBase {

    private static final Resources RESOURCES;

    protected final ItemsProvider itemsProvider;
    private final   Timer         showMenuTimer;

    private ActionHandler actionHandler;

    private FlowPanel marker = new FlowPanel();
    private ItemsList menu;

    public MenuButton(SafeHtml content, ItemsProvider itemsProvider) {
        super(Document.get().createDivElement());

        this.itemsProvider = itemsProvider;

        getElement().setInnerSafeHtml(content);

        showMenuTimer = new Timer() {
            @Override
            public void run() {
                showMenu();
            }
        };

        addStyleName(RESOURCES.css().button());
        attachEventHandlers();

        itemsProvider.setDataChangedHandler(this::updateButton);
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
            if (menu != null && menu.isShowing()) {
                return;
            }

            final Optional<MenuItem> defaultItem = itemsProvider.getDefaultItem();

            if (defaultItem.isPresent()) {
                getActionHandler().ifPresent(actionHandler -> actionHandler.onAction(defaultItem.get()));
            } else {
                showMenu();
            }
        });
    }

    public Optional<ActionHandler> getActionHandler() {
        return Optional.ofNullable(actionHandler);
    }

    /** Set {@link ActionHandler} to handle {@link MenuItem}s selection. */
    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    /** Shows or hides 'Open Menu' button. */
    private void updateButton() {
        if (itemsProvider.getItems().isEmpty()) {
            marker.removeFromParent();
        } else {
            marker = new FlowPanel();
            marker.getElement().appendChild(RESOURCES.menuArrow().getSvg().getElement());
            marker.addStyleName(RESOURCES.css().expandedImage());
            getElement().appendChild(marker.getElement());
        }
    }

    private void showMenu() {
        final List<MenuItem> menuItems = itemsProvider.getItems();

        if (!menuItems.isEmpty()) {
            menu = new ItemsList(menuItems, itemsProvider, RESOURCES, null);
            getActionHandler().ifPresent(actionHandler -> menu.setActionHandler(actionHandler));
            menu.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
            menu.show();
        }
    }

    public interface Resources extends ClientBundle {

        @Source("rightArrowIcon.svg")
        SVGResource menuArrow();

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

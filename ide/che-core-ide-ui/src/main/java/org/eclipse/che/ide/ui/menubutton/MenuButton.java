/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.menubutton;

import static com.google.gwt.dom.client.NativeEvent.BUTTON_LEFT;
import static java.util.Optional.ofNullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import java.util.List;
import java.util.Optional;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Button with popup menu. */
public class MenuButton extends FlowPanel {

  private static final Resources RESOURCES;

  protected final ItemsProvider itemsProvider;

  private final Timer showMenuTimer;

  private ActionHandler actionHandler;
  private ItemsList menu;

  public MenuButton(SafeHtml content, ItemsProvider itemsProvider) {
    super();

    this.itemsProvider = itemsProvider;

    addStyleName(RESOURCES.css().menuButton());

    showMenuTimer =
        new Timer() {
          @Override
          public void run() {
            showMenu();
          }
        };

    final FocusWidget mainButton = new MainButton(content);
    final FocusWidget dropButton = new DropButton();

    add(mainButton);
    add(dropButton);

    attachMouseEventHandlers(mainButton);
    attachMouseEventHandlers(dropButton);
  }

  private void attachMouseEventHandlers(FocusWidget widget) {
    widget.addMouseOutHandler(event -> showMenuTimer.cancel());
    widget.addMouseUpHandler(event -> showMenuTimer.cancel());
    widget.addMouseDownHandler(
        event -> {
          if (event.getNativeButton() == BUTTON_LEFT) {
            showMenuTimer.schedule(1000);
          } else {
            showMenuTimer.cancel();
          }
        });
  }

  public Optional<ActionHandler> getActionHandler() {
    return ofNullable(actionHandler);
  }

  /** Set {@link ActionHandler} to handle {@link MenuItem}s selection. */
  public void setActionHandler(ActionHandler actionHandler) {
    this.actionHandler = actionHandler;
  }

  private void showMenu() {
    final List<MenuItem> items = itemsProvider.getItems();

    if (!items.isEmpty()) {
      menu = new ItemsList(items, itemsProvider, RESOURCES, null);
      getActionHandler().ifPresent(handler -> menu.setActionHandler(handler));
      menu.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
      menu.show();
      menu.getElement().setId("commandsPopup");
    }
  }

  public interface Resources extends ClientBundle {

    @Source({"button.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css css();

    @Source("arrowIcon.svg")
    SVGResource arrowIcon();
  }

  public interface Css extends CssResource {

    String menuButton();

    String button();

    String mainButton();

    String dropButton();

    String popupPanel();

    String expandedImage();

    String popupItem();

    String popupItemOver();

    String arrow();

    String label();
  }

  private class MainButton extends FocusWidget {

    MainButton(SafeHtml content) {
      super(Document.get().createDivElement());

      getElement().setInnerSafeHtml(content);

      addStyleName(RESOURCES.css().button());
      addStyleName(RESOURCES.css().mainButton());

      addClickHandler(
          event -> {
            if (menu != null && menu.isShowing()) {
              return;
            }

            final Optional<MenuItem> defaultItem = itemsProvider.getDefaultItem();

            if (defaultItem.isPresent()) {
              getActionHandler()
                  .ifPresent(actionHandler -> actionHandler.onAction(defaultItem.get()));
            } else {
              showMenu();
            }
          });
    }
  }

  private class DropButton extends FocusWidget {

    DropButton() {
      super(Document.get().createDivElement());

      addStyleName(RESOURCES.css().button());
      addStyleName(RESOURCES.css().dropButton());

      final FlowPanel marker = new FlowPanel();
      marker.getElement().appendChild(RESOURCES.arrowIcon().getSvg().getElement());
      marker.addStyleName(RESOURCES.css().expandedImage());

      getElement().appendChild(marker.getElement());

      addClickHandler(
          event -> {
            if (menu == null || !menu.isShowing()) {
              showMenu();
            }
          });
    }
  }

  static {
    RESOURCES = GWT.create(Resources.class);
    RESOURCES.css().ensureInjected();
  }
}

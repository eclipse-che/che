/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.popup;

import static elemental.css.CSSStyleDeclaration.Unit.PX;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventTarget;
import elemental.events.MouseEvent;
import elemental.html.ClientRect;
import elemental.html.Window;
import org.eclipse.che.ide.util.dom.Elements;

/** Popup widow that hides itself on outside mouse down actions.. */
public abstract class PopupWidget<T> {

  private static final int MIN_WIDTH = 400;
  private static final int MIN_HEIGHT = 130;

  protected final Element popupBodyElement;

  /** The list (ul) element for the popup. */
  private final Element listElement;

  private final EventListener popupListener;

  /** The keyboard listener in the popup. */
  private final EventListener keyboardListener;

  protected final PopupResources popupResources;

  /** The previously focused element. */
  private Element previousFocus;
  /** The main element for the popup. */
  private Element popupElement;

  public PopupWidget(final PopupResources popupResources, String title) {
    this.popupResources = popupResources;

    popupElement = Elements.createDivElement(popupResources.popupStyle().popup());

    Element headerElement = Elements.createDivElement(popupResources.popupStyle().header());
    headerElement.setInnerText(title);
    popupElement.appendChild(headerElement);

    popupBodyElement = Elements.createDivElement(popupResources.popupStyle().body());
    popupElement.appendChild(popupBodyElement);

    listElement = Elements.createUListElement();
    popupBodyElement.appendChild(listElement);

    popupListener =
        new EventListener() {
          @Override
          public void handleEvent(final Event evt) {
            if (evt instanceof MouseEvent) {
              final MouseEvent mouseEvent = (MouseEvent) evt;
              final EventTarget target = mouseEvent.getTarget();
              if (target instanceof Element) {
                final Element elementTarget = (Element) target;
                if (!PopupWidget.this.popupElement.contains(elementTarget)) {
                  hide();
                  evt.preventDefault();
                }
              }
            }
            // else won't happen
          }
        };

    keyboardListener = new PopupKeyDownListener(this, this.listElement);
  }

  public abstract String getEmptyMessage();

  /** Create an element for the given item data. */
  public abstract Element createItem(final T itemModel);

  /**
   * Show the widget at the given document position.
   *
   * @param left the horizontal pixel position in the document
   * @param top the vertical pixel position in the document
   */
  public void show(final float left, final float top) {
    if (!listElement.hasChildNodes()) {
      Element emptyElement = Elements.createLiElement(popupResources.popupStyle().item());
      emptyElement.setTextContent(getEmptyMessage());
      listElement.appendChild(emptyElement);
      return;
    }

    /* Reset popup dimensions and show. */
    popupElement.getStyle().setLeft(left, PX);
    popupElement.getStyle().setTop(top, PX);
    popupElement.getStyle().setWidth("" + MIN_WIDTH + "px");
    popupElement.getStyle().setHeight("" + MIN_HEIGHT + "px");
    popupElement.getStyle().setOpacity(0);
    Elements.getDocument().getBody().appendChild(popupElement);

    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                popupElement.getStyle().setOpacity(1);
              }
            });

    Elements.getDocument().addEventListener(Event.MOUSEDOWN, popupListener, false);

    // does it fit inside the doc body?
    // This does exactly the same thing for height/top and width/left

    final Window window = Elements.getWindow();
    final int winX = window.getInnerWidth();
    final int winY = window.getInnerHeight();
    ClientRect widgetRect = this.popupElement.getBoundingClientRect();
    if (widgetRect.getBottom() > winY) {
      // it doesn't fit
      final float overflow = widgetRect.getBottom() - winY;
      if (widgetRect.getHeight() - overflow > MIN_HEIGHT) {
        // the widget can be shrunk to fit
        this.popupElement.getStyle().setHeight(widgetRect.getHeight() - overflow, PX);
      } else {
        // we need to shrink AND move the widget up
        this.popupElement.getStyle().setHeight(MIN_HEIGHT, PX);
        final int newTop = Math.max(winY - MIN_HEIGHT, MIN_HEIGHT);
        this.popupElement.getStyle().setTop(newTop, PX);
      }
    }
    // bounding rect has changed
    widgetRect = this.popupElement.getBoundingClientRect();
    if (widgetRect.getRight() > winX) {
      // it doesn't fit
      final float overflow = widgetRect.getRight() - winX;
      if (widgetRect.getWidth() - overflow > MIN_WIDTH) {
        // the widget can be shrunk to fit
        this.popupElement.getStyle().setWidth(widgetRect.getWidth() - overflow, PX);
      } else {
        // we need to shrink AND move the widget up
        this.popupElement.getStyle().setWidth(MIN_WIDTH, PX);
        final int newLeft = Math.max(winX - MIN_WIDTH, MIN_WIDTH);
        this.popupElement.getStyle().setLeft(newLeft - MIN_WIDTH, PX);
      }
    }

    if (needsFocus()) {
      // save previous focus and set focus in popup
      previousFocus = Elements.getDocument().getActiveElement();
      Element elementToFocus = listElement.getFirstElementChild();
      elementToFocus.setAttribute("selected", "true");
      elementToFocus.focus();
    }

    // add key event listener on popup
    listElement.addEventListener(Event.KEYDOWN, keyboardListener, false);
  }

  /**
   * Add an item in the popup view.
   *
   * @param itemModel the data for the item
   */
  public void addItem(final T itemModel) {
    if (itemModel == null) {
      return;
    }

    Element element = createItem(itemModel);
    element.setTabIndex(1);
    listElement.appendChild(element);
  }

  /** Hide the popup. */
  public void hide() {
    // restore previous focus state
    if (previousFocus != null) {
      previousFocus.focus();
      previousFocus = null;
    }

    popupElement.getStyle().setOpacity(0);
    new Timer() {
      @Override
      public void run() {
        if (popupElement != null) {
          // detach assist popup
          popupElement.getParentNode().removeChild(popupElement);
          popupElement = null;
        }
        // remove all items from popup element
        listElement.setInnerHTML("");
      }
    }.schedule(250);

    // remove the keyboard listener
    listElement.removeEventListener(Event.KEYDOWN, keyboardListener, false);

    // remove the mouse listener
    Elements.getDocument().removeEventListener(Event.MOUSEDOWN, popupListener);
  }

  /**
   * Action taken when an item is validated.
   *
   * @param itemElement the validated item
   */
  public void validateItem(final Element itemElement) {
    // by default, only hide the popup
    hide();
  }

  /**
   * Returns the widget viewed as an element.
   *
   * @return the element
   */
  public Element asElement() {
    return this.popupElement;
  }

  /**
   * Tells if the popup widget wants focus.<br>
   * Override the method to match needed value.
   *
   * @return true iff the widget needs the focus
   */
  public boolean needsFocus() {
    return false;
  }
}

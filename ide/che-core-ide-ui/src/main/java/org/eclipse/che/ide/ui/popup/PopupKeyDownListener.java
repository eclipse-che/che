/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.popup;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.dom.Elements;

public class PopupKeyDownListener implements EventListener {

  /** The related opopup widget. */
  private final PopupWidget<?> popupWidget;

  /** The list element (contains the items). */
  private final Element listElement;

  public PopupKeyDownListener(final PopupWidget<?> popupWidget, final Element listElement) {
    this.popupWidget = popupWidget;
    this.listElement = listElement;
  }

  @Override
  public void handleEvent(final Event evt) {
    if (evt instanceof KeyboardEvent) {
      final KeyboardEvent keyEvent = (KeyboardEvent) evt;
      switch (keyEvent.getKeyCode()) {
        case KeyCodes.KEY_ESCAPE:
          Scheduler.get()
              .scheduleDeferred(
                  new ScheduledCommand() {
                    @Override
                    public void execute() {
                      popupWidget.hide();
                    }
                  });
          break;
        case KeyCodes.KEY_DOWN:
          focusNext();
          break;
        case KeyCodes.KEY_UP:
          focusPrevious();
          break;
        case KeyCodes.KEY_HOME:
          focusFirst();
          break;
        case KeyCodes.KEY_END:
          focusLast();
          break;
        case KeyCodes.KEY_ENTER:
          evt.preventDefault();
          evt.stopImmediatePropagation();
          validateItem();
          break;
        default:
      }
    }
  }

  /**
   * Focus the next item in the list, or the first item if we are already at the last. In the case
   * the list doesn't currently have focus, focus the first element.
   */
  private void focusNext() {
    final Element current = Elements.getDocument().getActiveElement();
    if (current.getParentElement().isEqualNode(listElement)) {
      final Element next = current.getNextElementSibling();
      if (next != null) {
        select(next);
        next.focus();
      } else {
        focusFirst();
      }
    } else {
      // we don't actually have focus, focus the first element
      focusFirst();
    }
  }

  /**
   * Focus the previous item in the list, or the last item if we are already at the first. In the
   * case the list doesn't currently have focus, focus the first element.
   */
  private void focusPrevious() {
    final Element current = Elements.getDocument().getActiveElement();
    if (current.getParentElement().isEqualNode(listElement)) {
      final Element prev = current.getPreviousElementSibling();
      if (prev != null) {
        select(prev);
        prev.focus();
      } else {
        focusLast();
      }
    } else {
      // we don't actually have focus, focus the first element
      focusFirst();
    }
  }

  /** Focus the first item in the list (if any). */
  private void focusFirst() {
    if (listElement.hasChildNodes()) {
      Element firstElement = listElement.getFirstElementChild();
      select(firstElement);
      firstElement.focus();
    }
  }

  /** Focus the last item in the list (if any). */
  private void focusLast() {
    if (listElement.hasChildNodes()) {
      Element lastElement = listElement.getLastElementChild();
      select(lastElement);
      lastElement.focus();
    }
  }

  /** Activates the currently selected item. */
  private void validateItem() {
    final Element current = Elements.getDocument().getActiveElement();
    if (current.getParentElement().isEqualNode(listElement)) {
      this.popupWidget.validateItem(current);
    }
  }

  private void select(Element elementToSelect) {
    if (elementToSelect == null) {
      return;
    }

    Element currentSelectedElement = getSelectedElement();
    if (currentSelectedElement == null) {
      elementToSelect.setAttribute("selected", "true");
      return;
    }

    if (currentSelectedElement.isEqualNode(elementToSelect)) {
      return;
    }

    currentSelectedElement.removeAttribute("selected");
    elementToSelect.setAttribute("selected", "true");
  }

  /**
   * Returns current selected element when we have an item in focus or {@code null} otherwise
   *
   * @return current selected element or {@code null} when we have no any items in focus
   */
  @Nullable
  private Element getSelectedElement() {
    Element selectedElement = Elements.getDocument().getActiveElement();
    return selectedElement.getParentElement().isEqualNode(listElement) ? selectedElement : null;
  }
}

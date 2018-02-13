// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.dom;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MouseEvent;
import elemental.html.ClientRect;
import elemental.html.DivElement;
import elemental.js.dom.JsElement;
import org.eclipse.che.ide.util.browser.UserAgent;

/** Utility methods for DOM manipulation. */
public final class DomUtils {

  private static final ZIndexImpl zIndexImpl = GWT.create(ZIndexImpl.class);

  public static class ZIndexImpl {
    private int zIndexId = 1000000;

    int incrementAndGetTopZIndex() {
      return ++zIndexId;
    }

    int incrementAndGetTopZIndex(int i) {
      zIndexId = zIndexId + i + 1;
      return zIndexId;
    }
  }

  public static class Offset {
    public int top = 0;

    public int left = 0;

    private Offset() {}

    private Offset(int top, int left) {
      this.top = top;
      this.left = left;
    }
  }

  /**
   * Increments and returns the top z-index value.
   *
   * @return index of z-index value
   */
  public static int incrementAndGetTopZIndex() {
    return zIndexImpl.incrementAndGetTopZIndex();
  }

  /**
   * Increments and returns the top z-index value.
   *
   * @param i increment index
   * @return index of z-index value
   */
  public static int incrementAndGetTopZIndex(int i) {
    return zIndexImpl.incrementAndGetTopZIndex(i);
  }

  private static final EventListener STOP_PROPAGATION_EVENT_LISTENER =
      new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          evt.stopPropagation();
          evt.preventDefault();
        }
      };

  /** Returns the client offset to the top-left of the given element. */
  @Deprecated
  public static Offset calculateElementClientOffset(Element element) {
    return calculateElementOffset(element, null, false);
  }

  /**
   * Returns an offset to the top-left of a child element relative to the top-left of an ancestor
   * element, optionally including any scroll top or left in elements from the ancestor (inclusive)
   * to the child (exclusive).
   *
   * @param ancestorElement optional, if null the offset from the top-left of the page will be
   *     given. Should not be the childElement.
   */
  @Deprecated
  public static Offset calculateElementOffset(
      Element childElement, Element ancestorElement, boolean includeScroll) {

    Offset offset = new Offset();
    Element element = childElement;
    for (;
        element.getOffsetParent() != null && element != ancestorElement;
        element = element.getOffsetParent()) {
      offset.top += element.getOffsetTop();
      offset.left += element.getOffsetLeft();

      if (!includeScroll) {
        offset.top -= element.getOffsetParent().getScrollTop();
        offset.left -= element.getOffsetParent().getScrollLeft();
      }
    }

    return offset;
  }

  /**
   * Wrapper for getting the offsetX from a mouse event that provides a fallback implementation for
   * Firefox. (See https://bugzilla.mozilla.org/show_bug.cgi?id=122665#c3 )
   */
  public static int getOffsetX(MouseEvent event) {
    if (UserAgent.isFirefox()) {
      return event.getClientX() - calculateElementClientOffset((Element) event.getTarget()).left;
    } else {
      return event.getOffsetX();
    }
  }

  /** @see #getOffsetX(MouseEvent) */
  public static int getOffsetY(MouseEvent event) {
    if (UserAgent.isFirefox()) {
      return event.getClientY() - calculateElementClientOffset((Element) event.getTarget()).top;
    } else {
      return event.getOffsetY();
    }
  }

  public static Element getNthChild(Element element, int index) {
    Element child = getFirstChildElement(element);
    while (child != null && index > 0) {
      --index;
      child = getNextSiblingElement(element);
    }
    return child;
  }

  public static Element getNthChildWithClassName(Element element, int index, String className) {
    Element child = getFirstChildElement(element);
    while (child != null) {
      if (Elements.hasClassName(className, child)) {
        --index;
        if (index < 0) {
          break;
        }
      }
      child = getNextSiblingElement(child);
    }
    return child;
  }

  /** @return number of previous sibling elements that have the given class */
  public static int getSiblingIndexWithClassName(Element element, String className) {
    int index = 0;
    while (element != null) {
      element = (Element) element.getPreviousSibling();
      if (element != null && Elements.hasClassName(className, element)) {
        ++index;
      }
    }
    return index;
  }

  public static Element getFirstElementByClassName(Element element, String className) {
    return (Element) element.getElementsByClassName(className).item(0);
  }

  public static DivElement appendDivWithTextContent(Element root, String className, String text) {
    DivElement element = Elements.createDivElement(className);
    element.setTextContent(text);
    root.appendChild(element);
    return element;
  }

  /**
   * Ensures that the {@code scrollable} element is scrolled such that {@code target} is visible.
   *
   * <p>Note: This can trigger a synchronous layout.
   */
  public static boolean ensureScrolledTo(Element scrollable, Element target) {
    ClientRect targetBounds = target.getBoundingClientRect();
    ClientRect scrollableBounds = scrollable.getBoundingClientRect();

    int deltaBottoms = (int) (targetBounds.getBottom() - scrollableBounds.getBottom());
    int deltaTops = (int) (targetBounds.getTop() - scrollableBounds.getTop());

    if (deltaTops >= 0 && deltaBottoms <= 0) {
      // In bounds
      return false;
    }

    if (targetBounds.getHeight() > scrollableBounds.getHeight() || deltaTops < 0) {
      /*
       * Selected is taller than viewport height or selected is scrolled above
       * viewport, so set to top
       */
      scrollable.setScrollTop(scrollable.getScrollTop() + deltaTops);
    } else {
      // Selected is scrolled below viewport
      scrollable.setScrollTop(scrollable.getScrollTop() + deltaBottoms);
    }

    return true;
  }

  /**
   * Checks whether the given {@code target} element is fully visible in {@code scrollable}'s
   * scrolled viewport.
   *
   * <p>Note: This can trigger a synchronous layout.
   */
  public static boolean isFullyInScrollViewport(Element scrollable, Element target) {
    ClientRect targetBounds = target.getBoundingClientRect();
    ClientRect scrollableBounds = scrollable.getBoundingClientRect();

    return targetBounds.getTop() >= scrollableBounds.getTop()
        && targetBounds.getBottom() <= scrollableBounds.getBottom();
  }

  /** Stops propagation for the common mouse events (down, move, up, click, dblclick). */
  public static void stopMousePropagation(Element element) {
    element.addEventListener(Event.MOUSEDOWN, STOP_PROPAGATION_EVENT_LISTENER, false);
    element.addEventListener(Event.MOUSEMOVE, STOP_PROPAGATION_EVENT_LISTENER, false);
    element.addEventListener(Event.MOUSEUP, STOP_PROPAGATION_EVENT_LISTENER, false);
    element.addEventListener(Event.CLICK, STOP_PROPAGATION_EVENT_LISTENER, false);
    element.addEventListener(Event.DBLCLICK, STOP_PROPAGATION_EVENT_LISTENER, false);
  }

  /**
   * Prevent propagation of scrolling to parent containers on mouse wheeling, when target container
   * can not be scrolled anymore.
   */
  public static void preventExcessiveScrollingPropagation(final Element container) {
    // The MOUSEWHEEL does not exist on FF, so in FF the common browser behavior won't be canceled
    // and the parent container will be scrolled.
    container.addEventListener(
        Event.MOUSEWHEEL,
        new EventListener() {
          @Override
          public void handleEvent(Event evt) {
            int deltaY = DOM.eventGetMouseWheelVelocityY((com.google.gwt.user.client.Event) evt);
            int scrollTop = container.getScrollTop();
            if (deltaY < 0 && scrollTop == 0) {
              evt.preventDefault();
            } else if (deltaY > 0
                && scrollTop == (container.getScrollHeight() - container.getClientHeight())) {
              evt.preventDefault();
            }
            evt.stopPropagation();
          }
        },
        false);
  }

  /**
   * Doing Elements.asJsElement(button).setDisabled(true); doesn't work for buttons, possibly
   * because they're actually AnchorElements
   */
  public static void setDisabled(Element element, boolean disabled) {
    if (disabled) {
      element.setAttribute("disabled", "disabled");
    } else {
      element.removeAttribute("disabled");
    }
  }

  public static boolean getDisabled(Element element) {
    return element.hasAttribute("disabled");
  }

  /** @return true if the provided element or one of its children have focus. */
  public static boolean isElementOrChildFocused(Element element) {
    Element active = element.getOwnerDocument().getActiveElement();
    return element.contains(active);
  }

  public static boolean isWidgetOrChildFocused(Widget widget) {
    return isElementOrChildFocused((Element) widget.getElement());
  }

  public static JsElement getFirstChildElement(Element element) {
    elemental.dom.Node child = element.getFirstChild();
    while ((child != null) && child.getNodeType() != Node.ELEMENT_NODE) {
      child = child.getNextSibling();
    }
    return ((JsElement) child);
  }

  public static JsElement getNextSiblingElement(Element element) {
    Node sib = element.getNextSibling();
    while ((sib != null) && sib.getNodeType() != Node.ELEMENT_NODE) {
      sib = sib.getNextSibling();
    }
    return ((JsElement) sib);
  }

  public static void removeFromParent(Element element) {
    Element parent = getParentElement(element);
    if (parent != null) {
      parent.removeChild(element);
    }
  }

  public static elemental.js.dom.JsElement getParentElement(Element element) {
    Node parent = element.getParentNode();
    if ((parent == null) || parent.getNodeType() != Node.ELEMENT_NODE) {
      parent = null;
    }
    return (JsElement) parent;
  }

  public static void ensureDebugId(Element element, String id) {
    final String uniqueId = com.google.gwt.dom.client.Document.get().createUniqueId();
    final String trimId = Strings.nullToEmpty(id).trim();
    final String debugId = Strings.isNullOrEmpty(trimId) ? uniqueId : trimId + "-" + uniqueId;

    UIObject.ensureDebugId((com.google.gwt.dom.client.Element) element, debugId);
  }

  private DomUtils() {} // COV_NF_LINE
}

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

package org.eclipse.che.ide.util;

import com.google.gwt.user.client.Timer;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.EventTarget;
import elemental.events.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller to manage a group of elements that are hovered and unhovered together. For example,
 * you can use this controller to link a button to its submenu.
 *
 * <p>When the user mouses over any of the "partner" elements, the controller calls {@link
 * HoverListener#onHover()}. When the user mouses out of all partner elements and does not mouse
 * over one of the elements within a fixed delay, the controller calls {@link
 * UnhoverListener#onUnhover()}.
 *
 * <p>The default delay is 1300ms, but you can override this. We recommend using one of the static
 * values so that similar UI components use the same delay.
 */
public class HoverController {

  /** The default unhover delay. */
  public static final int DEFAULT_UNHOVER_DELAY = 1300;

  /** The unhover delay used for dropdown UI components, such as button menus. */
  public static final int DROP_DOWN_UNHOVER_DELAY = 300;

  /** Listener interface to be notified of hover state changes. */
  public static interface HoverListener {
    /** Handles the event when the user hovers one of the partner elements. */
    public void onHover();
  }

  /** Listener interface to be notified of hover state changes. */
  public static interface UnhoverListener {
    /**
     * Handles the event when the user unhovers one of the partner elements, and does not hover
     * another partner element within the fixed delay.
     */
    public void onUnhover();
  }

  /** A helper class to store a partner element and it's event removers. */
  private class PartnerHolder {
    private final Element element;

    private final EventRemover mouseOverRemover;

    private final EventRemover mouseOutRemover;

    PartnerHolder(final Element element) {
      this.element = element;
      mouseOverRemover =
          element.addEventListener(
              Event.MOUSEOVER,
              new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                  if (relatedTargetOutsideElement((MouseEvent) evt)) {
                    hover();
                  }
                }
              },
              false);
      mouseOutRemover =
          element.addEventListener(
              Event.MOUSEOUT,
              new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                  if (relatedTargetOutsideElement((MouseEvent) evt)) {
                    unhover();
                  }
                }
              },
              false);
    }

    Element getElement() {
      return element;
    }

    void teardown() {
      mouseOverRemover.remove();
      mouseOutRemover.remove();
    }

    /**
     * Checks if the related target of the MouseEvent (the "from" element for a mouseover, the "to"
     * element for a mouseout) is actually outside of the partner element. If the target element
     * contains children, we will receive mouseover/mouseout events when the mouse moves over/out of
     * the children, even if the mouse is still within the partner element. These intra-element
     * events don't affect the hover state of the partner element, so we want to ignore them.
     */
    private boolean relatedTargetOutsideElement(MouseEvent evt) {
      EventTarget relatedTarget = evt.getRelatedTarget();
      return relatedTarget == null || !element.contains((Node) relatedTarget);
    }
  }

  private HoverListener hoverListener;

  private UnhoverListener unhoverListener;

  private boolean isHovering = false;

  private int unhoverDelay = DEFAULT_UNHOVER_DELAY;

  private Timer unhoverTimer;

  private final List<PartnerHolder> partners = new ArrayList<>();

  /**
   * Adds a partner element to this controller. See class javadoc for an explanation of the
   * interaction between partner elements.
   */
  public void addPartner(Element element) {
    if (!hasPartner(element)) {
      partners.add(new PartnerHolder(element));
    }
  }

  /** Removes a partner element from this controller. */
  public void removePartner(Element element) {
    for (int i = 0, n = partners.size(); i < n; ++i) {
      PartnerHolder holder = partners.get(i);
      if (holder.getElement() == element) {
        holder.teardown();
        partners.remove(i);
        break;
      }
    }
  }

  private boolean hasPartner(Element element) {
    for (int i = 0, n = partners.size(); i < n; ++i) {
      PartnerHolder holder = partners.get(i);
      if (holder.getElement() == element) {
        return true;
      }
    }
    return false;
  }

  /** Sets the listener that will receive events when any of the partner elements is hovered. */
  public void setHoverListener(HoverListener listener) {
    this.hoverListener = listener;
  }

  /** Sets the listener that will receive events when all of the partner elements are unhovered. */
  public void setUnhoverListener(UnhoverListener listener) {
    this.unhoverListener = listener;
  }

  /**
   * Sets the delay between the last native mouseout event and when {@link
   * UnhoverListener#onUnhover()} is called. If the user mouses out of one partner element and over
   * another partner element within the unhover delay, the unhover event is not triggered.
   *
   * <p>If the delay is zero, the unhover listener is called synchronously. If the delay is less
   * than zero, the unhover listener is never called.
   *
   * @param delay the delay in milliseconds
   */
  public void setUnhoverDelay(int delay) {
    this.unhoverDelay = delay;
  }

  /**
   * Cancels the unhover timer if one is pending. This will prevent an unhover listener from firing
   * until the next time the user mouses out of a partner element.
   */
  public void cancelUnhoverTimer() {
    if (unhoverTimer != null) {
      unhoverTimer.cancel();
      unhoverTimer = null;
    }
  }

  /**
   * Flushes the unhover timer if one is pending. This will reset the hover controller to a state
   * where it can fire a hover event the next time the element is hovered.
   */
  public void flushUnhoverTimer() {
    if (unhoverTimer != null) {
      cancelUnhoverTimer();
      unhoverNow();
    }
  }

  /**
   * Updates the state of the controller to indicate that the user is hovering over one of the
   * partner elements.
   */
  private void hover() {
    cancelUnhoverTimer();

    isHovering = true;
    if (hoverListener != null) {
      hoverListener.onHover();
    }
  }

  /**
   * Starts a timer that will update the controller to the unhover state if the user doesn't hover
   * one of the partner elements within the specified unhover delay.
   */
  private void unhover() {
    // Early exit if already unhovering or if the delay is negative.
    if (!isHovering || unhoverDelay < 0) {
      return;
    }

    if (unhoverDelay == 0) {
      unhoverNow();
    } else if (unhoverTimer == null) {
      // Wait a short time before unhovering so the user has a chance to move
      // the mouse from one partner to another.
      unhoverTimer =
          new Timer() {
            @Override
            public void run() {
              unhoverNow();
            }
          };
      unhoverTimer.schedule(unhoverDelay);
    }
  }

  /**
   * Updates the state of the controller to indicate that the user is no longer hovering any of the
   * partner elements.
   */
  private void unhoverNow() {
    cancelUnhoverTimer();
    isHovering = false;
    if (unhoverListener != null) {
      unhoverListener.onUnhover();
    }
  }
}

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

package org.eclipse.che.ide.ui.menu;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.util.Timer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.mvp.UiComponent;
import org.eclipse.che.ide.util.HoverController;
import org.eclipse.che.ide.util.HoverController.UnhoverListener;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Component that can automatically hide its View when the mouse is not over it. Alternatively, this
 * can be used to have a pop-up that closes when clicked outside.
 *
 * <p>WARNING: If you happen to detach the AutoHideComponent's View from the DOM while it is
 * visible, you must remember to call forceHide(). If you don't you will never get a mouse out
 * event, and thus will have a dangling global click listener that has leaked and will trap the next
 * click.
 *
 * @param <V> component view class
 * @param <M> component model class
 */
public abstract class AutoHideComponent<
        V extends AutoHideView<?>, M extends AutoHideComponent.AutoHideModel>
    extends UiComponent<V> {

  /** Handler used to catch auto hide events. */
  public static interface AutoHideHandler {
    void onShow();

    /** Called when the element is hidden, in response to an auto hide event or programatically. */
    void onHide();
  }

  /** Instance state */
  public static class AutoHideModel {
    boolean hidden = true;
  }

  // Capture clicks on the body to close the popup.
  private AutoHideHandler autoHideHandler;

  private final EventListener outsideClickListener =
      new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          for (Element clickTarget : clickTargets) {
            if (clickTarget.contains((Node) evt.getTarget())) {
              return;
            }
          }
          forceHide();
          if (stopOutsideClick) {
            evt.stopPropagation();
          }
        }
      };

  private EventRemover outsideClickListenerRemover;

  private final M model;

  private boolean hideBlocked = false;

  private boolean stopOutsideClick = true;

  private final HoverController hoverController;

  private final List<Element> clickTargets = new ArrayList<>();

  private final Timer hideTimer =
      new Timer() {
        @Override
        public void run() {
          forceHide();
        }
      };

  /**
   * Constructor.
   *
   * @param view
   * @param model
   */
  protected AutoHideComponent(V view, M model) {
    super(view);
    this.model = model;

    // Setup the hover controller to handle hover events.
    hoverController = new HoverController();
    hoverController.addPartner(view.getElement());
    hoverController.setUnhoverListener(
        new UnhoverListener() {
          @Override
          public void onUnhover() {
            if (isShowing()) {
              hide();
            }
          }
        });

    // Add ourselves to the valid click targets
    clickTargets.add(view.getElement());
  }

  /**
   * Add a partner element to the component. Partners are hover targets that will keep the component
   * visible. If the user hovers over a partner element, the component will not be hidden.
   */
  public void addPartner(Element element) {
    hoverController.addPartner(element);
  }

  /** Removes a partner element from the component. */
  public void removePartner(Element element) {
    hoverController.removePartner(element);
  }

  /**
   * Add one or more partner elements that when clicked when not cause the auto-hide component to
   * hide automatically. This is useful for toggle buttons that have a dropdown or similar.
   */
  public void addPartnerClickTargets(Element... elems) {
    if (elems != null) {
      for (Element e : elems) {
        clickTargets.add(e);
      }
    }
  }

  public void removePartnerClickTargets(Element... elems) {
    if (elems != null) {
      for (Element e : elems) {
        clickTargets.remove(e);
      }
    }
  }

  public M getModel() {
    return model;
  }

  /** Cancels any pending deferred hide. */
  public void cancelPendingHide() {
    hoverController.cancelUnhoverTimer();
  }

  /**
   * Hides the View immediately.
   *
   * <p>WARNING: If you happen to detach the AutoHideComponent's View from the DOM while it is
   * visible, you must remember to call forceHide(). If you don't you will never get a mouse out
   * event, and thus will have a dangling global click listener that has leaked and will trap the
   * next click.
   */
  public void forceHide() {
    if (outsideClickListenerRemover != null) {
      outsideClickListenerRemover.remove();
      outsideClickListenerRemover = null;
    }

    AutoHideModel model = getModel();

    // If the thing is already hidden, then we don't need to do anything.
    if (isShowing()) {
      model.hidden = true;
      hideView();
      if (autoHideHandler != null) {
        autoHideHandler.onHide();
      }
    }

    // Force the unhover timer to fire. This is a no-op because we are already
    // hidden, but it resets the hover controller state.
    hoverController.flushUnhoverTimer();
  }

  /** @return true if in the showing state, even if still animating closed */
  public boolean isShowing() {
    return !getModel().hidden;
  }

  /**
   * Hides the View for this component if the mouse isn't over, or if we don't have a pending
   * deferred hide.
   *
   * @return whether or not we forced the hide. If the mouse is still over the popup, then we don't
   *     hide and thus return false.
   */
  public boolean hide() {
    if (!hideBlocked) {
      forceHide();
      return true;
    }

    return false;
  }

  /**
   * Prevents hiding, for example if a cascaded AutoHideComponent or the file-selection box of a
   * form is showing.
   */
  public void setHideBlocked(boolean block) {
    this.hideBlocked = block;
  }

  /**
   * Sets the delay in ms for how long the Component waits before being hidden. Negative delay will
   * make this component waiting until the outside click.
   *
   * @param delay time in ms before hiding
   */
  public void setDelay(int delay) {
    hoverController.setUnhoverDelay(delay);
  }

  /** Set the {@link AutoHideHandler} associated with an element. */
  public void setAutoHideHandler(AutoHideHandler handler) {
    this.autoHideHandler = handler;
  }

  /**
   * Set to True if this component should capture and prevent clicks outside the component when it
   * closes itself.
   */
  public void setCaptureOutsideClickOnClose(boolean close) {
    stopOutsideClick = close;
  }

  /** Makes the View visible and schedules it to be re-hidden if the user does not mouse over. */
  public void show() {
    // Nothing to do if it is showing.
    if (isShowing()) {
      return;
    }

    getView().show();
    getModel().hidden = false;

    // Catch clicks that are outside the autohide component to trigger a hide.
    outsideClickListenerRemover =
        Elements.getBody().addEventListener(Event.MOUSEDOWN, outsideClickListener, true);

    if (autoHideHandler != null) {
      autoHideHandler.onShow();
    }
  }

  /** Displays the autohide component for a maximum of forceHideAfterMs */
  public void show(int forceHideAfterMs) {
    show();
    hideTimer.schedule(forceHideAfterMs);
  }

  protected HoverController getHoverController() {
    return hoverController;
  }

  protected void hideView() {
    getView().hide();
  }
}

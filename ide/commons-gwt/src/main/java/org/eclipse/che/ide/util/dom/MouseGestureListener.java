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

import com.google.gwt.user.client.Timer;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.MouseEvent;
import org.eclipse.che.ide.util.ListenerRegistrar;

/**
 * An {@link EventListener} implementation that parses the low-level events and produces high-level
 * gestures, such as triple-click.
 *
 * <p>This class differs subtly from the native {@link Event#CLICK} and {@link Event#DBLCLICK}
 * events by dispatching the respective callback on mouse down instead of mouse up. This API
 * difference allows clients to easily handle for example the double-click-and-drag case.
 */
public class MouseGestureListener {

  /*
   * TODO: When we have time, look into learning the native OS's
   * delay by checking timing between CLICK and DBLCLICK events
   */
  /**
   * The maximum time in milliseconds between clicks to consider the latter click to be part of the
   * same gesture as the previous click.
   */
  public static final int MAX_CLICK_TIMEOUT_MS = 250;

  public static ListenerRegistrar.Remover createAndAttach(Element element, Callback callback) {
    MouseGestureListener instance = new MouseGestureListener(callback);
    final EventRemover eventRemover =
        element.addEventListener(Event.MOUSEDOWN, instance.captureListener, false);
    return new ListenerRegistrar.Remover() {
      @Override
      public void remove() {
        eventRemover.remove();
      }
    };
  }

  /**
   * An interface that receives callbacks from the {@link MouseGestureListener} when gestures occur.
   */
  public interface Callback {
    /** @return false to abort any handling of subsequent mouse events in this gesture */
    boolean onClick(int clickCount, MouseEvent event);

    void onDrag(MouseEvent event);

    void onDragRelease(MouseEvent event);
  }

  private final Callback callback;

  private final MouseCaptureListener captureListener =
      new MouseCaptureListener() {
        @Override
        protected boolean onMouseDown(MouseEvent evt) {
          return handleNativeMouseDown(evt);
        }

        @Override
        protected void onMouseMove(MouseEvent evt) {
          handleNativeMouseMove(evt);
        }

        @Override
        protected void onMouseUp(MouseEvent evt) {
          handleNativeMouseUp(evt);
        }
      };

  private boolean hasDragInThisGesture;
  private int numberOfClicks;
  private final Timer resetClickStateTimer =
      new Timer() {
        @Override
        public void run() {
          resetClickState();
        }
      };

  private MouseGestureListener(Callback callback) {
    this.callback = callback;
  }

  private boolean handleNativeMouseDown(MouseEvent event) {
    numberOfClicks++;

    if (!callback.onClick(numberOfClicks, event)) {
      resetClickState();
      return false;
    }

    /*
     * If the user does not click again within this timeout, we will revert
     * back to a clean state
     */
    resetClickStateTimer.schedule(MAX_CLICK_TIMEOUT_MS);

    return true;
  }

  private void handleNativeMouseMove(MouseEvent event) {
    if (!hasDragInThisGesture) {
      // Dragging the mouse resets the click state
      resetClickState();
      hasDragInThisGesture = true;
    }

    callback.onDrag(event);
  }

  private void handleNativeMouseUp(MouseEvent event) {
    if (hasDragInThisGesture) {
      callback.onDragRelease(event);
      hasDragInThisGesture = false;
    }
  }

  private void resetClickState() {
    numberOfClicks = 0;
    resetClickStateTimer.cancel();
  }
}

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

import elemental.events.MouseEvent;
import elemental.util.Timer;

/**
 * A helper that detects mouse movement pause.
 * <p/>
 * When mouse movement pause is detected, {@link Callback#onMouseMovePaused()}
 * is invoked once. Even if mouse stays still for a long time,
 * {@link Callback#onMouseMovePaused()} is only invoked once. Only after mouse
 * starts to move again, the detector resumes.
 */
public final class MouseMovePauseDetector {

    /**
     * An interface that receives callback from the {@link MouseMovePauseDetector}
     * when mouse move pause occur.
     */
    public interface Callback {
        void onMouseMovePaused();
    }

    private final Callback callback;
    private final Timer    getRevisionWhenDragTimer;
    /**
     * If mouse did not move after DEFAULT_PAUSE_DURATION_MS, we consider move
     * paused.
     */
    private static final int DEFAULT_PAUSE_DURATION_MS        = 500;
    private static final int MOUSE_INVALID_POSITION           = Integer.MIN_VALUE;
    private static final int MOUSE_MOVEMENT_THRESHOLD_SQUARED = 900;
    // (lastMouseX, lastMouseY) is the last mouse location.
    // It gets reset if mouse movement is over the threshold.
    private              int lastMouseX                       = MOUSE_INVALID_POSITION;
    private              int lastMouseY                       = MOUSE_INVALID_POSITION;

    private boolean enabled;

    public MouseMovePauseDetector(Callback callback) {
        this.callback = callback;
        getRevisionWhenDragTimer = new Timer() {
            @Override
            public void run() {
                mousePaused();
            }
        };
    }

    public void start() {
        enabled = true;
        lastMouseX = lastMouseY = MOUSE_INVALID_POSITION;
    }

    public void stop() {
        enabled = false;
        getRevisionWhenDragTimer.cancel();
    }

    public void handleMouseMove(MouseEvent event) {
        if (!enabled) {
            return;
        }

        if (lastMouseX == MOUSE_INVALID_POSITION
            || isMoved(event.getClientX(), event.getClientY(), lastMouseX, lastMouseY)) {
            // Cancel current timer and start timeout.
            // Whenever the timer timeouts, mouse has paused long enough.
            getRevisionWhenDragTimer.cancel();
            getRevisionWhenDragTimer.schedule(DEFAULT_PAUSE_DURATION_MS);

            lastMouseX = event.getClientX();
            lastMouseY = event.getClientY();
        }
    }

    private void mousePaused() {
        if (!enabled) {
            return;
        }
        callback.onMouseMovePaused();
    }

    private boolean isMoved(int x1, int y1, int x2, int y2) {
        int deltaX = x1 - x2;
        int deltaY = y1 - y2;

        return deltaX * deltaX + deltaY * deltaY > MOUSE_MOVEMENT_THRESHOLD_SQUARED;
    }

}

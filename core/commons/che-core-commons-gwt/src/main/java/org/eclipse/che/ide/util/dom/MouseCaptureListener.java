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

import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MouseEvent;

/**
 * Listener that can be used for event capture. Simply add this as a
 * {@link Event#MOUSEDOWN} listener and then you will get mouse capture for
 * moves and mouse up for that element.
 */
public abstract class MouseCaptureListener implements EventListener {

    private int prevClientX;
    private int prevClientY;

    private int deltaX;
    private int deltaY;

    private CaptureReleaser releaser;

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    @Override
    public void handleEvent(Event evt) {
        MouseEvent mouseEvent = (MouseEvent)evt;
        updateXYState(mouseEvent);

        if (evt.getType().equals(Event.MOUSEMOVE)) {
            onMouseMove(mouseEvent);
        } else if (evt.getType().equals(Event.MOUSEUP)) {
            release();
            onMouseUp(mouseEvent);
        } else if (evt.getType().equals(Event.MOUSEDOWN)) {
            if (onMouseDown(mouseEvent)) {
                // Start the capture
                MouseEventCapture.capture(this);
                mouseEvent.preventDefault();
            }
        }
    }

    public void release() {
        if (releaser != null) {
            releaser.release();
        }
    }

    /**
     * Called when the mousedown event is received.
     *
     * @return true if the capture should be initiated for mousemove and mouseup
     *         events.
     */
    protected boolean onMouseDown(MouseEvent evt) {
        return true;
    }

    protected void onMouseMove(MouseEvent evt) {
    }

    protected void onMouseUp(MouseEvent evt) {
    }

    void setCaptureReleaser(CaptureReleaser releaser) {
        this.releaser = releaser;
    }

    private void updateXYState(MouseEvent evt) {
        int x = evt.getClientX();
        int y = evt.getClientY();

        if (evt.getType().equals(Event.MOUSEDOWN)) {
            deltaX = deltaY = 0;
        } else {
            deltaX = x - prevClientX;
            deltaY = y - prevClientY;
        }

        prevClientX = x;
        prevClientY = y;
    }
}

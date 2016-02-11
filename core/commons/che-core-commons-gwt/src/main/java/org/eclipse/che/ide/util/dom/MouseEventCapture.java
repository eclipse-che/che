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

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventTarget;

import java.util.ArrayList;
import java.util.List;

/** Utility class which allows simulation of Event Capture. */
public class MouseEventCapture {
    private interface Remover {
        public void remove();
    }

    /** Current mouse capture owner. */
    private static MouseCaptureListener captureOwner;

    /**
     * These are the Remover objects for the hooks into Window that fire to this
     * class. We have exactly one listener per event type that supports capture.
     * When no UI component possesses capture (meaning that the captureOwner stack
     * is empty) we can disconnect these.
     */
    private static final List<Remover> mouseRemovers = new ArrayList<>();

    /**
     * Capture happens on a per listener instance basis. We throw the
     * CaptureListener on the top of the capture stack and then provide a handle
     * to an object that can be used to
     *
     * @param listener
     */
    public static void capture(final MouseCaptureListener listener) {

        // Make sure to release the previous capture owner.
        if (captureOwner != null) {
            captureOwner.release();
        }

        // Lazily initialize event hookups (this should be below the release above
        // since the above in turn clears the capture hookups)
        if (mouseRemovers.isEmpty()) {
            registerEventCaptureHookups();
        }

        captureOwner = listener;
        listener.setCaptureReleaser(new CaptureReleaser() {
            @Override
            public void release() {
                // nuke the reference to this releaser in the listener (which should
                // still be the capture owner).
                listener.setCaptureReleaser(null);
                // nuke the captureOwner
                captureOwner = null;

                // Release the event listeners.
                for (int i = 0, n = mouseRemovers.size(); i < n; i++) {
                    mouseRemovers.get(i).remove();
                }
                mouseRemovers.clear();
            }
        });
    }

    private static Remover addCaptureEventListener(final String type, final EventTarget source,
                                                   final EventListener listener) {
        source.addEventListener(type, listener, true);
        return new Remover() {
            @Override
            public void remove() {
                source.removeEventListener(type, listener, true);
            }
        };
    }

    private static void forwardToCaptureOwner(Event event) {
        if (captureOwner != null) {
            captureOwner.handleEvent(event);
        }
    }

    /**
     * Registers for relevant events on the Window.
     * <p/>
     * Capture should be lazily initialized, and then destroyed each time nothing
     * has capture (as to not cause useless event dispatch and handling for events
     * like move events).
     */
    private static void registerEventCaptureHookups() {

        mouseRemovers.add(addCaptureEventListener(Event.MOUSEMOVE, Browser.getWindow(), new EventListener() {
            @Override
            public void handleEvent(Event event) {
                forwardToCaptureOwner(event);
            }
        }));

        mouseRemovers.add(addCaptureEventListener(Event.MOUSEUP, Browser.getWindow(), new EventListener() {
            @Override
            public void handleEvent(Event event) {
                forwardToCaptureOwner(event);
            }
        }));

        mouseRemovers.add(addCaptureEventListener(Event.BLUR, Browser.getWindow(), new EventListener() {
            @Override
            public void handleEvent(Event event) {
                captureOwner.release();
            }
        }));
    }
}

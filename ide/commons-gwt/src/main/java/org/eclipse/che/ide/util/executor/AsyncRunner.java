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

package org.eclipse.che.ide.util.executor;

import com.google.gwt.core.client.JavaScriptObject;
import elemental.dom.Document.Events;
import elemental.events.EventListener;
import elemental.js.events.JsEvent;
import org.eclipse.che.ide.util.dom.Elements;

/*
 * TODO: Make a scheduler so there's only one event listener for
 * ALL things that want to asyncrun
 */

/** An utility class to execute some logic asynchronously as soon as possible. */
public abstract class AsyncRunner implements Runnable {

  private static final String EVENT_MESSAGE = "message";

  private static class MessageEvent extends JsEvent implements Events {
    protected MessageEvent() {}

    public final native Object getData() /*-{
            return this.data;
        }-*/;

    public final native JavaScriptObject getSource() /*-{
            return this.source;
        }-*/;
  }

  private static int instanceId = 0;

  private final String messageName = "test" + ":AsyncRunner." + instanceId++;
  private final String targetOrigin =
      Elements.getDocument().getLocation().getProtocol()
          + "//"
          + Elements.getDocument().getLocation().getHost();

  private boolean isCancelled;
  private boolean isAttached = false;

  private EventListener messageHandler =
      new EventListener() {
        @Override
        public void handleEvent(elemental.events.Event rawEvent) {
          MessageEvent event = (MessageEvent) rawEvent;
          if (!isCancelled && event.getData().equals(messageName)) {
            detachMessageHandler();
            event.stopPropagation();
            run();
          }
        }
      };

  public AsyncRunner() {}

  public void cancel() {
    isCancelled = true;
    detachMessageHandler();
  }

  public void schedule() {
    isCancelled = false;
    attachMessageHandler();
    scheduleJs();
  }

  private void attachMessageHandler() {
    if (!isAttached) {
      Elements.getWindow().addEventListener(EVENT_MESSAGE, messageHandler, true);
      isAttached = true;
    }
  }

  private void detachMessageHandler() {
    if (isAttached) {
      Elements.getWindow().removeEventListener(EVENT_MESSAGE, messageHandler, true);
      isAttached = false;
    }
  }

  private native void scheduleJs() /*-{
        // This is more responsive than setTimeout(0)
        $wnd.postMessage(this.@org.eclipse.che.ide.util.executor.AsyncRunner::messageName, this.
            @org.eclipse.che.ide.util.executor.AsyncRunner::targetOrigin);
    }-*/;
}

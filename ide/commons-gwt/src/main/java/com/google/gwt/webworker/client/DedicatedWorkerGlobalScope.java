/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.webworker.client;

import com.google.gwt.core.client.JsArray;

/**
 * The Dedicated worker has these methods on the top level object.
 *
 * <p>Runs inside the webworker, so this class cannot assume that Window or Document exists.
 *
 * <p>http://www.whatwg.org/specs/web-workers/current-work/
 */
public class DedicatedWorkerGlobalScope extends WorkerGlobalScope {

  public static native DedicatedWorkerGlobalScope get() /*-{
    return $self;
  }-*/;

  protected DedicatedWorkerGlobalScope() {
    // Constructors must be protected in JavaScriptObject overlays.
  };

  public final native void postMessage(double message) /*-{
    this.postMessage(message);
  }-*/;

  public final native void postMessage(double message, JsArray<MessagePort> ports) /*-{
    this.postMessage(message, ports);
  }-*/;

  public final native void postMessage(String message) /*-{
    this.postMessage(message);
  }-*/;

  public final native void postMessage(String message, JsArray<MessagePort> ports) /*-{
    this.postMessage(message, ports);
  }-*/;

  public final native void setOnMessage(MessageHandler messageHandler) /*-{
    this.onmessage = function(event) {
      messageHandler.@com.google.gwt.webworker.client.MessageHandler::onMessage(Lcom/google/gwt/webworker/client/MessageEvent;)(event);
    }
  }-*/;

  public final native void terminate() /*-{
    this.terminate();
  }-*/;
}

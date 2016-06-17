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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * 
 */
public class MessagePort extends JavaScriptObject {

  /**
   * Takes care of reporting exceptions to the console in hosted mode.
   * 
   * @param listener the listener object to call back.
   * @param port argument from the callback.
   */
  private static void onMessageImpl(MessageHandler messageHandler,
      MessageEvent event) {
    UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
    if (ueh != null) {
      try {
        messageHandler.onMessage(event);
      } catch (Exception ex) {
        ueh.onUncaughtException(ex);
      }
    } else {
      messageHandler.onMessage(event);
    }
  }

  protected MessagePort() {
    // Constructors must be protected in JavaScriptObject overlays.
  }

  public final native void close() /*-{
    this.close();
  }-*/;

  public final native void postMessage(double message) /*-{
    this.postMessage(message);
  }-*/;

  public final native void postMessage(double message,
      JsArray<MessagePort> ports) /*-{
    this.postMessage(message, ports);
  }-*/;

  public final native void postMessage(String message) /*-{
    this.postMessage(message);
  }-*/;

  public final native void postMessage(String message,
      JsArray<MessagePort> ports) /*-{
    this.postMessage(message, ports);
  }-*/;

  public final native void setOnMessage(MessageHandler messageHandler) /*-{
    this.onmessage = function(event) {
      @com.google.gwt.webworker.client.MessagePort::onMessageImpl(Lcom/google/gwt/webworker/client/MessageHandler;Lcom/google/gwt/webworker/client/MessageEvent;)(handler,event);
    }
  }-*/;

  public final native void start() /*-{
    this.start();
  }-*/;

  public final native void terminate() /*-{
    this.terminate();
  }-*/;
}

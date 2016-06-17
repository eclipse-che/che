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

/**
 * Base class used for Dedicated Workers and Shared Workers.
 * http://www.whatwg.org/specs/web-workers/current-work/
 */
public class AbstractWorker extends JavaScriptObject {

  /**
   * Takes care of reporting exceptions to the console in hosted mode.
   * 
   * @param listener the listener object to call back.
   * @param port argument from the callback.
   */
  private static void onErrorImpl(ErrorHandler errorHandler, ErrorEvent event) {
    UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
    if (ueh != null) {
      try {
        errorHandler.onError(event);
      } catch (Exception ex) {
        ueh.onUncaughtException(ex);
      }
    } else {
      errorHandler.onError(event);
    }
  }

  protected AbstractWorker() {
    // constructors must be protected in JavaScriptObject overlays.
  }

  /**
   * A handler that will be called if the worker encounters an error. Replaces
   * any existing handler.
   * 
   * @param handler handler to set when a worker encounters an error.
   */
  public final native void setOnError(ErrorHandler handler) /*-{
    this.onerror = function(event) {
      @com.google.gwt.webworker.client.AbstractWorker::onErrorImpl(Lcom/google/gwt/webworker/client/ErrorHandler;Lcom/google/gwt/webworker/client/ErrorEvent;)(handler, event);
    }
  }-*/;

}

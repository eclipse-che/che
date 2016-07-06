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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Represents the top level object for a Web Worker.
 * 
 * Runs inside the webworker, so this class cannot assume that Window or
 * Document exists.
 * 
 * http://www.whatwg.org/specs/web-workers/current-work/
 */
public class WorkerGlobalScope extends JavaScriptObject {
  protected WorkerGlobalScope() {
    // protected constructor required for JavaScriptObject overlay.
  }

  public final native void close() /*-{
    this.close();
  }-*/;

  public final native WorkerLocation getLocation() /*-{
    return this.location;
  }-*/;

  public final native void importScript(String url) /*-{
    this.importScripts([url]);
  }-*/;

  /**
   * Caveat!! If this array has more than one entry, importscript may not work.
   * It should eventually.
   * 
   * @param urls
   */
  public final native void importScripts(JsArrayString urls) /*-{
    this.importScripts(urls);
  }-*/;

  public final void importScripts(String[] urls) {
    JsArrayString jsUrls = JsArrayString.createArray().cast();
    for (int i = 0, l = urls.length; i < l; ++i) {
      jsUrls.set(i, urls[i]);
    }
    importScripts(jsUrls);
  }

  public final native WorkerGlobalScope self() /*-{
    return self;
  }-*/;

  /**
   * A handler that will be called if the worker encounters an error. Replaces
   * any existing handler.
   * 
   * @param handler handler to set when a worker encounters an error.
   */
  // TODO(zundel): use UncaughtExceptionHandler... chain more than one handler?
  // May not be needed until hosted mode can support webworkers.
  public final native void setOnError(ErrorHandler handler) /*-{
    this.onerror = function(event) {
      handler.@com.google.gwt.webworker.client.ErrorHandler::onError(Lcom/google/gwt/webworker/client/ErrorEvent;)(event);
    }
  }-*/;
}

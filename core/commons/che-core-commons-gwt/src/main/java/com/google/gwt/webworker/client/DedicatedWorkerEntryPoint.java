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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;

/**
 * Entry point class for implementing a DedicatedWorker.
 * 
 * Runs inside the webworker, so this class cannot assume that Window or
 * Document exists.
 * 
 * http://www.whatwg.org/specs/web-workers/current-work/
 */
public abstract class DedicatedWorkerEntryPoint implements EntryPoint {
  private DedicatedWorkerGlobalScope scope;

  public final void close() {
    getGlobalScope().close();
  }

  public final WorkerLocation getLocation() {
    return getGlobalScope().getLocation();
  }

  public final void importScript(String url) {
    getGlobalScope().importScript(url);
  }

  public final void onModuleLoad() {
    scope = DedicatedWorkerGlobalScope.get();
    onWorkerLoad();
  }

  public abstract void onWorkerLoad();

  protected DedicatedWorkerGlobalScope getGlobalScope() {
    return scope;
  }

  protected final void postMessage(double message) {
    getGlobalScope().postMessage(message);
  }

  protected final void postMessage(double message, JsArray<MessagePort> ports) {
    getGlobalScope().postMessage(message, ports);
  }

  protected final void postMessage(String message) {
    getGlobalScope().postMessage(message);
  }

  protected final void postMessage(String message, JsArray<MessagePort> ports) {
    getGlobalScope().postMessage(message, ports);
  }

  protected final void setOnMessage(MessageHandler messageHandler) {
    getGlobalScope().setOnMessage(messageHandler);
  }

  protected final void terminate() {
    getGlobalScope().terminate();
  }
}

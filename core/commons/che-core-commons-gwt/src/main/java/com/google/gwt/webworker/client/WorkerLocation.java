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

/**
 * A WorkerLocation object represents an absolute URL set at its creation.
 * 
 * Runs inside the webworker, so this class cannot assume that Window or
 * Document exists.
 * 
 * http://www.whatwg.org/specs/web-workers/current-work/
 */
public class WorkerLocation extends JavaScriptObject {

  protected WorkerLocation() {
    // protected constructor required for JavaScriptObject overlay.
  }

  public final native String getHash() /*-{
    return this.hash;
  }-*/;

  public final native String getHost() /*-{
    return this.host;
  }-*/;

  public final native String getHostname() /*-{
    return this.hostname;
  }-*/;

  public final native String getHref() /*-{
    return this.href;
  }-*/;

  public final native String getPathname() /*-{
    return this.pathname;
  }-*/;

  public final native String getPort() /*-{
    return this.port;
  }-*/;

  public final native String getProtocol() /*-{
    return this.protocol;
  }-*/;

  public final native String getSearch() /*-{
    return this.search;
  }-*/;
}

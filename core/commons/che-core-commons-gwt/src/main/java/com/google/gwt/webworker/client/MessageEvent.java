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
import com.google.gwt.core.client.JsArray;

/**
 * General HTML 5 Message event.
 */
public class MessageEvent extends JavaScriptObject {
  protected MessageEvent() {
    // required protected constructor for JavaScriptObject
  }

  public final native JavaScriptObject getDataAsJSO() /*-{
    return JSON.parse(this.data);
  }-*/;

  public final native double getDataAsNumber() /*-{
    return this.data;
  }-*/;

  public final native String getDataAsString() /*-{
    return this.data;
  }-*/;

  public final native String getLastEventId() /*-{
    return this.lastEventId;
  }-*/;

  public final native String getOrigin() /*-{
    return this.origin;
  }-*/;

  public final native JsArray<MessagePort> getPorts() /*-{
    return this.ports;
  }-*/;

  public final native String getSource() /*-{
    return this.source;
  }-*/;
}

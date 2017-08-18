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

package com.google.gwt.webworker.client.messages;

import org.eclipse.che.ide.collections.Jso;

/** Client side base class for all DTO payload implementations. */
public abstract class MessageImpl extends Jso implements Message {

  // To work around devmode bug where static field references on the interface
  // implemented by this SingleJsoImpl, blow up.
  private static final String TYPE_FIELD = Message.TYPE_FIELD;

  private static final int NON_ROUTABLE_TYPE = Message.NON_ROUTABLE_TYPE;

  protected MessageImpl() {}

  /** @return the type of the JsonMessage so the client knows how to route it. */
  public final native int getType() /*-{
        return this[@com.google.gwt.webworker.client.messages.MessageImpl::TYPE_FIELD] ||
            @com.google.gwt.webworker.client.messages.MessageImpl::NON_ROUTABLE_TYPE;
    }-*/;
}

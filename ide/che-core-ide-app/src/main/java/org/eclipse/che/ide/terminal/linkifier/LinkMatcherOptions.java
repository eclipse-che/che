/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.terminal.linkifier;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * This class represents LinkMatcher Options for terminal "linkify" feature. If terminal find some
 * content by regexp pattern, terminal can transformate dom of this content to clickable link (dom
 * content wraps by anchor tag and on this tags applies click handler) . When you click to this link
 * terminal do logic from {@link LinkMatcherHandler}
 *
 * @author Alexander Andrienko
 */
@JsType(namespace = JsPackage.GLOBAL)
public class LinkMatcherOptions {

  /**
   * MatchIndex defines what part of the content became clickable link. MatchIndex is the number of
   * the group in the linkifier regexp. For example we want find in the terminal lines text "Hello
   * World" and transformate "World" to clickable link. In this case we can use such regexp:
   * ".*(Hello)\\s(World).*". "World" is second group in the regexp, so match index is 2. If we want
   * to make "Hello" clickable link we should set up match index 1.
   *
   * @param matchIndex
   */
  @JsProperty(name = "matchIndex")
  public native void setMatchIndex(int matchIndex);

  /**
   * Priority in the search. By default is always 0; If we want to set higher priority this field
   * should be bigger than 0;
   *
   * @param priority search priority;
   */
  @JsProperty(name = "priority")
  public native void setPriority(int priority);
}

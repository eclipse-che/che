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
package org.eclipse.che.ide.terminal;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * This class represents link Matcher for terminal "linkify" feature. If terminal find some content
 * by regexp pattern, terminal can transformate this content to clickable link. When you click to
 * this link terminal do logic from {@link LinkMatcherHandler}
 *
 * @author Alexander Andrienko
 */
@JsType(namespace = JsPackage.GLOBAL)
public class LinkMatcherOptions {

  @JsProperty(name = "matchIndex")
  public native void setMatchIndex(int matchIndex);

  @JsProperty(name = "priority")
  public native void setPriority(int priority);
}

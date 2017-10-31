/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.plugin.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/** Entry point classes define <code>onModuleLoad()</code> */
public class CheHelloJSPlugin implements EntryPoint {

  /** This is the entry point method. */
  @Override
  public void onModuleLoad() {
    PluginInjector injector = GWT.create(PluginInjector.class);
    injector.getPluginReady();
  }
}

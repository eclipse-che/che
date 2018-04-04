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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for Orion editor options.
 *
 * @author Alexander Andrienko
 */
public class OrionEditorOptionsOverlay extends JavaScriptObject {

  protected OrionEditorOptionsOverlay() {}

  /** Returns status reporter function. This function handles editor status messages. */
  public final native JavaScriptObject getStatusReporter() /*-{
        return this.statusReporter;
    }-*/;

  /**
   * Set status reporter function.
   *
   * @param statusReporter status reporter function.
   */
  public final native void setStatusReporter(JavaScriptObject statusReporter) /*-{
        this.statusReporter = statusReporter;
    }-*/;
}

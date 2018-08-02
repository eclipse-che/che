/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

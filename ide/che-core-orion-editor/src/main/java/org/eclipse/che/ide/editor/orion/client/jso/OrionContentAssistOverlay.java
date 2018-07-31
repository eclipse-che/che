/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript overlay over Orion ContentAssist object.
 *
 * @author Artem Zatsarynnyi
 */
public class OrionContentAssistOverlay extends JavaScriptObject {

  protected OrionContentAssistOverlay() {}

  /** Checks whether the content assist is active or not. */
  public final native boolean isActive() /*-{
        return this.isActive();
    }-*/;

  /** Activates the content assist. */
  public final native void activate() /*-{
        this.activate();
    }-*/;

  /** Deactivates the content assist. */
  public final native void deactivate() /*-{
        this.deactivate();
    }-*/;
}

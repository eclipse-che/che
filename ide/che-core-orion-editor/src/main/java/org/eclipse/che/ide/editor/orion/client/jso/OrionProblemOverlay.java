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

/** @author Evgen Vidolob */
public class OrionProblemOverlay extends JavaScriptObject {
  protected OrionProblemOverlay() {}

  public final native void setDescription(String description) /*-{
        this["description"] = description;
    }-*/;

  public final native void setId(String id) /*-{
        this["id"] = id;
    }-*/;

  public final native void setStart(int offset) /*-{
        this["start"]= offset;
    }-*/;

  public final native void setEnd(int offset) /*-{
        this["end"]= offset;
    }-*/;

  public final native void setSeverity(String severity) /*-{
        this["severity"]= severity;
    }-*/;
}

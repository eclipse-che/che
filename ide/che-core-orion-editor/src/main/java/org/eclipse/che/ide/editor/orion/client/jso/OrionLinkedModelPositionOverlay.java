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
public class OrionLinkedModelPositionOverlay extends JavaScriptObject {
  protected OrionLinkedModelPositionOverlay() {}

  public final native void setOffset(int offset) /*-{
        this.offset = offset;
    }-*/;

  public final native void setLength(int length) /*-{
        this.length = length;
    }-*/;
}

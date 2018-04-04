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
import com.google.gwt.core.client.JsArrayString;

/**
 * The 'Hover Context Object' for Orion hover See <a
 * href="https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#orion.edit.hover">Orion
 * Hover</a>
 *
 * @author Evgen Vidolob
 */
public class OrionHoverContextOverlay extends JavaScriptObject {
  protected OrionHoverContextOverlay() {}

  /**
   * @return An array of strings representing the annotations at the given offset. This will change
   *     to being a unique identifier for a particular annotation once these have been defined.
   *     Available when hovering over an annotation (in the ruler or the text).
   */
  public final native JsArrayString getAnnotations() /*-{
        return this.annotations;
    }-*/;

  /**
   * @return The offset within the file of the cursor's current position when. Available when
   *     hovering over text.
   */
  public final native int getOffset() /*-{
        return this.offset;
    }-*/;
}

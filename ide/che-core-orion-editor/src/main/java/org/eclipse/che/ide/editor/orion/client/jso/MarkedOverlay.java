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

/**
 * This class gives access to the marked.js module from orion editor.
 *
 * @author Thomas Mäder
 */

import com.google.gwt.core.client.JavaScriptObject;
import java.util.function.Consumer;

public class MarkedOverlay extends JavaScriptObject {

  private static MarkedOverlay INSTANCE;

  static {
    create(
        marked -> {
          INSTANCE = marked;
        });
  }

  public static MarkedOverlay getInstance() {
    return INSTANCE;
  }

  protected MarkedOverlay() {}

  private static native void create(Consumer<MarkedOverlay> callback) /*-{
        $wnd.require(['marked/marked'], function (marked) {
            var m = {};
            m.marked= marked;
            callback.@java.util.function.Consumer::accept(*)(m);
        });
    }-*/;

  /**
   * Converts the given markdown to HTML
   * 
   * @param markdown marked string
   * @return the html version of the same content
   */
  public final native String toHTML(String markdown) /*-{
        return this.marked(markdown, {
                sanitize: true
            });
    }-*/;
}

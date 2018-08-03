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
import java.util.function.Consumer;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;

/**
 * This class gives access to the marked.js module from orion editor.
 *
 * @author Thomas Mäder
 */
public class MarkedOverlay extends JavaScriptObject {

  private static Promise<MarkedOverlay> markedPromise = create();

  protected MarkedOverlay() {}

  public static Promise<MarkedOverlay> getMarkedPromise() {
    return markedPromise;
  }

  private static Promise<MarkedOverlay> create() {
    return Promises.create(
        (resolve, reject) -> {
          create(
              (marked) -> {
                resolve.apply(marked);
              });
        });
  }

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

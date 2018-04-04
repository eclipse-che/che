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
import com.google.gwt.dom.client.Element;
import org.eclipse.che.api.promises.client.Promise;

/**
 * JavaScript overlay over Orion CodeEdit object.
 *
 * @author Artem Zatsarynnyi
 */
public class OrionCodeEditWidgetOverlay extends JavaScriptObject {

  protected OrionCodeEditWidgetOverlay() {}

  /**
   * Creates an Orion CodeEdit widget instance.
   *
   * @param plugins array of URLs of plugins that should be installed before CodeEdit widget
   *     initialization
   */
  public final native OrionCodeEditWidgetOverlay create(JsArrayString plugins) /*-{
        return new this({userPlugins: plugins});
    }-*/;

  /**
   * Creates an Orion EditorView instance.
   *
   * @param element the element backing the editor view
   * @param options the editor view options
   * @return an editor view instance
   */
  public final native Promise<OrionEditorViewOverlay> createEditorView(
      final Element element, final JavaScriptObject options) /*-{
        options.parent = element;
        return this.create(options);
    }-*/;

  /**
   * Provides Access to Orion's service registry.
   *
   * @return the service registry
   */
  public final native OrionServiceRegistryOverlay getServiceRegistry() /*-{
        return this.serviceRegistry;
    }-*/;
}

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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistryOverlay;

/** @author Evgen Vidolob */
@Singleton
public class OrionHoverRegistrant {

  private final Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider;
  private final EditorInitializePromiseHolder editorModule;

  @Inject
  public OrionHoverRegistrant(
      Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider,
      EditorInitializePromiseHolder editorModule) {
    this.codeEditWidgetProvider = codeEditWidgetProvider;
    this.editorModule = editorModule;
  }

  public void registerHover(final JsArrayString contentTypes, final OrionHoverHandler handler) {
    editorModule
        .getInitializerPromise()
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void arg) throws OperationException {
                registerHover(
                    codeEditWidgetProvider.get().getServiceRegistry(), contentTypes, handler);
              }
            });
  }

  private final native void registerHover(
      OrionServiceRegistryOverlay serviceRegistry,
      JsArrayString contentTypes,
      OrionHoverHandler handler) /*-{
        serviceRegistry.registerService("orion.edit.hover", {
            computeHoverInfo: function (editorContext, context) {
                return handler.@OrionHoverHandler::computeHover(*)(context);
            }
        }, {
            name: "Hover",
            contentType: contentTypes
        });
    }-*/;
}

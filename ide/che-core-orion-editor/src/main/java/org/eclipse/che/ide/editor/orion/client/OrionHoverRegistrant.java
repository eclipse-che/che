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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistrationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistryOverlay;

/** @author Evgen Vidolob */
@Singleton
public class OrionHoverRegistrant {

  private final Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider;
  private final EditorInitializePromiseHolder editorModule;
  private OrionServiceRegistrationOverlay hoverRegistration = null;

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
            ignored -> {
              if (null != hoverRegistration) {
                hoverRegistration.doUnregister();
                hoverRegistration = null;
              }

              hoverRegistration =
                  registerHover(
                      codeEditWidgetProvider.get().getServiceRegistry(), contentTypes, handler);
            });
  }

  private final native OrionServiceRegistrationOverlay registerHover(
      OrionServiceRegistryOverlay serviceRegistry,
      JsArrayString contentTypes,
      OrionHoverHandler handler) /*-{
        return serviceRegistry.registerService("orion.edit.hover", {
            computeHoverInfo: function (editorContext, context) {
                return handler.@OrionHoverHandler::computeHover(*)(context);
            }
        }, {
            name: "Hover",
            contentType: contentTypes
        });
    }-*/;
}

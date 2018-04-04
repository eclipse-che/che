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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.client.JsArrayString;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistryOverlay;

/** @author Xavier Coulon,Red Hat */
@Singleton
public class OrionOccurrencesRegistrant {

  private final Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider;
  private final EditorInitializePromiseHolder editorModule;

  @Inject
  public OrionOccurrencesRegistrant(
      Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider,
      EditorInitializePromiseHolder editorModule) {
    this.codeEditWidgetProvider = codeEditWidgetProvider;
    this.editorModule = editorModule;
  }

  public void registerOccurrencesHandler(
      final JsArrayString contentTypes, final OrionOccurrencesHandler handler) {
    editorModule
        .getInitializerPromise()
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void arg) throws OperationException {
                registerOccurrencesHandler(
                    codeEditWidgetProvider.get().getServiceRegistry(), contentTypes, handler);
              }
            });
  }

  private final native void registerOccurrencesHandler(
      OrionServiceRegistryOverlay serviceRegistry,
      JsArrayString contentTypes,
      OrionOccurrencesHandler handler) /*-{
        serviceRegistry.registerService("orion.edit.occurrences", {
            computeOccurrences: function(editorContext, context) {
           		return handler.@OrionOccurrencesHandler::computeOccurrences(*)(context);
       		}
        }, {
            name: "Occurrences",
            contentType: contentTypes
        });
    }-*/;
}

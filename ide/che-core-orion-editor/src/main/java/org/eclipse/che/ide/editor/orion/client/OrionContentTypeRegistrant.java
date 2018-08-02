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

import static com.google.common.collect.Lists.newArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.editor.filetype.MultipleMethodFileIdentifier;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionContentTypeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHighlightingConfigurationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistryOverlay;

/**
 * Component to register new content types and corresponding highlighting configuration.
 *
 * @author Sven Efftinge (typefox.io)
 */
public class OrionContentTypeRegistrant {

  final MultipleMethodFileIdentifier fileTypeIdentifier;
  final Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider;
  final EditorInitializePromiseHolder editorModule;

  @Inject
  public OrionContentTypeRegistrant(
      final MultipleMethodFileIdentifier fileTypeIdentifier,
      final Provider<OrionCodeEditWidgetOverlay> codeEditWidgetProvider,
      final EditorInitializePromiseHolder editorModule) {
    this.fileTypeIdentifier = fileTypeIdentifier;
    this.codeEditWidgetProvider = codeEditWidgetProvider;
    this.editorModule = editorModule;
  }

  public void registerFileType(
      final OrionContentTypeOverlay contentType,
      final OrionHighlightingConfigurationOverlay config) {
    // register content type and configure orion
    JsArrayString extensions = contentType.getExtensions();
    for (int i = 0; i < extensions.length(); i++) {
      String extension = extensions.get(i);
      fileTypeIdentifier.registerNewExtension(extension, newArrayList(contentType.getId()));
    }
    editorModule
        .getInitializerPromise()
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void arg) throws OperationException {
                OrionServiceRegistryOverlay serviceRegistry =
                    codeEditWidgetProvider.get().getServiceRegistry();
                serviceRegistry.doRegisterService(
                    "orion.core.contenttype",
                    JavaScriptObject.createObject(),
                    contentType.toServiceObject());
                if (config.getPatterns() != null && config.getPatterns().length() != 0) {
                  serviceRegistry.doRegisterService(
                      "orion.edit.highlighter", JavaScriptObject.createObject(), config);
                }
              }
            });
  }
}

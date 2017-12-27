/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.editor.orion.client.OrionContentTypeRegistrant;
import org.eclipse.che.ide.editor.orion.client.OrionHoverRegistrant;
import org.eclipse.che.ide.editor.orion.client.OrionOccurrencesRegistrant;
import org.eclipse.che.ide.editor.orion.client.jso.OrionContentTypeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHighlightingConfigurationOverlay;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.highlighting.OccurrencesProvider;
import org.eclipse.che.plugin.languageserver.ide.hover.HoverProvider;
import org.eclipse.che.plugin.languageserver.ide.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerRegistryServiceClient;

/** @author Evgen Vidolob */
@Singleton
public class LanguageServerFileTypeRegister {

  private final LanguageServerRegistryServiceClient serverLanguageRegistry;
  private final LanguageServerRegistry lsRegistry;
  private final LanguageServerResources resources;
  private final EditorRegistry editorRegistry;
  private final OrionContentTypeRegistrant contentTypeRegistrant;
  private final OrionHoverRegistrant orionHoverRegistrant;
  private final OrionOccurrencesRegistrant orionOccurrencesRegistrant;
  private final LanguageServerEditorProvider editorProvider;
  private final HoverProvider hoverProvider;
  private final OccurrencesProvider occurrencesProvider;

  @Inject
  public LanguageServerFileTypeRegister(
      LanguageServerRegistryServiceClient serverLanguageRegistry,
      LanguageServerRegistry lsRegistry,
      LanguageServerResources resources,
      EditorRegistry editorRegistry,
      OrionContentTypeRegistrant contentTypeRegistrant,
      OrionHoverRegistrant orionHoverRegistrant,
      OrionOccurrencesRegistrant orionOccurrencesRegistrant,
      LanguageServerEditorProvider editorProvider,
      HoverProvider hoverProvider,
      OccurrencesProvider occurrencesProvider) {
    this.serverLanguageRegistry = serverLanguageRegistry;
    this.lsRegistry = lsRegistry;
    this.resources = resources;
    this.editorRegistry = editorRegistry;
    this.contentTypeRegistrant = contentTypeRegistrant;
    this.orionHoverRegistrant = orionHoverRegistrant;
    this.orionOccurrencesRegistrant = orionOccurrencesRegistrant;
    this.editorProvider = editorProvider;
    this.hoverProvider = hoverProvider;
    this.occurrencesProvider = occurrencesProvider;
  }

  void start() {
    Promise<List<LanguageDescription>> registeredLanguages =
        serverLanguageRegistry.getSupportedLanguages();
    registeredLanguages
        .then(
            new Operation<List<LanguageDescription>>() {
              @Override
              public void apply(List<LanguageDescription> langs) throws OperationException {
                if (!langs.isEmpty()) {
                  JsArrayString contentTypes = JsArrayString.createArray().cast();
                  for (LanguageDescription lang : langs) {
                    for (String ext : lang.getFileExtensions()) {
                      final FileType fileType = new FileType(resources.file(), ext);
                      lsRegistry.registerFileType(fileType, lang);
                      editorRegistry.registerDefaultEditor(fileType, editorProvider);
                    }
                    for (String fileName : lang.getFileNames()) {
                      final FileType fileType =
                          new FileType(resources.file(), null, RegExp.quote(fileName));
                      lsRegistry.registerFileType(fileType, lang);
                      editorRegistry.registerDefaultEditor(fileType, editorProvider);
                    }
                    String mimeType = lang.getMimeType();
                    contentTypes.push(mimeType);
                    OrionContentTypeOverlay contentType = OrionContentTypeOverlay.create();
                    contentType.setId(mimeType);
                    contentType.setName(lang.getLanguageId());
                    contentType.setFileName(
                        lang.getFileNames().toArray(new String[lang.getFileNames().size()]));
                    contentType.setExtension(
                        lang.getFileExtensions()
                            .toArray(new String[lang.getFileExtensions().size()]));
                    contentType.setExtends("text/plain");

                    // highlighting
                    OrionHighlightingConfigurationOverlay config =
                        OrionHighlightingConfigurationOverlay.create();
                    config.setId(lang.getLanguageId() + ".highlighting");
                    config.setContentTypes(mimeType);
                    config.setPatterns(lang.getHighlightingConfiguration());
                    contentTypeRegistrant.registerFileType(contentType, config);
                  }
                  orionHoverRegistrant.registerHover(contentTypes, hoverProvider);
                  orionOccurrencesRegistrant.registerOccurrencesHandler(
                      contentTypes, occurrencesProvider);
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                Log.error(LanguageServerFileTypeRegister.this.getClass(), arg.getCause());
              }
            });
  }
}

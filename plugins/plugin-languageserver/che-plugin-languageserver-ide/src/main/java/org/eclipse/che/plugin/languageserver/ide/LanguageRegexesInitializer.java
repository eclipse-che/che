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
package org.eclipse.che.plugin.languageserver.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageServerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LanguageRegexesInitializer {
  private static Logger LOGGER = LoggerFactory.getLogger(LanguageRegexesInitializer.class);

  private final LanguageServerRegistry lsRegistry;
  private final LanguageServerResources resources;
  private final EditorRegistry editorRegistry;
  private final LanguageServerEditorProvider editorProvider;
  private final LanguageServerServiceClient languageServerServiceClient;
  private final FileTypeProvider fileTypeProvider;

  @Inject
  public LanguageRegexesInitializer(
      EventBus eventBus,
      LanguageServerRegistry lsRegistry,
      LanguageServerResources resources,
      EditorRegistry editorRegistry,
      LanguageServerEditorProvider editorProvider,
      LanguageServerServiceClient languageServerServiceClient,
      FileTypeProvider fileTypeProvider) {
    this.lsRegistry = lsRegistry;
    this.resources = resources;
    this.editorRegistry = editorRegistry;
    this.editorProvider = editorProvider;
    this.languageServerServiceClient = languageServerServiceClient;
    this.fileTypeProvider = fileTypeProvider;

    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, e -> unInstall());
    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, e -> unInstall());
  }

  void initialize() {
    languageServerServiceClient
        .getLanguageRegexes()
        .then(
            languageRegexes -> {
              languageRegexes.forEach(
                  languageRegex -> {
                    Set<FileType> fileTypes =
                        fileTypeProvider.getByNamePattern(
                            resources.file(), languageRegex.getNamePattern());
                    fileTypes.forEach(
                        fileType -> {
                          lsRegistry.registerFileType(fileType, languageRegex);
                          editorRegistry.registerDefaultEditor(fileType, editorProvider);
                        });
                  });
            })
        .catchError(
            promiseError -> {
              LOGGER.error("Error", promiseError.getCause());
            });
  }

  private void unInstall() {
    lsRegistry
        .getRegisteredFileTypes()
        .forEach(
            fileType -> {
              lsRegistry.unRegister(fileType);
              editorRegistry.unRegister(fileType, editorProvider);
            });
  }
}

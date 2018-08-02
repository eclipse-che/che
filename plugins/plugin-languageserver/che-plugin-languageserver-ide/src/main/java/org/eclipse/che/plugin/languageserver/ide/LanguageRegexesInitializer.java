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

import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
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
  private final FileTypeRegistry fileTypeRegistry;

  @Inject
  public LanguageRegexesInitializer(
      LanguageServerRegistry lsRegistry,
      LanguageServerResources resources,
      EditorRegistry editorRegistry,
      LanguageServerEditorProvider editorProvider,
      LanguageServerServiceClient languageServerServiceClient,
      FileTypeRegistry fileTypeRegistry) {
    this.lsRegistry = lsRegistry;
    this.resources = resources;
    this.editorRegistry = editorRegistry;
    this.editorProvider = editorProvider;
    this.languageServerServiceClient = languageServerServiceClient;
    this.fileTypeRegistry = fileTypeRegistry;
  }

  void initialize() {
    languageServerServiceClient
        .getLanguageRegexes()
        .then(
            languageRegexes -> {
              languageRegexes.forEach(
                  languageRegex -> {
                    String namePattern = languageRegex.getNamePattern();

                    FileType fileTypeCandidate = null;
                    for (FileType fileType : fileTypeRegistry.getRegisteredFileTypes()) {
                      String extension = fileType.getExtension();
                      if (extension != null && RegExp.compile(namePattern).test('.' + extension)) {
                        fileTypeCandidate = fileType;
                      }

                      String namePatternCandidate = fileType.getNamePattern();
                      if ((namePattern.equals(namePatternCandidate)
                          || RegExp.quote(namePattern).equals(namePatternCandidate))) {
                        fileTypeCandidate = fileType;
                      }
                    }

                    if (fileTypeCandidate == null) {
                      fileTypeCandidate = new FileType(resources.file(), null, namePattern);
                      fileTypeRegistry.registerFileType(fileTypeCandidate);
                    } else {
                      fileTypeCandidate.setNamePattern(namePattern);
                    }

                    lsRegistry.registerFileType(fileTypeCandidate, languageRegex);
                    editorRegistry.registerDefaultEditor(fileTypeCandidate, editorProvider);
                  });
            })
        .catchError(
            promiseError -> {
              LOGGER.error("Error", promiseError.getCause());
            });
  }
}

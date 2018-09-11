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
package org.eclipse.che.plugin.java.languageserver;

import static org.eclipse.che.api.fs.server.WsPathUtils.SEPARATOR;
import static org.eclipse.che.plugin.java.server.rest.JavaFormatterService.CHE_FOLDER;
import static org.eclipse.che.plugin.java.server.rest.JavaFormatterService.CHE_FORMATTER_XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.languageserver.LanguageServerException;
import org.eclipse.che.api.languageserver.LanguageServiceUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class JavaTextDocumentServiceWraper {
  private static final Logger LOG = LoggerFactory.getLogger(JavaTextDocumentServiceWraper.class);

  private TextDocumentService wrapped;

  public JavaTextDocumentServiceWraper(TextDocumentService wrapped) {
    this.wrapped = wrapped;
  }

  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    CompletableFuture<List<Either<Command, CodeAction>>> result = wrapped.codeAction(params);
    return result.thenApply(
        (List<Either<Command, CodeAction>> commands) -> {
          commands.forEach(
              cmd -> {
                if (cmd.isLeft()) {
                  if ("java.apply.workspaceEdit".equals(cmd.getLeft().getCommand())) {
                    cmd.getLeft().setCommand("lsp.applyWorkspaceEdit");
                  }
                }
              });
          return commands;
        });
  }

  public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params)
      throws LanguageServerException {

    String fileUri = params.getTextDocument().getUri();
    FormattingOptions options = params.getOptions();
    if (options == null) {
      options = new FormattingOptions();
      params.setOptions(options);
    }

    updateFormatterOptions(params, fileUri);

    return wrapped.formatting(params);
  }

  private void updateFormatterOptions(DocumentFormattingParams params, String fileUri)
      throws LanguageServerException {
    String projectPath = WsPathUtils.absolutize(LanguageServiceUtils.extractProjectPath(fileUri));
    String formatterPathSuffix = CHE_FOLDER + SEPARATOR + CHE_FORMATTER_XML;
    Path projectFormatterPath = Paths.get(projectPath, formatterPathSuffix);
    Path wsFormatterPath = Paths.get(LanguageServiceUtils.PROJECTS, formatterPathSuffix);
    if (Files.exists(projectFormatterPath)) {
      updateFormatterOptions(params, getFormatSettingsFromFile(projectFormatterPath.toFile()));
    } else if (Files.exists(wsFormatterPath)) {
      updateFormatterOptions(params, getFormatSettingsFromFile(wsFormatterPath.toFile()));
    }
  }

  private void updateFormatterOptions(
      DocumentFormattingParams params, Map<String, String> options) {
    for (String key : options.keySet()) {
      params.getOptions().putString(key, options.get(key));
    }
  }

  private static Map<String, String> getFormatSettingsFromFile(File file) {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    XMLParser parserXML = new XMLParser();
    try (FileInputStream fis = new FileInputStream(file)) {
      SAXParser parser = factory.newSAXParser();
      parser.parse(fis, parserXML);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("It is not possible to parse file " + file.getName(), e);
    }
    return parserXML.getSettings();
  }
}

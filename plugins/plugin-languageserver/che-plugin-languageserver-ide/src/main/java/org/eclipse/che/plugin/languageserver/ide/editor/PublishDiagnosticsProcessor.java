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
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.lsp4j.Diagnostic;

/** @author Anatolii Bazko */
@Singleton
public class PublishDiagnosticsProcessor {

  private final EditorAgent editorAgent;

  @Inject
  public PublishDiagnosticsProcessor(EditorAgent editorAgent) {
    this.editorAgent = editorAgent;
  }

  public void processDiagnostics(ExtendedPublishDiagnosticsParams diagnosticsMessage) {
    EditorPartPresenter openedEditor =
        editorAgent.getOpenedEditor(new Path(diagnosticsMessage.getParams().getUri()));
    // TODO add markers
    if (openedEditor == null) {
      return;
    }

    if (openedEditor instanceof TextEditor) {
      TextEditorConfiguration editorConfiguration = ((TextEditor) openedEditor).getConfiguration();
      AnnotationModel annotationModel = editorConfiguration.getAnnotationModel();
      if (annotationModel != null && annotationModel instanceof DiagnosticCollector) {
        DiagnosticCollector collector = (DiagnosticCollector) annotationModel;
        String languageServerId = diagnosticsMessage.getLanguageServerId();
        collector.beginReporting(languageServerId);
        try {
          for (Diagnostic diagnostic : diagnosticsMessage.getParams().getDiagnostics()) {
            collector.acceptDiagnostic(languageServerId, diagnostic);
          }
        } finally {
          collector.endReporting(languageServerId);
        }
      }
    }
  }
}

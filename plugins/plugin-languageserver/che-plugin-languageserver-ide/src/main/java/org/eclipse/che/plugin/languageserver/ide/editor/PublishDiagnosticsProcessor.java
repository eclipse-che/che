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
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.lsp4j.Diagnostic;

/** @author Anatolii Bazko */
@Singleton
public class PublishDiagnosticsProcessor {

  private final EditorAgent editorAgent;
  private final Tree tree;

  @Inject
  public PublishDiagnosticsProcessor(
      EditorAgent editorAgent, ProjectExplorerPresenter projectExplorer) {
    this.editorAgent = editorAgent;
    this.tree = projectExplorer.getTree();
  }

  public void processDiagnostics(List<ExtendedPublishDiagnosticsParams> diagnostics) {
    tree.getNodeStorage()
        .getAll()
        .stream()
        .filter(node -> node instanceof ResourceNode)
        .map(node -> ((ResourceNode) node))
        .forEach(
            node -> {
              node.getData().setHasError(false);
              tree.refresh(node);
            });
    diagnostics.forEach(this::processDiagnostics);
  }

  private void processDiagnostics(ExtendedPublishDiagnosticsParams diagnosticsMessage) {
    final Path path = Path.valueOf(diagnosticsMessage.getParams().getUri());
    final Optional<ResourceNode> resource =
        tree.getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> node instanceof ResourceNode)
            .map(node -> ((ResourceNode) node))
            .filter(node -> node.getData().isFile())
            .filter(node -> path.equals(node.getData().asFile().getLocation()))
            .findAny();
    resource.ifPresent(this::setHasError);

    EditorPartPresenter openedEditor = editorAgent.getOpenedEditor(path);
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

  private void setHasError(ResourceNode node) {
    node.getData().setHasError(true);
    tree.refresh(node);
    if (node.getParent() instanceof ResourceNode) {
      setHasError((ResourceNode) node.getParent());
    }
  }
}

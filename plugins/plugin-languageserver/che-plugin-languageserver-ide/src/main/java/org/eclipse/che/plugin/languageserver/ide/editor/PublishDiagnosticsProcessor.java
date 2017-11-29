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
import java.util.Collection;
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
import org.eclipse.che.ide.ui.smartTree.NodeDescriptor;
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

  public void processDiagnostics(ExtendedPublishDiagnosticsParams diagnosticsMessage) {
    final Path path = Path.valueOf(diagnosticsMessage.getParams().getUri());
    findVisibleResource(path).ifPresent(this::setHasError);

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

  /**
   * Find the given resource in the Project Explorer or its first visible parent.
   *
   * @param path the path of the resource to find in the Project Explorer.
   * @return an optional containing the {@link ResourceNode} with the given path if it is visible.
   *     If the desired resource is not visible, try to return its first visible parent. If no
   *     matching {@link ResourceNode} can be find, returns an empty {@link Optional}.
   */
  private Optional<ResourceNode> findVisibleResource(Path path) {
    if (path.isRoot()) {
      if (tree.getNodeStorage().getStoredNodes().isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          (ResourceNode) tree.getNodeStorage().getStoredNodes().iterator().next().getNode());
    }
    return findVisibleResource(path, tree.getNodeStorage().getStoredNodes(), path.uptoSegment(1));
  }

  private static Optional<ResourceNode> findVisibleResource(
      Path completePath, Collection<NodeDescriptor> nodes, Path partialPath) {
    for (final NodeDescriptor child : nodes) {
      if (!(child.getNode() instanceof ResourceNode)) {
        continue;
      }
      final ResourceNode resourceNode = (ResourceNode) child.getNode();
      if (!partialPath.equals(resourceNode.getData().getLocation())) {
        continue;
      }
      if (child.getChildren().isEmpty()) {
        return Optional.of(resourceNode);
      }
      return findVisibleResource(
          completePath,
          child.getChildren(),
          completePath.uptoSegment(partialPath.segmentCount() + 1));
    }
    return Optional.empty();
  }

  private void setHasError(ResourceNode node) {
    node.getData().setHasError(true);
    tree.refresh(node);
    if (node.getParent() instanceof ResourceNode) {
      setHasError((ResourceNode) node.getParent());
    }
  }
}

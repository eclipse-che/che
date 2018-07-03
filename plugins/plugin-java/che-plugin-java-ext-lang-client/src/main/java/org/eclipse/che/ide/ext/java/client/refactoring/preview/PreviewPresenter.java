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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.dom.client.Document;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringActionDelegate;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.jdt.ls.extension.api.dto.CheResourceChange;
import org.eclipse.che.jdt.ls.extension.api.dto.CheWorkspaceEdit;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;
import org.eclipse.lsp4j.ResourceChange;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class PreviewPresenter implements PreviewView.ActionDelegate {

  private final PreviewView view;
  private final AppContext appContext;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;
  private final DtoFactory dtoFactory;

  private Map<String, PreviewNode> fileNodes;
  private CheWorkspaceEdit workspaceEdit;
  private RefactoringActionDelegate refactoringActionDelegate;

  @Inject
  public PreviewPresenter(
      PreviewView view,
      AppContext appContext,
      ApplyWorkspaceEditAction applyWorkspaceEditAction,
      DtoFactory dtoFactory) {
    this.view = view;
    this.appContext = appContext;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
    this.dtoFactory = dtoFactory;
    this.view.setDelegate(this);

    fileNodes = new LinkedHashMap<>();
  }

  public void show(String refactoringSessionId, RefactorInfo refactorInfo) {
    view.showDialog();
  }

  /**
   * Set a title of the window.
   *
   * @param title the name of the preview window
   */
  public void setTitle(String title) {
    view.setTitleCaption(title);
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    view.close();
  }

  /** {@inheritDoc} */
  @Override
  public void onAcceptButtonClicked() {
    updateFinalEdits();

    applyWorkspaceEditAction.applyWorkspaceEdit(workspaceEdit);
    view.close();
    refactoringActionDelegate.closeWizard();
  }

  /** {@inheritDoc} */
  @Override
  public void onBackButtonClicked() {
    view.close();
  }

  @Override
  public void onSelectionChanged(PreviewNode selectedNode) {
    Either<ResourceChange, TextEdit> data = selectedNode.getData();
    if (data != null && data.isLeft()) {
      view.showDiff(null);
      return;
    }

    List<TextEdit> edits = collectTextEditsForSelectedNode(selectedNode);

    updateContentInCompareWidget(selectedNode, edits);
  }

  private void updateContentInCompareWidget(PreviewNode selectedNode, List<TextEdit> edits) {
    String path = selectedNode.getUri();
    Container workspaceRoot = appContext.getWorkspaceRoot();
    Promise<Optional<File>> file = workspaceRoot.getFile(path);
    file.then(
        fileOptional -> {
          if (!fileOptional.isPresent()) {
            return;
          }
          File existingFile = fileOptional.get();
          existingFile
              .getContent()
              .then(
                  content -> {
                    ChangePreview changePreview = new ChangePreview();
                    changePreview.setFileName(existingFile.getName());
                    changePreview.setOldContent(content);

                    // apply all related TextEdit to show new content in compare widget
                    StringBuilder output = new StringBuilder();
                    new StringStreamEditor(edits, content, output).transform();
                    String result = output.toString();

                    changePreview.setNewContent(result);

                    view.showDiff(changePreview);
                  });
        });
  }

  /**
   * Finds all enabled TextEdit changes which are children of the selected node and collect them to
   * the list.
   *
   * @param selectedNode the node which was selected
   * @return list of the enabled changes
   */
  private List<TextEdit> collectTextEditsForSelectedNode(PreviewNode selectedNode) {
    Either<ResourceChange, TextEdit> data = selectedNode.getData();
    PreviewNode node = fileNodes.get(selectedNode.getUri());
    List<TextEdit> edits = new ArrayList<>();
    if (node.getId().equals(selectedNode.getId())) {
      for (PreviewNode child : node.getChildren()) {
        TextEdit right = child.getData().getRight();
        if (child.isEnable()) {
          edits.add(right);
        }
      }
    } else if (data != null && selectedNode.isEnable()) {
      edits.add(data.getRight());
    }
    return edits;
  }

  @Override
  public void onEnabledStateChanged(PreviewNode change) {
    Either<ResourceChange, TextEdit> data = change.getData();
    if (data != null && data.isLeft()) {
      ResourceChange left = data.getLeft();
      fileNodes.get(left.getNewUri()).setEnable(change.isEnable());
    } else {
      PreviewNode previewNode = fileNodes.get(change.getUri());
      if (previewNode.getId().equals(change.getId())) {
        previewNode.setEnable(change.isEnable());
        for (PreviewNode node : previewNode.getChildren()) {
          node.setEnable(change.isEnable());
        }
      } else {
        for (PreviewNode node : previewNode.getChildren()) {
          if (node.getId().equals(change.getId())) {
            node.setEnable(change.isEnable());
          }
        }
      }
    }
  }

  public void show(
      CheWorkspaceEdit workspaceEdit, RefactoringActionDelegate refactoringActionDelegate) {
    this.workspaceEdit = workspaceEdit;
    this.refactoringActionDelegate = refactoringActionDelegate;

    prepareNodes(workspaceEdit);

    view.setTreeOfChanges(fileNodes);
    view.showDialog();
  }

  private void prepareNodes(CheWorkspaceEdit workspaceEdit) {
    fileNodes.clear();
    prepareTextEditNodes(workspaceEdit.getChanges());
    prepareResourceChangeNodes(workspaceEdit.getCheResourceChanges());
  }

  private void prepareResourceChangeNodes(List<CheResourceChange> resourceChanges) {
    for (CheResourceChange resourceChange : resourceChanges) {
      PreviewNode node = new PreviewNode();
      node.setData(Either.forLeft(resourceChange));
      node.setEnable(true);
      String uniqueId = Document.get().createUniqueId();
      node.setId(uniqueId);
      String current = resourceChange.getCurrent();
      String newUri = resourceChange.getNewUri();
      node.setUri(newUri);
      if (current != null && newUri != null) {
        if (!Strings.isNullOrEmpty(resourceChange.getDescription())) {
          node.setDescription(resourceChange.getDescription());
        } else if (Path.valueOf(current)
            .removeLastSegments(1)
            .equals(Path.valueOf(newUri).removeLastSegments(1))) {
          node.setDescription(
              "Rename resource '"
                  + Path.valueOf(current).lastSegment()
                  + "' to '"
                  + Path.valueOf(newUri).lastSegment()
                  + "'");
        } else {
          node.setDescription(
              "Move resource '"
                  + Path.valueOf(current).lastSegment()
                  + "' to '"
                  + Path.valueOf(newUri).removeLastSegments(1)
                  + "'");
        }
        fileNodes.put(newUri, node);
      } else if (current == null && newUri != null) {
        node.setDescription("Create resource: '" + Path.valueOf(newUri) + "'");
        fileNodes.put(newUri, node);
      }
    }
  }

  private void prepareTextEditNodes(Map<String, List<TextEdit>> changes) {
    for (String uri : changes.keySet()) {
      PreviewNode parent = new PreviewNode();
      parent.setUri(uri);
      parent.setEnable(true);
      String uniqueId = Document.get().createUniqueId();
      parent.setId(uniqueId);
      Path path = Path.valueOf(uri);
      parent.setDescription(path.lastSegment() + " - " + path.removeLastSegments(1));
      fileNodes.put(uri, parent);
      for (TextEdit change : changes.get(uri)) {
        PreviewNode child = new PreviewNode();
        child.setEnable(true);
        child.setId(Document.get().createUniqueId());
        child.setDescription("Textual change");
        child.setData(Either.forRight(change));
        child.setUri(uri);
        parent.getChildren().add(child);
      }
    }
  }

  private void updateFinalEdits() {
    for (PreviewNode node : fileNodes.values()) {
      Either<ResourceChange, TextEdit> data = node.getData();
      if (data != null && data.isLeft()) {
        if (node.isEnable()) {
          continue;
        }
        ResourceChange left = data.getLeft();
        List<CheResourceChange> selectedResourceChanges =
            workspaceEdit
                .getCheResourceChanges()
                .stream()
                .filter(item -> !item.equals(left))
                .collect(toList());
        workspaceEdit.setCheResourceChanges(selectedResourceChanges);
      } else {
        if (data == null && !node.isEnable()) {
          workspaceEdit.getChanges().remove(node.getUri());
          continue;
        }
        List<PreviewNode> children = node.getChildren();
        for (PreviewNode textNode : children) {
          if (textNode.isEnable()) {
            continue;
          }
          TextEdit right = textNode.getData().getRight();
          List<TextEdit> textNodes =
              workspaceEdit
                  .getChanges()
                  .get(node.getUri())
                  .stream()
                  .filter(item -> !item.equals(right))
                  .collect(toList());

          workspaceEdit.getChanges().put(node.getUri(), textNodes);
        }
      }
    }
  }
}

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
package org.eclipse.che.ide.search.presentation;

import static org.eclipse.che.ide.util.dom.Elements.createSpanElement;
import static org.eclipse.che.ide.util.dom.Elements.createTextNode;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Instance describe occurrence where found give text. If in file found several occurrence, will be
 * created dedicated instance for each occurrence
 *
 * @author Vitalii Parfonov
 */
public class FoundOccurrenceNode extends AbstractTreeNode implements HasPresentation, HasAction {

  private NodePresentation nodePresentation;
  private AppContext appContext;
  private TreeStyles styles;
  private Resources resources;
  private EditorAgent editorAgent;
  private String itemPath;
  private SearchOccurrence searchOccurrence;

  @Inject
  public FoundOccurrenceNode(
      AppContext appContext,
      TreeStyles styles,
      Resources resources,
      EditorAgent editorAgent,
      @Assisted SearchOccurrence searchOccurrence,
      @Assisted String itemPath) {
    this.appContext = appContext;
    this.styles = styles;
    this.resources = resources;
    this.editorAgent = editorAgent;
    this.itemPath = itemPath;
    this.searchOccurrence = searchOccurrence;
  }

  @Override
  @SuppressWarnings("Duplicates")
  public void actionPerformed() {
    final EditorPartPresenter editorPartPresenter =
        editorAgent.getOpenedEditor(Path.valueOf(itemPath));
    if (editorPartPresenter != null) {
      selectRange(editorPartPresenter);
      Scheduler.get().scheduleDeferred(() -> editorAgent.activateEditor(editorPartPresenter));
      return;
    }

    appContext
        .getWorkspaceRoot()
        .getFile(itemPath)
        .then(
            file -> {
              if (file.isPresent()) {
                editorAgent.openEditor(
                    file.get(),
                    new OpenEditorCallbackImpl() {
                      @Override
                      public void onEditorOpened(EditorPartPresenter editor) {
                        selectRange(editor);
                      }
                    });
              }
            });
  }

  private void selectRange(EditorPartPresenter editor) {
    if (editor instanceof TextEditor) {
      ((TextEditor) editor)
          .getDocument()
          .setSelectedRange(
              LinearRange.createWithStart(searchOccurrence.getStartOffset())
                  .andEnd(searchOccurrence.getEndOffset()),
              true);
    }
  }

  /** {@inheritDoc} */
  @Override
  public NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
      updatePresentation(nodePresentation);
      return nodePresentation;
    }
    if (update) {
      updatePresentation(nodePresentation);
    }
    return nodePresentation;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return searchOccurrence.getPhrase();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isLeaf() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  // TODO
  public void updatePresentation(@NotNull NodePresentation presentation) {
    SpanElement spanElement = createSpanElement(styles.styles().presentableTextContainer());
    spanElement.setAttribute("debugFilePath", itemPath);
    SpanElement lineNumberElement = createSpanElement();
    lineNumberElement.setInnerHTML(
        String.valueOf(searchOccurrence.getLineNumber() + 1) + ":&nbsp;&nbsp;&nbsp;");
    spanElement.appendChild(lineNumberElement);
    SpanElement textElement = createSpanElement();
    String phrase = searchOccurrence.getPhrase();
    String matchedLine = searchOccurrence.getLineContent();
    if (matchedLine != null && phrase != null) {
      String startOfLine = matchedLine.substring(0, matchedLine.indexOf(phrase));
      String endOfLine = matchedLine.substring(matchedLine.indexOf(phrase) + phrase.length());
      textElement.appendChild(createTextNode(startOfLine));
      SpanElement highlightElement = createSpanElement(resources.coreCss().searchMatch());
      highlightElement.setInnerText(phrase);
      textElement.appendChild(highlightElement);
      textElement.appendChild(createTextNode(endOfLine));
    } else {
      textElement.appendChild(createTextNode("Can't find sources"));
    }
    spanElement.appendChild(textElement);
    presentation.setPresentableIcon(resources.searchMatch());
    presentation.setUserElement((Element) spanElement);
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }
}

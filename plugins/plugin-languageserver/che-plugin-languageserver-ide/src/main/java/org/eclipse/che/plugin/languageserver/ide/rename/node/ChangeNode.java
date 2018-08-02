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
package org.eclipse.che.plugin.languageserver.ide.rename.node;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameChange;
import org.eclipse.lsp4j.Range;

/** Tree node, represent edit in file */
public class ChangeNode extends AbstractPresentationNode implements HasAction {

  private final TreeStyles styles;
  private final LanguageServerResources resources;
  private final EditorAgent editorAgent;
  private final AppContext appContext;
  private final RenameChange change;

  @Inject
  public ChangeNode(
      TreeStyles styles,
      LanguageServerResources resources,
      EditorAgent editorAgent,
      AppContext appContext,
      @Assisted RenameChange change) {
    this.styles = styles;
    this.resources = resources;
    this.editorAgent = editorAgent;
    this.appContext = appContext;
    this.change = change;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }

  @Override
  public String getName() {
    return change.getTextEdit().getLineText();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    SpanElement spanElement =
        Elements.createSpanElement(styles.treeStylesCss().presentableTextContainer());

    SpanElement lineNumberElement = Elements.createSpanElement();
    lineNumberElement.setInnerHTML(
        String.valueOf(change.getTextEdit().getRange().getStart().getLine() + 1)
            + ":&nbsp;&nbsp;&nbsp;");
    spanElement.appendChild(lineNumberElement);

    SpanElement textElement = Elements.createSpanElement();

    String matchedLine = change.getTextEdit().getLineText();
    int startOffset = change.getTextEdit().getInLineStart();
    int endOffset = change.getTextEdit().getInLineEnd();
    if (matchedLine != null) {
      String startLine = matchedLine.substring(0, startOffset);
      textElement.appendChild(Elements.createTextNode(startLine));
      SpanElement highlightElement =
          Elements.createSpanElement(resources.quickOpenListCss().searchMatch());
      highlightElement.setInnerText(matchedLine.substring(startOffset, endOffset));
      textElement.appendChild(highlightElement);

      textElement.appendChild(Elements.createTextNode(matchedLine.substring(endOffset)));
    } else {
      textElement.appendChild(Elements.createTextNode("Can't find sources"));
    }
    spanElement.appendChild(textElement);

    presentation.setUserElement((Element) spanElement);
  }

  @Override
  public void actionPerformed() {
    final EditorPartPresenter editorPartPresenter =
        editorAgent.getOpenedEditor(Path.valueOf(change.getFilePath()));
    if (editorPartPresenter != null) {
      selectRange(editorPartPresenter);
      Scheduler.get().scheduleDeferred(() -> editorAgent.activateEditor(editorPartPresenter));
      return;
    }

    appContext
        .getWorkspaceRoot()
        .getFile(change.getFilePath())
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
      Range range = change.getTextEdit().getRange();
      ((TextEditor) editor)
          .getDocument()
          .setSelectedRange(
              new TextRange(
                  new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()),
                  new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter())),
              true);
    }
  }
}

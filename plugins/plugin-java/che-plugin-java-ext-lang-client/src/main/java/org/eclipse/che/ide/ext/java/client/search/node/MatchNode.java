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
package org.eclipse.che.ide.ext.java.client.search.node;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import elemental.html.SpanElement;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SyntheticFile;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.shared.dto.ClassContent;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Node represent Match for find usages search.
 *
 * @author Evgen Vidolob
 */
public class MatchNode extends AbstractPresentationNode implements HasAction {

  private TreeStyles styles;
  private JavaResources resources;
  private EditorAgent editorAgent;
  private AppContext appContext;
  private Match match;
  private CompilationUnit compilationUnit;
  private ClassFile classFile;
  private final JavaNavigationService service;

  @Inject
  public MatchNode(
      TreeStyles styles,
      JavaResources resources,
      EditorAgent editorAgent,
      AppContext appContext,
      @Assisted Match match,
      @Nullable @Assisted CompilationUnit compilationUnit,
      @Nullable @Assisted ClassFile classFile,
      JavaNavigationService service) {
    this.styles = styles;
    this.resources = resources;
    this.editorAgent = editorAgent;
    this.appContext = appContext;
    this.match = match;
    this.compilationUnit = compilationUnit;
    this.classFile = classFile;
    this.service = service;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    SpanElement spanElement =
        Elements.createSpanElement(styles.treeStylesCss().presentableTextContainer());

    SpanElement lineNumberElement = Elements.createSpanElement();
    lineNumberElement.setInnerHTML(
        String.valueOf(match.getMatchLineNumber() + 1) + ":&nbsp;&nbsp;&nbsp;");
    spanElement.appendChild(lineNumberElement);

    SpanElement textElement = Elements.createSpanElement();
    Region matchInLine = match.getMatchInLine();
    String matchedLine = match.getMatchedLine();
    if (matchedLine != null && matchInLine != null) {
      String startLine = matchedLine.substring(0, matchInLine.getOffset());
      textElement.appendChild(Elements.createTextNode(startLine));
      SpanElement highlightElement = Elements.createSpanElement(resources.css().searchMatch());
      highlightElement.setInnerText(
          matchedLine.substring(
              matchInLine.getOffset(), matchInLine.getOffset() + matchInLine.getLength()));
      textElement.appendChild(highlightElement);

      textElement.appendChild(
          Elements.createTextNode(
              matchedLine.substring(matchInLine.getOffset() + matchInLine.getLength())));
    } else {
      textElement.appendChild(Elements.createTextNode("Can't find sources"));
    }
    spanElement.appendChild(textElement);

    presentation.setPresentableIcon(resources.searchMatch());
    presentation.setUserElement((Element) spanElement);
  }

  @Override
  public String getName() {
    return match.getMatchedLine();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  public Match getMatch() {
    return match;
  }

  @Override
  public void actionPerformed() {
    if (compilationUnit != null) {
      final EditorPartPresenter editorPartPresenter =
          editorAgent.getOpenedEditor(Path.valueOf(compilationUnit.getPath()));
      if (editorPartPresenter != null) {
        selectRange(editorPartPresenter);
        Scheduler.get()
            .scheduleDeferred(
                new Scheduler.ScheduledCommand() {
                  @Override
                  public void execute() {
                    editorAgent.activateEditor(editorPartPresenter);
                  }
                });
        return;
      }

      appContext
          .getWorkspaceRoot()
          .getFile(compilationUnit.getPath())
          .then(
              new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
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
                }
              });
    } else if (classFile != null) {
      final String className = classFile.getElementName();

      final Resource resource = appContext.getResource();

      if (resource == null) {
        return;
      }

      final Project project = resource.getRelatedProject().get();

      service
          .getContent(project.getLocation(), className)
          .then(
              new Operation<ClassContent>() {
                @Override
                public void apply(ClassContent content) throws OperationException {
                  final VirtualFile file =
                      new SyntheticFile(
                          Path.valueOf(className.replace('.', '/')).lastSegment(),
                          content.getContent());
                  editorAgent.openEditor(
                      file,
                      new OpenEditorCallbackImpl() {
                        @Override
                        public void onEditorOpened(EditorPartPresenter editor) {
                          selectRange(editor);
                        }
                      });
                }
              });
    }
  }

  private void selectRange(EditorPartPresenter editor) {
    if (editor instanceof TextEditor) {
      ((TextEditor) editor)
          .getDocument()
          .setSelectedRange(
              LinearRange.createWithStart(match.getFileMatchRegion().getOffset())
                  .andLength(match.getFileMatchRegion().getLength()),
              true);
    }
  }
}

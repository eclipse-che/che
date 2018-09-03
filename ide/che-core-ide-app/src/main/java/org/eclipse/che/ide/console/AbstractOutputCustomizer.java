/*
 * Copyright (c) 2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

import com.google.gwt.user.client.Timer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;

/**
 * Default customizer adds an anchor link to the lines that match a stack trace line pattern and
 * installs a handler function for the link. The handler parses the stack trace line, searches for
 * the candidate Java files to navigate to, opens the first file (of the found candidates) in editor
 * and reveals it to the required line according to the stack trace line information
 *
 * @author Victor Rubezhny
 */
public abstract class AbstractOutputCustomizer implements OutputCustomizer {

  protected AppContext appContext;
  protected EditorAgent editorAgent;

  public AbstractOutputCustomizer(AppContext appContext, EditorAgent editorAgent) {
    this.appContext = appContext;
    this.editorAgent = editorAgent;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
   * OutputCustomizer#canCustomize(java.lang.String)
   */
  @Override
  public abstract boolean canCustomize(String text);

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
   * OutputCustomizer#customize(java.lang.String)
   */
  @Override
  public abstract String customize(String text);

  /*
   * Returns the list of workspace files filtered by a relative path
   */
  protected Promise<List<File>> collectChildren(Container root, Path relativeFilePath) {
    return root.getTree(-1)
        .then(
            new Function<Resource[], List<File>>() {
              @Override
              public List<File> apply(Resource[] children) throws FunctionException {
                return Stream.of(children)
                    .filter(
                        child ->
                            child.isFile()
                                && endsWith(child.asFile().getLocation(), relativeFilePath))
                    .map(Resource::asFile)
                    .collect(Collectors.toList());
              }
            });
  }

  /*
   * Checks if a path's last segments are equal to the provided relative path
   */
  protected boolean endsWith(Path path, Path relativePath) {
    checkNotNull(path);
    checkNotNull(relativePath);

    if (path.segmentCount() < relativePath.segmentCount()) return false;

    for (int i = relativePath.segmentCount() - 1, j = path.segmentCount() - 1; i >= 0; i--, j--) {
      if (!nullToEmpty(relativePath.segment(i)).equals(path.segment(j))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Finds a file by its path, opens it in editor and sets the text selection and reveals according
   * to the specified line and column numbers
   *
   * @param file
   * @param lineNumber
   * @param columnNumber
   */
  protected void openFileInEditorAndReveal(
      AppContext appContext,
      EditorAgent editorAgent,
      Path file,
      final int lineNumber,
      final int columnNumber) {
    appContext
        .getWorkspaceRoot()
        .getFile(file)
        .then(
            optional -> {
              if (optional.isPresent()) {
                editorAgent.openEditor(
                    optional.get(),
                    new OpenEditorCallbackImpl() {
                      @Override
                      public void onEditorOpened(EditorPartPresenter editor) {
                        Timer t =
                            new Timer() {
                              @Override
                              public void run() {
                                EditorPartPresenter editorPart = editorAgent.getActiveEditor();
                                selectRange(editorPart, lineNumber, columnNumber);
                              }
                            };
                        t.schedule(500);
                      }

                      @Override
                      public void onEditorActivated(EditorPartPresenter editor) {
                        selectRange(editor, lineNumber, columnNumber);
                      }
                    });
              }
            });
  }

  /**
   * Selects and shows the specified line and column of text in editor
   *
   * @param editor
   * @param line
   * @param column
   */
  protected void selectRange(EditorPartPresenter editor, int line, int column) {
    line--;
    column--;
    if (line < 0) line = 0;
    if (editor instanceof TextEditor) {
      Document document = ((TextEditor) editor).getDocument();
      LinearRange selectionRange = document.getLinearRangeForLine(line);
      if (column >= 0) {
        selectionRange =
            LinearRange.createWithStart(selectionRange.getStartOffset() + column).andLength(0);
      }
      document.setSelectedRange(selectionRange, true);
      document.setCursorPosition(new TextPosition(line, column >= 0 ? column : 0));
    }
  }
}

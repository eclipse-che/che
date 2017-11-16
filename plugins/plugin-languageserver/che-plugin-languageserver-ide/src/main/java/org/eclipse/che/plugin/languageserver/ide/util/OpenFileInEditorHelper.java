/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.util.Constants;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.languageserver.ide.location.LanguageServerFile;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

/**
 * Util class, helps to open file by path in editor
 *
 * @author Evgen Vidolob
 */
@Singleton
public class OpenFileInEditorHelper {

  private final EditorAgent editorAgent;
  private final AppContext appContext;
  private final TextDocumentServiceClient textDocumentService;

  @Inject
  public OpenFileInEditorHelper(
      EditorAgent editorAgent,
      AppContext appContext,
      TextDocumentServiceClient textDocumentService) {
    this.editorAgent = editorAgent;
    this.appContext = appContext;
    this.textDocumentService = textDocumentService;
  }

  public void openPath(final String filePath, final TextRange selectionRange) {
    doIfUnopened(
        filePath,
        selectionRange,
        () -> {
          appContext.getWorkspaceRoot().getFile(filePath).then(openNode(selectionRange));
        });
  }

  public void openFile(VirtualFile file, TextRange selectionRange) {
    doIfUnopened(
        file.getLocation().toString(), selectionRange, () -> doOpenFile(file, selectionRange));
  }

  private void doIfUnopened(final String filePath, final TextRange selectionRange, Runnable r) {
    if (Strings.isNullOrEmpty(filePath)) {
      return;
    }

    EditorPartPresenter editorPartPresenter = editorAgent.getOpenedEditor(Path.valueOf(filePath));
    if (editorPartPresenter != null) {
      editorAgent.activateEditor(editorPartPresenter);
      fileOpened(editorPartPresenter, selectionRange);
      return;
    }
    r.run();
  }

  public void openFile(String filePath) {
    openPath(filePath, null);
  }

  private Function<Optional<File>, Optional<File>> openNode(final TextRange selectionRange) {
    return new Function<Optional<File>, Optional<File>>() {
      @Override
      public Optional<File> apply(Optional<File> node) {
        if (node.isPresent()) {
          doOpenFile(node.get(), selectionRange);
        }
        return node;
      }
    };
  }

  private void doOpenFile(VirtualFile result, final TextRange selectionRange) {
    editorAgent.openEditor(
        result,
        new OpenEditorCallbackImpl() {
          @Override
          public void onEditorOpened(EditorPartPresenter editor) {
            fileOpened(editor, selectionRange);
          }
        });
  }

  private void fileOpened(final EditorPartPresenter editor, final TextRange selectionRange) {
    if (editor instanceof TextEditor && selectionRange != null) {
      new DelayedTask() {
        @Override
        public void onExecute() {
          ((TextEditor) editor).getDocument().setSelectedRange(selectionRange, true);
          editor.activate(); // force set focus to the editor
        }
      }.delay(100);
    }
  }

  public void openLocation(Location location) {
    Range range = location.getRange();
    String uri = location.getUri();
    TextRange selectionRange =
        new TextRange(
            new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()),
            new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
    if (uri.startsWith(Constants.CHE_WKSP_SCHEME)) {
      openPath(location.getUri().substring(Constants.CHE_WKSP_SCHEME.length()), selectionRange);
    } else {
      openFile(new LanguageServerFile(textDocumentService, location), selectionRange);
    }
  }
}

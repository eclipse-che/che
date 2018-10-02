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
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.function.Consumer;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
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
  private PromiseProvider promiseProvider;

  @Inject
  public OpenFileInEditorHelper(
      EditorAgent editorAgent,
      AppContext appContext,
      TextDocumentServiceClient textDocumentService,
      PromiseProvider promiseProvider) {
    this.editorAgent = editorAgent;
    this.appContext = appContext;
    this.textDocumentService = textDocumentService;
    this.promiseProvider = promiseProvider;
  }

  public void openPath(final String filePath, final TextRange selectionRange) {
    Consumer<TextEditor> selectRange = selectRange(selectionRange);
    openPath(filePath, selectRange);
  }

  private void openPath(final String filePath, Consumer<TextEditor> selectRange) {
    doIfUnopened(
        filePath,
        selectRange,
        failIfNull(appContext.getWorkspaceRoot().getFile(filePath)).thenPromise(this::doOpenFile));
  }

  private <T> Promise<T> failIfNull(Promise<Optional<T>> optionalPromis) {
    return optionalPromis.thenPromise(
        (optional) -> {
          return promiseProvider.create(
              Executor.create(
                  (resolve, reject) -> {
                    if (optional.isPresent()) {
                      resolve.apply(optional.get());
                    } else {
                      reject.apply(JsPromiseError.create("optional is null"));
                    }
                  }));
        });
  }

  private Consumer<TextEditor> selectRange(TextRange range) {
    return (editor) -> {
      Document document = editor.getDocument();
      document.setSelectedRange(range, true);
    };
  }

  private Consumer<TextEditor> selectRange(LinearRange range) {
    return (editor) -> {
      editor
          .getDocument()
          .setSelectedRange(
              org.eclipse.che.ide.api.editor.text.LinearRange.createWithStart(range.getOffset())
                  .andLength(range.getLength()),
              true);
    };
  }

  public void openFile(VirtualFile file, TextRange selectionRange) {
    Consumer<TextEditor> select = selectRange(selectionRange);
    openFile(file, select);
  }

  private void openFile(VirtualFile file, Consumer<TextEditor> select) {
    doIfUnopened(file.getLocation().toString(), select, doOpenFile(file));
  }

  private void doIfUnopened(
      final String filePath, Consumer<TextEditor> onEditorOpened, Promise<TextEditor> openEditor) {
    if (Strings.isNullOrEmpty(filePath)) {
      return;
    }

    EditorPartPresenter editorPartPresenter = editorAgent.getOpenedEditor(Path.valueOf(filePath));
    if (editorPartPresenter != null) {
      editorAgent.activateEditor(editorPartPresenter);
      fileOpened(editorPartPresenter, onEditorOpened);
    } else {
      openEditor.then(
          (editor) -> {
            fileOpened(editor, onEditorOpened);
          });
    }
  }

  private Promise<TextEditor> doOpenFile(VirtualFile result) {
    return promiseProvider.create(
        Executor.create(
            (resolve, reject) -> {
              editorAgent.openEditor(
                  result,
                  new OpenEditorCallbackImpl() {
                    @Override
                    public void onEditorOpened(EditorPartPresenter editor) {
                      if (editor instanceof TextEditor) {
                        resolve.apply((TextEditor) editor);
                      } else {
                        reject.apply(JsPromiseError.create("Editor is not a TextEditor"));
                      }
                    }
                  });
            }));
  }

  private void fileOpened(final EditorPartPresenter editor, Consumer<TextEditor> onEditorOpened) {
    if (editor instanceof TextEditor && onEditorOpened != null) {
      new DelayedTask() {
        @Override
        public void onExecute() {
          onEditorOpened.accept((TextEditor) editor);
          editor.activate(); // force set focus to the editor
        }
      }.delay(100);
    }
  }

  public void openLocation(String uri, LinearRange range) {
    Consumer<TextEditor> selectRange = selectRange(range);
    if (uri.startsWith("/")) {
      openPath(uri, selectRange);
    } else {
      openFile(new LanguageServerFile(textDocumentService, uri), selectRange);
    }
  }

  public void openLocation(Location location) {
    Range range = location.getRange();
    String uri = location.getUri();
    TextRange selectionRange =
        new TextRange(
            new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()),
            new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
    if (uri.startsWith("/")) {
      openPath(location.getUri(), selectionRange);
    } else {
      openFile(new LanguageServerFile(textDocumentService, location.getUri()), selectionRange);
    }
  }
}

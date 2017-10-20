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
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.languageserver.shared.util.RangeComparator;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * Applies a list of {@link org.eclipse.lsp4j.TextEdit} changes to the current editor.
 *
 * @author Thomas Mäder
 */
@Singleton
public class ApplyTextEditAction extends BaseAction {
  private static final Comparator<TextEdit> COMPARATOR =
      RangeComparator.transform(new RangeComparator(), TextEdit::getRange);

  private EditorAgent editorAgent;
  private DtoFactory dtoFactory;

  @Inject
  public ApplyTextEditAction(EditorAgent editorAgent, DtoFactory dtoFactory) {
    super("Apply Text Edit");
    this.editorAgent = editorAgent;
    this.dtoFactory = dtoFactory;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (!(activeEditor instanceof TextEditor)) {
      return;
    }
    Document document = ((TextEditor) activeEditor).getDocument();
    // We expect the arguments to be of the correct type: static misconfiguration is
    // a programming error.
    List<Object> arguments = ((QuickassistActionEvent) evt).getArguments();
    List<TextEdit> edits =
        arguments
            .stream()
            .map(arg -> dtoFactory.createDtoFromJson(arg.toString(), TextEdit.class))
            .sorted(COMPARATOR)
            .collect(Collectors.toList());

    // jdt.ls sends text edits in reverse order of application
    // see https://github.com/eclipse/eclipse.jdt.ls/issues/398
    for (int i = edits.size() - 1; i >= 0; i--) {
      TextEdit e = edits.get(i);
      Range r = e.getRange();
      Position start = r.getStart();
      Position end = r.getEnd();
      document.replace(
          start.getLine(), start.getCharacter(), end.getLine(), end.getCharacter(), e.getNewText());
    }
  }
}

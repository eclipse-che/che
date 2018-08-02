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
package org.eclipse.che.plugin.languageserver.ide.navigation.symbol;

import java.util.List;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenEntryGroup;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
class SymbolEntry extends QuickOpenEntryGroup {

  private String name;
  private String type;
  private String description;
  private TextRange range;
  private TextEditor editor;
  private SVGResource icon;

  public SymbolEntry(
      String name,
      String type,
      String description,
      TextRange range,
      TextEditor editor,
      List<Match> highlights,
      SVGResource icon) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.range = range;
    this.editor = editor;
    this.icon = icon;
    setHighlights(highlights);
  }

  @Override
  public String getLabel() {
    return name;
  }

  @Override
  public SVGResource getIcon() {
    return icon;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public TextRange getRange() {
    return range;
  }

  @Override
  public boolean run(Mode mode) {
    if (mode == Mode.OPEN) {
      return runOpen();
    }
    return runPreview();
  }

  private boolean runPreview() {
    editor.getDocument().setSelectedRange(range, true);
    return false;
  }

  private boolean runOpen() {
    editor
        .getDocument()
        .setCursorPosition(
            new TextPosition(range.getFrom().getLine(), range.getFrom().getCharacter()));
    editor.setFocus();
    return true;
  }
}

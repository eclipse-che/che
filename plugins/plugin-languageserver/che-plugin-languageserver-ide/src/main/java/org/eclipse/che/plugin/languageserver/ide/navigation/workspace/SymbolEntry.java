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
package org.eclipse.che.plugin.languageserver.ide.navigation.workspace;

import java.util.List;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.quickopen.EditorQuickOpenEntry;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
class SymbolEntry extends EditorQuickOpenEntry {

  private String name;
  private String parameters;
  private String description;
  private String filePath;
  private String type;
  private TextRange range;
  private SVGResource icon;

  public SymbolEntry(
      String name,
      String parameters,
      String description,
      String filePath,
      String type,
      TextRange range,
      SVGResource icon,
      OpenFileInEditorHelper editorHelper,
      List<Match> matches) {
    super(editorHelper);
    this.name = name;
    this.parameters = parameters;
    this.description = description;
    this.filePath = filePath;
    this.type = type;
    this.range = range;
    this.icon = icon;
    setHighlights(matches);
  }

  @Override
  protected String getFilePath() {
    return filePath;
  }

  @Override
  public String getLabel() {
    return name + parameters;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public SVGResource getIcon() {
    return icon;
  }

  public String getType() {
    return type;
  }

  @Override
  protected TextRange getTextRange() {
    return range;
  }
}

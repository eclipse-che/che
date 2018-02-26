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
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenEntry;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Location;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
class SymbolEntry extends QuickOpenEntry {

  private String name;
  private String parameters;
  private String description;
  private Location symbolLocation;
  private String type;
  private OpenFileInEditorHelper editorHelper;
  private SVGResource icon;

  public SymbolEntry(
      String name,
      String parameters,
      String description,
      Location symbolLocation,
      String type,
      SVGResource icon,
      OpenFileInEditorHelper editorHelper,
      List<Match> matches) {
    this.name = name;
    this.parameters = parameters;
    this.description = description;
    this.editorHelper = editorHelper;
    this.type = type;
    this.icon = icon;
    this.symbolLocation = symbolLocation;
    setHighlights(matches);
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

  public Location getLocation() {
    return symbolLocation;
  }

  @Override
  public boolean run(Mode mode) {
    if (mode == Mode.OPEN) {
      editorHelper.openLocation(getLocation());
      return true;
    }
    return false;
  }
}

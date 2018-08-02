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
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import java.util.List;
import org.eclipse.che.ide.filters.Match;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
public class QuickOpenEntryGroup extends QuickOpenEntry {

  private QuickOpenEntry entry;
  private String groupLabel;
  private boolean withBorder;

  public QuickOpenEntryGroup() {}

  public QuickOpenEntryGroup(QuickOpenEntry entry, String groupLabel, boolean withBorder) {
    this.entry = entry;
    this.groupLabel = groupLabel;
    this.withBorder = withBorder;
  }

  public QuickOpenEntry getEntry() {
    return entry;
  }

  public String getGroupLabel() {
    return groupLabel;
  }

  public void setGroupLabel(String groupLabel) {
    this.groupLabel = groupLabel;
  }

  public boolean isWithBorder() {
    return withBorder;
  }

  public void setWithBorder(boolean withBorder) {
    this.withBorder = withBorder;
  }

  @Override
  public String getLabel() {
    return entry != null ? entry.getLabel() : super.getLabel();
  }

  @Override
  public String getDetail() {
    return entry != null ? entry.getDetail() : super.getDetail();
  }

  @Override
  public SVGResource getIcon() {
    return entry != null ? entry.getIcon() : super.getIcon();
  }

  @Override
  public String getDescription() {
    return entry != null ? entry.getDescription() : super.getDescription();
  }

  @Override
  public String getURI() {
    return entry != null ? entry.getURI() : super.getURI();
  }

  @Override
  public String getAdditionalClass() {
    return entry != null ? entry.getAdditionalClass() : super.getAdditionalClass();
  }

  @Override
  public boolean isHidden() {
    return entry != null ? entry.isHidden() : super.isHidden();
  }

  @Override
  public void setHidden(boolean hidden) {
    if (entry != null) {
      entry.setHidden(hidden);
    } else {
      super.setHidden(hidden);
    }
  }

  @Override
  public List<Match> getHighlights() {
    return entry != null ? entry.getHighlights() : super.getHighlights();
  }

  @Override
  public void setHighlights(List<Match> highlights) {
    if (entry != null) {
      entry.setHighlights(highlights);
    } else {
      super.setHighlights(highlights);
    }
  }

  @Override
  public boolean run(Mode mode) {
    return entry != null ? entry.run(mode) : super.run(mode);
  }
}

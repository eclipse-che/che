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

import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.filters.Match;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Base data class for all Quick open entries.
 *
 * @author Evgen Vidolob
 */
public class QuickOpenEntry {
  private static int IDs;

  private final String id;

  private List<Match> labelHighlights;
  private boolean hidden;

  public QuickOpenEntry() {
    this(Collections.<Match>emptyList());
  }

  public QuickOpenEntry(List<Match> labelHighlights) {
    id = "" + IDs++;
    this.labelHighlights = labelHighlights;
  }

  /**
   * A identifier for the entry
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /** The label of the entry. */
  public String getLabel() {
    return null;
  }

  /**
   * Detail information. Optional.
   *
   * @return
   */
  public String getDetail() {
    return null;
  }

  /**
   * The icon of the entry.
   *
   * @return
   */
  public SVGResource getIcon() {
    return null;
  }

  /**
   * A another description, can be shown right to the label
   *
   * @return
   */
  public String getDescription() {
    return null;
  }

  public String getURI() {
    return null;
  }

  /**
   * Additional CSS class name.
   *
   * @return
   */
  public String getAdditionalClass() {
    return null;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public List<Match> getHighlights() {
    return labelHighlights;
  }

  public void setHighlights(List<Match> highlights) {
    labelHighlights = highlights;
  }

  /**
   * Called when the entry selected for opening. Returns a boolean value indicating if an action was
   * performed.
   *
   * @param mode gives an indicating if the element is previewed or opened.
   * @return
   */
  public boolean run(Mode mode) {
    return false;
  }

  public enum Mode {
    OPEN,
    PREVIEW
  }
}

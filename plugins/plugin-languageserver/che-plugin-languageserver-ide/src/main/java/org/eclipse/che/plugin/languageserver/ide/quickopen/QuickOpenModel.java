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

/** @author Evgen Vidolob */
public class QuickOpenModel {
  private List<QuickOpenEntry> entries;
  private Renderer renderer;

  @SuppressWarnings("unchecked")
  public QuickOpenModel(List<? extends QuickOpenEntry> entries) {
    this.entries = (List<QuickOpenEntry>) entries;
  }

  public List<QuickOpenEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<QuickOpenEntry> entries) {
    this.entries = entries;
  }

  public Renderer getRenderer() {
    return renderer;
  }

  public void setRenderer(Renderer renderer) {
    this.renderer = renderer;
  }

  public boolean run(QuickOpenEntry entry, QuickOpenEntry.Mode mode) {
    return entry.run(mode);
  }
}

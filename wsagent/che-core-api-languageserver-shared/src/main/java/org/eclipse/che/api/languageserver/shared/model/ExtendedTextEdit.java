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
package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.Range;

/** */
public class ExtendedTextEdit {
  private Range range;
  private String newText;
  private String lineText;
  private int inLineStart;
  private int inLineEnd;

  public ExtendedTextEdit() {}

  public ExtendedTextEdit(Range range, String newText) {
    this.range = range;
    this.newText = newText;
  }

  public ExtendedTextEdit(
      Range range, String newText, String lineText, int inLineStart, int inLineEnd) {
    this.range = range;
    this.newText = newText;
    this.lineText = lineText;
    this.inLineStart = inLineStart;
    this.inLineEnd = inLineEnd;
  }

  public Range getRange() {
    return range;
  }

  public void setRange(Range range) {
    this.range = range;
  }

  public String getNewText() {
    return newText;
  }

  public void setNewText(String newText) {
    this.newText = newText;
  }

  public String getLineText() {
    return lineText;
  }

  public void setLineText(String lineText) {
    this.lineText = lineText;
  }

  public int getInLineStart() {
    return inLineStart;
  }

  public void setInLineStart(int inLineStart) {
    this.inLineStart = inLineStart;
  }

  public int getInLineEnd() {
    return inLineEnd;
  }

  public void setInLineEnd(int inLineEnd) {
    this.inLineEnd = inLineEnd;
  }
}

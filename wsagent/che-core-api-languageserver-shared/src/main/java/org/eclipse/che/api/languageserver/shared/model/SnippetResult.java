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

import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;

public class SnippetResult {
  private String snippet;
  private LinearRange linearRange;
  private int lineIndex;
  private LinearRange rangeInSnippet;

  public SnippetResult() {}

  public SnippetResult(
      LinearRange linearRange, String snippet, int lineIndex, LinearRange rangeInSnippet) {
    this.linearRange = linearRange;
    this.snippet = snippet;
    this.lineIndex = lineIndex;
    this.rangeInSnippet = rangeInSnippet;
  }

  public LinearRange getLinearRange() {
    return linearRange;
  }

  public void setLinearRange(LinearRange linearRange) {
    this.linearRange = linearRange;
  }

  public String getSnippet() {
    return snippet;
  }

  public void setSnippet(String snippet) {
    this.snippet = snippet;
  }

  public int getLineIndex() {
    return lineIndex;
  }

  public void setLineIndex(int lineIndex) {
    this.lineIndex = lineIndex;
  }

  public LinearRange getRangeInSnippet() {
    return rangeInSnippet;
  }

  public void setRangeInSnippet(LinearRange rangeInSnippet) {
    this.rangeInSnippet = rangeInSnippet;
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.resources;

import org.eclipse.che.api.project.shared.SearchOccurrence;

/** @author Vitalii Parfonov */
public class SearchOccurrenceImpl implements SearchOccurrence {

  private float score;
  private int endOffset;
  private int startOffset;
  private String phrase;
  private String lineContent;
  private int lineNumber;

  public SearchOccurrenceImpl(SearchOccurrence searchOccurrence) {
    score = searchOccurrence.getScore();
    endOffset = searchOccurrence.getEndOffset();
    startOffset = searchOccurrence.getStartOffset();
    phrase = searchOccurrence.getPhrase();
    lineContent = searchOccurrence.getLineContent();
    lineNumber = searchOccurrence.getLineNumber();
  }

  @Override
  public float getScore() {
    return score;
  }

  @Override
  public void setScore(float score) {
    this.score = score;
  }

  @Override
  public String getPhrase() {
    return phrase;
  }

  @Override
  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  @Override
  public int getEndOffset() {
    return endOffset;
  }

  @Override
  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }

  @Override
  public int getStartOffset() {
    return startOffset;
  }

  @Override
  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }

  @Override
  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  @Override
  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public void setLineContent(String lineContent) {
    this.lineContent = lineContent;
  }

  @Override
  public String getLineContent() {
    return lineContent;
  }
}

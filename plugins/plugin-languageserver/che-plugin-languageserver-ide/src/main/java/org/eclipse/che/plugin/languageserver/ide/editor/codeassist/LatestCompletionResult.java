/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionList;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * Contains the latest completion result retrieved from the completion service.
 *
 * @author Kaloyan Raev
 */
public class LatestCompletionResult {
  public static final LatestCompletionResult NO_RESULT =
      new LatestCompletionResult(null, 0, null, null);

  private TextDocumentIdentifier documentId;
  private int offset;
  private String word;
  private ExtendedCompletionList completionList;

  /**
   * Construct a new LatestCompletionResult
   *
   * @param documentId a text document identifier
   * @param offset an offset position in the document
   * @param word the word at the current position in the document
   * @param completionList a completion list
   */
  public LatestCompletionResult(
      TextDocumentIdentifier documentId,
      int offset,
      String word,
      ExtendedCompletionList completionList) {
    this.documentId = documentId;
    this.offset = offset;
    this.word = word;
    this.completionList = completionList;
  }

  /**
   * Returns the identifier of document used to compute the latest completion result.
   *
   * @return the document identifier
   */
  public TextDocumentIdentifier getDocumentId() {
    return this.documentId;
  }

  /**
   * Returns the offset position in document used to compute the latest completion result.
   *
   * @return the offset
   */
  public int getOffset() {
    return this.offset;
  }

  /**
   * Returns the word at the cursor at the time of computing the latest completion result.
   *
   * @return the word
   */
  public String getWord() {
    return this.word;
  }

  /**
   * Returns the latest completion list DTO object.
   *
   * @return the completion list
   */
  public ExtendedCompletionList getCompletionList() {
    return this.completionList;
  }

  /**
   * Checks if the completion result is still good for the given document position.
   *
   * <p>
   *
   * <p>The following checks are executed:
   *
   * <ol>
   *   <li>A completion result has been retrieved at least once.
   *   <li>The latest completion result is "complete", i.e. the <code>isIncomplete</code> property
   *       is <code>false</code>.
   *   <li>The given document id is the same as in the latest completion result.
   *   <li>The given word starts with the one in the latest completion result.
   *   <li>The difference between the given offset and the one in the latest completion result
   *       matches the respective difference between the words.
   * </ol>
   *
   * Only if all checks are satisfied then the latest completion result can be reused for the given
   * document position.
   *
   * @param documentId a text document identifier
   * @param offset an offset position in the document
   * @param word the word at the current position in the document
   * @return <code>true</code> if the completion result can still be used for the given document
   *     position, <code>false</code> otherwise.
   */
  public boolean isGoodFor(TextDocumentIdentifier documentId, int offset, String word) {
    return completionList != null
        && !completionList.isInComplete()
        && this.documentId != null
        && this.documentId.getUri().equals(documentId.getUri())
        && word.startsWith(this.word)
        && offset - this.offset == word.length() - this.word.length();
  }
}

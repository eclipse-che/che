/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.dto.shared.DTO;

/**
 * @see org.eclipse.che.api.project.shared.SearchOccurrence
 * @author Vitalii Parfonov
 */
@DTO
public interface SearchOccurrenceDto extends SearchOccurrence {

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param score
   * @return
   */
  SearchOccurrenceDto withScore(float score);

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param phrase
   * @return
   */
  SearchOccurrenceDto withPhrase(String phrase);

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param endOffset
   */
  SearchOccurrenceDto withEndOffset(int endOffset);

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param startOffset
   */
  SearchOccurrenceDto withStartOffset(int startOffset);

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param lineNumber
   * @return
   */
  SearchOccurrenceDto withLineNumber(int lineNumber);

  /**
   * @see org.eclipse.che.api.project.shared.SearchOccurrence
   * @param lineContent
   * @return
   */
  SearchOccurrenceDto withLineContent(String lineContent);
}

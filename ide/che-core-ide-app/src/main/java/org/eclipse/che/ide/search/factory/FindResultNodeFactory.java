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
package org.eclipse.che.ide.search.factory;

import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.search.presentation.FoundItemNode;
import org.eclipse.che.ide.search.presentation.FoundOccurrenceNode;

/**
 * Factory for creating tree element for the result of searching.
 *
 * @author Valeriy Svydenko
 */
public interface FindResultNodeFactory {
  /**
   * Create new instance of {@link FoundItemNode}.
   *
   * @param searchItemReference the result of the search operation
   * @param request requested text to search
   * @return new instance of {@link FoundItemNode}
   */
  FoundItemNode newFoundItemNode(SearchItemReference searchItemReference, String request);

  /**
   * Create new instance of {@link FoundOccurrenceNode}.
   *
   * @param searchOccurrence linforamtion about occurrence
   * @param itemPath path to the file resource
   * @return new instance of {@link FoundOccurrenceNode}
   */
  FoundOccurrenceNode newFoundOccurrenceNode(SearchOccurrence searchOccurrence, String itemPath);
}

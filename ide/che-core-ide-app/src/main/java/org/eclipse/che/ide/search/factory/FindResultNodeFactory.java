/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.search.factory;

import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.search.presentation.FindResultGroupNode;
import org.eclipse.che.ide.search.presentation.FoundItemNode;
import org.eclipse.che.ide.search.presentation.FoundOccurrenceNode;

import java.util.List;

/**
 * Factory for creating tree element for the result of searching.
 *
 * @author Valeriy Svydenko
 */
public interface FindResultNodeFactory {
    /**
     * Create new instance of {@link FindResultGroupNode}.
     *
     * @param result
     *         list of files with occurrences
     * @param request
     *         requested text to search
     * @return new instance of {@link FindResultGroupNode}
     */
    FindResultGroupNode newResultNode(List<SearchResult> result, String request);

    FoundItemNode newFoundItemNode(SearchResult searchResult, String request);

    FoundOccurrenceNode newFoundOccurrenceNode(SearchOccurrence searchOccurrence, String itemPath);
}

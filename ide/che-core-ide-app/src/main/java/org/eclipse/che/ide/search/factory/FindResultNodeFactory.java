/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.search.factory;

import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.search.presentation.FindResultGroupNode;

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
    FindResultGroupNode newResultNode(Resource[] result, String request);
}

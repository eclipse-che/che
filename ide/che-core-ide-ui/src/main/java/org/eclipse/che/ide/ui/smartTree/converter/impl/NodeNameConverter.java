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
package org.eclipse.che.ide.ui.smartTree.converter.impl;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.converter.NodeConverter;

/**
 * Convert node into String representing. Need for speed search.
 *
 * @author Vlad Zhukovskyi
 */
public class NodeNameConverter implements NodeConverter<Node, String> {

    /** {@inheritDoc} */
    @Override
    public String convert(Node node) {
        return node.getName();
    }
}

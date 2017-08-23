/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree.converter.impl;

import org.eclipse.che.ide.ui.smartTree.converter.NodeConverter;
import org.eclipse.che.ide.ui.smartTree.data.Node;

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

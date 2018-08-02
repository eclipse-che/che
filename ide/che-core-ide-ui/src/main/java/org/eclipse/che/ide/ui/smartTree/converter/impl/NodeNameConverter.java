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

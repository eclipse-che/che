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
package org.eclipse.che.ide.ui.smartTree.converter;

import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Mechanism to convert node or specific part of node into another representation.
 *
 * @author Vlad Zhukovskyi
 */
public interface NodeConverter<N extends Node, D> {
  /**
   * Convert node into another type, e.g. String or other type.
   *
   * @param node node to be converted.
   * @return instance of {@link D}
   */
  D convert(N node);
}

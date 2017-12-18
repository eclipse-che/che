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
package org.eclipse.che.ide.ui.smartTree;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Current ID provider is responsible for providing a unique identification for a specified node.
 *
 * @author Vlad Zhukovskyi
 */
public interface NodeUniqueKeyProvider extends UniqueKeyProvider<Node> {
  /** {@inheritDoc} */
  @NotNull
  String getKey(@NotNull Node item);
}

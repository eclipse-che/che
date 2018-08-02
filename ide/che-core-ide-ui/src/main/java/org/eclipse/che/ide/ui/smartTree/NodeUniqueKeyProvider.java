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

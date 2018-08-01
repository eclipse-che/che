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
package org.eclipse.che.ide.processes;

import elemental.html.SpanElement;
import org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType;

/**
 * Strategy for rendering process tree node.
 *
 * <p>To registered own rendering strategy developer need to create a new implementation of this
 * interface. Register in gin module:
 *
 * <pre>
 *     GinMapBinder.newMapBinder(binder(), String.class, ProcessTreeNodeRenderStrategy.class)
 *                 .addBinding("nodeType")
 *                 .to(ProcessTreeNodeRenderStrategyImplementation.class);
 * </pre>
 *
 * {@code nodeType} is represents by {@link ProcessNodeType#getStringValue()}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.11.0
 */
public interface ProcessTreeNodeRenderStrategy {

  /**
   * Render span element for given {@code candidate}.
   *
   * @param candidate candidate to be shown in the tree widget
   * @return rendered span element
   */
  SpanElement renderSpanElementFor(ProcessTreeNode candidate);
}

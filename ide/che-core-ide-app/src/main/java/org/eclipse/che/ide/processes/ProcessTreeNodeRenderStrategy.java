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

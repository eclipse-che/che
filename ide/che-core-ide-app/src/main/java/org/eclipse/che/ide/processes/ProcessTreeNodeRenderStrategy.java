package org.eclipse.che.ide.processes;

import elemental.html.SpanElement;

import org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType;

/**
 * Strategy for rendering process tree node.
 * <p>
 * To registered own rendering strategy developer need to create a new implementation of this interface. Register in gin module:
 * <pre>
 *     GinMapBinder.newMapBinder(binder(), String.class, ProcessTreeNodeRenderStrategy.class)
 *                 .addBinding("nodeType")
 *                 .to(ProcessTreeNodeRenderStrategyImplementation.class);
 * </pre>
 * {@code nodeType} is represents by {@link ProcessNodeType#getStringValue()}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.10.0
 */
public interface ProcessTreeNodeRenderStrategy {

    /**
     * Render span element for given {@code candidate}.
     *
     * @param candidate
     *         candidate to be shown in the tree widget
     * @return rendered span element
     */
    SpanElement renderSpanElementFor(ProcessTreeNode candidate);
}

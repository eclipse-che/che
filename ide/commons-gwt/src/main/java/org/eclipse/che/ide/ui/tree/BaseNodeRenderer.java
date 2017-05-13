package org.eclipse.che.ide.ui.tree;

import elemental.dom.Element;
import elemental.html.SpanElement;

/**
 * Base node renderer which do nothing.
 *
 * @author Vlad Zhukovskyi
 * @since 5.10.0
 */
public abstract class BaseNodeRenderer<D> implements NodeRenderer<D> {
    @Override
    public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
        return null;
    }

    @Override
    public SpanElement renderNodeContents(D data) {
        return null;
    }

    @Override
    public void updateNodeContents(TreeNodeElement<D> treeNode) {

    }
}

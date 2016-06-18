// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.ui.tree;

import org.eclipse.che.ide.util.AnimationController;
import org.eclipse.che.ide.util.CssUtils;
import org.eclipse.che.ide.util.dom.DomUtils;
import org.eclipse.che.ide.util.dom.Elements;

import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import elemental.js.html.JsLIElement;
import elemental.js.html.JsUListElement;

/**
 * Overlay type for the base element for a Node in the tree.
 * <p/>
 * Nodes with no children have no UL element.
 * <p/>
 * Nodes that have children, but that have never been expanded (nodes render
 * lazily on expansion), have an empty UL element.
 * <p/>
 * <pre>
 *
 * <li class="treeNode">
 *   <div class="treeNodeBody">
 *     <div class="expandControl"></div><span class="treeNodeLabel"></span>
 *   </div>
 *   <ul class="childrenContainer">
 *   </ul>
 * </li>
 *
 * </pre>
 */
public class TreeNodeElement<D> extends JsLIElement {

    /**
     * Creates a TreeNodeElement from some data. Should only be called by
     * {@link Tree}.
     *
     * @param <D>
     *         the type of data
     * @param dataAdapter
     *         An {@link NodeDataAdapter} that allows us to visit the
     *         NodeData
     * @return a new {@link TreeNodeElement} created from the supplied data.
     */
    static <D> TreeNodeElement<D> create(
            D data, NodeDataAdapter<D> dataAdapter, NodeRenderer<D> nodeRenderer,
            Tree.Css css, Tree.Resources resources) {

        @SuppressWarnings("unchecked")
        TreeNodeElement<D> treeNode = (TreeNodeElement<D>)Elements.createElement("li", css.treeNode());
        treeNode.setData(data);
        treeNode.setRenderer(nodeRenderer);

        // Associate the rendered node with the underlying model data.
        dataAdapter.setRenderedTreeNode(data, treeNode);

                // Attach the Tree node body.
                DivElement nodeBody = Elements.createDivElement(css.treeNodeBody());
                nodeBody.setAttribute("draggable", "true");
                treeNode.appendChild(nodeBody);

                        // Attach expand node element
                        DivElement expand = Elements.createDivElement();
                        Elements.addClassName(css.expandControl(), expand);
                        nodeBody.appendChild(expand);

                        expand.setInnerHTML(resources.collapsedIcon().getSvg().getElement().getString() +
                                resources.expandedIcon().getSvg().getElement().getString());
                        ((Element)expand.getChildNodes().item(1)).getStyle().setDisplay("none");

                        SpanElement nodeContent = nodeRenderer.renderNodeContents(data);
                        Elements.addClassName(css.treeNodeLabel(), nodeContent);
                        nodeBody.appendChild(nodeContent);

                // Attach the Tree node children.
                treeNode.ensureChildrenContainer(dataAdapter, css);

        return treeNode;
    }

    protected TreeNodeElement() {
    }

    public final void updateLeafOffset(Element parent) {
        if (!parent.hasAttribute("___depth")) {
            return;
        }

        try {
            int depth = Integer.parseInt(parent.getAttribute("___depth"));

            Element expandElement = (Element)getNodeBody().getChildren().item(0);
            expandElement.getStyle().setMarginLeft("" + (depth * 8) + "px");

            if (! hasChildNodes()) {
                return;
            }
            final JsUListElement childrenContainer = getChildrenContainer();
            if (childrenContainer != null) {
                getChildrenContainer().setAttribute("___depth", "" + (depth + 1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends the specified child to this TreeNodeElement's child container
     * element.
     *
     * @param child
     *         The {@link TreeNodeElement} we want to append to as a child of
     *         this node.
     */
    public final void addChild(
            NodeDataAdapter<D> dataAdapter, TreeNodeElement<D> child, Tree.Css css) {
        ensureChildrenContainer(dataAdapter, css);
        getChildrenContainer().appendChild(child);
    }

    /**
     * @return The associated NodeData that is a bound to this node when it was
     *         rendered.
     */
    public final native D getData() /*-{
        return this.__nodeData;
    }-*/;

    /**
     * Nodes with no children have no UL element, only a DIV for the Node body.
     *
     * @return whether or not this node has children.
     */
    public final boolean hasChildrenContainer() {
        int length = this.getChildren().getLength();

        assert (length < 3) : "TreeNodeElement has more than 2 children of its root element!";

        return (length == 2);
    }

    public final boolean isActive(Tree.Css css) {
        return CssUtils.containsClassName(getSelectionElement(), css.active());
    }

    /** Checks whether or not this node is open. */
    public final native boolean isOpen() /*-{
        return !!this.__nodeOpen;
    }-*/;

    private void setOpen(Tree.Css css, boolean isOpen) {
        if (isOpen != isOpen()) {
            if (isOpen) {
                ((Element)getExpandControl().getChildNodes().item(0)).getStyle().setDisplay("none");
                ((Element)getExpandControl().getChildNodes().item(1)).getStyle().setDisplay("block");
            } else {
                ((Element)getExpandControl().getChildNodes().item(0)).getStyle().setDisplay("block");
                ((Element)getExpandControl().getChildNodes().item(1)).getStyle().setDisplay("none");
            }

            setOpenImpl(isOpen);
            getRenderer().updateNodeContents(this);
        }
    }

    private native void setOpenImpl(boolean isOpen) /*-{
        this.__nodeOpen = isOpen;
    }-*/;

    public final boolean isSelected(Tree.Css css) {
        return CssUtils.containsClassName(getSelectionElement(), css.selected());
    }

    /** Makes this node into a leaf node. */
    public final void makeLeafNode(Tree.Css css) {
        getExpandControl().setClassName(css.expandControl() + " " + css.leafIcon());
        if (hasChildrenContainer()) {
            DomUtils.removeFromParent(getChildrenContainer());
        }
    }

    /**
     * Removes this node from the {@link Tree} and breaks the back reference from
     * the underlying node data.
     */
    public final void removeFromTree() {
        DomUtils.removeFromParent(this);
    }

//    /** Sets whether or not this node has the active node styling applied. */
//    public final void setActive(boolean isActive, Tree.Css css) {
//        // Show the selection on the element returned by the node renderer
//        Element selectionElement = getSelectionElement();
//        CssUtils.setClassNameEnabled(selectionElement, css.active(), isActive);
//        if (isActive) {
//            selectionElement.focus();
//        }
//    }

    /** Sets whether or not this node has the selected styling applied. */
    public final void setSelected(boolean selected, boolean active, Tree.Css css) {
        Elements.removeClassName(css.selected(), getSelectionElement());
        Elements.removeClassName(css.selectedInactive(), getSelectionElement());

        if (selected) {
            Elements.addClassName(active ? css.selected() : css.selectedInactive(), getSelectionElement());
            getSelectionElement().setAttribute("selected", "true");
        } else {
            getSelectionElement().removeAttribute("selected");
        }
    }

    /** Sets whether or not this node is the active drop target. */
    public final void setIsDropTarget(boolean isDropTarget, Tree.Css css) {
        CssUtils.setClassNameEnabled(this, css.isDropTarget(), isDropTarget);
    }

    /**
     * Closes the current node. Must have children if you call this!
     *
     * @param css
     *         The {@link Tree.Css} instance that contains relevant selector
     *         names.
     * @param shouldAnimate
     *         whether to do the animation or not
     */
    final void closeNode(NodeDataAdapter<D> dataAdapter, Tree.Css css, AnimationController closer,
                         boolean shouldAnimate) {
        ensureChildrenContainer(dataAdapter, css);

        Element expandControl = getExpandControl();

        assert (hasChildrenContainer() && CssUtils.containsClassName(expandControl,
                                                                     css.expandControl())) :
                "Tried to close a node that didn't have an expand control";

        setOpen(css, false);

        Element childrenContainer = getChildrenContainer();
        if (shouldAnimate) {
            closer.hide(childrenContainer);
        } else {
            closer.hideWithoutAnimating(childrenContainer);
        }
    }

    /**
     * You should call hasChildren() before calling this method. This will throw
     * an exception if a Node is a leaf node.
     *
     * @return The UL element containing children of this TreeNodeElement.
     */
    final JsUListElement getChildrenContainer() {
        return (JsUListElement)this.getChildren().item(1);
    }

    public final SpanElement getNodeLabel() {
        return (SpanElement)getNodeBody().getChildren().item(1);
    }

    /**
     * Expands the current node. Must have children if you call this!
     *
     * @param css
     *         The {@link Tree.Css} instance that contains relevant selector
     *         names.
     * @param shouldAnimate
     *         whether to do the animation or not
     */
    final void openNode(NodeDataAdapter<D> dataAdapter, Tree.Css css, AnimationController opener,
                        boolean shouldAnimate) {
        ensureChildrenContainer(dataAdapter, css);

        Element expandControl = getExpandControl();

        assert (hasChildrenContainer() && CssUtils.containsClassName(expandControl,
                                                                     css.expandControl())) :
                "Tried to open a node that didn't have an expand control";

        setOpen(css, true);

        Element childrenContainer = getChildrenContainer();
        if (shouldAnimate) {
            opener.show(childrenContainer);
        } else {
            opener.showWithoutAnimating(childrenContainer);
        }
    }

    /**
     * If this node does not have a children container, but has children data,
     * then we coerce a children container into existence.
     */
    final void ensureChildrenContainer(NodeDataAdapter<D> dataAdapter, Tree.Css css) {
        if (!hasChildrenContainer()) {
            D data = getData();
            if (dataAdapter.hasChildren(data)) {
                Element childrenContainer = Elements.createElement("ul", css.childrenContainer());
                this.appendChild(childrenContainer);
                childrenContainer.getStyle().setDisplay(CSSStyleDeclaration.Display.NONE);
                ((Element)getExpandControl().getChildNodes().item(0)).getStyle().setDisplay("block");
                ((Element)getExpandControl().getChildNodes().item(1)).getStyle().setDisplay("none");

            } else {
                getExpandControl().setClassName(css.expandControl() + " " + css.leafIcon());
            }
        }
    }

    private Element getExpandControl() {
        return (Element)getNodeBody().getChildren().item(0);
    }

    /**
     * @return The node body element that contains the expansion control and the
     *         node contents.
     */
    private Element getNodeBody() {
        return (Element)getChildren().item(0);
    }

    final Element getSelectionElement() {
        return getNodeBody();
    }

    /**
     * Stashes associate NodeData as an expand on our element, and also sets up a
     * reverse mapping.
     *
     * @param data
     *         The NodeData we want to associate with this node element.
     */
    private native void setData(D data) /*-{
        this.__nodeData = data;
    }-*/;

    private native NodeRenderer<D> getRenderer() /*-{
        return this.__nodeRenderer;
    }-*/;

    private native void setRenderer(NodeRenderer<D> renderer) /*-{
        this.__nodeRenderer = renderer;
    }-*/;
}

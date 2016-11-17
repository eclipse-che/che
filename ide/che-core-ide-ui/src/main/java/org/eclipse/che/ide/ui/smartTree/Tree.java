/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.smartTree;

import com.google.common.base.Predicate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.impl.FocusImpl;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.MutableNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.event.BeforeCollapseNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.BeforeCollapseNodeEvent.HasBeforeCollapseItemHandlers;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent.HasBeforeExpandNodeHandlers;
import org.eclipse.che.ide.ui.smartTree.event.BlurEvent;
import org.eclipse.che.ide.ui.smartTree.event.BlurEvent.HasBlurHandlers;
import org.eclipse.che.ide.ui.smartTree.event.CancellableEvent;
import org.eclipse.che.ide.ui.smartTree.event.CollapseNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.CollapseNodeEvent.HasCollapseItemHandlers;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent.HasExpandItemHandlers;
import org.eclipse.che.ide.ui.smartTree.event.FocusEvent;
import org.eclipse.che.ide.ui.smartTree.event.NodeAddedEvent;
import org.eclipse.che.ide.ui.smartTree.event.NodeAddedEvent.HasNodeAddedEventHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent.StoreAddHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler;
import org.eclipse.che.ide.ui.smartTree.event.internal.NativeTreeEvent;
import org.eclipse.che.ide.ui.smartTree.handler.GroupingHandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.PresentationRenderer;
import org.eclipse.che.ide.ui.status.ComponentWithEmptyStatus;
import org.eclipse.che.ide.ui.status.EmptyStatus;
import org.eclipse.che.ide.ui.status.StatusText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.che.ide.util.dom.Elements.disableTextSelection;

/**
 * Widget which support displaying hierarchical data. Internal data stores in special storage {@code NodeStorage}.
 * <p>
 * {@code UniqueKeyProvider} provides interface between external node and internal storage by generating specified
 * unique id.
 * <p>
 * {@code NodeStorage} provides mechanism for storing external nodes. It doesn't response for displaying the last
 * one's. Each node in {@code NodeStorage} wraps into internal object, called {@code NodeDescriptor} which has
 * various fields which represent node state, e.g. expand/load state, rendered DOM elements, etc. Applied changes
 * in {@code NodeStorage} immediately affects on the view representation.
 * <p>
 * {@code NodeLoader} provides mechanism for loading node's children.
 * <p>
 * {@code SelectionModel} provides mechanism for controlling selection on the current tree widget.
 * <p>
 * Communication between {@code Tree}, {@code NodeStorage}, {@link NodeLoader} and {@code SelectionModel} is
 * organized by own internal event bus.
 * <p>
 * Following snippet displays how to initialize tree widget:
 * <pre>
 *     NodeUniqueKeyProvider idProvider = new NodeUniqueKeyProvider() {
 *         public String getKey(@NotNull Node item) {
 *             return String.valueOf(item.hashCode());
 *         }
 *     }
 *
 *     NodeStorage nodeStorage = new NodeStorage(idProvider);
 *     NodeLoader nodeLoader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
 *
 *     Tree tree = new Tree(nodeStorage, nodeLoader);
 *
 *     //add nodes into tree
 *     tree.getNodeStorage().add(Collections.<Node>emptyList());
 *
 *     FlowPanel panel = new FlowPanel();
 *     panel.add(tree);
 * </pre>
 * <p>
 * By default, each node will be rendered with simple name. If you want to have extended rendered presentation for
 * each node, you should implement {@code Node} with {@code HasPresentation} interface. In this case rendered
 * presentation will include these attributes which you can configure:
 * <ul>
 * <li>SVG Icon</li>
 * <li>User element</li>
 * <li>Presentable text</li>
 * <li>Info text</li>
 * </ul>
 * <p>
 * By `User element` means, that you can provide any {@code com.google.gwt.dom.client.Element} element to display
 * own elements.
 * <p>
 * By `Presentable text` it means that node has base name to be displayed.
 * By `Info text` it means that node may has additional text to be displayed next to `Presentable text`.
 * <p>
 * Presentable text can be styled by providing valid css snippet as parameter {@code org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation#setPresentableTextCss(java.lang.String)}
 * The same rule is suitable for info text.
 * <p>
 * Following snippet displays how to configure node presentation:
 * <pre>
 *     ...
 *     public void updatePresentation(@NotNull NodePresentation presentation) {
 *         presentation.setPresentableCss("color:black;font-weight:bold;text-decoration:underline;");
 *         presentation.setPresentableText("node name");
 *         presentation.setPresentableIcon(icon); //SVGResource
 *         presentation.setInfoText("into text");
 *         presentation.setInfoTextCss("color:grey;font-size:0.5em;");
 *         presentation.setInfoTextWrapper(Pair.of("(", ")"));
 *         presentation.setUserElement(Document.get.createDivElement()); //any html element
 *     }
 *     ...
 * </pre>
 *
 * @author Vlad Zhukovskiy
 * @see NodeStorage
 * @see NodeLoader
 * @see SelectionModel
 * @see Node
 * @see HasPresentation
 * @see org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation
 * @see UniqueKeyProvider
 * @see NodeUniqueKeyProvider
 */
public class Tree extends FocusWidget implements HasBeforeExpandNodeHandlers,
                                                 HasExpandItemHandlers,
                                                 HasBeforeCollapseItemHandlers,
                                                 HasCollapseItemHandlers,
                                                 ComponentWithEmptyStatus,
                                                 HasBlurHandlers,
                                                 HasNodeAddedEventHandlers {

    private static final String NULL_NODE_MSG         = "Node should not be a null";
    private static final String NULL_NODE_STORAGE_MSG = "Node should not be a null";

    /**
     * Flag that instruct tree widget always expand non-leaf nodes.
     */
    private boolean autoExpand = false;

    /**
     * Flag that instruct tree widget always load non-leaf nodes, but not expand one's.
     */
    private boolean autoLoad = false;

    /**
     * Flag that instruct tree widget to track onMouseOver event and set hover class to node.
     */
    private boolean trackMouseOver = true;

    /**
     * Flag that instruct tree widget to use auto selection.
     */
    private boolean autoSelect = true;

    /**
     * Flag that instruct tree widget to allow user selection on nodes element.
     */
    private boolean allowTextSelection = false;

    /**
     * Flag that instruct tree widget to disable browser native context menu.
     */
    private boolean disableNativeContextMenu = true;

    /**
     * Internal node storage. Contains information about each loaded node with additional information, such as expand state of DOM.
     */
    private NodeStorage nodeStorage;

    /**
     * Component that performs loading node's children.
     */
    private NodeLoader nodeLoader;

    /**
     * Selection model component.
     */
    private SelectionModel selectionModel;

    /**
     * External context menu invocation handler.
     */
    private ContextMenuInvocationHandler contextMenuInvocationHandler;

    /**
     * Node presentation renderer.
     */
    private PresentationRenderer<Node> presentationRenderer;

    /**
     * Tree's root widget element.
     */
    private Element rootContainer;

    /**
     * Component which show some configurable text if there is a condition that tree widget doesn't have any element.
     */
    private EmptyStatus<Tree> emptyStatus;

    /**
     * View for the tree. View allow manipulating with node DOM elements.
     */
    private TreeView view;

    /**
     * Tree style configuration.
     */
    private TreeStyles treeStyles;

    /**
     * Internal temporary storage for node ID and node descriptor.
     */
    private Map<String, NodeDescriptor> nodesByDom;

    /**
     * @see FocusImpl#getFocusImplForPanel()
     */
    private FocusImpl focusImpl;

    /**
     * @see FocusImpl#createFocusable()
     */
    private Element focusEl;

    /**
     * Delayed task to update visual state of specific node.
     */
    private DelayedTask updateTask;

    /**
     * Experimental feature that allow tree to simulate "Go Into" on non-leaf node if one's allow this by checking Node#supportGoInto().
     */
    private GoInto goInto;

    private GroupingHandlerRegistration storeHandlers;

    private boolean focusConstrainScheduled = false;

    private boolean focused = false;

    public Tree(NodeStorage nodeStorage, NodeLoader nodeLoader) {
        this(nodeStorage, nodeLoader, GWT.<TreeStyles>create(TreeStyles.class));
    }

    public Tree(NodeStorage nodeStorage, NodeLoader nodeLoader, EmptyStatus<Tree> emptyStatus) {
        this(nodeStorage, nodeLoader, GWT.<TreeStyles>create(TreeStyles.class), emptyStatus);
    }

    public Tree(NodeStorage nodeStorage, NodeLoader nodeLoader, TreeStyles treeStyles) {
        this(nodeStorage, nodeLoader, treeStyles, null);
    }

    public Tree(NodeStorage nodeStorage, NodeLoader nodeLoader, TreeStyles treeStyles, EmptyStatus<Tree> emptyStatus) {
        checkNotNull(nodeStorage);
        checkNotNull(nodeLoader);
        checkNotNull(treeStyles);

        this.treeStyles = treeStyles;
        this.treeStyles.styles().ensureInjected();
        this.nodesByDom = new HashMap<>();
        this.focusImpl = FocusImpl.getFocusImplForPanel();
        this.storeHandlers = new GroupingHandlerRegistration();

        ensureTreeElement();
        ensureFocusElement();

        setNodeStorage(nodeStorage);
        setNodeLoader(nodeLoader);
        setSelectionModel(new SelectionModel());
        setGoInto(new DefaultGoInto());
        setView(new TreeView());
        setAllowTextSelection(false);

        disableBrowserContextMenu(true);

        // use as default
        if (emptyStatus == null) {
            emptyStatus = new StatusText<>();
        }
        this.emptyStatus = emptyStatus;
        this.emptyStatus.init(this, new Predicate<Tree>() {
            @Override
            public boolean apply(@Nullable Tree tree) {
                return tree.getNodeStorage().getRootCount() == 0;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addBeforeCollapseHandler(BeforeCollapseNodeEvent.BeforeCollapseNodeHandler handler) {
        return addHandler(handler, BeforeCollapseNodeEvent.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addBeforeExpandHandler(BeforeExpandNodeEvent.BeforeExpandNodeHandler handler) {
        return addHandler(handler, BeforeExpandNodeEvent.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addCollapseHandler(CollapseNodeEvent.CollapseNodeHandler handler) {
        return addHandler(handler, CollapseNodeEvent.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addExpandHandler(ExpandNodeEvent.ExpandNodeHandler handler) {
        return addHandler(handler, ExpandNodeEvent.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addNodeAddedHandler(NodeAddedEvent.NodeAddedEventHandler handler) {
        return addHandler(handler, NodeAddedEvent.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerRegistration addBlurHandler(BlurEvent.BlurHandler handler) {
        return addHandler(handler, BlurEvent.getType());
    }

    public final void setView(TreeView view) {
        this.view = view;
        view.bind(this);
    }

    /**
     * Returns true if checked node was expanded.
     *
     * @param node node to check
     * @return true if node was expanded, otherwise false
     */
    public boolean isExpanded(Node node) {
        checkNotNull(node, NULL_NODE_MSG);

        NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
        return nodeDescriptor != null && nodeDescriptor.isExpanded();
    }

    /**
     * Returns true if checked node is leaf.
     *
     * @param node node to check
     * @return true if node is leaf, otherwise false
     */
    public boolean isLeaf(Node node) {
        checkNotNull(node, NULL_NODE_MSG);

        return node.isLeaf();
    }

    /**
     * Returns internal node descriptor object.
     * Descriptor contains expand, load/loading state, rendered DOM elements and parent/children relationship.
     *
     * @param node node to process
     * @return instance of {@link NodeDescriptor} or <code>null</code> if one's doesn't exists
     */
    public NodeDescriptor getNodeDescriptor(Node node) {
        checkNotNull(node, NULL_NODE_MSG);
        checkNotNull(nodeStorage, NULL_NODE_STORAGE_MSG);

        return nodeStorage.getNodeMap().get(getUniqueId(node));
    }

    /**
     * Returns internal node descriptor object.
     * Descriptor contains expand, load/loading state, rendered DOM elements and parent/children relationship.
     * <p>
     * User can pass any internal element, such as joint, icon, presentable or info text and method will find
     * the nearest suitable parent element which can be casted into node.
     * <p>
     * Method should be used to find nodes after handling DOM events, such as Event.ONCLICK or Event.ONDBLCLICK.
     *
     * @param target DOM element, e.g. joint, icon, presentable of info text
     * @return instance of {@link NodeDescriptor} or <code>null</code> if one's doesn't exists
     */
    public NodeDescriptor getNodeDescriptor(Element target) {
        checkNotNull(target);

        Element nodeElement = getNearestParentElement(target, treeStyles.styles().rootContainer());
        if (!(nodeElement == null || isNullOrEmpty(nodeElement.getId()))) {
            return nodesByDom.get(nodeElement.getId());
        }

        return null;
    }

    /**
     * Returns unique internal ID for the specific node.
     * ID is retrieving not from the stored nodes. It forms dynamically from {@link UniqueKeyProvider}.
     *
     * @param node node to process
     * @return unique ID or null if no ID was found
     * @see UniqueKeyProvider
     */
    public String getUniqueId(Node node) {
        checkNotNull(node, NULL_NODE_MSG);

        return nodeStorage.getKeyProvider().getKey(node);
    }

    /**
     * Set expanded state for the specific node.
     * Expand performs only for the first nested level.
     *
     * @param node   node to expand/collapse
     * @param expand true if node should be expanded, otherwise false
     * @see Tree#setExpanded(Node, boolean, boolean)
     */
    public void setExpanded(Node node, boolean expand) {
        checkNotNull(node, NULL_NODE_MSG);

        setExpanded(node, expand, false);
    }

    /**
     * Set expanded state for the specific node.
     *
     * @param node   node to expand/collapse
     * @param expand true if node should be expanded, otherwise false
     * @param deep   true if nested nodes should also be expanded, otherwise false
     */
    public void setExpanded(Node node, boolean expand, boolean deep) {
        checkNotNull(node, NULL_NODE_MSG);

        if (expand) {
            // make item visible by expanding parents
            List<Node> list = new ArrayList<>();
            Node p = node;
            while ((p = nodeStorage.getParent(p)) != null) {
                NodeDescriptor nodeDescriptor = getNodeDescriptor(p);
                if (nodeDescriptor == null || !nodeDescriptor.isExpanded()) {
                    list.add(p);
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                Node item = list.get(i);
                setExpanded(item, true, false);
            }
        }


        NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
        if (nodeDescriptor == null) {
            return;
        }

        if (!isAttached()) {
            nodeDescriptor.setExpand(expand);
            return;
        }

        if (expand) {
            onExpand(node, nodeDescriptor, deep);
        } else {
            onCollapse(node, nodeDescriptor, deep);
        }
    }

    /**
     * Set leaf state for the specific node.
     * To be able to change leaf state, node should implement {@link MutableNode}.
     * <p>
     * Useful for dynamically changing node state, e.g. to show members on leaf node.
     *
     * @param node node to process
     * @param leaf true if node should become to be a leaf, otherwise false
     * @return true if node changed own state, otherwise false
     */
    public boolean setLeaf(Node node, boolean leaf) {
        checkNotNull(node, NULL_NODE_MSG);

        if (node instanceof MutableNode) {
            NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
            if (nodeDescriptor != null) {
                nodeDescriptor.setLeaf(leaf);
                refresh(node);
                return true;
            }
        }

        return false;
    }

    /**
     * Set custom {@link NodeLoader} component.
     *
     * @param nodeLoader instance of {@link NodeLoader}
     * @see NodeLoader
     */
    public final void setNodeLoader(NodeLoader nodeLoader) {
        if (this.nodeLoader != null) {
            this.nodeLoader.bindTree(null);
        }

        this.nodeLoader = nodeLoader;
        if (nodeLoader != null) {
            nodeLoader.bindTree(this);
        }
    }

    /**
     * Instruct tree to automatically load all nodes but not expand them.
     *
     * @param autoLoad true if nodes should be automatically loaded
     */
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    /**
     * Instruct tree to automatically expand all non-leaf nodes.
     * Be carefully with configuring this field. Because if tree has many nodes that loads asynchronously it may affect performance.
     *
     * @param autoExpand true if nodes should be automatically expanded
     */
    public void setAutoExpand(boolean autoExpand) {
        this.autoExpand = autoExpand;
    }

    /**
     * Instruct tree to automatically setup selection when one's is rendering.
     *
     * @param autoSelect true if first node should be selected after node rendering
     */
    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    /**
     * Returns list of current root nodes.
     * Before return method check if tree is in "Go Into" mode, if it is, then method will return only one node that is in "Go Into" mode.
     * Otherwise all root nodes will be returned.
     *
     * @return unmodifiable list of root nodes
     */
    public List<Node> getRootNodes() {
        List<Node> nodes = goInto.isActive() ? singletonList(goInto.getLastUsed()) : nodeStorage.getRootItems();
        return unmodifiableList(nodes);
    }

    /**
     * Returns tree view for the specified internal operations with one's.
     *
     * @return instance of {@link TreeView}
     */
    public TreeView getView() {
        return view;
    }

    /**
     * Collect all children for specified list of nodes. Collected nodes may be filtered by visible status.
     *
     * @param parent      list of nodes, which children should be collected
     * @param onlyVisible true if only visible children should be collected, otherwise false
     * @return unmodifiable list of children
     */
    public List<Node> getAllChildNodes(List<Node> parent, boolean onlyVisible) {
        List<Node> list = new ArrayList<>();
        for (Node node : parent) {
            list.add(node);
            if (!onlyVisible || getNodeDescriptor(node).isExpanded()) {
                findChildren(node, list, onlyVisible);
            }
        }
        return unmodifiableList(list);
    }

    /**
     * Render node with specified depth.
     * Rendered node doesn't affect existed rendered nodes in internal storage
     *
     * @param node  node to render
     * @param depth node depth
     * @return rendered DOM element
     */
    public Element renderNode(Node node, int depth) {
        checkNotNull(node, NULL_NODE_MSG);

        return getPresentationRenderer().render(node, register(node), getJoint(node), depth);
    }

    /**
     * Returns joint element for the specified node.
     *
     * @param node node to process
     * @return instance of {@link org.eclipse.che.ide.ui.smartTree.Tree.Joint} element
     */
    public Joint getJoint(Node node) {
        if (node == null) {
            return Joint.NONE;
        }

        if (isLeaf(node)) {
            return Joint.NONE;
        }

        if (getNodeDescriptor(node) != null && getNodeDescriptor(node).isLoaded() && nodeStorage.getChildCount(node) == 0) {
            return Joint.NONE;
        }

        return getNodeDescriptor(node).isExpanded() ? Joint.EXPANDED : Joint.COLLAPSED;
    }

    /**
     * Scroll focus element into specific node.
     *
     * @param node node to scroll
     */
    public void scrollIntoView(Node node) {
        checkNotNull(node, NULL_NODE_MSG);
        NodeDescriptor descriptor = getNodeDescriptor(node);
        if (descriptor == null) {
            return;
        }
        Element container = descriptor.getNodeContainerElement();
        if (container == null) {
            return;
        }
        container.scrollIntoView();
        focusEl.getStyle().setLeft((nodeStorage.getDepth(node) - 1) * 16, Style.Unit.PX);
        focusEl.getStyle().setTop(container.getOffsetTop(), Style.Unit.PX);
    }

    /**
     * Sets window focus to current tree.
     */
    public void focus() {
        focusImpl.focus(focusEl);
    }

    /**
     * Returns instance of {@link NodeStorage}.
     *
     * @return instance of {@link NodeStorage}
     * @see NodeStorage
     */
    public NodeStorage getNodeStorage() {
        return nodeStorage;
    }

    /**
     * Returns instance of {@link NodeLoader}.
     *
     * @return instance of {@link NodeLoader}
     * @see NodeLoader
     */
    public NodeLoader getNodeLoader() {
        return nodeLoader;
    }

    /**
     * Returns instance of {@link SelectionModel}.
     *
     * @return instance of {@link SelectionModel}
     * @see SelectionModel
     */
    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Sets custom {@link NodeStorage}.
     *
     * @param nodeStorage custom {@link NodeStorage}
     */
    public final void setNodeStorage(NodeStorage nodeStorage) {
        checkNotNull(nodeStorage, NULL_NODE_STORAGE_MSG);

        if (this.nodeStorage != null) {
            storeHandlers.removeHandler();
            if (isOrWasAttached()) {
                clear();
            }
        }

        this.nodeStorage = nodeStorage;

        Handler handler = new Handler();
        storeHandlers.add(nodeStorage.addStoreAddHandler(handler));
        storeHandlers.add(nodeStorage.addStoreUpdateHandler(handler));
        storeHandlers.add(nodeStorage.addStoreRemoveHandler(handler));
        storeHandlers.add(nodeStorage.addStoreDataChangeHandler(handler));
        storeHandlers.add(nodeStorage.addStoreClearHandler(handler));
        storeHandlers.add(nodeStorage.addStoreSortHandler(handler));

        if (getSelectionModel() != null) {
            getSelectionModel().bindStorage(nodeStorage);
        }
        if (isOrWasAttached()) {
            renderChildren(null);
        }
    }

    /**
     * Set custom {@link SelectionModel}.
     *
     * @param selectionModel custom {@link SelectionModel}
     */
    public final void setSelectionModel(SelectionModel selectionModel) {
        checkNotNull(selectionModel);

        if (this.selectionModel != null) {
            this.selectionModel.bindTree(null);
        }
        this.selectionModel = selectionModel;
        selectionModel.bindTree(this);
    }

    /**
     * Clear tree. Calling this method doesn't remove existed nodes from the internal storage. It affects only visual representation of
     * nodes. To remove nodes from internal storage, method org.eclipse.che.ide.ui.smartTree.TreeNodeStorage#clear() should be called.
     */
    public void clear() {
        if (isOrWasAttached()) {
            Element container = getContainer(null);
            if (container != null) {
                container.setInnerHTML("");
            }

            Map<String, NodeDescriptor> nodeMap = getNodeStorage().getNodeMap();
            for (NodeDescriptor nodeDescriptor : nodeMap.values()) {
                nodeDescriptor.clearElements();
            }

            nodesByDom.clear();
            if (isAttached()) {
                moveFocus(getContainer(null));
            }
            getEmptyStatus().paint(); //draw empty label
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBrowserEvent(Event event) {
        switch (event.getTypeInt()) {
            case Event.ONCLICK:
                onClick(event);
                break;
            case Event.ONDBLCLICK:
                onDoubleClick(event);
                break;
            case Event.ONSCROLL:
                onScroll(event);
                break;
            case Event.ONFOCUS:
                onFocus(event);
                break;
            case Event.ONBLUR:
                onBlur(event);
                break;
            case Event.ONCONTEXTMENU:
                if (disableNativeContextMenu) {
                    event.preventDefault();
                }
                onRightClick(event);
                break;
        }
        view.onEvent(event);

        // we are not calling super so must fire dom events
        DomEvent.fireNativeEvent(event, this, this.getElement());
    }

    /**
     * Returns {@code true} if nodes are highlighted on mouse over.
     *
     * @return true if enabled
     */
    public boolean isTrackMouseOver() {
        return trackMouseOver;
    }

    /**
     * True to highlight nodes when the mouse is over (defaults to {@code true}).
     *
     * @param trackMouseOver {@code true} to highlight nodes on mouse over
     */
    public void setTrackMouseOver(boolean trackMouseOver) {
        this.trackMouseOver = trackMouseOver;
    }

    /**
     * Allow to select inner text.
     *
     * @param enable true if text is allowed to be selected, otherwise false
     */
    public void setAllowTextSelection(boolean enable) {
        allowTextSelection = enable;
        if (isAttached()) {
            disableTextSelection(getRootContainer(), !enable);
        }
    }

    /**
     * Refresh visual representation for the specific node.
     *
     * @param node node to be refreshed
     */
    public void refresh(Node node) {
        checkNotNull(node, NULL_NODE_MSG);

        if (!isOrWasAttached()) {
            return;
        }

        NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
        if (view.getRootContainer(nodeDescriptor) == null) {
            return;
        }

        if (!(node instanceof HasPresentation)) {
            return;
        }

        ((HasPresentation)node).getPresentation(true); //update presentation
        Element el = getPresentationRenderer().render(node, nodeDescriptor.getDomId(), getJoint(node), nodeStorage.getDepth(node) - 1);
        view.onElementChanged(nodeDescriptor, el);
    }

    /**
     * Disable or enable browser context menu.
     *
     * @param disable true if browser context menu should be disabled, otherwise false
     */
    public void disableBrowserContextMenu(boolean disable) {
        disableNativeContextMenu = disable;
        if (disable) {
            sinkEvents(Event.ONCONTEXTMENU);
        }
    }

    /**
     * Returns true if it is allowed to select text inside tree.
     *
     * @return true if allowed, otherwise false
     */
    public boolean isAllowTextSelection() {
        return allowTextSelection;
    }

    /**
     * Set external context menu invocation handler.
     * Need to allow use context menu outside of tree widget.
     *
     * @param invocationHandler context menu invocation handler
     */
    public void setContextMenuInvocationHandler(ContextMenuInvocationHandler invocationHandler) {
        checkNotNull(invocationHandler);

        contextMenuInvocationHandler = invocationHandler;
    }

    /**
     * Reset registered invocation handler if such exists.
     */
    public void resetContextMenuInvocationHandler() {
        if (contextMenuInvocationHandler != null) {
            contextMenuInvocationHandler = null;
        }
    }

    /**
     * Returns registered invocation handler or null if such handler hadn't registered before.
     *
     * @return context menu invocation handler
     */
    public ContextMenuInvocationHandler getContextMenuInvocationHandler() {
        return contextMenuInvocationHandler;
    }

    /**
     * Returns tree style configuration.
     *
     * @return tree style configuration.
     */
    public TreeStyles getTreeStyles() {
        return treeStyles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmptyStatus getEmptyStatus() {
        return emptyStatus;
    }

    /**
     * Expands all non-leaf node in current tree.
     * Be careful with this method. In case if you have nodes, which children may be loaded asynchronously it may perform powerful
     * load to your server and completely reduce your performance. It useful for those tree, which has static data model.
     */
    public void expandAll() {
        for (Node node : nodeStorage.getRootItems()) {
            setExpanded(node, true, true);
        }
    }


    /**
     * Collapse all expanded nodes.
     */
    public void collapseAll() {
        for (Node node : nodeStorage.getRootItems()) {
            setExpanded(node, false, true);
        }
    }

    /**
     * Returns presentation node renderer. Need for unusual operations with node presentations.
     *
     * @return {@link DefaultPresentationRenderer} in case if no presentation renderer was registered before
     */
    public PresentationRenderer<Node> getPresentationRenderer() {
        if (presentationRenderer == null) {
            presentationRenderer = new DefaultPresentationRenderer<>(treeStyles);
        }
        return presentationRenderer;
    }

    /**
     * Set custom node presentation renderer.
     * Useful to override default mechanism of node rendering. With it you can provide for example custom attributes for each rendered
     * node.
     *
     * @param presentationRenderer presentation renderer
     */
    public void setPresentationRenderer(PresentationRenderer<Node> presentationRenderer) {
        this.presentationRenderer = presentationRenderer;
    }

    /**
     * Set custom implementation of "Go Into" mode.
     *
     * @param goInto {@link GoInto} processor
     * @see GoInto
     */
    public final void setGoInto(GoInto goInto) {
        this.goInto = goInto;
        this.goInto.bind(this);
    }

    /**
     * Returns registered processor for "Go Into" feature.
     *
     * @return {@link GoInto} processor
     * @see GoInto
     */
    public GoInto getGoInto() {
        return goInto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onAttach() {
        boolean isOrWasAttached = isOrWasAttached();
        super.onAttach();
        if (nodeStorage == null) {
            throw new IllegalStateException("Cannot attach a tree without a store");
        }

        if (!isOrWasAttached) {
            onAfterFirstAttach();
        }

        update();
    }

    protected void update() {
        if (updateTask == null) {
            updateTask = new DelayedTask() {
                @Override
                public void onExecute() {
                    int count = getVisibleRowCount();
                    if (count > 0) {
                        List<Node> rootItems = getRootNodes();
                        List<Node> visible = getAllChildNodes(rootItems, true);
                        int[] vr = getVisibleRows(visible, count);

                        for (int i = vr[0]; i <= vr[1]; i++) {
                            if (goInto.isActive()) {
                                //constraint node indention
                                int goIntoDirDepth = nodeStorage.getDepth(goInto.getLastUsed());
                                int currentNodeDepth = nodeStorage.getDepth(visible.get(i));

                                view.onDepthUpdated(getNodeDescriptor(visible.get(i)), currentNodeDepth - goIntoDirDepth);
                            }
                            if (!isRowRendered(i, visible)) {
                                Node parent = nodeStorage.getParent(visible.get(i));
                                Element html = renderNode(visible.get(i), nodeStorage.getDepth(parent));
                                Element rootContainer = view.getRootContainer(getNodeDescriptor(visible.get(i)));
                                rootContainer.replaceChild(rootContainer.getFirstChildElement(), html);
                            } else {
                                refresh(visible.get(i));
                            }
                        }
                    }
                }
            };
        }
        updateTask.delay(view.getScrollDelay());
    }

    protected Element getContainer(Node node) {
        if (node == null) {
            return rootContainer;
        }

        NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
        if (nodeDescriptor != null) {
            return view.getDescendantsContainer(nodeDescriptor);
        }

        return null;
    }

    protected void moveFocus(Element selectedElem) {
        if (selectedElem == null) {
            return;
        }

        int containerLeft = getAbsoluteLeft();
        int containerTop = getAbsoluteTop();

        int left = selectedElem.getAbsoluteLeft() - containerLeft;
        int top = selectedElem.getAbsoluteTop() - containerTop;

        int width = selectedElem.getOffsetWidth();
        int height = selectedElem.getOffsetHeight();

        if (width == 0 || height == 0) {
            focusEl.getStyle().setTop(0, Style.Unit.PX);
            focusEl.getStyle().setLeft(0, Style.Unit.PX);
            return;
        }

        focusEl.getStyle().setTop(top, Style.Unit.PX);
        focusEl.getStyle().setLeft(left, Style.Unit.PX);
    }

    protected void toggle(Node node) {
        NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
        if (nodeDescriptor != null) {
            if (nodeDescriptor.isExpanded()) {
                setExpanded(node, false, true);
            } else {
                setExpanded(node, true);
            }
        }
    }

    /**
     * Completely redraws the children of the given parent (or all items if parent is null), throwing away details like
     * currently expanded nodes, etc.
     *
     * @param parent the parent of the items to redraw
     */
    private void redraw(Node parent) {
        if (!isOrWasAttached()) {
            return;
        }

        if (parent == null) {
            clear();
            renderChildren(null);

            if (autoSelect) {
                Node child = nodeStorage.getChild(0);
                if (child != null) {
                    getSelectionModel().setSelection(singletonList(child));
                }
            }
        } else {
            NodeDescriptor nodeDescriptor = getNodeDescriptor(parent);
            nodeDescriptor.setLoaded(true);
            nodeDescriptor.setLoading(false);

            if (isLeaf(nodeDescriptor.getNode())) {
                return;
            }

            if (isExpanded(parent)) {
                setExpanded(parent, false, true);
                Element container = getContainer(parent);
                container.setInnerHTML("");
                nodeDescriptor.setChildrenRendered(false);
                setExpanded(parent, true, nodeDescriptor.isExpandDeep());
            } else {
                if (nodeDescriptor.isChildrenRendered()) {
                    Element container = getContainer(parent);
                    container.setInnerHTML("");
                    nodeDescriptor.setChildrenRendered(false);
                }
                setExpanded(parent, true, nodeDescriptor.isExpandDeep());
            }
        }
    }

    private void onExpand(Node node, NodeDescriptor nodeDescriptor, boolean deep) {
        if (isLeaf(node)) {
            return;
        }

        if (nodeDescriptor.isLoading()) { //node may have been already requested for expanding
            return;
        }

        if (!nodeDescriptor.isExpanded() && nodeLoader != null && (!nodeDescriptor.isLoaded())) {
            nodeStorage.removeChildren(node);
            nodeDescriptor.setExpand(true);
            nodeDescriptor.setExpandDeep(deep);
            nodeDescriptor.setLoading(true);
            view.onLoadChange(nodeDescriptor, true);
            nodeLoader.loadChildren(node);
            return;
        }

        if (!fireCancellableEvent(new BeforeExpandNodeEvent(node))) {
            if (deep) {
                nodeDescriptor.setExpandDeep(false);
            }

            return;
        }

        if (!nodeDescriptor.isExpanded()) {
            nodeDescriptor.setExpanded(true);

            if (!nodeDescriptor.isChildrenRendered()) {
                renderChildren(node);
                nodeDescriptor.setChildrenRendered(true);
            }

            //direct expand on the view
            view.expand(nodeDescriptor);

            update();
            fireEvent(new ExpandNodeEvent(node));
        }

        if (deep) {
            setExpandChildren(node, true);
        }
    }

    private void setExpandChildren(Node node, boolean expand) {
        for (Node child : nodeStorage.getChildren(node)) {
            setExpanded(child, expand, true);
        }
    }

    private void renderChildren(Node parent) {
        int depth = nodeStorage.getDepth(parent);
        List<Node> children = parent == null ? nodeStorage.getRootItems() : nodeStorage.getChildren(parent);
        if (children.size() == 0) {
            emptyStatus.paint();
            return;
        }

        Element container = getContainer(parent);

        if (container == null) {
            return;
        }

        for (Node child : children) {
            Element element = renderNode(child, depth);
            container.appendChild(element);
        }

        for (Node child : children) {
            NodeDescriptor nodeDescriptor = getNodeDescriptor(child);
            if (autoExpand) {
                setExpanded(child, true);
            } else if (nodeDescriptor.isExpand() && !isLeaf(nodeDescriptor.getNode())) {
                nodeDescriptor.setExpand(false);
                setExpanded(child, true);
            } else if (nodeLoader != null) {
                if (autoLoad) {
                    if (nodeLoader.mayHaveChildren(child)) {
                        nodeLoader.loadChildren(child);
                    }
                }
            } else if (autoLoad) {
                renderChildren(child);
            }
        }

        if (parent == null) {
            ensureFocusElement();
        }
        update();
    }

    private void onCollapse(Node node, NodeDescriptor nodeDescriptor, boolean deep) {
        if (nodeDescriptor.isExpanded() && fireCancellableEvent(new BeforeCollapseNodeEvent(node))) {
            nodeDescriptor.setExpanded(false);
            view.collapse(nodeDescriptor);

            fireEvent(new CollapseNodeEvent(node));
        }

        nodeDescriptor.setLoaded(false);

        for (Node toRemove : nodeStorage.getAllChildren(node)) {
            nodeStorage.remove(toRemove);
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                update();
            }
        });
    }

    private String register(Node node) {
        String id = getUniqueId(node);
        if (nodeStorage.getNodeMap().containsKey(id)) {
            NodeDescriptor nodeDescriptor = nodeStorage.getNodeMap().get(id);
            if (nodeDescriptor.getDomId() == null || nodeDescriptor.getDomId().isEmpty()) {
                String domId = Document.get().createUniqueId();
                nodeDescriptor.setDomId(domId);
            }
            nodeDescriptor.reset();
            nodeDescriptor.clearElements();
            nodesByDom.put(nodeDescriptor.getDomId(), nodeDescriptor);
            return nodeDescriptor.getDomId();
        } else {
            NodeDescriptor nodeDescriptor = nodeStorage.wrap(node);
            String domId = Document.get().createUniqueId();
            nodeDescriptor.setDomId(domId);

            nodesByDom.put(nodeDescriptor.getDomId(), nodeDescriptor);
            return domId;
        }
    }

    private void unregister(Node node) {
        if (node != null) {
            NodeDescriptor nodeDescriptor = nodeStorage.getNodeMap().remove(getUniqueId(node));
            if (nodeDescriptor != null) {
                nodesByDom.remove(nodeDescriptor.getDomId());
                nodeDescriptor.clearElements();
                nodeStorage.getNodeMap().remove(nodeStorage.getKeyProvider().getKey(node));
            }
        }

    }

    private boolean fireCancellableEvent(GwtEvent<?> event) {
        fireEvent(event);
        if (event instanceof CancellableEvent) {
            return !((CancellableEvent)event).isCancelled();
        }
        return true;
    }

    private void ensureTreeElement() {
        DivElement element = Document.get().createDivElement();
        element.addClassName(treeStyles.styles().tree());
        setElement(element);
    }

    private void ensureFocusElement() {
        if (focusEl != null) {
            focusEl.removeFromParent();
        }
        focusEl = getElement().appendChild(focusImpl.createFocusable());
        focusEl.addClassName(treeStyles.styles().noFocusOutline());
        if (focusEl.hasChildNodes()) {
            focusEl.getFirstChildElement().addClassName(treeStyles.styles().noFocusOutline());
            Style focusElStyle = focusEl.getFirstChildElement().getStyle();
            focusElStyle.setBorderWidth(0, Style.Unit.PX);
            focusElStyle.setFontSize(1, Style.Unit.PX);
            focusElStyle.setPropertyPx("lineHeight", 1);
        }
        focusEl.getStyle().setLeft(0, Style.Unit.PX);
        focusEl.getStyle().setTop(0, Style.Unit.PX);
        focusEl.getStyle().setPosition(Style.Position.ABSOLUTE);

        //subscribe for Event.FOCUSEVENTS
        int bits = DOM.getEventsSunk((Element)focusEl.cast()); //do not remove redundant cast, GWT tests will fail
        DOM.sinkEvents((Element)focusEl.cast(), bits | Event.FOCUSEVENTS);
    }

    private boolean isRowRendered(int i, List<Node> visible) {
        Element e = view.getRootContainer(getNodeDescriptor(visible.get(i)));
        return e != null && e.getFirstChild().hasChildNodes();
    }

    private int getVisibleRowCount() {
        int rh = view.getCalculatedRowHeight();
        int visibleHeight = getElement().getOffsetHeight();
        return (int)((visibleHeight < 1) ? 0 : Math.ceil(visibleHeight / rh));
    }

    private void findChildren(Node parent, List<Node> list, boolean onlyVisible) {
        for (Node child : nodeStorage.getChildren(parent)) {
            final NodeDescriptor descriptor = getNodeDescriptor(child);
            if (descriptor == null) {
                continue;
            }

            list.add(child);

            if (!onlyVisible || descriptor.isExpanded()) {
                findChildren(child, list, onlyVisible);
            }
        }
    }


    private int[] getVisibleRows(List<Node> visible, int count) {
        int sc = getElement().getScrollTop();
        int start = (int)(sc == 0 ? 0 : Math.floor(sc / view.getCalculatedRowHeight()) - 1);
        int first = Math.max(start, 0);
        int last = Math.min(start + count + 2, visible.size() - 1);
        return new int[] {first, last};
    }

    private Element getRootContainer() {
        return getElement();
    }

    private void onAdd(StoreAddEvent event) {
        for (Node child : event.getNodes()) {
            register(child);
        }
        if (isOrWasAttached()) {
            Node parent = nodeStorage.getParent(event.getNodes().get(0));

            final Element container = getContainer(parent);
            final int index = event.getIndex();

            if (parent == null) {
                for (Node child : event.getNodes()) {
                    if (index == 0) {
                        container.insertFirst(renderNode(child, 0));
                    } else if (index == getNodeStorage().getRootCount() - event.getNodes().size()) {
                        com.google.gwt.dom.client.Node lastChild = container.getLastChild();
                        container.insertAfter(renderNode(child, 0), lastChild);
                    } else {
                        container.insertBefore(renderNode(child, 0), container.getChild(index));
                    }
                    scrollIntoView(child);
                }
            } else {
                NodeDescriptor descriptor = getNodeDescriptor(parent);
                if (descriptor != null && descriptor.isChildrenRendered()) {
                    int parentDepth = nodeStorage.getDepth(parent);

                    int parentChildCount = nodeStorage.getChildCount(parent);
                    for (Node child : event.getNodes()) {
                        if (!descriptor.isExpanded() && nodeStorage.getChildCount(descriptor.getNode()) == 1) {
                            setExpanded(descriptor.getNode(), true);
                        }
                        if (index == 0) {
                            container.insertFirst(renderNode(child, parentDepth));
                        } else if (index == parentChildCount - event.getNodes().size()) {
                            com.google.gwt.dom.client.Node lastChild = container.getLastChild();
                            container.insertAfter(renderNode(child, parentDepth), lastChild);
                        } else {
                            container.insertBefore(renderNode(child, parentDepth), container.getChild(index));
                        }
                        scrollIntoView(child);
                    }
                } else {
                    redraw(parent);
                }
            }
            update();

            if (selectionModel.getSelectedNodes().isEmpty() && autoSelect) {
                selectionModel.select(event.getNodes().get(0), false);
            }

            fireEvent(new NodeAddedEvent(event.getNodes()));
        }

        if (!getRootNodes().isEmpty()) {
            emptyStatus.paint();
        }
    }

    @SuppressWarnings("unused") //temporary no need to use event parameter
    private void onClear(StoreClearEvent event) {
        clear();
    }

    private void onDataChanged(StoreDataChangeEvent event) {
        redraw(event.getParent());
    }

    private void onRemove(StoreRemoveEvent se) {
        NodeDescriptor nodeDescriptor = getNodeDescriptor(se.getNode());
        if (nodeDescriptor != null) {
            if (view.getRootContainer(nodeDescriptor) != null) {
                nodeDescriptor.getRootContainer().removeFromParent();
            }
            unregister(se.getNode());

            for (Node child : se.getChildren()) {
                unregister(child);
            }

            Node parent = se.getParent();
            if (parent != null) {
                NodeDescriptor descriptor = getNodeDescriptor(parent);
                if (descriptor != null && descriptor.isExpanded() && nodeStorage.getChildCount(descriptor.getNode()) == 0) {
                    if (fireCancellableEvent(new BeforeCollapseNodeEvent(parent))) {
                        descriptor.setExpanded(false);
                        view.onJointChange(descriptor, Joint.COLLAPSED);
                        fireEvent(new CollapseNodeEvent(parent));
                    }
                }
                moveFocus(nodeDescriptor.getRootContainer());
            }
        }

        if (getRootNodes().isEmpty()) {
            emptyStatus.paint();
        }
    }

    @SuppressWarnings("unused") //temporary no need to use event parameter
    private void onSort(StoreSortEvent se) {
        redraw(null);
    }

    private void onUpdate(StoreUpdateEvent event) {
        for (Node node : event.getNodes()) {
            NodeDescriptor nodeDescriptor = getNodeDescriptor(node);
            if (nodeDescriptor != null) {
                if (nodeDescriptor.getNode() != node) {
                    nodeDescriptor.setNode(node);
                }
            }
        }
    }

    private void onRightClick(Event event) {
        event.preventDefault();
        event.stopPropagation();

        final int x = event.getClientX();
        final int y = event.getClientY();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                getSelectionModel().fireSelectionChange();
                if (contextMenuInvocationHandler != null && disableNativeContextMenu) {
                    contextMenuInvocationHandler.onInvokeContextMenu(x, y);
                }
            }
        });
    }

    private void onFocus(Event event) {
        fireEvent(new FocusEvent());
        focused = true;
    }

    private void onBlur(Event event) {
        fireEvent(new BlurEvent());
        focused = false;
    }

    private void onScroll(Event event) {
        update();
        constrainFocusElement();
    }

    private void constrainFocusElement() {
        if (!focusConstrainScheduled) {
            focusConstrainScheduled = true;
            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    focusConstrainScheduled = false;
                    int scrollLeft = getElement().getScrollLeft();
                    int scrollTop = getElement().getScrollTop();
                    int left = getElement().getOffsetWidth() / 2 + scrollLeft;
                    int top = getElement().getOffsetHeight() / 2 + scrollTop;
                    focusEl.getStyle().setTop(top, Style.Unit.PX);
                    focusEl.getStyle().setLeft(left, Style.Unit.PX);
                }
            });
        }
    }

    private void onDoubleClick(Event event) {
        NodeDescriptor nodeDescriptor = getNodeDescriptor(event.getEventTarget().<Element>cast());
        if (nodeDescriptor == null) {
            return;
        }

        if (nodeDescriptor.isLeaf()) {
            if (nodeDescriptor.getNode() instanceof HasAction) {
                ((HasAction)nodeDescriptor.getNode()).actionPerformed();
            }
        } else {
            toggle(nodeDescriptor.getNode());
        }
    }

    private void onClick(Event event) {
        NativeTreeEvent e = event.cast();
        NodeDescriptor node = getNodeDescriptor((Element)event.getEventTarget().cast());
        if (node != null) {
            Element jointEl = view.getJointContainer(node);
            if (jointEl != null && e.within(jointEl)) {
                toggle(node.getNode());
            }
        }

        focus();
    }

    private void onAfterFirstAttach() {
        rootContainer = getRootContainer();

        getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);

        renderChildren(null);


        sinkEvents(Event.ONSCROLL | Event.ONCLICK | Event.ONDBLCLICK | Event.MOUSEEVENTS | Event.KEYEVENTS);
    }

    private native Element getNearestParentElement(Element target, String selector) /*-{
        function findAncestor(el, cls) {
            while ((el = el.parentElement) && !el.classList.contains(cls));
            return el;
        }

        return findAncestor(target, selector);
    }-*/;

    /**
     * Describes joint element. By joint element it means
     * expand/collapse control, which may have one of three
     * state <code>collapsed</code>, <code>expanded</code>
     * and <code>hidden</code>.
     */
    public enum Joint {
        COLLAPSED(1), EXPANDED(2), NONE(0);

        private int value;

        Joint(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    /**
     * Handler which delegates Event.ONCONTEXTMENU to external code.
     */
    public interface ContextMenuInvocationHandler {
        /**
         * Handle Event.ONCONTEXTMENU event.
         */
        void onInvokeContextMenu(int x, int y);
    }

    private class Handler implements StoreAddHandler,
                                     StoreClearHandler,
                                     StoreDataChangeHandler,
                                     StoreRemoveHandler,
                                     StoreUpdateHandler,
                                     StoreSortHandler {
        @Override
        public void onAdd(StoreAddEvent event) {
            Tree.this.onAdd(event);
        }

        @Override
        public void onClear(StoreClearEvent event) {
            Tree.this.onClear(event);
        }

        @Override
        public void onDataChange(StoreDataChangeEvent event) {
            Tree.this.onDataChanged(event);
        }

        @Override
        public void onRemove(StoreRemoveEvent event) {
            Tree.this.onRemove(event);
        }

        @Override
        public void onSort(StoreSortEvent event) {
            Tree.this.onSort(event);
        }

        @Override
        public void onUpdate(StoreUpdateEvent event) {
            Tree.this.onUpdate(event);
        }
    }
}

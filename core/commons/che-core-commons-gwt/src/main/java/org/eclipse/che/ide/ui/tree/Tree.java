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

import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.js.dom.JsElement;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.mvp.CompositeView;
import org.eclipse.che.ide.mvp.UiComponent;
import org.eclipse.che.ide.util.AnimationController;
import org.eclipse.che.ide.util.CssUtils;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.ide.util.dom.DomUtils;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.dom.MouseGestureListener;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventImpl;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A tree widget that is capable of rendering any tree data structure whose node
 * data type is specified in the class parameterization.
 * <p/>
 * Users of this widget must specify an appropriate
 * {@link NodeDataAdapter} and
 * {@link NodeRenderer}.
 * <p/>
 * The DOM structure for a tree is a recursive structure of the following form
 * (note that class names will be obfuscated at runtime, and are just specified
 * in human readable form for documentation purposes):
 * <p/>
 * <pre>
 *
 *  <ul class="treeRoot childrenContainer">
 *    <li class="treeNode">
 *      <div class="treeNodeBody">
 *       <div class="expandControl"></div>
 *       <span class="treeNodeContents"></span>
 *      </div>
 *      <ul class="childrenContainer">
 *       ...
 *       ...
 *      </ul>
 *    </li>
 *  </ul>
 *
 * </pre>
 */
public class Tree<D> extends UiComponent<Tree.View<D>> implements IsWidget {

    /** Static factory method for obtaining an instance of the Tree. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <NodeData> Tree<NodeData> create(Resources resources, NodeDataAdapter<NodeData> dataAdapter,
                                                   NodeRenderer<NodeData> nodeRenderer, final boolean multilevelSelection) {
        final View view = new View(resources);

        final Model<NodeData> model = new Model<NodeData>(dataAdapter, nodeRenderer, resources, multilevelSelection);
        return new Tree<NodeData>(view, model);
    }

    public static <NodeData> Tree<NodeData> create(Resources resources, NodeDataAdapter<NodeData> dataAdapter,
                                                   NodeRenderer<NodeData> nodeRenderer) {
        return create(resources, dataAdapter, nodeRenderer, false);
    }

    private static boolean thisIsTablet = false;


    /** Css selectors applied to DOM elements in the tree. */
    public interface Css extends CssResource {
        String active();

        String childrenContainer();

        String expandControl();

        String isDropTarget();

        String leafIcon();

        String selected();

        String selectedInactive();

        String treeNode();

        String treeNodeBody();

        String treeNodeLabel();

        String treeRoot();
    }

    /** Listener interface for being notified about tree events. */
    public interface Listener<D> {

        void onNodeAction(TreeNodeElement<D> node);

        void onNodeClosed(TreeNodeElement<D> node);

        void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<D> node);

        void onNodeDragStart(TreeNodeElement<D> node, MouseEvent event);

        void onNodeDragDrop(TreeNodeElement<D> node, MouseEvent event);

        void onNodeExpanded(TreeNodeElement<D> node);

        void onNodeSelected(TreeNodeElement<D> node, SignalEvent event);

        void onRootContextMenu(int mouseX, int mouseY);

        void onRootDragDrop(MouseEvent event);

        void onKeyboard(KeyboardEvent event);
    }

    /** A visitor interface to visit nodes of the tree. */
    public interface Visitor<D> {

        /**
         * @return whether to visit a given node. This is useful to prune a subtree
         *         from being visited
         */
        boolean shouldVisit(D node);

        /**
         * Called for nodes that pass the {@link #shouldVisit} check.
         *
         * @param node
         *         the node being iterated
         * @param willVisitChildren
         *         true if the given node has a child that will be
         *         (or has been) visited
         */
        void visit(D node, boolean willVisitChildren);
    }

    /** Instance state for the Tree. */
    public static class Model<D> {
        private final NodeDataAdapter<D> dataAdapter;

        private Listener<D> externalEventDelegate;

        private final NodeRenderer<D> nodeRenderer;

        private D root;

        private final SelectionModel<D> selectionModel;

        private final Resources resources;

        private final AnimationController animator;

        public Model(NodeDataAdapter<D> dataAdapter, NodeRenderer<D> nodeRenderer, Tree.Resources resources) {
            this(dataAdapter, nodeRenderer, resources, false);
        }

        public Model(final NodeDataAdapter<D> dataAdapter, final NodeRenderer<D> nodeRenderer,
                     final Tree.Resources resources, final boolean multilevelSelection) {
            this.dataAdapter = dataAdapter;
            this.nodeRenderer = nodeRenderer;
            this.resources = resources;
            this.selectionModel = new SelectionModel<D>(dataAdapter, resources.treeCss(), multilevelSelection);
            this.animator = new AnimationController.Builder().setCollapse(true).setFade(true).build();
        }

        public NodeDataAdapter<D> getDataAdapter() {
            return dataAdapter;
        }

        public NodeRenderer<D> getNodeRenderer() {
            return nodeRenderer;
        }

        public D getRoot() {
            return root;
        }

        public void setRoot(D root) {
            this.root = root;
        }
    }

    /**
     * Images and Css resources used by the Tree.
     * <p/>
     * In order to theme the Tree, you extend this interface and override
     * {@link Tree.Resources#treeCss()}.
     */
    public interface Resources extends ClientBundle {

        @Source("expandedIcon.svg")
        SVGResource expandedIcon();

        @Source("collapsedIcon.svg")
        SVGResource collapsedIcon();

        // Default Stylesheet.
        @Source({"org/eclipse/che/ide/ui/constants.css", "Tree.css", "org/eclipse/che/ide/api/ui/style.css"})
        Css treeCss();
    }

    /** The view for a Tree is simply a thin wrapper around a ULElement. */
    public static class View<D> extends CompositeView<ViewEvents<D>> {

        /** Base event listener for DOM events fired by elements in the view. */
        private abstract class TreeNodeEventListener implements EventListener {

            private final boolean primaryMouseButtonOnly;

            /** The {@link Duration#currentTimeMillis()} of the most recent click, or 0 */
            private double previousClickMs;

            private Element previousClickTreeNodeBody;

            TreeNodeEventListener(boolean primaryMouseButtonOnly) {
                this.primaryMouseButtonOnly = primaryMouseButtonOnly;
            }

            @Override
            public void handleEvent(Event evt) {
                // Don't even bother to do anything unless we have someone ready to
                // handle events.
                if (getDelegate() == null
                    || (primaryMouseButtonOnly && ((MouseEvent)evt).getButton() != MouseEvent.Button.PRIMARY)) {
                    return;
                }

                Element eventTarget = (Element)evt.getTarget();

                if (CssUtils.containsClassName(eventTarget, css.expandControl())) {
                    onExpansionControlEvent(evt, eventTarget);
                } else {
                    Element treeNodeBody = CssUtils.getAncestorOrSelfWithClassName(eventTarget, css.treeNodeBody());
                    if (treeNodeBody != null) {
                        //this code emulate double click for tablets
                        if (Event.MOUSEDOWN.equals(evt.getType())) {
                            double currentClickMs = Duration.currentTimeMillis();
                            if (currentClickMs - previousClickMs < MouseGestureListener.MAX_CLICK_TIMEOUT_MS
                                && treeNodeBody.equals(previousClickTreeNodeBody)) {

                                if (BrowserUtils.isAndroid() || BrowserUtils.isIPad() ||
                                    BrowserUtils.isIphone()) {
                                    evt.stopPropagation();
                                    evt.preventDefault();
                                    doActionsForDoubleClick(treeNodeBody, evt);
                                }
                                return;
                            } else {
                                this.previousClickMs = currentClickMs;
                                this.previousClickTreeNodeBody = treeNodeBody;
                            }
                        }

                        onTreeNodeBodyChildEvent(evt, treeNodeBody);
                    } else {
                        onOtherEvent(evt);
                    }
                }
            }

            private void doActionsForDoubleClick(Element treeNodeBody, Object evt) {
                SignalEvent signalEvent = SignalEventImpl.create((com.google.gwt.user.client.Event)evt, true);
                // Select the node.
                dispatchNodeSelectedEvent(treeNodeBody, signalEvent, css);

                // Don't dispatch a node action if there is a modifier key depressed.
                if (!(signalEvent.getCommandKey() || signalEvent.getShiftKey())) {
                    dispatchNodeActionEvent(treeNodeBody, css);

                    TreeNodeElement<D> node = getTreeNodeFromTreeNodeBody(treeNodeBody, css);
                    if (node.hasChildrenContainer()) {
                        dispatchExpansionEvent(node, css);
                    }
                }
            }

            /**
             * Catch-all that is called if the target element was not matched to an
             * element rooted at the TreeNodeBody.
             */
            protected void onOtherEvent(Event evt) {
            }

            /**
             * If an event was dispatched by the TreeNodeBody, or one of its children.
             * <p/>
             * IMPORTANT: However, if the event target is the expansion control, do
             * not call this method.
             */
            protected void onTreeNodeBodyChildEvent(Event evt, Element treeNodeBody) {
            }

            /** If an event was dispatched by the ExpansionControl. */
            protected void onExpansionControlEvent(Event evt, Element expansionControl) {
            }
        }

        private final Tree.Css css;

        @SuppressWarnings("unused")
        private final Tree.Resources resources;

        public View(Tree.Resources resources) {
            super(Elements.createElement("ul"));
            this.resources = resources;
            this.css = resources.treeCss();
            getElement().setTabIndex(0);
            getElement().setClassName(resources.treeCss().treeRoot());
            attachEventListeners();
        }

        void attachEventListeners() {

            // There used to be a MOUSEDOWN handler with stopPropagation() and
            // preventDefault() actions, but this badly affected the inline editing
            // experience inside the Tree (e.g. debugger's RemoteObjectTree).

            // Ok. Currently RemoteObjectTree doesn't exist anymore. So, the event can be changed on MOUSEDOWN.

            getElement().addEventListener(Event.MOUSEDOWN, new TreeNodeEventListener(true) {
                @Override
                protected void onTreeNodeBodyChildEvent(Event evt, Element treeNodeBody) {
                    SignalEvent signalEvent = SignalEventImpl.create((com.google.gwt.user.client.Event)evt, true);
                    // Select the node.
                    dispatchNodeSelectedEvent(treeNodeBody, signalEvent, css);
                }

                @Override
                protected void onExpansionControlEvent(Event evt, Element expansionControl) {
                    if (!CssUtils.containsClassName(expansionControl, css.leafIcon())) {
                  /*
                   * they've clicked on the expand control of a tree node that is a
                   * directory (so expand it)
                   */
                        TreeNodeElement<D> treeNode =
                                ((JsElement)expansionControl.getParentElement().getParentElement()).<TreeNodeElement<D>>cast();
                        dispatchExpansionEvent(treeNode, css);
                    }
                }
            }, false);

            getElement().addEventListener(Event.DBLCLICK, new TreeNodeEventListener(true) {
                @Override
                protected void onTreeNodeBodyChildEvent(Event evt, Element treeNodeBody) {
                    SignalEvent signalEvent = SignalEventImpl.create((com.google.gwt.user.client.Event)evt, true);

                    // Select the node.
                    dispatchNodeSelectedEvent(treeNodeBody, signalEvent, css);

                    // Don't dispatch a node action if there is a modifier key depressed.
                    if (!(signalEvent.getCommandKey() || signalEvent.getShiftKey())) {
                        dispatchNodeActionEvent(treeNodeBody, css);

                        TreeNodeElement<D> node = getTreeNodeFromTreeNodeBody(treeNodeBody, css);
                        if (node.hasChildrenContainer()) {
                            dispatchExpansionEvent(node, css);
                        }
                    }
                }

                @Override
                protected void onExpansionControlEvent(Event evt, Element expansionControl) {
                    if (!CssUtils.containsClassName(expansionControl, css.leafIcon())) {
                  /*
                   * they've clicked on the expand control of a tree node that is a
                   * directory (so expand it)
                   */
                        TreeNodeElement<D> treeNode =
                                ((JsElement)expansionControl.getParentElement().getParentElement()).<TreeNodeElement<D>>cast();
                        dispatchExpansionEvent(treeNode, css);
                    }
                }
            }, false);

            getElement().addEventListener(Event.KEYDOWN, new TreeNodeEventListener(false) {
                @Override
                public void handleEvent(Event event) {
                    if (getDelegate() != null) {
                        getDelegate().onKeyBoard((KeyboardEvent)event);
                    }
                }
            }, false);

            getElement().addEventListener(Event.CONTEXTMENU, new TreeNodeEventListener(false) {
                @Override
                public void handleEvent(Event evt) {
                    super.handleEvent(evt);
                    evt.stopPropagation();
                    evt.preventDefault();
                }

                @Override
                protected void onOtherEvent(Event evt) {
                    MouseEvent mouseEvt = (MouseEvent)evt;

                    // This is a click on the root.
                    dispatchOnRootContextMenuEvent(mouseEvt.getClientX(), mouseEvt.getClientY());
                }

                @Override
                protected void onTreeNodeBodyChildEvent(Event evt, Element treeNodeBody) {
                    MouseEvent mouseEvt = (MouseEvent)evt;

                    // Dispatch if eventTarget is the treeNodeBody, or if it is a child
                    // of a treeNodeBody.
                    dispatchContextMenuEvent(mouseEvt.getClientX(), mouseEvt.getClientY(), treeNodeBody, css);
                }
            }, false);

            getElement().addEventListener(Event.FOCUS, new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    if (getDelegate() != null) {
                        getDelegate().onFocus(event);
                    }
                }
            }, false);

            getElement().addEventListener(Event.BLUR, new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    if (getDelegate() != null) {
                        getDelegate().onBlur(event);
                    }
                }
            }, false);
        }

        private void dispatchContextMenuEvent(int mouseX, int mouseY, Element treeNodeBody, Css css) {

            // We assume the click happened on a TreeNodeBody. We walk up one level
            // to grab the treeNode element.
            @SuppressWarnings("unchecked")
            TreeNodeElement<D> treeNode = (TreeNodeElement<D>)treeNodeBody.getParentElement();

            assert (CssUtils.containsClassName(treeNode, css.treeNode())) : "Parent of an expandControl wasn't a TreeNode!";

            getDelegate().onNodeContextMenu(mouseX, mouseY, treeNode);
        }

        private void dispatchExpansionEvent(TreeNodeElement<D> treeNode, Css css) {

            // Is the node opened or closed?
            if (treeNode.isOpen()) {
                getDelegate().onNodeClosed(treeNode);
            } else {

                // We might have set the CSS to say it is closed, but the animation
                // takes a little while. As such, we check to make sure the children
                // container is set to display:none before trying to dispatch an open.
                // Otherwise we can get into an inconsistent state if we click really
                // fast.
                Element childrenContainer = treeNode.getChildrenContainer();
                if (childrenContainer != null /*&& !CssUtils.isVisible(childrenContainer)*/) {
                    getDelegate().onNodeExpanded(treeNode);
                }
            }
        }

        private void dispatchNodeActionEvent(Element treeNodeBody, Css css) {
            getDelegate().onNodeAction(getTreeNodeFromTreeNodeBody(treeNodeBody, css));
        }

        private void dispatchNodeSelectedEvent(Element treeNodeBody, SignalEvent evt, Css css) {
            getDelegate().onNodeSelected(getTreeNodeFromTreeNodeBody(treeNodeBody, css), evt);
        }

        private void dispatchOnRootContextMenuEvent(int mouseX, int mouseY) {
            getDelegate().onRootContextMenu(mouseX, mouseY);
        }

        private TreeNodeElement<D> getTreeNodeFromTreeNodeBody(Element treeNodeBody, Css css) {
            TreeNodeElement<D> treeNode = ((JsElement)treeNodeBody.getParentElement()).<TreeNodeElement<D>>cast();

            assert (CssUtils.containsClassName(treeNode, css.treeNode())) : "Unexpected element when looking for tree node: "
                    + treeNode.toString();

            return treeNode;
        }
    }

    /**
     * Logical events sourced by the Tree's View. Note that these events get
     * dispatched synchronously in our DOM event handlers.
     */
    private interface ViewEvents<D> {
        void onNodeAction(TreeNodeElement<D> node);

        void onNodeClosed(TreeNodeElement<D> node);

        void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<D> node);

        void onDragDropEvent(MouseEvent event);

        void onNodeExpanded(TreeNodeElement<D> node);

        void onNodeSelected(TreeNodeElement<D> node, SignalEvent event);

        void onRootContextMenu(int mouseX, int mouseY);

        void onRootDragDrop(MouseEvent event);

        void onKeyBoard(KeyboardEvent event);

        void onFocus(Event event);

        void onBlur(Event event);
    }

    private class DragDropController {
        private TreeNodeElement<D> targetNode;

        private boolean hadDragEnterEvent;

        private final ScheduledCommand hadDragEnterEventResetter = new ScheduledCommand() {
            @Override
            public void execute() {
                hadDragEnterEvent = false;
            }
        };

        private final Timer hoverToExpandTimer = new Timer() {
            @Override
            public void run() {
                expandNode(targetNode, true, true);
            }
        };

        void handleDragDropEvent(MouseEvent evt) {
            final D rootData = getModel().root;
            final NodeDataAdapter<D> dataAdapter = getModel().getDataAdapter();
            final Css css = getModel().resources.treeCss();

            @SuppressWarnings("unchecked")
            TreeNodeElement<D> node =
                    (TreeNodeElement<D>)CssUtils.getAncestorOrSelfWithClassName((Element)evt.getTarget(), css.treeNode());
            D newTargetData = node != null ? dataAdapter.getDragDropTarget(node.getData()) : rootData;
            if (newTargetData == null) {
                return;
            }
            TreeNodeElement<D> newTargetNode = dataAdapter.getRenderedTreeNode(newTargetData);

            String type = evt.getType();

            if (Event.DRAGSTART.equals(type)) {
                if (getModel().externalEventDelegate != null) {
                    D sourceData = node != null ? node.getData() : rootData;
                    // TODO support multiple folder selection.
                    // We do not support dragging without any folder/file selection.
                    if (sourceData != rootData) {
                        TreeNodeElement<D> sourceNode = dataAdapter.getRenderedTreeNode(sourceData);
                        getModel().externalEventDelegate.onNodeDragStart(sourceNode, evt);
                    }
                }
                return;
            }

            if (Event.DROP.equals(type)) {
                if (getModel().externalEventDelegate != null) {
                    if (newTargetData == rootData) {
                        getModel().externalEventDelegate.onRootDragDrop(evt);
                    } else {
                        getModel().externalEventDelegate.onNodeDragDrop(newTargetNode, evt);
                    }
                }

                clearDropTarget();

            } else if (Event.DRAGOVER.equals(type)) {
                if (newTargetNode != targetNode) {
                    clearDropTarget();

                    if (newTargetNode != null) {
                        // Highlight the node by setting its drop target property
                        targetNode = newTargetNode;
                        targetNode.setIsDropTarget(true, css);

                        if (dataAdapter.hasChildren(newTargetData) && !targetNode.isOpen()) {
                            hoverToExpandTimer.schedule(HOVER_TO_EXPAND_DELAY_MS);
                        }
                    }
                }

            } else if (Event.DRAGLEAVE.equals(type)) {
                if (!hadDragEnterEvent) {
                    // This wasn't part of a DRAGENTER-DRAGLEAVE pair (see below)
                    clearDropTarget();
                }

            } else if (Event.DRAGENTER.equals(type)) {
            /*
             * DRAGENTER comes before DRAGLEAVE, and a deferred command scheduled
             * here will execute after the DRAGLEAVE. We use hadDragEnter to track a
             * paired DRAGENTER-DRAGLEAVE so that we can cleanup when we get an
             * unpaired DRAGLEAVE.
             */
                hadDragEnterEvent = true;
                Scheduler.get().scheduleDeferred(hadDragEnterEventResetter);
            }

            evt.preventDefault();
            evt.stopPropagation();
        }

        private void clearDropTarget() {
            hoverToExpandTimer.cancel();

            if (targetNode != null) {
                targetNode.setIsDropTarget(false, getModel().resources.treeCss());
                targetNode = null;
            }
        }
    }

    private final DragDropController dragDropController = new DragDropController();

    private HTML widget;

    /** Handles logical events sourced by the View. */
    private final ViewEvents<D> viewEventHandler = new ViewEvents<D>() {
        @Override
        public void onNodeAction(final TreeNodeElement<D> node) {
            selectSingleNode(node, true);
        }

        @Override
        public void onNodeClosed(TreeNodeElement<D> node) {
            closeNode(node, true);
        }

        @Override
        public void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<D> node) {
            // Select the node the first
            getModel().selectionModel.contextSelect(node.getData());

            // Display context menu
            if (getModel().externalEventDelegate != null) {
                getModel().externalEventDelegate.onNodeContextMenu(mouseX, mouseY, node);
            }
        }

        @Override
        public void onDragDropEvent(MouseEvent event) {
            dragDropController.handleDragDropEvent(event);
        }

        @Override
        public void onNodeExpanded(TreeNodeElement<D> node) {
            expandNode(node, true, true);
        }

        @Override
        public void onNodeSelected(TreeNodeElement<D> node, SignalEvent event) {
            getSelectionModel().setTreeActive(true);
            selectNode(node.getData(), event, true);
        }

        @Override
        public void onRootContextMenu(int mouseX, int mouseY) {
            if (getModel().externalEventDelegate != null) {
                getModel().externalEventDelegate.onRootContextMenu(mouseX, mouseY);
            }
        }

        @Override
        public void onRootDragDrop(MouseEvent event) {
            if (getModel().externalEventDelegate != null) {
                getModel().externalEventDelegate.onRootDragDrop(event);
            }
        }

        @Override
        public void onKeyBoard(KeyboardEvent event) {
            if (event.getKeyCode() == KeyboardEvent.KeyCode.UP) {
                event.stopPropagation();
                event.preventDefault();
                upArrowPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.DOWN) {
                event.stopPropagation();
                event.preventDefault();
                downArrowPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.HOME) {
                event.stopPropagation();
                event.preventDefault();
                homePressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.END) {
                event.stopPropagation();
                event.preventDefault();
                endPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.PAGE_UP) {
                event.stopPropagation();
                event.preventDefault();
                pageUpPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.PAGE_DOWN) {
                event.stopPropagation();
                event.preventDefault();
                pageDownPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.ENTER) {
                enterPressed(event);

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.RIGHT) {
                event.stopPropagation();
                event.preventDefault();
                rightArrowPressed();

            } else if (event.getKeyCode() == KeyboardEvent.KeyCode.LEFT) {
                event.stopPropagation();
                event.preventDefault();
                leftArrowPressed();

            } else if (getModel().externalEventDelegate != null) {
                getModel().externalEventDelegate.onKeyboard(event);
            }
        }

        @Override
        public void onFocus(Event event) {
            getSelectionModel().updateSelection(true);
        }

        @Override
        public void onBlur(Event event) {
            getSelectionModel().updateSelection(false);
        }

    };

    private static final int HOVER_TO_EXPAND_DELAY_MS = 500;

    private final Tree.Model<D> treeModel;

    /** Constructor. */
    public Tree(View<D> view, Model<D> model) {
        super(view);
        this.treeModel = model;
        getView().setDelegate(viewEventHandler);
    }

    public Tree.Model<D> getModel() {
        return treeModel;
    }

    /**
     * Selects a node in the tree and auto expands the tree to this node.
     *
     * @param nodeData
     *         the node we want to select and expand to.
     * @param dispatchNodeAction
     *         whether or not to notify listeners of the node
     *         action for the selected node.
     */
    public void autoExpandAndSelectNode(D nodeData, boolean dispatchNodeAction) {

        // Expand the tree to the selected element.
        expandPathRecursive(getModel().root, getModel().dataAdapter.getNodePath(nodeData), false);
        // By now the node should have a rendered element.
        TreeNodeElement<D> renderedNode = getModel().dataAdapter.getRenderedTreeNode(nodeData);

        assert (renderedNode != null) : "Expanded selection has a null rendered node!";

        selectSingleNode(renderedNode, dispatchNodeAction);
    }

    /**
     * Selects a node and dispatches event to perform actions on this node.
     *
     * @param node node to select
     * @param dispatchNodeAction dispatch action or not
     */
    private void selectSingleNode(TreeNodeElement<D> node, boolean dispatchNodeAction) {
        getModel().selectionModel.selectSingleNode(node.getData());
        scrollToSelectedElement();
        maybeNotifyNodeActionExternal(node, dispatchNodeAction);
    }

    /**
     * Selects single node and notifies about selecting the node.
     *
     * @param node node to select
     */
    private void selectSingleNode(TreeNodeElement<D> node) {
        getModel().selectionModel.selectSingleNode(node.getData());
        scrollToSelectedElement();
        if (getModel().externalEventDelegate != null) {
            SignalEvent event = SignalEventImpl.DEFAULT_FACTORY.create();
            getModel().externalEventDelegate.onNodeSelected(node, event);
        }
    }

    /**
     * Selects single node and dispatches an event about selecting the node.
     *
     * @param node
     * @param event
     * @param dispatchNodeSelected
     */
    private void selectNode(D node, SignalEvent event, boolean dispatchNodeSelected) {
        getModel().selectionModel.selectNode(node, event);
        scrollToSelectedElement();
        if (dispatchNodeSelected && getModel().externalEventDelegate != null) {
            TreeNodeElement<D> renderedNode = getModel().dataAdapter.getRenderedTreeNode(node);
            getModel().externalEventDelegate.onNodeSelected(renderedNode, event);
        }
    }

    /**
     * Scrolls tree to selected element.
     */
    private void scrollToSelectedElement() {
        if (!getSelectionModel().getSelectedNodes().isEmpty()) {
            D selected = getSelectionModel().getSelectedNodes().get(0);
            TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);
            scrollToElement(asWidget().getElement(), selectedTreeNodeElement.getFirstChild());
        }
    }

    /**
     * Scrolls tree to specified row.
     *
     * @param tree tree element
     * @param element row element
     */
    private native void scrollToElement(JavaScriptObject tree, JavaScriptObject element) /*-{
        var maxTop = tree.getBoundingClientRect().top + tree.clientHeight;
        var elemTop = element.getBoundingClientRect().top;
        var elemHeight = element.getBoundingClientRect().height;

        if (elemTop + elemHeight > maxTop) {
            var diffHeight = elemTop + elemHeight - maxTop;
            tree.scrollTop += diffHeight;
            return;
        }

        if (element.getBoundingClientRect().top < tree.getBoundingClientRect().top) {
            tree.scrollTop -= tree.getBoundingClientRect().top - element.getBoundingClientRect().top;
        }
    }-*/;

    /**
     * Creates a {@link TreeNodeElement}. This does NOT attach said node to the
     * tree. You have to do that manually with {@link TreeNodeElement#addChild}.
     */
    public TreeNodeElement<D> createNode(D nodeData) {
        return TreeNodeElement.create(nodeData, getModel().dataAdapter, getModel().nodeRenderer,
                                      getModel().resources.treeCss(), getModel().resources);
    }

    /** @see: {@link #expandNode(TreeNodeElement, boolean, boolean)}. */
    public void expandNode(TreeNodeElement<D> treeNode) {
        expandNode(treeNode, false, false);
    }

    /**
     * Expands a {@link TreeNodeElement} and renders its children if it "needs
     * to". "Needs to" is defined as whether or not the children have never been
     * rendered before, or if size of the set of rendered children differs from
     * the size of children in the underlying model.
     *
     * @param treeNode
     *         the {@link TreeNodeElement} we are expanding.
     * @param shouldAnimate
     *         whether to animate the expansion
     * @param dispatchNodeExpanded
     *         whether or not to notify listeners of the node
     *         expansion
     */
    private void expandNode(TreeNodeElement<D> treeNode, boolean shouldAnimate, boolean dispatchNodeExpanded) {
        // This is most likely because someone tried to expand root. Ignore it.
        if (treeNode == null) {
            return;
        }

        NodeDataAdapter<D> dataAdapter = getModel().dataAdapter;

        // Nothing to do here.
        if (!dataAdapter.hasChildren(treeNode.getData())) {
            return;
        }

        // Ensure that the node's children container is birthed.
        treeNode.ensureChildrenContainer(dataAdapter, getModel().resources.treeCss());

        List<D> children = dataAdapter.getChildren(treeNode.getData());

        // Maybe render it's children if they aren't already rendered.
        if (treeNode.getChildrenContainer().getChildren().getLength() != children.size()) {

            // Then the model has not been correctly reflected in the UI.
            // Blank the children and render a single level for each.
            treeNode.getChildrenContainer().setInnerHTML("");
            for (int i = 0, n = children.size(); i < n; i++) {
                renderRecursive(treeNode.getChildrenContainer(), children.get(i), 0);
            }
        }

        // Render the node as being opened after the children have been added, so that
        // AnimationController can correctly measure the height of the child container.
        treeNode.openNode(dataAdapter, getModel().resources.treeCss(), getModel().animator, shouldAnimate);

        // Notify listeners of the event.
        if (dispatchNodeExpanded && getModel().externalEventDelegate != null) {
            getModel().externalEventDelegate.onNodeExpanded(treeNode);
        }
    }

    public void closeNode(TreeNodeElement<D> treeNode) {
        closeNode(treeNode, false);
    }

    private void closeNode(TreeNodeElement<D> treeNode, boolean dispatchNodeClosed) {
        if (!treeNode.isOpen()) {
            return;
        }

        treeNode.closeNode(getModel().dataAdapter, getModel().resources.treeCss(), getModel().animator, true);
        if (dispatchNodeClosed && getModel().externalEventDelegate != null) {
            getModel().externalEventDelegate.onNodeClosed(treeNode);
        }
    }

    /**
     * Takes in a list of paths relative to the root, that correspond to nodes in
     * the tree that need to be expanded.
     * <p/>
     * <p>This will try to expand all the given paths recursively, and return the
     * array of paths that could not be fully expanded, i.e. when the leaf that
     * the path points to was not found in the tree. In these cases all the middle
     * nodes that were found in the tree will be expanded though.
     * <p/>
     * <p>The returned array of not expanded paths may be used to save and restore
     * the expansion history.
     *
     * @param paths
     *         array of paths to expand
     * @param dispatchNodeExpanded
     *         whether to dispatch the NodeExpanded event
     * @return array of paths that were not expanded, or were partially expanded
     */
    public List<List<String>> expandPaths(List<List<String>> paths, boolean dispatchNodeExpanded) {
        List<List<String>> notExpanded = new ArrayList<>();
        for (int i = 0, n = paths.size(); i < n; i++) {
            if (!expandPathRecursive(getModel().root, paths.get(i), dispatchNodeExpanded)) {
                notExpanded.add(paths.get(i));
            }
        }
        return notExpanded;
    }

    /**
     * Gets the associated {@link TreeNodeElement} for a given nodeData.
     * <p/>
     * If there is no such node rendered in the tree, then {@code null} is
     * returned.
     */
    public TreeNodeElement<D> getNode(D nodeData) {
        return getModel().getDataAdapter().getRenderedTreeNode(nodeData);
    }

    public Tree.Resources getResources() {
        return getModel().resources;
    }

    public SelectionModel<D> getSelectionModel() {
        return getModel().selectionModel;
    }

    /**
     * Removes a node from the DOM. Does not mutate the the underlying model. That
     * should be already done before calling this method.
     */
    public void removeNode(TreeNodeElement<D> node) {
        if (node == null) {
            return;
        }

        // Remove from the DOM
        node.removeFromTree();

        // Notify the selection model in case it was selected.
        getModel().selectionModel.removeNode(node.getData());
    }

    /** Renders the entire tree starting with the root node. */
    public void renderTree() {
        renderTree(-1);
    }

    /**
     * Renders the tree starting with the root node up until the specified depth.
     * <p/>
     * This will NOT restore any expansions. If you want to re-render the tree
     * obeying previous expansions then,
     *
     * @param depth
     *         integer indicating how deep we should auto-expand. -1 means
     *         render the entire tree.
     */
    public void renderTree(int depth) {
        // Clear the current view.
        Element rootElement = getView().getElement();
        rootElement.setInnerHTML("");
        rootElement.setAttribute("___depth", "0");

        // If the root is not set, we have nothing to render.
        D root = getModel().root;
        if (root == null) {
            return;
        }

        // Root is special in that we don't render a directory for it. Only its
        // children.
        List<D> children = getModel().dataAdapter.getChildren(root);
        for (int i = 0, n = children.size(); i < n; i++) {
            renderRecursive(rootElement, children.get(i), depth);
        }
    }

    /**
     * Replaces the old node in the tree with data representing the subtree rooted
     * where the old node used to be if the old node was rendered.
     * <p/>
     * <p>{@code oldSubtreeData} and {@code incomingSubtreeData} are allowed to be
     * the same node (it will simply get re-rendered).
     * <p/>
     * <p>This methods also tries to preserve the original expansion state. Any
     * path that was expanded before executing this method but could not be
     * expanded after replacing the subtree, will be returned in the result array,
     * so that it could be expanded later using the {@link #expandPaths} method,
     * if needed (for example, if children of the tree are getting populated
     * asynchronously).
     *
     * @param shouldAnimate
     *         if true, the subtree will animate open if it is still open
     * @return array paths that could not be expanded in the new subtree
     */
    public List<List<String>> replaceSubtree(D oldSubtreeData, D incomingSubtreeData, boolean shouldAnimate) {

        // Gather paths that were expanded in this subtree so that we can restore
        // them later after rendering.
        List<List<String>> expandedPaths = gatherExpandedPaths(oldSubtreeData);

        boolean wasRoot = (oldSubtreeData == getModel().root);
        TreeNodeElement<D> oldRenderedNode = null;
        TreeNodeElement<D> newRenderedNode = null;

        if (wasRoot) {

            // We are rendering root! Just render it from the top. We will restore the
            // expansion later.
            getModel().setRoot(incomingSubtreeData);
            renderTree(0);
        } else {
            oldRenderedNode = getModel().dataAdapter.getRenderedTreeNode(oldSubtreeData);

            // If the node does not have a rendered node, then we have nothing to do.
            if (oldRenderedNode == null) {
                expandedPaths.clear();
                return expandedPaths;
            }

            JsElement parentElem = oldRenderedNode.getParentElement();

            // The old node may have been moved from a rendered to a non-rendered
            // state (e.g., into a collapsed folder). In that case, it doesn't have a
            // parent, and we're done here.
            if (parentElem == null) {
                expandedPaths.clear();
                return expandedPaths;
            }

            // Make a new tree node.
            newRenderedNode = createNode(incomingSubtreeData);
            parentElem.insertBefore(newRenderedNode, oldRenderedNode);
            newRenderedNode.updateLeafOffset(parentElem);

            // Remove the old rendered node from the tree.
            DomUtils.removeFromParent(oldRenderedNode);
        }

        // If the old node was the root, or if it and its parents were expanded, then we should
        // attempt to restore expansion.
        boolean shouldExpand = wasRoot;
        if (!wasRoot && oldRenderedNode != null) {
            shouldExpand = true;
            TreeNodeElement<D> curNode = oldRenderedNode;
            while (curNode != null) {
                if (!curNode.isOpen()) {
                    // One of the parents is closed, so we should not expand all paths.
                    shouldExpand = false;
                    break;
                }

                D parentData = getModel().dataAdapter.getParent(curNode.getData());
                curNode = (parentData == null) ? null : getModel().dataAdapter.getRenderedTreeNode(parentData);
            }
        }
        if (shouldExpand) {
            // Animate the top node if it was open. If we should not animate, the newRenderedNode will
            // still be expanded by the call to expandPaths() below.
            if (shouldAnimate && newRenderedNode != null) {
                expandNode(newRenderedNode, true, true);
            }

            // But if it is open, we need to attempt to restore the expansion.
            expandedPaths = expandPaths(expandedPaths, true);
        } else {
            expandedPaths.clear();
        }

        // TODO: Be more surgical about restoring the selection model. We
        // are currently recomputing all selected nodes.
        List<List<String>> selectedPaths = getModel().selectionModel.computeSelectedPaths();
        restoreSelectionModel(selectedPaths);

        return expandedPaths;
    }

    /**
     * Populates the selection model from a list of selected paths if they
     * resolve to nodes in the data model.
     */
    private void restoreSelectionModel(List<List<String>> selectedPaths) {
        getModel().selectionModel.clearSelections();
        for (int i = 0, n = selectedPaths.size(); i < n; i++) {
            D node = getModel().dataAdapter.getNodeByPath(getModel().root, selectedPaths.get(i));
            if (node != null) {
                selectNode(node, null, true);
            }
        }
    }

    /**
     * Receive callbacks for node expansion and node selection.
     *
     * @param externalEventDelegate
     *         The {@link ViewEvents} that will handle the
     *         events.
     */
    public void setTreeEventHandler(Listener<D> externalEventDelegate) {
        getModel().externalEventDelegate = externalEventDelegate;
    }

    /**
     * Gathers all visible nodes of subtree.
     *
     * @param node subtree parent
     * @return array containing all visible nodes of subtree
     */
    public List<TreeNodeElement<D>> getVisibleTreeNodes(TreeNodeElement<D> node) {
        List<TreeNodeElement<D>> nodes = new ArrayList<>();
        nodes.add(node);

        if (node.isOpen() && node.hasChildNodes()) {
            NodeList children = node.getChildrenContainer().getChildNodes();
            for (int ci = 0; ci < children.getLength(); ci++) {
                TreeNodeElement<D> child = (TreeNodeElement<D>)children.item(ci);
                nodes.addAll(getVisibleTreeNodes(child));
            }
        }

        return nodes;
    }

    /**
     * Gathers all visible nodes of the tree.
     *
     * @return array containing all visible nodes of the tree
     */
    public List<TreeNodeElement<D>> getVisibleTreeNodes() {
        List<TreeNodeElement<D>> nodes = new ArrayList<>();
        List<D> rootItems = getModel().dataAdapter.getChildren(getModel().getRoot());
        for (int i = 0; i < rootItems.size(); i++) {
            TreeNodeElement<D> rootTreeNode = getModel().dataAdapter.getRenderedTreeNode(rootItems.get(i));
            nodes.addAll(getVisibleTreeNodes(rootTreeNode));
        }
        return nodes;
    }

    /**
     * Handles pressing Up arrow button.
     */
    public void upArrowPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);

        List<TreeNodeElement<D>> visibleTreeNodes = getVisibleTreeNodes();
        for (int i = 0; i < visibleTreeNodes.size(); i++) {
            TreeNodeElement<D> treeNode = visibleTreeNodes.get(i);
            if (treeNode == selectedTreeNodeElement) {
                if (i > 0) {
                    selectSingleNode(visibleTreeNodes.get(i - 1));
                }
                return;
            }
        }
    }

    /**
     * Handles pressing Down arrow button.
     */
    public void downArrowPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);

        List<TreeNodeElement<D>> visibleTreeNodes = getVisibleTreeNodes();
        for (int i = 0; i < visibleTreeNodes.size(); i++) {
            TreeNodeElement<D> treeNode = visibleTreeNodes.get(i);
            if (treeNode == selectedTreeNodeElement) {
                if (i < visibleTreeNodes.size() - 1) {
                    selectSingleNode(visibleTreeNodes.get(i + 1));
                }
                return;
            }
        }
    }

    /**
     * Selects the root element when pressing HOME button.
     */
    public void homePressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D project = getModel().dataAdapter.getChildren(getModel().getRoot()).get(0);
        TreeNodeElement<D> projectTreeNode = getModel().dataAdapter.getRenderedTreeNode(project);
        selectSingleNode(projectTreeNode);
    }

    /**
     * Selects last element when pressing END button.
     */
    public void endPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        List<TreeNodeElement<D>> visibleTreeNodes = getVisibleTreeNodes();
        selectSingleNode(visibleTreeNodes.get(visibleTreeNodes.size() - 1));
    }

    /**
     * Handles the pressing Page Up button.
     */
    public void pageUpPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);
        int rowHeight = selectedTreeNodeElement.getSelectionElement().getOffsetHeight();

        int index = -1;
        List<TreeNodeElement<D>> visibleTreeNodes = getVisibleTreeNodes();
        for (int i = 0; i < visibleTreeNodes.size(); i++) {
            TreeNodeElement<D> treeNode = visibleTreeNodes.get(i);
            if (treeNode == selectedTreeNodeElement) {
                index = i;
                break;
            }
        }

        if (index <= 0) {
            return;
        }

        int visibleAreaHeight = asWidget().getElement().getClientHeight();
        int visibleRows = visibleAreaHeight / rowHeight;

        if (index > visibleRows) {
            selectSingleNode(visibleTreeNodes.get(index - visibleRows));
        } else {
            selectSingleNode(visibleTreeNodes.get(0));
        }
    }

    /**
     * Handles the pressing Page Up button.
     */
    public void pageDownPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);
        int rowHeight = selectedTreeNodeElement.getSelectionElement().getOffsetHeight();

        int index = -1;
        List<TreeNodeElement<D>> visibleTreeNodes = getVisibleTreeNodes();
        for (int i = 0; i < visibleTreeNodes.size(); i++) {
            TreeNodeElement<D> treeNode = visibleTreeNodes.get(i);
            if (treeNode == selectedTreeNodeElement) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        int visibleAreaHeight = asWidget().getElement().getClientHeight();
        int visibleRows = visibleAreaHeight / rowHeight;

        if (index + visibleRows < visibleTreeNodes.size()) {
            selectSingleNode(visibleTreeNodes.get(index + visibleRows));
        } else {
            selectSingleNode(visibleTreeNodes.get(visibleTreeNodes.size() - 1));
        }
    }

    /**
     * Handles pressing the Enter button.
     * Expands or collapses a folder or opens a file.
     */
    public void enterPressed(KeyboardEvent event) {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);

        if (selectedTreeNodeElement.hasChildrenContainer()) {
            if (selectedTreeNodeElement.isOpen()) {
                closeNode(selectedTreeNodeElement, true);
            } else {
                // Open the folder
                expandNode(selectedTreeNodeElement, true, true);
            }
        } else {
            if (getModel().externalEventDelegate != null) {
                getModel().externalEventDelegate.onKeyboard(event);
            }
        }
    }

    /**
     * Handles pressing the Right arrow button.
     * Does nothing when user selected a file. Expands a folder if the user has selected one.
     * Selects the first child if the folder is already selected and expanded.
     */
    public void rightArrowPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);

        if (selectedTreeNodeElement.hasChildrenContainer()) {
            if (selectedTreeNodeElement.isOpen()) {
                // Select the first child
                NodeList children = selectedTreeNodeElement.getChildrenContainer().getChildNodes();
                if (children.getLength() > 0) {
                    TreeNodeElement<D> firstChild = (TreeNodeElement<D>)children.item(0);
                    selectSingleNode(firstChild);
                }
            } else {
                // Open the folder
                expandNode(selectedTreeNodeElement, true, true);
            }
        }
    }

    /**
     * Handles pressing the Left arrow button.
     * Closes the folder if it's selected and opened, otherwise selects parent.
     */
    public void leftArrowPressed() {
        if (getModel().getRoot() == null ||
            getSelectionModel().getSelectedNodes().isEmpty() ||
            getModel().dataAdapter.getChildren(getModel().getRoot()).isEmpty()) {
            return;
        }

        D selected = getSelectionModel().getSelectedNodes().get(0);
        TreeNodeElement<D> selectedTreeNodeElement = getModel().dataAdapter.getRenderedTreeNode(selected);

        if (selectedTreeNodeElement.isOpen()) {
            closeNode(selectedTreeNodeElement, true);
        } else {
            D project = getModel().dataAdapter.getChildren(getModel().getRoot()).get(0);
            TreeNodeElement<D> projectTreeNode = getModel().dataAdapter.getRenderedTreeNode(project);

            if (selectedTreeNodeElement != projectTreeNode) {
                TreeNodeElement<D> parentTreeNode = (TreeNodeElement<D>)selectedTreeNodeElement.getParentElement().getParentElement();
                if (parentTreeNode.getData() == null) {
                    return;
                }
                selectSingleNode(parentTreeNode);
            }
        }
    }

    private boolean expandPathRecursive(D expandedParentNode, List<String> pathToExpand, boolean dispatchNodeExpanded) {
        if (expandedParentNode == null) {
            return false;
        }

        NodeDataAdapter<D> dataAdapter = getModel().dataAdapter;
        D previousParentNode = expandedParentNode;

        for (int pathIndex = 0; pathIndex < pathToExpand.size(); ++pathIndex) {
            if (!getModel().dataAdapter.hasChildren(previousParentNode)) {
                // Consider this path expanded, even if some path components are left.
                return true;
            }

            // The root is already expanded by default. So we really want to recur the
            // child that matches the first component.
            List<D> children = getModel().dataAdapter.getChildren(previousParentNode);
            previousParentNode = null;

            for (int i = 0, n = children.size(); i < n; ++i) {
                D child = children.get(i);
                if (dataAdapter.getNodeId(child).equals(pathToExpand.get(pathIndex))) {

                    // We have a match. Look up the rendered element. The parent should
                    // already be expanded, so this must exist.
                    TreeNodeElement<D> renderedNode = dataAdapter.getRenderedTreeNode(child);

                    assert (renderedNode != null);

                    // If this node is not open, then we open it.
                    if (!renderedNode.isOpen()) {
                        expandNode(renderedNode, false, dispatchNodeExpanded);
                    }

                    // Continue to expand the remainder of the path.
                    previousParentNode = child;
                    break;
                }
            }

            if (previousParentNode == null) {
                // The path was only partially expanded.
                return false;
            }
        }

        return true;
    }

    /**
     * Walks the tree rooted at the specified renderedNode and gathers a list of
     * paths that correspond to nodes that have been expanded below the specified
     * rendered node. All paths are expressed as root relative.
     * <p/>
     * These paths correspond to "expansion leaves". Which are effectively nodes
     * whose children are all leaves, or are all collapsed. That is, nodes whose
     * children all answer false to {@link TreeNodeElement#isOpen()}.
     */
    private List<List<String>> gatherExpandedPaths(D rootData) {
        final List<List<String>> expandedPaths = new ArrayList<>();

        // Can't gather the expansion state for a null parent.
        if (rootData == null) {
            return expandedPaths;
        }

        iterateDfs(rootData, getModel().dataAdapter, new Visitor<D>() {

            @Override
            public boolean shouldVisit(D node) {
                // If a child node is open, it means that it has been expanded and its
                // children should have rendered nodes.
                TreeNodeElement<D> renderedChild = getModel().dataAdapter.getRenderedTreeNode(node);
                return (renderedChild != null) && renderedChild.isOpen();
            }

            @Override
            public void visit(D node, boolean willVisitChildren) {
                if (!willVisitChildren) {
                    // This node is an expansion leaf. Accumulate the path.
                    expandedPaths.add(getModel().dataAdapter.getNodePath(node));
                }
            }
        });

        return expandedPaths;
    }

    /**
     * Recursively iterates children of a given root node using DFS.
     *
     * @param rootData
     *         root node to start the iteration from
     * @param dataAdapter
     *         data adapter to get the children of a node
     * @param callback
     *         iteration callback
     */
    public static <D> void iterateDfs(D rootData, NodeDataAdapter<D> dataAdapter, Visitor<D> callback) {
        LinkedList<D> nodes = new LinkedList<>();
        nodes.add(rootData);

        // Iterative DFS.
        while (!nodes.isEmpty()) {
            D parentNodeData = nodes.pop();
            boolean willVisitChildren = false;
            List<D> children = dataAdapter.getChildren(parentNodeData);

            for (int i = 0, n = children.size(); i < n; i++) {
                D child = children.get(i);
                if (callback.shouldVisit(child)) {
                    // Add a filtered child to the stack of the nodes to visit.
                    nodes.add(child);
                    willVisitChildren = true;
                }
            }

            callback.visit(parentNodeData, willVisitChildren);
        }
    }

    private void maybeNotifyNodeActionExternal(TreeNodeElement<D> renderedNode, boolean dispatchNodeAction) {
        if (dispatchNodeAction && getModel().externalEventDelegate != null) {
            getModel().externalEventDelegate.onNodeAction(renderedNode);
        }
    }

    private void renderRecursive(Element parentContainer, D nodeData, int depth) {
        NodeDataAdapter<D> dataAdapter = getModel().dataAdapter;
        Tree.Css css = getResources().treeCss();

        // Make the node.
        TreeNodeElement<D> newNode = createNode(nodeData);
        parentContainer.appendChild(newNode);
        newNode.updateLeafOffset(parentContainer);

        // If we reach depth 0, we stop the recursion.
        if (depth == 0 || !newNode.hasChildrenContainer()) {
            if (dataAdapter.hasChildren(nodeData)) {
                newNode.closeNode(dataAdapter, css, getModel().animator, false);
            }
            return;
        }

        // Maybe continue the expansion.
        newNode.openNode(dataAdapter, css, getModel().animator, false);
        List<D> children = dataAdapter.getChildren(nodeData);
        for (int i = 0, n = children.size(); i < n; i++) {
            renderRecursive(newNode.getChildrenContainer(), children.get(i), depth - 1);
        }
    }

    /**
     * Returns the tree node whose element is or contains the given element, or
     * null if the given element cannot be matched to a tree node.
     */
    public TreeNodeElement<D> getNodeFromElement(Element element) {
        Css css = getModel().resources.treeCss();
        Element treeNodeBody = CssUtils.getAncestorOrSelfWithClassName(element, css.treeNodeBody());
        return treeNodeBody != null ? getView().getTreeNodeFromTreeNodeBody(treeNodeBody, css) : null;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        if (widget == null) {
            widget = new HTML();
            Element element = getView().getElement();
            widget.getElement().appendChild((Node)element);
            widget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        }

        return widget;
    }
}

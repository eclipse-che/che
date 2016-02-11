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
package org.eclipse.che.ide.part.explorer.project;

import com.google.common.annotations.Beta;
import com.google.gwt.event.shared.HandlerRegistration;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.ui.smartTree.NodeStorage.StoreSortInfo;
import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent;
import org.eclipse.che.ide.ui.smartTree.event.CollapseNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent;

import java.util.List;

/**
 * View interface for the {@link ProjectExplorerViewImpl}.
 *
 * @author Vlad Zhukovskiy
 */
public interface ProjectExplorerView extends View<ProjectExplorerView.ActionDelegate> {

    /**
     * Return list of project nodes.
     *
     * @return list of the project nodes
     */
    List<Node> getRootNodes();

    /**
     * Return list of all registered nodes in the tree.
     *
     * @return list of all nodes
     */
    List<Node> getAllNodes();

    /**
     * Return list of all nodes from parent.
     *
     * @return list of all nodes from parent
     */
    List<Node> getAllNodes(Node parent);

    /**
     * Add specified node to parent.
     * Parent may be a null value, then in this case node will be added into root of the tree.
     *
     * @param parent
     *         parent node or null
     * @param child
     *         child node
     */
    void addNode(Node parent, Node child);

    /**
     * Add specified nodes to parent.
     * Parent may be a null value, then in this case nodes will be added into root of the tree.
     *
     * @param parent
     *         parent node or null
     * @param children
     *         child nodes
     */
    void addNodes(Node parent, List<Node> children);

    /**
     * Remove all registered nodes in the tree.
     */
    void removeAllNodes();

    /**
     * Remove node from the project tree.
     *
     * @param node
     *         node which should be remove
     * @param closeMissingFiles
     *         true if opened nodes in editor part should be closed
     */
    void removeNode(Node node, boolean closeMissingFiles);

    /**
     * Refresh visual node presentation in the tree.
     * This method doesn't refresh node's children.
     *
     * @param node
     *         node to refresh
     */
    void refresh(Node node);

    /**
     * Retrieve sort information.
     * Beta: need to rework mechanism of node sorting in runtime.
     *
     * @return list of the sort information objects
     */
    @Beta
    List<StoreSortInfo> getSortInfo();

    /**
     * Apply sorting information to the current tree.
     * Beta: need to rework mechanism of node sorting in runtime.
     */
    @Beta
    void onApplySort();

    /**
     * Set sorting option to show folders on top.
     * Beta: need to rework mechanism of node sorting in runtime.
     *
     * @param foldersAlwaysOnTop
     *         true if folders should be always on top
     */
    @Beta
    void setFoldersAlwaysOnTop(boolean foldersAlwaysOnTop);

    /**
     * Get status for sorting folders in the project tree.
     * Beta: need to rework mechanism of node sorting in runtime.
     *
     * @return true if folders shows always on top
     */
    @Beta
    boolean isFoldersAlwaysOnTop();

    /**
     * Navigate to the storable source node in the project tree.
     * Perform node search and setting selection.
     *
     * @param path
     *         path to search
     */
    Promise<Node> scrollFromSource(HasStorablePath path);

    /**
     * Activate "Go Into" mode on specified node if.
     * Node should support this mode. See {@link Node#supportGoInto()}.
     *
     * @param node
     *         node which should be activated in "Go Into" mode
     * @return true - if "Go Into" mode has been activated
     */
    boolean setGoIntoModeOn(Node node);

    /**
     * Reset "Go Into" mode. If tree wasn't in "Go Into" mode than this method will do nothing.
     */
    void resetGoIntoMode();

    /**
     * Get "Go Into" state on current tree.
     *
     * @return true - if "Go Into" mode has been activated.
     */
    boolean isGoIntoActivated();

    /**
     * Add ability to register custom handlers which listen "Go Into" state changes.
     *
     * @param handler
     *         go into state handler
     * @return handler registration
     */
    HandlerRegistration addGoIntoStateHandler(GoIntoStateEvent.GoIntoStateHandler handler);

    void reloadChildren(Node parent);

    void reloadChildren(Node parent, boolean deep);

    /**
     * Reload children by node type.
     * Useful method if you want to reload specified nodes, e.g. External Liraries.
     *
     * @param type
     *         node type to update
     */
    void reloadChildrenByType(Class<?> type);

    /**
     * Check if node is expanded or not.
     *
     * @param node
     *         node to check
     * @return true - if node expanded, otherwise - false
     */
    boolean isExpanded(Node node);

    /**
     * Expand all non-leaf nodes.
     * <p/>
     * CAUTION! Use this method for your own risk, because it may took a lot of traffic to the server.
     */
    void expandAll();

    /**
     * Collapse all non-leaf nodes.
     */
    void collapseAll();

    /**
     * Check if node has been already loaded.
     *
     * @param node
     *         node to check
     * @return true if node has been already loaded
     */
    boolean isLoaded(Node node);

    /**
     * Get all rendered and visible nodes.
     *
     * @return list of visible nodes
     */
    List<Node> getVisibleNodes();

    /**
     * Configure tree to show or hide files that starts with ".", e.g. hidden files.
     * Affects only current project which is under selection.
     *
     * @param show
     *         true - if those files should be shown, otherwise - false
     */
    void showHiddenFiles(boolean show);

    /**
     * Configure tree to show or hide files that starts with ".", e.g. hidden files.
     * Affects all expanded nodes.
     *
     * @param show
     *         true - if those files should be shown, otherwise - false
     */
    void showHiddenFilesForAllExpandedNodes(boolean show);

    /**
     * Retrieve status of showing hidden files from selected project.
     *
     * @return true - if hidden files are shown, otherwise - false
     */
    boolean isShowHiddenFiles();

    /**
     * Search node in the project explorer tree by storable path.
     *
     * @param path
     *         path to node
     * @param forceUpdate
     *         force children reload
     * @param closeMissingFiles
     *         allow editor to close removed files if they were opened
     * @return promise object with found node or promise error if node wasn't found
     */
    Promise<Node> getNodeByPath(HasStorablePath path, boolean forceUpdate, boolean closeMissingFiles);

    /**
     * Set selection on node in project tree.
     *
     * @param item
     *         node which should be selected
     * @param keepExisting
     *         keep current selection or reset it
     */
    void select(Node item, boolean keepExisting);

    /**
     * Set selection on nodes in project tree.
     *
     * @param items
     *         nodes which should be selected
     * @param keepExisting
     *         keep current selection or reset it
     */
    void select(List<Node> items, boolean keepExisting);

    /**
     * Set node expand state.
     *
     * @param node
     *         node to expand
     * @param expand
     *         true - if node should be expanded, otherwise - collapsed
     */
    void setExpanded(Node node, boolean expand);

    /**
     * Register node expand handler to allow custom functionality retrieve expand event from the project tree.
     *
     * @param handler
     *         expand handler
     * @return handler registration
     */
    HandlerRegistration addExpandHandler(ExpandNodeEvent.ExpandNodeHandler handler);

    /**
     * Register node before expand handler to allow custom functionality retrieve before expand event from the project tree.
     *
     * @param handler
     *         before expand handler
     * @return handler registration
     */
    HandlerRegistration addBeforeExpandHandler(BeforeExpandNodeEvent.BeforeExpandNodeHandler handler);

    /**
     * Register before node load handler to allow custom functionality retrieve before load event from the project tree.
     *
     * @param handler
     *         before load handler
     * @return handler registration
     */
    HandlerRegistration addBeforeNodeLoadHandler(BeforeLoadEvent.BeforeLoadHandler handler);

    /**
     * Register node collapse handler to allow custom functionality retrieve collapse event from the project tree.
     *
     * @param handler
     *         collapse handler
     * @return handler registration
     */
    HandlerRegistration addCollapseHandler(CollapseNodeEvent.CollapseNodeHandler handler);

    /**
     * Set current part visibility state.
     *
     * @param visible
     *         true - if visible, otherwise - false
     */
    void setVisible(boolean visible);

    /**
     * Return node ID provider. Ned to generate unique ID for the stored node in the tree.
     *
     * @return node id provider
     */
    UniqueKeyProvider<Node> getNodeIdProvider();

    /**
     * Reindex node which was stored in the tree by old id.
     *
     * @param oldId
     *         old node id
     * @param node
     *         node to be re-indexed
     * @return true if node has been re-indexed, otherwise - false
     */
    boolean reIndex(String oldId, Node node);

    /**
     * Perform visual scroll to node in the Project Explorer.
     *
     * @param node
     *         node
     */
    void scrollToNode(Node node);

    interface ActionDelegate extends BaseActionDelegate {
        void onSelectionChanged(List<Node> selection);

        void onDeleteKeyPressed();

        void onRefreshProjectsRequested();
    }
}

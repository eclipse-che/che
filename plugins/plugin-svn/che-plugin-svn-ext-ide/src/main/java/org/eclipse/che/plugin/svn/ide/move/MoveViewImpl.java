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
package org.eclipse.che.plugin.svn.ide.move;

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.ProjectTreeNodeDataAdapter;
import org.eclipse.che.plugin.svn.ide.merge.ProjectTreeNodeRenderer;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Implementation of {@link MoveView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class MoveViewImpl extends Window implements MoveView {
    interface MoveViewImplUiBinder extends UiBinder<Widget, MoveViewImpl> {
    }

    private static MoveViewImplUiBinder uiBinder = GWT.create(MoveViewImplUiBinder.class);

    Button btnMove;
    Button btnCancel;

    @UiField
    DockLayoutPanel treeContainer;

    @UiField
    CheckBox urlCheckBox;

    @UiField
    DeckPanel deckPanel;

    @UiField
    TextBox sourceUrlTextBox;

    @UiField
    TextBox targetUrlTextBox;

    @UiField
    TextBox commentTextBox;

    @UiField
    DockLayoutPanel newNamePanel;

    @UiField
    TextBox newNameTextBox;

    @UiField(provided = true)
    SubversionExtensionResources             resources;
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    private MoveView.ActionDelegate delegate;
    private Tree<TreeNode<?>>       tree;
    private AbstractTreeNode<?>     rootNode;
    private OMSVGSVGElement         alertMarker;

    private static final String PLACEHOLDER       = "placeholder";
    private static final String PLACEHOLDER_DUMMY = "https://subversion.site.com/svn/sht_site/trunk";

    @Inject
    public MoveViewImpl(SubversionExtensionResources resources,
                        SubversionExtensionLocalizationConstants constants,
                        ProjectTreeNodeRenderer projectTreeNodeRenderer,
                        org.eclipse.che.ide.Resources coreResources) {
        this.resources = resources;
        this.constants = constants;

        this.ensureDebugId("svn-move-window");
        this.setTitle(constants.moveViewTitle());

        this.setWidget(uiBinder.createAndBindUi(this));

        btnCancel = createButton(constants.buttonCancel(), "svn-move-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        getFooter().add(btnCancel);

        btnMove = createButton(constants.moveButton(), "svn-move-move", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onMoveClicked();
            }
        });
        getFooter().add(btnMove);

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMargin(10, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        rootNode = new AbstractTreeNode<Void>(null, null, null, null) {
            /** {@inheritDoc} */
            @NotNull
            @Override
            public String getId() {
                return "ROOT";
            }

            /** {@inheritDoc} */
            @NotNull
            @Override
            public String getDisplayName() {
                return "ROOT";
            }

            /** {@inheritDoc} */
            @Override
            public boolean isLeaf() {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
            }
        };

        tree = Tree.create(coreResources, new ProjectTreeNodeDataAdapter(), projectTreeNodeRenderer);
        tree.setTreeEventHandler(new Tree.Listener<TreeNode<?>>() {
            /** {@inheritDoc} */
            @Override
            public void onNodeAction(TreeNodeElement<TreeNode<?>> treeNodeElement) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeClosed(TreeNodeElement<TreeNode<?>> treeNodeElement) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeContextMenu(int i, int i1, TreeNodeElement<TreeNode<?>> treeNodeElement) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeDragStart(TreeNodeElement<TreeNode<?>> treeNodeElement, MouseEvent mouseEvent) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeDragDrop(TreeNodeElement<TreeNode<?>> treeNodeElement, MouseEvent mouseEvent) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeExpanded(TreeNodeElement<TreeNode<?>> treeNodeElement) {
                delegate.onNodeExpanded(treeNodeElement.getData());
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeSelected(TreeNodeElement<TreeNode<?>> treeNodeElement, SignalEvent signalEvent) {
                delegate.onNodeSelected(treeNodeElement.getData());
            }

            /** {@inheritDoc} */
            @Override
            public void onRootContextMenu(int i, int i1) {
            }

            /** {@inheritDoc} */
            @Override
            public void onRootDragDrop(MouseEvent mouseEvent) {
            }

            /** {@inheritDoc} */
            @Override
            public void onKeyboard(KeyboardEvent keyboardEvent) {
            }
        });

        treeContainer.add(tree.asWidget());

        sourceUrlTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
        targetUrlTextBox.getElement().setAttribute(PLACEHOLDER, PLACEHOLDER_DUMMY);
        commentTextBox.getElement().setAttribute(PLACEHOLDER, "Comment...");

        // TODO setValue(false, true), clean setEnabled(false) and deckPanel.showWidget(0) once CHE-942 is fixed
        urlCheckBox.setValue(true, true);
        urlCheckBox.setEnabled(false);
        onUrlCheckBoxClicked(null);

        deckPanel.showWidget(1);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void setProjectNodes(List<TreeNode<?>> rootNodes) {
        rootNode.setChildren(rootNodes);
        for (TreeNode<?> treeNode : rootNodes) {
            treeNode.setParent(rootNode);
        }

        tree.getSelectionModel().clearSelections();
        tree.getModel().setRoot(rootNode);
        tree.renderTree(0);

        if (rootNodes.isEmpty()) {
            delegate.onNodeSelected(null);
        } else {
            final TreeNode<?> firstNode = rootNodes.get(0);
            if (!firstNode.isLeaf()) {
                // expand first node that usually represents project itself
                tree.autoExpandAndSelectNode(firstNode, false);
                delegate.onNodeExpanded(firstNode);
            }
            // auto-select first node
            tree.getSelectionModel().selectSingleNode(firstNode);
            delegate.onNodeSelected(firstNode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateProjectNode(@NotNull TreeNode<?> oldNode, @NotNull TreeNode<?> newNode) {
        // get currently selected node
        final List<TreeNode<?>> selectedNodes = tree.getSelectionModel().getSelectedNodes();
        TreeNode<?> selectedNode = null;
        if (!selectedNodes.isEmpty()) {
            selectedNode = selectedNodes.get(0);
        }

        List<List<String>> pathsToExpand = tree.replaceSubtree(oldNode, newNode, false);
        tree.expandPaths(pathsToExpand, false);

        // restore selected node
        if (selectedNode != null) {
            tree.getSelectionModel().selectSingleNode(selectedNode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMarker(String message) {
        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);

        Tooltip.create((elemental.dom.Element)alertMarker.getElement(),
                PositionController.VerticalAlign.TOP,
                PositionController.HorizontalAlign.MIDDLE,
                message);

        btnMove.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public void hideErrorMarker() {
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        btnMove.setEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isURLSelected() {
        return urlCheckBox.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getSourceUrl() {
        return sourceUrlTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String getTargetUrl() {
        return targetUrlTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public TreeNode<?> getDestinationNode() {
        return tree.getSelectionModel().getSelectedNodes().get(0);
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(boolean singleSelectedItem) {
        newNamePanel.setVisible(singleSelectedItem);
        newNameTextBox.setText(null);
        show();
    }

    /** {@inheritDoc} */
    @Override
    public String getNewName() {
        return newNameTextBox.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String getComment() {
        return commentTextBox.getText();
    }

    @UiHandler({"sourceUrlTextBox", "targetUrlTextBox", "commentTextBox"})
    @SuppressWarnings("unused")
    public void onUrlFieldsChanged(KeyUpEvent event) {
        delegate.onUrlsChanged();
    }

    @UiHandler("urlCheckBox")
    @SuppressWarnings("unused")
    public void onUrlCheckBoxClicked(ClickEvent event) {
        if (isURLSelected()) {
            sourceUrlTextBox.setText(null);
            targetUrlTextBox.setText(null);
            delegate.onUrlsChanged();
            deckPanel.showWidget(1);
        } else {
            delegate.onNodeSelected(tree.getSelectionModel().getSelectedNodes().get(0));
            deckPanel.showWidget(0);
        }
    }
}

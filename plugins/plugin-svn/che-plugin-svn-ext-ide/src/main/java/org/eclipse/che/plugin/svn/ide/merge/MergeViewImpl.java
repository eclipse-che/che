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
package org.eclipse.che.plugin.svn.ide.merge;

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.ProjectTreeNodeDataAdapter;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of MergeView, represented as popup modal dialog.
 */
@Singleton
public class MergeViewImpl extends Window implements MergeView {

    /** UI binder */
    interface CopyViewImplUiBinder extends UiBinder<Widget, MergeViewImpl> {
    }

    /** UI binder instance */
    private static CopyViewImplUiBinder uiBinder = GWT.create(CopyViewImplUiBinder.class);

    /** Localization constants. */
    @UiField(provided = true)
    SubversionExtensionLocalizationConstants constants;

    /** Bundled resources. */
    @UiField(provided = true)
    SubversionExtensionResources resources;

    /** Delegate to perform actions */
    private ActionDelegate delegate;

    /** Target text box. */
    @UiField
    TextBox targetTextBox;

    @UiField
    DeckPanel deckPanel;

    /** Source URL check box. */
    @UiField
    CheckBox sourceURLCheckBox;

    /** Source URl text box. */
    @UiField
    TextBox sourceUrlTextBox;

    /** Root node for subversion item tree. */
    private AbstractTreeNode<?> rootNode;

    /** Subversion item tree. */
    private Tree<TreeNode<?>> tree;

    /** Tree node renderer. */
    private ProjectTreeNodeRenderer projectTreeNodeRenderer;

    @UiField
    DockLayoutPanel treeContainer;

    /** Merge button */
    private Button mergeButton;

    /** Cancel button */
    private Button cancelButton;

    /** Attention icon. */
    private OMSVGSVGElement alertMarker;

    /* Default constructor creating an instance of this MergeViewImpl */
    @Inject
    public MergeViewImpl(SubversionExtensionLocalizationConstants constants,
                         SubversionExtensionResources resources,
                         org.eclipse.che.ide.Resources coreResources,
                         ProjectTreeNodeRenderer projectTreeNodeRenderer) {
        this.constants = constants;
        this.resources = resources;

        this.projectTreeNodeRenderer = projectTreeNodeRenderer;

        ensureDebugId("plugin-svn merge-dialog");
        setWidget(uiBinder.createAndBindUi(this));
        setTitle(constants.mergeDialogTitle());

        mergeButton = createButton(constants.buttonMerge(), "plugin-svn-merge-dialog-merge-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {delegate.mergeClicked();}
        });
        mergeButton.addStyleName(Window.resources.windowCss().button());
        addButtonToFooter(mergeButton);

        cancelButton = createButton(constants.buttonCancel(), "plugin-svn-merge-dialog-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {delegate.cancelClicked();}
        });
        addButtonToFooter(cancelButton);

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMargin(10, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());
        alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);

        targetTextBox.setEnabled(false);

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
                treeNodeElement.getData().processNodeAction();
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

    }

    @Override
    public void setRootNode(TreeNode<?> node) {
        List<TreeNode<?>> children = new ArrayList<>();
        children.add(node);
        rootNode.setChildren(children);
        node.setParent(rootNode);

        tree.getSelectionModel().clearSelections();
        tree.getModel().setRoot(rootNode);
        tree.renderTree(0);

        tree.getSelectionModel().selectSingleNode(node);

        if (!node.isLeaf()) {
            tree.autoExpandAndSelectNode(node, false);
            delegate.onNodeExpanded(node);
        }
    }

    @Override
    public void render(TreeNode<?> node) {
        TreeNodeElement<TreeNode<?>> treeNodeElement = tree.getNode(node);
        if (node.isLeaf()) {
            Tree.Css css = tree.getResources().treeCss();
            treeNodeElement.makeLeafNode(css);
        }

        projectTreeNodeRenderer.renderNodeContents(node);
    }

    @Override
    public HasValue<Boolean> sourceCheckBox() {
        return sourceURLCheckBox;
    }

    @Override
    public void enableMergeButton(boolean enable) {
        mergeButton.setEnabled(enable);
    }

    @Override
    public void setError(final String message) {
        if (message == null) {
            alertMarker.getStyle().setVisibility(Style.Visibility.HIDDEN);
            return;
        }

        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Tooltip tooltip = Tooltip.create((elemental.dom.Element) alertMarker.getElement(),
                        PositionController.VerticalAlign.TOP,
                        PositionController.HorizontalAlign.MIDDLE,
                        message);
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        deckPanel.showWidget(0);
        super.show();
    }

    @Override
    public HasValue<String> targetTextBox() {
        return targetTextBox;
    }

    @Override
    public HasValue<String> sourceURLTextBox() {
        return sourceUrlTextBox;
    }

    @UiHandler("sourceURLCheckBox")
    @SuppressWarnings("unused")
    public void onSourceUrlCheckBoxActivated(ClickEvent event) {
        deckPanel.showWidget(sourceURLCheckBox.getValue() ? 1 : 0);
        delegate.onSourceCheckBoxClicked();
    }

    @UiHandler("sourceUrlTextBox")
    @SuppressWarnings("unused")
    public void onSourceURLChanged(KeyUpEvent event) {
        delegate.onSourceURLChanged(sourceUrlTextBox.getText());
    }

}

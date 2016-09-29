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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.input.SignalEvent;

/**
 * Provides implementation of view to display machines on special panel.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class MachinePanelViewImpl extends BaseView<MachinePanelView.ActionDelegate> implements MachinePanelView {
    interface MachinePanelImplUiBinder extends UiBinder<Widget, MachinePanelViewImpl> {
    }

    private static final MachinePanelImplUiBinder UI_BINDER = GWT.create(MachinePanelImplUiBinder.class);

    @UiField(provided = true)
    Tree<MachineTreeNode> tree;

    @Inject
    public MachinePanelViewImpl(org.eclipse.che.ide.Resources resources,
                                PartStackUIResources partStackUIResources,
                                MachineDataAdapter adapter,
                                MachineTreeRenderer renderer) {
        super(partStackUIResources);

        tree = Tree.create(resources, adapter, renderer);

        setContentWidget(UI_BINDER.createAndBindUi(this));

        tree.setTreeEventHandler(new Tree.Listener<MachineTreeNode>() {
            @Override
            public void onNodeAction(TreeNodeElement<MachineTreeNode> node) {

            }

            @Override
            public void onNodeClosed(TreeNodeElement<MachineTreeNode> node) {

            }

            @Override
            public void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<MachineTreeNode> node) {

            }

            @Override
            public void onNodeDragStart(TreeNodeElement<MachineTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeDragDrop(TreeNodeElement<MachineTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeExpanded(TreeNodeElement<MachineTreeNode> node) {

            }

            @Override
            public void onNodeSelected(TreeNodeElement<MachineTreeNode> node, SignalEvent event) {
                Object selectedNode = node.getData().getData();

                if (selectedNode instanceof MachineEntity) {
                    delegate.onMachineSelected((MachineEntity)selectedNode);
                }
            }

            @Override
            public void onRootContextMenu(int mouseX, int mouseY) {

            }

            @Override
            public void onRootDragDrop(MouseEvent event) {

            }

            @Override
            public void onKeyboard(KeyboardEvent event) {

            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setData(MachineTreeNode root) {
        tree.asWidget().setVisible(true);
        tree.getModel().setRoot(root);
        tree.renderTree(-1);
    }

    /** {@inheritDoc} */
    @Override
    public void selectNode(MachineTreeNode machineNode) {
        if (machineNode == null) {
            return;
        }

        tree.getSelectionModel().selectSingleNode(machineNode);

        delegate.onMachineSelected((MachineEntity)machineNode.getData());
    }
}

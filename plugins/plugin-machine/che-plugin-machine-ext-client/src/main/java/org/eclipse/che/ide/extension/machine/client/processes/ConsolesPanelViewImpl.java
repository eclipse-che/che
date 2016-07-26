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
package org.eclipse.che.ide.extension.machine.client.processes;

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.tree.SelectionModel;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.input.SignalEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link ConsolesPanelView}.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 */

public class ConsolesPanelViewImpl extends Composite implements ConsolesPanelView, RequiresResize {

    interface ProcessesViewImplUiBinder extends UiBinder<Widget, ConsolesPanelViewImpl> {
    }

    @UiField(provided = true)
    MachineResources machineResources;

    @UiField(provided = true)
    Tree<ProcessTreeNode> processTree;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    DeckLayoutPanel outputPanel;

    @UiField
    FlowPanel navigationPanel;

    ActionDelegate delegate;

    private Map<String, IsWidget> processWidgets;

    private LinkedHashMap<String, ProcessTreeNode> processTreeNodes;

    private String activeProcessId;

    @Inject
    public ConsolesPanelViewImpl(org.eclipse.che.ide.Resources resources,
                                 MachineResources machineResources,
                                 ProcessesViewImplUiBinder uiBinder,
                                 ProcessTreeRenderer renderer,
                                 ProcessDataAdapter adapter) {
        this.machineResources = machineResources;
        this.processWidgets = new HashMap<>();
        processTreeNodes = new LinkedHashMap<>();
        splitLayoutPanel = new SplitLayoutPanel(1);

        renderer.setAddTerminalClickHandler(new AddTerminalClickHandler() {
            @Override
            public void onAddTerminalClick(@NotNull String machineId) {
                delegate.onAddTerminal(machineId);
            }
        });

        renderer.setPreviewSshClickHandler(new PreviewSshClickHandler() {
            @Override
            public void onPreviewSshClick(@NotNull String machineId) {
                delegate.onPreviewSsh(machineId);
            }
        });

        renderer.setStopProcessHandler(new StopProcessHandler() {
            @Override
            public void onStopProcessClick(@NotNull ProcessTreeNode node) {
                delegate.onStopCommandProcess(node);
            }

            @Override
            public void onCloseProcessOutputClick(@NotNull ProcessTreeNode node) {
                ProcessTreeNode.ProcessNodeType type = node.getType();
                switch (type) {
                    case COMMAND_NODE:
                        delegate.onCloseCommandOutputClick(node);
                        break;
                    case TERMINAL_NODE:
                        delegate.onCloseTerminal(node);
                        break;
                }
            }
        });

        processTree = Tree.create(resources, adapter, renderer);
        processTree.asWidget().addStyleName(this.machineResources.getCss().processTree());

        processTree.setTreeEventHandler(new Tree.Listener<ProcessTreeNode>() {
            @Override
            public void onNodeAction(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeClosed(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<ProcessTreeNode> node) {
                delegate.onContextMenu(mouseX, mouseY, node.getData());
            }

            @Override
            public void onNodeDragStart(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeDragDrop(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeExpanded(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeSelected(TreeNodeElement<ProcessTreeNode> node, SignalEvent event) {
                delegate.onTreeNodeSelected(node.getData());
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

        initWidget(uiBinder.createAndBindUi(this));
        navigationPanel.getElement().setTabIndex(0);

        tuneSplitter();

        splitLayoutPanel.setWidgetHidden(navigationPanel, true);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Improves splitter visibility.
     */
    private void tuneSplitter() {
        NodeList<Node> nodes = splitLayoutPanel.getElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            if (node.hasChildNodes()) {
                com.google.gwt.dom.client.Element el = node.getFirstChild().cast();
                if ("gwt-SplitLayoutPanel-HDragger".equals(el.getClassName())) {
                    tuneSplitter(el);
                    return;
                }
            }
        }
    }

    /**
     * Tunes splitter. Makes it wider and adds double border to seem rich.
     *
     * @param el
     *         element to tune
     */
    private void tuneSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        /** Tune splitter catch panel */
        el.getStyle().setProperty("boxSizing", "border-box");
        el.getStyle().setProperty("width", "5px");
        el.getStyle().setProperty("overflow", "hidden");
        el.getStyle().setProperty("marginLeft", "-3px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement smallBorder = Document.get().createDivElement();
        smallBorder.getStyle().setProperty("position", "absolute");
        smallBorder.getStyle().setProperty("width", "1px");
        smallBorder.getStyle().setProperty("height", "100%");
        smallBorder.getStyle().setProperty("left", "3px");
        smallBorder.getStyle().setProperty("top", "0px");
        smallBorder.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        el.appendChild(smallBorder);

        /** Add large border */
        DivElement largeBorder = Document.get().createDivElement();
        largeBorder.getStyle().setProperty("position", "absolute");
        largeBorder.getStyle().setProperty("width", "2px");
        largeBorder.getStyle().setProperty("height", "100%");
        largeBorder.getStyle().setProperty("left", "1px");
        largeBorder.getStyle().setProperty("top", "0px");
        largeBorder.getStyle().setProperty("opacity", "0.4");
        largeBorder.getStyle().setProperty("backgroundColor", Style.getSplitterLargeBorderColor());
        el.appendChild(largeBorder);
    }

    protected void focusView() {
        getElement().focus();
    }

    @Override
    public void addProcessWidget(String processId, IsWidget widget) {
        processWidgets.put(processId, widget);
        outputPanel.add(widget);

        showProcessOutput(processId);
    }

    @Override
    public void addProcessNode(@NotNull ProcessTreeNode node) {
        processTreeNodes.put(node.getId(), node);
    }

    @Override
    public void removeProcessNode(@NotNull ProcessTreeNode node) {
        processTreeNodes.remove(node.getId());
    }

    @Override
    public void setProcessesData(@NotNull ProcessTreeNode root) {
        splitLayoutPanel.setWidgetHidden(navigationPanel, false);

        processTree.asWidget().setVisible(true);
        processTree.getModel().setRoot(root);
        processTree.renderTree();

        for (ProcessTreeNode processTreeNode : processTreeNodes.values()) {
            if (!processTreeNode.getId().equals(activeProcessId) && processTreeNode.hasUnreadContent()) {
                processTreeNode.getTreeNodeElement().getClassList().add(machineResources.getCss().badgeVisible());
            }
        }
    }

    @Override
    public void selectNode(final ProcessTreeNode node) {
        SelectionModel<ProcessTreeNode> selectionModel = processTree.getSelectionModel();

        if (node == null) {
            selectionModel.clearSelections();
            return;
        } else {
            selectionModel.setTreeActive(true);
            selectionModel.clearSelections();
            selectionModel.selectSingleNode(node);

            node.getTreeNodeElement().scrollIntoView();
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                delegate.onTreeNodeSelected(node);
            }
        });
    }

    @Override
    public void showProcessOutput(String processId) {
        if (!processWidgets.containsKey(processId)) {
            processId = "";
        }

        if (processWidgets.containsKey(processId)) {
            onResize();
            outputPanel.showWidget(processWidgets.get(processId).asWidget());

            activeProcessId = processId;

            ProcessTreeNode treeNode = processTreeNodes.get(processId);
            if (treeNode != null) {
                treeNode.setHasUnreadContent(false);
                treeNode.getTreeNodeElement().getClassList().remove(machineResources.getCss().badgeVisible());
            }
        }
    }

    @Override
    public void hideProcessOutput(String processId) {
        IsWidget widget = processWidgets.get(processId);
        outputPanel.remove(widget);
        processWidgets.remove(processId);
    }

    @Override
    public void markProcessHasOutput(String processId) {
        if (processId.equals(activeProcessId)) {
            return;
        }

        ProcessTreeNode treeNode = processTreeNodes.get(processId);
        if (treeNode != null) {
            treeNode.setHasUnreadContent(true);
            treeNode.getTreeNodeElement().getClassList().add(machineResources.getCss().badgeVisible());
        }
    }

    @Override
    public void clear() {
        for (IsWidget widget : processWidgets.values()) {
            outputPanel.remove(widget);
        }

        processWidgets.clear();
    }

    @Override
    public int getNodeIndex(String processId) {
        int index = 0;
        for (ProcessTreeNode processTreeNode : processTreeNodes.values()) {
            if (processTreeNode.getId().equals(processId)) {
                return index;
            }

            index++;
        }

        return -1;
    }

    @Override
    @Nullable
    public ProcessTreeNode getNodeByIndex(@NotNull int index) {
        return (ProcessTreeNode)processTreeNodes.values().toArray()[index];
    }

    @Override
    @Nullable
    public ProcessTreeNode getNodeById(@NotNull String nodeId) {
        return processTreeNodes.get(nodeId);
    }

    @Override
    public void setStopButtonVisibility(String nodeId, boolean visible) {
        ProcessTreeNode processTreeNode = processTreeNodes.get(nodeId);
        if (processTreeNode == null) {
            return;
        }

        if (visible) {
            processTreeNode.getTreeNodeElement().getClassList().remove(machineResources.getCss().hideStopButton());
        } else {
            processTreeNode.getTreeNodeElement().getClassList().add(machineResources.getCss().hideStopButton());
        }
    }

    @Override
    public void onResize() {
        for (int i = 0; i < outputPanel.getWidgetCount(); i++) {
            Widget widget = outputPanel.getWidget(i);
            if (widget instanceof RequiresResize) {
                ((RequiresResize)widget).onResize();
            }
        }
    }
}

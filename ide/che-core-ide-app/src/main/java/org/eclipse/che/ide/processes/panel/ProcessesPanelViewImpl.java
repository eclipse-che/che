/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.panel;

import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.ProcessDataAdapter;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.ProcessTreeRenderer;
import org.eclipse.che.ide.processes.StopProcessHandler;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;
import org.eclipse.che.ide.ui.SplitterFancyUtil;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanelFactory;
import org.eclipse.che.ide.ui.multisplitpanel.WidgetToShow;
import org.eclipse.che.ide.ui.tree.SelectionModel;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implementation of {@link ProcessesPanelView}.
 *
 * @author Artem Zatsarynnyi
 */
public class ProcessesPanelViewImpl extends BaseView<ProcessesPanelView.ActionDelegate>
    implements ProcessesPanelView,
        SubPanel.FocusListener,
        SubPanel.DoubleClickListener,
        SubPanel.AddTabButtonClickListener,
        RequiresResize {

  @UiField(provided = true)
  MachineResources machineResources;

  @UiField(provided = true)
  SplitLayoutPanel splitLayoutPanel;

  @UiField FlowPanel navigationPanel;

  @UiField(provided = true)
  Tree<ProcessTreeNode> processTree;

  private LinkedHashMap<String, ProcessTreeNode> processTreeNodes;

  private Map<WidgetToShow, SubPanel> widget2Panels;
  private Map<String, WidgetToShow> processWidgets;
  private Map<IsWidget, ProcessTreeNode> widget2TreeNodes;

  private SubPanel focusedSubPanel;

  private String activeProcessId = "";

  private Focusable lastFosuced;

  private boolean navigationPanelVisible;

  @Inject
  public ProcessesPanelViewImpl(
      org.eclipse.che.ide.Resources resources,
      MachineResources machineResources,
      ProcessTreeRenderer renderer,
      ProcessDataAdapter adapter,
      ProcessesPartViewImplUiBinder uiBinder,
      SubPanelFactory subPanelFactory,
      CoreLocalizationConstant localizationConstants,
      SplitterFancyUtil splitterFancyUtil) {
    setTitle(localizationConstants.viewProcessesTitle());
    this.machineResources = machineResources;

    processTreeNodes = new LinkedHashMap<>();
    widget2Panels = new HashMap<>();
    processWidgets = new HashMap<>();
    widget2TreeNodes = new HashMap<>();

    renderer.addAddTerminalClickHandler(
        machineId -> delegate.onAddTerminal(machineId, TerminalOptionsJso.createDefault()));
    renderer.addPreviewSshClickHandler(machineId -> delegate.onPreviewSsh(machineId));
    renderer.addStopProcessHandler(
        new StopProcessHandler() {
          @Override
          public void onStopProcessClick(ProcessTreeNode node) {
            delegate.onStopCommandProcess(node);
          }

          @Override
          public void onCloseProcessOutputClick(ProcessTreeNode node) {
            switch (node.getType()) {
              case COMMAND_NODE:
                delegate.onCloseCommandOutputClick(node);
                break;
              case TERMINAL_NODE:
                delegate.onCloseTerminal(node);
                break;
              default:
            }
          }
        });

    processTree = Tree.create(resources, adapter, renderer);
    processTree.asWidget().addStyleName(machineResources.getCss().processTree());
    processTree.setTreeEventHandler(
        new Tree.Listener<ProcessTreeNode>() {
          @Override
          public void onNodeAction(TreeNodeElement<ProcessTreeNode> node) {}

          @Override
          public void onNodeClosed(TreeNodeElement<ProcessTreeNode> node) {}

          @Override
          public void onNodeContextMenu(
              int mouseX, int mouseY, TreeNodeElement<ProcessTreeNode> node) {
            delegate.onContextMenu(mouseX, mouseY, node.getData());
          }

          @Override
          public void onNodeDragStart(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {}

          @Override
          public void onNodeDragDrop(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {}

          @Override
          public void onNodeExpanded(TreeNodeElement<ProcessTreeNode> node) {}

          @Override
          public void onNodeSelected(TreeNodeElement<ProcessTreeNode> node, SignalEvent event) {
            delegate.onTreeNodeSelected(node.getData());

            WidgetToShow widgetToFocus = processWidgets.get(node.getData().getId());
            if (widgetToFocus != null) {
              SubPanel panelToFocus = widget2Panels.get(widgetToFocus);
              focusGained(panelToFocus, widgetToFocus.getWidget());
            }
          }

          @Override
          public void onRootContextMenu(int mouseX, int mouseY) {}

          @Override
          public void onRootDragDrop(MouseEvent event) {}

          @Override
          public void onKeyboard(KeyboardEvent event) {}
        });
    processTree.asWidget().ensureDebugId("process-tree");

    splitLayoutPanel = new SplitLayoutPanel(1);

    setContentWidget(uiBinder.createAndBindUi(this));
    navigationPanel.getElement().setTabIndex(0);

    final SubPanel subPanel = subPanelFactory.newPanel();
    subPanel.setFocusListener(this);
    subPanel.setDoubleClickListener(this);
    subPanel.setAddTabButtonClickListener(this);
    splitLayoutPanel.add(subPanel.getView());
    focusedSubPanel = subPanel;

    splitterFancyUtil.tuneSplitter(splitLayoutPanel);
    splitLayoutPanel.setWidgetHidden(navigationPanel, true);
    navigationPanelVisible = false;
  }

  @Override
  public void addWidget(
      final String processId,
      final String title,
      final SVGResource icon,
      final IsWidget widget,
      final boolean removable) {

    Optional<WidgetToShow> existedWidget =
        focusedSubPanel
            .getAllWidgets()
            .stream()
            .filter(widgetToShow -> widgetToShow.getWidget().equals(widget))
            .findFirst();

    if (existedWidget.isPresent()) {
      focusedSubPanel.activateWidget(existedWidget.get());
      return;
    }

    final WidgetToShow widgetToShow =
        new WidgetToShow() {
          @Override
          public IsWidget getWidget() {
            return widget;
          }

          @Override
          public String getTitle() {
            return title;
          }

          @Override
          public SVGResource getIcon() {
            return icon;
          }
        };

    widget2Panels.put(widgetToShow, focusedSubPanel);

    focusedSubPanel.addWidget(
        widgetToShow,
        removable,
        new SubPanel.WidgetRemovingListener() {
          @Override
          public void onWidgetRemoving(SubPanel.RemoveCallback removeCallback) {
            final ProcessTreeNode treeNode = widget2TreeNodes.get(widgetToShow.getWidget());

            if (treeNode == null) {
              removeCallback.remove();
              return;
            }

            switch (treeNode.getType()) {
              case COMMAND_NODE:
                delegate.onCommandTabClosing(treeNode, removeCallback);
                break;
              case TERMINAL_NODE:
                delegate.onTerminalTabClosing(treeNode);
                removeCallback.remove();
                break;
              case MACHINE_NODE:
                removeCallback.remove();
                break;
              default:
                removeCallback.remove();
            }
          }
        });

    processWidgets.put(processId, widgetToShow);

    widget2TreeNodes.put(widgetToShow.getWidget(), processTreeNodes.get(processId));
  }

  @Override
  public void selectNode(final ProcessTreeNode node) {
    final SelectionModel<ProcessTreeNode> selectionModel = processTree.getSelectionModel();

    if (node == null) {
      selectionModel.clearSelections();
    } else {
      selectionModel.setTreeActive(true);
      selectionModel.clearSelections();
      selectionModel.selectSingleNode(node);

      node.getTreeNodeElement().scrollIntoView();
    }

    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                delegate.onTreeNodeSelected(node);
              }
            });
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

  @Nullable
  @Override
  public ProcessTreeNode getSelectedTreeNode() {
    List<ProcessTreeNode> selectedNodes = processTree.getSelectionModel().getSelectedNodes();
    if (!selectedNodes.isEmpty()) {
      return selectedNodes.get(0);
    }
    return null;
  }

  @Override
  @Nullable
  public ProcessTreeNode getNodeByIndex(int index) {
    return (ProcessTreeNode) processTreeNodes.values().toArray()[index];
  }

  @Override
  @Nullable
  public ProcessTreeNode getNodeById(String nodeId) {
    return processTreeNodes.get(nodeId);
  }

  @Override
  public void addProcessNode(ProcessTreeNode node) {
    processTreeNodes.put(node.getId(), node);
  }

  @Override
  public void removeProcessNode(ProcessTreeNode node) {
    processTreeNodes.remove(node.getId());
  }

  @Override
  public void setProcessesData(ProcessTreeNode root) {
    splitLayoutPanel.setWidgetHidden(navigationPanel, false);
    navigationPanelVisible = true;

    processTree.asWidget().setVisible(true);
    processTree.getModel().setRoot(root);
    processTree.renderTree();

    for (ProcessTreeNode processTreeNode : processTreeNodes.values()) {
      if (!processTreeNode.getId().equals(activeProcessId) && processTreeNode.hasUnreadContent()) {
        processTreeNode
            .getTreeNodeElement()
            .getClassList()
            .add(machineResources.getCss().badgeVisible());
      }
    }
  }

  @Override
  public void setStopButtonVisibility(String nodeId, boolean visible) {
    ProcessTreeNode processTreeNode = processTreeNodes.get(nodeId);
    if (processTreeNode == null) {
      return;
    }

    if (visible) {
      processTreeNode
          .getTreeNodeElement()
          .getClassList()
          .remove(machineResources.getCss().hideStopButton());
    } else {
      processTreeNode
          .getTreeNodeElement()
          .getClassList()
          .add(machineResources.getCss().hideStopButton());
    }
  }

  @Override
  public void showProcessOutput(String processId) {
    if (!processWidgets.containsKey(processId)) {
      processId = "";
    }

    onResize();

    final WidgetToShow widgetToShow = processWidgets.get(processId);
    if (!focusedSubPanel.getAllWidgets().contains(widgetToShow)) {
      return;
    }

    final SubPanel subPanel = widget2Panels.get(widgetToShow);
    if (subPanel != null) {
      subPanel.activateWidget(widgetToShow);
    }

    activeProcessId = processId;

    final ProcessTreeNode treeNode = processTreeNodes.get(processId);
    if (treeNode != null && !MACHINE_NODE.equals(treeNode.getType())) {
      treeNode.setHasUnreadContent(false);
      treeNode.getTreeNodeElement().getClassList().remove(machineResources.getCss().badgeVisible());
    }
  }

  @Override
  public void hideProcessOutput(String processId) {
    final WidgetToShow widgetToShow = processWidgets.get(processId);
    final SubPanel subPanel = widget2Panels.get(widgetToShow);
    if (subPanel != null) {
      subPanel.removeWidget(widgetToShow);
    }
    processWidgets.remove(processId);
  }

  @Override
  public void removeWidget(String processId) {
    WidgetToShow widget = processWidgets.get(processId);
    hideProcessOutput(processId);
    widget2Panels.remove(widget);
  }

  @Override
  public void markProcessHasOutput(String processId) {
    if (processId.equals(activeProcessId)) {
      return;
    }

    final ProcessTreeNode treeNode = processTreeNodes.get(processId);
    if (treeNode != null) {
      treeNode.setHasUnreadContent(true);
      treeNode.getTreeNodeElement().getClassList().add(machineResources.getCss().badgeVisible());
    }
  }

  @Override
  public void clear() {
    for (WidgetToShow widgetToShow : processWidgets.values()) {
      SubPanel subPanel = widget2Panels.get(widgetToShow);
      subPanel.removeWidget(widgetToShow);
    }

    processWidgets.clear();
  }

  @Override
  public void focusGained(SubPanel subPanel, IsWidget widget) {
    focusedSubPanel = subPanel;

    final ProcessTreeNode processTreeNode = widget2TreeNodes.get(widget);
    if (processTreeNode != null) {
      selectNode(processTreeNode);
    }

    if (lastFosuced != null && !lastFosuced.equals(widget)) {
      lastFosuced.setFocus(false);
    }

    if (widget instanceof Focusable) {
      ((Focusable) widget).setFocus(true);

      lastFosuced = (Focusable) widget;
    }
  }

  @Override
  public void onDoubleClicked(final SubPanel panel, final IsWidget widget) {
    delegate.onToggleMaximizeConsole();
  }

  @Override
  public void onResize() {
    for (WidgetToShow widgetToShow : widget2Panels.keySet()) {
      final IsWidget widget = widgetToShow.getWidget();
      if (widget instanceof RequiresResize) {
        ((RequiresResize) widget).onResize();
      }
    }

    for (SubPanel panel : widget2Panels.values()) {
      if (panel.getView() instanceof RequiresResize) {
        ((RequiresResize) panel.getView()).onResize();
      }
    }
  }

  @Override
  public void setProcessesTreeVisible(boolean visible) {
    splitLayoutPanel.setWidgetHidden(navigationPanel, !visible);
    navigationPanelVisible = visible;
  }

  @Override
  public void onAddTabButtonClicked(int mouseX, int mouseY) {
    delegate.onAddTabButtonClicked(mouseX, mouseY);
  }

  @Override
  public boolean isProcessesTreeVisible() {
    return navigationPanelVisible;
  }

  interface ProcessesPartViewImplUiBinder extends UiBinder<Widget, ProcessesPanelViewImpl> {}
}

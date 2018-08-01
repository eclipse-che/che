/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.ide.ui.smartTree.SortDir.ASC;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.debug.BreakpointResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.status.StatusText;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.DebuggerNodeFactory;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator.DebugNodeTypeComparator;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator.VariableNodeComparator;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.key.DebugNodeUniqueKeyProvider;

/**
 * The class business logic which allow us to change visual representation of debugger panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Oleksandr Andriienko
 */
@Singleton
public class DebuggerViewImpl extends BaseView<DebuggerView.ActionDelegate>
    implements DebuggerView {

  interface DebuggerViewImplUiBinder extends UiBinder<Widget, DebuggerViewImpl> {}

  @UiField Label vmName;
  @UiField Label executionPoint;
  @UiField SimplePanel toolbarPanel;
  @UiField ScrollPanel breakpointsPanel;
  @UiField SimplePanel watchExpressionPanel;

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField(provided = true)
  Resources coreRes;

  @UiField(provided = true)
  SplitLayoutPanel splitPanel = new SplitLayoutPanel(3);

  @UiField(provided = true)
  Tree tree;

  @UiField ListBox threads;
  @UiField ScrollPanel framesPanel;
  @UiField FlowPanel threadNotSuspendedPlaceHolder;

  private final SimpleList<ActiveBreakpointWrapper> breakpoints;
  private final SimpleList<StackFrameDump> frames;
  private final BreakpointResources breakpointResources;

  private final DebuggerNodeFactory nodeFactory;
  private final DebugNodeUniqueKeyProvider nodeKeyProvider;

  @Inject
  protected DebuggerViewImpl(
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      Resources coreRes,
      DebuggerViewImplUiBinder uiBinder,
      BreakpointResources breakpointResources,
      DebuggerNodeFactory nodeFactory,
      DebugNodeUniqueKeyProvider nodeKeyProvider) {
    super();

    this.locale = locale;
    this.coreRes = coreRes;
    this.breakpointResources = breakpointResources;
    this.nodeKeyProvider = nodeKeyProvider;

    StatusText<Tree> emptyTreeStatus = new StatusText<>();
    emptyTreeStatus.setText("");

    tree = new Tree(new NodeStorage(nodeKeyProvider), new NodeLoader(), emptyTreeStatus);
    setContentWidget(uiBinder.createAndBindUi(this));

    this.breakpoints = createBreakpointList();
    this.breakpointsPanel.add(breakpoints);

    this.frames = createFramesList();
    this.framesPanel.add(frames);
    this.nodeFactory = nodeFactory;

    tree.ensureDebugId("debugger-tree");

    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.addExpandHandler(
        event -> {
          Node expandedNode = event.getNode();
          if (expandedNode instanceof VariableNode) {
            delegate.onExpandVariable(((VariableNode) expandedNode).getData());
          }
        });

    tree.getNodeStorage()
        .addSortInfo(new NodeStorage.StoreSortInfo(new DebugNodeTypeComparator(), ASC));
    tree.getNodeStorage()
        .addSortInfo(new NodeStorage.StoreSortInfo(new VariableNodeComparator(), ASC));

    watchExpressionPanel.addStyleName(resources.getCss().watchExpressionsPanel());
  }

  @Override
  public void setExecutionPoint(@Nullable Location location) {
    StringBuilder labelText = new StringBuilder();
    if (location != null) {
      labelText
          .append(Path.valueOf(location.getTarget()).lastSegment())
          .append(":")
          .append(location.getLineNumber());
    }
    executionPoint.getElement().addClassName(coreRes.coreCss().defaultFont());
    executionPoint.setText(labelText.toString());
  }

  @Override
  public void removeAllVariables() {
    for (Node node : tree.getNodeStorage().getAll()) {
      if (node instanceof VariableNode) {
        tree.getNodeStorage().remove(node);
      }
    }
  }

  @Override
  public void setVariables(@NotNull List<? extends Variable> variables) {
    for (Variable variable : variables) {
      VariableNode node = nodeFactory.createVariableNode(variable);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void expandVariable(Variable variable) {
    String key = nodeKeyProvider.evaluateKey(variable);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null) {
      tree.getNodeStorage().update(nodeToUpdate);
      List<? extends Variable> varChildren = variable.getValue().getVariables();
      for (int i = 0; i < varChildren.size(); i++) {
        Node childNode = nodeFactory.createVariableNode(varChildren.get(i));
        tree.getNodeStorage().insert(nodeToUpdate, i, childNode);
      }
    }
  }

  @Override
  public void updateVariable(Variable variable) {
    String key = nodeKeyProvider.evaluateKey(variable);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null && nodeToUpdate instanceof VariableNode) {
      VariableNode variableNode = ((VariableNode) nodeToUpdate);
      variableNode.setData(variable);
      tree.getNodeStorage().update(variableNode);

      if (tree.isExpanded(nodeToUpdate)) {
        tree.getNodeLoader().loadChildren(variableNode);
      } else {
        tree.refresh(nodeToUpdate);
      }
    }
  }

  @Override
  public void addExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    if (tree.getNodeStorage().findNodeWithKey(key) == null) {
      WatchExpressionNode node = nodeFactory.createExpressionNode(expression);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void updateExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null && nodeToUpdate instanceof WatchExpressionNode) {
      WatchExpressionNode expNode = ((WatchExpressionNode) nodeToUpdate);
      expNode.setData(expression);
      tree.getNodeStorage().update(nodeToUpdate);
      tree.refresh(nodeToUpdate);
    }
  }

  @Override
  public void removeExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    Node nodeToRemove = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToRemove != null) {
      tree.getNodeStorage().remove(nodeToRemove);
    }
  }

  @Override
  public void setBreakpoints(@NotNull List<ActiveBreakpointWrapper> breakpoints) {
    this.breakpoints.render(breakpoints);
  }

  @Override
  public void setThreadDump(List<? extends ThreadState> threadDump, long threadIdToSelect) {
    threads.clear();

    for (int i = 0; i < threadDump.size(); i++) {
      ThreadState ts = threadDump.get(i);

      StringBuilder title = new StringBuilder();
      title.append("\"");
      title.append(ts.getName());
      title.append("\"@");
      title.append(ts.getId());
      title.append(" in group \"");
      title.append(ts.getGroupName());
      title.append("\": ");
      title.append(ts.getStatus());

      threads.addItem(title.toString(), String.valueOf(ts.getId()));
      if (ts.getId() == threadIdToSelect) {
        threads.setSelectedIndex(i);
      }
    }
  }

  @Override
  public void setFrames(List<? extends StackFrameDump> stackFrameDumps) {
    frames.render(new ArrayList<>(stackFrameDumps));
    if (!stackFrameDumps.isEmpty()) {
      frames.getSelectionModel().setSelectedItem(0);
    }
  }

  @Override
  public void setVMName(@Nullable String name) {
    vmName.setText(name == null ? "" : name);
  }

  @Override
  public Variable getSelectedVariable() {
    Node selectedNode = getSelectedNode();
    if (selectedNode instanceof VariableNode) {
      return ((VariableNode) selectedNode).getData();
    }
    return null;
  }

  @Override
  public WatchExpression getSelectedExpression() {
    Node selectedNode = getSelectedNode();
    if (selectedNode instanceof WatchExpressionNode) {
      return ((WatchExpressionNode) selectedNode).getData();
    }
    return null;
  }

  private Node getSelectedNode() {
    if (tree.getSelectionModel().getSelectedNodes().isEmpty()) {
      return null;
    }
    return tree.getSelectionModel().getSelectedNodes().get(0);
  }

  @Override
  public AcceptsOneWidget getDebuggerToolbarPanel() {
    return toolbarPanel;
  }

  @Override
  public AcceptsOneWidget getDebuggerWatchToolbarPanel() {
    return watchExpressionPanel;
  }

  @Override
  public long getSelectedThreadId() {
    String selectedValue = threads.getSelectedValue();
    return selectedValue == null ? -1 : Integer.parseInt(selectedValue);
  }

  @Override
  public int getSelectedFrameIndex() {
    return frames.getSelectionModel().getSelectedIndex();
  }

  public void setThreadNotSuspendPlaceHolderVisible(boolean visible) {
    threadNotSuspendedPlaceHolder.setVisible(visible);
  }

  @UiHandler({"threads"})
  void onThreadChanged(ChangeEvent event) {
    delegate.onSelectedThread(Integer.parseInt(threads.getSelectedValue()));
  }

  private SimpleList<ActiveBreakpointWrapper> createBreakpointList() {
    TableElement breakPointsElement = Elements.createTableElement();
    breakPointsElement.setAttribute("style", "width: 100%; border: none;");

    SimpleList.ListEventDelegate<ActiveBreakpointWrapper> breakpointListEventDelegate =
        new SimpleList.ListEventDelegate<ActiveBreakpointWrapper>() {
          public void onListItemClicked(Element itemElement, ActiveBreakpointWrapper itemData) {
            breakpoints.getSelectionModel().setSelectedItem(itemData);
          }

          @Override
          public void onListItemContextMenu(
              int clientX, int clientY, ActiveBreakpointWrapper itemData) {
            delegate.onBreakpointContextMenu(clientX, clientY, itemData.getBreakpoint());
          }

          @Override
          public void onListItemDoubleClicked(
              Element listItemBase, ActiveBreakpointWrapper itemData) {
            delegate.onBreakpointDoubleClick(itemData.getBreakpoint());
          }
        };

    return SimpleList.create(
        (SimpleList.View) breakPointsElement,
        coreRes.defaultSimpleListCss(),
        new BreakpointItemRender(breakpointResources),
        breakpointListEventDelegate);
  }

  private SimpleList<StackFrameDump> createFramesList() {
    TableElement frameElement = Elements.createTableElement();
    frameElement.setAttribute("style", "width: 100%; border: none;");

    SimpleList.ListEventDelegate<StackFrameDump> frameListEventDelegate =
        new SimpleList.ListEventDelegate<StackFrameDump>() {
          public void onListItemClicked(Element itemElement, StackFrameDump itemData) {
            frames.getSelectionModel().setSelectedItem(itemData);
            delegate.onSelectedFrame(frames.getSelectionModel().getSelectedIndex());
          }
        };

    return SimpleList.create(
        (SimpleList.View) frameElement,
        coreRes.defaultSimpleListCss(),
        new FrameItemRender(),
        frameListEventDelegate);
  }
}

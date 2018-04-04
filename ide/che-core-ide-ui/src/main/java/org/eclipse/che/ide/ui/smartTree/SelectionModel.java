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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.HasSelectionChangedHandlers;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent.StoreAddHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.StoreRecordChangeHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler;
import org.eclipse.che.ide.ui.smartTree.event.internal.NativeTreeEvent;
import org.eclipse.che.ide.ui.smartTree.handler.GroupingHandlerRegistration;

/** @author Vlad Zhukovskiy */
public class SelectionModel
    implements HasSelectionHandlers<Node>,
        HasBeforeSelectionHandlers<Node>,
        HasSelectionChangedHandlers {
  private class TreeMouseHandler implements MouseDownHandler, ClickHandler {
    @Override
    public void onClick(ClickEvent event) {}

    @Override
    public void onMouseDown(MouseDownEvent event) {
      SelectionModel.this.onMouseDown(event);
    }
  }

  private class TreeStorageHandler
      implements StoreAddHandler,
          StoreRemoveHandler,
          StoreClearHandler,
          StoreRecordChangeHandler,
          StoreUpdateHandler {

    @Override
    public void onAdd(StoreAddEvent event) {
      SelectionModel.this.onAdd(event.getNodes());
    }

    @Override
    public void onClear(StoreClearEvent event) {
      SelectionModel.this.onClear(event);
    }

    @Override
    public void onRecordChange(final StoreRecordChangeEvent event) {
      Scheduler.get()
          .scheduleFinally(
              new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                  SelectionModel.this.onRecordChange(event);
                }
              });
    }

    @Override
    public void onRemove(StoreRemoveEvent event) {
      SelectionModel.this.onRemove(event.getNode());
    }

    @Override
    public void onUpdate(StoreUpdateEvent event) {
      final List<Node> update = event.getNodes();
      // run defer to ensure the code runs after grid view refreshes row
      Scheduler.get()
          .scheduleFinally(
              new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                  for (Node anUpdate : update) {
                    SelectionModel.this.onUpdate(anUpdate);
                  }
                }
              });
    }
  }

  protected KeyboardNavigationHandler keyNav =
      new KeyboardNavigationHandler() {
        /** {@inheritDoc} */
        @Override
        public void onDown(NativeEvent evt) {
          onKeyDown(evt);
        }

        /** {@inheritDoc} */
        @Override
        public void onLeft(NativeEvent evt) {
          onKeyLeft(evt);
        }

        /** {@inheritDoc} */
        @Override
        public void onRight(NativeEvent evt) {
          onKeyRight(evt);
        }

        /** {@inheritDoc} */
        @Override
        public void onUp(NativeEvent evt) {
          onKeyUp(evt);
        }

        @Override
        public void onEnd(NativeEvent evt) {
          onKeyEnd(evt);
        }

        @Override
        public void onHome(NativeEvent evt) {
          onKeyHome(evt);
        }

        @Override
        public void onPageDown(NativeEvent evt) {
          onKeyPageDown(evt);
        }

        @Override
        public void onPageUp(NativeEvent evt) {
          onKeyPageUp(evt);
        }

        @Override
        public void onEsc(NativeEvent evt) {
          onKeyEsc(evt);
        }

        @Override
        public void onEnter(NativeEvent evt) {
          onKeyEnter(evt);
        }
      };

  protected Tree tree;

  protected NodeStorage nodeStorage;

  private TreeMouseHandler treeMouseHandler = new TreeMouseHandler();
  private TreeStorageHandler treeStorageHandler = new TreeStorageHandler();
  private GroupingHandlerRegistration handlerRegistration;
  protected List<Node> selectionStorage = new ArrayList<>();
  protected Node lastSelectedNode;
  protected Mode selectionMode = Mode.MULTI;
  private Node lastFocused;
  private HandlerManager handlerManager;
  protected boolean mouseDown;
  protected boolean fireSelectionChangeOnClick;

  public static enum Mode {
    SINGLE,
    SIMPLE,
    MULTI
  }

  public SelectionModel() {}

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addSelectionHandler(SelectionHandler<Node> handler) {
    return ensureHandlers().addHandler(SelectionEvent.getType(), handler);
  }

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<Node> handler) {
    return ensureHandlers().addHandler(BeforeSelectionEvent.getType(), handler);
  }

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addSelectionChangedHandler(SelectionChangedHandler handler) {
    return ensureHandlers().addHandler(SelectionChangedEvent.getType(), handler);
  }

  protected HandlerManager ensureHandlers() {
    if (handlerManager == null) {
      handlerManager = new HandlerManager(this);
    }
    return handlerManager;
  }

  /** {@inheritDoc} */
  @Override
  public void fireEvent(GwtEvent<?> event) {
    if (handlerManager != null) {
      handlerManager.fireEvent(event);
    }
  }

  public void bindTree(Tree tree) {
    if (this.tree != null) {
      handlerRegistration.removeHandler();
      keyNav.bind(null);
      bindStorage(null);
      nodeStorage = null;
    }

    this.tree = tree;

    if (tree != null) {
      if (handlerRegistration == null) {
        handlerRegistration = new GroupingHandlerRegistration();
      }
      handlerRegistration.add(tree.addDomHandler(treeMouseHandler, MouseDownEvent.getType()));
      handlerRegistration.add(tree.addDomHandler(treeMouseHandler, ClickEvent.getType()));
      keyNav.bind(tree);
      bindStorage(tree.getNodeStorage());
      nodeStorage = tree.getNodeStorage();
    }
  }

  public void bindStorage(NodeStorage store) {
    deselectAll();
    if (this.nodeStorage != null) {
      handlerRegistration.removeHandler();
    }
    this.nodeStorage = store;
    if (store != null) {
      if (handlerRegistration == null) {
        handlerRegistration = new GroupingHandlerRegistration();
      }

      handlerRegistration.add(store.addStoreAddHandler(treeStorageHandler));
      handlerRegistration.add(store.addStoreRemoveHandler(treeStorageHandler));
      handlerRegistration.add(store.addStoreClearHandler(treeStorageHandler));
      handlerRegistration.add(store.addStoreUpdateHandler(treeStorageHandler));
      handlerRegistration.add(store.addStoreRecordChangeHandler(treeStorageHandler));
    }
  }

  public Tree getTree() {
    return tree;
  }

  public boolean isSelected(Node node) {
    return selectionStorage.contains(node);
  }

  public void selectNext() {
    Node next = next();
    if (next != null) {
      doSingleSelect(next, false);
    }
  }

  public void selectPrevious() {
    Node prev = prev();
    if (prev != null) {
      doSingleSelect(prev, false);
    }
  }

  protected Node next() {
    Node sel = lastSelectedNode;
    if (sel == null) {
      return null;
    }

    Node first = nodeStorage.getFirstChild(sel);

    if (first != null && tree.isExpanded(sel)) {
      return first;
    } else {
      Node nextSibling = nodeStorage.getNextSibling(sel);
      if (nextSibling != null) {
        return nextSibling;
      } else {
        Node p = nodeStorage.getParent(sel);
        while (p != null) {
          nextSibling = nodeStorage.getNextSibling(p);
          if (nextSibling != null) {
            return nextSibling;
          }
          p = nodeStorage.getParent(p);
        }
      }
    }
    return null;
  }

  protected Node prev() {
    Node sel = lastSelectedNode;
    if (sel == null) {
      return null;
    }
    Node prev = nodeStorage.getPreviousSibling(sel);
    if (prev != null) {
      if ((!tree.isExpanded(prev) || nodeStorage.getChildCount(prev) < 1)) {
        return prev;
      } else {
        Node lastChild = nodeStorage.getLastChild(prev);
        while (lastChild != null
            && nodeStorage.getChildCount(lastChild) > 0
            && tree.isExpanded(lastChild)) {
          lastChild = nodeStorage.getLastChild(lastChild);
        }
        return lastChild;
      }
    } else {
      Node parent = nodeStorage.getParent(sel);
      if (parent != null) {
        return parent;
      }
    }
    return null;
  }

  protected void onKeyDown(NativeEvent e) {
    e.preventDefault();
    Node next = next();
    if (next != null) {
      doSingleSelect(next, false);
      tree.scrollIntoView(next);
    }
  }

  protected void onKeyLeft(NativeEvent ce) {
    ce.preventDefault();
    if (lastSelectedNode != null
        && !tree.isLeaf(lastSelectedNode)
        && tree.isExpanded(lastSelectedNode)) {
      tree.setExpanded(lastSelectedNode, false, true);
    } else if (lastSelectedNode != null && nodeStorage.getParent(lastSelectedNode) != null) {
      doSingleSelect(nodeStorage.getParent(lastSelectedNode), false);
    }
  }

  protected void onKeyRight(NativeEvent ce) {
    ce.preventDefault();
    if (lastSelectedNode != null
        && !tree.isLeaf(lastSelectedNode)
        && !tree.isExpanded(lastSelectedNode)) {
      tree.setExpanded(lastSelectedNode, true);
    }
  }

  protected void onKeyUp(NativeEvent ke) {
    NativeTreeEvent e = ke.cast();
    e.preventDefault();
    Node prev = prev();
    if (prev != null) {
      doSingleSelect(prev, false);
      tree.scrollIntoView(prev);
    }
  }

  private void onKeyEsc(NativeEvent evt) {
    evt.preventDefault();
    deselectAll();
  }

  private void onKeyEnter(NativeEvent evt) {
    for (Node node : selectionStorage) {
      if (node instanceof HasAction) {
        ((HasAction) node).actionPerformed();
      }

      if (!node.isLeaf()) {
        tree.toggle(node);
      }
    }
  }

  private void onKeyEnd(NativeEvent evt) {
    evt.preventDefault();
    // TODO implement this feature
  }

  private void onKeyHome(NativeEvent evt) {
    evt.preventDefault();

    // TODO implement this feature
  }

  private void onKeyPageDown(NativeEvent evt) {
    evt.preventDefault();
    // TODO implement this feature
  }

  private void onKeyPageUp(NativeEvent evt) {
    evt.preventDefault();
    // TODO implement this feature
  }

  protected void onMouseClick(ClickEvent ce) {
    NativeTreeEvent e = ce.getNativeEvent().cast();

    if (fireSelectionChangeOnClick) {
      fireSelectionChange();
      fireSelectionChangeOnClick = false;
    }

    if (selectionMode == Mode.MULTI) {
      NodeDescriptor node = tree.getNodeDescriptor((Element) e.getEventTarget().cast());
      // on dnd prevent drag the node will be null
      if (node != null) {
        Node sel = node.getNode();
        if (e.getCtrlOrMetaKey() && isSelected(sel)) {
          doDeselect(Collections.singletonList(sel), false);
          tree.setFocus(true);

          // reset the starting location of the click when meta is used during a multiselect
          lastSelectedNode = sel;
        } else if (e.getCtrlOrMetaKey()) {
          doSelect(Collections.singletonList(sel), true, false);
          tree.setFocus(true);

          // reset the starting location of the click when meta is used during a multiselect
          lastSelectedNode = sel;
        } else if (isSelected(sel)
            && !e.getShiftKey()
            && !e.getCtrlOrMetaKey()
            && selectionStorage.size() > 0) {
          doSelect(Collections.singletonList(sel), false, false);
          tree.setFocus(true);
        }
      }
    }
  }

  protected void onMouseDown(MouseDownEvent mde) {
    NativeTreeEvent e = mde.getNativeEvent().cast();
    Element target = e.getEventTargetEl();
    NodeDescriptor selNode = tree.getNodeDescriptor(target);

    if (selNode == null || tree == null) {
      return;
    }

    Node sel = selNode.getNode();
    if (!tree.getView().isSelectableTarget(sel, target)) {
      return;
    }

    boolean isSelected = isSelected(sel);
    boolean isMeta = e.getCtrlOrMetaKey();
    boolean isShift = e.getShiftKey();

    if (e.isRightClick() && isSelected) {
      return;
    } else {
      switch (selectionMode) {
        case SIMPLE:
          tree.setFocus(true);
          if (isSelected(sel)) {
            deselect(sel);
          } else {
            doSelect(Collections.singletonList(sel), true, false);
          }
          break;

        case SINGLE:
          tree.setFocus(true);
          if (isMeta && isSelected) {
            deselect(sel);
          } else if (!isSelected) {
            select(sel, false);
          }
          break;

        case MULTI:
          if (isShift && lastSelectedNode != null) {
            List<Node> selectedItems = new ArrayList<>();

            // from last selected or firstly selected
            NodeDescriptor lastSelTreeNode = tree.getNodeDescriptor(lastSelectedNode);
            Element lastSelTreeEl = tree.getView().getRootContainer(lastSelTreeNode);

            // to selected or secondly selected
            NodeDescriptor selTreeNode = tree.getNodeDescriptor(sel);
            Element selTreeNodeEl = tree.getView().getRootContainer(selTreeNode);

            // holding shift down, selecting the same item again, selecting itself
            if (sel == lastSelectedNode) {
              tree.setFocus(true);
              doSelect(Collections.singletonList(sel), false, false);

            } else if (lastSelTreeEl != null && selTreeNodeEl != null) {
              // add the last selected, as its not added during the walk
              selectedItems.add(lastSelectedNode);

              // After walking reset back to previously selected
              final Node previouslyLastSelected = lastSelectedNode;

              // This deals with flipping directions
              if (lastSelTreeEl.getAbsoluteTop() < selTreeNodeEl.getAbsoluteTop()) {
                // down selection
                Node next = next();
                while (next != null) {
                  selectedItems.add(next);
                  lastSelectedNode = next;
                  if (next == sel) break;
                  next = next();
                }

              } else {
                // up selection
                Node prev = prev();
                while (prev != null) {
                  selectedItems.add(prev);
                  lastSelectedNode = prev;
                  if (prev == sel) break;
                  prev = prev();
                }
              }

              tree.setFocus(true);
              doSelect(selectedItems, false, false);

              // change back to last selected, the walking causes this need
              lastSelectedNode = previouslyLastSelected;
            }

          } else if (!isSelected(sel)) {
            tree.setFocus(true);
            doSelect(Collections.singletonList(sel), e.getCtrlOrMetaKey(), false);

            // reset the starting location of multi select
            lastSelectedNode = sel;
          } else if (isSelected(sel)
              && !e.getShiftKey()
              && !e.getCtrlOrMetaKey()
              && !selectionStorage.isEmpty()) {
            doSelect(Collections.singletonList(sel), false, false);
            tree.setFocus(true);
          } else if (isSelected(sel) && !selectionStorage.isEmpty()) {
            doDeselect(Collections.singletonList(sel), false);
          }
          break;
      }
    }

    mouseDown = false;
  }

  protected void onSelectChange(Node node, boolean select) {
    tree.getView().onSelectChange(node, select);
  }

  public void deselectAll() {
    doDeselect(new ArrayList<>(selectionStorage), false);
  }

  protected void doDeselect(List<Node> nodes, boolean suppressEvent) {
    boolean change = false;
    for (Node node : nodes) {
      if (selectionStorage.remove(node)) {
        if (lastSelectedNode == node) {
          lastSelectedNode =
              selectionStorage.size() > 0
                  ? selectionStorage.get(selectionStorage.size() - 1)
                  : null;
        }
        onSelectChange(node, false);
        change = true;
      }
    }
    if (!suppressEvent && change) {
      fireSelectionChange();
    }
  }

  protected void doSelect(List<Node> nodes, boolean keepExisting, boolean suppressEvent) {
    if (selectionMode == Mode.SINGLE) {
      Node node = nodes.size() > 0 ? nodes.get(0) : null;
      if (node != null) {
        doSingleSelect(node, suppressEvent);
      }
    } else {
      doMultiSelect(nodes, keepExisting, suppressEvent);
    }
  }

  protected void doSingleSelect(Node node, boolean suppressEvent) {
    int index;
    index = nodeStorage.indexOf(node);
    if (index == -1 || isSelected(node)) {
      return;
    } else {
      if (!suppressEvent) {
        BeforeSelectionEvent<Node> evt = BeforeSelectionEvent.fire(this, node);
        if (evt != null && evt.isCanceled()) {
          return;
        }
      }
    }

    boolean change = false;
    if (selectionStorage.size() > 0 && !isSelected(node)) {
      doDeselect(Collections.singletonList(lastSelectedNode), true);
      change = true;
    }

    if (selectionStorage.size() == 0) {
      change = true;
    }

    selectionStorage.add(node);
    lastSelectedNode = node;
    onSelectChange(node, true);
    setLastFocused(lastSelectedNode);

    if (!suppressEvent) {
      SelectionEvent.fire(this, node);
    }

    if (change && !suppressEvent) {
      fireSelectionChange();
    }
  }

  protected void doMultiSelect(List<Node> nodes, boolean keepExisting, boolean suppressEvent) {
    boolean change = false;
    if (!keepExisting && selectionStorage.size() > 0) {
      change = true;
      doDeselect(new ArrayList<>(selectionStorage), true);
    }

    for (Node node : nodes) {

      if (tree.getNodeDescriptor(node) == null) {
        continue;
      }

      boolean isSelected = isSelected(node);
      if (!suppressEvent && !isSelected) {
        BeforeSelectionEvent<Node> evt = BeforeSelectionEvent.fire(this, node);
        if (evt != null && evt.isCanceled()) {
          continue;
        }
      }

      change = true;
      lastSelectedNode = node;

      selectionStorage.add(node);
      setLastFocused(lastSelectedNode);

      if (!isSelected) {
        onSelectChange(node, true);
        if (!suppressEvent) {
          SelectionEvent.fire(this, node);
        }
      }
    }

    if (change && !suppressEvent) {
      fireSelectionChange();
    }
  }

  public void deselect(Node item) {
    deselect(Collections.singletonList(item));
  }

  public void deselect(List<Node> items) {
    doDeselect(items, false);
  }

  public void deselect(Node... items) {
    deselect(Arrays.asList(items));
  }

  public void select(Node item, boolean keepExisting) {
    select(Collections.singletonList(item), keepExisting);
  }

  public void select(List<Node> items, boolean keepExisting) {
    doSelect(items, keepExisting, false);
  }

  protected void setLastFocused(Node lastFocused) {
    Node lf = this.lastFocused;
    this.lastFocused = lastFocused;

    onLastFocusedChange(lf, lastFocused);
  }

  public void refresh() {
    List<Node> sel = new ArrayList<>();
    boolean change = false;
    for (Node node : selectionStorage) {
      Node storeNode = nodeStorage.findNode(node);
      if (storeNode != null) {
        sel.add(storeNode);
      }
    }

    if (sel.size() != selectionStorage.size()) {
      change = true;
    }

    selectionStorage.clear();
    lastSelectedNode = null;
    setLastFocused(null);
    doSelect(sel, false, true);
    if (change) {
      fireSelectionChange();
    }
  }

  protected void fireSelectionChange() {
    if (mouseDown) {
      fireSelectionChangeOnClick = true;
    } else {
      fireEvent(new SelectionChangedEvent(selectionStorage));
    }
  }

  protected void onLastFocusedChange(Node oldFocused, Node newFocused) {
    // temporary stub
  }

  public Mode getSelectionMode() {
    return selectionMode;
  }

  public void setSelectionMode(Mode selectionMode) {
    this.selectionMode = selectionMode;
  }

  public void setSelection(List<Node> selection) {
    select(selection, false);
  }

  protected void onAdd(List<? extends Node> models) {
    // temporary stub
  }

  protected void onClear(StoreClearEvent event) {
    int oldSize = selectionStorage.size();
    selectionStorage.clear();
    lastSelectedNode = null;
    setLastFocused(null);
    if (oldSize > 0) fireSelectionChange();
  }

  protected void onRecordChange(StoreRecordChangeEvent event) {
    // temporary stub
  }

  protected Node getLastFocused() {
    return lastFocused;
  }

  protected void onUpdate(Node model) {
    for (int i = 0; i < selectionStorage.size(); i++) {
      Node m = selectionStorage.get(i);
      if (nodeStorage.hasMatchingKey(model, m)) {
        if (m != model) {
          selectionStorage.remove(m);
          selectionStorage.add(i, model);
        }
        if (lastSelectedNode == m) {
          lastSelectedNode = model;
        }
        break;
      }
    }
    if (getLastFocused() != null
        && model != getLastFocused()
        && nodeStorage.hasMatchingKey(model, getLastFocused())) {
      lastFocused = model;
    }
  }

  protected void onRemove(Node model) {
    if (selectionStorage.remove(model)) {
      fireSelectionChange();
    }
  }

  public List<Node> getSelectedNodes() {
    return Collections.unmodifiableList(selectionStorage);
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent;
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent;
import org.eclipse.che.ide.ui.smartTree.handler.GroupingHandlerRegistration;

/**
 * Hierarchical type storage. Based on Parent-Child relationship, which is uses internally by tree.
 * To obtain parent or children uses specified methods such as {@link #getParent(Node)} or {@link
 * #getChildren(Node)}. Modifications performs by calling such methods like {@link #add(Node)} or
 * {@link #remove(Node)}, etc.
 *
 * @author Vlad Zhukovskiy
 */
public class NodeStorage implements StoreHandlers.HasStoreHandlers {

  private final NodeDescriptor roots = new NodeDescriptor(this, null);
  private final Map<String, NodeDescriptor> idToNodeMap = new HashMap<>();

  private HandlerManager handlerManager;
  private UniqueKeyProvider<Node> keyProvider;

  private List<StoreSortInfo> comparators = new ArrayList<>();

  public NodeStorage() {
    this(
        new NodeUniqueKeyProvider() {
          @Override
          public String getKey(@NotNull Node item) {
            return String.valueOf(item.hashCode());
          }
        });
  }

  public NodeStorage(UniqueKeyProvider<Node> keyProvider) {
    this.keyProvider = keyProvider;
  }

  protected boolean isSorted() {
    return comparators.size() != 0;
  }

  /**
   * Adds the data models as roots of the tree.
   *
   * @param nodes
   */
  public void add(List<Node> nodes) {
    if (nodes.size() == 1) {
      insert(roots, roots.getChildren().size(), nodes.get(0));
    } else {
      insert(roots, roots.getChildren().size(), nodes);
    }
  }

  /**
   * Adds the given data model as a root of the tree.
   *
   * @param node
   */
  public void add(Node node) {
    insert(roots, roots.getChildren().size(), node);
  }

  /**
   * Adds the list of children to the end of the visible children of the given parent model
   *
   * @param parent
   * @param children
   */
  public void add(Node parent, List<Node> children) {
    NodeDescriptor nodeDescriptor = getWrapper(parent);
    if (children.size() == 1) {
      insert(nodeDescriptor, nodeDescriptor.getChildren().size(), children.get(0));
    } else {
      insert(nodeDescriptor, nodeDescriptor.getChildren().size(), children);
    }
  }

  /**
   * Adds the child to the end of the visible children of the parent model
   *
   * @param parent
   * @param child
   */
  public void add(Node parent, Node child) {
    NodeDescriptor nodeDescriptor = getWrapper(parent);
    insert(nodeDescriptor, nodeDescriptor.getChildren().size(), child);
  }

  /**
   * Imports a list of subtrees at the given position in the root of the tree.
   *
   * @param index
   * @param children
   */
  public void addSubTree(int index, List<Node> children) {
    List<NodeDescriptor> nodeDescriptors = convertTreeNodesHelper(children);
    roots.addChildren(index, nodeDescriptors);

    List<Node> nodes = new ArrayList<>();
    for (NodeDescriptor child : nodeDescriptors) {
      nodes.add(child.getNode());
    }

    if (!nodes.isEmpty()) {
      fireEvent(new StoreAddEvent(index, nodes));
    }
  }

  /**
   * Imports a list of subtrees to append to the given parent object already present in the tree.
   *
   * @param parent
   * @param index
   * @param children
   */
  public void addSubTree(Node parent, int index, List<Node> children) {
    List<NodeDescriptor> nodeDescriptors = convertTreeNodesHelper(children);
    getWrapper(parent).addChildren(index, nodeDescriptors);

    List<Node> nodes = new ArrayList<>();
    for (NodeDescriptor child : nodeDescriptors) {
      nodes.add(child.getNode());
    }

    if (!nodes.isEmpty()) {
      fireEvent(new StoreAddEvent(index, nodes));
    }
  }

  public void clear() {
    idToNodeMap.clear();
    roots.clear();

    fireEvent(new StoreClearEvent());
  }

  /**
   * Gets all visible items in the tree
   *
   * @return
   */
  public List<Node> getAll() {
    List<NodeDescriptor> allChildren = new LinkedList<>(roots.getChildren());
    for (int i = 0; i < allChildren.size(); i++) {
      allChildren.addAll(allChildren.get(i).getChildren());
    }
    return unwrap(allChildren);
  }

  /**
   * Recursively builds a list of all of the visible elements below the given one in the tree
   *
   * @param parent
   * @return
   */
  public List<Node> getAllChildren(Node parent) {
    List<NodeDescriptor> allChildren = new LinkedList<>(getWrapper(parent).getChildren());
    for (int i = 0; i < allChildren.size(); i++) {
      allChildren.addAll(allChildren.get(i).getChildren());
    }
    return unwrap(allChildren);
  }

  /**
   * Gets the total count of all visible items in the tree
   *
   * @return
   */
  public int getAllItemsCount() {
    List<NodeDescriptor> allChildren = new LinkedList<>(roots.getChildren());
    for (int i = 0; i < allChildren.size(); i++) {
      allChildren.addAll(allChildren.get(i).getChildren());
    }

    return allChildren.size();
  }

  /**
   * Returns the root level child.
   *
   * @param index
   * @return
   */
  public Node getChild(int index) {
    return getRootItems().get(index);
  }

  /**
   * Gets the number of visible children in the given node
   *
   * @param parent
   * @return
   */
  public int getChildCount(Node parent) {
    return getWrapper(parent).getChildren().size();
  }

  /**
   * Gets the list of visible children attached to the given element
   *
   * @param parent
   * @return
   */
  public List<Node> getChildren(Node parent) {
    return unwrap(getWrapper(parent).getChildren());
  }

  /**
   * Gets the depth of the given element in the tree, where 1 indicates that it is a root element of
   * the tree.
   *
   * @param child
   * @return
   */
  public int getDepth(Node child) {
    int depth = 0;
    while (child != null) {
      depth++;
      child = getParent(child);
    }
    return depth;
  }

  /**
   * Returns the fist child of the parent.
   *
   * @param parent
   * @return
   */
  public Node getFirstChild(Node parent) {
    NodeDescriptor nodeDescriptor = parent == null ? roots : getWrapper(parent);
    if (nodeDescriptor.getChildren().size() != 0) {
      return nodeDescriptor.getChildren().get(0).getNode();
    }
    return null;
  }

  /**
   * Returns the last child of the parent.
   *
   * @param parent
   * @return
   */
  public Node getLastChild(Node parent) {
    NodeDescriptor nodeDescriptor = parent == null ? roots : getWrapper(parent);
    List<NodeDescriptor> children = nodeDescriptor.getChildren();
    if (children.size() != 0) {
      return children.get(children.size() - 1).getNode();
    }
    return null;
  }

  /**
   * Returns the next sibling of the model.
   *
   * @param item
   * @return
   */
  public Node getNextSibling(Node item) {
    Node parent = getParent(item);
    List<Node> children = parent == null ? getRootItems() : getChildren(parent);
    int index = children.indexOf(item);
    if (children.size() > (index + 1)) {
      return children.get(index + 1);
    }
    return null;
  }

  /**
   * Returns the item's previous sibling.
   *
   * @param item
   * @return
   */
  public Node getPreviousSibling(Node item) {
    Node parent = getParent(item);
    List<Node> children = parent == null ? getRootItems() : getChildren(parent);
    int index = children.indexOf(item);
    if (index > 0) {
      return children.get(index - 1);
    }
    return null;
  }

  /**
   * Gets the number of items at the root of the tree, that is, the number of visible items that
   * have been added without specifying a parent.
   *
   * @return
   */
  public int getRootCount() {
    return roots.getChildren().size();
  }

  /**
   * Gets the visible items at the root of the tree.
   *
   * @return
   */
  public List<Node> getRootItems() {
    return unwrap(roots.getChildren());
  }

  /**
   * Returns true if the given node has visible children.
   *
   * @param item
   * @return
   */
  public boolean hasChildren(Node item) {
    return getChildCount(item) != 0;
  }

  /**
   * Returns the item's index in it's parent including root level items.
   *
   * @param item
   * @return
   */
  public int indexOf(Node item) {
    Node parent = getParent(item);
    if (parent == null) {
      return getRootItems().indexOf(item);
    } else {
      return getChildren(parent).indexOf(item);
    }
  }

  /**
   * Inserts the given models at the given index in the list of root nodes
   *
   * @param index
   * @param rootNodes
   */
  public void insert(int index, List<Node> rootNodes) {
    insert(roots, index, rootNodes);
  }

  /**
   * Inserts the given model at the given index in the list of root nodes
   *
   * @param index
   * @param root
   */
  public void insert(int index, Node root) {
    insert(roots, index, root);
  }

  public void insert(Node node, int index, List<Node> children) {
    insert(getWrapper(node), index, children);
  }

  public void insert(Node parent, int index, Node child) {
    insert(getWrapper(parent), index, child);
  }

  /**
   * Inserts the child models at the given position in the parent's list of visible children.
   *
   * @param parent
   * @param index
   * @param children
   */
  public void insert(NodeDescriptor parent, int index, List<Node> children) {
    int initialCount = parent.getChildren().size();
    parent.addChildren(index, wrap(children));

    if (initialCount != parent.getChildren().size()) {
      List<Node> addedChildren = new ArrayList<>();
      List<NodeDescriptor> currentChildren = parent.getChildren();
      if (isSorted()) {
        int currentChildrenSize = currentChildren.size();
        for (int i = 0; i < currentChildrenSize; i++) {
          int childrenSize = children.size();
          for (int j = 0; j < childrenSize; j++) {
            Node currentData = currentChildren.get(i).getNode();
            Node child = children.get(j);
            if (child == currentData) {
              addedChildren.add(child);
              break;
            }
          }
        }
      } else {
        for (NodeDescriptor currentChild : currentChildren) {
          if (children.contains(currentChild.getNode())) {
            addedChildren.add(currentChild.getNode());
          }
        }
      }
      if (addedChildren.size() != 0) {
        fireEvent(new StoreAddEvent(index, addedChildren));
      }
    }
  }

  /**
   * Inserts the child model at the given position in the parent's list of visible children
   *
   * @param parent
   * @param index
   * @param child
   */
  public void insert(NodeDescriptor parent, int index, Node child) {
    int initialCount = parent.getChildren().size();
    parent.addChild(index, wrap(child));

    if (parent.getChildren().size() != initialCount) {
      int addedIndex = -1;
      if (isSorted()) {
        List<NodeDescriptor> childrenModels = parent.getChildren();
        for (int i = 0; i < childrenModels.size(); i++) {
          if (childrenModels.get(i).getNode().equals(child)) {
            addedIndex = i;
            break;
          }
        }
      } else {
        addedIndex = index;
      }
      // if the change actually occurred, fire an event
      fireEvent(new StoreAddEvent(addedIndex, child));
    }
  }

  public boolean remove(Node node) {
    NodeDescriptor nodeDescriptor = idToNodeMap.get(getKeyProvider().getKey(node));
    if (nodeDescriptor != null) {
      Node parent = getParent(node);
      List<Node> children = getAllChildren(node);
      int visibleIndex = nodeDescriptor.getParent().getChildren().indexOf(nodeDescriptor);
      nodeDescriptor.getParent().remove(nodeDescriptor);
      if (visibleIndex != -1) {
        fireEvent(new StoreRemoveEvent(visibleIndex, node, parent, children));
      } else {
        List<NodeDescriptor> descriptors = new LinkedList<>();
        descriptors.add(nodeDescriptor);
        for (int i = 0; i < descriptors.size(); i++) {
          nodeDescriptor = descriptors.get(i);
          descriptors.addAll(nodeDescriptor.getChildren());

          idToNodeMap.remove(getKeyProvider().getKey(nodeDescriptor.getNode()));
        }
      }
      return true;
    }
    return false;
  }

  public void removeChildren(Node parent) {
    removeChildren(getWrapper(parent));
  }

  private void removeChildren(NodeDescriptor parent) {
    if (parent.getChildren().size() != 0) {
      List<NodeDescriptor> models = new LinkedList<>();
      models.addAll(parent.getChildren());
      parent.clear();
      for (int i = 0; i < models.size(); i++) {
        NodeDescriptor wrapper = models.get(i);
        models.addAll(wrapper.getChildren());

        List<Node> children = getAllChildren(wrapper.getNode());

        idToNodeMap.remove(getKeyProvider().getKey(wrapper.getNode()));
        if (wrapper.getParent() == parent) {
          fireEvent(new StoreRemoveEvent(0, wrapper.getNode(), parent.getNode(), children));
        }
      }
    }
  }

  public void replaceChildren(Node parent, List<Node> children) {
    if (parent == null) {
      roots.clear();
      idToNodeMap.clear();

      roots.addChildren(0, wrap(children));
    } else {
      NodeDescriptor parentNodeDescriptor = getWrapper(parent);
      List<NodeDescriptor> models = new LinkedList<>();
      models.addAll(parentNodeDescriptor.getChildren());
      for (int i = 0; i < models.size(); i++) {
        NodeDescriptor wrapper = models.get(i);
        models.addAll(wrapper.getChildren());
        idToNodeMap.remove(getKeyProvider().getKey(wrapper.getNode()));
        remove(wrapper.getNode());
      }
      parentNodeDescriptor.clear();

      parentNodeDescriptor.addChildren(0, wrap(children));
    }

    fireEvent(new StoreDataChangeEvent(parent));
  }

  private List<NodeDescriptor> findRemovedNodes(
      NodeDescriptor parent, final List<Node> loadedChildren) {
    List<NodeDescriptor> existed = parent.getChildren();

    if (existed == null || existed.isEmpty()) {
      return Collections.emptyList();
    }

    Iterable<NodeDescriptor> removedItems =
        Iterables.filter(
            existed,
            new Predicate<NodeDescriptor>() {
              @Override
              public boolean apply(NodeDescriptor existedChild) {
                return !loadedChildren.contains(existedChild.getNode());
              }
            });

    return Lists.newArrayList(removedItems);
  }

  private List<Node> findNewNodes(NodeDescriptor parent, final List<Node> loadedChildren) {
    final List<NodeDescriptor> existed = parent.getChildren();

    if (existed == null || existed.isEmpty()) {
      return loadedChildren;
    }

    Iterable<Node> newItems =
        Iterables.filter(
            loadedChildren,
            new Predicate<Node>() {
              @Override
              public boolean apply(Node loadedChild) {
                for (NodeDescriptor nodeDescriptor : existed) {
                  if (nodeDescriptor.getNode().equals(loadedChild)) {
                    return false;
                  }
                }
                return true;
              }
            });

    return Lists.newArrayList(newItems);
  }

  private List<NodeDescriptor> convertTreeNodesHelper(List<Node> children) {
    List<NodeDescriptor> nodeDescriptors = new ArrayList<>();
    if (children != null) {
      for (Node child : children) {
        NodeDescriptor nodeDescriptor = new NodeDescriptor(this, child);
        idToNodeMap.put(keyProvider.getKey(child), nodeDescriptor);
        nodeDescriptors.add(nodeDescriptor);
      }
    }
    return nodeDescriptors;
  }

  protected Map<String, NodeDescriptor> getNodeMap() {
    return idToNodeMap;
  }

  public Collection<NodeDescriptor> getStoredNodes() {
    return idToNodeMap.values();
  }

  public Node getParent(Node child) {
    final NodeDescriptor wrapper = getWrapper(child);

    if (wrapper == null) {
      return null;
    }

    NodeDescriptor nodeDescriptor = wrapper.getParent();
    return (nodeDescriptor != null && !nodeDescriptor.isRoot()) ? nodeDescriptor.getNode() : null;
  }

  public void fireEvent(GwtEvent<?> event) {
    if (handlerManager != null) {
      handlerManager.fireEvent(event);
    }
  }

  protected HandlerManager ensureHandlers() {
    if (handlerManager == null) {
      handlerManager = new HandlerManager(this);
    }
    return handlerManager;
  }

  public UniqueKeyProvider<Node> getKeyProvider() {
    return keyProvider;
  }

  public boolean hasMatchingKey(Node model1, Node model2) {
    return keyProvider.getKey(model1).equals(keyProvider.getKey(model2));
  }

  public Node findNode(Node node) {
    return findNodeWithKey(getKeyProvider().getKey(node));
  }

  public Node findNodeWithKey(String key) {
    NodeDescriptor nodeDescriptor = idToNodeMap.get(key);
    if (nodeDescriptor == null) {
      return null;
    }

    return nodeDescriptor.getNode();
  }

  @Override
  public HandlerRegistration addStoreHandlers(StoreHandlers handlers) {
    GroupingHandlerRegistration reg = new GroupingHandlerRegistration();
    reg.add(addStoreAddHandler(handlers));
    reg.add(addStoreRemoveHandler(handlers));
    reg.add(addStoreClearHandler(handlers));
    reg.add(addStoreDataChangeHandler(handlers));
    reg.add(addStoreUpdateHandler(handlers));
    reg.add(addStoreRecordChangeHandler(handlers));
    reg.add(addStoreSortHandler(handlers));
    return reg;
  }

  @Override
  public HandlerRegistration addStoreAddHandler(StoreAddEvent.StoreAddHandler handler) {
    return ensureHandlers().addHandler(StoreAddEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreClearHandler(StoreClearEvent.StoreClearHandler handler) {
    return ensureHandlers().addHandler(StoreClearEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreDataChangeHandler(
      StoreDataChangeEvent.StoreDataChangeHandler handler) {
    return ensureHandlers().addHandler(StoreDataChangeEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreRecordChangeHandler(
      StoreRecordChangeEvent.StoreRecordChangeHandler handler) {
    return ensureHandlers().addHandler(StoreRecordChangeEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreRemoveHandler(StoreRemoveEvent.StoreRemoveHandler handler) {
    return ensureHandlers().addHandler(StoreRemoveEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreSortHandler(StoreSortEvent.StoreSortHandler handler) {
    return ensureHandlers().addHandler(StoreSortEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addStoreUpdateHandler(StoreUpdateEvent.StoreUpdateHandler handler) {
    return ensureHandlers().addHandler(StoreUpdateEvent.getType(), handler);
  }

  public NodeDescriptor getWrapper(Node node) {
    return idToNodeMap.get(getKeyProvider().getKey(node));
  }

  public NodeDescriptor wrap(Node node) {
    NodeDescriptor nodeDescriptor = new NodeDescriptor(this, node);
    idToNodeMap.put(getKeyProvider().getKey(node), nodeDescriptor);
    return nodeDescriptor;
  }

  protected List<NodeDescriptor> wrap(List<Node> nodes) {
    List<NodeDescriptor> nodeDescriptors = new ArrayList<>();
    for (Node node : nodes) {
      nodeDescriptors.add(wrap(node));
    }
    return nodeDescriptors;
  }

  protected List<Node> unwrap(List<NodeDescriptor> nodeDescriptors) {
    List<Node> nodes = new ArrayList<>();
    for (NodeDescriptor nodeDescriptor : nodeDescriptors) {
      nodes.add(nodeDescriptor.getNode());
    }
    return Collections.unmodifiableList(nodes);
  }

  public void update(Node node) {
    fireEvent(new StoreUpdateEvent(Collections.singletonList(node)));
  }

  public List<StoreSortInfo> getSortInfo() {
    return comparators;
  }

  public void clearSortInfo() {
    comparators.clear();
  }

  public void addSortInfo(int index, StoreSortInfo info) {
    comparators.add(index, info);
    applySort(false);
  }

  public void addSortInfo(StoreSortInfo info) {
    comparators.add(info);
    applySort(false);
  }

  protected Comparator<NodeDescriptor> buildFullComparator() {
    return new Comparator<NodeDescriptor>() {
      public int compare(NodeDescriptor o1, NodeDescriptor o2) {
        for (StoreSortInfo comparator : comparators) {
          int val = comparator.compare(o1.getNode(), o2.getNode());
          if (val != 0) {
            return val;
          }
        }
        return 0;
      }
    };
  }

  public void applySort(boolean suppressEvent) {
    Comparator<NodeDescriptor> comparator = buildFullComparator();
    Collections.sort(roots.getChildren(), comparator);

    for (NodeDescriptor descriptor : idToNodeMap.values()) {
      Collections.sort(descriptor.getChildren(), comparator);
    }

    if (!suppressEvent) {
      fireEvent(new StoreSortEvent());
    }
  }

  public static class StoreSortInfo implements Comparator<Node> {
    private SortDir direction;
    private final Comparator<Node> comparator;

    /**
     * Creates a sort info object based on the given comparator to act on the item itself. Complex
     * comparators can easily be built in this way, instead of adding multiple StoreSortInfo
     * objects, or using one of the other constructors.
     *
     * @param itemComparator the comparator to use to sort the items
     * @param direction the sort direction
     */
    public StoreSortInfo(Comparator<Node> itemComparator, SortDir direction) {
      this.comparator = itemComparator;
      this.direction = direction;
    }

    public Comparator<Node> getComparator() {
      return comparator;
    }

    @Override
    public int compare(Node o1, Node o2) {
      int val = comparator.compare(o1, o2);
      return direction == SortDir.ASC ? val : -val;
    }

    public SortDir getDirection() {
      return direction;
    }

    public void setDirection(SortDir direction) {
      this.direction = direction;
    }
  }

  public boolean reIndexNode(String oldId, Node node) {
    if (!idToNodeMap.containsKey(oldId)) {
      return false;
    }

    NodeDescriptor descriptor = idToNodeMap.remove(oldId);
    idToNodeMap.put(getKeyProvider().getKey(node), descriptor);

    return true;
  }
}

/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang;

import static com.google.common.collect.Maps.newLinkedHashMapWithExpectedSize;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is an implementation of a stable topological sort on a directed graph.
 *
 * <p>The sorting does not pose any requirements on the types being sorted. Instead, the
 * implementation merely requires a function to provide it with a set of predecessors of a certain
 * "node". The implementation of the function is completely in the hands of the caller.
 *
 * <p>Additionally, a function to extract an "ID" from a node is required. The reasoning behind this
 * is that it usually is easier to work with identifiers when establishing the predecessors than
 * with the full node instances. That said, nothing prevents the caller from using the actual node
 * instance as its ID if the caller so wishes. The consequence of this is that, as a side-effect of
 * the sorting, the duplicates, as determined by the equality of {@code ID} instances, are removed
 * from the resulting sorted list.
 *
 * @param <N> the type of nodes
 * @param <ID> the type of a node ID
 */
public final class TopologicalSort<N, ID> {

  private final Function<N, ID> identityExtractor;
  private final Function<N, Set<ID>> directPredecessorsExtractor;

  /**
   * @param identityExtractor a function to extract some kind of value uniquely identifying a node
   *     amongst the others.
   * @param directPredecessorsExtractor a function returning a list of ids of direct predecessors of
   *     a node
   */
  public TopologicalSort(
      Function<N, ID> identityExtractor, Function<N, Set<ID>> directPredecessorsExtractor) {
    this.identityExtractor = identityExtractor;
    this.directPredecessorsExtractor = directPredecessorsExtractor;
  }

  /**
   * Given the function for determining the predecessors of the nodes, return the list of the nodes
   * in topological order. I.e. all predecessors will be placed sooner in the list than their
   * successors. Note that the input collection is assumed to contain no duplicate entries as
   * determined by the equality of the {@code ID} type. If such duplicates are present in the input
   * collection, the output list will only contain the first instance of the duplicates from the
   * input collection.
   *
   * <p>The implemented sort algorithm is stable. If there is no relationship between 2 nodes, they
   * retain the relative position to each other as they had in the provided collection (e.g. if "a"
   * preceded "b" in the original collection and there is no relationship between them (as
   * determined by the predecessor function), the "a" will still precede "b" in the resulting list.
   * Other nodes may be inserted in between them though in the result).
   *
   * <p>The cycles in the graph determined by the predecessor function are ignored and nodes in the
   * cycle are placed into the output list in the source order.
   *
   * @param nodes the collection of nodes
   * @return the list of nodes sorted in topological order
   */
  public List<N> sort(Collection<N> nodes) {
    // the linked hashmap is important to retain the original order of elements unless required
    // by the dependencies between nodes
    LinkedHashMap<ID, NodeInfo<ID, N>> nodeInfos = newLinkedHashMapWithExpectedSize(nodes.size());
    List<NodeInfo<ID, N>> results = new ArrayList<>(nodes.size());

    int pos = 0;
    boolean needsSorting = false;
    for (N node : nodes) {
      ID nodeID = identityExtractor.apply(node);
      // we need the set to be modifiable, so let's make our own
      Set<ID> preds = new HashSet<>(directPredecessorsExtractor.apply(node));
      needsSorting = needsSorting || !preds.isEmpty();

      NodeInfo<ID, N> nodeInfo = nodeInfos.computeIfAbsent(nodeID, __ -> new NodeInfo<>());
      nodeInfo.id = nodeID;
      nodeInfo.predecessors = preds;
      nodeInfo.sourcePosition = pos++;
      nodeInfo.node = node;

      for (ID pred : preds) {
        // note that this means that we're inserting the nodeinfos into the map in an incorrect
        // order and will have to sort them in the source order before we do the actual topo sort.
        // We take that cost because we gamble on there being no dependencies in the nodes as a
        // common case.
        NodeInfo<ID, N> predNode = nodeInfos.computeIfAbsent(pred, __ -> new NodeInfo<>());
        if (predNode.successors == null) {
          predNode.successors = new HashSet<>();
        }
        predNode.successors.add(nodeID);
      }
    }

    if (needsSorting) {
      // because of the predecessors, we have put the nodeinfos in the map in an incorrect order.
      // we need to correct that before we try to sort...
      TreeSet<NodeInfo<ID, N>> tmp = new TreeSet<>(Comparator.comparingInt(a -> a.sourcePosition));
      tmp.addAll(nodeInfos.values());
      nodeInfos.clear();
      tmp.forEach(ni -> nodeInfos.put(ni.id, ni));

      // now we're ready to produce the results
      sort(nodeInfos, results);
    } else {
      // we don't need to sort, but we need to keep the expected behavior of removing the duplicates
      results = new ArrayList<>(nodeInfos.values());
    }

    return results.stream().map(ni -> ni.node).collect(Collectors.toList());
  }

  private void sort(LinkedHashMap<ID, NodeInfo<ID, N>> nodes, List<NodeInfo<ID, N>> results) {

    while (!nodes.isEmpty()) {
      NodeInfo<ID, N> curr = removeFirstIndependent(nodes);
      if (curr != null) {
        // yay, simple. Just add the found independent node to the results.
        results.add(curr);
      } else {
        // ok, there is a cycle in the graph. Let's remove all the nodes in the first cycle we find
        // from our predecessors map, add them to the result in their original order and try to
        // continue normally

        // find the first cycle in the predecessors (in the original list order)
        Iterator<NodeInfo<ID, N>> nexts = nodes.values().iterator();
        List<NodeInfo<ID, N>> cycle;
        do {
          curr = nexts.next();
          cycle = findCycle(curr, nodes);
        } while (cycle.isEmpty() && nexts.hasNext());

        // If we ever find a graph that doesn't have any independent node, yet we fail to find a
        // cycle in it, the universe must be broken.
        if (cycle.isEmpty()) {
          throw new IllegalStateException(
              String.format(
                  "Failed to find a cycle in a graph that doesn't seem to  have any independent"
                      + " node. This should never happen. Please file a bug. Current state of the"
                      + " sorting is: nodes=%s, results=%s",
                  nodes.toString(), results.toString()));
        }

        cycle.sort(Comparator.comparingInt(a -> a.sourcePosition));

        for (NodeInfo<ID, N> n : cycle) {
          removePredecessorMapping(nodes, n);
          results.add(n);
        }
      }
    }
  }

  private void removePredecessorMapping(Map<ID, NodeInfo<ID, N>> nodes, NodeInfo<ID, N> node) {
    forgetNodeInSuccessors(node, nodes);
    nodes.remove(node.id);
  }

  private void forgetNodeInSuccessors(NodeInfo<ID, N> node, Map<ID, NodeInfo<ID, N>> nodes) {
    if (node.successors != null) {
      for (ID succ : node.successors) {
        NodeInfo<ID, N> succNode = nodes.get(succ);
        if (succNode != null) {
          succNode.predecessors.remove(node.id);
        }
      }
    }
  }

  private List<NodeInfo<ID, N>> findCycle(NodeInfo<ID, N> node, Map<ID, NodeInfo<ID, N>> nodes) {
    // bail out quickly if there are no preds - should be fairly common occurrence hopefully
    Set<ID> preds = node.predecessors;
    if (preds == null || preds.isEmpty()) {
      return emptyList();
    }

    List<NodeInfo<ID, N>> ret = new ArrayList<>();
    List<ID> todo = new ArrayList<>(preds);

    Set<ID> visited = new HashSet<>();

    while (!todo.isEmpty()) {
      ID n = todo.remove(0);

      if (visited.contains(n)) {
        continue;
      }

      visited.add(n);

      NodeInfo<ID, N> predNode = nodes.get(n);
      if (predNode != null) {
        todo.addAll(predNode.predecessors);
        ret.add(predNode);

        if (predNode.equals(node)) {
          // we found the cycle to our original node
          return ret;
        }
      }
    }

    return emptyList();
  }

  private NodeInfo<ID, N> removeFirstIndependent(Map<ID, NodeInfo<ID, N>> nodes) {
    Iterator<Entry<ID, NodeInfo<ID, N>>> it = nodes.entrySet().iterator();

    while (it.hasNext()) {
      Entry<ID, NodeInfo<ID, N>> e = it.next();
      if (e.getValue().predecessors.isEmpty()) {
        it.remove();
        NodeInfo<ID, N> ret = e.getValue();
        forgetNodeInSuccessors(ret, nodes);
        return ret;
      }
    }

    return null;
  }

  private static final class NodeInfo<ID, N> {
    private ID id;
    private int sourcePosition;
    private Set<ID> predecessors;
    private Set<ID> successors;
    private N node;
  }
}

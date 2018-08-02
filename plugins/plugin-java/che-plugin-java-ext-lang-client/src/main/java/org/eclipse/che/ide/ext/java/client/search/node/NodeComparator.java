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
package org.eclipse.che.ide.ext.java.client.search.node;

import java.util.Comparator;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Comparator for nodes that has MatchNode as child. Compares nodes according to match line number.
 *
 * @author Evgen Vidolob
 */
public class NodeComparator implements Comparator<Node> {

  @Override
  public int compare(Node o1, Node o2) {
    if (o1 instanceof MatchNode && o2 instanceof MatchNode) {
      return Integer.compare(
          ((MatchNode) o1).getMatch().getMatchLineNumber(),
          ((MatchNode) o2).getMatch().getMatchLineNumber());
    }

    if (o1 instanceof MethodNode && o2 instanceof MatchNode) {
      List<Match> matches = ((MethodNode) o1).getMatches();
      if (matches != null) {
        if (isHasGreaterLine(((MatchNode) o2).getMatch().getMatchLineNumber(), matches)) {
          return -1;
        } else {
          return 1;
        }
      }
    }

    if (o2 instanceof MethodNode && o1 instanceof MatchNode) {
      List<Match> matches = ((MethodNode) o2).getMatches();
      if (matches != null) {
        if (isHasGreaterLine(((MatchNode) o1).getMatch().getMatchLineNumber(), matches)) {
          return 1;
        } else {
          return -1;
        }
      }
    }

    if (o1 instanceof TypeNode && o2 instanceof MatchNode) {
      List<Match> matches = ((TypeNode) o1).getMatches();
      if (matches != null) {
        if (isHasGreaterLine(((MatchNode) o2).getMatch().getMatchLineNumber(), matches)) {
          return -1;
        } else {
          return 1;
        }
      }
    }
    if (o2 instanceof TypeNode && o1 instanceof MatchNode) {
      List<Match> matches = ((TypeNode) o2).getMatches();
      if (matches != null) {
        if (isHasGreaterLine(((MatchNode) o1).getMatch().getMatchLineNumber(), matches)) {
          return 1;
        } else {
          return -1;
        }
      }
    }

    if (o1 instanceof TypeNode && o2 instanceof MethodNode) {
      List<Match> typeMatches = ((TypeNode) o1).getMatches();
      List<Match> methodMatches = ((MethodNode) o2).getMatches();
      if (typeMatches == null) {
        return -1;
      }
      if (methodMatches == null) {
        return 1;
      }
      return compare(typeMatches, methodMatches);
    }

    if (o1 instanceof MethodNode && o2 instanceof TypeNode) {
      List<Match> methodMatches = ((TypeNode) o2).getMatches();
      List<Match> typeMatches = ((MethodNode) o1).getMatches();
      if (typeMatches == null) {
        return 1;
      }
      if (methodMatches == null) {
        return -1;
      }
      return compare(methodMatches, typeMatches);
    }

    if (o1 instanceof MethodNode && o2 instanceof MethodNode) {
      List<Match> methodMatches1 = ((MethodNode) o1).getMatches();
      List<Match> methodMatches2 = ((MethodNode) o2).getMatches();
      if (methodMatches1 == null) {
        return -1;
      }
      if (methodMatches2 == null) {
        return 1;
      }
      return compare(methodMatches1, methodMatches2);
    }

    if (o1 instanceof TypeNode && o2 instanceof TypeNode) {
      List<Match> first = ((TypeNode) o1).getMatches();
      List<Match> second = ((TypeNode) o2).getMatches();
      if (first == null) {
        return -1;
      }
      if (second == null) {
        return 1;
      }
      return compare(first, second);
    }

    return 0;
  }

  private boolean isHasGreaterLine(int matchLineNumber, List<Match> matches) {
    boolean hasGreaterLine = false;
    for (Match match : matches) {
      if (match.getMatchLineNumber() < matchLineNumber) {
        hasGreaterLine = true;
        break;
      }
    }
    return hasGreaterLine;
  }

  private int compare(List<Match> list1, List<Match> list2) {
    return Integer.compare(getMinLine(list1), getMinLine(list2));
  }

  private int getMinLine(List<Match> list) {
    int i = Integer.MAX_VALUE;
    for (Match match : list) {
      i = Math.min(i, match.getMatchLineNumber());
    }
    return i;
  }
}

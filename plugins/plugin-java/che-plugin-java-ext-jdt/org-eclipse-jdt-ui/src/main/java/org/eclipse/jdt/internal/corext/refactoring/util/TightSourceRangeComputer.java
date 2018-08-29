/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.TargetSourceRangeComputer;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;

/**
 * A source range computer that uses the shortest possible source range for a given set of nodes.
 *
 * <p>For other nodes, the default extended source range from {@link
 * TargetSourceRangeComputer#computeSourceRange(ASTNode)} is used.
 *
 * <p>For nodes inside "tight" nodes, the source range is the extended source range, unless this
 * would violate the no-overlapping condition from the superclass.
 *
 * @since 3.2
 */
public class TightSourceRangeComputer extends TargetSourceRangeComputer {
  private HashSet<ASTNode> fTightSourceRangeNodes = new HashSet<ASTNode>();

  /**
   * Add the given node to the set of "tight" nodes.
   *
   * @param reference a node
   * @since 3.2
   */
  public void addTightSourceNode(ASTNode reference) {
    fTightSourceRangeNodes.add(reference);

    List<StructuralPropertyDescriptor> properties = reference.structuralPropertiesForType();
    for (Iterator<StructuralPropertyDescriptor> iterator = properties.iterator();
        iterator.hasNext(); ) {
      StructuralPropertyDescriptor descriptor = iterator.next();
      if (descriptor.isChildProperty()) {
        ASTNode child = (ASTNode) reference.getStructuralProperty(descriptor);
        if (child != null && isExtending(child, reference)) {
          addTightSourceNode(child);
        }
      } else if (descriptor.isChildListProperty()) {
        List<? extends ASTNode> children =
            ASTNodes.getChildListProperty(reference, (ChildListPropertyDescriptor) descriptor);
        for (Iterator<? extends ASTNode> iterator2 = children.iterator(); iterator2.hasNext(); ) {
          ASTNode child = iterator2.next();
          if (isExtending(child, reference)) {
            addTightSourceNode(child);
          }
        }
      }
    }
  }

  @Override
  public SourceRange computeSourceRange(ASTNode node) {
    if (fTightSourceRangeNodes.contains(node)) {
      return new TargetSourceRangeComputer.SourceRange(node.getStartPosition(), node.getLength());
    } else {
      return super.computeSourceRange(node); // see bug 85850
    }
  }

  private boolean isExtending(ASTNode child, ASTNode parent) {
    SourceRange extendedRange = super.computeSourceRange(child);

    int parentStart = parent.getStartPosition();
    int extendedStart = extendedRange.getStartPosition();
    if (parentStart > extendedStart) return true;

    int parentEnd = parentStart + parent.getLength();
    int extendedEnd = extendedStart + extendedRange.getLength();
    if (parentEnd < extendedEnd) return true;

    return false;
  }
}

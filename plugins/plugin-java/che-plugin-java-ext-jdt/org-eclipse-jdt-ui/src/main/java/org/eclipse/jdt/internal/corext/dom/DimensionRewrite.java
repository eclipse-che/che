/**
 * ***************************************************************************** Copyright (c) 2013,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Rewrite helper for {@link Dimension} node lists and {@link ArrayType}s.
 *
 * @since 3.10
 */
public class DimensionRewrite {

  /**
   * Creates a {@link ASTRewrite#createCopyTarget(ASTNode) copy} of <code>type</code> and adds
   * <code>extraDimensions</code> to it.
   *
   * @param type the type to copy
   * @param extraDimensions the dimensions to add
   * @param rewrite the ASTRewrite with which to create new nodes
   * @return the copy target with added dimensions
   */
  public static Type copyTypeAndAddDimensions(
      Type type, List<Dimension> extraDimensions, ASTRewrite rewrite) {
    AST ast = rewrite.getAST();
    if (extraDimensions.isEmpty()) {
      return (Type) rewrite.createCopyTarget(type);
    }

    ArrayType result;
    if (type instanceof ArrayType) {
      ArrayType arrayType = (ArrayType) type;
      Type varElementType = (Type) rewrite.createCopyTarget(arrayType.getElementType());
      result = ast.newArrayType(varElementType, 0);
      result.dimensions().addAll(copyDimensions(extraDimensions, rewrite));
      result.dimensions().addAll(copyDimensions(arrayType.dimensions(), rewrite));
    } else {
      Type elementType = (Type) rewrite.createCopyTarget(type);
      result = ast.newArrayType(elementType, 0);
      result.dimensions().addAll(copyDimensions(extraDimensions, rewrite));
    }
    return result;
  }

  /**
   * Returns {@link ASTRewrite#createCopyTarget(ASTNode) copies} of the given <code>dimensions
   * </code>.
   *
   * @param dimensions the dimensions to copy
   * @param rewrite the ASTRewrite with which to create new nodes
   * @return list of copy targets
   */
  public static List<Dimension> copyDimensions(List<Dimension> dimensions, ASTRewrite rewrite) {
    ArrayList<Dimension> result = new ArrayList<Dimension>();
    for (int i = 0; i < dimensions.size(); i++) {
      result.add((Dimension) rewrite.createCopyTarget(dimensions.get(i)));
    }
    return result;
  }

  /**
   * Removes all children in <code>node</code>'s <code>childListProperty</code>.
   *
   * @param node ASTNode
   * @param childListProperty child list property
   * @param rewrite rewrite that removes the nodes
   * @param editGroup the edit group in which to collect the corresponding text edits, or null if
   *     ungrouped
   */
  public static void removeAllChildren(
      ASTNode node,
      ChildListPropertyDescriptor childListProperty,
      ASTRewrite rewrite,
      TextEditGroup editGroup) {
    ListRewrite listRewrite = rewrite.getListRewrite(node, childListProperty);
    @SuppressWarnings("unchecked")
    List<? extends ASTNode> children =
        (List<? extends ASTNode>) node.getStructuralProperty(childListProperty);
    for (ASTNode child : children) {
      listRewrite.remove(child, editGroup);
    }
  }
}

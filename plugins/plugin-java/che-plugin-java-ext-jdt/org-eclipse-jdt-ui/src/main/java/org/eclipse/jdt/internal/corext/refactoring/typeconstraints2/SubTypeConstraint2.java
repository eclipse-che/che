/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import org.eclipse.core.runtime.Assert;

public final class SubTypeConstraint2 implements ITypeConstraint2 {

  private final ConstraintVariable2 fAncestor;

  private final ConstraintVariable2 fDescendant;

  public SubTypeConstraint2(
      final ConstraintVariable2 descendant, final ConstraintVariable2 ancestor) {
    Assert.isNotNull(descendant);
    Assert.isNotNull(ancestor);
    fDescendant = descendant;
    fAncestor = ancestor;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(Object other) {
    // can use object identity on ConstraintVariables, since we have the stored (or to be stored)
    // objects
    if (other.getClass() != SubTypeConstraint2.class) return false;

    ITypeConstraint2 otherTC = (ITypeConstraint2) other;
    return fDescendant == otherTC.getLeft() && fAncestor == otherTC.getRight();
  }

  public final ConstraintVariable2 getLeft() {
    return fDescendant;
  }

  public final ConstraintVariable2 getRight() {
    return fAncestor;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public final int hashCode() {
    return fDescendant.hashCode() ^ 37 * fAncestor.hashCode();
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return fDescendant.toString() + " <= " + fAncestor.toString(); // $NON-NLS-1$
  }
}

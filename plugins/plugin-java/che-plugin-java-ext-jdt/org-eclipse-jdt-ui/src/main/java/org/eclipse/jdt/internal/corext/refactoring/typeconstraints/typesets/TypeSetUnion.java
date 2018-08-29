/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Robert M. Fuhrer (rfuhrer@watson.ibm.com), IBM Corporation - initial API and
 * implementation *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets;

import java.util.Iterator;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

public class TypeSetUnion extends TypeSet {
  private TypeSet fLHS;
  private TypeSet fRHS;

  public TypeSetUnion(TypeSet lhs, TypeSet rhs) {
    super(lhs.getTypeSetEnvironment());
    fLHS = lhs;
    fRHS = rhs;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isUniverse()
   */
  @Override
  public boolean isUniverse() {
    if (fLHS.isUniverse() || fRHS.isUniverse()) return true;
    if (fLHS.isSingleton() && fRHS.isSingleton()) return false;
    throw new IllegalStateException("unimplemented"); // $NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#makeClone()
   */
  @Override
  public TypeSet makeClone() {
    return new TypeSetUnion(fLHS.makeClone(), fRHS.makeClone());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return fLHS.isEmpty() && fRHS.isEmpty();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#contains(TType)
   */
  @Override
  public boolean contains(TType t) {
    return fLHS.contains(t) || fRHS.contains(t);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#containsAll(org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  public boolean containsAll(TypeSet s) {
    return fLHS.containsAll(s) || fRHS.containsAll(s);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#anyMember()
   */
  @Override
  public TType anyMember() {
    return fLHS.anyMember();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof TypeSetUnion) {
      TypeSetUnion other = (TypeSetUnion) o;
      return other.fLHS.equals(fLHS) && other.fRHS.equals(fRHS);
    } else return false;
  }

  @Override
  public int hashCode() {
    return fLHS.hashCode() * 37 + fRHS.hashCode();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#upperBound()
   */
  @Override
  public TypeSet upperBound() {
    throw new IllegalStateException("unimplemented"); // $NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#lowerBound()
   */
  @Override
  public TypeSet lowerBound() {
    throw new IllegalStateException("unimplemented"); // $NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#iterator()
   */
  @Override
  public Iterator<TType> iterator() {
    throw new IllegalStateException("unimplemented"); // $NON-NLS-1$
  }
  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    return fLHS.isSingleton() && fRHS.isSingleton() && fLHS.anyMember().equals(fRHS.anyMember());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueLowerBound()
   */
  @Override
  public boolean hasUniqueLowerBound() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueUpperBound()
   */
  @Override
  public boolean hasUniqueUpperBound() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueLowerBound()
   */
  @Override
  public TType uniqueLowerBound() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueUpperBound()
   */
  @Override
  public TType uniqueUpperBound() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#enumerate()
   */
  @Override
  public EnumeratedTypeSet enumerate() {
    EnumeratedTypeSet result = fLHS.enumerate();

    result.addAll(fRHS.enumerate());
    return result;
  }

  @Override
  public String toString() {
    return "<"
        + fID
        + ": union("
        + fLHS
        + ","
        + fRHS
        + ")>"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }
}

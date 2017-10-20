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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

public class SingletonTypeSet extends TypeSet {
  private final TType fType;

  // TODO: encapsulate in factory method and return the same set for known types
  public SingletonTypeSet(TType t, TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    Assert.isNotNull(t);
    fType = t;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isUniverse()
   */
  @Override
  public boolean isUniverse() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#makeClone()
   */
  @Override
  public TypeSet makeClone() {
    return this; // new SingletonTypeSet(fType, getTypeSetEnvironment());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#intersectedWith(org.eclipse.jdt.internal.corext
   * .refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  protected TypeSet specialCasesIntersectedWith(TypeSet s2) {
    if (s2.contains(fType)) return this;
    else return getTypeSetEnvironment().getEmptyTypeSet();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#upperBound()
   */
  @Override
  public TypeSet upperBound() {
    return this; // makeClone();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#lowerBound()
   */
  @Override
  public TypeSet lowerBound() {
    return this; // makeClone();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueLowerBound()
   */
  @Override
  public boolean hasUniqueLowerBound() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueUpperBound()
   */
  @Override
  public boolean hasUniqueUpperBound() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueLowerBound()
   */
  @Override
  public TType uniqueLowerBound() {
    return fType;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueUpperBound()
   */
  @Override
  public TType uniqueUpperBound() {
    return fType;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#contains(TType)
   */
  @Override
  public boolean contains(TType t) {
    return fType.equals(t);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#containsAll(org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  public boolean containsAll(TypeSet s) {
    if (s.isEmpty()) return true;
    if (s.isSingleton()) return s.anyMember().equals(fType);
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#iterator()
   */
  @Override
  public Iterator<TType> iterator() {
    return new Iterator<TType>() {
      private boolean done = false;

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public boolean hasNext() {
        return !done;
      }

      public TType next() {
        done = true;
        return fType;
      }
    };
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#anyMember()
   */
  @Override
  public TType anyMember() {
    return fType;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#enumerate()
   */
  @Override
  public EnumeratedTypeSet enumerate() {
    EnumeratedTypeSet enumeratedTypeSet = new EnumeratedTypeSet(fType, getTypeSetEnvironment());
    enumeratedTypeSet.initComplete();
    return enumeratedTypeSet;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SingletonTypeSet) {
      SingletonTypeSet other = (SingletonTypeSet) o;

      return fType.equals(other.fType);
    } else if (o instanceof TypeSet) {
      TypeSet other = (TypeSet) o;

      return other.isSingleton() && other.anyMember().equals(fType);
    } else return false;
  }

  @Override
  public int hashCode() {
    return fType.hashCode();
  }

  @Override
  public String toString() {
    return "{"
        + fID
        + ": "
        + fType.getPrettySignature()
        + "}"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}

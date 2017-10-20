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
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TTypes;

public class SubTypesOfSingleton extends TypeSet {
  /** The "base type" defining the upper bound of this set. */
  private final TType fUpperBound;

  protected SubTypesOfSingleton(TType t, TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    Assert.isNotNull(t);
    fUpperBound = t;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isUniverse()
   */
  @Override
  public boolean isUniverse() {
    return fUpperBound.equals(getJavaLangObject());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#makeClone()
   */
  @Override
  public TypeSet makeClone() {
    return this; // new SubTypesOfSingleton(fUpperBound);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#subTypes()
   */
  @Override
  public TypeSet subTypes() {
    return this; // makeClone();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#intersectedWith(org.eclipse.jdt.internal.corext
   * .refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  public TypeSet specialCasesIntersectedWith(TypeSet other) {
    if (other.isSingleton() && other.anyMember().equals(fUpperBound))
      return other; // xsect(subTypes(A),A) = A

    if (other instanceof SubTypesOfSingleton) {
      SubTypesOfSingleton otherSub = (SubTypesOfSingleton) other;

      if (TTypes.canAssignTo(otherSub.fUpperBound, fUpperBound)) return otherSub; // .makeClone();
      if (TTypes.canAssignTo(fUpperBound, otherSub.fUpperBound)) return this; // makeClone();
    } else if (other.hasUniqueLowerBound()) {
      TType otherLower = other.uniqueLowerBound();

      if (otherLower.equals(fUpperBound))
        return new SingletonTypeSet(fUpperBound, getTypeSetEnvironment());
      if (otherLower != fUpperBound && TTypes.canAssignTo(fUpperBound, otherLower)
          || !TTypes.canAssignTo(otherLower, fUpperBound))
        return getTypeSetEnvironment().getEmptyTypeSet();
    }
    //		else if (other instanceof SubTypesSet) {
    //			SubTypesSet otherSub= (SubTypesSet) other;
    //			TypeSet otherUppers= otherSub.upperBound();
    //
    //			for(Iterator iter= otherUppers.iterator(); iter.hasNext(); ) {
    //				TType t= (TType) iter.next();
    //
    //				if ()
    //			}
    //		}
    return null;
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
    return new SingletonTypeSet(fUpperBound, getTypeSetEnvironment());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#lowerBound()
   */
  @Override
  public TypeSet lowerBound() {
    EnumeratedTypeSet e = enumerate();
    return e.lowerBound();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueLowerBound()
   */
  @Override
  public boolean hasUniqueLowerBound() {
    //		TypeSet lowerBound= lowerBound();

    //		return lowerBound.isSingleton();
    return false; // fast, though perhaps inaccurate, but that's ok
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
    TypeSet lowerBound = lowerBound();

    return lowerBound.anyMember();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueUpperBound()
   */
  @Override
  public TType uniqueUpperBound() {
    return fUpperBound;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#contains(org.eclipse.jdt.core.TType)
   */
  @Override
  public boolean contains(TType t) {
    if (t.equals(fUpperBound)) return true;
    return TTypes.canAssignTo(t, fUpperBound);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#containsAll(org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  public boolean containsAll(TypeSet other) {
    if (isUniverse()) return true;

    // Optimization: if other is also a SubTypeOfSingleton, just compare bounds
    if (other instanceof SubTypesOfSingleton) {
      SubTypesOfSingleton otherSub = (SubTypesOfSingleton) other;
      return TTypes.canAssignTo(otherSub.fUpperBound, fUpperBound);
    }
    // Optimization: if other is a SubTypesSet, just compare each of its bounds to mine
    if (other instanceof SubTypesSet) {
      SubTypesSet otherSub = (SubTypesSet) other;
      TypeSet otherUpperBounds = otherSub.upperBound();

      for (Iterator<TType> iter = otherUpperBounds.iterator(); iter.hasNext(); ) {
        TType t = iter.next();
        if (!TTypes.canAssignTo(t, fUpperBound)) return false;
      }
      return true;
    }
    // For now, no more tricks up my sleeve; get an iterator
    for (Iterator<TType> iter = other.iterator(); iter.hasNext(); ) {
      TType t = iter.next();

      if (!TTypes.canAssignTo(t, fUpperBound)) return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#iterator()
   */
  @Override
  public Iterator<TType> iterator() {
    return enumerate().iterator();
    //		return new Iterator() {
    //			// First type returned is fUpperBound, then each of the subtypes, in turn
    //			//
    //			// If the upper bound is an array type, return the set of array types
    //			// { Array(subType(elementTypeOf(fUpperBound))) }
    //			private Set/*<TType>*/ subTypes=
    // sTypeHierarchy.getAllSubtypes(getElementTypeOf(fUpperBound));
    //			private Iterator/*<TType>*/ subTypeIter= subTypes.iterator();
    //			private int nDims= getDimsOf(fUpperBound);
    //			private int idx=-1;
    //			public void remove() { /*do nothing*/}
    //			public boolean hasNext() { return idx < subTypes.size(); }
    //			public Object next() {
    //				int i=idx++;
    //				if (i < 0) return fUpperBound;
    //				return makePossiblyArrayTypeFor((TType) subTypeIter.next(), nDims);
    //			}
    //		};
  }

  /**
   * Returns the element type of the given TType, if an array type, or the given TType itself,
   * otherwise.
   *
   * @param t a type
   * @return the element type
   */
  private TType getElementTypeOf(TType t) {
    if (t instanceof ArrayType) return ((ArrayType) t).getElementType();
    return t;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    return getElementTypeOf(fUpperBound).getSubTypes().length == 0;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#anyMember()
   */
  @Override
  public TType anyMember() {
    return fUpperBound;
  }

  private EnumeratedTypeSet fEnumCache = null;

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#enumerate()
   */
  @Override
  public EnumeratedTypeSet enumerate() {
    if (fEnumCache == null) {
      if (fUpperBound instanceof ArrayType) {
        ArrayType at = (ArrayType) fUpperBound;
        fEnumCache =
            EnumeratedTypeSet.makeArrayTypesForElements(
                TTypes.getAllSubTypesIterator(at.getComponentType()), getTypeSetEnvironment());
      } else
        fEnumCache =
            new EnumeratedTypeSet(
                TTypes.getAllSubTypesIterator(fUpperBound), getTypeSetEnvironment());

      fEnumCache.add(fUpperBound);
      fEnumCache.initComplete();
    }
    return fEnumCache;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SubTypesOfSingleton)) return false;
    SubTypesOfSingleton other = (SubTypesOfSingleton) o;

    return other.fUpperBound.equals(fUpperBound);
  }

  @Override
  public int hashCode() {
    return fUpperBound.hashCode();
  }

  @Override
  public String toString() {
    return "<"
        + fID
        + ": subTypes("
        + fUpperBound.getPrettySignature()
        + ")>"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}

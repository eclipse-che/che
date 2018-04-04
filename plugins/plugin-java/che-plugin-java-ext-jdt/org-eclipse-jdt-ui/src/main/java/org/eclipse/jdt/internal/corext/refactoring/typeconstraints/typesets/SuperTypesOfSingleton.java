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
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TTypes;

public class SuperTypesOfSingleton extends TypeSet {
  /** The "base type" defining the lower bound of this set. */
  private TType fLowerBound;

  SuperTypesOfSingleton(TType t, TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    fLowerBound = t;
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
    return this; // new SuperTypesOfSingleton(fLowerBound, getTypeSetEnvironment());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#intersectedWith(org.eclipse.jdt.internal.corext
   * .refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  protected TypeSet specialCasesIntersectedWith(TypeSet other) {
    if (other.isSingleton() && other.anyMember().equals(fLowerBound))
      return other; // xsect(superTypes(A),A) = A

    if (other instanceof SuperTypesOfSingleton) {
      SuperTypesOfSingleton otherSuper = (SuperTypesOfSingleton) other;

      if (TTypes.canAssignTo(otherSuper.fLowerBound, fLowerBound)) return this;
      if (TTypes.canAssignTo(fLowerBound, otherSuper.fLowerBound)) return otherSuper;
    } else if (other.hasUniqueUpperBound()) {
      TType otherUpper = other.uniqueUpperBound();

      if (otherUpper.equals(fLowerBound))
        return new SingletonTypeSet(fLowerBound, getTypeSetEnvironment());
      if ((otherUpper != fLowerBound && TTypes.canAssignTo(otherUpper, fLowerBound))
          || !TTypes.canAssignTo(fLowerBound, otherUpper))
        return getTypeSetEnvironment().getEmptyTypeSet();
    }
    //		else if (other instanceof SuperTypesSet) {
    //			SuperTypesSet otherSub= (SuperTypesSet) other;
    //			TypeSet otherLowers= otherSub.lowerBound();
    //
    //			for(Iterator iter= otherLowers.iterator(); iter.hasNext(); ) {
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
    return new SingletonTypeSet(getJavaLangObject(), getTypeSetEnvironment());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#lowerBound()
   */
  @Override
  public TypeSet lowerBound() {
    return new SingletonTypeSet(fLowerBound, getTypeSetEnvironment());
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
    return fLowerBound;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueUpperBound()
   */
  @Override
  public TType uniqueUpperBound() {
    return getJavaLangObject();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#superTypes()
   */
  @Override
  public TypeSet superTypes() {
    return this; // makeClone();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#contains(TType)
   */
  @Override
  public boolean contains(TType t) {
    if (t.equals(fLowerBound)) return true;
    if (t.equals(getJavaLangObject())) return true;
    return TTypes.canAssignTo(fLowerBound, t);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#containsAll(org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet)
   */
  @Override
  public boolean containsAll(TypeSet other) {
    // Optimization: if other is also a SubTypeOfSingleton, just compare bounds
    if (other instanceof SuperTypesOfSingleton) {
      SuperTypesOfSingleton otherSuper = (SuperTypesOfSingleton) other;
      return TTypes.canAssignTo(fLowerBound, otherSuper.fLowerBound);
    }
    // Optimization: if other is a SubTypesSet, just compare all its bounds to mine
    if (other instanceof SuperTypesSet) {
      SuperTypesSet otherSuper = (SuperTypesSet) other;
      TypeSet otherLowerBounds = otherSuper.lowerBound();

      for (Iterator<TType> iter = otherLowerBounds.iterator(); iter.hasNext(); ) {
        TType t = iter.next();
        if (!TTypes.canAssignTo(fLowerBound, t)) return false;
      }
      return true;
    }
    if (other.isUniverse()) {
      return false;
    }
    // For now, no more tricks up my sleeve; get an iterator
    for (Iterator<TType> iter = other.iterator(); iter.hasNext(); ) {
      TType t = iter.next();

      if (!TTypes.canAssignTo(fLowerBound, t)) return false;
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
    //			// First type returned is fLowerBound, then each of the supertypes, in turn
    //			//
    //			// If the lower bound is an array type, return the set of array types
    //			// { Array(superType(elementTypeOf(fUpperBound))) }
    //			boolean isArray= (fLowerBound instanceof ArrayType);
    //			private Set/*<TType>*/ superTypes=
    // sTypeHierarchy.getAllSupertypes(getElementTypeOf(fLowerBound));
    //			private Iterator/*<TType>*/ superTypeIter= superTypes.iterator();
    //			private int nDims= getDimsOf(fLowerBound);
    //			private int idx= (isArray ? -2 : -1);
    //			public void remove() { /*do nothing*/ }
    //			public boolean hasNext() { return idx < superTypes.size(); }
    //			public Object next() {
    //				int i=idx++;
    //				if (i < -1) return sJavaLangObject;
    //				if (i < 0) return fLowerBound;
    //				return makePossiblyArrayTypeFor((TType) superTypeIter.next(), nDims);
    //			}
    //		};
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    // The only thing that doesn't have at least 1 proper supertype is java.lang.Object
    return fLowerBound.equals(getJavaLangObject());
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#anyMember()
   */
  @Override
  public TType anyMember() {
    return fLowerBound;
  }

  private EnumeratedTypeSet fEnumCache = null;

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#enumerate()
   */
  @Override
  public EnumeratedTypeSet enumerate() {
    if (fEnumCache == null) {
      if (fLowerBound instanceof ArrayType) {
        ArrayType at = (ArrayType) fLowerBound;
        fEnumCache =
            EnumeratedTypeSet.makeArrayTypesForElements(
                TTypes.getAllSuperTypesIterator(at.getComponentType()), getTypeSetEnvironment());
        fEnumCache.add(getJavaLangObject());
      } else
        fEnumCache =
            new EnumeratedTypeSet(
                TTypes.getAllSuperTypesIterator(fLowerBound), getTypeSetEnvironment());

      fEnumCache.add(fLowerBound);
      fEnumCache.initComplete();
    }
    return fEnumCache;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SuperTypesOfSingleton)) return false;
    SuperTypesOfSingleton other = (SuperTypesOfSingleton) o;

    return other.fLowerBound.equals(fLowerBound);
  }

  @Override
  public int hashCode() {
    return fLowerBound.hashCode();
  }

  @Override
  public String toString() {
    return "<"
        + fID
        + ": superTypes("
        + fLowerBound.getPrettySignature()
        + ")>"; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}

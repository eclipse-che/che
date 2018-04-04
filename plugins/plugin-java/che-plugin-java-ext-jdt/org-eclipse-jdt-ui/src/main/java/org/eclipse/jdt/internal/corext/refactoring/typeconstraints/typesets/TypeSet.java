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
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ITypeSet;

public abstract class TypeSet implements ITypeSet {

  public TType chooseSingleType() {
    return null;
  }

  public ITypeSet restrictedTo(ITypeSet restrictionSet) {
    throw new UnsupportedOperationException();
  }

  protected TType getJavaLangObject() {
    return fTypeSetEnvironment.getJavaLangObject();
  }

  protected TypeSetEnvironment getTypeSetEnvironment() {
    return fTypeSetEnvironment;
  }

  private static int sID = 0;

  public static int getCount() {
    return sID;
  }

  public static void resetCount() {
    sID = 0;
  }

  /**
   * An ID unique to this EnumeratedTypeSet instance, to aid in debugging the sharing of TypeSets
   * across ConstraintVariables in a TypeEstimateEnvironment.
   */
  protected final int fID;

  private final TypeSetEnvironment fTypeSetEnvironment;

  protected TypeSet(TypeSetEnvironment typeSetEnvironment) {
    fTypeSetEnvironment = typeSetEnvironment;
    fID = sID++;
  }

  /** @return <code>true</code> iff this set represents the universe of TTypes */
  public abstract boolean isUniverse();

  public abstract TypeSet makeClone();

  /**
   * @param s2 another type set
   * @return intersection of this type set with the given type set
   */
  protected TypeSet specialCasesIntersectedWith(TypeSet s2) {
    return null;
  }

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  /**
   * Computes and returns a <em>new</em> TypeSet representing the intersection of the receiver with
   * s2. Does not modify the receiver or argument sets.
   *
   * @param s2 another type set
   * @return the new TypeSet
   */
  public TypeSet intersectedWith(TypeSet s2) {
    if (s2.isUniverse()) return makeClone();
    else if (isUniverse()) return s2.makeClone();
    else if (isEmpty() || s2.isEmpty()) return getTypeSetEnvironment().getEmptyTypeSet();
    else if (isSingleton()) {
      if (s2.contains(anyMember())) return makeClone();
      else return getTypeSetEnvironment().getEmptyTypeSet();
    } else if (s2.isSingleton()) {
      if (contains(s2.anyMember())) return s2.makeClone();
      else return getTypeSetEnvironment().getEmptyTypeSet();
    } else if (s2 instanceof TypeSetIntersection) {
      TypeSetIntersection x = (TypeSetIntersection) s2;
      // xsect(A,xsect(A,B)) = xsect(A,B) and
      // xsect(B,xsect(A,B)) = xsect(A,B)
      if (x.getLHS().equals(this) || x.getRHS().equals(this)) return x;
    }

    TypeSet result = specialCasesIntersectedWith(s2);

    if (result != null) return result;
    else return new TypeSetIntersection(this, s2);
  }

  /**
   * Returns the TypeSet resulting from union'ing the receiver with the argument. Does not modify
   * the receiver or the argument sets.
   *
   * @param that another type set
   * @return the union type set
   */
  public TypeSet addedTo(TypeSet that) {
    if (isUniverse() || that.isUniverse()) return getTypeSetEnvironment().getUniverseTypeSet();
    if ((this instanceof EnumeratedTypeSet || this instanceof SingletonTypeSet)
        && (that instanceof EnumeratedTypeSet || that instanceof SingletonTypeSet)) {
      EnumeratedTypeSet result = enumerate();

      result.addAll(that);
      return result;
    }
    return new TypeSetUnion(this, that);
  }

  /** @return a new TypeSet representing the set of all sub-types of the types in the receiver. */
  public TypeSet subTypes() {
    if (isUniverse() || contains(getJavaLangObject()))
      return getTypeSetEnvironment().getUniverseTypeSet();

    if (isSingleton()) return possiblyArraySubTypeSetFor(anyMember());

    return getTypeSetEnvironment().createSubTypesSet(this);
  }

  private TypeSet possiblyArraySubTypeSetFor(TType t) {
    // In Java, subTypes(x[]) == (subTypes(x))[]
    //		if (t.isArrayType()) {
    //			ArrayType at= (ArrayType) t;
    //
    //			return new ArrayTypeSet(possiblyArraySubTypeSetFor(at.getArrayElementType()));
    //		} else

    return getTypeSetEnvironment().createSubTypesOfSingleton(t);
  }

  private TypeSet possiblyArraySuperTypeSetFor(TType t) {
    // In Java, superTypes(x[]) == (superTypes(x))[] union {Object}
    //		if (t.isArrayType()) {
    //			ArrayType at= (ArrayType) t;
    //
    //			return new ArraySuperTypeSet(possiblyArraySuperTypeSetFor(at.getArrayElementType()));
    //		} else
    return getTypeSetEnvironment().createSuperTypesOfSingleton(t);
  }

  /** @return a new TypeSet representing the set of all super-types of the types in the receiver */
  public TypeSet superTypes() {
    if (isUniverse()) return getTypeSetEnvironment().getUniverseTypeSet();

    if (isSingleton()) return possiblyArraySuperTypeSetFor(anyMember());

    return getTypeSetEnvironment().createSuperTypesSet(this);
  }

  /** @return true iff the type set contains no types */
  public abstract boolean isEmpty();

  /** @return the types in the upper bound of this set */
  public abstract TypeSet upperBound();

  /** @return the types in the lower bound of this set */
  public abstract TypeSet lowerBound();

  /** @return true iff this TypeSet has a unique lower bound */
  public abstract boolean hasUniqueLowerBound();

  /** @return true iff this TypeSet has a unique upper bound other than java.lang.Object */
  public abstract boolean hasUniqueUpperBound();

  /** @return the unique lower bound of this set of types, if it has one, or null otherwise */
  public abstract TType uniqueLowerBound();

  /** @return the unique upper bound of this set of types, if it has one, or null otherwise */
  public abstract TType uniqueUpperBound();

  /**
   * @param t a type
   * @return true iff the type set contains the given type
   */
  public abstract boolean contains(TType t);

  /**
   * @param s another type set
   * @return true iff the type set contains all of the types in the given TypeSet
   */
  public abstract boolean containsAll(TypeSet s);

  /** @return an iterator over the types in the receiver */
  public abstract Iterator<TType> iterator();

  /** @return a new TypeSet enumerating the receiver's contents */
  public abstract EnumeratedTypeSet enumerate();

  /** @return true iff the given set has precisely one element */
  public abstract boolean isSingleton();

  /** @return an arbitrary member of the given Typeset */
  public abstract TType anyMember();
}

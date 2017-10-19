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
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TTypes;

/**
 * A type-safe wrapper for {@code Set<TType>} that also adds {@code TType}-specific functionality,
 * e.g. subTypes() and superTypes().
 */
public class EnumeratedTypeSet extends TypeSet {
  private static int sCount = 0;

  public static int getCount() {
    return sCount;
  }

  public static void resetCount() {
    sCount = 0;
  }

  /** Set containing the TTypes in this EnumeratedTypeSet. */
  Set<TType> fMembers = new LinkedHashSet<TType>();

  /**
   * Constructs a new EnumeratedTypeSet with the members of Set s in it. All elements of s must be
   * TTypes.
   *
   * @param types the types
   * @param typeSetEnvironment the environment
   */
  public EnumeratedTypeSet(Iterator<TType> types, TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    while (types.hasNext()) {
      fMembers.add(types.next());
    }
    sCount++;
  }

  /**
   * Constructs an empty EnumeratedTypeSet.
   *
   * @param typeSetEnvironment the environment
   */
  public EnumeratedTypeSet(TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    sCount++;
  }

  /**
   * Constructs a new EnumeratedTypeSet with the given single TType in it.
   *
   * @param t the type
   * @param typeSetEnvironment the environment
   */
  public EnumeratedTypeSet(TType t, TypeSetEnvironment typeSetEnvironment) {
    super(typeSetEnvironment);
    Assert.isNotNull(t);
    fMembers.add(t);
    sCount++;
  }

  /** @return <code>true</code> iff this set represents the universe of TTypes */
  @Override
  public boolean isUniverse() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof EnumeratedTypeSet) {
      EnumeratedTypeSet other = (EnumeratedTypeSet) o;

      return fMembers.equals(other.fMembers);
    } else if (o instanceof SingletonTypeSet) {
      SingletonTypeSet other = (SingletonTypeSet) o;

      return (fMembers.size() == 1) && fMembers.contains(other.anyMember());
    } else if (o instanceof TypeSet) {
      TypeSet other = (TypeSet) o;

      for (Iterator<TType> otherIter = other.iterator(); otherIter.hasNext(); ) {
        if (!fMembers.contains(otherIter.next())) return false;
      }
      for (Iterator<TType> myIter = fMembers.iterator(); myIter.hasNext(); ) {
        if (!other.contains(myIter.next())) return false;
      }
      return true;
    } else return false;
  }

  @Override
  public int hashCode() {
    return 37 + fMembers.hashCode();
  }

  /**
   * Computes and returns a <em>new</em> EnumeratedTypeSet representing the intersection of the
   * receiver with s2. Does not modify the receiver.
   *
   * @param s2 the other type set
   */
  @Override
  protected TypeSet specialCasesIntersectedWith(TypeSet s2) {
    if (s2 instanceof EnumeratedTypeSet) {
      EnumeratedTypeSet result = new EnumeratedTypeSet(getTypeSetEnvironment());

      result.addAll(this); // copy first since retainAll() modifies in-place
      result.retainAll(s2);
      if (result.size() > 0) return result;
      else return getTypeSetEnvironment().getEmptyTypeSet();
    }
    return null;
  }

  /**
   * Modifies this EnumeratedTypeSet to represent the intersection of the receiver with s2.
   *
   * @param s2 the other type set
   */
  public void intersectWith(TypeSet s2) {
    if (isUniverse()) {
      if (s2.isUniverse()) return;
      // More than an optimization: the universe never contains array types, so
      // if s2 has array types, the following will retain them, as it should.
      EnumeratedTypeSet ets2 = (EnumeratedTypeSet) s2;
      fMembers = new LinkedHashSet<TType>();
      fMembers.addAll(ets2.fMembers);
    } else retainAll(s2);
  }

  /** @return a new TypeSet representing the set of all sub-types of the types in the receiver */
  @Override
  public TypeSet subTypes() {
    if (isUniverse()) return makeClone(); // subtypes(universe) = universe

    if (fMembers.contains(getJavaLangObject())) return getTypeSetEnvironment().getUniverseTypeSet();

    return getTypeSetEnvironment().createSubTypesSet(this);
  }

  public static EnumeratedTypeSet makeArrayTypesForElements(
      Iterator<TType> elemTypes, TypeSetEnvironment typeSetEnvironment) {
    EnumeratedTypeSet result = new EnumeratedTypeSet(typeSetEnvironment);

    while (elemTypes.hasNext()) {
      TType t = elemTypes.next();
      result.add(TTypes.createArrayType(t, 1));
    }
    //		result.initComplete();
    return result;
  }

  /** @return a new TypeSet representing the set of all super-types of the types in the receiver */
  @Override
  public TypeSet superTypes() {
    if (isUniverse()) return makeClone(); // The supertypes of the universe is the universe

    return getTypeSetEnvironment().createSuperTypesSet(this);
  }

  @Override
  public TypeSet makeClone() {
    EnumeratedTypeSet result = new EnumeratedTypeSet(getTypeSetEnvironment());

    result.fMembers.addAll(fMembers);
    result.initComplete();
    return result;
  }

  /* (non-Javadoc)
   * @see java.util.Set#size()
   */
  public int size() {
    return fMembers.size();
  }

  /* (non-Javadoc)
   * @see java.util.Set#clear()
   */
  public void clear() {
    if (isUniverse()) fMembers = new LinkedHashSet<TType>();
    else fMembers.clear();
  }

  /* (non-Javadoc)
   * @see java.util.Set#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return fMembers.isEmpty();
  }

  /* (non-Javadoc)
   * @see java.util.Set#toArray()
   */
  public TType[] toArray() {
    return fMembers.toArray(new TType[fMembers.size()]);
  }

  /* (non-Javadoc)
   * @see java.util.Set#add(java.lang.Object)
   */
  public boolean add(TType t) {
    // Doesn't make sense to do here what other methods do (copy-and-modify)
    Assert.isTrue(!isUniverse(), "Someone's trying to expand the universe!"); // $NON-NLS-1$
    return fMembers.add(t);
  }

  /* (non-Javadoc)
   * @see java.util.Set#contains(java.lang.Object)
   */
  @Override
  public boolean contains(TType t) {
    if (isUniverse()) return true;
    return fMembers.contains(t);
  }

  /* (non-Javadoc)
   * @see java.util.Set#remove(java.lang.Object)
   */
  public boolean remove(TType t) {
    if (isUniverse()) fMembers = cloneSet(fMembers);
    return fMembers.remove(t);
  }

  private Set<TType> cloneSet(Set<TType> members) {
    Set<TType> result = new LinkedHashSet<TType>();
    result.addAll(members);
    return result;
  }

  /* (non-Javadoc)
   * @see java.util.Set#addAll(java.util.Collection)
   */
  public boolean addAll(TypeSet s) {
    if (s instanceof EnumeratedTypeSet) {
      EnumeratedTypeSet ets = (EnumeratedTypeSet) s;

      return fMembers.addAll(ets.fMembers);
    } else {
      EnumeratedTypeSet ets = s.enumerate();

      return fMembers.addAll(ets.fMembers);
    }
  }

  @Override
  public TypeSet addedTo(TypeSet that) {
    EnumeratedTypeSet result = new EnumeratedTypeSet(getTypeSetEnvironment());

    result.addAll(this);
    result.addAll(that);
    result.initComplete();
    return result;
  }

  /* (non-Javadoc)
   * @see java.util.Set#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll(TypeSet s) {
    if (isUniverse()) return true;
    if (s.isUniverse()) return false;
    EnumeratedTypeSet ets = s.enumerate();

    return fMembers.containsAll(ets.fMembers);
  }

  /* (non-Javadoc)
   * @see java.util.Set#removeAll(java.util.Collection)
   */
  public boolean removeAll(EnumeratedTypeSet s) {
    if (isUniverse()) fMembers = cloneSet(fMembers);
    return fMembers.removeAll(s.fMembers);
  }

  /* (non-Javadoc)
   * @see java.util.Set#retainAll(java.util.Collection)
   */
  public boolean retainAll(TypeSet s) {
    if (s.isUniverse()) return false;

    EnumeratedTypeSet ets = (EnumeratedTypeSet) s;

    if (isUniverse()) {
      fMembers = cloneSet(ets.fMembers);
      return true;
    } else return fMembers.retainAll(ets.fMembers);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#isSingleton()
   */
  @Override
  public boolean isSingleton() {
    return fMembers.size() == 1;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#anyMember()
   */
  @Override
  public TType anyMember() {
    return fMembers.iterator().next();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#upperBound()
   */
  @Override
  public TypeSet upperBound() {
    if (fMembers.size() == 1)
      return new SingletonTypeSet(fMembers.iterator().next(), getTypeSetEnvironment());
    if (fMembers.contains(getJavaLangObject()))
      return new SingletonTypeSet(getJavaLangObject(), getTypeSetEnvironment());

    EnumeratedTypeSet result = new EnumeratedTypeSet(getTypeSetEnvironment());

    // Add to result each element of fMembers that has no proper supertype in fMembers
    result.fMembers.addAll(fMembers);
    for (Iterator<TType> iter = fMembers.iterator(); iter.hasNext(); ) {
      TType t = iter.next();

      if (t.isArrayType()) {
        ArrayType at = (ArrayType) t;
        int numDims = at.getDimensions();
        for (Iterator<TType> subIter = TTypes.getAllSubTypesIterator(at.getElementType());
            subIter.hasNext(); ) {
          result.fMembers.remove(TTypes.createArrayType(subIter.next(), numDims));
        }
      } else {
        for (Iterator<TType> iterator = TTypes.getAllSubTypesIterator(t); iterator.hasNext(); ) {
          result.fMembers.remove(iterator.next());
        }
      }
    }
    result.initComplete();
    return result;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#lowerBound()
   */
  @Override
  public TypeSet lowerBound() {
    if (fMembers.size() == 1)
      return new SingletonTypeSet(fMembers.iterator().next(), getTypeSetEnvironment());

    EnumeratedTypeSet result = new EnumeratedTypeSet(getTypeSetEnvironment());

    // Add to result each element of fMembers that has no proper subtype in fMembers
    result.fMembers.addAll(fMembers);

    for (Iterator<TType> iter = fMembers.iterator(); iter.hasNext(); ) {
      TType t = iter.next();

      // java.lang.Object is only in the lower bound if fMembers consists
      // of only java.lang.Object, but that case is handled above.
      if (t.equals(getJavaLangObject())) {
        result.fMembers.remove(t);
        continue;
      }

      if (t instanceof ArrayType) {
        ArrayType at = (ArrayType) t;
        int numDims = at.getDimensions();
        for (Iterator<TType> superIter = TTypes.getAllSuperTypesIterator(at.getElementType());
            superIter.hasNext(); ) {
          result.fMembers.remove(TTypes.createArrayType(superIter.next(), numDims));
        }
      } else {
        for (Iterator<TType> iterator = TTypes.getAllSuperTypesIterator(t); iterator.hasNext(); ) {
          result.fMembers.remove(iterator.next());
        }
      }
    }
    if (result.size() > 0) return result;
    else return getTypeSetEnvironment().getEmptyTypeSet();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueLowerBound()
   */
  @Override
  public boolean hasUniqueLowerBound() {
    return fMembers.size() == 1;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#hasUniqueUpperBound()
   */
  @Override
  public boolean hasUniqueUpperBound() {
    return fMembers.size() == 1;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueLowerBound()
   */
  @Override
  public TType uniqueLowerBound() {
    if (fMembers.size() == 1) return fMembers.iterator().next();
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#uniqueUpperBound()
   */
  @Override
  public TType uniqueUpperBound() {
    if (fMembers.size() == 1) return fMembers.iterator().next();
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.Set#iterator()
   */
  @Override
  public Iterator<TType> iterator() {
    return fMembers.iterator();
  }

  /** Limits the display of set elements to the first sMaxElements. */
  private static final int sMaxElements = 10; // Integer.MAX_VALUE;

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append("{" + fID + ":"); // $NON-NLS-1$ //$NON-NLS-2$
    if (isUniverse()) b.append(" <universe>"); // $NON-NLS-1$
    else {
      int count = 0;
      Iterator<TType> iter;
      for (iter = iterator(); iter.hasNext() && count < sMaxElements; count++) {
        TType type = iter.next();
        b.append(' ').append(type.getPrettySignature());
        if (iter.hasNext()) b.append(',');
      }
      if (iter.hasNext()) b.append(" ..."); // $NON-NLS-1$
    }
    b.append(" }"); // $NON-NLS-1$
    return b.toString();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet#enumerate()
   */
  @Override
  public EnumeratedTypeSet enumerate() {
    return this; // (EnumeratedTypeSet) makeClone();
  }

  public void initComplete() {
    Assert.isTrue(!fMembers.isEmpty());
  }
}

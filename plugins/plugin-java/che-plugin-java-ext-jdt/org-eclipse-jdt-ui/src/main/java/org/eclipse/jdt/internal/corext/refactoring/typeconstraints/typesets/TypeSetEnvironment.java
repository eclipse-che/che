/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TypeEnvironment;

public class TypeSetEnvironment {

  private final TypeEnvironment fTypeEnvironment;
  private final TypeUniverseSet fUniverse;
  private final EmptyTypeSet fEmptyTypeSet;

  private final Map<TType, SubTypesOfSingleton> fSubtypesOfSingletons =
      new LinkedHashMap<TType, SubTypesOfSingleton>(); // @perf
  private final Map<TypeSet, SubTypesSet> fSubTypesSets =
      new LinkedHashMap<TypeSet, SubTypesSet>(); // @perf
  private final Map<TType, SuperTypesOfSingleton> fSuperTypesOfSingletons =
      new LinkedHashMap<TType, SuperTypesOfSingleton>(); // @perf
  private final Map<Object, SuperTypesSet> fSuperTypesSets =
      new LinkedHashMap<Object, SuperTypesSet>(); // @perf

  private int fgCommonExprHits = 0;
  private int fgCommonExprMisses = 0;

  public TypeSetEnvironment(TypeEnvironment typeEnvironment) {
    fTypeEnvironment = typeEnvironment;
    fUniverse = new TypeUniverseSet(this);
    fEmptyTypeSet = new EmptyTypeSet(this);
  }

  public TType getJavaLangObject() {
    return fTypeEnvironment.getJavaLangObject();
  }

  public TypeUniverseSet getUniverseTypeSet() {
    return fUniverse;
  }

  public EmptyTypeSet getEmptyTypeSet() {
    return fEmptyTypeSet;
  }

  public SubTypesOfSingleton createSubTypesOfSingleton(TType superType) {
    if (superType.isJavaLangObject()) return this.getUniverseTypeSet();
    if (fSubtypesOfSingletons.containsKey(superType)) {
      fgCommonExprHits++;
      return fSubtypesOfSingletons.get(superType);
    } else {
      SubTypesOfSingleton s = new SubTypesOfSingleton(superType, this);

      fgCommonExprMisses++;
      fSubtypesOfSingletons.put(superType, s);
      return s;
    }
  }

  public SubTypesSet createSubTypesSet(TypeSet superTypes) {
    if (fSubTypesSets.containsKey(superTypes)) {
      fgCommonExprHits++;
      return fSubTypesSets.get(superTypes);
    } else {
      SubTypesSet s = new SubTypesSet(superTypes);

      fgCommonExprMisses++;
      fSubTypesSets.put(superTypes, s);
      return s;
    }
  }

  public SuperTypesOfSingleton createSuperTypesOfSingleton(TType subType) {
    if (fSuperTypesOfSingletons.containsKey(subType)) {
      fgCommonExprHits++;
      return fSuperTypesOfSingletons.get(subType);
    } else {
      SuperTypesOfSingleton s = new SuperTypesOfSingleton(subType, this);

      fgCommonExprMisses++;
      fSuperTypesOfSingletons.put(subType, s);
      return s;
    }
  }

  public SuperTypesSet createSuperTypesSet(TType subType) {
    if (fSuperTypesSets.containsKey(subType)) {
      fgCommonExprHits++;
      return fSuperTypesSets.get(subType);
    } else {
      SuperTypesSet s = new SuperTypesSet(subType, this);

      fgCommonExprMisses++;
      fSuperTypesSets.put(subType, s);
      return s;
    }
  }

  public SuperTypesSet createSuperTypesSet(TypeSet subTypes) {
    if (fSuperTypesSets.containsKey(subTypes)) return fSuperTypesSets.get(subTypes);
    else {
      SuperTypesSet s = new SuperTypesSet(subTypes, this);

      fSuperTypesSets.put(subTypes, s);
      return s;
    }
  }

  public void dumpStats() {
    System.out.println("Common expression hits:   " + fgCommonExprHits); // $NON-NLS-1$
    System.out.println("Common expression misses: " + fgCommonExprMisses); // $NON-NLS-1$
  }
}

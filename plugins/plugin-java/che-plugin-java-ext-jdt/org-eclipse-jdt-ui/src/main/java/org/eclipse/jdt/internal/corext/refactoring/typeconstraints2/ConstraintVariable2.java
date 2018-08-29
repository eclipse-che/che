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
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

public abstract class ConstraintVariable2 {

  public static final String TO_STRING = "toString"; // $NON-NLS-1$

  private Object[] fDatas;

  private TypeEquivalenceSet fTypeEquivalenceSet;

  protected final TType fType;

  /** @param type the type */
  protected ConstraintVariable2(TType type) {
    fType = type;
  }

  public Object getData(String name) {
    if (fDatas == null) {
      return null;
    } else {
      for (int i = 0; i < fDatas.length; i += 2) {
        String key = (String) fDatas[i];
        if (key.equals(name)) return fDatas[i + 1];
      }
      return null;
    }
  }

  public TypeEquivalenceSet getTypeEquivalenceSet() {
    return fTypeEquivalenceSet;
  }

  /**
   * @return the type binding, or <code>null</code> iff the type constraint variable has no type in
   *     the original source (e.g. {@link CollectionElementVariable2})
   */
  public TType getType() {
    return fType;
  }

  public ITypeSet getTypeEstimate() {
    Assert.isNotNull(fTypeEquivalenceSet);
    return fTypeEquivalenceSet.getTypeEstimate();
  }

  public void setData(String name, Object data) {
    int index = 0;
    if (fDatas != null) {
      while (index < fDatas.length) {
        if (name.equals(fDatas[index])) break;
        index += 2;
      }
    }
    if (data != null) { // add
      if (fDatas != null) {
        if (index == fDatas.length) {
          Object[] newTable = new Object[fDatas.length + 2];
          System.arraycopy(fDatas, 0, newTable, 0, fDatas.length);
          fDatas = newTable;
        }
      } else {
        fDatas = new Object[2];
      }
      fDatas[index] = name;
      fDatas[index + 1] = data;
    } else { // remove
      if (fDatas != null) {
        if (index != fDatas.length) {
          int length = fDatas.length - 2;
          if (length == 0) {
            fDatas = null;
          } else {
            Object[] newTable = new Object[length];
            System.arraycopy(fDatas, 0, newTable, 0, index);
            System.arraycopy(fDatas, index + 2, newTable, index, length - index);
            fDatas = newTable;
          }
        }
      }
    }
  }

  public void setTypeEquivalenceSet(TypeEquivalenceSet set) {
    fTypeEquivalenceSet = set;
  }

  @Override
  public String toString() {
    String toString = (String) getData(TO_STRING);
    if (toString != null) return toString;

    String name = getClass().getName();
    int dot = name.lastIndexOf('.');
    return name.substring(dot + 1) + ": " + fType != null
        ? fType.getPrettySignature()
        : "<NONE>"; // $NON-NLS-1$ //$NON-NLS-2$
  }
}

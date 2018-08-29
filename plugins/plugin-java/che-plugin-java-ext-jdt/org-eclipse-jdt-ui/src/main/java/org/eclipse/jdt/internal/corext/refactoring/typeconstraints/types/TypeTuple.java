/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types;

public class TypeTuple {
  private TType fFirst;
  private TType fSecond;

  public TypeTuple(TType first, TType second) {
    super();
    fFirst = first;
    fSecond = second;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof TypeTuple)) return false;
    TypeTuple other = (TypeTuple) obj;
    return fFirst.equals(other.fFirst) && fSecond.equals(other.fSecond);
  }

  @Override
  public int hashCode() {
    return fFirst.hashCode() << 16 + fSecond.hashCode();
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.text;

/** Default implementation of {@link TypedRegion}. A <code>TypedRegion</code> is a value object. */
public class TypedRegionImpl extends RegionImpl implements TypedRegion {

  /** The region's type */
  private String fType;

  /**
   * Creates a typed region based on the given specification.
   *
   * @param offset the region's offset
   * @param length the region's length
   * @param type the region's type
   */
  public TypedRegionImpl(int offset, int length, String type) {
    super(offset, length);
    fType = type;
  }

  /* @see org.eclipse.jface.text.ITypedRegion#getProjectType() */
  @Override
  public String getType() {
    return fType;
  }

  /* @see java.lang.Object#isEquals(java.lang.Object) */
  @Override
  public boolean equals(Object o) {
    if (o instanceof TypedRegionImpl) {
      TypedRegionImpl r = (TypedRegionImpl) o;
      return super.equals(r)
          && ((fType == null && r.getType() == null) || fType.equals(r.getType()));
    }
    return false;
  }

  /* @see java.lang.Object#hashCode() */
  @Override
  public int hashCode() {
    int type = fType == null ? 0 : fType.hashCode();
    return super.hashCode() | type;
  }

  /*
   * @see org.eclipse.jface.text.Region#toString()
   * @since 3.5
   */
  @Override
  public String toString() {
    return fType + " - " + super.toString(); // $NON-NLS-1$
  }
}

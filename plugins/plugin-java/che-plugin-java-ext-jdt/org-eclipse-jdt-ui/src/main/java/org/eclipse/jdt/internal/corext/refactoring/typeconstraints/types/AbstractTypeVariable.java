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

import org.eclipse.jdt.core.dom.ITypeBinding;

public abstract class AbstractTypeVariable extends TType {

  protected TType[] fBounds;

  protected AbstractTypeVariable(TypeEnvironment environment) {
    super(environment);
  }

  @Override
  protected void initialize(ITypeBinding binding) {
    super.initialize(binding);
    ITypeBinding[] bounds = binding.getTypeBounds();
    if (bounds.length == 0) {
      fBounds = EMPTY_TYPE_ARRAY;
      if (getEnvironment().getJavaLangObject() == null) {
        getEnvironment().initializeJavaLangObject(binding.getErasure());
      }
    } else {
      fBounds = new TType[bounds.length];
      for (int i = 0; i < bounds.length; i++) {
        fBounds[i] = getEnvironment().create(bounds[i]);
      }
    }
  }

  @Override
  public TType getErasure() {
    if (fBounds.length == 0) return getEnvironment().getJavaLangObject();
    return fBounds[0].getErasure();
  }

  /* package */ final boolean isUnbounded() {
    if (fBounds.length == 0) return true;
    return fBounds[0].isJavaLangObject();
  }

  public final TType[] getBounds() {
    return fBounds.clone();
  }

  @Override
  public final TType[] getSubTypes() {
    return EMPTY_TYPE_ARRAY;
  }

  protected final boolean checkAssignmentBound(TType rhs) {
    if (fBounds.length == 0) return true;
    for (int i = 0; i < fBounds.length; i++) {
      if (rhs.canAssignTo(fBounds[i])) return true;
    }
    return false;
  }

  protected final boolean canAssignOneBoundTo(TType lhs) {
    if (fBounds.length == 0) return lhs.isJavaLangObject();
    for (int i = 0; i < fBounds.length; i++) {
      if (fBounds[i].canAssignTo(lhs)) return true;
    }
    return false;
  }
}

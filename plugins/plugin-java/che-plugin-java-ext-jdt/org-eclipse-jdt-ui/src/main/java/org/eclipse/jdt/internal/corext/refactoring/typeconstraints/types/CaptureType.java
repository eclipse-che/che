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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class CaptureType extends AbstractTypeVariable {

  private WildcardType fWildcard;
  private IJavaProject fJavaProject;

  protected CaptureType(TypeEnvironment environment) {
    super(environment);
  }

  protected void initialize(ITypeBinding binding, IJavaProject javaProject) {
    Assert.isTrue(binding.isCapture());
    super.initialize(binding);
    fWildcard = (WildcardType) getEnvironment().create(binding.getWildcard());
    fJavaProject = javaProject;
  }

  @Override
  public int getKind() {
    return CAPTURE_TYPE;
  }

  public WildcardType getWildcard() {
    return fWildcard;
  }

  @Override
  public boolean doEquals(TType type) {
    return getBindingKey().equals(((CaptureType) type).getBindingKey())
        && fJavaProject.equals(((CaptureType) type).fJavaProject);
  }

  @Override
  public int hashCode() {
    return getBindingKey().hashCode();
  }

  @Override
  protected boolean doCanAssignTo(TType lhs) {
    switch (lhs.getKind()) {
      case NULL_TYPE:
      case VOID_TYPE:
      case PRIMITIVE_TYPE:
        return false;

      case ARRAY_TYPE:
        return canAssignFirstBoundTo(lhs);

      case GENERIC_TYPE:
        return false;

      case STANDARD_TYPE:
      case PARAMETERIZED_TYPE:
      case RAW_TYPE:
        return canAssignOneBoundTo(lhs);

      case UNBOUND_WILDCARD_TYPE:
      case EXTENDS_WILDCARD_TYPE:
      case SUPER_WILDCARD_TYPE:
        return ((WildcardType) lhs).checkAssignmentBound(this);

      case TYPE_VARIABLE:
        return false; // fWildcard.doCanAssignTo(lhs);

      case CAPTURE_TYPE:
        return ((CaptureType) lhs).checkLowerBound(this.getWildcard());
    }
    return false;
  }

  protected boolean checkLowerBound(TType rhs) {
    if (!getWildcard().isSuperWildcardType()) return false;

    return rhs.canAssignTo(getWildcard().getBound());
  }

  private boolean canAssignFirstBoundTo(TType lhs) {
    if (fBounds.length > 0 && fBounds[0].isArrayType()) {
      // capture of ? extends X[]
      return fBounds[0].canAssignTo(lhs);
    }
    return false;
  }

  @Override
  public String getName() {
    return ""; // $NON-NLS-1$
  }

  @Override
  protected String getPlainPrettySignature() {
    return "capture-of " + fWildcard.getPrettySignature(); // $NON-NLS-1$
  }
}

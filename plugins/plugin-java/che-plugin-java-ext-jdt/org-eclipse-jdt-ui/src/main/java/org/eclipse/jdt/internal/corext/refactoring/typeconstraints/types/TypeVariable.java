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
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class TypeVariable extends AbstractTypeVariable {

  private ITypeParameter fJavaTypeParameter;

  protected TypeVariable(TypeEnvironment environment) {
    super(environment);
  }

  protected void initialize(ITypeBinding binding, ITypeParameter javaTypeParameter) {
    Assert.isTrue(binding.isTypeVariable());
    Assert.isNotNull(javaTypeParameter);
    fJavaTypeParameter = javaTypeParameter;
    super.initialize(binding);
  }

  @Override
  public int getKind() {
    return TYPE_VARIABLE;
  }

  @Override
  public boolean doEquals(TType type) {
    return fJavaTypeParameter.equals(((TypeVariable) type).fJavaTypeParameter);
  }

  @Override
  public int hashCode() {
    return fJavaTypeParameter.hashCode();
  }

  @Override
  protected boolean doCanAssignTo(TType lhs) {
    switch (lhs.getKind()) {
      case NULL_TYPE:
      case VOID_TYPE:
        return false;
      case PRIMITIVE_TYPE:

      case ARRAY_TYPE:
        return false;

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
        return doExtends((TypeVariable) lhs);
      case CAPTURE_TYPE:
        return ((CaptureType) lhs).checkLowerBound(this);
    }
    return false;
  }

  private boolean doExtends(TypeVariable other) {
    for (int i = 0; i < fBounds.length; i++) {
      TType bound = fBounds[i];
      if (other.equals(bound)
          || (bound.getKind() == TYPE_VARIABLE && ((TypeVariable) bound).doExtends(other)))
        return true;
    }
    return false;
  }

  @Override
  public String getName() {
    return fJavaTypeParameter.getElementName();
  }

  @Override
  public String getPrettySignature() {
    if (fBounds.length == 1 && fBounds[0].isJavaLangObject())
      return fJavaTypeParameter.getElementName(); // don't print the trivial bound

    StringBuffer result = new StringBuffer(fJavaTypeParameter.getElementName());
    if (fBounds.length > 0) {
      result.append(" extends "); // $NON-NLS-1$
      result.append(fBounds[0].getPlainPrettySignature());
      for (int i = 1; i < fBounds.length; i++) {
        result.append(" & "); // $NON-NLS-1$
        result.append(fBounds[i].getPlainPrettySignature());
      }
    }
    return result.toString();
  }

  @Override
  protected String getPlainPrettySignature() {
    return fJavaTypeParameter.getElementName();
  }
}

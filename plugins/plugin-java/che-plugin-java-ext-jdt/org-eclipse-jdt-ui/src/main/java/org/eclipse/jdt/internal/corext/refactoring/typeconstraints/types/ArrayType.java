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
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class ArrayType extends TType {
  private TType fElementType;
  private int fDimensions;

  private TType fErasure;

  protected ArrayType(TypeEnvironment environment) {
    super(environment);
  }

  protected ArrayType(TypeEnvironment environment, String key) {
    super(environment, key);
  }

  protected void initialize(ITypeBinding binding, TType elementType) {
    Assert.isTrue(binding.isArray());
    super.initialize(binding);
    fElementType = elementType;
    fDimensions = binding.getDimensions();
    if (fElementType.isStandardType()
        || fElementType.isGenericType()
        || fElementType.isPrimitiveType()) {
      fErasure = this;
    } else {
      fErasure = getEnvironment().create(binding.getErasure());
    }
  }

  protected void initialize(TType elementType, int dimensions) {
    fElementType = elementType;
    fDimensions = dimensions;
    if (fElementType.isStandardType()
        || fElementType.isGenericType()
        || fElementType.isPrimitiveType()) {
      fErasure = this;
    } else {
      fErasure = getEnvironment().createArrayType(elementType.getErasure(), dimensions);
    }
  }

  public TType getElementType() {
    return fElementType;
  }

  /**
   * Returns the component type of this array. If getDimensions() is 1, the component type is the
   * element type. If getDimensions() is > 1, the component type is an array type with element type
   * getElementType() and dimensions getDimensions() - 1.
   *
   * @return the component type
   */
  public TType getComponentType() {
    if (fDimensions > 1) return getEnvironment().createArrayType(fElementType, fDimensions - 1);
    else return fElementType;
  }

  public int getDimensions() {
    return fDimensions;
  }

  @Override
  public int getKind() {
    return ARRAY_TYPE;
  }

  @Override
  public TType[] getSubTypes() {
    TType[] subTypes = fElementType.getSubTypes();
    TType[] result = new TType[subTypes.length];
    for (int i = 0; i < subTypes.length; i++) {
      result[i] = getEnvironment().createArrayType(subTypes[i], fDimensions);
    }
    return result;
  }

  @Override
  public TType getErasure() {
    return fErasure;
  }

  @Override
  public boolean doEquals(TType other) {
    ArrayType arrayType = (ArrayType) other;
    return fElementType.equals(arrayType.fElementType) && fDimensions == arrayType.fDimensions;
  }

  @Override
  public int hashCode() {
    return fElementType.hashCode() << ARRAY_TYPE_SHIFT;
  }

  @Override
  protected boolean doCanAssignTo(TType lhs) {
    switch (lhs.getKind()) {
      case NULL_TYPE:
        return false;
      case VOID_TYPE:
        return false;
      case PRIMITIVE_TYPE:
        return false;

      case ARRAY_TYPE:
        return canAssignToArrayType((ArrayType) lhs);

      case GENERIC_TYPE:
        return false;
      case STANDARD_TYPE:
        return isArrayLhsCompatible(lhs);
      case PARAMETERIZED_TYPE:
        return false;
      case RAW_TYPE:
        return false;

      case UNBOUND_WILDCARD_TYPE:
      case EXTENDS_WILDCARD_TYPE:
      case SUPER_WILDCARD_TYPE:
        return ((WildcardType) lhs).checkAssignmentBound(this);

      case TYPE_VARIABLE:
        return false;
      case CAPTURE_TYPE:
        return ((CaptureType) lhs).checkLowerBound(this);
    }
    return false;
  }

  private boolean canAssignToArrayType(ArrayType lhs) {
    if (fDimensions == lhs.fDimensions) {
      // primitive type don't have any conversion for arrays.
      if (fElementType.getKind() == PRIMITIVE_TYPE || lhs.fElementType.getKind() == PRIMITIVE_TYPE)
        return fElementType.isTypeEquivalentTo(lhs.fElementType);
      return fElementType.canAssignTo(lhs.fElementType);
    }
    if (fDimensions < lhs.fDimensions) return false;
    return isArrayLhsCompatible(lhs.fElementType);
  }

  private boolean isArrayLhsCompatible(TType lhsElementType) {
    return lhsElementType.isJavaLangObject()
        || lhsElementType.isJavaLangCloneable()
        || lhsElementType.isJavaIoSerializable();
  }

  @Override
  protected String getPlainPrettySignature() {
    StringBuffer result = new StringBuffer(fElementType.getPlainPrettySignature());
    for (int i = 0; i < fDimensions; i++) {
      result.append("[]"); // $NON-NLS-1$
    }
    return result.toString();
  }

  @Override
  public String getName() {
    StringBuffer result = new StringBuffer(fElementType.getName());
    for (int i = 0; i < fDimensions; i++) {
      result.append("[]"); // $NON-NLS-1$
    }
    return result.toString();
  }
}

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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class ParameterizedType extends HierarchyType {

  private GenericType fTypeDeclaration;
  private TType[] fTypeArguments;

  protected ParameterizedType(TypeEnvironment environment) {
    super(environment);
  }

  @Override
  protected void initialize(ITypeBinding binding, IType javaElementType) {
    Assert.isTrue(binding.isParameterizedType());
    super.initialize(binding, javaElementType);
    TypeEnvironment environment = getEnvironment();
    fTypeDeclaration = (GenericType) environment.create(binding.getTypeDeclaration());
    ITypeBinding[] typeArguments = binding.getTypeArguments();
    fTypeArguments = new TType[typeArguments.length];
    for (int i = 0; i < typeArguments.length; i++) {
      fTypeArguments[i] = environment.create(typeArguments[i]);
    }
  }

  @Override
  public int getKind() {
    return PARAMETERIZED_TYPE;
  }

  @Override
  public TType getTypeDeclaration() {
    return fTypeDeclaration;
  }

  @Override
  public TType getErasure() {
    return fTypeDeclaration;
  }

  public TType[] getTypeArguments() {
    return fTypeArguments.clone();
  }

  @Override
  public boolean doEquals(TType type) {
    ParameterizedType other = (ParameterizedType) type;
    if (!getBindingKey().equals(other.getBindingKey())) return false;
    if (!getJavaElementType().equals(other.getJavaElementType())) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return getBindingKey().hashCode();
  }

  @Override
  protected boolean doCanAssignTo(TType lhs) {
    int targetType = lhs.getKind();
    switch (targetType) {
      case NULL_TYPE:
        return false;
      case VOID_TYPE:
        return false;
      case PRIMITIVE_TYPE:
        return false;

      case ARRAY_TYPE:
        return false;

      case STANDARD_TYPE:
        return canAssignToStandardType((StandardType) lhs);
      case GENERIC_TYPE:
        return false;
      case PARAMETERIZED_TYPE:
        return canAssignToParameterizedType((ParameterizedType) lhs);
      case RAW_TYPE:
        return canAssignToRawType((RawType) lhs);

      case UNBOUND_WILDCARD_TYPE:
      case SUPER_WILDCARD_TYPE:
      case EXTENDS_WILDCARD_TYPE:
        return ((WildcardType) lhs).checkAssignmentBound(this);

      case TYPE_VARIABLE:
        return false;
      case CAPTURE_TYPE:
        return ((CaptureType) lhs).checkLowerBound(this);
    }
    return false;
  }

  @Override
  protected boolean isTypeEquivalentTo(TType other) {
    int otherElementType = other.getKind();
    if (otherElementType == RAW_TYPE || otherElementType == GENERIC_TYPE)
      return getErasure().isTypeEquivalentTo(other.getErasure());
    return super.isTypeEquivalentTo(other);
  }

  private boolean canAssignToRawType(RawType target) {
    return fTypeDeclaration.isSubType(target.getHierarchyType());
  }

  private boolean canAssignToParameterizedType(ParameterizedType target) {
    GenericType targetDeclaration = target.fTypeDeclaration;
    ParameterizedType sameSourceType = findSameDeclaration(targetDeclaration);
    if (sameSourceType == null) return false;
    TType[] targetArguments = target.fTypeArguments;
    TType[] sourceArguments = sameSourceType.fTypeArguments;
    if (targetArguments.length != sourceArguments.length) return false;
    for (int i = 0; i < sourceArguments.length; i++) {
      if (!targetArguments[i].checkTypeArgument(sourceArguments[i])) return false;
    }
    return true;
  }

  private ParameterizedType findSameDeclaration(GenericType targetDeclaration) {
    if (fTypeDeclaration.equals(targetDeclaration)) return this;
    ParameterizedType result = null;
    TType type = getSuperclass();
    if (type != null && type.getKind() == PARAMETERIZED_TYPE) {
      result = ((ParameterizedType) type).findSameDeclaration(targetDeclaration);
      if (result != null) return result;
    }
    TType[] interfaces = getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      type = interfaces[i];
      if (type != null && type.getKind() == PARAMETERIZED_TYPE) {
        result = ((ParameterizedType) type).findSameDeclaration(targetDeclaration);
        if (result != null) return result;
      }
    }
    return null;
  }

  @Override
  public String getName() {
    StringBuffer result = new StringBuffer(getJavaElementType().getElementName());
    result.append("<"); // $NON-NLS-1$
    result.append(fTypeArguments[0].getName());
    for (int i = 1; i < fTypeArguments.length; i++) {
      result.append(", "); // $NON-NLS-1$
      result.append(fTypeArguments[i].getName());
    }
    result.append(">"); // $NON-NLS-1$
    return result.toString();
  }

  @Override
  protected String getPlainPrettySignature() {
    StringBuffer result = new StringBuffer(getJavaElementType().getFullyQualifiedName('.'));
    result.append("<"); // $NON-NLS-1$
    result.append(fTypeArguments[0].getPlainPrettySignature());
    for (int i = 1; i < fTypeArguments.length; i++) {
      result.append(", "); // $NON-NLS-1$
      result.append(fTypeArguments[i].getPlainPrettySignature());
    }
    result.append(">"); // $NON-NLS-1$
    return result.toString();
  }
}

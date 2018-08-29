/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class ParameterInfo {

  public static final int INDEX_FOR_ADDED = -1;
  public static final String ELLIPSIS = "..."; // $NON-NLS-1$

  private IVariableBinding fOldBinding;
  private ITypeBinding fOldTypeBinding;
  private final String fOldName;
  private final String fOldTypeName;
  private final int fOldIndex;

  private String fNewTypeName;
  private ITypeBinding fNewTypeBinding;
  private String fDefaultValue;
  private String fNewName;
  private boolean fIsDeleted;
  private boolean fCreateField = true;
  private boolean fInlined;
  private boolean fResolve = true;

  public ParameterInfo(String type, String name, int index) {
    this(null, null, type, name, index);
  }

  public ParameterInfo(IVariableBinding binding, String type, String name, int index) {
    this(binding, null, type, name, index);
  }

  private ParameterInfo(
      IVariableBinding binding, ITypeBinding typeBinding, String type, String name, int index) {
    fOldBinding = binding;
    fOldTypeBinding = typeBinding;
    fNewTypeBinding = typeBinding;
    fOldTypeName = type;
    fNewTypeName = type;
    fOldName = name;
    fNewName = name;
    fOldIndex = index;
    fDefaultValue = ""; // $NON-NLS-1$
    fIsDeleted = false;
  }

  /**
   * Creates a new ParameterInfo. Parameter is marked as added and not resolvable
   *
   * @param type the fullyqualified type
   * @param name the name
   * @return the parameter info object
   */
  public static ParameterInfo createInfoForAddedParameter(String type, String name) {
    ParameterInfo info = new ParameterInfo("", "", INDEX_FOR_ADDED); // $NON-NLS-1$ //$NON-NLS-2$
    info.setNewTypeName(type);
    info.setNewName(name);
    info.setResolve(false);
    return info;
  }

  private void setResolve(boolean resolve) {
    fResolve = resolve;
  }

  public static ParameterInfo createInfoForAddedParameter(
      String type, String name, String defaultValue) {
    ParameterInfo info = new ParameterInfo("", "", INDEX_FOR_ADDED); // $NON-NLS-1$ //$NON-NLS-2$
    info.setNewTypeName(type);
    info.setNewName(name);
    info.setDefaultValue(defaultValue);
    return info;
  }

  public static ParameterInfo createInfoForAddedParameter(
      ITypeBinding typeBinding, String type, String name, String defaultValue) {
    ParameterInfo info =
        new ParameterInfo(null, typeBinding, "", "", INDEX_FOR_ADDED); // $NON-NLS-1$ //$NON-NLS-2$
    info.setNewTypeName(type);
    info.setNewName(name);
    info.setDefaultValue(defaultValue);
    return info;
  }

  public int getOldIndex() {
    return fOldIndex;
  }

  public boolean isDeleted() {
    return fIsDeleted;
  }

  public void markAsDeleted() {
    Assert.isTrue(!isAdded()); // added param infos should be simply removed from the list
    fIsDeleted = true;
  }

  public boolean isAdded() {
    return fOldIndex == INDEX_FOR_ADDED;
  }

  public boolean isTypeNameChanged() {
    return !fOldTypeName.equals(fNewTypeName);
  }

  public boolean isRenamed() {
    return !fOldName.equals(fNewName);
  }

  public boolean isVarargChanged() {
    return isOldVarargs() != isNewVarargs();
  }

  public IVariableBinding getOldBinding() {
    return fOldBinding;
  }

  public String getOldTypeName() {
    return fOldTypeName;
  }

  public String getNewTypeName() {
    return fNewTypeName;
  }

  public void setNewTypeName(String type) {
    Assert.isNotNull(type);
    fNewTypeName = type;
  }

  public ITypeBinding getNewTypeBinding() {
    return fNewTypeBinding;
  }

  public void setNewTypeBinding(ITypeBinding typeBinding) {
    fNewTypeBinding = typeBinding;
  }

  public boolean isOldVarargs() {
    return isVarargs(fOldTypeName);
  }

  public boolean isNewVarargs() {
    return isVarargs(fNewTypeName);
  }

  public String getOldName() {
    return fOldName;
  }

  public String getNewName() {
    return fNewName;
  }

  public void setNewName(String newName) {
    Assert.isNotNull(newName);
    fNewName = newName;
  }

  public String getDefaultValue() {
    return fDefaultValue;
  }

  public void setDefaultValue(String value) {
    Assert.isNotNull(value);
    fDefaultValue = value;
  }

  @Override
  public String toString() {
    return fOldTypeName
        + " "
        + fOldName
        + " @"
        + fOldIndex
        + " -> " // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        + fNewTypeName
        + " "
        + fNewName
        + ": "
        + fDefaultValue // $NON-NLS-1$//$NON-NLS-2$
        + (fIsDeleted ? " (deleted)" : " (stays)"); // $NON-NLS-1$//$NON-NLS-2$
  }

  public static String stripEllipsis(String typeName) {
    if (isVarargs(typeName)) return typeName.substring(0, typeName.length() - 3);
    else return typeName;
  }

  public static boolean isVarargs(String typeName) {
    return typeName.endsWith("..."); // $NON-NLS-1$
  }

  public ITypeBinding getOldTypeBinding() {
    return fOldTypeBinding;
  }

  public boolean isCreateField() {
    return fCreateField;
  }

  public void setCreateField(boolean createField) {
    fIsDeleted = createField;
    fCreateField = createField;
  }

  public void setOldBinding(IVariableBinding binding) {
    // The variableBinding is needed by IPOR to check what modifier were present
    fOldBinding = binding;
    fOldTypeBinding = binding.getType();
    fNewTypeBinding = binding.getType();
  }

  public void setInlined(boolean inlined) {
    fInlined = inlined;
  }

  public boolean isInlined() {
    return fInlined;
  }

  public boolean isResolve() {
    return fResolve;
  }
}

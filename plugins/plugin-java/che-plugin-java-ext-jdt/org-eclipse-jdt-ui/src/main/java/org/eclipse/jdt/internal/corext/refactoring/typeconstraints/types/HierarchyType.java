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

import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

public abstract class HierarchyType extends TType {
  private HierarchyType fSuperclass;
  private HierarchyType[] fInterfaces;
  private IType fJavaElementType;

  protected HierarchyType(TypeEnvironment environment) {
    super(environment);
  }

  protected void initialize(ITypeBinding binding, IType javaElementType) {
    super.initialize(binding);
    Assert.isNotNull(javaElementType);
    fJavaElementType = javaElementType;
    TypeEnvironment environment = getEnvironment();
    ITypeBinding superclass = binding.getSuperclass();
    if (superclass != null) {
      fSuperclass = (HierarchyType) environment.create(superclass);
    }
    ITypeBinding[] interfaces = binding.getInterfaces();
    fInterfaces = new HierarchyType[interfaces.length];
    for (int i = 0; i < interfaces.length; i++) {
      fInterfaces[i] = (HierarchyType) environment.create(interfaces[i]);
    }
  }

  @Override
  public TType getSuperclass() {
    return fSuperclass;
  }

  @Override
  public TType[] getInterfaces() {
    return fInterfaces;
  }

  public IType getJavaElementType() {
    return fJavaElementType;
  }

  public boolean isSubType(HierarchyType other) {
    if (getEnvironment() == other.getEnvironment()) {
      Map<TypeTuple, Boolean> cache = getEnvironment().getSubTypeCache();
      TypeTuple key = new TypeTuple(this, other);
      Boolean value = cache.get(key);
      if (value != null) return value.booleanValue();
      boolean isSub = doIsSubType(other);
      value = Boolean.valueOf(isSub);
      cache.put(key, value);
      return isSub;
    }
    return doIsSubType(other);
  }

  private boolean doIsSubType(HierarchyType other) {
    if (fSuperclass != null
        && (other.isTypeEquivalentTo(fSuperclass) || fSuperclass.doIsSubType(other))) return true;
    for (int i = 0; i < fInterfaces.length; i++) {
      if (other.isTypeEquivalentTo(fInterfaces[i]) || fInterfaces[i].doIsSubType(other))
        return true;
    }
    return false;
  }

  protected boolean canAssignToStandardType(StandardType target) {
    if (target.isJavaLangObject()) return true;
    return isSubType(target);
  }
}

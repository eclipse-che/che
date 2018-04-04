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

/**
 * A ParameterizedTypeVariable2 is a ConstraintVariable which stands for a unique parameterization
 * of a generic type (without an updatable source location)
 */
public final class ParameterizedTypeVariable2 extends ConstraintVariable2 {

  public ParameterizedTypeVariable2(TType type) {
    super(type);
    Assert.isTrue(!type.isWildcardType());
    Assert.isTrue(!type.isTypeVariable());
  }

  // hashCode() and equals(..) not necessary (unique per construction)

  @Override
  public String toString() {
    return getType().getName();
  }
}

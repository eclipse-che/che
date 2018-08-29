/**
 * ***************************************************************************** Copyright (c) 2009,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Mateusz Wenus <mateusz.wenus@gmail.com> - [override method] generate in
 * declaration order [code generation] - https://bugs.eclipse.org/bugs/show_bug.cgi?id=140971 IBM
 * Corporation - bug fixes
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.util.Comparator;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * A comparator which sorts methods (IMethodBinding) of a type passed as constructor argument,
 * according to their order in source files. More formally, if <code>m1</code> and <code>m2</code>
 * are methods of type <code>T</code> then according to this comparator <code>m1</code> is less than
 * <code>m2</code> iff one of following holds:
 *
 * <ul>
 *   <li><code>m1</code> and <code>m2</code> are defined in the same type (<code>T</code> or any
 *       supertype of <code>T</code>), that type has a source attachment and <code>m1</code> appears
 *       before <code>m2</code> in source of that type
 *   <li><code>m1</code> and <code>m2</code> are defined in the same type (<code>T</code> or any
 *       supertype of <code>T</code>), that type doesn't have a source attachment and name of <code>
 *       m1</code> alphabetically precedes name of <code>m2</code>
 *   <li><code>m2</code> is defined in <code>T</code> and <code>m1</code> is defined in any
 *       supertype of <code>T</code>
 *   <li><code>m2</code> is defined in a superclass of <code>T</code> and <code>m1</code> is defined
 *       in a superinterface of <code>T</code>
 *   <li><code>m1</code> and <code>m2</code> are defined in different superclasses of <code>T</code>
 *       and a class which defines <code>m1</code> extends class which defines <code>m2</code>
 *   <li><code>m1</code> and <code>m2</code> are defined in different superinterfaces of <code>T
 *       </code> and an interface which defines <code>m2</code> appears before an interface which
 *       defines <code>m1</code> in <code>implements</code> clause of declaration of type <code>T
 *       </code>
 * </ul>
 */
public class MethodsSourcePositionComparator implements Comparator<IMethodBinding> {

  private final ITypeBinding fTypeBinding;

  public MethodsSourcePositionComparator(ITypeBinding typeBinding) {
    if (typeBinding == null) {
      throw new IllegalArgumentException();
    }
    fTypeBinding = typeBinding;
  }

  public int compare(IMethodBinding firstMethodBinding, IMethodBinding secondMethodBinding) {
    if (firstMethodBinding == null || secondMethodBinding == null) {
      return 0;
    }
    ITypeBinding firstMethodType = firstMethodBinding.getDeclaringClass();
    ITypeBinding secondMethodType = secondMethodBinding.getDeclaringClass();

    if (firstMethodType.equals(secondMethodType)) {
      return compareInTheSameType(firstMethodBinding, secondMethodBinding);
    }

    if (firstMethodType.equals(fTypeBinding)) {
      return 1;
    }
    if (secondMethodType.equals(fTypeBinding)) {
      return -1;
    }

    ITypeBinding type = fTypeBinding;
    int count = 0, firstCount = -1, secondCount = -1;
    while ((type = type.getSuperclass()) != null) {
      if (firstMethodType.equals(type)) {
        firstCount = count;
      }
      if (secondMethodType.equals(type)) {
        secondCount = count;
      }
      count++;
    }
    if (firstCount != -1 && secondCount != -1) {
      return (firstCount - secondCount);
    }
    if (firstCount != -1 && secondCount == -1) {
      return 1;
    }
    if (firstCount == -1 && secondCount != -1) {
      return -1;
    }

    ITypeBinding[] interfaces = fTypeBinding.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (firstMethodType.equals(interfaces[i])) {
        return 1;
      }
      if (secondMethodType.equals(interfaces[i])) {
        return -1;
      }
    }
    return 0;
  }

  private int compareInTheSameType(
      IMethodBinding firstMethodBinding, IMethodBinding secondMethodBinding) {
    try {
      IMethod firstMethod = (IMethod) firstMethodBinding.getJavaElement();
      IMethod secondMethod = (IMethod) secondMethodBinding.getJavaElement();
      if (firstMethod == null || secondMethod == null) {
        return 0;
      }
      ISourceRange firstSourceRange = firstMethod.getSourceRange();
      ISourceRange secondSourceRange = secondMethod.getSourceRange();

      if (!SourceRange.isAvailable(firstSourceRange)
          || !SourceRange.isAvailable(secondSourceRange)) {
        return firstMethod.getElementName().compareTo(secondMethod.getElementName());
      } else {
        return firstSourceRange.getOffset() - secondSourceRange.getOffset();
      }
    } catch (JavaModelException e) {
      return 0;
    }
  }
}

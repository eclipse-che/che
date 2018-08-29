/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Dmitry Stalnov
 * (dstalnov@fusionone.com) - contributed fix for bug "inline method - doesn't handle implicit cast"
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=24941).
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TypeEnvironment;

/**
 * Helper class to check if objects are assignable to each other. Methods with multiple arguments
 * also work across bindings environments.
 */
public class TypeRules {

  /**
   * Tests if two types are assign compatible. Void types are never compatible.
   *
   * @param typeToAssign The binding of the type to assign
   * @param definedType The type of the object that is assigned
   * @return <code>true</code> iff definedType = typeToAssign is a valid assignment
   */
  public static boolean canAssign(ITypeBinding typeToAssign, ITypeBinding definedType) {
    TypeEnvironment typeEnvironment = new TypeEnvironment(false, true);
    TType defined = typeEnvironment.create(definedType);
    TType toAssign = typeEnvironment.create(typeToAssign);
    return toAssign.canAssignTo(defined);
  }

  public static boolean isArrayCompatible(ITypeBinding definedType) {
    if (definedType.isTopLevel()) {
      if (definedType.isClass()) {
        return "Object".equals(definedType.getName())
            && "java.lang".equals(definedType.getPackage().getName()); // $NON-NLS-1$//$NON-NLS-2$
      } else {
        String qualifiedName = definedType.getQualifiedName();
        return "java.io.Serializable".equals(qualifiedName)
            || "java.lang.Cloneable".equals(qualifiedName); // $NON-NLS-1$ //$NON-NLS-2$
      }
    }
    return false;
  }

  public static boolean isJavaLangObject(ITypeBinding definedType) {
    return definedType.isTopLevel()
        && definedType.isClass()
        && "Object".equals(definedType.getName())
        && "java.lang".equals(definedType.getPackage().getName()); // $NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Tests if a two types are cast compatible
   *
   * @param castType The binding of the type to cast to
   * @param bindingToCast The binding ef the expression to cast.
   * @return boolean Returns true if (castType) bindingToCast is a valid cast expression (can be
   *     unnecessary, but not invalid).
   */
  public static boolean canCast(ITypeBinding castType, ITypeBinding bindingToCast) {
    // see bug 80715

    String voidName = PrimitiveType.VOID.toString();

    if (castType.isAnonymous() || castType.isNullType() || voidName.equals(castType.getName())) {
      throw new IllegalArgumentException();
    }

    if (castType == bindingToCast) {
      return true;
    }

    if (voidName.equals(bindingToCast.getName())) {
      return false;
    }

    if (bindingToCast.isArray()) {
      if (!castType.isArray()) {
        return isArrayCompatible(
            castType); // can not cast an arraytype to a non array type (except to Object,
        // Serializable...)
      }

      int toCastDim = bindingToCast.getDimensions();
      int castTypeDim = castType.getDimensions();
      if (toCastDim == castTypeDim) {
        bindingToCast = bindingToCast.getElementType();
        castType = castType.getElementType();
        if (castType.isPrimitive() && castType != bindingToCast) {
          return false; // can't assign arrays of different primitive types to each other
        }
        // fall through
      } else if (toCastDim < castTypeDim) {
        return isArrayCompatible(bindingToCast.getElementType());
      } else {
        return isArrayCompatible(castType.getElementType());
      }
    }
    if (castType.isPrimitive()) {
      if (!bindingToCast.isPrimitive()) {
        return false;
      }
      String boolName = PrimitiveType.BOOLEAN.toString();
      return (!boolName.equals(castType.getName()) && !boolName.equals(bindingToCast.getName()));
    } else {
      if (bindingToCast.isPrimitive()) {
        return false;
      }
      if (castType.isArray()) {
        return isArrayCompatible(bindingToCast);
      }
      if (castType.isInterface()) {
        if ((bindingToCast.getModifiers() & Modifier.FINAL) != 0) {
          return Bindings.isSuperType(castType, bindingToCast);
        } else {
          return true;
        }
      }
      if (bindingToCast.isInterface()) {
        if ((castType.getModifiers() & Modifier.FINAL) != 0) {
          return Bindings.isSuperType(bindingToCast, castType);
        } else {
          return true;
        }
      }
      if (isJavaLangObject(castType)) {
        return true;
      }

      return Bindings.isSuperType(bindingToCast, castType)
          || Bindings.isSuperType(castType, bindingToCast);
    }
  }
}

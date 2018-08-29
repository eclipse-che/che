/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Dmitry Stalnov
 * (dstalnov@fusionone.com) - contributed fix for bug "inline method - doesn't handle implicit cast"
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=24941). Rabea Gransberger
 * <rgransberger@gmx.de> - [quick fix] Fix several visibility issues -
 * https://bugs.eclipse.org/394692
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

/**
 * JDT-UI-internal helper methods that deal with {@link org.eclipse.jdt.core.dom.IBinding}s:
 *
 * <ul>
 *   <li>additional operations on {@link org.eclipse.jdt.core.dom.IBinding}s and subtypes
 *   <li>finding corresponding elements in the type hierarchy
 *   <li>resolve bindings from a family of {@link org.eclipse.jdt.core.dom.ASTNode} types
 * </ul>
 */
public class Bindings {

  public static final String ARRAY_LENGTH_FIELD_BINDING_STRING =
      "(array type):length"; // $NON-NLS-1$

  private Bindings() {
    // No instance
  }

  /**
   * Checks if the two bindings are equals. Also works across binding environments.
   *
   * @param b1 first binding treated as <code>this</code>. So it must not be <code>null</code>
   * @param b2 the second binding.
   * @return boolean
   */
  public static boolean equals(IBinding b1, IBinding b2) {
    return b1.isEqualTo(b2);
  }

  /**
   * Checks if the declarations of two bindings are equals. Also works across binding environments.
   *
   * @param b1 first binding, must not be <code>null</code>
   * @param b2 second binding, must not be <code>null</code>
   * @return boolean
   */
  public static boolean equalDeclarations(IBinding b1, IBinding b2) {
    if (b1.getKind() != b2.getKind()) return false;
    return getDeclaration(b1).isEqualTo(getDeclaration(b2));
  }

  /**
   * Checks if the two arrays of bindings have the same length and their elements are equal. Uses
   * <code>Bindings.equals(IBinding, IBinding)</code> to compare.
   *
   * @param b1 the first array of bindings. Must not be <code>null</code>.
   * @param b2 the second array of bindings.
   * @return boolean
   */
  public static boolean equals(IBinding[] b1, IBinding[] b2) {
    Assert.isNotNull(b1);
    if (b1 == b2) return true;
    if (b2 == null) return false;
    if (b1.length != b2.length) return false;
    for (int i = 0; i < b1.length; i++) {
      if (!Bindings.equals(b1[i], b2[i])) return false;
    }
    return true;
  }

  public static int hashCode(IBinding binding) {
    Assert.isNotNull(binding);
    String key = binding.getKey();
    if (key == null) return binding.hashCode();
    return key.hashCode();
  }

  /**
   * Note: this method is for debugging and testing purposes only. There are tests whose
   * pre-computed test results rely on the returned String's format.
   *
   * @param binding the binding
   * @return a string representation of given binding
   * @see org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider
   */
  public static String asString(IBinding binding) {
    if (binding instanceof IMethodBinding) return asString((IMethodBinding) binding);
    else if (binding instanceof ITypeBinding) return ((ITypeBinding) binding).getQualifiedName();
    else if (binding instanceof IVariableBinding) return asString((IVariableBinding) binding);
    return binding.toString();
  }

  private static String asString(IVariableBinding variableBinding) {
    if (!variableBinding.isField()) return variableBinding.toString();
    if (variableBinding.getDeclaringClass() == null) {
      Assert.isTrue(variableBinding.getName().equals("length")); // $NON-NLS-1$
      return ARRAY_LENGTH_FIELD_BINDING_STRING;
    }
    StringBuffer result = new StringBuffer();
    result.append(variableBinding.getDeclaringClass().getName());
    result.append(':');
    result.append(variableBinding.getName());
    return result.toString();
  }

  private static String asString(IMethodBinding method) {
    StringBuffer result = new StringBuffer();
    result.append(method.getDeclaringClass().getName());
    result.append(':');
    result.append(method.getName());
    result.append('(');
    ITypeBinding[] parameters = method.getParameterTypes();
    int lastComma = parameters.length - 1;
    for (int i = 0; i < parameters.length; i++) {
      ITypeBinding parameter = parameters[i];
      result.append(parameter.getName());
      if (i < lastComma) result.append(", "); // $NON-NLS-1$
    }
    result.append(')');
    return result.toString();
  }

  public static String getTypeQualifiedName(ITypeBinding type) {
    List<String> result = new ArrayList<String>(5);
    createName(type, false, result);

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < result.size(); i++) {
      if (i > 0) {
        buffer.append('.');
      }
      buffer.append(result.get(i));
    }
    return buffer.toString();
  }

  /**
   * Returns the fully qualified name of the specified type binding.
   *
   * <p>If the binding resolves to a generic type, the fully qualified name of the raw type is
   * returned.
   *
   * @param type the type binding to get its fully qualified name
   * @return the fully qualified name
   */
  public static String getFullyQualifiedName(ITypeBinding type) {
    String name = type.getQualifiedName();
    final int index = name.indexOf('<');
    if (index > 0) name = name.substring(0, index);
    return name;
  }

  public static String getImportName(IBinding binding) {
    ITypeBinding declaring = null;
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return getRawQualifiedName((ITypeBinding) binding);
      case IBinding.PACKAGE:
        return binding.getName() + ".*"; // $NON-NLS-1$
      case IBinding.METHOD:
        declaring = ((IMethodBinding) binding).getDeclaringClass();
        break;
      case IBinding.VARIABLE:
        declaring = ((IVariableBinding) binding).getDeclaringClass();
        if (declaring == null) {
          return binding.getName(); // array.length
        }

        break;
      default:
        return binding.getName();
    }
    return JavaModelUtil.concatenateName(getRawQualifiedName(declaring), binding.getName());
  }

  private static void createName(ITypeBinding type, boolean includePackage, List<String> list) {
    ITypeBinding baseType = type;
    if (type.isArray()) {
      baseType = type.getElementType();
    }
    if (!baseType.isPrimitive() && !baseType.isNullType()) {
      ITypeBinding declaringType = baseType.getDeclaringClass();
      if (declaringType != null) {
        createName(declaringType, includePackage, list);
      } else if (includePackage && !baseType.getPackage().isUnnamed()) {
        String[] components = baseType.getPackage().getNameComponents();
        for (int i = 0; i < components.length; i++) {
          list.add(components[i]);
        }
      }
    }
    if (!baseType.isAnonymous()) {
      list.add(type.getName());
    } else {
      list.add("$local$"); // $NON-NLS-1$
    }
  }

  public static String[] getNameComponents(ITypeBinding type) {
    List<String> result = new ArrayList<String>(5);
    createName(type, false, result);
    return result.toArray(new String[result.size()]);
  }

  public static String[] getAllNameComponents(ITypeBinding type) {
    List<String> result = new ArrayList<String>(5);
    createName(type, true, result);
    return result.toArray(new String[result.size()]);
  }

  public static ITypeBinding getTopLevelType(ITypeBinding type) {
    ITypeBinding parent = type.getDeclaringClass();
    while (parent != null) {
      type = parent;
      parent = type.getDeclaringClass();
    }
    return type;
  }

  /**
   * Checks whether the passed type binding is a runtime exception.
   *
   * @param thrownException the type binding
   * @return <code>true</code> if the passed type binding is a runtime exception; otherwise <code>
   *     false</code> is returned
   */
  public static boolean isRuntimeException(ITypeBinding thrownException) {
    if (thrownException == null || thrownException.isPrimitive() || thrownException.isArray())
      return false;
    return findTypeInHierarchy(thrownException, "java.lang.RuntimeException")
        != null; // $NON-NLS-1$
  }

  /**
   * Finds the field specified by <code>fieldName<code> in
   * the given <code>type</code>. Returns <code>null</code> if no such field exits.
   * @param type the type to search the field in
   * @param fieldName the field name
   * @return the binding representing the field or <code>null</code>
   */
  public static IVariableBinding findFieldInType(ITypeBinding type, String fieldName) {
    if (type.isPrimitive()) return null;
    IVariableBinding[] fields = type.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      IVariableBinding field = fields[i];
      if (field.getName().equals(fieldName)) return field;
    }
    return null;
  }

  /**
   * Finds the field specified by <code>fieldName</code> in the type hierarchy denoted by the given
   * type. Returns <code>null</code> if no such field exists. If the field is defined in more than
   * one super type only the first match is returned. First the super class is examined and then the
   * implemented interfaces.
   *
   * @param type The type to search the field in
   * @param fieldName The name of the field to find
   * @return the variable binding representing the field
   */
  public static IVariableBinding findFieldInHierarchy(ITypeBinding type, String fieldName) {
    IVariableBinding field = findFieldInType(type, fieldName);
    if (field != null) return field;
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      field = findFieldInHierarchy(superClass, fieldName);
      if (field != null) return field;
    }
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      field = findFieldInHierarchy(interfaces[i], fieldName);
      if (field != null) // no private fields in interfaces
      return field;
    }
    return null;
  }

  /**
   * Finds the method specified by <code>methodName<code> and </code>parameters</code> in the given
   * <code>type</code>. Returns <code>null</code> if no such method exits.
   *
   * @param type The type to search the method in
   * @param methodName The name of the method to find
   * @param parameters The parameter types of the method to find. If <code>null</code> is passed,
   *     only the name is matched and parameters are ignored.
   * @return the method binding representing the method
   */
  public static IMethodBinding findMethodInType(
      ITypeBinding type, String methodName, ITypeBinding[] parameters) {
    if (type.isPrimitive()) return null;
    IMethodBinding[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (parameters == null) {
        if (methodName.equals(methods[i].getName())) return methods[i];
      } else {
        if (isEqualMethod(methods[i], methodName, parameters)) return methods[i];
      }
    }
    return null;
  }

  /**
   * Finds the method specified by <code>methodName</code> and </code>parameters</code> in the type
   * hierarchy denoted by the given type. Returns <code>null</code> if no such method exists. If the
   * method is defined in more than one super type only the first match is returned. First the super
   * class is examined and then the implemented interfaces.
   *
   * @param type The type to search the method in
   * @param methodName The name of the method to find
   * @param parameters The parameter types of the method to find. If <code>null</code> is passed,
   *     only the name is matched and parameters are ignored.
   * @return the method binding representing the method
   */
  public static IMethodBinding findMethodInHierarchy(
      ITypeBinding type, String methodName, ITypeBinding[] parameters) {
    IMethodBinding method = findMethodInType(type, methodName, parameters);
    if (method != null) return method;
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      method = findMethodInHierarchy(superClass, methodName, parameters);
      if (method != null) return method;
    }
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      method = findMethodInHierarchy(interfaces[i], methodName, parameters);
      if (method != null) return method;
    }
    return null;
  }

  /**
   * Finds the method specified by <code>methodName<code> and </code>parameters</code> in the given
   * <code>type</code>. Returns <code>null</code> if no such method exits.
   *
   * @param type The type to search the method in
   * @param methodName The name of the method to find
   * @param parameters The parameter types of the method to find. If <code>null</code> is passed,
   *     only the name is matched and parameters are ignored.
   * @return the method binding representing the method
   */
  public static IMethodBinding findMethodInType(
      ITypeBinding type, String methodName, String[] parameters) {
    if (type.isPrimitive()) return null;
    IMethodBinding[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (parameters == null) {
        if (methodName.equals(methods[i].getName())) return methods[i];
      } else {
        if (isEqualMethod(methods[i], methodName, parameters)) return methods[i];
      }
    }
    return null;
  }

  /**
   * Finds the method specified by <code>methodName</code> and </code>parameters</code> in the type
   * hierarchy denoted by the given type. Returns <code>null</code> if no such method exists. If the
   * method is defined in more than one super type only the first match is returned. First the super
   * class is examined and then the implemented interfaces.
   *
   * @param type the type to search the method in
   * @param methodName The name of the method to find
   * @param parameters The parameter types of the method to find. If <code>null</code> is passed,
   *     only the name is matched and parameters are ignored.
   * @return the method binding representing the method
   */
  public static IMethodBinding findMethodInHierarchy(
      ITypeBinding type, String methodName, String[] parameters) {
    IMethodBinding method = findMethodInType(type, methodName, parameters);
    if (method != null) return method;
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      method = findMethodInHierarchy(superClass, methodName, parameters);
      if (method != null) return method;
    }
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      method = findMethodInHierarchy(interfaces[i], methodName, parameters);
      if (method != null) return method;
    }
    return null;
  }

  /**
   * Finds the method in the given <code>type</code> that is overridden by the specified <code>method<code>.
   * Returns <code>null</code> if no such method exits.
   * @param type The type to search the method in
   * @param method The specified method that would override the result
   * @return the method binding of the method that is overridden by the specified <code>method<code>, or <code>null</code>
   */
  public static IMethodBinding findOverriddenMethodInType(
      ITypeBinding type, IMethodBinding method) {
    IMethodBinding[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (isSubsignature(method, methods[i])) return methods[i];
    }
    return null;
  }

  /**
   * Finds a method in the hierarchy of <code>type</code> that is overridden by </code>binding
   * </code>. Returns <code>null</code> if no such method exists. If the method is defined in more
   * than one super type only the first match is returned. First the super class is examined and
   * then the implemented interfaces.
   *
   * @param type The type to search the method in
   * @param binding The method that overrides
   * @return the method binding overridden the method
   */
  public static IMethodBinding findOverriddenMethodInHierarchy(
      ITypeBinding type, IMethodBinding binding) {
    IMethodBinding method = findOverriddenMethodInType(type, binding);
    if (method != null) return method;
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      method = findOverriddenMethodInHierarchy(superClass, binding);
      if (method != null) return method;
    }
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      method = findOverriddenMethodInHierarchy(interfaces[i], binding);
      if (method != null) return method;
    }
    return null;
  }

  /**
   * Finds the method that is overridden by the given method. The search is bottom-up, so this
   * returns the nearest defining/declaring method.
   *
   * @param overriding overriding method
   * @param testVisibility If true the result is tested on visibility. Null is returned if the
   *     method is not visible.
   * @return the method binding representing the method
   */
  public static IMethodBinding findOverriddenMethod(
      IMethodBinding overriding, boolean testVisibility) {
    List<IMethodBinding> findOverriddenMethods =
        findOverriddenMethods(overriding, testVisibility, true);
    if (findOverriddenMethods.isEmpty()) {
      return null;
    }
    return findOverriddenMethods.get(0);
  }

  /**
   * Finds all methods that are overridden by the given method. The search is bottom-up, so this
   * returns the nearest defining/declaring methods in order.
   *
   * @param overriding overriding method
   * @param testVisibility if <code>true</code> the result is tested on visibility
   * @param firstOnly if <code>true</code> this method will return when the first overridden method
   *     is found
   * @return the method bindings representing the overridden method or an empty list if no result is
   *     found
   * @since 3.9
   */
  public static List<IMethodBinding> findOverriddenMethods(
      IMethodBinding overriding, boolean testVisibility, boolean firstOnly) {
    List<IMethodBinding> methodList = new ArrayList<IMethodBinding>();

    int modifiers = overriding.getModifiers();
    if (Modifier.isPrivate(modifiers)
        || Modifier.isStatic(modifiers)
        || overriding.isConstructor()) {
      return methodList;
    }

    ITypeBinding type = overriding.getDeclaringClass();
    if (type.getSuperclass() != null) {
      IMethodBinding res = findOverriddenMethodInHierarchy(type.getSuperclass(), overriding);
      if (res != null && !Modifier.isPrivate(res.getModifiers())) {
        if (!testVisibility
            || isVisibleInHierarchy(res, overriding.getDeclaringClass().getPackage())) {
          methodList.add(res);
          if (firstOnly) {
            return methodList;
          }
        }
      }
    }
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      IMethodBinding res = findOverriddenMethodInHierarchy(interfaces[i], overriding);
      if (res != null) {
        methodList.add(res); // methods from interfaces are always public and therefore visible
        if (firstOnly) {
          return methodList;
        }
      }
    }
    return methodList;
  }

  public static boolean isVisibleInHierarchy(IMethodBinding member, IPackageBinding pack) {
    int otherflags = member.getModifiers();
    ITypeBinding declaringType = member.getDeclaringClass();
    if (Modifier.isPublic(otherflags)
        || Modifier.isProtected(otherflags)
        || (declaringType != null && declaringType.isInterface())) {
      return true;
    } else if (Modifier.isPrivate(otherflags)) {
      return false;
    }
    return declaringType != null && pack == declaringType.getPackage();
  }

  /**
   * Returns all super types (classes and interfaces) for the given type.
   *
   * @param type The type to get the supertypes of.
   * @return all super types (excluding <code>type</code>)
   */
  public static ITypeBinding[] getAllSuperTypes(ITypeBinding type) {
    Set<ITypeBinding> result = new HashSet<ITypeBinding>();
    collectSuperTypes(type, result);
    result.remove(type);
    return result.toArray(new ITypeBinding[result.size()]);
  }

  private static void collectSuperTypes(ITypeBinding curr, Set<ITypeBinding> collection) {
    if (collection.add(curr)) {
      ITypeBinding[] interfaces = curr.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        collectSuperTypes(interfaces[i], collection);
      }
      ITypeBinding superClass = curr.getSuperclass();
      if (superClass != null) {
        collectSuperTypes(superClass, collection);
      }
    }
  }

  /**
   * Method to visit a type hierarchy defined by a given type. The given type itself is not visited.
   *
   * @param type the type whose hierarchy is to be visited
   * @param visitor the visitor
   * @return <code>true</code> if all types were visited, or <code>false</code> if the visiting got
   *     aborted because the <code>visit</code> method returned <code>false</code> for a type
   */
  public static boolean visitHierarchy(ITypeBinding type, TypeBindingVisitor visitor) {
    boolean result = visitSuperclasses(type, visitor);
    if (result) {
      result = visitInterfaces(type, visitor);
    }
    return result;
  }

  /**
   * Method to visit an interface hierarchy defined by a given type. The given type itself is not
   * visited.
   *
   * @param type the type whose interface hierarchy is to be visited
   * @param visitor the visitor
   * @return <code>true</code> if all types were visited, or <code>false</code> if the visiting got
   *     aborted because the <code>visit</code> method returned <code>false</code> for a type
   */
  public static boolean visitInterfaces(ITypeBinding type, TypeBindingVisitor visitor) {
    return visitInterfaces(type, visitor, new HashSet<ITypeBinding>());
  }

  private static boolean visitInterfaces(
      ITypeBinding type, TypeBindingVisitor visitor, HashSet<ITypeBinding> visited) {
    boolean unvisited = visited.add(type);
    if (!unvisited) return true;
    ITypeBinding[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      if (!visitor.visit(interfaces[i])) {
        return false;
      }
      if (!visitInterfaces(interfaces[i], visitor, visited)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Method to visit a super class hierarchy defined by a given type. The given type itself is not
   * visited.
   *
   * @param type the type whose super class hierarchy is to be visited
   * @param visitor the visitor
   * @return <code>true</code> if all types were visited, or <code>false</code> if the visiting got
   *     aborted because the <code>visit</code> method returned <code>false</code> for a type
   */
  public static boolean visitSuperclasses(ITypeBinding type, TypeBindingVisitor visitor) {
    while ((type = type.getSuperclass()) != null) {
      if (!visitor.visit(type)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether the two methods are erasure-equivalent.
   *
   * @param method the first method
   * @param methodName the name of the second method
   * @param parameters the parameters of the second method
   * @return return <code>true</code> if the two bindings are equal
   * @deprecated use {@link #isSubsignature(IMethodBinding, IMethodBinding)}
   */
  // TODO: rename to isErasureEquivalentMethod and change to two IMethodBinding parameters
  public static boolean isEqualMethod(
      IMethodBinding method, String methodName, ITypeBinding[] parameters) {
    if (!method.getName().equals(methodName)) return false;

    ITypeBinding[] methodParameters = method.getParameterTypes();
    if (methodParameters.length != parameters.length) return false;
    for (int i = 0; i < parameters.length; i++) {
      if (!equals(methodParameters[i].getErasure(), parameters[i].getErasure())) return false;
    }
    // Can't use this fix, since some clients assume that this method tests erasure equivalence:
    //		if (method.getTypeParameters().length == 0) {
    //			//a method without type parameters cannot be overridden by one that declares type
    // parameters -> can be exact here
    //			for (int i= 0; i < parameters.length; i++) {
    //				if ( ! (equals(methodParameters[i], parameters[i])
    //						|| equals(methodParameters[i].getErasure(), parameters[i]))) // subsignature
    //					return false;
    //			}
    //		} else {
    //			//this will find all overridden methods, but may generate false positives in some cases:
    //			for (int i= 0; i < parameters.length; i++) {
    //				if (!equals(methodParameters[i].getErasure(), parameters[i].getErasure()))
    //					return false;
    //			}
    //		}
    return true;
  }

  /**
   * @param overriding overriding method (m1)
   * @param overridden overridden method (m2)
   * @return <code>true</code> iff the method <code>m1</code> is a subsignature of the method <code>
   *     m2</code>. This is one of the requirements for m1 to override m2. Accessibility and return
   *     types are not taken into account. Note that subsignature is <em>not</em> symmetric!
   */
  public static boolean isSubsignature(IMethodBinding overriding, IMethodBinding overridden) {
    // TODO: use IMethodBinding#isSubsignature(..) once it is tested and fixed (only erasure of m1's
    // parameter types, considering type variable counts, doing type variable substitution
    if (!overriding.getName().equals(overridden.getName())) return false;

    ITypeBinding[] m1Params = overriding.getParameterTypes();
    ITypeBinding[] m2Params = overridden.getParameterTypes();
    if (m1Params.length != m2Params.length) return false;

    ITypeBinding[] m1TypeParams = overriding.getTypeParameters();
    ITypeBinding[] m2TypeParams = overridden.getTypeParameters();
    if (m1TypeParams.length != m2TypeParams.length
        && m1TypeParams.length != 0) // non-generic m1 can override a generic m2
    return false;

    // m1TypeParameters.length == (m2TypeParameters.length || 0)
    if (m2TypeParams.length != 0) {
      // Note: this branch does not 100% adhere to the spec and may report some false positives.
      // Full compliance would require major duplication of compiler code.

      // Compare type parameter bounds:
      for (int i = 0; i < m1TypeParams.length; i++) {
        // loop over m1TypeParams, which is either empty, or equally long as m2TypeParams
        Set<ITypeBinding> m1Bounds = getTypeBoundsForSubsignature(m1TypeParams[i]);
        Set<ITypeBinding> m2Bounds = getTypeBoundsForSubsignature(m2TypeParams[i]);
        if (!m1Bounds.equals(m2Bounds)) return false;
      }
      // Compare parameter types:
      if (equals(m2Params, m1Params)) return true;
      for (int i = 0; i < m1Params.length; i++) {
        ITypeBinding m1Param = m1Params[i];
        ITypeBinding m2Param = m2Params[i];
        if (containsTypeVariables(m1Param) || m1Param.isRawType())
          m1Param = m1Param.getErasure(); // try to achieve effect of "rename type variables"
        if (!(equals(m1Param, m2Param) || equals(m1Param, m2Param.getErasure()))) return false;
      }
      return true;

    } else {
      // m1TypeParams.length == m2TypeParams.length == 0
      if (equals(m1Params, m2Params)) return true;
      for (int i = 0; i < m1Params.length; i++) {
        ITypeBinding m1Param = m1Params[i];
        ITypeBinding m2Param = m2Params[i];
        if (m1Param.isRawType()) m1Param = m1Param.getTypeDeclaration();
        if (!(equals(m1Param, m2Param) || equals(m1Param, m2Param.getErasure()))) return false;
      }
      return true;
    }
  }

  static boolean containsTypeVariables(ITypeBinding type) {
    if (type.isTypeVariable()) return true;
    if (type.isArray()) return containsTypeVariables(type.getElementType());
    if (type.isCapture()) return containsTypeVariables(type.getWildcard());
    if (type.isParameterizedType()) return containsTypeVariables(type.getTypeArguments());
    if (type.isWildcardType() && type.getBound() != null)
      return containsTypeVariables(type.getBound());
    return false;
  }

  private static boolean containsTypeVariables(ITypeBinding[] types) {
    for (int i = 0; i < types.length; i++) if (containsTypeVariables(types[i])) return true;
    return false;
  }

  private static Set<ITypeBinding> getTypeBoundsForSubsignature(ITypeBinding typeParameter) {
    ITypeBinding[] typeBounds = typeParameter.getTypeBounds();
    int count = typeBounds.length;
    if (count == 0) return Collections.emptySet();

    Set<ITypeBinding> result = new HashSet<ITypeBinding>(typeBounds.length);
    for (int i = 0; i < typeBounds.length; i++) {
      ITypeBinding bound = typeBounds[i];
      if ("java.lang.Object".equals(typeBounds[0].getQualifiedName())) // $NON-NLS-1$
      continue;
      else if (containsTypeVariables(bound))
        result.add(bound.getErasure()); // try to achieve effect of "rename type variables"
      else if (bound.isRawType()) result.add(bound.getTypeDeclaration());
      else result.add(bound);
    }
    return result;
  }

  /**
   * Checks whether a method with the given name and parameter types is a subsignature of the given
   * method binding.
   *
   * @param method a method
   * @param methodName method name to match
   * @param parameters the parameter types of the method to find. If <code>null</code> is passed,
   *     only the name is matched and parameters are ignored.
   * @return <code>true</code> iff the method m1 (with name <code>methodName</code> and method
   *     parameters <code>parameters</code>) is a subsignature of the method <code>m2</code>.
   *     Accessibility and return types are not taken into account.
   */
  public static boolean isEqualMethod(
      IMethodBinding method, String methodName, String[] parameters) {
    if (!method.getName().equals(methodName)) return false;

    ITypeBinding[] methodParameters = method.getParameterTypes();
    if (methodParameters.length != parameters.length) return false;
    String first, second;
    int index;
    for (int i = 0; i < parameters.length; i++) {
      first = parameters[i];
      index = first.indexOf('<');
      if (index > 0) {
        int lastIndex = first.lastIndexOf('>');
        StringBuffer buf = new StringBuffer();
        buf.append(first.substring(0, index));
        if (lastIndex < first.length() - 1)
          buf.append(first.substring(lastIndex + 1, first.length()));
        first = buf.toString();
      }
      second = methodParameters[i].getQualifiedName();
      if (!first.equals(second)) {
        second = methodParameters[i].getErasure().getQualifiedName();
        if (!first.equals(second)) return false;
      }
    }
    return true;
  }

  /**
   * Finds a type binding for a given fully qualified type in the hierarchy of a type. Returns
   * <code>null</code> if no type binding is found.
   *
   * @param hierarchyType the binding representing the hierarchy
   * @param fullyQualifiedTypeName the fully qualified name to search for
   * @return the type binding
   */
  public static ITypeBinding findTypeInHierarchy(
      ITypeBinding hierarchyType, String fullyQualifiedTypeName) {
    if (hierarchyType.isArray() || hierarchyType.isPrimitive()) {
      return null;
    }
    if (fullyQualifiedTypeName.equals(hierarchyType.getTypeDeclaration().getQualifiedName())) {
      return hierarchyType;
    }
    ITypeBinding superClass = hierarchyType.getSuperclass();
    if (superClass != null) {
      ITypeBinding res = findTypeInHierarchy(superClass, fullyQualifiedTypeName);
      if (res != null) {
        return res;
      }
    }
    ITypeBinding[] superInterfaces = hierarchyType.getInterfaces();
    for (int i = 0; i < superInterfaces.length; i++) {
      ITypeBinding res = findTypeInHierarchy(superInterfaces[i], fullyQualifiedTypeName);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  /**
   * Returns the binding of the variable written in an Assignment.
   *
   * @param assignment The assignment
   * @return The binding or <code>null</code> if no bindings are available.
   */
  public static IVariableBinding getAssignedVariable(Assignment assignment) {
    Expression leftHand = assignment.getLeftHandSide();
    switch (leftHand.getNodeType()) {
      case ASTNode.SIMPLE_NAME:
        return (IVariableBinding) ((SimpleName) leftHand).resolveBinding();
      case ASTNode.QUALIFIED_NAME:
        return (IVariableBinding) ((QualifiedName) leftHand).getName().resolveBinding();
      case ASTNode.FIELD_ACCESS:
        return ((FieldAccess) leftHand).resolveFieldBinding();
      case ASTNode.SUPER_FIELD_ACCESS:
        return ((SuperFieldAccess) leftHand).resolveFieldBinding();
      default:
        return null;
    }
  }

  /**
   * Returns <code>true</code> if the given type is a super type of a candidate. <code>true</code>
   * is returned if the two type bindings are identical.
   *
   * <p><b>Warning:</b> With the addition of generics, this method is valid in less cases than
   * before. Consider using {@link TypeRules#canAssign(ITypeBinding, ITypeBinding)} if you're
   * dealing with types of variables. The classical notion of supertypes only makes sense if you
   * really need to walk the type hierarchy but don't need to play the assignment rules.
   *
   * @param possibleSuperType the type to inspect
   * @param type the type whose super types are looked at
   * @return <code>true</code> iff <code>possibleSuperType</code> is a super type of <code>type
   *     </code> or is equal to it
   */
  public static boolean isSuperType(ITypeBinding possibleSuperType, ITypeBinding type) {
    return isSuperType(possibleSuperType, type, true);
  }

  /**
   * Returns <code>true</code> if the given type is a super type of a candidate. <code>true</code>
   * is returned if the two type bindings are identical (TODO)
   *
   * @param possibleSuperType the type to inspect
   * @param type the type whose super types are looked at
   * @param considerTypeArguments if <code>true</code>, consider type arguments of <code>type</code>
   * @return <code>true</code> iff <code>possibleSuperType</code> is a super type of <code>type
   *     </code> or is equal to it
   */
  public static boolean isSuperType(
      ITypeBinding possibleSuperType, ITypeBinding type, boolean considerTypeArguments) {
    if (type.isArray() || type.isPrimitive()) {
      return false;
    }
    if (!considerTypeArguments) {
      type = type.getTypeDeclaration();
    }
    if (Bindings.equals(type, possibleSuperType)) {
      return true;
    }
    ITypeBinding superClass = type.getSuperclass();
    if (superClass != null) {
      if (isSuperType(possibleSuperType, superClass, considerTypeArguments)) {
        return true;
      }
    }

    if (possibleSuperType.isInterface()) {
      ITypeBinding[] superInterfaces = type.getInterfaces();
      for (int i = 0; i < superInterfaces.length; i++) {
        if (isSuperType(possibleSuperType, superInterfaces[i], considerTypeArguments)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Finds the compilation unit where the type of the given <code>ITypeBinding</code> is defined,
   * using the class path defined by the given Java project. Returns <code>null</code> if no
   * compilation unit is found (e.g. type binding is from a binary type)
   *
   * @param typeBinding the type binding to search for
   * @param project the project used as a scope
   * @return the compilation unit containing the type
   * @throws JavaModelException if an errors occurs in the Java model
   */
  public static ICompilationUnit findCompilationUnit(ITypeBinding typeBinding, IJavaProject project)
      throws JavaModelException {
    IJavaElement type = typeBinding.getJavaElement();
    if (type instanceof IType) return ((IType) type).getCompilationUnit();
    else return null;
  }

  /**
   * Finds a method for the given <code>IMethodBinding</code>. Returns <code>null</code> if the type
   * doesn't contain a corresponding method.
   *
   * @param method the method to find
   * @param type the type to look in
   * @return the corresponding IMethod or <code>null</code>
   * @throws JavaModelException if an error occurs in the Java model
   * @deprecated Use {@link #findMethodInHierarchy(ITypeBinding, String, String[])} or {@link
   *     JavaModelUtil}
   */
  public static IMethod findMethod(IMethodBinding method, IType type) throws JavaModelException {
    method = method.getMethodDeclaration();

    IMethod[] candidates = type.getMethods();
    for (int i = 0; i < candidates.length; i++) {
      IMethod candidate = candidates[i];
      if (candidate.getElementName().equals(method.getName())
          && sameParameters(method, candidate)) {
        return candidate;
      }
    }
    return null;
  }

  // ---- Helper methods to convert a method ---------------------------------------------

  private static boolean sameParameters(IMethodBinding method, IMethod candidate)
      throws JavaModelException {
    ITypeBinding[] methodParamters = method.getParameterTypes();
    String[] candidateParameters = candidate.getParameterTypes();
    if (methodParamters.length != candidateParameters.length) return false;
    IType scope = candidate.getDeclaringType();
    for (int i = 0; i < methodParamters.length; i++) {
      ITypeBinding methodParameter = methodParamters[i];
      String candidateParameter = candidateParameters[i];
      if (!sameParameter(methodParameter, candidateParameter, scope)) return false;
    }
    return true;
  }

  private static boolean sameParameter(ITypeBinding type, String candidate, IType scope)
      throws JavaModelException {
    if (type.getDimensions() != Signature.getArrayCount(candidate)) return false;

    // Normalizes types
    if (type.isArray()) type = type.getElementType();
    candidate = Signature.getElementType(candidate);

    if ((Signature.getTypeSignatureKind(candidate) == Signature.BASE_TYPE_SIGNATURE)
        != type.isPrimitive()) {
      return false;
    }

    if (type.isPrimitive() || type.isTypeVariable()) {
      return type.getName().equals(Signature.toString(candidate));
    } else {
      // normalize (quick hack until binding.getJavaElement works)
      candidate = Signature.getTypeErasure(candidate);
      type = type.getErasure();

      if (candidate.charAt(Signature.getArrayCount(candidate)) == Signature.C_RESOLVED) {
        return Signature.toString(candidate).equals(Bindings.getFullyQualifiedName(type));
      } else {
        String[][] qualifiedCandidates = scope.resolveType(Signature.toString(candidate));
        if (qualifiedCandidates == null || qualifiedCandidates.length == 0) return false;
        String packageName =
            type.getPackage().isUnnamed() ? "" : type.getPackage().getName(); // $NON-NLS-1$
        String typeName = getTypeQualifiedName(type);
        for (int i = 0; i < qualifiedCandidates.length; i++) {
          String[] qualifiedCandidate = qualifiedCandidates[i];
          if (qualifiedCandidate[0].equals(packageName) && qualifiedCandidate[1].equals(typeName))
            return true;
        }
      }
    }
    return false;
  }

  /**
   * Normalizes a type binding received from an expression to a type binding that can be used inside
   * a declaration signature, but <em>not</em> as type of a declaration (use {@link
   * #normalizeForDeclarationUse(ITypeBinding, AST)} for that).
   *
   * <p>Anonymous types are normalized to the super class or interface. For null or void bindings,
   * <code>null</code> is returned.
   *
   * @param binding the binding to normalize
   * @return the normalized binding, can be <code>null</code>
   * @see #normalizeForDeclarationUse(ITypeBinding, AST)
   */
  public static ITypeBinding normalizeTypeBinding(ITypeBinding binding) {
    if (binding != null && !binding.isNullType() && !isVoidType(binding)) {
      if (binding.isAnonymous()) {
        ITypeBinding[] baseBindings = binding.getInterfaces();
        if (baseBindings.length > 0) {
          return baseBindings[0];
        }
        return binding.getSuperclass();
      }
      if (binding.isCapture()) {
        return binding.getWildcard();
      }
      return binding;
    }
    return null;
  }

  public static boolean isVoidType(ITypeBinding binding) {
    return "void".equals(binding.getName()); // $NON-NLS-1$
  }

  /**
   * Normalizes the binding so that it can be used as a type inside a declaration (e.g. variable
   * declaration, method return type, parameter type, ...). For null bindings, java.lang.Object is
   * returned. For void bindings, <code>null</code> is returned.
   *
   * @param binding binding to normalize
   * @param ast current AST
   * @return the normalized type to be used in declarations, or <code>null</code>
   */
  public static ITypeBinding normalizeForDeclarationUse(ITypeBinding binding, AST ast) {
    if (binding.isNullType()) return ast.resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
    if (binding.isPrimitive()) return binding;
    binding = normalizeTypeBinding(binding);
    if (binding == null || !binding.isWildcardType()) return binding;
    ITypeBinding bound = binding.getBound();
    if (bound == null || !binding.isUpperbound()) {
      ITypeBinding[] typeBounds = binding.getTypeBounds();
      if (typeBounds.length > 0) {
        return typeBounds[0];
      } else {
        return binding.getErasure();
      }
    } else {
      return bound;
    }
  }

  /**
   * Returns the type binding of the node's enclosing type declaration.
   *
   * @param node an AST node
   * @return the type binding of the node's parent type declaration, or <code>null</code>
   */
  public static ITypeBinding getBindingOfParentType(ASTNode node) {
    while (node != null) {
      if (node instanceof AbstractTypeDeclaration) {
        return ((AbstractTypeDeclaration) node).resolveBinding();
      } else if (node instanceof AnonymousClassDeclaration) {
        return ((AnonymousClassDeclaration) node).resolveBinding();
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the type binding of the node's type context or null if the node is inside an
   * annotation, type parameter, super type declaration, or Javadoc of a top level type. The result
   * of this method is equal to the result of {@link #getBindingOfParentType(ASTNode)} for nodes in
   * the type's body.
   *
   * @param node an AST node
   * @return the type binding of the node's parent type context, or <code>null</code>
   */
  public static ITypeBinding getBindingOfParentTypeContext(ASTNode node) {
    StructuralPropertyDescriptor lastLocation = null;

    while (node != null) {
      if (node instanceof AbstractTypeDeclaration) {
        AbstractTypeDeclaration decl = (AbstractTypeDeclaration) node;
        if (lastLocation == decl.getBodyDeclarationsProperty()
            || lastLocation == decl.getJavadocProperty()) {
          return decl.resolveBinding();
        } else if (decl instanceof EnumDeclaration
            && lastLocation == EnumDeclaration.ENUM_CONSTANTS_PROPERTY) {
          return decl.resolveBinding();
        }
      } else if (node instanceof AnonymousClassDeclaration) {
        return ((AnonymousClassDeclaration) node).resolveBinding();
      }
      lastLocation = node.getLocationInParent();
      node = node.getParent();
    }
    return null;
  }

  public static String getRawName(ITypeBinding binding) {
    String name = binding.getName();
    if (binding.isParameterizedType() || binding.isGenericType()) {
      int idx = name.indexOf('<');
      if (idx != -1) {
        return name.substring(0, idx);
      }
    }
    return name;
  }

  public static String getRawQualifiedName(ITypeBinding binding) {
    final String EMPTY = ""; // $NON-NLS-1$

    if (binding.isAnonymous() || binding.isLocal()) {
      return EMPTY;
    }

    if (binding.isPrimitive() || binding.isNullType() || binding.isTypeVariable()) {
      return binding.getName();
    }

    if (binding.isArray()) {
      String elementTypeQualifiedName = getRawQualifiedName(binding.getElementType());
      if (elementTypeQualifiedName.length() != 0) {
        StringBuffer stringBuffer = new StringBuffer(elementTypeQualifiedName);
        stringBuffer.append('[').append(']');
        return stringBuffer.toString();
      } else {
        return EMPTY;
      }
    }
    if (binding.isMember()) {
      String outerName = getRawQualifiedName(binding.getDeclaringClass());
      if (outerName.length() > 0) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(outerName);
        buffer.append('.');
        buffer.append(getRawName(binding));
        return buffer.toString();
      } else {
        return EMPTY;
      }

    } else if (binding.isTopLevel()) {
      IPackageBinding packageBinding = binding.getPackage();
      StringBuffer buffer = new StringBuffer();
      if (packageBinding != null && packageBinding.getName().length() > 0) {
        buffer.append(packageBinding.getName()).append('.');
      }
      buffer.append(getRawName(binding));
      return buffer.toString();
    }
    return EMPTY;
  }

  /**
   * Tests if the given node is a declaration, not a instance of a generic type, method or field.
   * Declarations can be found in AST with CompilationUnit.findDeclaringNode
   *
   * @param binding binding to test
   * @return returns <code>true</code> if the binding is a declaration binding
   */
  public static boolean isDeclarationBinding(IBinding binding) {
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return ((ITypeBinding) binding).getTypeDeclaration() == binding;
      case IBinding.VARIABLE:
        return ((IVariableBinding) binding).getVariableDeclaration() == binding;
      case IBinding.METHOD:
        return ((IMethodBinding) binding).getMethodDeclaration() == binding;
    }
    return true;
  }

  public static IBinding getDeclaration(IBinding binding) {
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return ((ITypeBinding) binding).getTypeDeclaration();
      case IBinding.VARIABLE:
        return ((IVariableBinding) binding).getVariableDeclaration();
      case IBinding.METHOD:
        return ((IMethodBinding) binding).getMethodDeclaration();
    }
    return binding;
  }

  /**
   * @param candidates the candidates
   * @param overridable the overriding method
   * @return returns <code>true></code> if the overriding method overrides a candidate
   * @deprecated Need to review: Use {@link #isSubsignature(IMethodBinding, IMethodBinding)} if the
   *     two bindings are in the same hierarchy (directly overrides each other), or {@link
   *     #findMethodInHierarchy(ITypeBinding, String, ITypeBinding[])} else.
   */
  public static boolean containsSignatureEquivalentConstructor(
      IMethodBinding[] candidates, IMethodBinding overridable) {
    for (int index = 0; index < candidates.length; index++) {
      if (isSignatureEquivalentConstructor(candidates[index], overridable)) return true;
    }
    return false;
  }

  private static boolean isSignatureEquivalentConstructor(
      IMethodBinding overridden, IMethodBinding overridable) {

    if (!overridden.isConstructor() || !overridable.isConstructor()) return false;

    if (overridden.isDefaultConstructor()) return false;

    return areSubTypeCompatible(overridden, overridable);
  }

  /**
   * @param overridden the overridden method
   * @param overridable the overriding method
   * @return returns <code>true</code> if the overriding method overrrides the overridden
   * @deprecated Need to review: Use {@link #isSubsignature(IMethodBinding, IMethodBinding)} if the
   *     two bindings are in the same hierarchy (directly overrides each other), or {@link
   *     #findMethodInHierarchy(ITypeBinding, String, ITypeBinding[])} else.
   */
  public static boolean areOverriddenMethods(
      IMethodBinding overridden, IMethodBinding overridable) {

    if (!overridden.getName().equals(overridable.getName())) return false;

    return areSubTypeCompatible(overridden, overridable);
  }

  private static boolean areSubTypeCompatible(
      IMethodBinding overridden, IMethodBinding overridable) {

    if (overridden.getParameterTypes().length != overridable.getParameterTypes().length)
      return false;

    ITypeBinding overriddenReturn = overridden.getReturnType();
    ITypeBinding overridableReturn = overridable.getReturnType();
    if (overriddenReturn == null || overridableReturn == null) return false;

    if (!overriddenReturn.getErasure().isSubTypeCompatible(overridableReturn.getErasure()))
      return false;

    ITypeBinding[] overriddenTypes = overridden.getParameterTypes();
    ITypeBinding[] overridableTypes = overridable.getParameterTypes();
    Assert.isTrue(overriddenTypes.length == overridableTypes.length);
    for (int index = 0; index < overriddenTypes.length; index++) {
      final ITypeBinding overridableErasure = overridableTypes[index].getErasure();
      final ITypeBinding overriddenErasure = overriddenTypes[index].getErasure();
      if (!overridableErasure.isSubTypeCompatible(overriddenErasure)
          || !overridableErasure.getKey().equals(overriddenErasure.getKey())) return false;
    }
    ITypeBinding[] overriddenExceptions = overridden.getExceptionTypes();
    ITypeBinding[] overridableExceptions = overridable.getExceptionTypes();
    boolean checked = false;
    for (int index = 0; index < overriddenExceptions.length; index++) {
      checked = false;
      for (int offset = 0; offset < overridableExceptions.length; offset++) {
        if (overriddenExceptions[index].isSubTypeCompatible(overridableExceptions[offset]))
          checked = true;
      }
      if (!checked) return false;
    }
    return true;
  }

  /**
   * Returns the boxed type binding according to JLS3 5.1.7, or the original binding if the given
   * type is not a primitive type.
   *
   * @param type a type binding
   * @param ast an AST to resolve the boxed type
   * @return the boxed type, or the original type if no boxed type found
   */
  public static ITypeBinding getBoxedTypeBinding(ITypeBinding type, AST ast) {
    if (!type.isPrimitive()) return type;
    String boxedTypeName = getBoxedTypeName(type.getName());
    if (boxedTypeName == null) return type;
    ITypeBinding boxed = ast.resolveWellKnownType(boxedTypeName);
    if (boxed == null) return type;
    return boxed;
  }

  private static String getBoxedTypeName(String primitiveName) {
    if ("long".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Long"; // $NON-NLS-1$
    else if ("int".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Integer"; // $NON-NLS-1$
    else if ("short".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Short"; // $NON-NLS-1$
    else if ("char".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Character"; // $NON-NLS-1$
    else if ("byte".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Byte"; // $NON-NLS-1$
    else if ("boolean".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Boolean"; // $NON-NLS-1$
    else if ("float".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Float"; // $NON-NLS-1$
    else if ("double".equals(primitiveName)) // $NON-NLS-1$
    return "java.lang.Double"; // $NON-NLS-1$
    else return null;
  }

  /**
   * Returns the unboxed type binding according to JLS3 5.1.7, or the original binding if the given
   * type is not a boxed type.
   *
   * @param type a type binding
   * @param ast an AST to resolve the unboxed type
   * @return the unboxed type, or the original type if no unboxed type found
   */
  public static ITypeBinding getUnboxedTypeBinding(ITypeBinding type, AST ast) {
    if (!type.isClass()) return type;
    String unboxedTypeName = getUnboxedTypeName(type.getQualifiedName());
    if (unboxedTypeName == null) return type;
    ITypeBinding unboxed = ast.resolveWellKnownType(unboxedTypeName);
    if (unboxed == null) return type;
    return unboxed;
  }

  private static String getUnboxedTypeName(String boxedName) {
    if ("java.lang.Long".equals(boxedName)) // $NON-NLS-1$
    return "long"; // $NON-NLS-1$
    else if ("java.lang.Integer".equals(boxedName)) // $NON-NLS-1$
    return "int"; // $NON-NLS-1$
    else if ("java.lang.Short".equals(boxedName)) // $NON-NLS-1$
    return "short"; // $NON-NLS-1$
    else if ("java.lang.Character".equals(boxedName)) // $NON-NLS-1$
    return "char"; // $NON-NLS-1$
    else if ("java.lang.Byte".equals(boxedName)) // $NON-NLS-1$
    return "byte"; // $NON-NLS-1$
    else if ("java.lang.Boolean".equals(boxedName)) // $NON-NLS-1$
    return "boolean"; // $NON-NLS-1$
    else if ("java.lang.Float".equals(boxedName)) // $NON-NLS-1$
    return "float"; // $NON-NLS-1$
    else if ("java.lang.Double".equals(boxedName)) // $NON-NLS-1$
    return "double"; // $NON-NLS-1$
    else return null;
  }

  /**
   * Resolve the binding (<em>not</em> the type binding) for the expression or a nested expression
   * (e.g. nested in parentheses, cast, ...).
   *
   * @param expression an expression node
   * @param goIntoCast iff <code>true</code>, go into a CastExpression's expression to resolve
   * @return the expression binding, or <code>null</code> if the expression has no binding or the
   *     binding could not be resolved
   * @see StubUtility#getVariableNameSuggestions(int, IJavaProject, ITypeBinding, Expression,
   *     java.util.Collection)
   * @since 3.5
   */
  public static IBinding resolveExpressionBinding(Expression expression, boolean goIntoCast) {
    // TODO: search for callers of resolve*Binding() methods and replace with call to this method

    // similar to StubUtility#getVariableNameSuggestions(int, IJavaProject, ITypeBinding,
    // Expression, Collection)
    switch (expression.getNodeType()) {
      case ASTNode.SIMPLE_NAME:
      case ASTNode.QUALIFIED_NAME:
        return ((Name) expression).resolveBinding();

      case ASTNode.FIELD_ACCESS:
        return ((FieldAccess) expression).resolveFieldBinding();
      case ASTNode.SUPER_FIELD_ACCESS:
        return ((SuperFieldAccess) expression).resolveFieldBinding();

      case ASTNode.METHOD_INVOCATION:
        return ((MethodInvocation) expression).resolveMethodBinding();
      case ASTNode.SUPER_METHOD_INVOCATION:
        return ((SuperMethodInvocation) expression).resolveMethodBinding();
      case ASTNode.CLASS_INSTANCE_CREATION:
        return ((ClassInstanceCreation) expression).resolveConstructorBinding();

      case ASTNode.MARKER_ANNOTATION:
      case ASTNode.SINGLE_MEMBER_ANNOTATION:
      case ASTNode.NORMAL_ANNOTATION:
        return ((Annotation) expression).resolveAnnotationBinding();

      case ASTNode.ARRAY_ACCESS:
        return resolveExpressionBinding(((ArrayAccess) expression).getArray(), goIntoCast);
      case ASTNode.CAST_EXPRESSION:
        if (goIntoCast) {
          return resolveExpressionBinding(((CastExpression) expression).getExpression(), true);
        } else {
          return null;
        }
      case ASTNode.PARENTHESIZED_EXPRESSION:
        return resolveExpressionBinding(
            ((ParenthesizedExpression) expression).getExpression(), goIntoCast);
      case ASTNode.PREFIX_EXPRESSION:
        return resolveExpressionBinding(((PrefixExpression) expression).getOperand(), goIntoCast);
      case ASTNode.POSTFIX_EXPRESSION:
        return resolveExpressionBinding(((PostfixExpression) expression).getOperand(), goIntoCast);
      default:
        return null;
    }
  }

  public static boolean isNullAnnotation(ITypeBinding annotationType, IJavaProject project) {
    String qualifiedName = annotationType.getQualifiedName();
    return qualifiedName.equals(project.getOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, true))
        || qualifiedName.equals(
            project.getOption(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, true));
  }

  /**
   * Returns the n-th component type of the given type, or <code>null</code> if the type binding is
   * not an array type or has not that many dimensions.
   *
   * @param arrayType an array type binding
   * @param n number of dimensions to cut
   * @return arrayType with n dimensions removed, or <code>null</code>
   * @since 3.10
   */
  public static ITypeBinding getComponentType(ITypeBinding arrayType, int n) {
    ITypeBinding type = arrayType;
    while (n > 0 && type != null) {
      type = type.getComponentType();
      n--;
    }
    return type;
  }
}

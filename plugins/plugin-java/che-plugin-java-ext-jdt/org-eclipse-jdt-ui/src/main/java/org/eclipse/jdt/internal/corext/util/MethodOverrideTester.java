/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/** Finds overriding and overridden methods based on the Java model. */
public class MethodOverrideTester {
  private static class Substitutions {

    public static final Substitutions EMPTY_SUBST = new Substitutions();

    private HashMap<String, String[]> fMap;

    public Substitutions() {
      fMap = null;
    }

    public void addSubstitution(String typeVariable, String substitution, String erasure) {
      if (fMap == null) {
        fMap = new HashMap<String, String[]>(3);
      }
      fMap.put(typeVariable, new String[] {substitution, erasure});
    }

    private String[] getSubstArray(String typeVariable) {
      if (fMap != null) {
        return fMap.get(typeVariable);
      }
      return null;
    }

    public String getSubstitution(String typeVariable) {
      String[] subst = getSubstArray(typeVariable);
      if (subst != null) {
        return subst[0];
      }
      return null;
    }

    public String getErasure(String typeVariable) {
      String[] subst = getSubstArray(typeVariable);
      if (subst != null) {
        return subst[1];
      }
      return null;
    }
  }

  private final IType fFocusType;
  private final ITypeHierarchy fHierarchy;

  private Map<IMethod, Substitutions> fMethodSubstitutions;
  private Map<IType, Substitutions> fTypeVariableSubstitutions;

  public MethodOverrideTester(IType focusType, ITypeHierarchy hierarchy) {
    if (focusType == null || hierarchy == null) {
      throw new IllegalArgumentException();
    }
    fFocusType = focusType;
    fHierarchy = hierarchy;
    fTypeVariableSubstitutions = null;
    fMethodSubstitutions = null;
  }

  public IType getFocusType() {
    return fFocusType;
  }

  public ITypeHierarchy getTypeHierarchy() {
    return fHierarchy;
  }

  /**
   * Finds the method that declares the given method. A declaring method is the 'original' method
   * declaration that does not override nor implement a method. <code>null</code> is returned it the
   * given method does not override a method. When searching, super class are examined before
   * implemented interfaces.
   *
   * @param overriding the overriding method
   * @param testVisibility If true the result is tested on visibility. Null is returned if the
   *     method is not visible.
   * @return the declaring method, or <code>null</code>
   * @throws JavaModelException if a problem occurs
   */
  public IMethod findDeclaringMethod(IMethod overriding, boolean testVisibility)
      throws JavaModelException {
    IMethod result = null;
    IMethod overridden = findOverriddenMethod(overriding, testVisibility);
    while (overridden != null) {
      result = overridden;
      overridden = findOverriddenMethod(result, testVisibility);
    }
    return result;
  }

  /**
   * Finds the method that is overridden by the given method. First the super class is examined and
   * then the implemented interfaces.
   *
   * @param overriding the overriding method
   * @param testVisibility If true the result is tested on visibility. Null is returned if the
   *     method is not visible.
   * @return a method that is directly overridden by the given method, or <code>null</code>
   * @throws JavaModelException if a problem occurs
   */
  public IMethod findOverriddenMethod(IMethod overriding, boolean testVisibility)
      throws JavaModelException {
    int flags = overriding.getFlags();
    if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overriding.isConstructor()) {
      return null;
    }

    IType type = overriding.getDeclaringType();
    IType superClass = fHierarchy.getSuperclass(type);
    if (superClass != null) {
      IMethod res = findOverriddenMethodInHierarchy(superClass, overriding);
      if (res != null) {
        if (!testVisibility || JavaModelUtil.isVisibleInHierarchy(res, type.getPackageFragment())) {
          return res;
        }
      }
    }
    IType[] interfaces = fHierarchy.getSuperInterfaces(type);
    for (int i = 0; i < interfaces.length; i++) {
      IMethod res = findOverriddenMethodInHierarchy(interfaces[i], overriding);
      if (res != null) {
        return res; // methods from interfaces are always public and therefore visible
      }
    }
    return null;
  }

  /**
   * Finds the directly overridden method in a type and its super types. First the super class is
   * examined and then the implemented interfaces. With generics it is possible that 2 methods in
   * the same type are overidden at the same time. In that case, the first overridden method found
   * is returned.
   *
   * @param type The type to find methods in
   * @param overriding The overriding method
   * @return The first overridden method or <code>null</code> if no method is overridden
   * @throws JavaModelException if a problem occurs
   */
  public IMethod findOverriddenMethodInHierarchy(IType type, IMethod overriding)
      throws JavaModelException {
    IMethod method = findOverriddenMethodInType(type, overriding);
    if (method != null) {
      return method;
    }
    IType superClass = fHierarchy.getSuperclass(type);
    if (superClass != null) {
      IMethod res = findOverriddenMethodInHierarchy(superClass, overriding);
      if (res != null) {
        return res;
      }
    }
    IType[] superInterfaces = fHierarchy.getSuperInterfaces(type);
    for (int i = 0; i < superInterfaces.length; i++) {
      IMethod res = findOverriddenMethodInHierarchy(superInterfaces[i], overriding);
      if (res != null) {
        return res;
      }
    }
    return method;
  }

  /**
   * Finds an overridden method in a type. With generics it is possible that 2 methods in the same
   * type are overridden at the same time. In that case the first overridden method found is
   * returned.
   *
   * @param overriddenType The type to find methods in
   * @param overriding The overriding method
   * @return The first overridden method or <code>null</code> if no method is overridden
   * @throws JavaModelException if a problem occurs
   */
  public IMethod findOverriddenMethodInType(IType overriddenType, IMethod overriding)
      throws JavaModelException {
    int flags = overriding.getFlags();
    if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overriding.isConstructor()) return null;
    IMethod[] overriddenMethods = overriddenType.getMethods();
    for (int i = 0; i < overriddenMethods.length; i++) {
      IMethod overridden = overriddenMethods[i];
      flags = overridden.getFlags();
      if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overridden.isConstructor()) continue;
      if (isSubsignature(overriding, overridden)) {
        return overridden;
      }
    }
    return null;
  }

  /**
   * Finds an overriding method in a type.
   *
   * @param overridingType The type to find methods in
   * @param overridden The overridden method
   * @return The overriding method or <code>null</code> if no method is overriding.
   * @throws JavaModelException if a problem occurs
   */
  public IMethod findOverridingMethodInType(IType overridingType, IMethod overridden)
      throws JavaModelException {
    int flags = overridden.getFlags();
    if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overridden.isConstructor()) return null;
    IMethod[] overridingMethods = overridingType.getMethods();
    for (int i = 0; i < overridingMethods.length; i++) {
      IMethod overriding = overridingMethods[i];
      flags = overriding.getFlags();
      if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overriding.isConstructor()) continue;
      if (isSubsignature(overriding, overridden)) {
        return overriding;
      }
    }
    return null;
  }

  /**
   * Tests if a method is a subsignature of another method.
   *
   * @param overriding overriding method (m1)
   * @param overridden overridden method (m2)
   * @return <code>true</code> iff the method <code>m1</code> is a subsignature of the method <code>
   *     m2</code>. This is one of the requirements for m1 to override m2. Accessibility and return
   *     types are not taken into account. Note that subsignature is <em>not</em> symmetric!
   * @throws JavaModelException if a problem occurs
   */
  public boolean isSubsignature(IMethod overriding, IMethod overridden) throws JavaModelException {
    if (!overridden.getElementName().equals(overriding.getElementName())) {
      return false;
    }
    int nParameters = overridden.getNumberOfParameters();
    if (nParameters != overriding.getNumberOfParameters()) {
      return false;
    }

    if (!hasCompatibleTypeParameters(overriding, overridden)) {
      return false;
    }

    return nParameters == 0 || hasCompatibleParameterTypes(overriding, overridden);
  }

  private boolean hasCompatibleTypeParameters(IMethod overriding, IMethod overridden)
      throws JavaModelException {
    ITypeParameter[] overriddenTypeParameters = overridden.getTypeParameters();
    ITypeParameter[] overridingTypeParameters = overriding.getTypeParameters();
    int nOverridingTypeParameters = overridingTypeParameters.length;
    if (overriddenTypeParameters.length != nOverridingTypeParameters) {
      return nOverridingTypeParameters == 0;
    }
    Substitutions overriddenSubst = getMethodSubstitions(overridden);
    Substitutions overridingSubst = getMethodSubstitions(overriding);
    for (int i = 0; i < nOverridingTypeParameters; i++) {
      String erasure1 = overriddenSubst.getErasure(overriddenTypeParameters[i].getElementName());
      String erasure2 = overridingSubst.getErasure(overridingTypeParameters[i].getElementName());
      if (erasure1 == null || !erasure1.equals(erasure2)) {
        return false;
      }
      // comparing only the erasure is not really correct: Need to compare all bounds, that can be
      // in different order
      int nBounds = overriddenTypeParameters[i].getBounds().length;
      if (nBounds > 1 && nBounds != overridingTypeParameters[i].getBounds().length) {
        return false;
      }
    }
    return true;
  }

  private boolean hasCompatibleParameterTypes(IMethod overriding, IMethod overridden)
      throws JavaModelException {
    String[] overriddenParamTypes = overridden.getParameterTypes();
    String[] overridingParamTypes = overriding.getParameterTypes();

    String[] substitutedOverriding = new String[overridingParamTypes.length];
    boolean testErasure = false;

    for (int i = 0; i < overridingParamTypes.length; i++) {
      String overriddenParamSig = overriddenParamTypes[i];
      String overriddenParamName = getSubstitutedTypeName(overriddenParamSig, overridden);
      String overridingParamName = getSubstitutedTypeName(overridingParamTypes[i], overriding);
      substitutedOverriding[i] = overridingParamName;
      if (!overriddenParamName.equals(overridingParamName)) {
        testErasure = true;
        break;
      }
    }
    if (testErasure) {
      for (int i = 0; i < overridingParamTypes.length; i++) {
        String overriddenParamSig = overriddenParamTypes[i];
        String overriddenParamName = getErasedTypeName(overriddenParamSig, overridden);
        String overridingParamName = substitutedOverriding[i];
        if (overridingParamName == null)
          overridingParamName = getSubstitutedTypeName(overridingParamTypes[i], overriding);
        if (!overriddenParamName.equals(overridingParamName)) {
          return false;
        }
      }
    }
    return true;
  }

  private String getVariableSubstitution(IMember context, String variableName)
      throws JavaModelException {
    IType type;
    if (context instanceof IMethod) {
      String subst = getMethodSubstitions((IMethod) context).getSubstitution(variableName);
      if (subst != null) {
        return subst;
      }
      type = context.getDeclaringType();
    } else {
      type = (IType) context;
    }
    String subst = getTypeSubstitions(type).getSubstitution(variableName);
    if (subst != null) {
      return subst;
    }
    IJavaElement parent = type.getParent();
    if (parent instanceof IMethod) {
      return getVariableSubstitution((IMethod) parent, variableName);
    } else if (type.getDeclaringType() != null) {
      return getVariableSubstitution(type.getDeclaringType(), variableName);
    }
    return variableName; // not a type variable
  }

  private String getVariableErasure(IMember context, String variableName)
      throws JavaModelException {
    IType type;
    if (context instanceof IMethod) {
      String subst = getMethodSubstitions((IMethod) context).getErasure(variableName);
      if (subst != null) {
        return subst;
      }
      type = context.getDeclaringType();
    } else {
      type = (IType) context;
    }
    String subst = getTypeSubstitions(type).getErasure(variableName);
    if (subst != null) {
      return subst;
    }
    IJavaElement parent = type.getParent();
    if (parent instanceof IMethod) {
      return getVariableErasure((IMethod) parent, variableName);
    } else if (type.getDeclaringType() != null) {
      return getVariableErasure(type.getDeclaringType(), variableName);
    }
    return variableName; // not a type variable
  }

  /*
   * Returns the substitutions for a method's type parameters
   */
  private Substitutions getMethodSubstitions(IMethod method) throws JavaModelException {
    if (fMethodSubstitutions == null) {
      fMethodSubstitutions = new LRUMap<IMethod, Substitutions>(3);
    }

    Substitutions s = fMethodSubstitutions.get(method);
    if (s == null) {
      ITypeParameter[] typeParameters = method.getTypeParameters();
      if (typeParameters.length == 0) {
        s = Substitutions.EMPTY_SUBST;
      } else {
        IType instantiatedType = method.getDeclaringType();
        s = new Substitutions();
        for (int i = 0; i < typeParameters.length; i++) {
          ITypeParameter curr = typeParameters[i];
          s.addSubstitution(
              curr.getElementName(),
              '+' + String.valueOf(i),
              getTypeParameterErasure(curr, instantiatedType));
        }
      }
      fMethodSubstitutions.put(method, s);
    }
    return s;
  }

  /*
   * Returns the substitutions for a type's type parameters
   */
  private Substitutions getTypeSubstitions(IType type) throws JavaModelException {
    if (fTypeVariableSubstitutions == null) {
      fTypeVariableSubstitutions = new HashMap<IType, Substitutions>();
      computeSubstitutions(fFocusType, null, null);
    }
    Substitutions subst = fTypeVariableSubstitutions.get(type);
    if (subst == null) {
      return Substitutions.EMPTY_SUBST;
    }
    return subst;
  }

  private void computeSubstitutions(
      IType instantiatedType, IType instantiatingType, String[] typeArguments)
      throws JavaModelException {
    Substitutions s = new Substitutions();
    fTypeVariableSubstitutions.put(instantiatedType, s);

    ITypeParameter[] typeParameters = instantiatedType.getTypeParameters();

    if (instantiatingType == null) { // the focus type
      for (int i = 0; i < typeParameters.length; i++) {
        ITypeParameter curr = typeParameters[i];
        // use star to make type variables different from type refs
        s.addSubstitution(
            curr.getElementName(),
            '*' + curr.getElementName(),
            getTypeParameterErasure(curr, instantiatedType));
      }
    } else {
      if (typeParameters.length == typeArguments.length) {
        for (int i = 0; i < typeParameters.length; i++) {
          ITypeParameter curr = typeParameters[i];
          String substString =
              getSubstitutedTypeName(
                  typeArguments[i],
                  instantiatingType); // substitute in the context of the instantiatingType
          String erasure =
              getErasedTypeName(
                  typeArguments[i], instantiatingType); // get the erasure from the type argument
          s.addSubstitution(curr.getElementName(), substString, erasure);
        }
      } else if (typeArguments.length == 0) { // raw type reference
        for (int i = 0; i < typeParameters.length; i++) {
          ITypeParameter curr = typeParameters[i];
          String erasure = getTypeParameterErasure(curr, instantiatedType);
          s.addSubstitution(curr.getElementName(), erasure, erasure);
        }
      } else {
        // code with errors
      }
    }
    String superclassTypeSignature = instantiatedType.getSuperclassTypeSignature();
    if (superclassTypeSignature != null) {
      String[] superTypeArguments = Signature.getTypeArguments(superclassTypeSignature);
      IType superclass = fHierarchy.getSuperclass(instantiatedType);
      if (superclass != null && !fTypeVariableSubstitutions.containsKey(superclass)) {
        computeSubstitutions(superclass, instantiatedType, superTypeArguments);
      }
    }
    String[] superInterfacesTypeSignature;
    if (instantiatedType.isAnonymous()) {
      // special case: superinterface is also returned by IType#getSuperclassTypeSignature()
      superInterfacesTypeSignature = new String[] {superclassTypeSignature};
    } else {
      superInterfacesTypeSignature = instantiatedType.getSuperInterfaceTypeSignatures();
    }
    int nInterfaces = superInterfacesTypeSignature.length;
    if (nInterfaces > 0) {
      IType[] superInterfaces = fHierarchy.getSuperInterfaces(instantiatedType);
      if (superInterfaces.length == nInterfaces) {
        for (int i = 0; i < nInterfaces; i++) {
          String[] superTypeArguments = Signature.getTypeArguments(superInterfacesTypeSignature[i]);
          IType superInterface = superInterfaces[i];
          if (!fTypeVariableSubstitutions.containsKey(superInterface)) {
            computeSubstitutions(superInterface, instantiatedType, superTypeArguments);
          }
        }
      }
    }
  }

  private String getTypeParameterErasure(ITypeParameter typeParameter, IType context)
      throws JavaModelException {
    String[] bounds = typeParameter.getBounds();
    if (bounds.length > 0) {
      return getSubstitutedTypeName(Signature.createTypeSignature(bounds[0], false), context);
    }
    return "Object"; // $NON-NLS-1$
  }

  /**
   * Translates the type signature to a 'normalized' type name where all variables are substituted
   * for the given type or method context. The returned name contains only simple names and can be
   * used to compare against other substituted type names
   *
   * @param typeSig The type signature to translate
   * @param context The context for the substitution
   * @return a type name
   * @throws JavaModelException if a problem occurs
   */
  private String getSubstitutedTypeName(String typeSig, IMember context) throws JavaModelException {
    return internalGetSubstitutedTypeName(typeSig, context, false, new StringBuffer()).toString();
  }

  private String getErasedTypeName(String typeSig, IMember context) throws JavaModelException {
    return internalGetSubstitutedTypeName(typeSig, context, true, new StringBuffer()).toString();
  }

  private StringBuffer internalGetSubstitutedTypeName(
      String typeSig, IMember context, boolean erasure, StringBuffer buf)
      throws JavaModelException {
    int sigKind = Signature.getTypeSignatureKind(typeSig);
    switch (sigKind) {
      case Signature.BASE_TYPE_SIGNATURE:
        return buf.append(Signature.toString(typeSig));
      case Signature.ARRAY_TYPE_SIGNATURE:
        internalGetSubstitutedTypeName(Signature.getElementType(typeSig), context, erasure, buf);
        for (int i = Signature.getArrayCount(typeSig); i > 0; i--) {
          buf.append('[').append(']');
        }
        return buf;
      case Signature.CLASS_TYPE_SIGNATURE:
        {
          String erasureSig = Signature.getTypeErasure(typeSig);
          String erasureName = Signature.getSimpleName(Signature.toString(erasureSig));

          char ch = erasureSig.charAt(0);
          if (ch == Signature.C_RESOLVED) {
            buf.append(erasureName);
          } else if (ch == Signature.C_UNRESOLVED) { // could be a type variable
            if (erasure) {
              buf.append(getVariableErasure(context, erasureName));
            } else {
              buf.append(getVariableSubstitution(context, erasureName));
            }
          } else {
            Assert.isTrue(false, "Unknown class type signature"); // $NON-NLS-1$
          }
          if (!erasure) {
            String[] typeArguments = Signature.getTypeArguments(typeSig);
            if (typeArguments.length > 0) {
              buf.append('<');
              for (int i = 0; i < typeArguments.length; i++) {
                if (i > 0) {
                  buf.append(',');
                }
                internalGetSubstitutedTypeName(typeArguments[i], context, erasure, buf);
              }
              buf.append('>');
            }
          }
          return buf;
        }
      case Signature.TYPE_VARIABLE_SIGNATURE:
        String varName = Signature.toString(typeSig);
        if (erasure) {
          return buf.append(getVariableErasure(context, varName));
        } else {
          return buf.append(getVariableSubstitution(context, varName));
        }
      case Signature.WILDCARD_TYPE_SIGNATURE:
        {
          buf.append('?');
          char ch = typeSig.charAt(0);
          if (ch == Signature.C_STAR) {
            return buf;
          } else if (ch == Signature.C_EXTENDS) {
            buf.append(" extends "); // $NON-NLS-1$
          } else {
            buf.append(" super "); // $NON-NLS-1$
          }
          return internalGetSubstitutedTypeName(typeSig.substring(1), context, erasure, buf);
        }
      case Signature.CAPTURE_TYPE_SIGNATURE:
        return internalGetSubstitutedTypeName(typeSig.substring(1), context, erasure, buf);
      default:
        Assert.isTrue(false, "Unhandled type signature kind"); // $NON-NLS-1$
        return buf;
    }
  }
}

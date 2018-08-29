/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;

/**
 * Proposal info that computes the javadoc lazily when it is queried.
 *
 * @since 3.1
 */
public final class MethodProposalInfo extends MemberProposalInfo {

  /**
   * Fallback in case we can't match a generic method. The fall back is only based on method name
   * and number of parameters.
   */
  private IMethod fFallbackMatch;

  /**
   * Creates a new proposal info.
   *
   * @param project the java project to reference when resolving types
   * @param proposal the proposal to generate information for
   */
  public MethodProposalInfo(IJavaProject project, CompletionProposal proposal) {
    super(project, proposal);
  }

  /**
   * Resolves the member described by the receiver and returns it if found. Returns <code>null
   * </code> if no corresponding member can be found.
   *
   * @return the resolved member or <code>null</code> if none is found
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  @Override
  protected IMember resolveMember() throws JavaModelException {
    char[] declarationSignature = fProposal.getDeclarationSignature();
    String typeName = SignatureUtil.stripSignatureToFQN(String.valueOf(declarationSignature));
    IType type = fJavaProject.findType(typeName);
    if (type != null) {
      String name = String.valueOf(fProposal.getName());
      String[] parameters =
          Signature.getParameterTypes(
              String.valueOf(SignatureUtil.fix83600(fProposal.getSignature())));
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = SignatureUtil.getLowerBound(parameters[i]);
      }
      boolean isConstructor = fProposal.isConstructor();

      return findMethod(name, parameters, isConstructor, type);
    }

    return null;
  }

  /* adapted from JavaModelUtil */

  /**
   * Finds a method in a type. This searches for a method with the same name and signature.
   * Parameter types are only compared by the simple name, no resolving for the fully qualified type
   * name is done. Constructors are only compared by parameters, not the name.
   *
   * @param name The name of the method to find
   * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
   * @param isConstructor If the method is a constructor
   * @param type the type
   * @return The first found method or <code>null</code>, if nothing found
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the Java model fails
   */
  private IMethod findMethod(String name, String[] paramTypes, boolean isConstructor, IType type)
      throws JavaModelException {
    Map<String, char[]> typeVariables = computeTypeVariables(type);
    return findMethod(name, paramTypes, isConstructor, type, typeVariables);
  }

  /**
   * The type and method signatures received in <code>CompletionProposals</code> of type <code>
   * METHOD_REF</code> contain concrete type bounds. When comparing parameters of the signature with
   * an <code>IMethod</code>, we have to make sure that we match the case where the formal method
   * declaration uses a type variable which in the signature is already substituted with a concrete
   * type (bound).
   *
   * <p>This method creates a map from type variable names to type signatures based on the position
   * they appear in the type declaration. The type signatures are filtered through {@link
   * SignatureUtil#getLowerBound(char[])}.
   *
   * @param type the type to get the variables from
   * @return a map from type variables to concrete type signatures
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  private Map<String, char[]> computeTypeVariables(IType type) throws JavaModelException {
    Map<String, char[]> map = new HashMap<String, char[]>();
    char[] declarationSignature = fProposal.getDeclarationSignature();
    if (declarationSignature == null) // array methods don't contain a declaration signature
    return map;
    char[][] concreteParameters = Signature.getTypeArguments(declarationSignature);

    ITypeParameter[] typeParameters = type.getTypeParameters();
    for (int i = 0; i < typeParameters.length; i++) {
      String variable = typeParameters[i].getElementName();
      if (concreteParameters.length > i)
        // use lower bound since method equality is only parameter based
        map.put(variable, SignatureUtil.getLowerBound(concreteParameters[i]));
      else
        // fProposal.getDeclarationSignature() is a raw type - use Object
        map.put(variable, "Ljava.lang.Object;".toCharArray()); // $NON-NLS-1$
    }

    return map;
  }

  /**
   * Finds a method by name. This searches for a method with a name and signature. Parameter types
   * are only compared by the simple name, no resolving for the fully qualified type name is done.
   * Constructors are only compared by parameters, not the name.
   *
   * @param name The name of the method to find
   * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
   * @param isConstructor If the method is a constructor
   * @param type the given type in which to search for methods
   * @param typeVariables a map from type variables to concretely used types
   * @return The found method or <code>null</code>, if nothing found
   * @throws org.eclipse.jdt.core.JavaModelException if the method does not exist or if an exception
   *     occurs while accessing its corresponding resource
   */
  private IMethod findMethod(
      String name,
      String[] paramTypes,
      boolean isConstructor,
      IType type,
      Map<String, char[]> typeVariables)
      throws JavaModelException {
    IMethod[] methods = type.getMethods();
    for (int i = methods.length - 1; i >= 0; i--) {
      if (isSameMethodSignature(name, paramTypes, isConstructor, methods[i], typeVariables, type)) {
        return methods[i];
      }
    }
    return fFallbackMatch;
  }

  /**
   * Tests if a method equals to the given signature. Parameter types are only compared by the
   * simple name, no resolving for the fully qualified type name is done. Constructors are only
   * compared by parameters, not the name.
   *
   * @param name Name of the method
   * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
   * @param isConstructor Specifies if the method is a constructor
   * @param method the method to be compared with this info's method
   * @param typeVariables a map from type variables to types
   * @param type the given type that declares the method
   * @return Returns <code>true</code> if the method has the given name and parameter types and
   *     constructor state.
   * @throws org.eclipse.jdt.core.JavaModelException if the method does not exist or if an exception
   *     occurs while accessing its corresponding resource
   */
  private boolean isSameMethodSignature(
      String name,
      String[] paramTypes,
      boolean isConstructor,
      IMethod method,
      Map<String, char[]> typeVariables,
      IType type)
      throws JavaModelException {
    if (isConstructor || name.equals(method.getElementName())) {
      if (isConstructor == method.isConstructor()) {
        String[] otherParams = method.getParameterTypes(); // types may be type variables
        boolean isBinaryConstructorForNonStaticMemberClass =
            method.isBinary() && type.isMember() && !Flags.isStatic(type.getFlags());
        int syntheticParameterCorrection =
            isBinaryConstructorForNonStaticMemberClass
                    && paramTypes.length == otherParams.length - 1
                ? 1
                : 0;
        if (paramTypes.length == otherParams.length - syntheticParameterCorrection) {
          fFallbackMatch = method;
          String signature = method.getSignature();
          String[] otherParamsFromSignature =
              Signature.getParameterTypes(signature); // types are resolved / upper-bounded
          // no need to check method type variables since these are
          // not yet bound when proposing a method
          for (int i = 0; i < paramTypes.length; i++) {
            String ourParamName = computeSimpleTypeName(paramTypes[i], typeVariables);
            String otherParamName1 =
                computeSimpleTypeName(otherParams[i + syntheticParameterCorrection], typeVariables);
            String otherParamName2 =
                computeSimpleTypeName(
                    otherParamsFromSignature[i + syntheticParameterCorrection], typeVariables);

            if (!ourParamName.equals(otherParamName1) && !ourParamName.equals(otherParamName2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the simple erased name for a given type signature, possibly replacing type variables.
   *
   * @param signature the type signature
   * @param typeVariables the Map&lt;SimpleName, VariableName>
   * @return the simple erased name for signature
   */
  private String computeSimpleTypeName(String signature, Map<String, char[]> typeVariables) {
    // method equality uses erased types
    String erasure = Signature.getTypeErasure(signature);
    erasure = erasure.replaceAll("/", "."); // $NON-NLS-1$//$NON-NLS-2$
    String simpleName = Signature.getSimpleName(Signature.toString(erasure));
    char[] typeVar = typeVariables.get(simpleName);
    if (typeVar != null) simpleName = String.valueOf(Signature.getSignatureSimpleName(typeVar));
    return simpleName;
  }
}

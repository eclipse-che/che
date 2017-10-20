/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.template.java;

import java.util.Arrays;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Utilities for Signature operations.
 *
 * @see Signature
 * @since 3.1
 */
public final class SignatureUtil {

  /**
   * The signature of the null type. This type does not really exist in the type system. It
   * represents the bound of type variables that have no lower bound, for example the parameter type
   * to the <code>add</code> method of an instance of <code>java.util.List&lt;? extends Number&gt;
   * </code>.
   *
   * <p>The only possible value that has that type is <code>null</code>.
   *
   * <p>The representation of the null type is the signature of a type variable named <code>null
   * </code> ({@value}), which will only work if no such variable gets declared in the same context.
   */
  private static final String NULL_TYPE_SIGNATURE = "Tnull;"; // $NON-NLS-1$

  private static final char[] NULL_TYPE_SIGNATURE_ARRAY = NULL_TYPE_SIGNATURE.toCharArray();
  /** The signature of <code>java.lang.Object</code> ({@value}). */
  private static final String OBJECT_SIGNATURE = "Ljava.lang.Object;"; // $NON-NLS-1$

  private static final char[] OBJECT_SIGNATURE_ARRAY = OBJECT_SIGNATURE.toCharArray();

  private SignatureUtil() {
    // do not instantiate
  }

  /**
   * Returns <code>true</code> if <code>signature</code> is the signature of the <code>
   * java.lang.Object</code> type.
   *
   * @param signature the signature
   * @return <code>true</code> if <code>signature</code> is the signature of the <code>
   *     java.lang.Object</code> type, <code>false</code> otherwise
   */
  public static boolean isJavaLangObject(String signature) {
    return OBJECT_SIGNATURE.equals(signature);
  }

  /**
   * Returns the upper bound of a type signature. Returns the signature of <code>java.lang.Object
   * </code> if <code>signature</code> is a lower bound (<code>? super T</code>); returns the
   * signature of the type <code>T</code> of an upper bound (<code>? extends T</code>) or <code>
   * signature</code> itself if it is not a bound signature.
   *
   * @param signature the signature
   * @return the upper bound signature of <code>signature</code>
   */
  public static String getUpperBound(String signature) {
    return String.valueOf(getUpperBound(signature.toCharArray()));
  }

  /**
   * Returns the upper bound of a type signature. Returns the signature of <code>java.lang.Object
   * </code> if <code>signature</code> is a lower bound (<code>? super T</code>); returns the
   * signature of the type <code>T</code> of an upper bound (<code>? extends T</code>) or <code>
   * signature</code> itself if it is not a bound signature.
   *
   * @param signature the signature
   * @return the upper bound signature of <code>signature</code>
   */
  public static char[] getUpperBound(char[] signature) {
    if (signature.length < 1) return signature;

    if (signature[0] == Signature.C_STAR) return OBJECT_SIGNATURE_ARRAY;

    int superIndex = indexOf(signature, Signature.C_SUPER);
    if (superIndex == 0) return OBJECT_SIGNATURE_ARRAY;

    if (superIndex != -1) {
      char afterSuper = signature[superIndex + 1];
      if (afterSuper == Signature.C_STAR) {
        char[] type = new char[signature.length - 1];
        System.arraycopy(signature, 0, type, 0, superIndex);
        type[superIndex] = Signature.C_STAR;
        System.arraycopy(
            signature, superIndex + 2, type, superIndex + 1, signature.length - superIndex - 2);
        return getUpperBound(type);
      }

      if (afterSuper == Signature.C_EXTENDS) {
        int typeEnd = typeEnd(signature, superIndex + 1);
        char[] type = new char[signature.length - (typeEnd - superIndex - 1)];
        System.arraycopy(signature, 0, type, 0, superIndex);
        type[superIndex] = Signature.C_STAR;
        System.arraycopy(signature, typeEnd, type, superIndex + 1, signature.length - typeEnd);
        return getUpperBound(type);
      }
    }

    if (signature[0] == Signature.C_EXTENDS) {
      char[] type = new char[signature.length - 1];
      System.arraycopy(signature, 1, type, 0, signature.length - 1);
      return type;
    }

    return signature;
  }

  /**
   * Returns the lower bound of a type signature. Returns the null type signature if <code>signature
   * </code> is a wildcard or upper bound (<code>? extends T</code>); returns the signature of the
   * type <code>T</code> of a lower bound (<code>? super T</code>) or <code>signature</code> itself
   * if it is not a bound signature.
   *
   * @param signature the signature
   * @return the lower bound signature of <code>signature</code>
   */
  public static String getLowerBound(String signature) {
    return String.valueOf(getLowerBound(signature.toCharArray()));
  }

  /**
   * Returns the lower bound of a type signature. Returns the null type signature if <code>signature
   * </code> is a wildcard or upper bound (<code>? extends T</code>); returns the signature of the
   * type <code>T</code> of a lower bound (<code>? super T</code>) or <code>signature</code> itself
   * if it is not a bound signature.
   *
   * @param signature the signature
   * @return the lower bound signature of <code>signature</code>
   */
  public static char[] getLowerBound(char[] signature) {
    if (signature.length < 1) return signature;

    if (signature.length == 1 && signature[0] == Signature.C_STAR) return signature;

    int superIndex = indexOf(signature, Signature.C_EXTENDS);
    if (superIndex == 0) return NULL_TYPE_SIGNATURE_ARRAY;

    if (superIndex != -1) {
      char afterSuper = signature[superIndex + 1];
      if (afterSuper == Signature.C_STAR || afterSuper == Signature.C_EXTENDS)
        // impossible captured type
        return NULL_TYPE_SIGNATURE_ARRAY;
    }

    char[][] typeArguments = Signature.getTypeArguments(signature);
    for (int i = 0; i < typeArguments.length; i++)
      if (Arrays.equals(typeArguments[i], NULL_TYPE_SIGNATURE_ARRAY))
        return NULL_TYPE_SIGNATURE_ARRAY;

    if (signature[0] == Signature.C_SUPER) {
      char[] type = new char[signature.length - 1];
      System.arraycopy(signature, 1, type, 0, signature.length - 1);
      return type;
    }

    return signature;
  }

  private static int indexOf(char[] signature, char ch) {
    for (int i = 0; i < signature.length; i++) {
      if (signature[i] == ch) return i;
    }
    return -1;
  }

  /**
   * Returns the fully qualified type name of the given signature, with any type parameters and
   * arrays erased.
   *
   * @param signature the signature
   * @return the fully qualified type name of the signature
   * @throws IllegalArgumentException if the signature is syntactically incorrect
   */
  public static String stripSignatureToFQN(String signature) throws IllegalArgumentException {
    signature = Signature.getTypeErasure(signature);
    signature = Signature.getElementType(signature);
    return Signature.toString(signature);
  }

  /**
   * Returns the qualified signature corresponding to <code>signature</code>.
   *
   * @param signature the signature to qualify
   * @param context the type inside which an unqualified type will be resolved to find the
   *     qualifier, or <code>null</code> if no context is available
   * @return the qualified signature
   */
  public static String qualifySignature(final String signature, final IType context) {
    if (context == null) return signature;

    String qualifier = Signature.getSignatureQualifier(signature);
    if (qualifier.length() > 0) return signature;

    String elementType = Signature.getElementType(signature);
    String erasure = Signature.getTypeErasure(elementType);
    String simpleName = Signature.getSignatureSimpleName(erasure);
    String genericSimpleName = Signature.getSignatureSimpleName(elementType);

    int dim = Signature.getArrayCount(signature);

    try {
      String[][] strings = context.resolveType(simpleName);
      if (strings != null && strings.length > 0) qualifier = strings[0][0];
    } catch (JavaModelException e) {
      // ignore - not found
    }

    if (qualifier.length() == 0) return signature;

    String qualifiedType = Signature.toQualifiedName(new String[] {qualifier, genericSimpleName});
    String qualifiedSignature = Signature.createTypeSignature(qualifiedType, true);
    String newSignature = Signature.createArraySignature(qualifiedSignature, dim);

    return newSignature;
  }

  /**
   * Takes a method signature <code>
   * [&lt; typeVariableName : formalTypeDecl &gt;] ( paramTypeSig1* ) retTypeSig</code> and returns
   * it with any parameter signatures filtered through <code>getLowerBound</code> and the return
   * type filtered through <code>getUpperBound</code>. Any preceding formal type variable
   * declarations are removed.
   *
   * <p>TODO this is a temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83600
   *
   * @param signature the method signature to convert
   * @return the signature with no bounded types
   */
  public static char[] unboundedSignature(char[] signature) {
    if (signature == null || signature.length < 2) return signature;

    final boolean BUG_83600 = true;
    // XXX the signatures from CompletionRequestor contain a superfluous '+'
    // before type parameters to parameter types
    if (BUG_83600) {
      signature = fix83600(signature);
    }

    StringBuffer res = new StringBuffer("("); // $NON-NLS-1$
    char[][] parameters = Signature.getParameterTypes(signature);
    for (int i = 0; i < parameters.length; i++) {
      char[] param = parameters[i];
      res.append(getLowerBound(param));
    }
    res.append(')');
    res.append(getUpperBound(Signature.getReturnType(signature)));
    return res.toString().toCharArray();
  }

  /**
   * TODO this is a temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83600 and
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
   *
   * @param signature the method signature to convert
   * @return the fixed signature
   */
  public static char[] fix83600(char[] signature) {
    if (signature == null || signature.length < 2) return signature;

    return Signature.removeCapture(signature);
  }

  private static int typeEnd(char[] signature, int pos) {
    int depth = 0;
    while (pos < signature.length) {
      switch (signature[pos]) {
        case Signature.C_GENERIC_START:
          depth++;
          break;
        case Signature.C_GENERIC_END:
          if (depth == 0) return pos;
          depth--;
          break;
        case Signature.C_SEMICOLON:
          if (depth == 0) return pos + 1;
          break;
      }
      pos++;
    }
    return pos + 1;
  }
}

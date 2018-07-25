/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.jdt.dom.ASTNodes;
import org.eclipse.che.jdt.javadoc.JavaElementLabels;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/** @author Evgen Vidolob */
@Singleton
public class SourcesFromBytecodeGenerator {

  public static final String METHOD_BODY = " /* compiled code */ ";
  private static final String COMMENT =
      new String(
          "\n // Failed to get sources. Instead, stub sources have been generated.\n // Implementation of methods is unavailable.\n");
  private static final String TAB = "    ";

  public String generateSource(IType type) throws JavaModelException {
    StringBuilder builder = new StringBuilder();
    builder.append(COMMENT);
    builder.append("package ").append(type.getPackageFragment().getElementName()).append(";\n");

    generateType(type, builder, TAB);
    return builder.toString();
  }

  private void generateType(IType type, StringBuilder builder, String indent)
      throws JavaModelException {
    int flags = 0;

    appendAnnotationLabels(type.getAnnotations(), flags, builder, indent.substring(TAB.length()));
    builder.append(indent.substring(TAB.length()));
    builder
        .append(getModifiers(type.getFlags(), type.getFlags()))
        .append(' ')
        .append(getJavaType(type))
        .append(' ')
        .append(type.getElementName());

    if (type.isResolved()) {
      BindingKey key = new BindingKey(type.getKey());
      if (key.isParameterizedType()) {
        String[] typeArguments = key.getTypeArguments();
        appendTypeArgumentSignaturesLabel(type, typeArguments, flags, builder);
      } else {
        String[] typeParameters = Signature.getTypeParameters(key.toSignature());
        appendTypeParameterSignaturesLabel(typeParameters, builder);
      }
    } else {
      appendTypeParametersLabels(type.getTypeParameters(), flags, builder);
    }

    if (!"java.lang.Object".equals(type.getSuperclassName())
        && !"java.lang.Enum".equals(type.getSuperclassName())) {

      builder.append(" extends ");
      if (type.getSuperclassTypeSignature() != null) {
        //                appendTypeSignatureLabel(type, type.getSuperclassTypeSignature(), flags,
        // builder);
        builder.append(Signature.toString(type.getSuperclassTypeSignature()));
      } else {
        builder.append(type.getSuperclassName());
      }
    }
    if (!type.isAnnotation()) {
      if (type.getSuperInterfaceNames().length != 0) {
        builder.append(" implements ");
        String[] signatures = type.getSuperInterfaceTypeSignatures();
        if (signatures.length == 0) {
          signatures = type.getSuperInterfaceNames();
        }
        for (String interfaceFqn : signatures) {
          builder.append(Signature.toString(interfaceFqn)).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
      }
    }
    builder.append(" {\n");

    List<IField> fields = new ArrayList<>();
    if (type.isEnum()) {
      builder.append(indent);
      for (IField field : type.getFields()) {
        if (field.isEnumConstant()) {
          builder.append(field.getElementName()).append(", ");
        } else {
          fields.add(field);
        }
      }
      if (", ".equals(builder.substring(builder.length() - 2))) {
        builder.delete(builder.length() - 2, builder.length());
      }
      builder.append(";\n");

    } else {
      fields.addAll(Arrays.asList(type.getFields()));
    }

    for (IField field : fields) {
      if (Flags.isSynthetic(field.getFlags())) {
        continue;
      }
      appendAnnotationLabels(field.getAnnotations(), flags, builder, indent);
      builder.append(indent).append(getModifiers(field.getFlags(), type.getFlags()));
      if (builder.charAt(builder.length() - 1) != ' ') {
        builder.append(' ');
      }

      builder
          .append(Signature.toCharArray(field.getTypeSignature().toCharArray()))
          .append(' ')
          .append(field.getElementName());
      if (field.getConstant() != null) {
        builder.append(" = ");
        if (field.getConstant() instanceof String) {
          builder.append('"').append(field.getConstant()).append('"');
        } else {
          builder.append(field.getConstant());
        }
      }
      builder.append(";\n");
    }
    builder.append('\n');

    for (IMethod method : type.getMethods()) {
      if (method.getElementName().equals("<clinit>") || Flags.isSynthetic(method.getFlags())) {
        continue;
      }
      appendAnnotationLabels(method.getAnnotations(), flags, builder, indent);
      BindingKey resolvedKey = method.isResolved() ? new BindingKey(method.getKey()) : null;
      String resolvedSig = (resolvedKey != null) ? resolvedKey.toSignature() : null;
      builder.append(indent).append(getModifiers(method.getFlags(), type.getFlags()));

      if (builder.charAt(builder.length() - 1) != ' ') {
        builder.append(' ');
      }
      if (resolvedKey != null) {
        if (resolvedKey.isParameterizedMethod()) {
          String[] typeArgRefs = resolvedKey.getTypeArguments();
          if (typeArgRefs.length > 0) {
            appendTypeArgumentSignaturesLabel(method, typeArgRefs, flags, builder);
            builder.append(' ');
          }
        } else {
          String[] typeParameterSigs = Signature.getTypeParameters(resolvedSig);
          if (typeParameterSigs.length > 0) {
            appendTypeParameterSignaturesLabel(typeParameterSigs, builder);
            builder.append(' ');
          }
        }
      } else if (method.exists()) {
        ITypeParameter[] typeParameters = method.getTypeParameters();
        if (typeParameters.length > 0) {
          appendTypeParametersLabels(typeParameters, flags, builder);
          builder.append(' ');
        }
      }

      if (!method.isConstructor()) {

        String returnTypeSig =
            resolvedSig != null ? Signature.getReturnType(resolvedSig) : method.getReturnType();
        appendTypeSignatureLabel(method, returnTypeSig, 0, builder);
        builder.append(' ');
        //
        // builder.append(Signature.toCharArray(method.getReturnType().toCharArray())).append(' ');
      }
      builder.append(method.getElementName());
      builder.append('(');
      for (ILocalVariable variable : method.getParameters()) {
        builder.append(Signature.toString(variable.getTypeSignature()));
        builder.append(' ').append(variable.getElementName()).append(", ");
      }

      if (builder.charAt(builder.length() - 1) == ' ') {
        builder.delete(builder.length() - 2, builder.length());
      }
      builder.append(')');
      String[] exceptionTypes = method.getExceptionTypes();
      if (exceptionTypes != null && exceptionTypes.length != 0) {
        builder.append(' ').append("throws ");
        for (String exceptionType : exceptionTypes) {
          builder.append(Signature.toCharArray(exceptionType.toCharArray())).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
      }
      if (type.isInterface() || type.isAnnotation()) {
        builder.append(";\n\n");
      } else {
        builder.append(" {").append(METHOD_BODY).append("}\n\n");
      }
    }
    for (IType iType : type.getTypes()) {
      generateType(iType, builder, indent + indent);
    }
    builder.append(indent.substring(TAB.length()));
    builder.append("}\n");
  }

  protected void appendAnnotationLabels(
      IAnnotation[] annotations, long flags, StringBuilder builder, String indent)
      throws JavaModelException {
    for (IAnnotation annotation : annotations) {
      builder.append(indent);
      appendAnnotationLabel(annotation, flags, builder);
      builder.append('\n');
    }
  }

  public void appendAnnotationLabel(IAnnotation annotation, long flags, StringBuilder builder)
      throws JavaModelException {
    builder.append('@');
    appendTypeSignatureLabel(
        annotation,
        Signature.createTypeSignature(annotation.getElementName(), false),
        flags,
        builder);
    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    if (memberValuePairs.length == 0) return;
    builder.append('(');
    for (int i = 0; i < memberValuePairs.length; i++) {
      if (i > 0) builder.append(JavaElementLabels.COMMA_STRING);
      IMemberValuePair memberValuePair = memberValuePairs[i];
      builder.append(
          getMemberName(annotation, annotation.getElementName(), memberValuePair.getMemberName()));
      builder.append('=');
      appendAnnotationValue(
          annotation, memberValuePair.getValue(), memberValuePair.getValueKind(), flags, builder);
    }
    builder.append(')');
  }

  /**
   * Returns the simple name of the given member.
   *
   * @param enclosingElement the enclosing element
   * @param typeName the name of the member's declaring type
   * @param memberName the name of the member
   * @return the simple name of the member
   */
  protected String getMemberName(
      IJavaElement enclosingElement, String typeName, String memberName) {
    return memberName;
  }

  private void appendAnnotationValue(
      IAnnotation annotation, Object value, int valueKind, long flags, StringBuilder builder)
      throws JavaModelException {
    // Note: To be bug-compatible with Javadoc from Java 5/6/7, we currently don't escape HTML tags
    // in String-valued annotations.
    if (value instanceof Object[]) {
      builder.append('{');
      Object[] values = (Object[]) value;
      for (int j = 0; j < values.length; j++) {
        if (j > 0) builder.append(JavaElementLabels.COMMA_STRING);
        value = values[j];
        appendAnnotationValue(annotation, value, valueKind, flags, builder);
      }
      builder.append('}');
    } else {
      switch (valueKind) {
        case IMemberValuePair.K_CLASS:
          appendTypeSignatureLabel(
              annotation, Signature.createTypeSignature((String) value, false), flags, builder);
          builder.append(".class"); // $NON-NLS-1$
          break;
        case IMemberValuePair.K_QUALIFIED_NAME:
          String name = (String) value;
          int lastDot = name.lastIndexOf('.');
          if (lastDot != -1) {
            String type = name.substring(0, lastDot);
            String field = name.substring(lastDot + 1);
            appendTypeSignatureLabel(
                annotation, Signature.createTypeSignature(type, false), flags, builder);
            builder.append('.');
            builder.append(getMemberName(annotation, type, field));
            break;
          }
          //				case IMemberValuePair.K_SIMPLE_NAME: // can't implement, since parent type is not
          // known
          // $FALL-THROUGH$
        case IMemberValuePair.K_ANNOTATION:
          appendAnnotationLabel((IAnnotation) value, flags, builder);
          break;
        case IMemberValuePair.K_STRING:
          builder.append(ASTNodes.getEscapedStringLiteral((String) value));
          break;
        case IMemberValuePair.K_CHAR:
          builder.append(ASTNodes.getEscapedCharacterLiteral(((Character) value).charValue()));
          break;
        default:
          builder.append(String.valueOf(value));
          break;
      }
    }
  }

  /**
   * Returns the string for rendering the {@link org.eclipse.jdt.core.IJavaElement#getElementName()
   * element name} of the given element.
   *
   * <p><strong>Note:</strong> This class only calls this helper for those elements where ( {@link
   * org.eclipse.che.jdt.javadoc.JavaElementLinks}) has the need to render the name differently.
   *
   * @param element the element to render
   * @return the string for rendering the element name
   */
  protected String getElementName(IJavaElement element) {
    return element.getElementName();
  }

  private void appendTypeParameterWithBounds(
      ITypeParameter typeParameter, long flags, StringBuilder builder) throws JavaModelException {
    builder.append(getElementName(typeParameter));

    if (typeParameter.exists()) {
      String[] bounds = typeParameter.getBoundsSignatures();
      if (bounds.length > 0
          && !(bounds.length == 1 && "Ljava.lang.Object;".equals(bounds[0]))) { // $NON-NLS-1$
        builder.append(" extends "); // $NON-NLS-1$
        for (int j = 0; j < bounds.length; j++) {
          if (j > 0) {
            builder.append(" & "); // $NON-NLS-1$
          }
          appendTypeSignatureLabel(typeParameter, bounds[j], flags, builder);
        }
      }
    }
  }

  /**
   * Appends labels for type parameters from type binding array.
   *
   * @param typeParameters the type parameters
   * @param flags flags with render options
   * @throws org.eclipse.jdt.core.JavaModelException ...
   */
  private void appendTypeParametersLabels(
      ITypeParameter[] typeParameters, long flags, StringBuilder builder)
      throws JavaModelException {
    if (typeParameters.length > 0) {
      builder.append(getLT());
      for (int i = 0; i < typeParameters.length; i++) {
        if (i > 0) {
          builder.append(JavaElementLabels.COMMA_STRING);
        }
        appendTypeParameterWithBounds(typeParameters[i], flags, builder);
      }
      builder.append(getGT());
    }
  }

  /**
   * Appends labels for type parameters from a signature.
   *
   * @param typeParamSigs the type parameter signature
   */
  private void appendTypeParameterSignaturesLabel(String[] typeParamSigs, StringBuilder builder) {
    if (typeParamSigs.length > 0) {
      builder.append(getLT());
      for (int i = 0; i < typeParamSigs.length; i++) {
        if (i > 0) {
          builder.append(JavaElementLabels.COMMA_STRING);
        }
        builder.append(Signature.getTypeVariable(typeParamSigs[i]));
      }
      builder.append(getGT());
    }
  }

  private void appendTypeArgumentSignaturesLabel(
      IJavaElement enclosingElement, String[] typeArgsSig, long flags, StringBuilder builder) {
    if (typeArgsSig.length > 0) {
      builder.append(getLT());
      for (int i = 0; i < typeArgsSig.length; i++) {
        if (i > 0) {
          builder.append(JavaElementLabels.COMMA_STRING);
        }
        appendTypeSignatureLabel(enclosingElement, typeArgsSig[i], flags, builder);
      }
      builder.append(getGT());
    }
  }

  protected void appendTypeSignatureLabel(
      IJavaElement enclosingElement, String typeSig, long flags, StringBuilder builder) {
    int sigKind = Signature.getTypeSignatureKind(typeSig);
    switch (sigKind) {
      case Signature.BASE_TYPE_SIGNATURE:
        builder.append(Signature.toString(typeSig));
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        appendTypeSignatureLabel(
            enclosingElement, Signature.getElementType(typeSig), flags, builder);
        for (int dim = Signature.getArrayCount(typeSig); dim > 0; dim--) {
          builder.append('[').append(']');
        }
        break;
      case Signature.CLASS_TYPE_SIGNATURE:
        String baseType = getSimpleTypeName(enclosingElement, typeSig);
        builder.append(baseType);

        String[] typeArguments = Signature.getTypeArguments(typeSig);
        appendTypeArgumentSignaturesLabel(enclosingElement, typeArguments, flags, builder);
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        builder.append(getSimpleTypeName(enclosingElement, typeSig));
        break;
      case Signature.WILDCARD_TYPE_SIGNATURE:
        char ch = typeSig.charAt(0);
        if (ch == Signature.C_STAR) { // workaround for bug 85713
          builder.append('?');
        } else {
          if (ch == Signature.C_EXTENDS) {
            builder.append("? extends "); // $NON-NLS-1$
            appendTypeSignatureLabel(enclosingElement, typeSig.substring(1), flags, builder);
          } else if (ch == Signature.C_SUPER) {
            builder.append("? super "); // $NON-NLS-1$
            appendTypeSignatureLabel(enclosingElement, typeSig.substring(1), flags, builder);
          }
        }
        break;
      case Signature.CAPTURE_TYPE_SIGNATURE:
        appendTypeSignatureLabel(enclosingElement, typeSig.substring(1), flags, builder);
        break;
      case Signature.INTERSECTION_TYPE_SIGNATURE:
        String[] typeBounds = Signature.getIntersectionTypeBounds(typeSig);
        appendTypeBoundsSignaturesLabel(enclosingElement, typeBounds, flags, builder);
        break;
      default:
        // unknown
    }
  }

  private void appendTypeBoundsSignaturesLabel(
      IJavaElement enclosingElement, String[] typeArgsSig, long flags, StringBuilder builder) {
    for (int i = 0; i < typeArgsSig.length; i++) {
      if (i > 0) {
        builder.append(" | "); // $NON-NLS-1$
      }
      appendTypeSignatureLabel(enclosingElement, typeArgsSig[i], flags, builder);
    }
  }

  /**
   * Returns the simple name of the given type signature.
   *
   * @param enclosingElement the enclosing element in which to resolve the signature
   * @param typeSig a {@link org.eclipse.jdt.core.Signature#CLASS_TYPE_SIGNATURE} or {@link
   *     org.eclipse.jdt.core .Signature#TYPE_VARIABLE_SIGNATURE}
   * @return the simple name of the given type signature
   */
  protected String getSimpleTypeName(IJavaElement enclosingElement, String typeSig) {
    return Signature.toString(Signature.getTypeErasure(typeSig));
  }

  /**
   * Returns the string for rendering the '<code>&lt;</code>' character.
   *
   * @return the string for rendering '<code>&lt;</code>'
   */
  protected String getLT() {
    return "<"; // $NON-NLS-1$
  }

  /**
   * Returns the string for rendering the '<code>&gt;</code>' character.
   *
   * @return the string for rendering '<code>&gt;</code>'
   */
  protected String getGT() {
    return ">"; // $NON-NLS-1$
  }

  private String getJavaType(IType type) throws JavaModelException {
    if (type.isAnnotation()) {
      return "@interface";
    }
    if (type.isClass()) {
      return "class";
    }

    if (type.isInterface()) {
      return "interface";
    }

    if (type.isEnum()) {
      return "enum";
    }

    return "can't determine type";
  }

  private String getModifiers(int flags, int typeFlags) {
    StringBuilder modifiers = new StringBuilder();
    // package private modifier has no string representation

    if (Flags.isPublic(flags)) {
      modifiers.append("public ");
    }

    if (Flags.isProtected(flags)) {
      modifiers.append("protected ");
    }

    if (Flags.isPrivate(flags)) {
      modifiers.append("private ");
    }

    if (Flags.isStatic(flags)) {
      modifiers.append("static ");
    }

    if (Flags.isAbstract(flags) && !Flags.isInterface(typeFlags)) {
      modifiers.append("abstract ");
    }

    if (Flags.isFinal(flags)) {
      modifiers.append("final ");
    }

    if (Flags.isNative(flags)) {
      modifiers.append("native ");
    }

    if (Flags.isSynchronized(flags)) {
      modifiers.append("synchronized ");
    }

    if (Flags.isVolatile(flags)) {
      modifiers.append("volatile ");
    }

    int len = modifiers.length();
    if (len == 0) return "";
    modifiers.setLength(len - 1);
    return modifiers.toString();
  }
}

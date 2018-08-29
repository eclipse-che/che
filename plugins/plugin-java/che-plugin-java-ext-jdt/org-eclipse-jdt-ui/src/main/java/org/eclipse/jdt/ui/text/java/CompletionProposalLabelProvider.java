/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

/**
 * Provides labels for java content assist proposals. The functionality is similar to the one
 * provided by {@link org.eclipse.jdt.ui.JavaElementLabels}, but based on signatures and {@link
 * org.eclipse.jdt.core.CompletionProposal}s.
 *
 * @see org.eclipse.jdt.core.Signature
 * @since 3.1
 */
public class CompletionProposalLabelProvider {

  private static final String QUALIFIER_SEPARATOR = JavaElementLabels.CONCAT_STRING;
  private static final String RETURN_TYPE_SEPARATOR = JavaElementLabels.DECL_STRING;
  private static final String VAR_TYPE_SEPARATOR = JavaElementLabels.DECL_STRING;

  /**
   * The completion context.
   *
   * @since 3.2
   */
  private CompletionContext fContext;

  /** Creates a new label provider. */
  public CompletionProposalLabelProvider() {}

  /**
   * Creates and returns a parameter list of the given method or type proposal suitable for display.
   * The list does not include parentheses. The lower bound of parameter types is returned.
   *
   * <p>Examples:
   *
   * <pre>
   *   &quot;void method(int i, String s)&quot; -&gt; &quot;int i, String s&quot;
   *   &quot;? extends Number method(java.lang.String s, ? super Number n)&quot; -&gt; &quot;String s, Number n&quot;
   * </pre>
   *
   * @param proposal the proposal to create the parameter list for
   * @return the list of comma-separated parameters suitable for display
   */
  public String createParameterList(CompletionProposal proposal) {
    String paramList;
    int kind = proposal.getKind();
    switch (kind) {
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
        paramList = appendUnboundedParameterList(new StyledString(), proposal).getString();
        return Strings.markJavaElementLabelLTR(paramList);
      case CompletionProposal.TYPE_REF:
      case CompletionProposal.JAVADOC_TYPE_REF:
        paramList = appendTypeParameterList(new StyledString(), proposal).getString();
        return Strings.markJavaElementLabelLTR(paramList);
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        paramList = appendUnboundedParameterList(new StyledString(), proposal).getString();
        return Strings.markJavaElementLabelLTR(paramList);
      default:
        Assert.isLegal(false);
        return null; // dummy
    }
  }

  /**
   * Appends the parameter list to <code>buffer</code>.
   *
   * @param buffer the buffer to append to
   * @param methodProposal the method proposal
   * @return the modified <code>buffer</code>
   */
  private StyledString appendUnboundedParameterList(
      StyledString buffer, CompletionProposal methodProposal) {
    // TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
    // gets fixed.
    char[] signature = SignatureUtil.fix83600(methodProposal.getSignature());
    char[][] parameterNames = methodProposal.findParameterNames(null);
    char[][] parameterTypes = Signature.getParameterTypes(signature);

    for (int i = 0; i < parameterTypes.length; i++)
      parameterTypes[i] = createTypeDisplayName(SignatureUtil.getLowerBound(parameterTypes[i]));

    if (Flags.isVarargs(methodProposal.getFlags())) {
      int index = parameterTypes.length - 1;
      parameterTypes[index] = convertToVararg(parameterTypes[index]);
    }
    return appendParameterSignature(buffer, parameterTypes, parameterNames);
  }

  /**
   * Appends the type parameter list to <code>buffer</code>.
   *
   * @param buffer the buffer to append to
   * @param typeProposal the type proposal
   * @return the modified <code>buffer</code>
   * @since 3.2
   */
  private StyledString appendTypeParameterList(
      StyledString buffer, CompletionProposal typeProposal) {
    // TODO remove once https://bugs.eclipse.org/bugs/show_bug.cgi?id=85293
    // gets fixed.
    char[] signature = SignatureUtil.fix83600(typeProposal.getSignature());
    char[][] typeParameters = Signature.getTypeArguments(signature);
    for (int i = 0; i < typeParameters.length; i++) {
      char[] param = typeParameters[i];
      typeParameters[i] = Signature.toCharArray(param);
    }
    return appendParameterSignature(buffer, typeParameters, null);
  }

  /**
   * Converts the display name for an array type into a variable arity display name.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"int[]" -> "int..."
   *   <li>"Object[][]" -> "Object[]..."
   *   <li>"String" -> "String"
   * </ul>
   *
   * <p>If <code>typeName</code> does not include the substring "[]", it is returned unchanged.
   *
   * @param typeName the type name to convert
   * @return the converted type name
   * @since 3.2
   */
  private char[] convertToVararg(char[] typeName) {
    if (typeName == null) return typeName;
    final int len = typeName.length;
    if (len < 2) return typeName;

    if (typeName[len - 1] != ']') return typeName;
    if (typeName[len - 2] != '[') return typeName;

    char[] vararg = new char[len + 1];
    System.arraycopy(typeName, 0, vararg, 0, len - 2);
    vararg[len - 2] = '.';
    vararg[len - 1] = '.';
    vararg[len] = '.';
    return vararg;
  }

  /**
   * Returns the display string for a java type signature.
   *
   * @param typeSignature the type signature to create a display name for
   * @return the display name for <code>typeSignature</code>
   * @throws IllegalArgumentException if <code>typeSignature</code> is not a valid signature
   * @see org.eclipse.jdt.core.Signature#toCharArray(char[])
   * @see org.eclipse.jdt.core.Signature#getSimpleName(char[])
   */
  private char[] createTypeDisplayName(char[] typeSignature) throws IllegalArgumentException {
    char[] displayName = Signature.getSimpleName(Signature.toCharArray(typeSignature));

    // XXX see https://bugs.eclipse.org/bugs/show_bug.cgi?id=84675
    boolean useShortGenerics = false;
    if (useShortGenerics) {
      StringBuffer buf = new StringBuffer();
      buf.append(displayName);
      int pos;
      do {
        pos = buf.indexOf("? extends "); // $NON-NLS-1$
        if (pos >= 0) {
          buf.replace(pos, pos + 10, "+"); // $NON-NLS-1$
        } else {
          pos = buf.indexOf("? super "); // $NON-NLS-1$
          if (pos >= 0) buf.replace(pos, pos + 8, "-"); // $NON-NLS-1$
        }
      } while (pos >= 0);
      return buf.toString().toCharArray();
    }
    return displayName;
  }

  /**
   * Creates a display string of a parameter list (without the parentheses) for the given parameter
   * types and names.
   *
   * @param buffer the string buffer
   * @param parameterTypes the parameter types
   * @param parameterNames the parameter names
   * @return the display string of the parameter list defined by the passed arguments
   */
  private final StyledString appendParameterSignature(
      StyledString buffer, char[][] parameterTypes, char[][] parameterNames) {
    if (parameterTypes != null) {
      for (int i = 0; i < parameterTypes.length; i++) {
        if (i > 0) {
          buffer.append(',');
          buffer.append(' ');
        }
        buffer.append(parameterTypes[i]);
        if (parameterNames != null && parameterNames[i] != null) {
          buffer.append(' ');
          buffer.append(parameterNames[i]);
        }
      }
    }
    return buffer;
  }

  /**
   * Creates a display label for the given method proposal. The display label consists of:
   *
   * <ul>
   *   <li>the method name
   *   <li>the parameter list (see {@link
   *       #createParameterList(org.eclipse.jdt.core.CompletionProposal)})
   *   <li>the upper bound of the return type (see {@link SignatureUtil#getUpperBound(String)})
   *   <li>the raw simple name of the declaring type
   * </ul>
   *
   * <p>Examples: For the <code>get(int)</code> method of a variable of type <code>
   * List<? extends Number></code>, the following display name is returned: <code>
   * get(int index)  Number - List</code>.<br>
   * For the <code>add(E)</code> method of a variable of type <code>List<? super Number></code>, the
   * following display name is returned: <code>add(Number o)  void - List</code>.<br>
   *
   * @param methodProposal the method proposal to display
   * @return the display label for the given method proposal
   */
  StyledString createMethodProposalLabel(CompletionProposal methodProposal) {
    StyledString nameBuffer = new StyledString();

    // method name
    nameBuffer.append(methodProposal.getName());

    // parameters
    nameBuffer.append('(');
    appendUnboundedParameterList(nameBuffer, methodProposal);
    nameBuffer.append(')');

    // return type
    if (!methodProposal.isConstructor()) {
      // TODO remove SignatureUtil.fix83600 call when bugs are fixed
      char[] returnType =
          createTypeDisplayName(
              SignatureUtil.getUpperBound(
                  Signature.getReturnType(SignatureUtil.fix83600(methodProposal.getSignature()))));
      nameBuffer.append(RETURN_TYPE_SEPARATOR);
      nameBuffer.append(returnType);
    }

    // declaring type
    nameBuffer.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
    String declaringType = extractDeclaringTypeFQN(methodProposal);

    if (methodProposal.getRequiredProposals() != null) {
      String qualifier = Signature.getQualifier(declaringType);
      if (qualifier.length() > 0) {
        nameBuffer.append(qualifier, StyledString.QUALIFIER_STYLER);
        nameBuffer.append('.', StyledString.QUALIFIER_STYLER);
      }
    }

    declaringType = Signature.getSimpleName(declaringType);
    nameBuffer.append(declaringType, StyledString.QUALIFIER_STYLER);
    return Strings.markJavaElementLabelLTR(nameBuffer);
  }

  /**
   * Creates a display label for the given method proposal. The display label consists of:
   *
   * <ul>
   *   <li>the method name
   *   <li>the raw simple name of the declaring type
   * </ul>
   *
   * <p>Examples: For the <code>get(int)</code> method of a variable of type <code>
   * List<? extends Number></code>, the following display name is returned <code>get(int) - List
   * </code>.<br>
   * For the <code>add(E)</code> method of a variable of type <code>List</code>, the following
   * display name is returned: <code>add(Object) - List</code>.<br>
   *
   * @param methodProposal the method proposal to display
   * @return the display label for the given method proposal
   * @since 3.2
   */
  StyledString createJavadocMethodProposalLabel(CompletionProposal methodProposal) {
    StyledString nameBuffer = new StyledString();

    // method name
    nameBuffer.append(methodProposal.getCompletion());

    // declaring type
    nameBuffer.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
    String declaringType = extractDeclaringTypeFQN(methodProposal);
    declaringType = Signature.getSimpleName(declaringType);
    nameBuffer.append(declaringType, StyledString.QUALIFIER_STYLER);

    return Strings.markJavaElementLabelLTR(nameBuffer);
  }

  StyledString createOverrideMethodProposalLabel(CompletionProposal methodProposal) {
    StyledString nameBuffer = new StyledString();

    // method name
    nameBuffer.append(methodProposal.getName());

    // parameters
    nameBuffer.append('(');
    appendUnboundedParameterList(nameBuffer, methodProposal);
    nameBuffer.append(')');

    nameBuffer.append(RETURN_TYPE_SEPARATOR);

    // return type
    // TODO remove SignatureUtil.fix83600 call when bugs are fixed
    char[] returnType =
        createTypeDisplayName(
            SignatureUtil.getUpperBound(
                Signature.getReturnType(SignatureUtil.fix83600(methodProposal.getSignature()))));
    nameBuffer.append(returnType);

    // declaring type
    nameBuffer.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);

    String declaringType = extractDeclaringTypeFQN(methodProposal);
    declaringType = Signature.getSimpleName(declaringType);
    nameBuffer.append(
        Messages.format(
            JavaTextMessages.ResultCollector_overridingmethod,
            BasicElementLabels.getJavaElementName(declaringType)),
        StyledString.QUALIFIER_STYLER);

    return nameBuffer;
  }

  /**
   * Extracts the fully qualified name of the declaring type of a method reference.
   *
   * @param methodProposal a proposed method
   * @return the qualified name of the declaring type
   */
  private String extractDeclaringTypeFQN(CompletionProposal methodProposal) {
    char[] declaringTypeSignature = methodProposal.getDeclarationSignature();
    // special methods may not have a declaring type: methods defined on arrays etc.
    // TODO remove when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=84690 gets fixed
    if (declaringTypeSignature == null) return "java.lang.Object"; // $NON-NLS-1$
    return SignatureUtil.stripSignatureToFQN(String.valueOf(declaringTypeSignature));
  }

  /**
   * Creates a display label for a given type proposal. The display label consists of:
   *
   * <ul>
   *   <li>the simple type name (erased when the context is in javadoc)
   *   <li>the package name
   * </ul>
   *
   * <p>Examples: A proposal for the generic type <code>java.util.List&lt;E&gt;</code>, the display
   * label is: <code>List<E> - java.util</code>.
   *
   * @param typeProposal the method proposal to display
   * @return the display label for the given type proposal
   */
  StyledString createTypeProposalLabel(CompletionProposal typeProposal) {
    char[] signature;
    if (fContext != null && fContext.isInJavadoc())
      signature = Signature.getTypeErasure(typeProposal.getSignature());
    else signature = typeProposal.getSignature();
    char[] fullName = Signature.toCharArray(signature);
    return createTypeProposalLabel(fullName);
  }

  StyledString createJavadocTypeProposalLabel(CompletionProposal typeProposal) {
    char[] fullName = Signature.toCharArray(typeProposal.getSignature());
    return createJavadocTypeProposalLabel(fullName);
  }

  StyledString createJavadocSimpleProposalLabel(CompletionProposal proposal) {
    // TODO get rid of this
    return createSimpleLabel(proposal);
  }

  StyledString createTypeProposalLabel(char[] fullName) {
    // only display innermost type name as type name, using any
    // enclosing types as qualification
    int qIndex = findSimpleNameStart(fullName);

    StyledString buf = new StyledString();
    buf.append(new String(fullName, qIndex, fullName.length - qIndex));
    if (qIndex > 0) {
      buf.append(JavaElementLabels.CONCAT_STRING, StyledString.QUALIFIER_STYLER);
      buf.append(new String(fullName, 0, qIndex - 1), StyledString.QUALIFIER_STYLER);
    }
    return Strings.markJavaElementLabelLTR(buf);
  }

  StyledString createJavadocTypeProposalLabel(char[] fullName) {
    // only display innermost type name as type name, using any
    // enclosing types as qualification
    int qIndex = findSimpleNameStart(fullName);

    StyledString buf = new StyledString("{@link "); // $NON-NLS-1$
    buf.append(new String(fullName, qIndex, fullName.length - qIndex));
    buf.append('}');
    if (qIndex > 0) {
      buf.append(JavaElementLabels.CONCAT_STRING, StyledString.QUALIFIER_STYLER);
      buf.append(new String(fullName, 0, qIndex - 1), StyledString.QUALIFIER_STYLER);
    }
    return Strings.markJavaElementLabelLTR(buf);
  }

  private int findSimpleNameStart(char[] array) {
    int lastDot = 0;
    for (int i = 0, len = array.length; i < len; i++) {
      char ch = array[i];
      if (ch == '<') {
        return lastDot;
      } else if (ch == '.') {
        lastDot = i + 1;
      }
    }
    return lastDot;
  }

  StyledString createSimpleLabelWithType(CompletionProposal proposal) {
    StyledString buf = new StyledString();
    buf.append(proposal.getCompletion());
    char[] typeName = Signature.getSignatureSimpleName(proposal.getSignature());
    if (typeName.length > 0) {
      buf.append(VAR_TYPE_SEPARATOR);
      buf.append(typeName);
    }
    return Strings.markJavaElementLabelLTR(buf);
  }

  /**
   * Returns whether the given string starts with "this.".
   *
   * @param string string to test
   * @return <code>true</code> if the given string starts with "this."
   * @since 3.3
   */
  private boolean isThisPrefix(char[] string) {
    if (string == null || string.length < 5) return false;
    return string[0] == 't'
        && string[1] == 'h'
        && string[2] == 'i'
        && string[3] == 's'
        && string[4] == '.';
  }

  StyledString createLabelWithTypeAndDeclaration(CompletionProposal proposal) {
    char[] name = proposal.getCompletion();
    if (!isThisPrefix(name)) name = proposal.getName();

    StyledString buf = new StyledString();
    buf.append(name);
    char[] typeName = Signature.getSignatureSimpleName(proposal.getSignature());
    if (typeName.length > 0) {
      buf.append(VAR_TYPE_SEPARATOR);
      buf.append(typeName);
    }
    char[] declaration = proposal.getDeclarationSignature();
    if (declaration != null) {
      declaration = Signature.getSignatureSimpleName(declaration);
      if (declaration.length > 0) {
        buf.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
        if (proposal.getRequiredProposals() != null) {
          String declaringType = extractDeclaringTypeFQN(proposal);
          String qualifier = Signature.getQualifier(declaringType);
          if (qualifier.length() > 0) {
            buf.append(qualifier, StyledString.QUALIFIER_STYLER);
            buf.append('.', StyledString.QUALIFIER_STYLER);
          }
        }
        buf.append(declaration, StyledString.QUALIFIER_STYLER);
      }
    }

    return Strings.markJavaElementLabelLTR(buf);
  }

  StyledString createPackageProposalLabel(CompletionProposal proposal) {
    Assert.isTrue(proposal.getKind() == CompletionProposal.PACKAGE_REF);
    return Strings.markJavaElementLabelLTR(
        new StyledString(String.valueOf(proposal.getDeclarationSignature())));
  }

  StyledString createSimpleLabel(CompletionProposal proposal) {
    return Strings.markJavaElementLabelLTR(
        new StyledString(String.valueOf(proposal.getCompletion())));
  }

  StyledString createAnonymousTypeLabel(CompletionProposal proposal) {
    char[] declaringTypeSignature = proposal.getDeclarationSignature();
    declaringTypeSignature = Signature.getTypeErasure(declaringTypeSignature);

    StyledString buffer = new StyledString();
    buffer.append(Signature.getSignatureSimpleName(declaringTypeSignature));
    buffer.append('(');
    appendUnboundedParameterList(buffer, proposal);
    buffer.append(')');
    buffer.append("  "); // $NON-NLS-1$
    buffer.append(JavaTextMessages.ResultCollector_anonymous_type);

    if (proposal.getRequiredProposals() != null) {
      char[] signatureQualifier = Signature.getSignatureQualifier(declaringTypeSignature);
      if (signatureQualifier.length > 0) {
        buffer.append(JavaElementLabels.CONCAT_STRING, StyledString.QUALIFIER_STYLER);
        buffer.append(signatureQualifier, StyledString.QUALIFIER_STYLER);
      }
    }

    return Strings.markJavaElementLabelLTR(buffer);
  }

  /**
   * Creates the display label for a given <code>CompletionProposal</code>.
   *
   * @param proposal the completion proposal to create the display label for
   * @return the display label for <code>proposal</code>
   */
  public String createLabel(CompletionProposal proposal) {
    return createStyledLabel(proposal).getString();
  }

  /**
   * Creates a display label with styles for a given <code>CompletionProposal</code>.
   *
   * @param proposal the completion proposal to create the display label for
   * @return the display label for <code>proposal</code>
   * @since 3.4
   */
  public StyledString createStyledLabel(CompletionProposal proposal) {
    switch (proposal.getKind()) {
      case CompletionProposal.METHOD_NAME_REFERENCE:
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
      case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
      case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
        if (fContext != null && fContext.isInJavadoc())
          return createJavadocMethodProposalLabel(proposal);
        return createMethodProposalLabel(proposal);
      case CompletionProposal.METHOD_DECLARATION:
        return createOverrideMethodProposalLabel(proposal);
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        return createAnonymousTypeLabel(proposal);
      case CompletionProposal.TYPE_REF:
        return createTypeProposalLabel(proposal);
      case CompletionProposal.JAVADOC_TYPE_REF:
        return createJavadocTypeProposalLabel(proposal);
      case CompletionProposal.JAVADOC_FIELD_REF:
      case CompletionProposal.JAVADOC_VALUE_REF:
      case CompletionProposal.JAVADOC_BLOCK_TAG:
      case CompletionProposal.JAVADOC_INLINE_TAG:
      case CompletionProposal.JAVADOC_PARAM_REF:
        return createJavadocSimpleProposalLabel(proposal);
      case CompletionProposal.JAVADOC_METHOD_REF:
        return createJavadocMethodProposalLabel(proposal);
      case CompletionProposal.PACKAGE_REF:
        return createPackageProposalLabel(proposal);
      case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        return createLabelWithTypeAndDeclaration(proposal);
      case CompletionProposal.LOCAL_VARIABLE_REF:
      case CompletionProposal.VARIABLE_DECLARATION:
        return createSimpleLabelWithType(proposal);
      case CompletionProposal.KEYWORD:
      case CompletionProposal.LABEL_REF:
        return createSimpleLabel(proposal);
      default:
        Assert.isTrue(false);
        return null;
    }
  }

  /**
   * Creates and returns a decorated image descriptor for a completion proposal.
   *
   * @param proposal the proposal for which to create an image descriptor
   * @return the created image descriptor, or <code>null</code> if no image is available
   */
  public ImageDescriptor createImageDescriptor(CompletionProposal proposal) {
    final int flags = proposal.getFlags();

    ImageDescriptor descriptor;
    switch (proposal.getKind()) {
      case CompletionProposal.METHOD_DECLARATION:
      case CompletionProposal.METHOD_NAME_REFERENCE:
      case CompletionProposal.METHOD_REF:
      case CompletionProposal.CONSTRUCTOR_INVOCATION:
      case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
      case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
      case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
      case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
        descriptor = JavaElementImageProvider.getMethodImageDescriptor(false, flags);
        break;
      case CompletionProposal.TYPE_REF:
        switch (Signature.getTypeSignatureKind(proposal.getSignature())) {
          case Signature.CLASS_TYPE_SIGNATURE:
            descriptor =
                JavaElementImageProvider.getTypeImageDescriptor(false, false, flags, false);
            break;
          case Signature.TYPE_VARIABLE_SIGNATURE:
            descriptor = JavaPluginImages.DESC_OBJS_TYPEVARIABLE;
            break;
          default:
            descriptor = null;
        }
        break;
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        descriptor = JavaElementImageProvider.getFieldImageDescriptor(false, flags);
        break;
      case CompletionProposal.LOCAL_VARIABLE_REF:
      case CompletionProposal.VARIABLE_DECLARATION:
        descriptor = JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE;
        break;
      case CompletionProposal.PACKAGE_REF:
        descriptor = JavaPluginImages.DESC_OBJS_PACKAGE;
        break;
      case CompletionProposal.KEYWORD:
      case CompletionProposal.LABEL_REF:
        descriptor = null;
        break;
      case CompletionProposal.JAVADOC_METHOD_REF:
      case CompletionProposal.JAVADOC_TYPE_REF:
      case CompletionProposal.JAVADOC_FIELD_REF:
      case CompletionProposal.JAVADOC_VALUE_REF:
      case CompletionProposal.JAVADOC_BLOCK_TAG:
      case CompletionProposal.JAVADOC_INLINE_TAG:
      case CompletionProposal.JAVADOC_PARAM_REF:
        descriptor = JavaPluginImages.DESC_OBJS_JAVADOCTAG;
        break;
      default:
        descriptor = null;
        Assert.isTrue(false);
    }

    if (descriptor == null) return null;
    return decorateImageDescriptor(descriptor, proposal);
  }

  ImageDescriptor createMethodImageDescriptor(CompletionProposal proposal) {
    final int flags = proposal.getFlags();
    return decorateImageDescriptor(
        JavaElementImageProvider.getMethodImageDescriptor(false, flags), proposal);
  }

  ImageDescriptor createTypeImageDescriptor(CompletionProposal proposal) {
    final int flags = proposal.getFlags();
    boolean isInterfaceOrAnnotation = Flags.isInterface(flags) || Flags.isAnnotation(flags);
    return decorateImageDescriptor(
        JavaElementImageProvider.getTypeImageDescriptor(
            true /* in order to get all visibility decorations */,
            isInterfaceOrAnnotation,
            flags,
            false),
        proposal);
  }

  ImageDescriptor createFieldImageDescriptor(CompletionProposal proposal) {
    final int flags = proposal.getFlags();
    return decorateImageDescriptor(
        JavaElementImageProvider.getFieldImageDescriptor(false, flags), proposal);
  }

  ImageDescriptor createLocalImageDescriptor(CompletionProposal proposal) {
    return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE, proposal);
  }

  ImageDescriptor createPackageImageDescriptor(CompletionProposal proposal) {
    return decorateImageDescriptor(JavaPluginImages.DESC_OBJS_PACKAGE, proposal);
  }

  /**
   * Returns a version of <code>descriptor</code> decorated according to the passed <code>modifier
   * </code> flags.
   *
   * @param descriptor the image descriptor to decorate
   * @param proposal the proposal
   * @return an image descriptor for a method proposal
   * @see org.eclipse.jdt.core.Flags
   */
  private ImageDescriptor decorateImageDescriptor(
      ImageDescriptor descriptor, CompletionProposal proposal) {
    int adornments = 0;
    int flags = proposal.getFlags();
    int kind = proposal.getKind();

    boolean deprecated = Flags.isDeprecated(flags);
    if (!deprecated) {
      CompletionProposal[] requiredProposals = proposal.getRequiredProposals();
      if (requiredProposals != null) {
        for (int i = 0; i < requiredProposals.length; i++) {
          CompletionProposal requiredProposal = requiredProposals[i];
          if (requiredProposal.getKind() == CompletionProposal.TYPE_REF) {
            deprecated |= Flags.isDeprecated(requiredProposal.getFlags());
          }
        }
      }
    }
    if (deprecated) adornments |= JavaElementImageDescriptor.DEPRECATED;

    if (kind == CompletionProposal.FIELD_REF
        || kind == CompletionProposal.METHOD_DECLARATION
        || kind == CompletionProposal.METHOD_NAME_REFERENCE
        || kind == CompletionProposal.METHOD_REF
        || kind == CompletionProposal.CONSTRUCTOR_INVOCATION)
      if (Flags.isStatic(flags)) adornments |= JavaElementImageDescriptor.STATIC;

    if (kind == CompletionProposal.METHOD_DECLARATION
        || kind == CompletionProposal.METHOD_NAME_REFERENCE
        || kind == CompletionProposal.METHOD_REF
        || kind == CompletionProposal.CONSTRUCTOR_INVOCATION)
      if (Flags.isSynchronized(flags)) adornments |= JavaElementImageDescriptor.SYNCHRONIZED;
    if (kind == CompletionProposal.METHOD_DECLARATION
        || kind == CompletionProposal.METHOD_NAME_REFERENCE
        || kind == CompletionProposal.METHOD_REF)
      if (Flags.isDefaultMethod(flags)) adornments |= JavaElementImageDescriptor.DEFAULT_METHOD;
    if (kind == CompletionProposal.ANNOTATION_ATTRIBUTE_REF)
      if (Flags.isAnnnotationDefault(flags))
        adornments |= JavaElementImageDescriptor.ANNOTATION_DEFAULT;

    if (kind == CompletionProposal.TYPE_REF && Flags.isAbstract(flags) && !Flags.isInterface(flags))
      adornments |= JavaElementImageDescriptor.ABSTRACT;

    if (kind == CompletionProposal.FIELD_REF) {
      if (Flags.isFinal(flags)) adornments |= JavaElementImageDescriptor.FINAL;
      if (Flags.isTransient(flags)) adornments |= JavaElementImageDescriptor.TRANSIENT;
      if (Flags.isVolatile(flags)) adornments |= JavaElementImageDescriptor.VOLATILE;
    }

    return new JavaElementImageDescriptor(
        descriptor, adornments /*, JavaElementImageProvider.SMALL_SIZE*/);
  }

  /**
   * Sets the completion context.
   *
   * @param context the completion context
   * @since 3.2
   */
  void setContext(CompletionContext context) {
    fContext = context;
  }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.template.contentassist.PositionBasedCompletionProposal;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;

/**
 * This class triggers a code-completion that will track all local and member variables for later
 * use as a parameter guessing proposal.
 */
public class ParameterGuesser {

  private static final class Variable {

    /**
     * Variable type. Used to choose the best guess based on scope (Local beats instance beats
     * inherited).
     */
    public static final int LOCAL = 0;

    public static final int FIELD = 1;
    public static final int INHERITED_FIELD = 2;
    public static final int METHOD = 3;
    public static final int INHERITED_METHOD = 4;
    public static final int LITERALS = 5;

    public final String qualifiedTypeName;
    public final String name;
    public final int variableType;
    public final int positionScore;

    public final boolean isAutoboxingMatch;

    public final char[] triggerChars;
    public final ImageDescriptor descriptor;

    public boolean alreadyMatched;

    public Variable(
        String qualifiedTypeName,
        String name,
        int variableType,
        boolean isAutoboxMatch,
        int positionScore,
        char[] triggerChars,
        ImageDescriptor descriptor) {
      this.qualifiedTypeName = qualifiedTypeName;
      this.name = name;
      this.variableType = variableType;
      this.positionScore = positionScore;
      this.triggerChars = triggerChars;
      this.descriptor = descriptor;
      this.isAutoboxingMatch = isAutoboxMatch;
      this.alreadyMatched = false;
    }

    /*
     * @see Object#toString()
     */
    @Override
    public String toString() {

      StringBuffer buffer = new StringBuffer();
      buffer.append(qualifiedTypeName);
      buffer.append(' ');
      buffer.append(name);
      buffer.append(" ("); // $NON-NLS-1$
      buffer.append(variableType);
      buffer.append(')');

      return buffer.toString();
    }
  }

  private static final char[] NO_TRIGGERS = new char[0];

  private final Set<String> fAlreadyMatchedNames;
  private final IJavaElement fEnclosingElement;

  /**
   * Creates a parameter guesser
   *
   * @param enclosingElement the enclosing Java element
   */
  public ParameterGuesser(IJavaElement enclosingElement) {
    fEnclosingElement = enclosingElement;
    fAlreadyMatchedNames = new HashSet<String>();
  }

  private List<Variable> evaluateVisibleMatches(String expectedType, IJavaElement[] suggestions)
      throws JavaModelException {
    IType currentType = null;
    if (fEnclosingElement != null) {
      currentType = (IType) fEnclosingElement.getAncestor(IJavaElement.TYPE);
    }

    ArrayList<Variable> res = new ArrayList<Variable>();
    for (int i = 0; i < suggestions.length; i++) {
      Variable variable = createVariable(suggestions[i], currentType, expectedType, i);
      if (variable != null) {
        if (fAlreadyMatchedNames.contains(variable.name)) {
          variable.alreadyMatched = true;
        }
        res.add(variable);
      }
    }

    // add 'this'
    if (currentType != null
        && !(fEnclosingElement instanceof IMethod
            && Flags.isStatic(((IMethod) fEnclosingElement).getFlags()))) {
      String fullyQualifiedName = currentType.getFullyQualifiedName('.');
      if (fullyQualifiedName.equals(expectedType)) {
        ImageDescriptor desc =
            new JavaElementImageDescriptor(
                JavaPluginImages.DESC_FIELD_PUBLIC,
                JavaElementImageDescriptor.FINAL | JavaElementImageDescriptor.STATIC
                /* JavaElementImageProvider.SMALL_SIZE*/ );
        res.add(
            new Variable(
                fullyQualifiedName,
                "this",
                Variable.LITERALS,
                false,
                res.size(),
                new char[] {'.'},
                desc)); // $NON-NLS-1$
      }
    }

    Code primitiveTypeCode = getPrimitiveTypeCode(expectedType);
    if (primitiveTypeCode == null) {
      // add 'null'
      res.add(
          new Variable(
              expectedType,
              "null",
              Variable.LITERALS,
              false,
              res.size(),
              NO_TRIGGERS,
              null)); // $NON-NLS-1$
    } else {
      String typeName = primitiveTypeCode.toString();
      boolean isAutoboxing = !typeName.equals(expectedType);
      if (primitiveTypeCode == PrimitiveType.BOOLEAN) {
        // add 'true', 'false'
        res.add(
            new Variable(
                typeName,
                "true",
                Variable.LITERALS,
                isAutoboxing,
                res.size(),
                NO_TRIGGERS,
                null)); // $NON-NLS-1$
        res.add(
            new Variable(
                typeName,
                "false",
                Variable.LITERALS,
                isAutoboxing,
                res.size(),
                NO_TRIGGERS,
                null)); // $NON-NLS-1$
      } else {
        // add 0
        res.add(
            new Variable(
                typeName,
                "0",
                Variable.LITERALS,
                isAutoboxing,
                res.size(),
                NO_TRIGGERS,
                null)); // $NON-NLS-1$
      }
    }
    return res;
  }

  public Variable createVariable(
      IJavaElement element, IType enclosingType, String expectedType, int positionScore)
      throws JavaModelException {
    int variableType;
    int elementType = element.getElementType();
    String elementName = element.getElementName();

    String typeSignature;
    switch (elementType) {
      case IJavaElement.FIELD:
        {
          IField field = (IField) element;
          if (field.getDeclaringType().equals(enclosingType)) {
            variableType = Variable.FIELD;
          } else {
            variableType = Variable.INHERITED_FIELD;
          }
          if (field.isResolved()) {
            typeSignature = new BindingKey(field.getKey()).toSignature();
          } else {
            typeSignature = field.getTypeSignature();
          }
          break;
        }
      case IJavaElement.LOCAL_VARIABLE:
        {
          ILocalVariable locVar = (ILocalVariable) element;
          variableType = Variable.LOCAL;
          typeSignature = locVar.getTypeSignature();
          break;
        }
      case IJavaElement.METHOD:
        {
          IMethod method = (IMethod) element;
          if (isMethodToSuggest(method)) {
            if (method.getDeclaringType().equals(enclosingType)) {
              variableType = Variable.METHOD;
            } else {
              variableType = Variable.INHERITED_METHOD;
            }
            if (method.isResolved()) {
              typeSignature =
                  Signature.getReturnType(new BindingKey(method.getKey()).toSignature());
            } else {
              typeSignature = method.getReturnType();
            }
            elementName = elementName + "()"; // $NON-NLS-1$
          } else {
            return null;
          }
          break;
        }
      default:
        return null;
    }
    String type = Signature.toString(typeSignature);

    boolean isAutoboxMatch = isPrimitiveType(expectedType) != isPrimitiveType(type);
    return new Variable(
        type,
        elementName,
        variableType,
        isAutoboxMatch,
        positionScore,
        NO_TRIGGERS,
        getImageDescriptor(element));
  }

  private ImageDescriptor getImageDescriptor(IJavaElement elem) {
    JavaElementImageProvider imageProvider = new JavaElementImageProvider();
    ImageDescriptor desc =
        imageProvider.getBaseImageDescriptor(elem, JavaElementImageProvider.OVERLAY_ICONS);
    imageProvider.dispose();
    return desc;
  }

  private boolean isPrimitiveType(String type) {
    return PrimitiveType.toCode(type) != null;
  }

  private PrimitiveType.Code getPrimitiveTypeCode(String type) {
    PrimitiveType.Code code = PrimitiveType.toCode(type);
    if (code != null) {
      return code;
    }
    if (fEnclosingElement != null
        && JavaModelUtil.is50OrHigher(fEnclosingElement.getJavaProject())) {
      if (code == PrimitiveType.SHORT) {
        if ("java.lang.Short".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.INT) {
        if ("java.lang.Integer".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.LONG) {
        if ("java.lang.Long".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.FLOAT) {
        if ("java.lang.Float".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.DOUBLE) {
        if ("java.lang.Double".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.CHAR) {
        if ("java.lang.Character".equals(type)) { // $NON-NLS-1$
          return code;
        }
      } else if (code == PrimitiveType.BYTE) {
        if ("java.lang.Byte".equals(type)) { // $NON-NLS-1$
          return code;
        }
      }
    }
    return null;
  }

  private boolean isMethodToSuggest(IMethod method) {
    try {
      String methodName = method.getElementName();
      return method.getNumberOfParameters() == 0
          && !Signature.SIG_VOID.equals(method.getReturnType())
          && (methodName.startsWith("get")
              || methodName.startsWith("is")); // $NON-NLS-1$//$NON-NLS-2$
    } catch (JavaModelException e) {
      return false;
    }
  }

  /**
   * Returns the matches for the type and name argument, ordered by match quality.
   *
   * @param expectedType - the qualified type of the parameter we are trying to match
   * @param paramName - the name of the parameter (used to find similarly named matches)
   * @param pos the position
   * @param suggestions the suggestions or <code>null</code>
   * @param fillBestGuess <code>true</code> if the best guess should be filled in
   * @param isLastParameter <code>true</code> iff this proposal is for the last parameter of a
   *     method
   * @return returns the name of the best match, or <code>null</code> if no match found
   * @throws org.eclipse.jdt.core.JavaModelException if it fails
   */
  public ICompletionProposal[] parameterProposals(
      String expectedType,
      String paramName,
      Position pos,
      IJavaElement[] suggestions,
      boolean fillBestGuess,
      boolean isLastParameter)
      throws JavaModelException {
    List<Variable> typeMatches = evaluateVisibleMatches(expectedType, suggestions);
    orderMatches(typeMatches, paramName);

    boolean hasVarWithParamName = false;
    ICompletionProposal[] ret = new ICompletionProposal[typeMatches.size()];
    int i = 0;
    int replacementLength = 0;
    for (Iterator<Variable> it = typeMatches.iterator(); it.hasNext(); ) {
      Variable v = it.next();
      if (i == 0) {
        fAlreadyMatchedNames.add(v.name);
        replacementLength = v.name.length();
      }

      String displayString = v.name;
      hasVarWithParamName |= displayString.equals(paramName);

      final char[] triggers;
      if (isLastParameter) {
        triggers = v.triggerChars;
      } else {
        triggers = new char[v.triggerChars.length + 1];
        System.arraycopy(v.triggerChars, 0, triggers, 0, v.triggerChars.length);
        triggers[triggers.length - 1] = ',';
      }

      ret[i++] =
          new PositionBasedCompletionProposal(
              v.name,
              pos,
              replacementLength,
              getImage(v.descriptor),
              displayString,
              null,
              null,
              triggers);
    }
    if (!fillBestGuess && !hasVarWithParamName) {
      // insert a proposal with the argument name
      ICompletionProposal[] extended = new ICompletionProposal[ret.length + 1];
      System.arraycopy(ret, 0, extended, 1, ret.length);
      extended[0] =
          new PositionBasedCompletionProposal(
              paramName,
              pos,
              replacementLength,
              null,
              paramName,
              null,
              null,
              isLastParameter ? null : new char[] {','});
      return extended;
    }
    return ret;
  }

  private static class MatchComparator implements Comparator<Variable> {

    private String fParamName;

    MatchComparator(String paramName) {
      fParamName = paramName;
    }

    public int compare(Variable one, Variable two) {
      return score(two) - score(one);
    }

    /**
     * The four order criteria as described below - put already used into bit 10, all others into
     * bits 0-9, 11-20, 21-30; 31 is sign - always 0
     *
     * @param v the variable
     * @return the score for <code>v</code>
     */
    private int score(Variable v) {
      int variableScore = 100 - v.variableType; // since these are increasing with distance
      int subStringScore = getLongestCommonSubstring(v.name, fParamName).length();
      // substring scores under 60% are not considered
      // this prevents marginal matches like a - ba and false - isBool that will
      // destroy the sort order
      int shorter = Math.min(v.name.length(), fParamName.length());
      if (subStringScore < 0.6 * shorter) subStringScore = 0;

      int positionScore = v.positionScore; // since ???
      int matchedScore = v.alreadyMatched ? 0 : 1;
      int autoboxingScore = v.isAutoboxingMatch ? 0 : 1;

      int score =
          autoboxingScore << 30
              | variableScore << 21
              | subStringScore << 11
              | matchedScore << 10
              | positionScore;
      return score;
    }
  }

  /**
   * Determine the best match of all possible type matches. The input into this method is all
   * possible completions that match the type of the argument. The purpose of this method is to
   * choose among them based on the following simple rules:
   *
   * <p>1) Local Variables > Instance/Class Variables > Inherited Instance/Class Variables
   *
   * <p>2) A longer case insensitive substring match will prevail
   *
   * <p>3) Variables that have not been used already during this completion will prevail over those
   * that have already been used (this avoids the same String/int/char from being passed in for
   * multiple arguments)
   *
   * <p>4) A better source position score will prevail (the declaration point of the variable, or
   * "how close to the point of completion?"
   *
   * @param typeMatches the list of type matches
   * @param paramName the parameter name
   */
  private static void orderMatches(List<Variable> typeMatches, String paramName) {
    if (typeMatches != null) Collections.sort(typeMatches, new MatchComparator(paramName));
  }

  /**
   * Returns the longest common substring of two strings.
   *
   * @param first the first string
   * @param second the second string
   * @return the longest common substring
   */
  private static String getLongestCommonSubstring(String first, String second) {

    String shorter = (first.length() <= second.length()) ? first : second;
    String longer = shorter == first ? second : first;

    int minLength = shorter.length();

    StringBuffer pattern = new StringBuffer(shorter.length() + 2);
    String longestCommonSubstring = ""; // $NON-NLS-1$

    for (int i = 0; i < minLength; i++) {
      for (int j = i + 1; j <= minLength; j++) {
        if (j - i < longestCommonSubstring.length()) continue;

        String substring = shorter.substring(i, j);
        pattern.setLength(0);
        pattern.append('*');
        pattern.append(substring);
        pattern.append('*');

        StringMatcher matcher = new StringMatcher(pattern.toString(), true, false);
        if (matcher.match(longer)) longestCommonSubstring = substring;
      }
    }

    return longestCommonSubstring;
  }

  private Image getImage(ImageDescriptor descriptor) {
    return (descriptor == null) ? null : JavaPlugin.getImageDescriptorRegistry().get(descriptor);
  }
}

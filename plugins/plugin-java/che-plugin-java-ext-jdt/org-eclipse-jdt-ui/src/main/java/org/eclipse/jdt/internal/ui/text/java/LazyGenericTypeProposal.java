/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedModeModelImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.LinkedPositionGroupImpl;
import static org.eclipse.che.plugin.java.server.dto.DtoServerImpls.RegionImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.jdt.javaeditor.HasLinkedModel;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.che.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Proposal for generic types.
 *
 * <p>Only used when compliance is set to 5.0 or higher.
 */
public class LazyGenericTypeProposal extends LazyJavaTypeCompletionProposal
    implements HasLinkedModel {
  /** Triggers for types. Do not modify. */
  private static final char[] GENERIC_TYPE_TRIGGERS = new char[] {'.', '\t', '[', '(', '<', ' '};

  private LinkedModeModelImpl linkedModel;

  /**
   * Short-lived context information object for generic types. Currently, these are only created
   * after inserting a type proposal, as core doesn't give us the correct type proposal from within
   * SomeType<|>.
   */
  private static class ContextInformation
      implements IContextInformation, IContextInformationExtension {
    private final String fInformationDisplayString;
    private final String fContextDisplayString;
    private final Image fImage;
    private final int fPosition;

    ContextInformation(LazyGenericTypeProposal proposal) {
      // don't cache the proposal as content assistant
      // might hang on to the context info
      fContextDisplayString = proposal.getDisplayString();
      fInformationDisplayString = computeContextString(proposal);
      fImage = proposal.getImage();
      fPosition =
          proposal.getReplacementOffset() + proposal.getReplacementString().indexOf('<') + 1;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.IContextInformation#getContextDisplayString()
     */
    public String getContextDisplayString() {
      return fContextDisplayString;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.IContextInformation#getImage()
     */
    public Image getImage() {
      return fImage;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.IContextInformation#getInformationDisplayString()
     */
    public String getInformationDisplayString() {
      return fInformationDisplayString;
    }

    private String computeContextString(LazyGenericTypeProposal proposal) {
      try {
        TypeArgumentProposal[] proposals = proposal.computeTypeArgumentProposals();
        if (proposals.length == 0) return null;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < proposals.length; i++) {
          buf.append(proposals[i].getDisplayName());
          if (i < proposals.length - 1) buf.append(", "); // $NON-NLS-1$
        }
        return Strings.markJavaElementLabelLTR(buf.toString());

      } catch (JavaModelException e) {
        return null;
      }
    }

    /*
     * @see org.eclipse.jface.text.contentassist.IContextInformationExtension#getContextInformationPosition()
     */
    public int getContextInformationPosition() {
      return fPosition;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ContextInformation) {
        ContextInformation ci = (ContextInformation) obj;
        return getContextInformationPosition() == ci.getContextInformationPosition()
            && getInformationDisplayString().equals(ci.getInformationDisplayString());
      }
      return false;
    }

    /*
     * @see java.lang.Object#hashCode()
     * @since 3.1
     */
    @Override
    public int hashCode() {
      int low = fContextDisplayString != null ? fContextDisplayString.hashCode() : 0;
      return fPosition << 24 | fInformationDisplayString.hashCode() << 16 | low;
    }
  }

  private static final class TypeArgumentProposal {
    private final boolean fIsAmbiguous;
    private final String fProposal;
    private final String fTypeDisplayName;

    TypeArgumentProposal(String proposal, boolean ambiguous, String typeDisplayName) {
      fIsAmbiguous = ambiguous;
      fProposal = proposal;
      fTypeDisplayName = typeDisplayName;
    }

    public String getDisplayName() {
      return fTypeDisplayName;
    }

    boolean isAmbiguous() {
      return fIsAmbiguous;
    }

    @Override
    public String toString() {
      return fProposal;
    }
  }

  private IRegion fSelectedRegion; // initialized by apply()
  private TypeArgumentProposal[] fTypeArgumentProposals;
  private boolean fCanUseDiamond;

  public LazyGenericTypeProposal(
      CompletionProposal typeProposal, JavaContentAssistInvocationContext context) {
    super(typeProposal, context);
  }

  /*
   * @see ICompletionProposalExtension#apply(IDocument, char)
   */
  @Override
  public void apply(IDocument document, char trigger, int offset) {
    boolean onlyAppendArguments;
    try {
      onlyAppendArguments =
          fProposal.getCompletion().length == 0
              && offset > 0
              && document.getChar(offset - 1) == '<';
    } catch (BadLocationException e) {
      onlyAppendArguments = false;
    }

    if (onlyAppendArguments || shouldAppendArguments(document, offset, trigger)) {
      try {
        TypeArgumentProposal[] typeArgumentProposals = computeTypeArgumentProposals();
        if (typeArgumentProposals.length > 0) {

          int[] offsets = new int[typeArgumentProposals.length];
          int[] lengths = new int[typeArgumentProposals.length];
          StringBuffer buffer;

          if (canUseDiamond()) {
            buffer = new StringBuffer(getReplacementString());
            buffer.append("<>"); // $NON-NLS-1$
          } else
            buffer =
                createParameterList(typeArgumentProposals, offsets, lengths, onlyAppendArguments);

          // set the generic type as replacement string
          boolean insertClosingParenthesis = trigger == '(' && autocloseBrackets();
          if (insertClosingParenthesis) updateReplacementWithParentheses(buffer);
          super.setReplacementString(buffer.toString());

          // add import & remove package, update replacement offset
          super.apply(document, '\0', offset);

          if (getTextViewer() != null) {
            if (hasAmbiguousProposals(typeArgumentProposals)) {
              adaptOffsets(offsets, buffer);
              installLinkedMode(
                  document,
                  offsets,
                  lengths,
                  typeArgumentProposals,
                  insertClosingParenthesis,
                  onlyAppendArguments);
            } else {
              //                            if (insertClosingParenthesis)
              //                                setUpLinkedMode(document, ')');
              //                            else
              fSelectedRegion =
                  new Region(getReplacementOffset() + getReplacementString().length(), 0);
            }
          }

          return;
        }
      } catch (JavaModelException e) {
        // log and continue
        JavaPlugin.log(e);
      }
    }

    // default is to use the super implementation
    // reasons:
    // - not a parameterized type,
    // - already followed by <type arguments>
    // - proposal type does not inherit from expected type
    super.apply(document, trigger, offset);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal#computeTriggerCharacters()
   */
  @Override
  protected char[] computeTriggerCharacters() {
    return GENERIC_TYPE_TRIGGERS;
  }

  /**
   * Adapt the parameter offsets to any modification of the replacement string done by <code>apply
   * </code>. For example, applying the proposal may add an import instead of inserting the fully
   * qualified name.
   *
   * <p>This assumes that modifications happen only at the beginning of the replacement string and
   * do not touch the type arguments list.
   *
   * @param offsets the offsets to modify
   * @param buffer the original replacement string
   */
  private void adaptOffsets(int[] offsets, StringBuffer buffer) {
    String replacementString = getReplacementString();
    int delta =
        buffer.length() - replacementString.length(); // due to using an import instead of package
    for (int i = 0; i < offsets.length; i++) {
      offsets[i] -= delta;
    }
  }

  /**
   * Computes the type argument proposals for this type proposals. If there is an expected type
   * binding that is a super type of the proposed type, the wildcard type arguments of the proposed
   * type that can be mapped through to type the arguments of the expected type binding are bound
   * accordingly.
   *
   * <p>For type arguments that cannot be mapped to arguments in the expected type, or if there is
   * no expected type, the upper bound of the type argument is proposed.
   *
   * <p>The argument proposals have their <code>isAmbiguos</code> flag set to <code>false</code> if
   * the argument can be mapped to a non-wildcard type argument in the expected type, otherwise the
   * proposal is ambiguous.
   *
   * @return the type argument proposals for the proposed type
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  private TypeArgumentProposal[] computeTypeArgumentProposals() throws JavaModelException {
    if (fTypeArgumentProposals == null) {

      IType type = (IType) getJavaElement();
      if (type == null) return new TypeArgumentProposal[0];

      ITypeParameter[] parameters = type.getTypeParameters();
      if (parameters.length == 0) return new TypeArgumentProposal[0];

      TypeArgumentProposal[] arguments = new TypeArgumentProposal[parameters.length];

      ITypeBinding expectedTypeBinding = getExpectedType();
      if (expectedTypeBinding != null && expectedTypeBinding.isParameterizedType()) {
        // in this case, the type arguments we propose need to be compatible
        // with the corresponding type parameters to declared type

        IType expectedType = (IType) expectedTypeBinding.getJavaElement();

        IType[] path = computeInheritancePath(type, expectedType);
        if (path == null)
          // proposed type does not inherit from expected type
          // the user might be looking for an inner type of proposed type
          // to instantiate -> do not add any type arguments
          return new TypeArgumentProposal[0];

        int[] indices = new int[parameters.length];
        for (int paramIdx = 0; paramIdx < parameters.length; paramIdx++) {
          indices[paramIdx] = mapTypeParameterIndex(path, path.length - 1, paramIdx);
        }

        // for type arguments that are mapped through to the expected type's
        // parameters, take the arguments of the expected type
        ITypeBinding[] typeArguments = expectedTypeBinding.getTypeArguments();
        for (int paramIdx = 0; paramIdx < parameters.length; paramIdx++) {
          if (indices[paramIdx] != -1) {
            // type argument is mapped through
            ITypeBinding binding = typeArguments[indices[paramIdx]];
            arguments[paramIdx] = computeTypeProposal(binding, parameters[paramIdx]);
          }
        }
      }

      // for type arguments that are not mapped through to the expected type,
      // take the lower bound of the type parameter
      for (int i = 0; i < arguments.length; i++) {
        if (arguments[i] == null) {
          arguments[i] = computeTypeProposal(parameters[i]);
        }
      }
      fTypeArgumentProposals = arguments;
    }
    return fTypeArgumentProposals;
  }

  /**
   * Returns a type argument proposal for a given type parameter. The proposal is:
   *
   * <ul>
   *   <li>the type bound for type parameters with a single bound
   *   <li>the type parameter name for all other (unbounded or more than one bound) type parameters
   * </ul>
   *
   * Type argument proposals for type parameters are always ambiguous.
   *
   * @param parameter the type parameter of the inserted type
   * @return a type argument proposal for <code>parameter</code>
   * @throws org.eclipse.jdt.core.JavaModelException if this element does not exist or if an
   *     exception occurs while accessing its corresponding resource
   */
  private TypeArgumentProposal computeTypeProposal(ITypeParameter parameter)
      throws JavaModelException {
    String[] bounds = parameter.getBounds();
    String elementName = parameter.getElementName();
    String displayName = computeTypeParameterDisplayName(parameter, bounds);
    if (bounds.length == 1 && !"java.lang.Object".equals(bounds[0])) // $NON-NLS-1$
    return new TypeArgumentProposal(Signature.getSimpleName(bounds[0]), true, displayName);
    else return new TypeArgumentProposal(elementName, true, displayName);
  }

  private String computeTypeParameterDisplayName(ITypeParameter parameter, String[] bounds) {
    if (bounds.length == 0
        || bounds.length == 1 && "java.lang.Object".equals(bounds[0])) // $NON-NLS-1$
    return parameter.getElementName();
    StringBuffer buf = new StringBuffer(parameter.getElementName());
    buf.append(" extends "); // $NON-NLS-1$
    for (int i = 0; i < bounds.length; i++) {
      buf.append(Signature.getSimpleName(bounds[i]));
      if (i < bounds.length - 1) buf.append(" & "); // $NON-NLS-1$
    }
    return buf.toString();
  }

  /**
   * Returns a type argument proposal for a given type binding. The proposal is:
   *
   * <ul>
   *   <li>the simple type name for normal types or type variables (unambigous proposal)
   *   <li>for wildcard types (ambigous proposals):
   *       <ul>
   *         <li>the upper bound for wildcards with an upper bound
   *         <li>the {@linkplain #computeTypeProposal(org.eclipse.jdt.core.ITypeParameter) parameter
   *             proposal} for unbounded wildcards or wildcards with a lower bound
   *       </ul>
   * </ul>
   *
   * @param binding the type argument binding in the expected type
   * @param parameter the type parameter of the inserted type
   * @return a type argument proposal for <code>binding</code>
   * @throws org.eclipse.jdt.core.JavaModelException if this element does not exist or if an
   *     exception occurs while accessing its corresponding resource
   * @see #computeTypeProposal(org.eclipse.jdt.core.ITypeParameter)
   */
  private TypeArgumentProposal computeTypeProposal(ITypeBinding binding, ITypeParameter parameter)
      throws JavaModelException {
    final String name = Bindings.getTypeQualifiedName(binding);
    if (binding.isWildcardType()) {

      if (binding.isUpperbound()) {
        // replace the wildcard ? with the type parameter name to get "E extends Bound" instead of
        // "? extends Bound"
        String contextName = name.replaceFirst("\\?", parameter.getElementName()); // $NON-NLS-1$
        // upper bound - the upper bound is the bound itself
        return new TypeArgumentProposal(binding.getBound().getName(), true, contextName);
      }

      // no or upper bound - use the type parameter of the inserted type, as it may be more
      // restrictive (eg. List<?> list= new SerializableList<Serializable>())
      return computeTypeProposal(parameter);
    }

    // not a wildcard but a type or type variable - this is unambigously the right thing to insert
    return new TypeArgumentProposal(name, false, name);
  }

  /**
   * Computes one inheritance path from <code>superType</code> to <code>subType</code> or <code>null
   * </code> if <code>subType</code> does not inherit from <code>superType</code>. Note that there
   * may be more than one inheritance path - this method simply returns one.
   *
   * <p>The returned array contains <code>superType</code> at its first index, and <code>subType
   * </code> at its last index. If <code>subType</code> equals <code>superType</code> , an array of
   * length 1 is returned containing that type.
   *
   * @param subType the sub type
   * @param superType the super type
   * @return an inheritance path from <code>superType</code> to <code>subType</code>, or <code>null
   *     </code> if <code>subType</code> does not inherit from <code>superType</code>
   * @throws org.eclipse.jdt.core.JavaModelException if this element does not exist or if an
   *     exception occurs while accessing its corresponding resource
   */
  private IType[] computeInheritancePath(IType subType, IType superType) throws JavaModelException {
    if (superType == null) return null;

    // optimization: avoid building the type hierarchy for the identity case
    if (superType.equals(subType)) return new IType[] {subType};

    ITypeHierarchy hierarchy = subType.newSupertypeHierarchy(getProgressMonitor());
    if (!hierarchy.contains(superType)) return null; // no path

    List<IType> path = new LinkedList<IType>();
    path.add(superType);
    do {
      // any sub type must be on a hierarchy chain from superType to subType
      superType = hierarchy.getSubtypes(superType)[0];
      path.add(superType);
    } while (!superType.equals(
        subType)); // since the equality case is handled above, we can spare one check

    return path.toArray(new IType[path.size()]);
  }

  private NullProgressMonitor getProgressMonitor() {
    return new NullProgressMonitor();
  }

  /**
   * For the type parameter at <code>paramIndex</code> in the type at <code>path[pathIndex]</code> ,
   * this method computes the corresponding type parameter index in the type at <code>path[0]</code>
   * . If the type parameter does not map to a type parameter of the super type, <code>-1</code> is
   * returned.
   *
   * @param path the type inheritance path, a non-empty array of consecutive sub types
   * @param pathIndex an index into <code>path</code> specifying the type to start with
   * @param paramIndex the index of the type parameter to map - <code>path[pathIndex]</code> must
   *     have a type parameter at that index, lest an <code>ArrayIndexOutOfBoundsException</code> is
   *     thrown
   * @return the index of the type parameter in <code>path[0]</code> corresponding to the type
   *     parameter at <code>paramIndex</code> in <code>path[pathIndex]</code>, or -1 if there is no
   *     corresponding type parameter
   * @throws org.eclipse.jdt.core.JavaModelException if this element does not exist or if an
   *     exception occurs while accessing its corresponding resource
   * @throws ArrayIndexOutOfBoundsException if <code>path[pathIndex]</code> has &lt;= <code>
   *     paramIndex</code> parameters
   */
  private int mapTypeParameterIndex(IType[] path, int pathIndex, int paramIndex)
      throws JavaModelException, ArrayIndexOutOfBoundsException {
    if (pathIndex == 0)
      // break condition: we've reached the top of the hierarchy
      return paramIndex;

    IType subType = path[pathIndex];
    IType superType = path[pathIndex - 1];

    String superSignature = findMatchingSuperTypeSignature(subType, superType);
    ITypeParameter param = subType.getTypeParameters()[paramIndex];
    int index = findMatchingTypeArgumentIndex(superSignature, param.getElementName());
    if (index == -1) {
      // not mapped through
      return -1;
    }

    return mapTypeParameterIndex(path, pathIndex - 1, index);
  }

  /**
   * Finds and returns the super type signature in the <code>extends</code> or <code>implements
   * </code> clause of <code>subType</code> that corresponds to <code>superType</code>.
   *
   * @param subType a direct and true sub type of <code>superType</code>
   * @param superType a direct super type (super class or interface) of <code>subType</code>
   * @return the super type signature of <code>subType</code> referring to <code>superType</code>
   * @throws org.eclipse.jdt.core.JavaModelException if extracting the super type signatures fails,
   *     or if <code>subType</code> contains no super type signature to <code>superType</code>
   */
  private String findMatchingSuperTypeSignature(IType subType, IType superType)
      throws JavaModelException {
    String[] signatures = getSuperTypeSignatures(subType, superType);
    for (int i = 0; i < signatures.length; i++) {
      String signature = signatures[i];
      String qualified = SignatureUtil.qualifySignature(signature, subType);
      String subFQN = SignatureUtil.stripSignatureToFQN(qualified);

      String superFQN = superType.getFullyQualifiedName();
      if (subFQN.equals(superFQN)) {
        return signature;
      }

      // TODO handle local types
    }

    throw new JavaModelException(
        new CoreException(
            new Status(
                IStatus.ERROR,
                JavaPlugin.getPluginId(),
                IStatus.OK,
                "Illegal hierarchy",
                null))); // $NON-NLS-1$
  }

  /**
   * Finds and returns the index of the type argument named <code>argument</code> in the given super
   * type signature.
   *
   * <p>If <code>signature</code> does not contain a corresponding type argument, or if <code>
   * signature</code> has no type parameters (i.e. is a reference to a non-parameterized type or a
   * raw type), -1 is returned.
   *
   * @param signature the super type signature from a type's <code>extends</code> or <code>
   *     implements</code> clause
   * @param argument the name of the type argument to find
   * @return the index of the given type argument, or -1 if there is none
   */
  private int findMatchingTypeArgumentIndex(String signature, String argument) {
    String[] typeArguments = Signature.getTypeArguments(signature);
    for (int i = 0; i < typeArguments.length; i++) {
      if (Signature.getSignatureSimpleName(typeArguments[i]).equals(argument)) return i;
    }
    return -1;
  }

  /**
   * Returns the super interface signatures of <code>subType</code> if <code>superType</code> is an
   * interface, otherwise returns the super type signature.
   *
   * @param subType the sub type signature
   * @param superType the super type signature
   * @return the super type signatures of <code>subType</code>
   * @throws org.eclipse.jdt.core.JavaModelException if any java model operation fails
   */
  private String[] getSuperTypeSignatures(IType subType, IType superType)
      throws JavaModelException {
    if (superType.isInterface()) return subType.getSuperInterfaceTypeSignatures();
    else return new String[] {subType.getSuperclassTypeSignature()};
  }

  /**
   * Returns the type binding of the expected type as it is contained in the code completion
   * context.
   *
   * @return the binding of the expected type
   */
  private ITypeBinding getExpectedType() {
    char[][] chKeys = fInvocationContext.getCoreContext().getExpectedTypesKeys();
    if (chKeys == null || chKeys.length == 0) return null;

    String[] keys = new String[chKeys.length];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = String.valueOf(chKeys[0]);
    }

    final CheASTParser parser = CheASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
    parser.setProject(fCompilationUnit.getJavaProject());
    parser.setResolveBindings(true);
    parser.setStatementsRecovery(true);

    final Map<String, IBinding> bindings = new HashMap<String, IBinding>();
    ASTRequestor requestor =
        new ASTRequestor() {
          @Override
          public void acceptBinding(String bindingKey, IBinding binding) {
            bindings.put(bindingKey, binding);
          }
        };
    parser.createASTs(new ICompilationUnit[0], keys, requestor, null);

    if (bindings.size() > 0) return (ITypeBinding) bindings.get(keys[0]);

    return null;
  }

  /**
   * Returns <code>true</code> if type arguments should be appended when applying this proposal,
   * <code>false</code> if not (for example if the document already contains a type argument list
   * after the insertion point.
   *
   * @param document the document
   * @param offset the insertion offset
   * @param trigger the trigger character
   * @return <code>true</code> if arguments should be appended
   */
  private boolean shouldAppendArguments(IDocument document, int offset, char trigger) {
    /*
     * No argument list if there were any special triggers (for example a period to qualify an
     * inner type).
     */
    if (trigger != '\0' && trigger != '<' && trigger != '(') return false;

    /* No argument list if the completion is empty (already within the argument list). */
    char[] completion = fProposal.getCompletion();
    if (completion.length == 0) return false;

    /* No argument list if there already is a generic signature behind the name. */
    try {
      IRegion region = document.getLineInformationOfOffset(offset);
      String line = document.get(region.getOffset(), region.getLength());

      int index = offset - region.getOffset();
      while (index != line.length() && Character.isUnicodeIdentifierPart(line.charAt(index)))
        ++index;

      if (index == line.length()) return true;

      char ch = line.charAt(index);
      return ch != '<';

    } catch (BadLocationException e) {
      return true;
    }
  }

  private StringBuffer createParameterList(
      TypeArgumentProposal[] typeArguments,
      int[] offsets,
      int[] lengths,
      boolean onlyAppendArguments) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getReplacementString());

    FormatterPrefs prefs = getFormatterPrefs();
    final char LESS = '<';
    final char GREATER = '>';
    if (!onlyAppendArguments) {
      if (prefs.beforeOpeningBracket) buffer.append(SPACE);
      buffer.append(LESS);
    }
    if (prefs.afterOpeningBracket) buffer.append(SPACE);
    StringBuffer separator = new StringBuffer(3);
    if (prefs.beforeTypeArgumentComma) separator.append(SPACE);
    separator.append(COMMA);
    if (prefs.afterTypeArgumentComma) separator.append(SPACE);

    for (int i = 0; i != typeArguments.length; i++) {
      if (i != 0) buffer.append(separator);

      offsets[i] = buffer.length();
      buffer.append(typeArguments[i]);
      lengths[i] = buffer.length() - offsets[i];
    }
    if (prefs.beforeClosingBracket) buffer.append(SPACE);

    if (!onlyAppendArguments) buffer.append(GREATER);

    return buffer;
  }

  private void installLinkedMode(
      final IDocument document,
      int[] offsets,
      int[] lengths,
      TypeArgumentProposal[] typeArgumentProposals,
      boolean withParentheses,
      final boolean onlyAppendArguments) {
    int replacementOffset = getReplacementOffset();
    String replacementString = getReplacementString();

    try {
      LinkedModeModelImpl model = new LinkedModeModelImpl();
      for (int i = 0; i != offsets.length; i++) {
        if (typeArgumentProposals[i].isAmbiguous()) {
          LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();
          RegionImpl region = new RegionImpl();
          region.setOffset(replacementOffset + offsets[i]);
          region.setLength(lengths[i]);
          group.addPositions(region);
          model.addGroups(group);
        }
      }
      if (withParentheses) {
        LinkedPositionGroupImpl group = new LinkedPositionGroupImpl();
        RegionImpl region = new RegionImpl();
        region.setOffset(replacementOffset + getCursorPosition());
        region.setLength(0);
        group.addPositions(region);
        model.addGroups(group);
      }
      model.setEscapePosition(replacementOffset + replacementString.length());
      this.linkedModel = model;
      //			model.forceInstall();
      //			JavaEditor editor= getJavaEditor();
      //			if (editor != null) {
      //				model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
      //			}

      if (!onlyAppendArguments && (document instanceof IDocumentExtension)) { // see bug 301990
        FormatterPrefs prefs = getFormatterPrefs();
        final Position firstBracketPosition;
        final Position secondBracketPosition;

        int firstBracketOffset = replacementOffset + offsets[0] - 1;
        if (prefs.afterOpeningBracket) {
          firstBracketOffset--;
        }
        firstBracketPosition = new Position(firstBracketOffset, 1);
        document.addPosition(firstBracketPosition);

        int secondBracketOffset =
            replacementOffset + offsets[offsets.length - 1] + lengths[offsets.length - 1] + 1;
        if (prefs.beforeClosingBracket) {
          secondBracketOffset++;
        }
        secondBracketPosition = new Position(secondBracketOffset, 0);
        document.addPosition(secondBracketPosition);

        //				model.addLinkingListener(new ILinkedModeListener() {
        //					public void left(LinkedModeModel environment, int flags) {
        //						try {
        //							if (getTextViewer().getSelectedRange().y > 1 || flags !=
        // ILinkedModeListener.EXTERNAL_MODIFICATION)
        //								return;
        //							((IDocumentExtension) document).registerPostNotificationReplace(null, new
        // IDocumentExtension.IReplace() {
        //								public void perform(IDocument d, IDocumentListener owner) {
        //									try {
        //										if ((firstBracketPosition.length == 0 || firstBracketPosition.isDeleted) &&
        // !secondBracketPosition.isDeleted) {
        //											d.replace(firstBracketPosition.offset, secondBracketPosition.offset -
        // firstBracketPosition.offset, ""); //$NON-NLS-1$
        //										}
        //									} catch (BadLocationException e) {
        //										JavaPlugin.log(e);
        //									}
        //								}
        //							});
        //						} finally {
        //							document.removePosition(firstBracketPosition);
        //							document.removePosition(secondBracketPosition);
        //						}
        //					}
        //
        //					public void suspend(LinkedModeModel environment) {
        //					}
        //
        //					public void resume(LinkedModeModel environment, int flags) {
        //					}
        //				});
      }

      //			LinkedModeUI ui= new EditorLinkedModeUI(model, getTextViewer());
      //			ui.setExitPolicy(new ExitPolicy(withParentheses ? ')' : '>', document));
      //			ui.setExitPosition(getTextViewer(), replacementOffset + replacementString.length(), 0,
      // Integer.MAX_VALUE);
      //			ui.setDoContextInfo(true);
      //			ui.enter();

      fSelectedRegion =
          new Region(replacementOffset + replacementString.length(), 0); // ui.getSelectedRegion();

    } catch (BadLocationException e) {
      JavaPlugin.log(e);
      openErrorDialog(e);
    }
  }

  private boolean hasAmbiguousProposals(TypeArgumentProposal[] typeArgumentProposals) {
    boolean hasAmbiguousProposals = false;
    for (int i = 0; i < typeArgumentProposals.length; i++) {
      if (typeArgumentProposals[i].isAmbiguous()) {
        hasAmbiguousProposals = true;
        break;
      }
    }
    return hasAmbiguousProposals;
  }

  //	/**
  //	 * Returns the currently active java editor, or <code>null</code> if it
  //	 * cannot be determined.
  //	 *
  //	 * @return  the currently active java editor, or <code>null</code>
  //	 */
  //	private JavaEditor getJavaEditor() {
  //		IEditorPart part= JavaPlugin.getActivePage().getActiveEditor();
  //		if (part instanceof JavaEditor)
  //			return (JavaEditor) part;
  //		else
  //			return null;
  //	}

  /*
   * @see ICompletionProposal#getSelection(IDocument)
   */
  @Override
  public Point getSelection(IDocument document) {
    if (fSelectedRegion == null) return super.getSelection(document);

    return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
  }

  private void openErrorDialog(BadLocationException e) {
    //		Shell shell= getTextViewer().getTextWidget().getShell();
    //		String message= e.getMessage();
    //		MessageDialog.openError(shell, JavaTextMessages.FilledArgumentNamesMethodProposal_error_msg,
    //				message == null ? e.toString() : message);
    JavaPlugin.log(e);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeContextInformation()
   */
  @Override
  protected IContextInformation computeContextInformation() {
    try {
      if (hasParameters()) {
        TypeArgumentProposal[] proposals = computeTypeArgumentProposals();
        if (hasAmbiguousProposals(proposals)) return new ContextInformation(this);
      }
    } catch (JavaModelException e) {
    }
    return super.computeContextInformation();
  }

  @Override
  protected int computeCursorPosition() {
    if (fSelectedRegion != null) return fSelectedRegion.getOffset() - getReplacementOffset();
    return super.computeCursorPosition();
  }

  private boolean hasParameters() {
    try {
      IType type = (IType) getJavaElement();
      if (type == null) return false;

      return type.getTypeParameters().length > 0;
    } catch (JavaModelException e) {
      return false;
    }
  }

  /**
   * Sets whether this proposal can use the diamond.
   *
   * @param canUseDiamond <code>true</code> if a diamond can be inserted
   * @see
   *     org.eclipse.jdt.core.CompletionProposal#canUseDiamond(org.eclipse.jdt.core.CompletionContext)
   * @since 3.7
   */
  void canUseDiamond(boolean canUseDiamond) {
    fCanUseDiamond = canUseDiamond;
  }

  /**
   * Tells whether this proposal can use the diamond.
   *
   * @return <code>true</code> if a diamond can be used
   * @see
   *     org.eclipse.jdt.core.CompletionProposal#canUseDiamond(org.eclipse.jdt.core.CompletionContext)
   * @since 3.7
   */
  protected boolean canUseDiamond() {
    return fCanUseDiamond;
  }

  @Override
  public LinkedModeModel getLinkedModel() {
    return linkedModel;
  }
}

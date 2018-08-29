/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.HashSet;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.TypeFilter;

public class SimilarElementsRequestor extends CompletionRequestor {

  public static final int CLASSES = 1 << 1;
  public static final int INTERFACES = 1 << 2;
  public static final int ANNOTATIONS = 1 << 3;
  public static final int ENUMS = 1 << 4;
  public static final int VARIABLES = 1 << 5;
  public static final int PRIMITIVETYPES = 1 << 6;
  public static final int VOIDTYPE = 1 << 7;
  public static final int REF_TYPES = CLASSES | INTERFACES | ENUMS | ANNOTATIONS;
  public static final int REF_TYPES_AND_VAR = REF_TYPES | VARIABLES;
  public static final int ALL_TYPES = PRIMITIVETYPES | REF_TYPES_AND_VAR;

  private static final String[] PRIM_TYPES = {
    "boolean", "byte", "char", "short", "int", "long", "float", "double"
  }; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
  // //$NON-NLS-7$ //$NON-NLS-8$

  private int fKind;
  private String fName;

  private HashSet<SimilarElement> fResult;

  public static SimilarElement[] findSimilarElement(ICompilationUnit cu, Name name, int kind)
      throws JavaModelException {
    int pos = name.getStartPosition();
    int nArguments = -1;

    String identifier = ASTNodes.getSimpleNameIdentifier(name);
    String returnType = null;
    ICompilationUnit preparedCU = null;

    try {
      if (name.isQualifiedName()) {
        pos = ((QualifiedName) name).getName().getStartPosition();
      } else {
        pos = name.getStartPosition() + 1; // first letter must be included, other
      }
      Javadoc javadoc = (Javadoc) ASTNodes.getParent(name, ASTNode.JAVADOC);
      if (javadoc != null) {
        preparedCU = createPreparedCU(cu, javadoc, name.getStartPosition());
        cu = preparedCU;
      }

      SimilarElementsRequestor requestor =
          new SimilarElementsRequestor(identifier, kind, nArguments, returnType);
      requestor.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, true);
      requestor.setIgnored(CompletionProposal.KEYWORD, true);
      requestor.setIgnored(CompletionProposal.LABEL_REF, true);
      requestor.setIgnored(CompletionProposal.METHOD_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.PACKAGE_REF, true);
      requestor.setIgnored(CompletionProposal.VARIABLE_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.METHOD_REF, true);
      requestor.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, true);
      requestor.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, true);
      requestor.setIgnored(CompletionProposal.FIELD_REF, true);
      requestor.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, true);
      requestor.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, true);
      requestor.setIgnored(CompletionProposal.VARIABLE_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.VARIABLE_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, true);
      requestor.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, true);
      return requestor.process(cu, pos);
    } finally {
      if (preparedCU != null) {
        preparedCU.discardWorkingCopy();
      }
    }
  }

  private static ICompilationUnit createPreparedCU(
      ICompilationUnit cu, Javadoc comment, int wordStart) throws JavaModelException {
    int startpos = comment.getStartPosition();
    boolean isTopLevel = comment.getParent().getParent() instanceof CompilationUnit;
    char[] content = cu.getBuffer().getCharacters().clone();
    if (isTopLevel && (wordStart + 6 < content.length)) {
      content[startpos++] = 'i';
      content[startpos++] = 'm';
      content[startpos++] = 'p';
      content[startpos++] = 'o';
      content[startpos++] = 'r';
      content[startpos++] = 't';
    }
    if (wordStart < content.length) {
      for (int i = startpos; i < wordStart; i++) {
        content[i] = ' ';
      }
    }

    /*
     * Explicitly create a new non-shared working copy.
     */
    ICompilationUnit newCU = cu.getWorkingCopy(null);
    newCU.getBuffer().setContents(content);
    return newCU;
  }

  /**
   * Constructor for SimilarElementsRequestor.
   *
   * @param name the name
   * @param kind the type kind
   * @param nArguments the number of arguments
   * @param preferredType the preferred type
   */
  private SimilarElementsRequestor(String name, int kind, int nArguments, String preferredType) {
    super();
    fName = name;
    fKind = kind;

    fResult = new HashSet<SimilarElement>();
    // nArguments and preferredType not yet used
  }

  private void addResult(SimilarElement elem) {
    fResult.add(elem);
  }

  private SimilarElement[] process(ICompilationUnit cu, int pos) throws JavaModelException {
    try {
      cu.codeComplete(pos, this);
      processKeywords();
      return fResult.toArray(new SimilarElement[fResult.size()]);
    } finally {
      fResult.clear();
    }
  }

  private boolean isKind(int kind) {
    return (fKind & kind) != 0;
  }

  /** Method addPrimitiveTypes. */
  private void processKeywords() {
    if (isKind(PRIMITIVETYPES)) {
      for (int i = 0; i < PRIM_TYPES.length; i++) {
        if (NameMatcher.isSimilarName(fName, PRIM_TYPES[i])) {
          addResult(new SimilarElement(PRIMITIVETYPES, PRIM_TYPES[i], 50));
        }
      }
    }
    if (isKind(VOIDTYPE)) {
      String voidType = "void"; // $NON-NLS-1$
      if (NameMatcher.isSimilarName(fName, voidType)) {
        addResult(new SimilarElement(PRIMITIVETYPES, voidType, 50));
      }
    }
  }

  private static final int getKind(int flags, char[] typeNameSig) {
    if (Signature.getTypeSignatureKind(typeNameSig) == Signature.TYPE_VARIABLE_SIGNATURE) {
      return VARIABLES;
    }
    if (Flags.isAnnotation(flags)) {
      return ANNOTATIONS;
    }
    if (Flags.isInterface(flags)) {
      return INTERFACES;
    }
    if (Flags.isEnum(flags)) {
      return ENUMS;
    }
    return CLASSES;
  }

  private void addType(char[] typeNameSig, int flags, int relevance) {
    int kind = getKind(flags, typeNameSig);
    if (!isKind(kind)) {
      return;
    }
    String fullName = new String(Signature.toCharArray(Signature.getTypeErasure(typeNameSig)));
    if (TypeFilter.isFiltered(fullName)) {
      return;
    }
    if (NameMatcher.isSimilarName(fName, Signature.getSimpleName(fullName))) {
      addResult(new SimilarElement(kind, fullName, relevance));
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.CompletionRequestor#accept(org.eclipse.jdt.core.CompletionProposal)
   */
  @Override
  public void accept(CompletionProposal proposal) {
    if (proposal.getKind() == CompletionProposal.TYPE_REF) {
      addType(proposal.getSignature(), proposal.getFlags(), proposal.getRelevance());
    }
  }

  public static String[] getStaticImportFavorites(
      ICompilationUnit cu, final String elementName, boolean isMethod, String[] favorites)
      throws JavaModelException {
    StringBuffer dummyCU = new StringBuffer();
    String packName = cu.getParent().getElementName();
    IType type = cu.findPrimaryType();
    if (type == null) return new String[0];

    if (packName.length() > 0) {
      dummyCU.append("package ").append(packName).append(';'); // $NON-NLS-1$
    }
    dummyCU
        .append("public class ")
        .append(type.getElementName())
        .append("{\n static {\n")
        .append(elementName); // static initializer  //$NON-NLS-1$//$NON-NLS-2$
    int offset = dummyCU.length();
    dummyCU.append("\n}\n }"); // $NON-NLS-1$

    ICompilationUnit newCU = null;
    try {
      newCU = cu.getWorkingCopy(null);
      newCU.getBuffer().setContents(dummyCU.toString());

      final HashSet<String> result = new HashSet<String>();

      CompletionRequestor requestor =
          new CompletionRequestor(true) {
            @Override
            public void accept(CompletionProposal proposal) {
              if (elementName.equals(new String(proposal.getName()))) {
                CompletionProposal[] requiredProposals = proposal.getRequiredProposals();
                for (int i = 0; i < requiredProposals.length; i++) {
                  CompletionProposal curr = requiredProposals[i];
                  if (curr.getKind() == CompletionProposal.METHOD_IMPORT
                      || curr.getKind() == CompletionProposal.FIELD_IMPORT) {
                    result.add(
                        JavaModelUtil.concatenateName(
                            Signature.toCharArray(curr.getDeclarationSignature()), curr.getName()));
                  }
                }
              }
            }
          };

      if (isMethod) {
        requestor.setIgnored(CompletionProposal.METHOD_REF, false);
        requestor.setAllowsRequiredProposals(
            CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);
      } else {
        requestor.setIgnored(CompletionProposal.FIELD_REF, false);
        requestor.setAllowsRequiredProposals(
            CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);
      }
      requestor.setFavoriteReferences(favorites);

      newCU.codeComplete(offset, requestor);

      return result.toArray(new String[result.size()]);
    } finally {
      if (newCU != null) {
        newCU.discardWorkingCopy();
      }
    }
  }
}

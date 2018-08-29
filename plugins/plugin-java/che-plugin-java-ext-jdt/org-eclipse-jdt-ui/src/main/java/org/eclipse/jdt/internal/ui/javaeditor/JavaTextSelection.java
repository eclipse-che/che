/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.javaeditor;

import org.eclipse.che.jface.text.TextSelection;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IDocument;

/** A special text selection that gives access to the resolved and enclosing element. */
public class JavaTextSelection extends TextSelection {

  private ITypeRoot fElement;
  private IJavaElement[] fResolvedElements;

  private boolean fEnclosingElementRequested;
  private IJavaElement fEnclosingElement;

  private boolean fPartialASTRequested;
  private CompilationUnit fPartialAST;

  private boolean fNodesRequested;
  private ASTNode[] fSelectedNodes;
  private ASTNode fCoveringNode;

  private boolean fInMethodBodyRequested;
  private boolean fInMethodBody;

  private boolean fInClassInitializerRequested;
  private boolean fInClassInitializer;

  private boolean fInVariableInitializerRequested;
  private boolean fInVariableInitializer;

  /**
   * Indicates whether the selection node has been checked to be of type <code>Annotation</code>.
   *
   * @since 3.7
   */
  private boolean fInAnnotationRequested;

  /**
   * Indicates whether selection node is of type <code>Annotation</code>.
   *
   * @since 3.7
   */
  private boolean fInAnnotation;

  /**
   * Creates a new text selection at the given offset and length.
   *
   * @param element the root element
   * @param document the document
   * @param offset offset of the selection
   * @param length length of the selection
   */
  public JavaTextSelection(ITypeRoot element, IDocument document, int offset, int length) {
    super(document, offset, length);
    fElement = element;
  }

  /**
   * Resolves the <code>IJavaElement</code>s at the current offset. Returns an empty array if the
   * string under the offset doesn't resolve to a <code>IJavaElement</code>.
   *
   * @return the resolved java elements at the current offset
   * @throws JavaModelException passed from the underlying code resolve API
   */
  public IJavaElement[] resolveElementAtOffset() throws JavaModelException {
    if (fResolvedElements != null) return fResolvedElements;
    // long start= System.currentTimeMillis();
    fResolvedElements = SelectionConverter.codeResolve(fElement, this);
    // System.out.println("Time resolving element: " + (System.currentTimeMillis() - start));
    return fResolvedElements;
  }

  public IJavaElement resolveEnclosingElement() throws JavaModelException {
    if (fEnclosingElementRequested) return fEnclosingElement;
    fEnclosingElementRequested = true;
    fEnclosingElement = SelectionConverter.resolveEnclosingElement(fElement, this);
    return fEnclosingElement;
  }

  public CompilationUnit resolvePartialAstAtOffset() {
    if (fPartialASTRequested) return fPartialAST;
    fPartialASTRequested = true;
    // long start= System.currentTimeMillis();
    fPartialAST = SharedASTProvider.getAST(fElement, SharedASTProvider.WAIT_YES, null);
    // System.out.println("Time requesting partial AST: " + (System.currentTimeMillis() - start));
    return fPartialAST;
  }

  public ASTNode[] resolveSelectedNodes() {
    if (fNodesRequested) return fSelectedNodes;
    fNodesRequested = true;
    CompilationUnit root = resolvePartialAstAtOffset();
    if (root == null) return null;
    Selection ds = Selection.createFromStartLength(getOffset(), getLength());
    SelectionAnalyzer analyzer = new SelectionAnalyzer(ds, false);
    root.accept(analyzer);
    fSelectedNodes = analyzer.getSelectedNodes();
    fCoveringNode = analyzer.getLastCoveringNode();
    return fSelectedNodes;
  }

  public ASTNode resolveCoveringNode() {
    if (fNodesRequested) return fCoveringNode;
    resolveSelectedNodes();
    return fCoveringNode;
  }

  public boolean resolveInMethodBody() {
    if (fInMethodBodyRequested) return fInMethodBody;
    fInMethodBodyRequested = true;
    resolveSelectedNodes();
    ASTNode node = getStartNode();
    if (node == null) {
      fInMethodBody = true;
    } else {
      while (node != null) {
        int nodeType = node.getNodeType();
        if (nodeType == ASTNode.BLOCK && node.getParent() instanceof BodyDeclaration) {
          fInMethodBody = node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION;
          break;
        } else if (nodeType == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
          fInMethodBody = false;
          break;
        }
        node = node.getParent();
      }
    }
    return fInMethodBody;
  }

  public boolean resolveInClassInitializer() {
    if (fInClassInitializerRequested) return fInClassInitializer;
    fInClassInitializerRequested = true;
    resolveSelectedNodes();
    ASTNode node = getStartNode();
    if (node == null) {
      fInClassInitializer = true;
    } else {
      while (node != null) {
        int nodeType = node.getNodeType();
        if (node instanceof AbstractTypeDeclaration) {
          fInClassInitializer = false;
          break;
        } else if (nodeType == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
          fInClassInitializer = false;
          break;
        } else if (nodeType == ASTNode.INITIALIZER) {
          fInClassInitializer = true;
          break;
        }
        node = node.getParent();
      }
    }
    return fInClassInitializer;
  }

  public boolean resolveInVariableInitializer() {
    if (fInVariableInitializerRequested) return fInVariableInitializer;
    fInVariableInitializerRequested = true;
    resolveSelectedNodes();
    ASTNode node = getStartNode();
    ASTNode last = null;
    while (node != null) {
      int nodeType = node.getNodeType();
      if (node instanceof AbstractTypeDeclaration) {
        fInVariableInitializer = false;
        break;
      } else if (nodeType == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
        fInVariableInitializer = false;
        break;
      } else if (nodeType == ASTNode.VARIABLE_DECLARATION_FRAGMENT
          && ((VariableDeclarationFragment) node).getInitializer() == last) {
        fInVariableInitializer = true;
        break;
      } else if (nodeType == ASTNode.SINGLE_VARIABLE_DECLARATION
          && ((SingleVariableDeclaration) node).getInitializer() == last) {
        fInVariableInitializer = true;
        break;
      } else if (nodeType == ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION
          && ((AnnotationTypeMemberDeclaration) node).getDefault() == last) {
        fInVariableInitializer = true;
        break;
      }
      last = node;
      node = node.getParent();
    }
    return fInVariableInitializer;
  }

  /**
   * Resolves the selected nodes and returns <code>true</code> if the node or any of its ancestors
   * is of type <code>Annotation</code>, <code>false</code> otherwise.
   *
   * @return <code>true</code> if the node or any of its ancestors is of type <code>Annotation
   *     </code>, <code>false</code> otherwise
   * @since 3.7
   */
  public boolean resolveInAnnotation() {
    if (fInAnnotationRequested) return fInAnnotation;
    fInAnnotationRequested = true;
    resolveSelectedNodes();
    ASTNode node = getStartNode();
    while (node != null) {
      if (node instanceof Annotation) {
        fInAnnotation = true;
        break;
      }
      node = node.getParent();
    }
    return fInAnnotation;
  }

  private ASTNode getStartNode() {
    if (fSelectedNodes != null && fSelectedNodes.length > 0) return fSelectedNodes[0];
    else return fCoveringNode;
  }
}

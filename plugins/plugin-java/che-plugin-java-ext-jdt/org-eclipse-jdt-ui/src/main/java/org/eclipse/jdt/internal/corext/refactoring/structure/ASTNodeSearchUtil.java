/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.structure;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

/**
 * JDT-UI-internal helper methods to find AST nodes for {@link SearchMatch}es or {@link
 * IJavaElement}s. :
 */
public class ASTNodeSearchUtil {

  private ASTNodeSearchUtil() {
    // no instance
  }

  public static ASTNode[] getAstNodes(SearchMatch[] searchResults, CompilationUnit cuNode) {
    List<ASTNode> result = new ArrayList<ASTNode>(searchResults.length);
    for (int i = 0; i < searchResults.length; i++) {
      ASTNode node = getAstNode(searchResults[i], cuNode);
      if (node != null) result.add(node);
    }
    return result.toArray(new ASTNode[result.size()]);
  }

  public static ASTNode getAstNode(SearchMatch searchResult, CompilationUnit cuNode) {
    ASTNode selectedNode = getAstNode(cuNode, searchResult.getOffset(), searchResult.getLength());
    if (selectedNode == null) return null;
    if (selectedNode.getParent() == null) return null;
    return selectedNode;
  }

  public static ASTNode getAstNode(CompilationUnit cuNode, int start, int length) {
    SelectionAnalyzer analyzer =
        new SelectionAnalyzer(Selection.createFromStartLength(start, length), true);
    cuNode.accept(analyzer);
    // XXX workaround for jdt core feature 23527
    ASTNode node = analyzer.getFirstSelectedNode();
    if (node == null && analyzer.getLastCoveringNode() instanceof SuperConstructorInvocation)
      node = analyzer.getLastCoveringNode().getParent();
    else if (node == null && analyzer.getLastCoveringNode() instanceof ConstructorInvocation)
      node = analyzer.getLastCoveringNode().getParent();

    if (node == null) return null;

    ASTNode parentNode = node.getParent();

    if (parentNode instanceof MethodDeclaration) {
      MethodDeclaration md = (MethodDeclaration) parentNode;
      if (!(node instanceof SimpleName)
          && md.isConstructor()
          && md.getBody() != null
          && md.getBody().statements().size() > 0
          && (md.getBody().statements().get(0) instanceof ConstructorInvocation
              || md.getBody().statements().get(0) instanceof SuperConstructorInvocation)
          && ((ASTNode) md.getBody().statements().get(0)).getLength() == length + 1)
        return (ASTNode) md.getBody().statements().get(0);
    }

    if (parentNode instanceof SuperConstructorInvocation) {
      if (parentNode.getLength() == length + 1) return parentNode;
    }
    if (parentNode instanceof ConstructorInvocation) {
      if (parentNode.getLength() == length + 1) return parentNode;
    }
    return node;
  }

  public static MethodDeclaration getMethodDeclarationNode(IMethod iMethod, CompilationUnit cuNode)
      throws JavaModelException {
    return (MethodDeclaration)
        ASTNodes.getParent(getNameNode(iMethod, cuNode), MethodDeclaration.class);
  }

  public static AnnotationTypeMemberDeclaration getAnnotationTypeMemberDeclarationNode(
      IMethod iMethod, CompilationUnit cuNode) throws JavaModelException {
    return (AnnotationTypeMemberDeclaration)
        ASTNodes.getParent(getNameNode(iMethod, cuNode), AnnotationTypeMemberDeclaration.class);
  }

  public static BodyDeclaration getMethodOrAnnotationTypeMemberDeclarationNode(
      IMethod iMethod, CompilationUnit cuNode) throws JavaModelException {
    if (JdtFlags.isAnnotation(iMethod.getDeclaringType()))
      return getAnnotationTypeMemberDeclarationNode(iMethod, cuNode);
    else return getMethodDeclarationNode(iMethod, cuNode);
  }

  public static VariableDeclarationFragment getFieldDeclarationFragmentNode(
      IField iField, CompilationUnit cuNode) throws JavaModelException {
    ASTNode node = getNameNode(iField, cuNode);
    if (node instanceof VariableDeclarationFragment) return (VariableDeclarationFragment) node;
    return (VariableDeclarationFragment)
        ASTNodes.getParent(node, VariableDeclarationFragment.class);
  }

  public static FieldDeclaration getFieldDeclarationNode(IField iField, CompilationUnit cuNode)
      throws JavaModelException {
    return (FieldDeclaration)
        ASTNodes.getParent(getNameNode(iField, cuNode), FieldDeclaration.class);
  }

  public static EnumConstantDeclaration getEnumConstantDeclaration(
      IField iField, CompilationUnit cuNode) throws JavaModelException {
    return (EnumConstantDeclaration)
        ASTNodes.getParent(getNameNode(iField, cuNode), EnumConstantDeclaration.class);
  }

  public static BodyDeclaration getFieldOrEnumConstantDeclaration(
      IField iField, CompilationUnit cuNode) throws JavaModelException {
    if (JdtFlags.isEnum(iField)) return getEnumConstantDeclaration(iField, cuNode);
    else return getFieldDeclarationNode(iField, cuNode);
  }

  public static EnumDeclaration getEnumDeclarationNode(IType iType, CompilationUnit cuNode)
      throws JavaModelException {
    return (EnumDeclaration) ASTNodes.getParent(getNameNode(iType, cuNode), EnumDeclaration.class);
  }

  public static AnnotationTypeDeclaration getAnnotationTypeDeclarationNode(
      IType iType, CompilationUnit cuNode) throws JavaModelException {
    return (AnnotationTypeDeclaration)
        ASTNodes.getParent(getNameNode(iType, cuNode), AnnotationTypeDeclaration.class);
  }

  public static BodyDeclaration getBodyDeclarationNode(IMember iMember, CompilationUnit cuNode)
      throws JavaModelException {
    return (BodyDeclaration)
        ASTNodes.getParent(getNameNode(iMember, cuNode), BodyDeclaration.class);
  }

  public static AbstractTypeDeclaration getAbstractTypeDeclarationNode(
      IType iType, CompilationUnit cuNode) throws JavaModelException {
    return (AbstractTypeDeclaration)
        ASTNodes.getParent(getNameNode(iType, cuNode), AbstractTypeDeclaration.class);
  }

  public static TypeDeclaration getTypeDeclarationNode(IType iType, CompilationUnit cuNode)
      throws JavaModelException {
    return (TypeDeclaration) ASTNodes.getParent(getNameNode(iType, cuNode), TypeDeclaration.class);
  }

  public static ClassInstanceCreation getClassInstanceCreationNode(
      IType iType, CompilationUnit cuNode) throws JavaModelException {
    return (ClassInstanceCreation)
        ASTNodes.getParent(getNameNode(iType, cuNode), ClassInstanceCreation.class);
  }

  public static List<BodyDeclaration> getBodyDeclarationList(IType iType, CompilationUnit cuNode)
      throws JavaModelException {
    if (iType.isAnonymous())
      return getClassInstanceCreationNode(iType, cuNode)
          .getAnonymousClassDeclaration()
          .bodyDeclarations();
    else return getAbstractTypeDeclarationNode(iType, cuNode).bodyDeclarations();
  }

  // returns an array because of the import container, which does not represent 1 node but many
  // for fields, it returns the whole declaration node
  public static ASTNode[] getDeclarationNodes(IJavaElement element, CompilationUnit cuNode)
      throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.FIELD:
        return new ASTNode[] {getFieldOrEnumConstantDeclaration((IField) element, cuNode)};
      case IJavaElement.IMPORT_CONTAINER:
        return getImportNodes((IImportContainer) element, cuNode);
      case IJavaElement.IMPORT_DECLARATION:
        return new ASTNode[] {getImportDeclarationNode((IImportDeclaration) element, cuNode)};
      case IJavaElement.INITIALIZER:
        return new ASTNode[] {getInitializerNode((IInitializer) element, cuNode)};
      case IJavaElement.METHOD:
        return new ASTNode[] {
          getMethodOrAnnotationTypeMemberDeclarationNode((IMethod) element, cuNode)
        };
      case IJavaElement.PACKAGE_DECLARATION:
        return new ASTNode[] {getPackageDeclarationNode((IPackageDeclaration) element, cuNode)};
      case IJavaElement.TYPE:
        return new ASTNode[] {getAbstractTypeDeclarationNode((IType) element, cuNode)};
      default:
        Assert.isTrue(false, String.valueOf(element.getElementType()));
        return null;
    }
  }

  private static ASTNode getNameNode(IMember iMember, CompilationUnit cuNode)
      throws JavaModelException {
    return NodeFinder.perform(cuNode, iMember.getNameRange());
  }

  public static PackageDeclaration getPackageDeclarationNode(
      IPackageDeclaration reference, CompilationUnit cuNode) throws JavaModelException {
    return (PackageDeclaration) findNode(reference.getSourceRange(), cuNode);
  }

  public static ImportDeclaration getImportDeclarationNode(
      IImportDeclaration reference, CompilationUnit cuNode) throws JavaModelException {
    return (ImportDeclaration) findNode(reference.getSourceRange(), cuNode);
  }

  public static ASTNode[] getImportNodes(IImportContainer reference, CompilationUnit cuNode)
      throws JavaModelException {
    IJavaElement[] imps = reference.getChildren();
    ASTNode[] result = new ASTNode[imps.length];
    for (int i = 0; i < imps.length; i++) {
      result[i] = getImportDeclarationNode((IImportDeclaration) imps[i], cuNode);
    }
    return result;
  }

  public static Initializer getInitializerNode(IInitializer initializer, CompilationUnit cuNode)
      throws JavaModelException {
    ASTNode node = findNode(initializer.getSourceRange(), cuNode);
    if (node instanceof Initializer) return (Initializer) node;
    if (node instanceof Block && node.getParent() instanceof Initializer)
      return (Initializer) node.getParent();
    return null;
  }

  private static ASTNode findNode(ISourceRange range, CompilationUnit cuNode) {
    NodeFinder nodeFinder = new NodeFinder(cuNode, range.getOffset(), range.getLength());
    ASTNode coveredNode = nodeFinder.getCoveredNode();
    if (coveredNode != null) return coveredNode;
    else return nodeFinder.getCoveringNode();
  }

  public static ASTNode[] findNodes(SearchMatch[] searchResults, CompilationUnit cuNode) {
    List<ASTNode> result = new ArrayList<ASTNode>(searchResults.length);
    for (int i = 0; i < searchResults.length; i++) {
      ASTNode node = findNode(searchResults[i], cuNode);
      if (node != null) result.add(node);
    }
    return result.toArray(new ASTNode[result.size()]);
  }

  public static ASTNode findNode(SearchMatch searchResult, CompilationUnit cuNode) {
    ASTNode selectedNode =
        NodeFinder.perform(cuNode, searchResult.getOffset(), searchResult.getLength());
    if (selectedNode == null) return null;
    if (selectedNode.getParent() == null) return null;
    return selectedNode;
  }
}

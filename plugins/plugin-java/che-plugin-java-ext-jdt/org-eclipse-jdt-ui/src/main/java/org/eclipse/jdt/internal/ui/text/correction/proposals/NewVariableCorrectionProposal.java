/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.JavadocTagsSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.swt.graphics.Image;

public class NewVariableCorrectionProposal extends LinkedCorrectionProposal {

  public static final int LOCAL = 1;
  public static final int FIELD = 2;
  public static final int PARAM = 3;

  public static final int CONST_FIELD = 4;
  public static final int ENUM_CONST = 5;

  private static final String KEY_NAME = "name"; // $NON-NLS-1$
  private static final String KEY_TYPE = "type"; // $NON-NLS-1$
  private static final String KEY_INITIALIZER = "initializer"; // $NON-NLS-1$

  private final int fVariableKind;
  private final SimpleName fOriginalNode;
  private final ITypeBinding fSenderBinding;

  public NewVariableCorrectionProposal(
      String label,
      ICompilationUnit cu,
      int variableKind,
      SimpleName node,
      ITypeBinding senderBinding,
      int relevance,
      Image image) {
    super(label, cu, null, relevance, image);
    if (senderBinding == null) {
      Assert.isTrue(variableKind == PARAM || variableKind == LOCAL);
    } else {
      Assert.isTrue(Bindings.isDeclarationBinding(senderBinding));
    }

    fVariableKind = variableKind;
    fOriginalNode = node;
    fSenderBinding = senderBinding;
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    CompilationUnit cu = ASTResolving.findParentCompilationUnit(fOriginalNode);
    switch (fVariableKind) {
      case PARAM:
        return doAddParam(cu);
      case FIELD:
      case CONST_FIELD:
        return doAddField(cu);
      case LOCAL:
        return doAddLocal(cu);
      case ENUM_CONST:
        return doAddEnumConst(cu);
      default:
        throw new IllegalArgumentException(
            "Unsupported variable kind: " + fVariableKind); // $NON-NLS-1$
    }
  }

  private ASTRewrite doAddParam(CompilationUnit cu) {
    AST ast = cu.getAST();
    SimpleName node = fOriginalNode;

    BodyDeclaration decl = ASTResolving.findParentBodyDeclaration(node);
    if (decl instanceof MethodDeclaration) {
      MethodDeclaration methodDeclaration = (MethodDeclaration) decl;

      ASTRewrite rewrite = ASTRewrite.create(ast);

      ImportRewrite imports = createImportRewrite((CompilationUnit) decl.getRoot());
      ImportRewriteContext importRewriteContext =
          new ContextSensitiveImportRewriteContext(decl, imports);

      SingleVariableDeclaration newDecl = ast.newSingleVariableDeclaration();
      newDecl.setType(
          evaluateVariableType(
              ast, imports, importRewriteContext, methodDeclaration.resolveBinding()));
      newDecl.setName(ast.newSimpleName(node.getIdentifier()));

      ListRewrite listRewriter =
          rewrite.getListRewrite(decl, MethodDeclaration.PARAMETERS_PROPERTY);
      listRewriter.insertLast(newDecl, null);

      addLinkedPosition(rewrite.track(node), true, KEY_NAME);

      // add javadoc tag
      Javadoc javadoc = methodDeclaration.getJavadoc();
      if (javadoc != null) {
        HashSet<String> leadingNames = new HashSet<String>();
        for (Iterator<SingleVariableDeclaration> iter = methodDeclaration.parameters().iterator();
            iter.hasNext(); ) {
          SingleVariableDeclaration curr = iter.next();
          leadingNames.add(curr.getName().getIdentifier());
        }
        SimpleName newTagRef = ast.newSimpleName(node.getIdentifier());

        TagElement newTagElement = ast.newTagElement();
        newTagElement.setTagName(TagElement.TAG_PARAM);
        newTagElement.fragments().add(newTagRef);
        TextElement commentStart = ast.newTextElement();
        newTagElement.fragments().add(commentStart);

        addLinkedPosition(rewrite.track(newTagRef), false, KEY_NAME);
        addLinkedPosition(rewrite.track(commentStart), false, "comment_start"); // $NON-NLS-1$

        ListRewrite tagsRewriter = rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY);
        JavadocTagsSubProcessor.insertTag(tagsRewriter, newTagElement, leadingNames);
      }

      addLinkedPosition(rewrite.track(newDecl.getType()), false, KEY_TYPE);
      addLinkedPosition(rewrite.track(newDecl.getName()), false, KEY_NAME);

      return rewrite;
    }
    return null;
  }

  private boolean isAssigned(Statement statement, SimpleName name) {
    if (statement instanceof ExpressionStatement) {
      ExpressionStatement exstat = (ExpressionStatement) statement;
      if (exstat.getExpression() instanceof Assignment) {
        Assignment assignment = (Assignment) exstat.getExpression();
        return assignment.getLeftHandSide() == name;
      }
    }
    return false;
  }

  private boolean isForStatementInit(Statement statement, SimpleName name) {
    if (statement instanceof ForStatement) {
      ForStatement forStatement = (ForStatement) statement;
      List<Expression> list = forStatement.initializers();
      if (list.size() == 1 && list.get(0) instanceof Assignment) {
        Assignment assignment = (Assignment) list.get(0);
        return assignment.getLeftHandSide() == name;
      }
    }
    return false;
  }

  private boolean isEnhancedForStatementVariable(Statement statement, SimpleName name) {
    if (statement instanceof EnhancedForStatement) {
      EnhancedForStatement forStatement = (EnhancedForStatement) statement;
      SingleVariableDeclaration param = forStatement.getParameter();
      return param.getType()
          == name.getParent(); // strange recovery, see https://bugs.eclipse.org/180456
    }
    return false;
  }

  private ASTRewrite doAddLocal(CompilationUnit cu) {
    AST ast = cu.getAST();

    Block body;
    BodyDeclaration decl = ASTResolving.findParentBodyDeclaration(fOriginalNode);
    IBinding targetContext = null;
    if (decl instanceof MethodDeclaration) {
      body = (((MethodDeclaration) decl).getBody());
      targetContext = ((MethodDeclaration) decl).resolveBinding();
    } else if (decl instanceof Initializer) {
      body = (((Initializer) decl).getBody());
      targetContext = Bindings.getBindingOfParentType(decl);
    } else {
      return null;
    }
    ASTRewrite rewrite = ASTRewrite.create(ast);

    ImportRewrite imports = createImportRewrite((CompilationUnit) decl.getRoot());

    SimpleName[] names = getAllReferences(body);
    ASTNode dominant = getDominantNode(names);

    Statement dominantStatement = ASTResolving.findParentStatement(dominant);
    if (ASTNodes.isControlStatementBody(dominantStatement.getLocationInParent())) {
      dominantStatement = (Statement) dominantStatement.getParent();
    }

    SimpleName node = names[0];
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(node, imports);

    if (isAssigned(dominantStatement, node)) {
      // x = 1; -> int x = 1;
      Assignment assignment = (Assignment) node.getParent();

      // trick to avoid comment removal around the statement: keep the expression statement
      // and replace the assignment with an VariableDeclarationExpression
      VariableDeclarationFragment newDeclFrag = ast.newVariableDeclarationFragment();
      VariableDeclarationExpression newDecl = ast.newVariableDeclarationExpression(newDeclFrag);
      newDecl.setType(evaluateVariableType(ast, imports, importRewriteContext, targetContext));

      Expression placeholder = (Expression) rewrite.createCopyTarget(assignment.getRightHandSide());
      newDeclFrag.setInitializer(placeholder);
      newDeclFrag.setName(ast.newSimpleName(node.getIdentifier()));
      rewrite.replace(assignment, newDecl, null);

      addLinkedPosition(rewrite.track(newDecl.getType()), false, KEY_TYPE);
      addLinkedPosition(rewrite.track(newDeclFrag.getName()), true, KEY_NAME);

      setEndPosition(rewrite.track(assignment.getParent()));

      return rewrite;
    } else if ((dominant != dominantStatement) && isForStatementInit(dominantStatement, node)) {
      //	for (x = 1;;) ->for (int x = 1;;)

      Assignment assignment = (Assignment) node.getParent();

      VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
      VariableDeclarationExpression expression = ast.newVariableDeclarationExpression(frag);
      frag.setName(ast.newSimpleName(node.getIdentifier()));
      Expression placeholder = (Expression) rewrite.createCopyTarget(assignment.getRightHandSide());
      frag.setInitializer(placeholder);
      expression.setType(evaluateVariableType(ast, imports, importRewriteContext, targetContext));

      rewrite.replace(assignment, expression, null);

      addLinkedPosition(rewrite.track(expression.getType()), false, KEY_TYPE);
      addLinkedPosition(rewrite.track(frag.getName()), true, KEY_NAME);

      setEndPosition(rewrite.track(expression));

      return rewrite;

    } else if ((dominant != dominantStatement)
        && isEnhancedForStatementVariable(dominantStatement, node)) {
      //	for (x: collectionOfT) -> for (T x: collectionOfT)

      EnhancedForStatement enhancedForStatement = (EnhancedForStatement) dominantStatement;
      SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
      Expression expression = enhancedForStatement.getExpression();

      SimpleName newName = (SimpleName) rewrite.createMoveTarget(node);
      rewrite.set(parameter, SingleVariableDeclaration.NAME_PROPERTY, newName, null);

      ITypeBinding elementBinding = null;
      ITypeBinding typeBinding = expression.resolveTypeBinding();
      if (typeBinding != null) {
        if (typeBinding.isArray()) {
          elementBinding = typeBinding.getElementType();
        } else {
          ITypeBinding iterable =
              Bindings.findTypeInHierarchy(typeBinding, "java.lang.Iterable"); // $NON-NLS-1$
          if (iterable != null) {
            ITypeBinding[] typeArguments = iterable.getTypeArguments();
            if (typeArguments.length == 1) {
              elementBinding = typeArguments[0];
              elementBinding = Bindings.normalizeForDeclarationUse(elementBinding, ast);
            }
          }
        }
      }
      Type type;
      if (elementBinding != null) {
        type = imports.addImport(elementBinding, ast, importRewriteContext);
      } else {
        type = ast.newSimpleType(ast.newSimpleName("Object")); // $NON-NLS-1$
      }

      rewrite.set(parameter, SingleVariableDeclaration.TYPE_PROPERTY, type, null);

      addLinkedPosition(rewrite.track(type), false, KEY_TYPE);
      addLinkedPosition(rewrite.track(newName), true, KEY_NAME);

      setEndPosition(rewrite.track(expression));

      return rewrite;
    }

    //	foo(x) -> int x; foo(x)

    VariableDeclarationFragment newDeclFrag = ast.newVariableDeclarationFragment();
    VariableDeclarationStatement newDecl = ast.newVariableDeclarationStatement(newDeclFrag);

    newDeclFrag.setName(ast.newSimpleName(node.getIdentifier()));
    newDecl.setType(evaluateVariableType(ast, imports, importRewriteContext, targetContext));
    //		newDeclFrag.setInitializer(ASTNodeFactory.newDefaultExpression(ast, newDecl.getType(), 0));

    addLinkedPosition(rewrite.track(newDecl.getType()), false, KEY_TYPE);
    addLinkedPosition(rewrite.track(node), true, KEY_NAME);
    addLinkedPosition(rewrite.track(newDeclFrag.getName()), false, KEY_NAME);

    Statement statement = dominantStatement;
    List<? extends ASTNode> list = ASTNodes.getContainingList(statement);
    while (list == null
        && statement.getParent() instanceof Statement) { // parent must be if, for or while
      statement = (Statement) statement.getParent();
      list = ASTNodes.getContainingList(statement);
    }
    if (list != null) {
      ASTNode parent = statement.getParent();
      StructuralPropertyDescriptor childProperty = statement.getLocationInParent();
      if (childProperty.isChildListProperty()) {
        rewrite
            .getListRewrite(parent, (ChildListPropertyDescriptor) childProperty)
            .insertBefore(newDecl, statement, null);
        return rewrite;
      } else {
        return null;
      }
    }
    return rewrite;
  }

  private SimpleName[] getAllReferences(Block body) {
    SimpleName[] names = LinkedNodeFinder.findByProblems(body, fOriginalNode);
    if (names == null) {
      return new SimpleName[] {fOriginalNode};
    }
    if (names.length > 1) {
      Arrays.sort(
          names,
          new Comparator<SimpleName>() {
            public int compare(SimpleName s1, SimpleName s2) {
              return s1.getStartPosition() - s2.getStartPosition();
            }
          });
    }
    return names;
  }

  private ASTNode getDominantNode(SimpleName[] names) {
    ASTNode dominator = names[0]; // ASTResolving.findParentStatement(names[0]);
    for (int i = 1; i < names.length; i++) {
      ASTNode curr = names[i]; // ASTResolving.findParentStatement(names[i]);
      if (curr != dominator) {
        ASTNode parent = getCommonParent(curr, dominator);

        if (curr.getStartPosition() < dominator.getStartPosition()) {
          dominator = curr;
        }
        while (dominator.getParent() != parent) {
          dominator = dominator.getParent();
        }
      }
    }
    int parentKind = dominator.getParent().getNodeType();
    if (parentKind != ASTNode.BLOCK && parentKind != ASTNode.FOR_STATEMENT) {
      return dominator.getParent();
    }
    return dominator;
  }

  private ASTNode getCommonParent(ASTNode node1, ASTNode node2) {
    ASTNode parent = node1.getParent();
    while (parent != null && !ASTNodes.isParent(node2, parent)) {
      parent = parent.getParent();
    }
    return parent;
  }

  private ASTRewrite doAddField(CompilationUnit astRoot) {
    SimpleName node = fOriginalNode;
    boolean isInDifferentCU = false;

    ASTNode newTypeDecl = astRoot.findDeclaringNode(fSenderBinding);
    if (newTypeDecl == null) {
      astRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
      newTypeDecl = astRoot.findDeclaringNode(fSenderBinding.getKey());
      isInDifferentCU = true;
    }
    ImportRewrite imports = createImportRewrite(astRoot);
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(
            ASTResolving.findParentBodyDeclaration(node), imports);

    if (newTypeDecl != null) {
      AST ast = newTypeDecl.getAST();

      ASTRewrite rewrite = ASTRewrite.create(ast);

      VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
      fragment.setName(ast.newSimpleName(node.getIdentifier()));

      Type type = evaluateVariableType(ast, imports, importRewriteContext, fSenderBinding);

      FieldDeclaration newDecl = ast.newFieldDeclaration(fragment);
      newDecl.setType(type);
      newDecl
          .modifiers()
          .addAll(ASTNodeFactory.newModifiers(ast, evaluateFieldModifiers(newTypeDecl)));

      if (fSenderBinding.isInterface() || fVariableKind == CONST_FIELD) {
        fragment.setInitializer(ASTNodeFactory.newDefaultExpression(ast, type, 0));
      }

      ChildListPropertyDescriptor property = ASTNodes.getBodyDeclarationsProperty(newTypeDecl);
      List<BodyDeclaration> decls =
          ASTNodes.<BodyDeclaration>getChildListProperty(newTypeDecl, property);

      int maxOffset = isInDifferentCU ? -1 : node.getStartPosition();

      int insertIndex = findFieldInsertIndex(decls, newDecl, maxOffset);

      ListRewrite listRewriter = rewrite.getListRewrite(newTypeDecl, property);
      listRewriter.insertAt(newDecl, insertIndex, null);

      ModifierCorrectionSubProcessor.installLinkedVisibilityProposals(
          getLinkedProposalModel(), rewrite, newDecl.modifiers(), fSenderBinding.isInterface());

      addLinkedPosition(rewrite.track(newDecl.getType()), false, KEY_TYPE);
      if (!isInDifferentCU) {
        addLinkedPosition(rewrite.track(node), true, KEY_NAME);
      }
      addLinkedPosition(rewrite.track(fragment.getName()), false, KEY_NAME);

      if (fragment.getInitializer() != null) {
        addLinkedPosition(rewrite.track(fragment.getInitializer()), false, KEY_INITIALIZER);
      }
      return rewrite;
    }
    return null;
  }

  private int findFieldInsertIndex(
      List<BodyDeclaration> decls, FieldDeclaration newDecl, int maxOffset) {
    if (maxOffset != -1) {
      for (int i = decls.size() - 1; i >= 0; i--) {
        BodyDeclaration curr = decls.get(i);
        if (maxOffset > curr.getStartPosition() + curr.getLength()) {
          return ASTNodes.getInsertionIndex(newDecl, decls.subList(0, i + 1));
        }
      }
      return 0;
    }
    return ASTNodes.getInsertionIndex(newDecl, decls);
  }

  private Type evaluateVariableType(
      AST ast,
      ImportRewrite imports,
      ImportRewriteContext importRewriteContext,
      IBinding targetContext) {
    if (fOriginalNode.getParent() instanceof MethodInvocation) {
      MethodInvocation parent = (MethodInvocation) fOriginalNode.getParent();
      if (parent.getExpression() == fOriginalNode) {
        // _x_.foo() -> guess qualifier type by looking for a type with method 'foo'
        ITypeBinding[] bindings =
            ASTResolving.getQualifierGuess(
                fOriginalNode.getRoot(),
                parent.getName().getIdentifier(),
                parent.arguments(),
                targetContext);
        if (bindings.length > 0) {
          for (int i = 0; i < bindings.length; i++) {
            addLinkedPositionProposal(KEY_TYPE, bindings[i]);
          }
          return imports.addImport(bindings[0], ast, importRewriteContext);
        }
      }
    }

    ITypeBinding binding = ASTResolving.guessBindingForReference(fOriginalNode);
    if (binding != null) {
      if (binding.isWildcardType()) {
        binding = ASTResolving.normalizeWildcardType(binding, isVariableAssigned(), ast);
        if (binding == null) {
          // only null binding applies
          binding = ast.resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
        }
      }

      if (isVariableAssigned()) {
        ITypeBinding[] typeProposals = ASTResolving.getRelaxingTypes(ast, binding);
        for (int i = 0; i < typeProposals.length; i++) {
          addLinkedPositionProposal(KEY_TYPE, typeProposals[i]);
        }
      }
      return imports.addImport(binding, ast, importRewriteContext);
    }
    // no binding, find type AST node instead -> ABC a= x-> use 'ABC' as is
    Type type = ASTResolving.guessTypeForReference(ast, fOriginalNode);
    if (type != null) {
      return type;
    }
    if (fVariableKind == CONST_FIELD) {
      return ast.newSimpleType(ast.newSimpleName("String")); // $NON-NLS-1$
    }
    return ast.newSimpleType(ast.newSimpleName("Object")); // $NON-NLS-1$
  }

  private boolean isVariableAssigned() {
    ASTNode parent = fOriginalNode.getParent();
    return (parent instanceof Assignment)
        && (fOriginalNode == ((Assignment) parent).getLeftHandSide());
  }

  private int evaluateFieldModifiers(ASTNode newTypeDecl) {
    if (fSenderBinding.isAnnotation()) {
      return 0;
    }
    if (fSenderBinding.isInterface()) {
      // for interface members copy the modifiers from an existing field
      FieldDeclaration[] fieldDecls = ((TypeDeclaration) newTypeDecl).getFields();
      if (fieldDecls.length > 0) {
        return fieldDecls[0].getModifiers();
      }
      return 0;
    }
    int modifiers = 0;

    if (fVariableKind == CONST_FIELD) {
      modifiers |= Modifier.FINAL | Modifier.STATIC;
    } else {
      ASTNode parent = fOriginalNode.getParent();
      if (parent instanceof QualifiedName) {
        IBinding qualifierBinding = ((QualifiedName) parent).getQualifier().resolveBinding();
        if (qualifierBinding instanceof ITypeBinding) {
          modifiers |= Modifier.STATIC;
        }
      } else if (ASTResolving.isInStaticContext(fOriginalNode)) {
        modifiers |= Modifier.STATIC;
      }
    }
    ASTNode node = ASTResolving.findParentType(fOriginalNode, true);
    if (newTypeDecl.equals(node)) {
      modifiers |= Modifier.PRIVATE;
    } else if (node instanceof AnonymousClassDeclaration) {
      modifiers |= Modifier.PROTECTED;
    } else {
      modifiers |= Modifier.PUBLIC;
    }

    return modifiers;
  }

  private ASTRewrite doAddEnumConst(CompilationUnit astRoot) {
    SimpleName node = fOriginalNode;

    ASTNode newTypeDecl = astRoot.findDeclaringNode(fSenderBinding);
    if (newTypeDecl == null) {
      astRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
      newTypeDecl = astRoot.findDeclaringNode(fSenderBinding.getKey());
    }

    if (newTypeDecl != null) {
      AST ast = newTypeDecl.getAST();

      ASTRewrite rewrite = ASTRewrite.create(ast);

      EnumConstantDeclaration constDecl = ast.newEnumConstantDeclaration();
      constDecl.setName(ast.newSimpleName(node.getIdentifier()));

      ListRewrite listRewriter =
          rewrite.getListRewrite(newTypeDecl, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
      listRewriter.insertLast(constDecl, null);

      addLinkedPosition(rewrite.track(constDecl.getName()), false, KEY_NAME);

      return rewrite;
    }
    return null;
  }

  /**
   * Returns the variable kind.
   *
   * @return the variable kind
   */
  public int getVariableKind() {
    return fVariableKind;
  }
}

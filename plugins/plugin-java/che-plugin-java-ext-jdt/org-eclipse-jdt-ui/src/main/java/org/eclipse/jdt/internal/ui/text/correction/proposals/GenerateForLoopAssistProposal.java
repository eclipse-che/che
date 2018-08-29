/**
 * ***************************************************************************** Copyright (c) 2014
 * Yatta Solutions GmbH and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Lukas Hanke <hanke@yatta.de> - Bug 241696 [quick fix] quickfix to iterate over a
 * collection - https://bugs.eclipse.org/bugs/show_bug.cgi?id=241696 Lukas Hanke <hanke@yatta.de> -
 * Bug 430818 [1.8][quick fix] Quick fix for "for loop" is not shown for bare local
 * variable/argument/field - https://bugs.eclipse.org/bugs/show_bug.cgi?id=430818
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.IProposalRelevance;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;

/**
 * Generates a proposal for quick assist, to loop over a variable or method result which represents
 * an {@link Iterable} or an array.
 */
public class GenerateForLoopAssistProposal extends LinkedCorrectionProposal {

  public static final int GENERATE_FOREACH = 0;

  public static final int GENERATE_ITERATOR_FOR = 1;

  public static final int GENERATE_ITERATE_ARRAY = 2;

  public static final int GENERATE_ITERATE_LIST = 3;

  private ASTNode fCurrentNode;

  private Expression fCurrentExpression;

  private ITypeBinding fExpressionType;

  private int fLoopTypeToGenerate = -1;

  /**
   * Creates an instance of a {@link GenerateForLoopAssistProposal}.
   *
   * @param cu the current {@link ICompilationUnit}
   * @param expressionType the {@link ITypeBinding} of the element to iterate over
   * @param currentNode the {@link ASTNode} instance representing the statement on which the assist
   *     was called
   * @param currentExpression the {@link Expression} contained in the currentNode
   * @param loopTypeToGenerate the type of the loop to generate, possible values are {@link
   *     GenerateForLoopAssistProposal#GENERATE_FOREACH}, {@link
   *     GenerateForLoopAssistProposal#GENERATE_ITERATOR_FOR} or {@link
   *     GenerateForLoopAssistProposal#GENERATE_ITERATE_ARRAY}
   */
  public GenerateForLoopAssistProposal(
      ICompilationUnit cu,
      ITypeBinding expressionType,
      ASTNode currentNode,
      Expression currentExpression,
      int loopTypeToGenerate) {
    super(
        "",
        cu,
        null,
        IProposalRelevance.GENERATE_FOR_LOOP,
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE)); // $NON-NLS-1$
    fCurrentNode = currentNode;
    fCurrentExpression = currentExpression;
    fLoopTypeToGenerate = loopTypeToGenerate;
    fExpressionType = expressionType;

    switch (loopTypeToGenerate) {
      case GenerateForLoopAssistProposal.GENERATE_FOREACH:
        setDisplayName(CorrectionMessages.QuickAssistProcessor_generate_enhanced_for_loop);
        setRelevance(IProposalRelevance.GENERATE_ENHANCED_FOR_LOOP);
        break;
      case GenerateForLoopAssistProposal.GENERATE_ITERATOR_FOR:
        setDisplayName(CorrectionMessages.QuickAssistProcessor_generate_iterator_for_loop);
        break;
      case GenerateForLoopAssistProposal.GENERATE_ITERATE_ARRAY:
        setDisplayName(CorrectionMessages.QuickAssistProcessor_generate_for_loop);
        break;
      case GenerateForLoopAssistProposal.GENERATE_ITERATE_LIST:
        setDisplayName(CorrectionMessages.QuickAssistProcessor_generate_index_for_loop);
        break;
      default:
        break;
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() throws CoreException {

    AST ast = fCurrentNode.getAST();
    createImportRewrite((CompilationUnit) fCurrentExpression.getRoot());

    switch (fLoopTypeToGenerate) {
      case GenerateForLoopAssistProposal.GENERATE_FOREACH:
        return generateForEachRewrite(ast);
      case GenerateForLoopAssistProposal.GENERATE_ITERATOR_FOR:
        return generateIteratorBasedForRewrite(ast);
      case GenerateForLoopAssistProposal.GENERATE_ITERATE_ARRAY:
        return generateForRewrite(ast);
      case GenerateForLoopAssistProposal.GENERATE_ITERATE_LIST:
        return generateIndexBasedForRewrite(ast);
      default:
        return null;
    }
  }

  /**
   * Helper to generate a <code>foreach</code> loop to iterate over an {@link Iterable}.
   *
   * @param ast the {@link AST} instance to rewrite the loop to
   * @return the complete {@link ASTRewrite} object
   */
  private ASTRewrite generateForEachRewrite(AST ast) {

    EnhancedForStatement loopStatement = ast.newEnhancedForStatement();

    ASTRewrite rewrite = ASTRewrite.create(ast);
    ITypeBinding loopOverType = extractElementType(ast);

    // generate name proposals and add them to the variable declaration
    SimpleName forDeclarationName =
        resolveLinkedVariableNameWithProposals(rewrite, loopOverType.getName(), null, true);

    SingleVariableDeclaration forLoopInitializer = ast.newSingleVariableDeclaration();
    forLoopInitializer.setType(
        getImportRewrite()
            .addImport(
                loopOverType,
                ast,
                new ContextSensitiveImportRewriteContext(fCurrentNode, getImportRewrite())));
    forLoopInitializer.setName(forDeclarationName);

    loopStatement.setParameter(forLoopInitializer);
    loopStatement.setExpression((Expression) rewrite.createCopyTarget(fCurrentExpression));

    Block forLoopBody = ast.newBlock();
    forLoopBody.statements().add(createBlankLineStatementWithCursorPosition(rewrite));

    loopStatement.setBody(forLoopBody);

    rewrite.replace(fCurrentNode, loopStatement, null);

    return rewrite;
  }

  /**
   * Helper to generate an iterator based <code>for</code> loop to iterate over an {@link Iterable}.
   *
   * @param ast the {@link AST} instance to rewrite the loop to
   * @return the complete {@link ASTRewrite} object
   */
  private ASTRewrite generateIteratorBasedForRewrite(AST ast) {
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ForStatement loopStatement = ast.newForStatement();

    ITypeBinding loopOverType = extractElementType(ast);

    SimpleName loopVariableName =
        resolveLinkedVariableNameWithProposals(rewrite, "iterator", null, true); // $NON-NLS-1$
    loopStatement.initializers().add(getIteratorBasedForInitializer(rewrite, loopVariableName));

    MethodInvocation loopExpression = ast.newMethodInvocation();
    loopExpression.setName(ast.newSimpleName("hasNext")); // $NON-NLS-1$
    SimpleName expressionName = ast.newSimpleName(loopVariableName.getIdentifier());
    addLinkedPosition(
        rewrite.track(expressionName), LinkedPositionGroup.NO_STOP, expressionName.getIdentifier());
    loopExpression.setExpression(expressionName);

    loopStatement.setExpression(loopExpression);

    Block forLoopBody = ast.newBlock();
    Assignment assignResolvedVariable =
        getIteratorBasedForBodyAssignment(rewrite, loopOverType, loopVariableName);
    forLoopBody.statements().add(ast.newExpressionStatement(assignResolvedVariable));
    forLoopBody.statements().add(createBlankLineStatementWithCursorPosition(rewrite));

    loopStatement.setBody(forLoopBody);

    rewrite.replace(fCurrentNode, loopStatement, null);

    return rewrite;
  }

  /**
   * Generates the initializer for an iterator based <code>for</code> loop, which declares and
   * initializes the variable to loop over.
   *
   * @param rewrite the instance of {@link ASTRewrite}
   * @param loopVariableName the proposed name of the loop variable
   * @return a {@link VariableDeclarationExpression} to use as initializer
   */
  private VariableDeclarationExpression getIteratorBasedForInitializer(
      ASTRewrite rewrite, SimpleName loopVariableName) {
    AST ast = rewrite.getAST();
    IMethodBinding iteratorMethodBinding =
        Bindings.findMethodInHierarchy(
            fExpressionType, "iterator", new ITypeBinding[] {}); // $NON-NLS-1$
    // initializing fragment
    VariableDeclarationFragment varDeclarationFragment = ast.newVariableDeclarationFragment();
    varDeclarationFragment.setName(loopVariableName);
    MethodInvocation iteratorExpression = ast.newMethodInvocation();
    iteratorExpression.setName(ast.newSimpleName(iteratorMethodBinding.getName()));
    iteratorExpression.setExpression((Expression) rewrite.createCopyTarget(fCurrentExpression));
    varDeclarationFragment.setInitializer(iteratorExpression);

    // declaration
    VariableDeclarationExpression varDeclarationExpression =
        ast.newVariableDeclarationExpression(varDeclarationFragment);
    varDeclarationExpression.setType(
        getImportRewrite()
            .addImport(
                iteratorMethodBinding.getReturnType(),
                ast,
                new ContextSensitiveImportRewriteContext(fCurrentNode, getImportRewrite())));

    return varDeclarationExpression;
  }

  /**
   * Generates the Assignment in an iterator based for, used in the first statement of an iterator
   * based <code>for</code> loop body, to retrieve the next element of the {@link Iterable}
   * instance.
   *
   * @param rewrite the current instance of {@link ASTRewrite}
   * @param loopOverType the {@link ITypeBinding} of the loop variable
   * @param loopVariableName the name of the loop variable
   * @return an {@link Assignment}, which retrieves the next element of the {@link Iterable} using
   *     the active {@link Iterator}
   */
  private Assignment getIteratorBasedForBodyAssignment(
      ASTRewrite rewrite, ITypeBinding loopOverType, SimpleName loopVariableName) {
    AST ast = rewrite.getAST();
    Assignment assignResolvedVariable = ast.newAssignment();

    // left hand side
    SimpleName resolvedVariableName =
        resolveLinkedVariableNameWithProposals(
            rewrite, loopOverType.getName(), loopVariableName.getIdentifier(), false);
    VariableDeclarationFragment resolvedVariableDeclarationFragment =
        ast.newVariableDeclarationFragment();
    resolvedVariableDeclarationFragment.setName(resolvedVariableName);
    VariableDeclarationExpression resolvedVariableDeclaration =
        ast.newVariableDeclarationExpression(resolvedVariableDeclarationFragment);
    resolvedVariableDeclaration.setType(
        getImportRewrite()
            .addImport(
                loopOverType,
                ast,
                new ContextSensitiveImportRewriteContext(fCurrentNode, getImportRewrite())));
    assignResolvedVariable.setLeftHandSide(resolvedVariableDeclaration);

    // right hand side
    MethodInvocation invokeIteratorNextExpression = ast.newMethodInvocation();
    invokeIteratorNextExpression.setName(ast.newSimpleName("next")); // $NON-NLS-1$
    SimpleName currentElementName = ast.newSimpleName(loopVariableName.getIdentifier());
    addLinkedPosition(
        rewrite.track(currentElementName),
        LinkedPositionGroup.NO_STOP,
        currentElementName.getIdentifier());
    invokeIteratorNextExpression.setExpression(currentElementName);
    assignResolvedVariable.setRightHandSide(invokeIteratorNextExpression);

    assignResolvedVariable.setOperator(Assignment.Operator.ASSIGN);

    return assignResolvedVariable;
  }

  /**
   * Helper to generate an index based <code>for</code> loop to iterate over an array.
   *
   * @param ast the current {@link AST} instance to generate the {@link ASTRewrite} for
   * @return an applicable {@link ASTRewrite} instance
   */
  private ASTRewrite generateForRewrite(AST ast) {
    ASTRewrite rewrite = ASTRewrite.create(ast);

    ForStatement loopStatement = ast.newForStatement();
    SimpleName loopVariableName =
        resolveLinkedVariableNameWithProposals(rewrite, "int", null, true); // $NON-NLS-1$
    loopStatement.initializers().add(getForInitializer(ast, loopVariableName));

    FieldAccess getArrayLengthExpression = ast.newFieldAccess();
    getArrayLengthExpression.setExpression(
        (Expression) rewrite.createCopyTarget(fCurrentExpression));
    getArrayLengthExpression.setName(ast.newSimpleName("length")); // $NON-NLS-1$

    loopStatement.setExpression(
        getLinkedInfixExpression(
            rewrite,
            loopVariableName.getIdentifier(),
            getArrayLengthExpression,
            InfixExpression.Operator.LESS));
    loopStatement
        .updaters()
        .add(getLinkedIncrementExpression(rewrite, loopVariableName.getIdentifier()));

    Block forLoopBody = ast.newBlock();
    forLoopBody
        .statements()
        .add(ast.newExpressionStatement(getForBodyAssignment(rewrite, loopVariableName)));
    forLoopBody.statements().add(createBlankLineStatementWithCursorPosition(rewrite));
    loopStatement.setBody(forLoopBody);
    rewrite.replace(fCurrentNode, loopStatement, null);

    return rewrite;
  }

  /**
   * Creates an {@link Assignment} as first expression appearing in a <code>for</code> loop's body.
   * This Assignment declares a local variable and initializes it using the array's current element
   * identified by the loop index.
   *
   * @param rewrite the current {@link ASTRewrite} instance
   * @param loopVariableName the name of the index variable in String representation
   * @return a completed {@link Assignment} containing the mentioned declaration and initialization
   */
  private Assignment getForBodyAssignment(ASTRewrite rewrite, SimpleName loopVariableName) {
    AST ast = rewrite.getAST();
    ITypeBinding loopOverType = extractElementType(ast);

    Assignment assignResolvedVariable = ast.newAssignment();

    // left hand side
    SimpleName resolvedVariableName =
        resolveLinkedVariableNameWithProposals(
            rewrite, loopOverType.getName(), loopVariableName.getIdentifier(), false);
    VariableDeclarationFragment resolvedVariableDeclarationFragment =
        ast.newVariableDeclarationFragment();
    resolvedVariableDeclarationFragment.setName(resolvedVariableName);
    VariableDeclarationExpression resolvedVariableDeclaration =
        ast.newVariableDeclarationExpression(resolvedVariableDeclarationFragment);
    resolvedVariableDeclaration.setType(
        getImportRewrite()
            .addImport(
                loopOverType,
                ast,
                new ContextSensitiveImportRewriteContext(fCurrentNode, getImportRewrite())));
    assignResolvedVariable.setLeftHandSide(resolvedVariableDeclaration);

    // right hand side
    ArrayAccess access = ast.newArrayAccess();
    access.setArray((Expression) rewrite.createCopyTarget(fCurrentExpression));
    SimpleName indexName = ast.newSimpleName(loopVariableName.getIdentifier());
    addLinkedPosition(
        rewrite.track(indexName), LinkedPositionGroup.NO_STOP, indexName.getIdentifier());
    access.setIndex(indexName);
    assignResolvedVariable.setRightHandSide(access);

    assignResolvedVariable.setOperator(Assignment.Operator.ASSIGN);

    return assignResolvedVariable;
  }

  /**
   * Creates an {@link InfixExpression} which is linked to the group of the variableToIncrement.
   *
   * @param rewrite the current {@link ASTRewrite} instance
   * @param variableToIncrement the name of the variable to generate the {@link InfixExpression} for
   * @param rightHandSide the right hand side expression which shall be included in the {@link
   *     InfixExpression}
   * @param operator the {@link org.eclipse.jdt.core.dom.InfixExpression.Operator} to use in the
   *     {@link InfixExpression} to create
   * @return a filled, new {@link InfixExpression} instance
   */
  private InfixExpression getLinkedInfixExpression(
      ASTRewrite rewrite,
      String variableToIncrement,
      Expression rightHandSide,
      InfixExpression.Operator operator) {
    AST ast = rewrite.getAST();
    InfixExpression loopExpression = ast.newInfixExpression();
    SimpleName name = ast.newSimpleName(variableToIncrement);
    addLinkedPosition(rewrite.track(name), LinkedPositionGroup.NO_STOP, name.getIdentifier());
    loopExpression.setLeftOperand(name);

    loopExpression.setOperator(operator);

    loopExpression.setRightOperand(rightHandSide);
    return loopExpression;
  }

  /**
   * Creates a {@link PostfixExpression} used to increment the loop variable of a <code>for</code>
   * loop to iterate over an array.
   *
   * @param rewrite the current {@link ASTRewrite} instance
   * @param variableToIncrement the name of the variable to increment
   * @return a filled {@link PostfixExpression} realizing an incrementation of the specified
   *     variable
   */
  private Expression getLinkedIncrementExpression(ASTRewrite rewrite, String variableToIncrement) {
    AST ast = rewrite.getAST();
    PostfixExpression incrementLoopVariable = ast.newPostfixExpression();
    SimpleName name = ast.newSimpleName(variableToIncrement);
    addLinkedPosition(rewrite.track(name), LinkedPositionGroup.NO_STOP, name.getIdentifier());
    incrementLoopVariable.setOperand(name);
    incrementLoopVariable.setOperator(PostfixExpression.Operator.INCREMENT);
    return incrementLoopVariable;
  }

  /**
   * Generates a {@link VariableDeclarationExpression}, which initializes the loop variable to
   * iterate over an array.
   *
   * @param ast the current {@link AST} instance
   * @param loopVariableName the name of the variable which should be initialized
   * @return a filled {@link VariableDeclarationExpression}, declaring a int variable, which is
   *     initializes with 0
   */
  private VariableDeclarationExpression getForInitializer(AST ast, SimpleName loopVariableName) {
    // initializing fragment
    VariableDeclarationFragment firstDeclarationFragment = ast.newVariableDeclarationFragment();
    firstDeclarationFragment.setName(loopVariableName);
    NumberLiteral startIndex = ast.newNumberLiteral();
    firstDeclarationFragment.setInitializer(startIndex);

    // declaration
    VariableDeclarationExpression variableDeclaration =
        ast.newVariableDeclarationExpression(firstDeclarationFragment);
    PrimitiveType variableType = ast.newPrimitiveType(PrimitiveType.INT);
    variableDeclaration.setType(variableType);

    return variableDeclaration;
  }

  /**
   * Helper to generate an index based <code>for</code> loop to iterate over a {@link List}
   * implementation.
   *
   * @param ast the current {@link AST} instance to generate the {@link ASTRewrite} for
   * @return an applicable {@link ASTRewrite} instance
   */
  private ASTRewrite generateIndexBasedForRewrite(AST ast) {
    ASTRewrite rewrite = ASTRewrite.create(ast);

    ForStatement loopStatement = ast.newForStatement();
    SimpleName loopVariableName =
        resolveLinkedVariableNameWithProposals(rewrite, "int", null, true); // $NON-NLS-1$
    loopStatement.initializers().add(getForInitializer(ast, loopVariableName));

    MethodInvocation listSizeExpression = ast.newMethodInvocation();
    listSizeExpression.setName(ast.newSimpleName("size")); // $NON-NLS-1$
    Expression listExpression = (Expression) rewrite.createCopyTarget(fCurrentExpression);
    listSizeExpression.setExpression(listExpression);

    loopStatement.setExpression(
        getLinkedInfixExpression(
            rewrite,
            loopVariableName.getIdentifier(),
            listSizeExpression,
            InfixExpression.Operator.LESS));
    loopStatement
        .updaters()
        .add(getLinkedIncrementExpression(rewrite, loopVariableName.getIdentifier()));

    Block forLoopBody = ast.newBlock();
    forLoopBody
        .statements()
        .add(ast.newExpressionStatement(getIndexBasedForBodyAssignment(rewrite, loopVariableName)));
    forLoopBody.statements().add(createBlankLineStatementWithCursorPosition(rewrite));
    loopStatement.setBody(forLoopBody);
    rewrite.replace(fCurrentNode, loopStatement, null);

    return rewrite;
  }

  /**
   * Creates an {@link Assignment} as first expression appearing in an index based <code>for</code>
   * loop's body. This Assignment declares a local variable and initializes it using the {@link
   * List}'s current element identified by the loop index.
   *
   * @param rewrite the current {@link ASTRewrite} instance
   * @param loopVariableName the name of the index variable in String representation
   * @return a completed {@link Assignment} containing the mentioned declaration and initialization
   */
  private Expression getIndexBasedForBodyAssignment(
      ASTRewrite rewrite, SimpleName loopVariableName) {
    AST ast = rewrite.getAST();
    ITypeBinding loopOverType = extractElementType(ast);

    Assignment assignResolvedVariable = ast.newAssignment();

    // left hand side
    SimpleName resolvedVariableName =
        resolveLinkedVariableNameWithProposals(
            rewrite, loopOverType.getName(), loopVariableName.getIdentifier(), false);
    VariableDeclarationFragment resolvedVariableDeclarationFragment =
        ast.newVariableDeclarationFragment();
    resolvedVariableDeclarationFragment.setName(resolvedVariableName);
    VariableDeclarationExpression resolvedVariableDeclaration =
        ast.newVariableDeclarationExpression(resolvedVariableDeclarationFragment);
    resolvedVariableDeclaration.setType(
        getImportRewrite()
            .addImport(
                loopOverType,
                ast,
                new ContextSensitiveImportRewriteContext(fCurrentNode, getImportRewrite())));
    assignResolvedVariable.setLeftHandSide(resolvedVariableDeclaration);

    // right hand side
    MethodInvocation invokeGetExpression = ast.newMethodInvocation();
    invokeGetExpression.setName(ast.newSimpleName("get")); // $NON-NLS-1$
    SimpleName indexVariableName = ast.newSimpleName(loopVariableName.getIdentifier());
    addLinkedPosition(
        rewrite.track(indexVariableName),
        LinkedPositionGroup.NO_STOP,
        indexVariableName.getIdentifier());
    invokeGetExpression.arguments().add(indexVariableName);
    invokeGetExpression.setExpression((Expression) rewrite.createCopyTarget(fCurrentExpression));
    assignResolvedVariable.setRightHandSide(invokeGetExpression);

    assignResolvedVariable.setOperator(Assignment.Operator.ASSIGN);

    return assignResolvedVariable;
  }

  /**
   * Resolves name proposals by the given basename and adds a {@link LinkedPosition} to the returned
   * {@link SimpleName} expression.
   *
   * @param rewrite the current instance of an {@link ASTRewrite}
   * @param basename the base string to use for proposal calculation
   * @param excludedName a name that cannot be used for the variable; <code>null</code> if none
   * @param firstLinkedProposal true if the generated name is the first {@link LinkedPosition} to
   *     edit in the current {@link CompilationUnit}, false otherwise
   * @return the linked {@link SimpleName} instance based on the name proposals
   */
  private SimpleName resolveLinkedVariableNameWithProposals(
      ASTRewrite rewrite, String basename, String excludedName, boolean firstLinkedProposal) {
    AST ast = rewrite.getAST();
    String[] nameProposals = getVariableNameProposals(basename, excludedName);
    SimpleName forDeclarationName =
        ast.newSimpleName(nameProposals.length > 0 ? nameProposals[0] : basename);
    for (int i = 0; i < nameProposals.length; i++) {
      addLinkedPositionProposal(forDeclarationName.getIdentifier(), nameProposals[i], null);
    }

    // mark declaration name as editable
    addLinkedPosition(
        rewrite.track(forDeclarationName), firstLinkedProposal, forDeclarationName.getIdentifier());
    return forDeclarationName;
  }

  /**
   * Generates an empty statement, which is shown as blank line and is set as end position for the
   * cursor.
   *
   * @param rewrite the current {@link ASTRewrite} instance
   * @return an empty statement, shown as blank line
   */
  private Statement createBlankLineStatementWithCursorPosition(ASTRewrite rewrite) {
    Statement blankLineStatement =
        (Statement) rewrite.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT); // $NON-NLS-1$
    setEndPosition(rewrite.track(blankLineStatement));
    return blankLineStatement;
  }

  /**
   * Retrieves name proposals for a fresh local variable.
   *
   * @param basename the basename of the proposals
   * @param excludedName a name that cannot be used for the variable; <code>null</code> if none
   * @return an array of proposal strings
   */
  private String[] getVariableNameProposals(String basename, String excludedName) {
    ASTNode surroundingBlock = fCurrentNode;
    while ((surroundingBlock = surroundingBlock.getParent()) != null) {
      if (surroundingBlock instanceof Block) {
        break;
      }
    }
    Collection<String> localUsedNames =
        new ScopeAnalyzer((CompilationUnit) fCurrentExpression.getRoot())
            .getUsedVariableNames(
                surroundingBlock.getStartPosition(), surroundingBlock.getLength());
    if (excludedName != null) {
      localUsedNames.add(excludedName);
    }
    String[] names =
        StubUtility.getLocalNameSuggestions(
            getCompilationUnit().getJavaProject(),
            basename,
            0,
            localUsedNames.toArray(new String[localUsedNames.size()]));
    return names;
  }

  /**
   * Extracts the type parameter of the variable contained in fCurrentExpression or the elements
   * type to iterate over an array using <code>foreach</code>.
   *
   * @param ast the current {@link AST} instance
   * @return the {@link ITypeBinding} of the elements to iterate over
   */
  private ITypeBinding extractElementType(AST ast) {
    if (fExpressionType.isArray()) {
      return Bindings.normalizeForDeclarationUse(fExpressionType.getElementType(), ast);
    }

    // extract elements type directly out of the bindings
    IMethodBinding iteratorMethodBinding =
        Bindings.findMethodInHierarchy(
            fExpressionType, "iterator", new ITypeBinding[] {}); // $NON-NLS-1$
    IMethodBinding iteratorNextMethodBinding =
        Bindings.findMethodInHierarchy(
            iteratorMethodBinding.getReturnType(), "next", new ITypeBinding[] {}); // $NON-NLS-1$

    ITypeBinding currentElementBinding = iteratorNextMethodBinding.getReturnType();

    return Bindings.normalizeForDeclarationUse(currentElementBinding, ast);
  }
}

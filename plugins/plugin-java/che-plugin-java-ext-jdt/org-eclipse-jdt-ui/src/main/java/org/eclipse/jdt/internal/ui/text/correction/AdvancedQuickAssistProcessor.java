/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Konstantin Scheglov (scheglov_ke@nlmk.ru) - initial API and implementation
 * (reports 71244 & 74746: New Quick Assist's [quick assist]) IBM Corporation - implementation Billy
 * Huang <billyhuang31@gmail.com> - [quick assist] concatenate/merge string literals -
 * https://bugs.eclipse.org/77632
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.dom.NecessaryParenthesesChecker;
import org.eclipse.jdt.internal.corext.dom.StatementRewrite;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.ExpressionsFix;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.refactoring.code.Invocations;
import org.eclipse.jdt.internal.corext.refactoring.code.OperatorPrecedence;
import org.eclipse.jdt.internal.corext.refactoring.util.TightSourceRangeComputer;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.fix.ExpressionsCleanUp;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.swt.graphics.Image;

/** */
public class AdvancedQuickAssistProcessor implements IQuickAssistProcessor {
  public AdvancedQuickAssistProcessor() {
    super();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.IAssistProcessor#hasAssists(org.eclipse.jdt.internal.ui.text.correction.IAssistContext)
   */
  public boolean hasAssists(IInvocationContext context) throws CoreException {
    ASTNode coveringNode = context.getCoveringNode();
    if (coveringNode != null) {
      ArrayList<ASTNode> coveredNodes = getFullyCoveredNodes(context, coveringNode);
      return getConvertToIfReturnProposals(context, coveringNode, null)
          || getInverseIfProposals(context, coveringNode, null)
          || getIfReturnIntoIfElseAtEndOfVoidMethodProposals(context, coveringNode, null)
          || getInverseIfContinueIntoIfThenInLoopsProposals(context, coveringNode, null)
          || getInverseIfIntoContinueInLoopsProposals(context, coveringNode, null)
          || getInverseConditionProposals(context, coveringNode, coveredNodes, null)
          || getRemoveExtraParenthesesProposals(context, coveringNode, coveredNodes, null)
          || getAddParanoidalParenthesesProposals(context, coveredNodes, null)
          || getAddParenthesesForExpressionProposals(context, coveringNode, null)
          || getJoinAndIfStatementsProposals(context, coveringNode, null)
          || getSplitAndConditionProposals(context, coveringNode, null)
          || getJoinOrIfStatementsProposals(context, coveringNode, coveredNodes, null)
          || getSplitOrConditionProposals(context, coveringNode, null)
          || getInverseConditionalExpressionProposals(context, coveringNode, null)
          || getExchangeInnerAndOuterIfConditionsProposals(context, coveringNode, null)
          || getExchangeOperandsProposals(context, coveringNode, null)
          || getCastAndAssignIfStatementProposals(context, coveringNode, null)
          || getCombineStringProposals(context, coveringNode, null)
          || getPickOutStringProposals(context, coveringNode, null)
          || getReplaceIfElseWithConditionalProposals(context, coveringNode, null)
          || getReplaceConditionalWithIfElseProposals(context, coveringNode, null)
          || getInverseLocalVariableProposals(context, coveringNode, null)
          || getPushNegationDownProposals(context, coveringNode, null)
          || getPullNegationUpProposals(context, coveredNodes, null)
          || getJoinIfListInIfElseIfProposals(context, coveringNode, coveredNodes, null)
          || getConvertSwitchToIfProposals(context, coveringNode, null)
          || getConvertIfElseToSwitchProposals(context, coveringNode, null)
          || GetterSetterCorrectionSubProcessor.addGetterSetterProposal(
              context, coveringNode, null, null);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.IAssistProcessor#getAssists(org.eclipse.jdt.internal.ui.text.correction.IAssistContext, org.eclipse.jdt.internal.ui.text.correction.IProblemLocation[])
   */
  public IJavaCompletionProposal[] getAssists(
      IInvocationContext context, IProblemLocation[] locations) throws CoreException {
    ASTNode coveringNode = context.getCoveringNode();
    if (coveringNode != null) {
      ArrayList<ASTNode> coveredNodes = getFullyCoveredNodes(context, coveringNode);
      ArrayList<ICommandAccess> resultingCollections = new ArrayList<ICommandAccess>();

      // quick assists that show up also if there is an error/warning
      getReplaceConditionalWithIfElseProposals(context, coveringNode, resultingCollections);

      if (QuickAssistProcessor.noErrorsAtLocation(locations)) {
        getConvertToIfReturnProposals(context, coveringNode, resultingCollections);
        getInverseIfProposals(context, coveringNode, resultingCollections);
        getIfReturnIntoIfElseAtEndOfVoidMethodProposals(
            context, coveringNode, resultingCollections);
        getInverseIfContinueIntoIfThenInLoopsProposals(context, coveringNode, resultingCollections);
        getInverseIfIntoContinueInLoopsProposals(context, coveringNode, resultingCollections);
        getInverseConditionProposals(context, coveringNode, coveredNodes, resultingCollections);
        getRemoveExtraParenthesesProposals(
            context, coveringNode, coveredNodes, resultingCollections);
        getAddParanoidalParenthesesProposals(context, coveredNodes, resultingCollections);
        getAddParenthesesForExpressionProposals(context, coveringNode, resultingCollections);
        getJoinAndIfStatementsProposals(context, coveringNode, resultingCollections);
        getSplitAndConditionProposals(context, coveringNode, resultingCollections);
        getJoinOrIfStatementsProposals(context, coveringNode, coveredNodes, resultingCollections);
        getSplitOrConditionProposals(context, coveringNode, resultingCollections);
        getInverseConditionalExpressionProposals(context, coveringNode, resultingCollections);
        getExchangeInnerAndOuterIfConditionsProposals(context, coveringNode, resultingCollections);
        getExchangeOperandsProposals(context, coveringNode, resultingCollections);
        getCastAndAssignIfStatementProposals(context, coveringNode, resultingCollections);
        getCombineStringProposals(context, coveringNode, resultingCollections);
        getPickOutStringProposals(context, coveringNode, resultingCollections);
        getReplaceIfElseWithConditionalProposals(context, coveringNode, resultingCollections);
        getInverseLocalVariableProposals(context, coveringNode, resultingCollections);
        getPushNegationDownProposals(context, coveringNode, resultingCollections);
        getPullNegationUpProposals(context, coveredNodes, resultingCollections);
        getJoinIfListInIfElseIfProposals(context, coveringNode, coveredNodes, resultingCollections);
        getConvertSwitchToIfProposals(context, coveringNode, resultingCollections);
        getConvertIfElseToSwitchProposals(context, coveringNode, resultingCollections);
        GetterSetterCorrectionSubProcessor.addGetterSetterProposal(
            context, coveringNode, locations, resultingCollections);
      }

      return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
    }
    return null;
  }

  private static boolean getConvertToIfReturnProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      ArrayList<ICommandAccess> resultingCollections) {
    if (!(coveringNode instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) coveringNode;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }

    // enclosing lambda or method should return 'void'
    LambdaExpression enclosingLambda = ASTResolving.findEnclosingLambdaExpression(ifStatement);
    if (enclosingLambda != null) {
      IMethodBinding lambdaMethodBinding = enclosingLambda.resolveMethodBinding();
      if (lambdaMethodBinding == null) {
        return false;
      }
      if (!(ifStatement
          .getAST()
          .resolveWellKnownType("void")
          .equals(lambdaMethodBinding.getReturnType()))) { // $NON-NLS-1$
        return false;
      }
    } else {
      MethodDeclaration coveringMethod = ASTResolving.findParentMethodDeclaration(ifStatement);
      if (coveringMethod == null) {
        return false;
      }
      Type returnType = coveringMethod.getReturnType2();
      if (!isVoid(returnType)) {
        return false;
      }
    }

    // should be present in a block
    if (!(ifStatement.getParent() instanceof Block)) {
      return false;
    }
    // should have at least one statement in 'then' part other than 'return'
    Statement thenStatement = ifStatement.getThenStatement();
    if (thenStatement instanceof ReturnStatement) {
      return false;
    }
    if (thenStatement instanceof Block) {
      List<Statement> thenStatements = ((Block) thenStatement).statements();
      if (thenStatements.isEmpty()
          || (thenStatements.size() == 1 && (thenStatements.get(0) instanceof ReturnStatement))) {
        return false;
      }
    }
    // should have no further executable statement
    if (!isLastStatementInEnclosingMethodOrLambda(ifStatement)) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }

    AST ast = coveringNode.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    // create inverted 'if' statement
    Expression inversedExpression = getInversedExpression(rewrite, ifStatement.getExpression());
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(inversedExpression);
    newIf.setThenStatement(ast.newReturnStatement());
    ListRewrite listRewriter =
        rewrite.getListRewrite(
            ifStatement.getParent(),
            (ChildListPropertyDescriptor) ifStatement.getLocationInParent());
    listRewriter.replace(ifStatement, newIf, null);
    // remove last 'return' in 'then' block
    ArrayList<Statement> statements = getUnwrappedStatements(ifStatement.getThenStatement());
    Statement lastStatement = statements.get(statements.size() - 1);
    if (lastStatement instanceof ReturnStatement) {
      statements.remove(lastStatement);
    }
    // add statements from 'then' to the end of block
    for (Statement statement : statements) {
      listRewriter.insertLast(rewrite.createMoveTarget(statement), null);
    }

    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfReturn;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.CONVERT_TO_IF_RETURN,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean isVoid(Type type) {
    return type instanceof PrimitiveType
        && ((PrimitiveType) type).getPrimitiveTypeCode() == PrimitiveType.VOID;
  }

  private static boolean isLastStatementInEnclosingMethodOrLambda(Statement statement) {
    ASTNode currentStructure = statement;
    ASTNode currentParent = statement.getParent();
    while (!(currentParent instanceof MethodDeclaration
        || currentParent instanceof LambdaExpression)) {
      // should not be in a loop
      if (currentParent instanceof ForStatement
          || currentParent instanceof EnhancedForStatement
          || currentParent instanceof WhileStatement
          || currentParent instanceof DoStatement) {
        return false;
      }
      if (currentParent instanceof Block) {
        Block parentBlock = (Block) currentParent;
        if (parentBlock.statements().indexOf(currentStructure)
            != parentBlock.statements().size() - 1) { // not last statement in the block
          return false;
        }
      }
      currentStructure = currentParent;
      currentParent = currentParent.getParent();
    }
    return true;
  }

  private static boolean getIfReturnIntoIfElseAtEndOfVoidMethodProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!(covering instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) covering;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }
    // 'then' block should have 'return' as last statement
    Statement thenStatement = ifStatement.getThenStatement();
    if (!(thenStatement instanceof Block)) {
      return false;
    }
    Block thenBlock = (Block) thenStatement;
    List<Statement> thenStatements = thenBlock.statements();
    if (thenStatements.isEmpty()
        || !(thenStatements.get(thenStatements.size() - 1) instanceof ReturnStatement)) {
      return false;
    }
    // method should return 'void'
    MethodDeclaration coveringMetod = ASTResolving.findParentMethodDeclaration(covering);
    if (coveringMetod == null) {
      return false;
    }
    Type returnType = coveringMetod.getReturnType2();
    if (!isVoid(returnType)) {
      return false;
    }
    //
    List<Statement> statements = coveringMetod.getBody().statements();
    int ifIndex = statements.indexOf(ifStatement);
    if (ifIndex == -1) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // remove last 'return' in 'then' block
    ListRewrite listRewriter =
        rewrite.getListRewrite(
            thenBlock, (ChildListPropertyDescriptor) ifStatement.getLocationInParent());
    listRewriter.remove(thenStatements.get(thenStatements.size() - 1), null);
    // prepare original nodes
    Expression conditionPlaceholder =
        (Expression) rewrite.createMoveTarget(ifStatement.getExpression());
    Statement thenPlaceholder =
        (Statement) rewrite.createMoveTarget(ifStatement.getThenStatement());
    // prepare 'else' block
    Block elseBlock = ast.newBlock();
    for (int i = ifIndex + 1; i < statements.size(); i++) {
      Statement statement = statements.get(i);
      elseBlock.statements().add(rewrite.createMoveTarget(statement));
    }
    // prepare new 'if' statement
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(conditionPlaceholder);
    newIf.setThenStatement(thenPlaceholder);
    newIf.setElseStatement(elseBlock);
    rewrite.replace(ifStatement, newIf, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_convertToIfElse_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.CONVERT_TO_IF_ELSE,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInverseIfProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!(covering instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) covering;
    if (ifStatement.getElseStatement() == null) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    Statement thenStatement = ifStatement.getThenStatement();
    Statement elseStatement = ifStatement.getElseStatement();

    // prepare original nodes
    Expression inversedExpression = getInversedExpression(rewrite, ifStatement.getExpression());

    Statement newElseStatement = (Statement) rewrite.createMoveTarget(thenStatement);
    Statement newThenStatement = (Statement) rewrite.createMoveTarget(elseStatement);
    // set new nodes
    rewrite.set(ifStatement, IfStatement.EXPRESSION_PROPERTY, inversedExpression, null);

    if (elseStatement instanceof IfStatement) { // bug 79507 && bug 74580
      Block elseBlock = ast.newBlock();
      elseBlock.statements().add(newThenStatement);
      newThenStatement = elseBlock;
    }
    rewrite.set(ifStatement, IfStatement.THEN_STATEMENT_PROPERTY, newThenStatement, null);
    rewrite.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, newElseStatement, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_inverseIf_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERSE_IF_STATEMENT,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInverseIfContinueIntoIfThenInLoopsProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!(covering instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) covering;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }
    // check that 'then' is 'continue'
    if (!(ifStatement.getThenStatement() instanceof ContinueStatement)) {
      return false;
    }
    // check that 'if' statement is statement in block that is body of loop
    Block loopBlock = null;
    if (ifStatement.getParent() instanceof Block
        && ifStatement.getParent().getParent() instanceof ForStatement) {
      loopBlock = (Block) ifStatement.getParent();
    } else if (ifStatement.getParent() instanceof Block
        && ifStatement.getParent().getParent() instanceof WhileStatement) {
      loopBlock = (Block) ifStatement.getParent();
    } else {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // create inverted 'if' statement
    Expression inversedExpression = getInversedExpression(rewrite, ifStatement.getExpression());
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(inversedExpression);
    // prepare 'then' for new 'if'
    Block thenBlock = ast.newBlock();
    int ifIndex = loopBlock.statements().indexOf(ifStatement);
    for (int i = ifIndex + 1; i < loopBlock.statements().size(); i++) {
      Statement statement = (Statement) loopBlock.statements().get(i);
      thenBlock.statements().add(rewrite.createMoveTarget(statement));
    }
    newIf.setThenStatement(thenBlock);
    // replace 'if' statement in loop
    rewrite.replace(ifStatement, newIf, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_inverseIfContinue_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERSE_IF_CONTINUE,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInverseIfIntoContinueInLoopsProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!(covering instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) covering;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }
    // prepare outer control structure and block that contains 'if' statement
    ASTNode ifParent = ifStatement.getParent();
    Block ifParentBlock = null;
    ASTNode ifParentStructure = ifParent;
    if (ifParentStructure instanceof Block) {
      ifParentBlock = (Block) ifParent;
      ifParentStructure = ifParentStructure.getParent();
    }
    // check that control structure is loop and 'if' statement if last statement
    if (!(ifParentStructure instanceof ForStatement)
        && !(ifParentStructure instanceof WhileStatement)) {
      return false;
    }
    if (ifParentBlock != null
        && ifParentBlock.statements().indexOf(ifStatement)
            != ifParentBlock.statements().size() - 1) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // create inverted 'if' statement
    Expression inversedExpression = getInversedExpression(rewrite, ifStatement.getExpression());
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(inversedExpression);
    newIf.setThenStatement(ast.newContinueStatement());
    //
    if (ifParentBlock == null) {
      // if there is no block, create it
      ifParentBlock = ast.newBlock();
      ifParentBlock.statements().add(newIf);
      for (Iterator<Statement> iter =
              getUnwrappedStatements(ifStatement.getThenStatement()).iterator();
          iter.hasNext(); ) {
        Statement statement = iter.next();
        ifParentBlock.statements().add(rewrite.createMoveTarget(statement));
      }
      // replace 'if' statement as body with new block
      if (ifParentStructure instanceof ForStatement) {
        rewrite.set(ifParentStructure, ForStatement.BODY_PROPERTY, ifParentBlock, null);
      } else if (ifParentStructure instanceof WhileStatement) {
        rewrite.set(ifParentStructure, WhileStatement.BODY_PROPERTY, ifParentBlock, null);
      }
    } else {
      // if there was block, replace
      ListRewrite listRewriter =
          rewrite.getListRewrite(
              ifParentBlock, (ChildListPropertyDescriptor) ifStatement.getLocationInParent());
      listRewriter.replace(ifStatement, newIf, null);
      // add statements from 'then' to the end of block
      for (Iterator<Statement> iter =
              getUnwrappedStatements(ifStatement.getThenStatement()).iterator();
          iter.hasNext(); ) {
        Statement statement = iter.next();
        listRewriter.insertLast(rewrite.createMoveTarget(statement), null);
      }
    }
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_inverseIfToContinue_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERT_IF_TO_CONTINUE,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static ArrayList<Statement> getUnwrappedStatements(Statement body) {
    ArrayList<Statement> statements = new ArrayList<Statement>();
    if (body instanceof Block) {
      for (Iterator<Statement> iter = ((Block) body).statements().iterator(); iter.hasNext(); ) {
        Statement statement = iter.next();
        statements.add(statement);
      }
    } else {
      statements.add(body);
    }
    return statements;
  }

  private static boolean getInverseConditionProposals(
      IInvocationContext context,
      ASTNode covering,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    if (coveredNodes.isEmpty()) {
      return false;
    }
    //
    final AST ast = covering.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    // check sub-expressions in fully covered nodes
    boolean hasChanges = false;
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      ASTNode covered = iter.next();
      Expression coveredExpression = getBooleanExpression(covered);
      if (coveredExpression != null) {
        Expression inversedExpression = getInversedExpression(rewrite, coveredExpression);
        rewrite.replace(coveredExpression, inversedExpression, null);
        hasChanges = true;
      }
    }
    //
    if (!hasChanges) {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_inverseConditions_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERSE_CONDITIONS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static Expression getInversedExpression(ASTRewrite rewrite, Expression expression) {
    return getInversedExpression(rewrite, expression, null);
  }

  private interface SimpleNameRenameProvider {
    SimpleName getRenamed(SimpleName name);
  }

  private static Expression getRenamedNameCopy(
      SimpleNameRenameProvider provider, ASTRewrite rewrite, Expression expression) {
    if (provider != null) {
      if (expression instanceof SimpleName) {
        SimpleName name = (SimpleName) expression;
        SimpleName newName = provider.getRenamed(name);
        if (newName != null) {
          return newName;
        }
      }
    }
    return (Expression) rewrite.createCopyTarget(expression);
  }

  private static Expression getInversedExpression(
      ASTRewrite rewrite, Expression expression, SimpleNameRenameProvider provider) {
    AST ast = rewrite.getAST();
    //
    if (expression instanceof BooleanLiteral) {
      return ast.newBooleanLiteral(!((BooleanLiteral) expression).booleanValue());
    }
    if (expression instanceof InfixExpression) {
      InfixExpression infixExpression = (InfixExpression) expression;
      InfixExpression.Operator operator = infixExpression.getOperator();
      if (operator == InfixExpression.Operator.LESS) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.GREATER_EQUALS, provider);
      }
      if (operator == InfixExpression.Operator.GREATER) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.LESS_EQUALS, provider);
      }
      if (operator == InfixExpression.Operator.LESS_EQUALS) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.GREATER, provider);
      }
      if (operator == InfixExpression.Operator.GREATER_EQUALS) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.LESS, provider);
      }
      if (operator == InfixExpression.Operator.EQUALS) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.NOT_EQUALS, provider);
      }
      if (operator == InfixExpression.Operator.NOT_EQUALS) {
        return getInversedInfixExpression(
            rewrite, infixExpression, InfixExpression.Operator.EQUALS, provider);
      }
      if (operator == InfixExpression.Operator.CONDITIONAL_AND) {
        return getInversedAndOrExpression(
            rewrite, infixExpression, InfixExpression.Operator.CONDITIONAL_OR, provider);
      }
      if (operator == InfixExpression.Operator.CONDITIONAL_OR) {
        return getInversedAndOrExpression(
            rewrite, infixExpression, InfixExpression.Operator.CONDITIONAL_AND, provider);
      }
      if (operator == InfixExpression.Operator.AND) {
        return getInversedAndOrExpression(
            rewrite, infixExpression, InfixExpression.Operator.OR, provider);
      }
      if (operator == InfixExpression.Operator.OR) {
        return getInversedAndOrExpression(
            rewrite, infixExpression, InfixExpression.Operator.AND, provider);
      }
      if (operator == InfixExpression.Operator.XOR) {
        return getInversedNotExpression(rewrite, expression, ast);
      }
    }
    if (expression instanceof PrefixExpression) {
      PrefixExpression prefixExpression = (PrefixExpression) expression;
      if (prefixExpression.getOperator() == PrefixExpression.Operator.NOT) {
        Expression operand = prefixExpression.getOperand();
        if ((operand instanceof ParenthesizedExpression)
            && NecessaryParenthesesChecker.canRemoveParentheses(
                operand, expression.getParent(), expression.getLocationInParent())) {
          operand = ((ParenthesizedExpression) operand).getExpression();
        }
        Expression renamedNameCopy = getRenamedNameCopy(provider, rewrite, operand);
        if (renamedNameCopy instanceof InfixExpression) {
          InfixExpression infixExpression = (InfixExpression) renamedNameCopy;
          infixExpression.setOperator(((InfixExpression) operand).getOperator());
        }
        return renamedNameCopy;
      }
    }
    if (expression instanceof InstanceofExpression) {
      return getInversedNotExpression(rewrite, expression, ast);
    }
    if (expression instanceof ParenthesizedExpression) {
      ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
      Expression innerExpression = parenthesizedExpression.getExpression();
      while (innerExpression instanceof ParenthesizedExpression) {
        innerExpression = ((ParenthesizedExpression) innerExpression).getExpression();
      }
      if (innerExpression instanceof InstanceofExpression) {
        return getInversedExpression(rewrite, innerExpression, provider);
      }
      parenthesizedExpression =
          getParenthesizedExpression(
              ast, getInversedExpression(rewrite, innerExpression, provider));
      return parenthesizedExpression;
    }
    if (expression instanceof ConditionalExpression) {
      ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
      ConditionalExpression newExpression = ast.newConditionalExpression();
      newExpression.setExpression(
          (Expression) rewrite.createCopyTarget(conditionalExpression.getExpression()));
      newExpression.setThenExpression(
          getInversedExpression(rewrite, conditionalExpression.getThenExpression()));
      newExpression.setElseExpression(
          getInversedExpression(rewrite, conditionalExpression.getElseExpression()));
      return newExpression;
    }

    PrefixExpression prefixExpression = ast.newPrefixExpression();
    prefixExpression.setOperator(PrefixExpression.Operator.NOT);
    Expression renamedNameCopy = getRenamedNameCopy(provider, rewrite, expression);
    if (NecessaryParenthesesChecker.needsParentheses(
        renamedNameCopy, prefixExpression, PrefixExpression.OPERAND_PROPERTY)) {
      renamedNameCopy = getParenthesizedExpression(ast, renamedNameCopy);
    }
    prefixExpression.setOperand(renamedNameCopy);
    return prefixExpression;
  }

  private static Expression getInversedNotExpression(
      ASTRewrite rewrite, Expression expression, AST ast) {
    PrefixExpression prefixExpression = ast.newPrefixExpression();
    prefixExpression.setOperator(PrefixExpression.Operator.NOT);
    ParenthesizedExpression parenthesizedExpression =
        getParenthesizedExpression(ast, (Expression) rewrite.createCopyTarget(expression));
    prefixExpression.setOperand(parenthesizedExpression);
    return prefixExpression;
  }

  private static boolean isBoolean(Expression expression) {
    ITypeBinding typeBinding = expression.resolveTypeBinding();
    AST ast = expression.getAST();
    return typeBinding == ast.resolveWellKnownType("boolean") // $NON-NLS-1$
        || typeBinding == ast.resolveWellKnownType("java.lang.Boolean"); // $NON-NLS-1$
  }

  private static Expression getInversedInfixExpression(
      ASTRewrite rewrite,
      InfixExpression expression,
      InfixExpression.Operator newOperator,
      SimpleNameRenameProvider provider) {
    InfixExpression newExpression = rewrite.getAST().newInfixExpression();
    newExpression.setOperator(newOperator);
    newExpression.setLeftOperand(
        getRenamedNameCopy(provider, rewrite, expression.getLeftOperand()));
    newExpression.setRightOperand(
        getRenamedNameCopy(provider, rewrite, expression.getRightOperand()));
    return newExpression;
  }

  private static Expression parenthesizeIfRequired(Expression operand, int newOperatorPrecedence) {
    if (newOperatorPrecedence > OperatorPrecedence.getExpressionPrecedence(operand)) {
      return getParenthesizedExpression(operand.getAST(), operand);
    }
    return operand;
  }

  private static Expression getInversedAndOrExpression(
      ASTRewrite rewrite,
      InfixExpression infixExpression,
      Operator newOperator,
      SimpleNameRenameProvider provider) {
    InfixExpression newExpression = rewrite.getAST().newInfixExpression();
    newExpression.setOperator(newOperator);

    int newOperatorPrecedence = OperatorPrecedence.getOperatorPrecedence(newOperator);
    //
    Expression leftOperand =
        getInversedExpression(rewrite, infixExpression.getLeftOperand(), provider);
    newExpression.setLeftOperand(parenthesizeIfRequired(leftOperand, newOperatorPrecedence));

    Expression rightOperand =
        getInversedExpression(rewrite, infixExpression.getRightOperand(), provider);
    newExpression.setRightOperand(parenthesizeIfRequired(rightOperand, newOperatorPrecedence));

    List<Expression> extraOperands = infixExpression.extendedOperands();
    List<Expression> newExtraOperands = newExpression.extendedOperands();
    for (int i = 0; i < extraOperands.size(); i++) {
      Expression extraOperand = getInversedExpression(rewrite, extraOperands.get(i), provider);
      newExtraOperands.add(parenthesizeIfRequired(extraOperand, newOperatorPrecedence));
    }
    return newExpression;
  }

  private static boolean getRemoveExtraParenthesesProposals(
      IInvocationContext context,
      ASTNode covering,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    ArrayList<ASTNode> nodes;
    if (context.getSelectionLength() == 0 && covering instanceof ParenthesizedExpression) {
      nodes = new ArrayList<ASTNode>();
      nodes.add(covering);
    } else {
      nodes = coveredNodes;
    }
    if (nodes.isEmpty()) return false;

    IProposableFix fix =
        ExpressionsFix.createRemoveUnnecessaryParenthesisFix(
            context.getASTRoot(), nodes.toArray(new ASTNode[nodes.size()]));
    if (fix == null) return false;

    if (resultingCollections == null) return true;

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
    Map<String, String> options = new Hashtable<String, String>();
    options.put(CleanUpConstants.EXPRESSIONS_USE_PARENTHESES, CleanUpOptions.TRUE);
    options.put(CleanUpConstants.EXPRESSIONS_USE_PARENTHESES_NEVER, CleanUpOptions.TRUE);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix,
            new ExpressionsCleanUp(options),
            IProposalRelevance.REMOVE_EXTRA_PARENTHESES,
            image,
            context);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getAddParanoidalParenthesesProposals(
      IInvocationContext context,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {

    IProposableFix fix =
        ExpressionsFix.createAddParanoidalParenthesisFix(
            context.getASTRoot(), coveredNodes.toArray(new ASTNode[coveredNodes.size()]));
    if (fix == null) return false;

    if (resultingCollections == null) return true;

    // add correction proposal
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CAST);
    Map<String, String> options = new Hashtable<String, String>();
    options.put(CleanUpConstants.EXPRESSIONS_USE_PARENTHESES, CleanUpOptions.TRUE);
    options.put(CleanUpConstants.EXPRESSIONS_USE_PARENTHESES_ALWAYS, CleanUpOptions.TRUE);
    FixCorrectionProposal proposal =
        new FixCorrectionProposal(
            fix,
            new ExpressionsCleanUp(options),
            IProposalRelevance.ADD_PARANOIDAL_PARENTHESES,
            image,
            context);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getAddParenthesesForExpressionProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      Collection<ICommandAccess> resultingCollections) {
    ASTNode node;

    if (context.getSelectionLength() == 0) {
      node = coveringNode;
      while (node != null
          && !(node instanceof CastExpression)
          && !(node instanceof InfixExpression)
          && !(node instanceof InstanceofExpression)
          && !(node instanceof ConditionalExpression)) {
        node = node.getParent();
      }
    } else {
      node = context.getCoveredNode();
    }

    String label = null;
    if (node instanceof CastExpression) {
      label = CorrectionMessages.UnresolvedElementsSubProcessor_missingcastbrackets_description;
    } else if (node instanceof InstanceofExpression) {
      label = CorrectionMessages.LocalCorrectionsSubProcessor_setparenteses_instanceof_description;
    } else if (node instanceof InfixExpression) {
      InfixExpression infixExpression = (InfixExpression) node;
      label =
          Messages.format(
              CorrectionMessages.LocalCorrectionsSubProcessor_setparenteses_description,
              infixExpression.getOperator().toString());
    } else if (node instanceof ConditionalExpression) {
      label = CorrectionMessages.AdvancedQuickAssistProcessor_putConditionalExpressionInParentheses;
    } else {
      return false;
    }

    if (node.getParent() instanceof ParenthesizedExpression) return false;

    if (resultingCollections == null) return true;

    AST ast = node.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
    parenthesizedExpression.setExpression((Expression) rewrite.createCopyTarget(node));
    rewrite.replace(node, parenthesizedExpression, null);

    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CAST);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.ADD_PARENTHESES_FOR_EXPRESSION,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  static ArrayList<ASTNode> getFullyCoveredNodes(IInvocationContext context, ASTNode coveringNode) {
    final ArrayList<ASTNode> coveredNodes = new ArrayList<ASTNode>();
    final int selectionBegin = context.getSelectionOffset();
    final int selectionEnd = selectionBegin + context.getSelectionLength();
    coveringNode.accept(
        new GenericVisitor() {
          @Override
          protected boolean visitNode(ASTNode node) {
            int nodeStart = node.getStartPosition();
            int nodeEnd = nodeStart + node.getLength();
            // if node does not intersects with selection, don't visit children
            if (nodeEnd < selectionBegin || selectionEnd < nodeStart) {
              return false;
            }
            // if node is fully covered, we don't need to visit children
            if (isCovered(node)) {
              ASTNode parent = node.getParent();
              if (parent == null || !isCovered(parent)) {
                coveredNodes.add(node);
                return false;
              }
            }
            // if node only partly intersects with selection, we try to find fully covered children
            return true;
          }

          private boolean isCovered(ASTNode node) {
            int begin = node.getStartPosition();
            int end = begin + node.getLength();
            return begin >= selectionBegin && end <= selectionEnd;
          }
        });
    return coveredNodes;
  }

  private static boolean getJoinAndIfStatementsProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {

    //
    if (!(node instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) node;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }
    // case when current IfStatement is sole child of another IfStatement
    {
      IfStatement outerIf = null;
      if (ifStatement.getParent() instanceof IfStatement) {
        outerIf = (IfStatement) ifStatement.getParent();
      } else if (ifStatement.getParent() instanceof Block) {
        Block block = (Block) ifStatement.getParent();
        if (block.getParent() instanceof IfStatement && block.statements().size() == 1) {
          outerIf = (IfStatement) block.getParent();
        }
      }
      if (outerIf != null && outerIf.getElseStatement() == null) {
        if (resultingCollections == null) {
          return true;
        }
        //
        AST ast = node.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // create compound condition
        InfixExpression condition = ast.newInfixExpression();
        condition.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
        // prepare condition parts, add parentheses if needed
        Expression outerCondition =
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                outerIf.getExpression(),
                condition,
                InfixExpression.LEFT_OPERAND_PROPERTY);
        Expression innerCondition =
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                ifStatement.getExpression(),
                condition,
                InfixExpression.RIGHT_OPERAND_PROPERTY);
        condition.setLeftOperand(outerCondition);
        condition.setRightOperand(innerCondition);
        // create new IfStatement
        IfStatement newIf = ast.newIfStatement();
        newIf.setExpression(condition);
        Statement bodyPlaceholder =
            (Statement) rewrite.createCopyTarget(ifStatement.getThenStatement());
        newIf.setThenStatement(bodyPlaceholder);
        rewrite.replace(outerIf, newIf, null);
        // add correction proposal
        String label = CorrectionMessages.AdvancedQuickAssistProcessor_joinWithOuter_description;
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.JOIN_IF_WITH_OUTER_IF,
                image);
        resultingCollections.add(proposal);
      }
    }
    // case when current IfStatement has another IfStatement as sole child
    {
      IfStatement innerIf = null;
      if (ifStatement.getThenStatement() instanceof IfStatement) {
        innerIf = (IfStatement) ifStatement.getThenStatement();
      } else if (ifStatement.getThenStatement() instanceof Block) {
        Block block = (Block) ifStatement.getThenStatement();
        if (block.statements().size() == 1 && block.statements().get(0) instanceof IfStatement) {
          innerIf = (IfStatement) block.statements().get(0);
        }
      }
      if (innerIf != null && innerIf.getElseStatement() == null) {
        if (resultingCollections == null) {
          return true;
        }
        //
        AST ast = node.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // create compound condition
        InfixExpression condition = ast.newInfixExpression();
        condition.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
        // prepare condition parts, add parentheses if needed
        Expression outerCondition =
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                ifStatement.getExpression(),
                condition,
                InfixExpression.LEFT_OPERAND_PROPERTY);
        Expression innerCondition =
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                innerIf.getExpression(),
                condition,
                InfixExpression.RIGHT_OPERAND_PROPERTY);
        condition.setLeftOperand(outerCondition);
        condition.setRightOperand(innerCondition);
        // create new IfStatement
        IfStatement newIf = ast.newIfStatement();
        newIf.setExpression(condition);
        Statement bodyPlaceholder =
            (Statement) rewrite.createCopyTarget(innerIf.getThenStatement());
        newIf.setThenStatement(bodyPlaceholder);
        rewrite.replace(ifStatement, newIf, null);
        // add correction proposal
        String label = CorrectionMessages.AdvancedQuickAssistProcessor_joinWithInner_description;
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.JOIN_IF_WITH_INNER_IF,
                image);
        resultingCollections.add(proposal);
      }
    }
    return true;
  }

  private static Expression getParenthesizedExpressionIfNeeded(
      AST ast,
      ASTRewrite rewrite,
      Expression expression,
      ASTNode parent,
      StructuralPropertyDescriptor locationInParent) {
    boolean addParentheses =
        NecessaryParenthesesChecker.needsParentheses(expression, parent, locationInParent);
    expression = (Expression) rewrite.createCopyTarget(expression);
    if (addParentheses) {
      return getParenthesizedExpression(ast, expression);
    }
    return expression;
  }

  private static ParenthesizedExpression getParenthesizedExpression(
      AST ast, Expression expression) {
    ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
    parenthesizedExpression.setExpression(expression);
    return parenthesizedExpression;
  }

  public static boolean getSplitAndConditionProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    Operator andOperator = InfixExpression.Operator.CONDITIONAL_AND;
    // check that user invokes quick assist on infix expression
    if (!(node instanceof InfixExpression)) {
      return false;
    }
    InfixExpression infixExpression = (InfixExpression) node;
    if (infixExpression.getOperator() != andOperator) {
      return false;
    }
    int offset =
        isOperatorSelected(
            infixExpression, context.getSelectionOffset(), context.getSelectionLength());
    if (offset == -1) {
      return false;
    }

    // check that infix expression belongs to IfStatement
    Statement statement = ASTResolving.findParentStatement(node);
    if (!(statement instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) statement;

    // check that infix expression is part of first level && condition of IfStatement
    InfixExpression topInfixExpression = infixExpression;
    while (topInfixExpression.getParent() instanceof InfixExpression
        && ((InfixExpression) topInfixExpression.getParent()).getOperator() == andOperator) {
      topInfixExpression = (InfixExpression) topInfixExpression.getParent();
    }
    if (ifStatement.getExpression() != topInfixExpression) {
      return false;
    }
    //
    if (resultingCollections == null) {
      return true;
    }
    AST ast = ifStatement.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    // prepare left and right conditions
    Expression[] newOperands = {null, null};
    breakInfixOperationAtOperation(
        rewrite, topInfixExpression, andOperator, offset, true, newOperands);

    Expression leftCondition = newOperands[0];
    Expression rightCondition = newOperands[1];

    // replace conditions in outer IfStatement
    rewrite.set(ifStatement, IfStatement.EXPRESSION_PROPERTY, leftCondition, null);

    // prepare inner IfStatement
    IfStatement innerIf = ast.newIfStatement();

    innerIf.setExpression(rightCondition);
    innerIf.setThenStatement((Statement) rewrite.createMoveTarget(ifStatement.getThenStatement()));
    Block innerBlock = ast.newBlock();
    innerBlock.statements().add(innerIf);

    Statement elseStatement = ifStatement.getElseStatement();
    if (elseStatement != null) {
      innerIf.setElseStatement((Statement) rewrite.createCopyTarget(elseStatement));
    }

    // replace outer thenStatement
    rewrite.replace(ifStatement.getThenStatement(), innerBlock, null);

    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_splitAndCondition_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.SPLIT_AND_CONDITION,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean isSelectingOperator(ASTNode n1, ASTNode n2, int offset, int length) {
    // between the nodes
    if (offset + length <= n2.getStartPosition() && offset >= ASTNodes.getExclusiveEnd(n1)) {
      return true;
    }
    // or exactly select the node (but not with infix expressions)
    if (n1.getStartPosition() == offset && ASTNodes.getExclusiveEnd(n2) == offset + length) {
      if (n1 instanceof InfixExpression || n2 instanceof InfixExpression) {
        return false;
      }
      return true;
    }
    return false;
  }

  private static int isOperatorSelected(InfixExpression infixExpression, int offset, int length) {
    ASTNode left = infixExpression.getLeftOperand();
    ASTNode right = infixExpression.getRightOperand();

    if (isSelectingOperator(left, right, offset, length)) {
      return ASTNodes.getExclusiveEnd(left);
    }
    List<Expression> extended = infixExpression.extendedOperands();
    for (int i = 0; i < extended.size(); i++) {
      left = right;
      right = extended.get(i);
      if (isSelectingOperator(left, right, offset, length)) {
        return ASTNodes.getExclusiveEnd(left);
      }
    }
    return -1;
  }

  private static boolean getJoinOrIfStatementsProposals(
      IInvocationContext context,
      ASTNode covering,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    Operator orOperator = InfixExpression.Operator.CONDITIONAL_OR;
    if (coveredNodes.size() < 2) return false;
    // check that all covered nodes are IfStatement's with same 'then' statement and without 'else'
    String commonThenSource = null;
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      ASTNode node = iter.next();
      if (!(node instanceof IfStatement)) return false;
      //
      IfStatement ifStatement = (IfStatement) node;
      if (ifStatement.getElseStatement() != null) return false;
      //
      Statement thenStatement = ifStatement.getThenStatement();
      try {
        String thenSource =
            context
                .getCompilationUnit()
                .getBuffer()
                .getText(thenStatement.getStartPosition(), thenStatement.getLength());
        if (commonThenSource == null) {
          commonThenSource = thenSource;
        } else {
          if (!commonThenSource.equals(thenSource)) return false;
        }
      } catch (Throwable e) {
        return false;
      }
    }
    if (resultingCollections == null) {
      return true;
    }
    //
    final AST ast = covering.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepare OR'ed condition
    InfixExpression condition = null;
    boolean hasRightOperand = false;
    Statement thenStatement = null;
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      IfStatement ifStatement = (IfStatement) iter.next();
      if (thenStatement == null)
        thenStatement = (Statement) rewrite.createCopyTarget(ifStatement.getThenStatement());
      if (condition == null) {
        condition = ast.newInfixExpression();
        condition.setOperator(orOperator);
        condition.setLeftOperand(
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                ifStatement.getExpression(),
                condition,
                InfixExpression.LEFT_OPERAND_PROPERTY));
      } else if (!hasRightOperand) {
        condition.setRightOperand(
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                ifStatement.getExpression(),
                condition,
                InfixExpression.RIGHT_OPERAND_PROPERTY));
        hasRightOperand = true;
      } else {
        InfixExpression newCondition = ast.newInfixExpression();
        newCondition.setOperator(orOperator);
        newCondition.setLeftOperand(condition);
        newCondition.setRightOperand(
            getParenthesizedExpressionIfNeeded(
                ast,
                rewrite,
                ifStatement.getExpression(),
                condition,
                InfixExpression.RIGHT_OPERAND_PROPERTY));
        condition = newCondition;
      }
    }
    // prepare new IfStatement with OR'ed condition
    IfStatement newIf = ast.newIfStatement();
    newIf.setExpression(condition);
    newIf.setThenStatement(thenStatement);
    //
    ListRewrite listRewriter = null;
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      IfStatement ifStatement = (IfStatement) iter.next();
      if (listRewriter == null) {
        Block sourceBlock = (Block) ifStatement.getParent();
        // int insertIndex = sourceBlock.statements().indexOf(ifStatement);
        listRewriter =
            rewrite.getListRewrite(
                sourceBlock, (ChildListPropertyDescriptor) ifStatement.getLocationInParent());
      }
      if (newIf != null) {
        listRewriter.replace(ifStatement, newIf, null);
        newIf = null;
      } else {
        listRewriter.remove(ifStatement, null);
      }
    }
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_joinWithOr_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.JOIN_IF_STATEMENTS_WITH_OR,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  public static boolean getSplitOrConditionProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    Operator orOperator = InfixExpression.Operator.CONDITIONAL_OR;
    // check that user invokes quick assist on infix expression
    if (!(node instanceof InfixExpression)) {
      return false;
    }
    InfixExpression infixExpression = (InfixExpression) node;
    if (infixExpression.getOperator() != orOperator) {
      return false;
    }
    int offset =
        isOperatorSelected(
            infixExpression, context.getSelectionOffset(), context.getSelectionLength());
    if (offset == -1) {
      return false;
    }
    // check that infix expression belongs to IfStatement
    Statement statement = ASTResolving.findParentStatement(node);
    if (!(statement instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) statement;

    // check that infix expression is part of first level || condition of IfStatement
    InfixExpression topInfixExpression = infixExpression;
    while (topInfixExpression.getParent() instanceof InfixExpression
        && ((InfixExpression) topInfixExpression.getParent()).getOperator() == orOperator) {
      topInfixExpression = (InfixExpression) topInfixExpression.getParent();
    }
    if (ifStatement.getExpression() != topInfixExpression) {
      return false;
    }
    //
    if (resultingCollections == null) {
      return true;
    }
    AST ast = ifStatement.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    // prepare left and right conditions
    Expression[] newOperands = {null, null};
    breakInfixOperationAtOperation(
        rewrite, topInfixExpression, orOperator, offset, true, newOperands);

    Expression leftCondition = newOperands[0];
    Expression rightCondition = newOperands[1];

    // prepare first statement
    rewrite.replace(ifStatement.getExpression(), leftCondition, null);

    IfStatement secondIf = ast.newIfStatement();
    secondIf.setExpression(rightCondition);
    secondIf.setThenStatement((Statement) rewrite.createCopyTarget(ifStatement.getThenStatement()));

    Statement elseStatement = ifStatement.getElseStatement();
    if (elseStatement == null) {
      rewrite.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, secondIf, null);
    } else {
      rewrite.replace(elseStatement, secondIf, null);
      secondIf.setElseStatement((Statement) rewrite.createMoveTarget(elseStatement));
    }

    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_splitOrCondition_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.SPLIT_OR_CONDITION,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInverseConditionalExpressionProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    // try to find conditional expression as parent
    while (covering instanceof Expression) {
      if (covering instanceof ConditionalExpression) break;
      covering = covering.getParent();
    }
    if (!(covering instanceof ConditionalExpression)) {
      return false;
    }
    ConditionalExpression expression = (ConditionalExpression) covering;
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepare new conditional expression
    ConditionalExpression newExpression = ast.newConditionalExpression();
    newExpression.setExpression(getInversedExpression(rewrite, expression.getExpression()));
    newExpression.setThenExpression(
        (Expression) rewrite.createCopyTarget(expression.getElseExpression()));
    newExpression.setElseExpression(
        (Expression) rewrite.createCopyTarget(expression.getThenExpression()));
    // replace old expression with new
    rewrite.replace(expression, newExpression, null);
    // add correction proposal
    String label =
        CorrectionMessages.AdvancedQuickAssistProcessor_inverseConditionalExpression_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERSE_CONDITIONAL_EXPRESSION,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getExchangeInnerAndOuterIfConditionsProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    boolean result = false;
    //
    if (!(node instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) node;
    if (ifStatement.getElseStatement() != null) {
      return false;
    }
    // case when current IfStatement is sole child of another IfStatement
    {
      IfStatement outerIf = null;
      if (ifStatement.getParent() instanceof IfStatement) {
        outerIf = (IfStatement) ifStatement.getParent();
      } else if (ifStatement.getParent() instanceof Block) {
        Block block = (Block) ifStatement.getParent();
        if (block.getParent() instanceof IfStatement && block.statements().size() == 1) {
          outerIf = (IfStatement) block.getParent();
        }
      }
      if (outerIf != null && outerIf.getElseStatement() == null) {
        if (resultingCollections == null) {
          return true;
        }
        //
        AST ast = node.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // prepare conditions
        Expression outerCondition = (Expression) rewrite.createCopyTarget(outerIf.getExpression());
        Expression innerCondition =
            (Expression) rewrite.createCopyTarget(ifStatement.getExpression());
        // exchange conditions
        rewrite.replace(outerIf.getExpression(), innerCondition, null);
        rewrite.replace(ifStatement.getExpression(), outerCondition, null);
        // add correction proposal
        String label =
            CorrectionMessages
                .AdvancedQuickAssistProcessor_exchangeInnerAndOuterIfConditions_description;
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.EXCHANGE_INNER_AND_OUTER_IF_CONDITIONS,
                image);
        resultingCollections.add(proposal);
        result = true;
      }
    }
    // case when current IfStatement has another IfStatement as sole child
    {
      IfStatement innerIf = null;
      if (ifStatement.getThenStatement() instanceof IfStatement) {
        innerIf = (IfStatement) ifStatement.getThenStatement();
      } else if (ifStatement.getThenStatement() instanceof Block) {
        Block block = (Block) ifStatement.getThenStatement();
        if (block.statements().size() == 1 && block.statements().get(0) instanceof IfStatement) {
          innerIf = (IfStatement) block.statements().get(0);
        }
      }
      if (innerIf != null && innerIf.getElseStatement() == null) {
        if (resultingCollections == null) {
          return true;
        }
        //
        AST ast = node.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        // prepare conditions
        Expression innerCondition = (Expression) rewrite.createCopyTarget(innerIf.getExpression());
        Expression outerCondition =
            (Expression) rewrite.createCopyTarget(ifStatement.getExpression());
        // exchange conditions
        rewrite.replace(innerIf.getExpression(), outerCondition, null);
        rewrite.replace(ifStatement.getExpression(), innerCondition, null);
        // add correction proposal
        String label =
            CorrectionMessages
                .AdvancedQuickAssistProcessor_exchangeInnerAndOuterIfConditions_description;
        Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.EXCHANGE_INNER_AND_OUTER_IF_CONDITIONS,
                image);
        resultingCollections.add(proposal);
        result = true;
      }
    }
    return result;
  }

  private static boolean getExchangeOperandsProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    // check that user invokes quick assist on infix expression
    if (!(node instanceof InfixExpression)) {
      return false;
    }
    InfixExpression infixExpression = (InfixExpression) node;
    Operator operator = infixExpression.getOperator();
    if (operator != InfixExpression.Operator.CONDITIONAL_AND
        && operator != InfixExpression.Operator.AND
        && operator != InfixExpression.Operator.CONDITIONAL_OR
        && operator != InfixExpression.Operator.OR
        && operator != InfixExpression.Operator.EQUALS
        && operator != InfixExpression.Operator.NOT_EQUALS
        && operator != InfixExpression.Operator.LESS
        && operator != InfixExpression.Operator.LESS_EQUALS
        && operator != InfixExpression.Operator.GREATER
        && operator != InfixExpression.Operator.GREATER_EQUALS
        && operator != InfixExpression.Operator.PLUS
        && operator != InfixExpression.Operator.TIMES
        && operator != InfixExpression.Operator.XOR) {
      return false;
    }

    int offset =
        isOperatorSelected(
            infixExpression, context.getSelectionOffset(), context.getSelectionLength());
    if (offset == -1) {
      return false;
    }

    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    AST ast = infixExpression.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepare left and right expressions
    Expression leftExpression = null;
    Expression rightExpression = null;
    InfixExpression currentExpression = infixExpression;
    leftExpression =
        combineOperands(rewrite, leftExpression, infixExpression.getLeftOperand(), false, operator);
    if (infixExpression.getRightOperand().getStartPosition() <= context.getSelectionOffset()) {
      leftExpression =
          combineOperands(
              rewrite, leftExpression, infixExpression.getRightOperand(), false, operator);
    } else {
      rightExpression =
          combineOperands(
              rewrite, rightExpression, infixExpression.getRightOperand(), false, operator);
    }
    for (Iterator<Expression> iter = currentExpression.extendedOperands().iterator();
        iter.hasNext(); ) {
      Expression extendedOperand = iter.next();
      if (extendedOperand.getStartPosition() <= context.getSelectionOffset()) {
        leftExpression = combineOperands(rewrite, leftExpression, extendedOperand, false, operator);
      } else {
        rightExpression =
            combineOperands(rewrite, rightExpression, extendedOperand, false, operator);
      }
    }

    if (NecessaryParenthesesChecker.needsParentheses(
        leftExpression, infixExpression, InfixExpression.RIGHT_OPERAND_PROPERTY)) {
      leftExpression = getParenthesizedExpression(ast, leftExpression);
    }
    if (NecessaryParenthesesChecker.needsParentheses(
        rightExpression, infixExpression, InfixExpression.LEFT_OPERAND_PROPERTY)) {
      rightExpression = getParenthesizedExpression(ast, rightExpression);
    }

    if (operator == InfixExpression.Operator.LESS) {
      operator = InfixExpression.Operator.GREATER;
    } else if (operator == InfixExpression.Operator.LESS_EQUALS) {
      operator = InfixExpression.Operator.GREATER_EQUALS;
    } else if (operator == InfixExpression.Operator.GREATER) {
      operator = InfixExpression.Operator.LESS;
    } else if (operator == InfixExpression.Operator.GREATER_EQUALS) {
      operator = InfixExpression.Operator.LESS_EQUALS;
    }

    // create new infix expression
    InfixExpression newInfix = ast.newInfixExpression();
    newInfix.setOperator(operator);
    newInfix.setLeftOperand(rightExpression);
    newInfix.setRightOperand(leftExpression);
    rewrite.replace(infixExpression, newInfix, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_exchangeOperands_description;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.EXCHANGE_OPERANDS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  /*
   * Breaks an infix operation with possible extended operators at the given operator and returns the new left and right operands.
   * a & b & c   ->  [[a' & b' ] & c' ]   (c' == copy of c)
   */
  private static void breakInfixOperationAtOperation(
      ASTRewrite rewrite,
      Expression expression,
      Operator operator,
      int operatorOffset,
      boolean removeParentheses,
      Expression[] res) {
    if (expression.getStartPosition() + expression.getLength() <= operatorOffset) {
      // add to the left
      res[0] = combineOperands(rewrite, res[0], expression, removeParentheses, operator);
      return;
    }
    if (operatorOffset <= expression.getStartPosition()) {
      // add to the right
      res[1] = combineOperands(rewrite, res[1], expression, removeParentheses, operator);
      return;
    }
    if (!(expression instanceof InfixExpression)) {
      throw new IllegalArgumentException("Cannot break up non-infix expression"); // $NON-NLS-1$
    }
    InfixExpression infixExpression = (InfixExpression) expression;
    if (infixExpression.getOperator() != operator) {
      throw new IllegalArgumentException("Incompatible operator"); // $NON-NLS-1$
    }
    breakInfixOperationAtOperation(
        rewrite,
        infixExpression.getLeftOperand(),
        operator,
        operatorOffset,
        removeParentheses,
        res);
    breakInfixOperationAtOperation(
        rewrite,
        infixExpression.getRightOperand(),
        operator,
        operatorOffset,
        removeParentheses,
        res);

    List<Expression> extended = infixExpression.extendedOperands();
    for (int i = 0; i < extended.size(); i++) {
      breakInfixOperationAtOperation(
          rewrite, extended.get(i), operator, operatorOffset, removeParentheses, res);
    }
  }

  private static Expression combineOperands(
      ASTRewrite rewrite,
      Expression existing,
      Expression originalNode,
      boolean removeParentheses,
      Operator operator) {
    if (existing == null && removeParentheses) {
      while (originalNode instanceof ParenthesizedExpression) {
        originalNode = ((ParenthesizedExpression) originalNode).getExpression();
      }
    }
    Expression newRight = (Expression) rewrite.createMoveTarget(originalNode);
    if (originalNode instanceof InfixExpression) {
      ((InfixExpression) newRight).setOperator(((InfixExpression) originalNode).getOperator());
    }

    if (existing == null) {
      return newRight;
    }
    AST ast = rewrite.getAST();
    InfixExpression infix = ast.newInfixExpression();
    infix.setOperator(operator);
    infix.setLeftOperand(existing);
    infix.setRightOperand(newRight);
    return infix;
  }

  private static boolean getCastAndAssignIfStatementProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (node instanceof IfStatement) {
      node = ((IfStatement) node).getExpression();
    } else if (node instanceof WhileStatement) {
      node = ((WhileStatement) node).getExpression();
    } else if (node instanceof Block) {
      List<Statement> statements = ((Block) node).statements();
      if (statements.size() > 0) {
        if (context.getSelectionOffset() > statements.get(0).getStartPosition()) {
          return false;
        }
      }
      ASTNode parent = node.getParent();
      Expression expression = null;
      if (parent instanceof IfStatement) {
        expression = ((IfStatement) parent).getExpression();
      } else if (parent instanceof WhileStatement) {
        expression = ((WhileStatement) parent).getExpression();
      } else {
        return false;
      }

      if (expression instanceof InstanceofExpression) {
        node = expression;
      } else {
        final ArrayList<InstanceofExpression> nodes = new ArrayList<InstanceofExpression>();
        expression.accept(
            new ASTVisitor() {
              @Override
              public boolean visit(InstanceofExpression instanceofExpression) {
                nodes.add(instanceofExpression);
                return false;
              }
            });

        if (nodes.size() != 1) {
          return false;
        }
        node = nodes.get(0);
      }
    } else {
      while (node != null
          && !(node instanceof InstanceofExpression)
          && !(node instanceof Statement)) {
        node = node.getParent();
      }
    }

    if (!(node instanceof InstanceofExpression)) {
      return false;
    }
    InstanceofExpression expression = (InstanceofExpression) node;
    // test that we are the expression of a 'while' or 'if'
    while (node.getParent() instanceof Expression) {
      node = node.getParent();
    }
    StructuralPropertyDescriptor locationInParent = node.getLocationInParent();

    boolean negated = isNegated(expression);

    Statement body = null;
    ASTNode insertionPosition = null;
    if (negated) {
      insertionPosition = node.getParent();
      if (locationInParent == IfStatement.EXPRESSION_PROPERTY) {
        body = ((IfStatement) node.getParent()).getElseStatement();
        if (body != null) {
          negated = false;
        }
      }
      if (body == null && insertionPosition.getParent() instanceof Block) {
        body = (Statement) insertionPosition.getParent();
      }
    } else {
      if (locationInParent == IfStatement.EXPRESSION_PROPERTY) {
        body = ((IfStatement) node.getParent()).getThenStatement();
      } else if (locationInParent == WhileStatement.EXPRESSION_PROPERTY) {
        body = ((WhileStatement) node.getParent()).getBody();
      }
    }
    if (body == null) {
      return false;
    }

    Type originalType = expression.getRightOperand();
    if (originalType.resolveBinding() == null) {
      return false;
    }

    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }

    final String KEY_NAME = "name"; // $NON-NLS-1$
    final String KEY_TYPE = "type"; // $NON-NLS-1$
    //
    AST ast = expression.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ICompilationUnit cu = context.getCompilationUnit();
    // prepare correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_castAndAssign;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(label, cu, rewrite, IProposalRelevance.CAST_AND_ASSIGN, image);
    // prepare possible variable names
    List<String> excludedNames = Arrays.asList(ASTResolving.getUsedVariableNames(body));
    String[] varNames = suggestLocalVariableNames(cu, originalType.resolveBinding(), excludedNames);
    for (int i = 0; i < varNames.length; i++) {
      proposal.addLinkedPositionProposal(KEY_NAME, varNames[i], null);
    }
    CastExpression castExpression = ast.newCastExpression();
    castExpression.setExpression(
        (Expression) rewrite.createCopyTarget(expression.getLeftOperand()));
    castExpression.setType((Type) ASTNode.copySubtree(ast, originalType));
    // prepare new variable declaration
    VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
    vdf.setName(ast.newSimpleName(varNames[0]));
    vdf.setInitializer(castExpression);
    // prepare new variable declaration statement
    VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
    vds.setType((Type) ASTNode.copySubtree(ast, originalType));

    // add new variable declaration statement
    if (negated) {
      ListRewrite listRewriter = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);
      listRewriter.insertAfter(vds, insertionPosition, null);
    } else {
      if (body instanceof Block) {
        ListRewrite listRewriter = rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY);
        listRewriter.insertAt(vds, 0, null);
      } else {
        Block newBlock = ast.newBlock();
        List<Statement> statements = newBlock.statements();
        statements.add(vds);
        statements.add((Statement) rewrite.createMoveTarget(body));
        rewrite.replace(body, newBlock, null);
      }
    }

    // setup linked positions
    proposal.addLinkedPosition(rewrite.track(vdf.getName()), true, KEY_NAME);
    proposal.addLinkedPosition(rewrite.track(vds.getType()), false, KEY_TYPE);
    proposal.addLinkedPosition(rewrite.track(castExpression.getType()), false, KEY_TYPE);
    proposal.setEndPosition(rewrite.track(vds)); // set cursor after expression statement
    // add correction proposal
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean isNegated(Expression expression) {
    if (!(expression.getParent() instanceof ParenthesizedExpression)) return false;

    ParenthesizedExpression parenthesis = (ParenthesizedExpression) expression.getParent();
    if (!(parenthesis.getParent() instanceof PrefixExpression)) return false;

    PrefixExpression prefix = (PrefixExpression) parenthesis.getParent();
    if (!(prefix.getOperator() == PrefixExpression.Operator.NOT)) return false;

    return true;
  }

  private static String[] suggestLocalVariableNames(
      ICompilationUnit cu, ITypeBinding binding, List<String> excluded) {
    return StubUtility.getVariableNameSuggestions(
        NamingConventions.VK_LOCAL, cu.getJavaProject(), binding, null, excluded);
  }

  private static boolean getCombineStringProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    // we work with InfixExpressions
    InfixExpression infixExpression;
    if (node instanceof InfixExpression) {
      infixExpression = (InfixExpression) node;
    } else if (node.getParent() instanceof InfixExpression) {
      infixExpression = (InfixExpression) node.getParent();
    } else {
      return false;
    }

    // only + is valid for combining strings
    if (!(infixExpression.getOperator().equals(InfixExpression.Operator.PLUS))) {
      return false;
    }

    // all expressions must be strings
    Expression leftOperand = infixExpression.getLeftOperand();
    Expression rightOperand = infixExpression.getRightOperand();
    if (!(leftOperand instanceof StringLiteral && rightOperand instanceof StringLiteral)) {
      return false;
    }

    StringLiteral leftString = (StringLiteral) leftOperand;
    StringLiteral rightString = (StringLiteral) rightOperand;

    if (resultingCollections == null) {
      return true;
    }

    // begin building combined string
    StringBuilder stringBuilder = new StringBuilder(leftString.getLiteralValue());
    stringBuilder.append(rightString.getLiteralValue());

    // append extended string literals
    for (Object operand : infixExpression.extendedOperands()) {
      if (!(operand instanceof StringLiteral)) return false;
      StringLiteral stringLiteral = (StringLiteral) operand;
      stringBuilder.append(stringLiteral.getLiteralValue());
    }

    // prepare new string literal
    AST ast = node.getAST();
    StringLiteral combinedStringLiteral = ast.newStringLiteral();
    combinedStringLiteral.setLiteralValue(stringBuilder.toString());

    ASTRewrite rewrite = ASTRewrite.create(ast);
    rewrite.replace(infixExpression, combinedStringLiteral, null);

    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_combineSelectedStrings;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.COMBINE_STRINGS,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getPickOutStringProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    // we work with String's
    if (!(node instanceof StringLiteral)) {
      return false;
    }
    // user should select part of String
    int selectionPos = context.getSelectionOffset();
    int selectionLen = context.getSelectionLength();
    if (selectionLen == 0) {
      return false;
    }
    int valueStart = node.getStartPosition() + 1;
    int valueEnd = node.getStartPosition() + node.getLength() - 1;

    // selection must be inside node and the quotes and not contain the full value
    if (selectionPos < valueStart
        || selectionPos + selectionLen > valueEnd
        || valueEnd - valueStart == selectionLen) {
      return false;
    }

    // prepare string parts positions
    StringLiteral stringLiteral = (StringLiteral) node;
    String stringValue = stringLiteral.getEscapedValue();

    int firstPos = selectionPos - node.getStartPosition();
    int secondPos = firstPos + selectionLen;

    // prepare new string literals

    AST ast = node.getAST();
    StringLiteral leftLiteral = ast.newStringLiteral();
    StringLiteral centerLiteral = ast.newStringLiteral();
    StringLiteral rightLiteral = ast.newStringLiteral();
    try {
      leftLiteral.setEscapedValue('"' + stringValue.substring(1, firstPos) + '"');
      centerLiteral.setEscapedValue('"' + stringValue.substring(firstPos, secondPos) + '"');
      rightLiteral.setEscapedValue(
          '"' + stringValue.substring(secondPos, stringValue.length() - 1) + '"');
    } catch (IllegalArgumentException e) {
      return false;
    }
    if (resultingCollections == null) {
      return true;
    }

    ASTRewrite rewrite = ASTRewrite.create(ast);

    // prepare new expression instead of StringLiteral
    InfixExpression expression = ast.newInfixExpression();
    expression.setOperator(InfixExpression.Operator.PLUS);
    if (firstPos != 1) {
      expression.setLeftOperand(leftLiteral);
    }

    if (firstPos == 1) {
      expression.setLeftOperand(centerLiteral);
    } else {
      expression.setRightOperand(centerLiteral);
    }

    if (secondPos < stringValue.length() - 1) {
      if (firstPos == 1) {
        expression.setRightOperand(rightLiteral);
      } else {
        expression.extendedOperands().add(rightLiteral);
      }
    }
    // use new expression instead of old StirngLiteral
    rewrite.replace(stringLiteral, expression, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_pickSelectedString;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.PICK_SELECTED_STRING,
            image);
    proposal.addLinkedPosition(rewrite.track(centerLiteral), true, "CENTER_STRING"); // $NON-NLS-1$
    resultingCollections.add(proposal);
    return true;
  }

  private static Statement getSingleStatement(Statement statement) {
    if (statement instanceof Block) {
      List<Statement> blockStatements = ((Block) statement).statements();
      if (blockStatements.size() != 1) {
        return null;
      }
      return blockStatements.get(0);
    }
    return statement;
  }

  private static boolean getReplaceIfElseWithConditionalProposals(
      IInvocationContext context, ASTNode node, Collection<ICommandAccess> resultingCollections) {
    if (!(node instanceof IfStatement)) {
      return false;
    }
    IfStatement ifStatement = (IfStatement) node;
    Statement thenStatement = getSingleStatement(ifStatement.getThenStatement());
    Statement elseStatement = getSingleStatement(ifStatement.getElseStatement());
    if (thenStatement == null || elseStatement == null) {
      return false;
    }
    Expression assigned = null;
    Expression thenExpression = null;
    Expression elseExpression = null;

    ITypeBinding exprBinding = null;
    if (thenStatement instanceof ReturnStatement && elseStatement instanceof ReturnStatement) {
      thenExpression = ((ReturnStatement) thenStatement).getExpression();
      elseExpression = ((ReturnStatement) elseStatement).getExpression();
      MethodDeclaration declaration = ASTResolving.findParentMethodDeclaration(node);
      if (declaration == null || declaration.isConstructor()) {
        return false;
      }
      exprBinding = declaration.getReturnType2().resolveBinding();
    } else if (thenStatement instanceof ExpressionStatement
        && elseStatement instanceof ExpressionStatement) {
      Expression inner1 = ((ExpressionStatement) thenStatement).getExpression();
      Expression inner2 = ((ExpressionStatement) elseStatement).getExpression();
      if (inner1 instanceof Assignment && inner2 instanceof Assignment) {
        Assignment assign1 = (Assignment) inner1;
        Assignment assign2 = (Assignment) inner2;
        Expression left1 = assign1.getLeftHandSide();
        Expression left2 = assign2.getLeftHandSide();
        if (left1 instanceof Name
            && left2 instanceof Name
            && assign1.getOperator() == assign2.getOperator()) {
          IBinding bind1 = ((Name) left1).resolveBinding();
          IBinding bind2 = ((Name) left2).resolveBinding();
          if (bind1 == bind2 && bind1 instanceof IVariableBinding) {
            assigned = left1;
            exprBinding = ((IVariableBinding) bind1).getType();
            thenExpression = assign1.getRightHandSide();
            elseExpression = assign2.getRightHandSide();
          }
        }
      }
    }
    if (thenExpression == null || elseExpression == null) {
      return false;
    }

    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = node.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    TightSourceRangeComputer sourceRangeComputer = new TightSourceRangeComputer();
    sourceRangeComputer.addTightSourceNode(ifStatement);
    rewrite.setTargetSourceRangeComputer(sourceRangeComputer);

    String label = CorrectionMessages.AdvancedQuickAssistProcessor_replaceIfWithConditional;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.REPLACE_IF_ELSE_WITH_CONDITIONAL,
            image);

    // prepare conditional expression
    ConditionalExpression conditionalExpression = ast.newConditionalExpression();
    Expression conditionCopy = (Expression) rewrite.createCopyTarget(ifStatement.getExpression());
    conditionalExpression.setExpression(conditionCopy);
    Expression thenCopy = (Expression) rewrite.createCopyTarget(thenExpression);
    Expression elseCopy = (Expression) rewrite.createCopyTarget(elseExpression);

    IJavaProject project = context.getCompilationUnit().getJavaProject();
    if (!JavaModelUtil.is50OrHigher(project)) {
      ITypeBinding thenBinding = thenExpression.resolveTypeBinding();
      ITypeBinding elseBinding = elseExpression.resolveTypeBinding();
      if (thenBinding != null
          && elseBinding != null
          && exprBinding != null
          && !elseBinding.isAssignmentCompatible(thenBinding)) {
        CastExpression castException = ast.newCastExpression();
        ImportRewrite importRewrite = proposal.createImportRewrite(context.getASTRoot());
        ImportRewriteContext importRewriteContext =
            new ContextSensitiveImportRewriteContext(node, importRewrite);
        castException.setType(importRewrite.addImport(exprBinding, ast, importRewriteContext));
        castException.setExpression(elseCopy);
        elseCopy = castException;
      }
    } else if (JavaModelUtil.is17OrHigher(project)) {
      addExplicitTypeArgumentsIfNecessary(rewrite, proposal, thenExpression);
      addExplicitTypeArgumentsIfNecessary(rewrite, proposal, elseExpression);
    }
    conditionalExpression.setThenExpression(thenCopy);
    conditionalExpression.setElseExpression(elseCopy);

    // replace 'if' statement with conditional expression
    if (assigned == null) {
      ReturnStatement returnStatement = ast.newReturnStatement();
      returnStatement.setExpression(conditionalExpression);
      rewrite.replace(ifStatement, returnStatement, null);
    } else {
      Assignment assignment = ast.newAssignment();
      assignment.setLeftHandSide((Expression) rewrite.createCopyTarget(assigned));
      assignment.setRightHandSide(conditionalExpression);
      assignment.setOperator(((Assignment) assigned.getParent()).getOperator());

      ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
      rewrite.replace(ifStatement, expressionStatement, null);
    }

    // add correction proposal
    resultingCollections.add(proposal);
    return true;
  }

  private static void addExplicitTypeArgumentsIfNecessary(
      ASTRewrite rewrite, ASTRewriteCorrectionProposal proposal, Expression invocation) {
    if (Invocations.isResolvedTypeInferredFromExpectedType(invocation)) {
      ITypeBinding[] typeArguments = Invocations.getInferredTypeArguments(invocation);
      if (typeArguments == null) return;

      ImportRewrite importRewrite = proposal.getImportRewrite();
      if (importRewrite == null) {
        importRewrite = proposal.createImportRewrite((CompilationUnit) invocation.getRoot());
      }
      ImportRewriteContext importRewriteContext =
          new ContextSensitiveImportRewriteContext(invocation, importRewrite);

      AST ast = invocation.getAST();
      ListRewrite typeArgsRewrite =
          Invocations.getInferredTypeArgumentsRewrite(rewrite, invocation);

      for (int i = 0; i < typeArguments.length; i++) {
        Type typeArgumentNode =
            importRewrite.addImport(typeArguments[i], ast, importRewriteContext);
        typeArgsRewrite.insertLast(typeArgumentNode, null);
      }

      if (invocation instanceof MethodInvocation) {
        MethodInvocation methodInvocation = (MethodInvocation) invocation;
        Expression expression = methodInvocation.getExpression();
        if (expression == null) {
          IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
          if (methodBinding != null && Modifier.isStatic(methodBinding.getModifiers())) {
            expression =
                ast.newName(
                    importRewrite.addImport(
                        methodBinding.getDeclaringClass().getTypeDeclaration(),
                        importRewriteContext));
          } else {
            expression = ast.newThisExpression();
          }
          rewrite.set(invocation, MethodInvocation.EXPRESSION_PROPERTY, expression, null);
        }
      }
    }
  }

  private static ReturnStatement createReturnExpression(ASTRewrite rewrite, Expression expression) {
    AST ast = rewrite.getAST();
    ReturnStatement thenReturn = ast.newReturnStatement();
    thenReturn.setExpression((Expression) rewrite.createCopyTarget(expression));
    return thenReturn;
  }

  private static Statement createAssignmentStatement(
      ASTRewrite rewrite,
      Assignment.Operator assignmentOperator,
      Expression origAssignee,
      Expression origAssigned) {
    AST ast = rewrite.getAST();
    Assignment elseAssignment = ast.newAssignment();
    elseAssignment.setOperator(assignmentOperator);
    elseAssignment.setLeftHandSide((Expression) rewrite.createCopyTarget(origAssignee));
    elseAssignment.setRightHandSide((Expression) rewrite.createCopyTarget(origAssigned));
    ExpressionStatement statement = ast.newExpressionStatement(elseAssignment);
    return statement;
  }

  private static boolean getReplaceConditionalWithIfElseProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    ASTNode node = covering;
    while (!(node instanceof ConditionalExpression) && node instanceof Expression) {
      node = node.getParent();
    }
    if (!(node instanceof ConditionalExpression)) {
      node = covering;
      while (node != null && !(node instanceof Statement)) {
        node = node.getParent();
      }
      if (node instanceof VariableDeclarationStatement) {
        node = (ASTNode) (((VariableDeclarationStatement) node).fragments().get(0));
        node = ((VariableDeclarationFragment) node).getInitializer();
      }
      if (node instanceof ExpressionStatement) {
        node = ((ExpressionStatement) node).getExpression();
        if (node instanceof Assignment) {
          node = ((Assignment) node).getRightHandSide();
        }
      }
      if (node instanceof ReturnStatement) {
        node = ((ReturnStatement) node).getExpression();
      }
    }

    if (!(node instanceof ConditionalExpression)) {
      return false;
    }
    covering = node;

    StructuralPropertyDescriptor locationInParent = covering.getLocationInParent();
    if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
      if (covering.getParent().getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
        return false;
      }
    } else if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
      ASTNode statement = covering.getParent().getParent();
      if (!(statement instanceof VariableDeclarationStatement)
          || statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
        return false;
      }
    } else if (locationInParent != ReturnStatement.EXPRESSION_PROPERTY) {
      return false;
    }

    ConditionalExpression conditional = (ConditionalExpression) covering;
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = covering.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepare new 'if' statement
    Expression expression = conditional.getExpression();
    while (expression instanceof ParenthesizedExpression) {
      expression = ((ParenthesizedExpression) expression).getExpression();
    }
    IfStatement ifStatement = ast.newIfStatement();
    ifStatement.setExpression((Expression) rewrite.createCopyTarget(expression));
    if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
      Assignment assignment = (Assignment) covering.getParent();
      Expression assignee = assignment.getLeftHandSide();
      Assignment.Operator op = assignment.getOperator();

      ifStatement.setThenStatement(
          createAssignmentStatement(rewrite, op, assignee, conditional.getThenExpression()));
      ifStatement.setElseStatement(
          createAssignmentStatement(rewrite, op, assignee, conditional.getElseExpression()));

      // replace return conditional expression with if/then/else/return
      rewrite.replace(covering.getParent().getParent(), ifStatement, null);

    } else if (locationInParent == ReturnStatement.EXPRESSION_PROPERTY) {
      ifStatement.setThenStatement(
          createReturnExpression(rewrite, conditional.getThenExpression()));
      ifStatement.setElseStatement(
          createReturnExpression(rewrite, conditional.getElseExpression()));
      //
      // replace return conditional expression with if/then/else/return
      rewrite.replace(conditional.getParent(), ifStatement, null);
    } else if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
      VariableDeclarationFragment frag = (VariableDeclarationFragment) covering.getParent();
      Assignment.Operator op = Assignment.Operator.ASSIGN;

      Expression assignee = frag.getName();
      ifStatement.setThenStatement(
          createAssignmentStatement(rewrite, op, assignee, conditional.getThenExpression()));
      ifStatement.setElseStatement(
          createAssignmentStatement(rewrite, op, assignee, conditional.getElseExpression()));

      rewrite.set(
          frag, VariableDeclarationFragment.INITIALIZER_PROPERTY, null, null); // clear initializer

      ASTNode statement = frag.getParent();
      rewrite
          .getListRewrite(statement.getParent(), Block.STATEMENTS_PROPERTY)
          .insertAfter(ifStatement, statement, null);
    }

    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_replaceConditionalWithIf;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.REPLACE_CONDITIONAL_WITH_IF_ELSE,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getInverseLocalVariableProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    final AST ast = covering.getAST();
    // cursor should be placed on variable name
    if (!(covering instanceof SimpleName)) {
      return false;
    }
    SimpleName coveringName = (SimpleName) covering;
    if (!coveringName.isDeclaration()) {
      return false;
    }
    // prepare bindings
    final IBinding variableBinding = coveringName.resolveBinding();
    if (!(variableBinding instanceof IVariableBinding)) {
      return false;
    }
    IVariableBinding binding = (IVariableBinding) variableBinding;
    if (binding.isField()) {
      return false;
    }
    // we operate only on boolean variable
    if (!isBoolean(coveringName)) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    // find linked nodes
    final MethodDeclaration method = ASTResolving.findParentMethodDeclaration(covering);
    SimpleName[] linkedNodes = LinkedNodeFinder.findByBinding(method, variableBinding);
    //
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    // create proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_inverseBooleanVariable;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    final String KEY_NAME = "name"; // $NON-NLS-1$
    final LinkedCorrectionProposal proposal =
        new LinkedCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.INVERSE_BOOLEAN_VARIABLE,
            image);
    // prepare new variable identifier
    final String oldIdentifier = coveringName.getIdentifier();
    final String notString =
        Messages.format(
            CorrectionMessages.AdvancedQuickAssistProcessor_negatedVariableName, ""); // $NON-NLS-1$
    final String newIdentifier;
    if (oldIdentifier.startsWith(notString)) {
      int notLength = notString.length();
      if (oldIdentifier.length() > notLength) {
        newIdentifier =
            Character.toLowerCase(oldIdentifier.charAt(notLength))
                + oldIdentifier.substring(notLength + 1);
      } else {
        newIdentifier = oldIdentifier;
      }
    } else {
      newIdentifier =
          Messages.format(
              CorrectionMessages.AdvancedQuickAssistProcessor_negatedVariableName,
              Character.toUpperCase(oldIdentifier.charAt(0)) + oldIdentifier.substring(1));
    }
    //
    proposal.addLinkedPositionProposal(KEY_NAME, newIdentifier, null);
    proposal.addLinkedPositionProposal(KEY_NAME, oldIdentifier, null);
    // iterate over linked nodes and replace variable references with negated reference
    final HashSet<SimpleName> renamedNames = new HashSet<SimpleName>();
    for (int i = 0; i < linkedNodes.length; i++) {
      SimpleName name = linkedNodes[i];
      if (renamedNames.contains(name)) {
        continue;
      }
      // prepare new name with new identifier
      SimpleName newName = ast.newSimpleName(newIdentifier);
      proposal.addLinkedPosition(rewrite.track(newName), name == coveringName, KEY_NAME);
      //
      StructuralPropertyDescriptor location = name.getLocationInParent();
      if (location == SingleVariableDeclaration.NAME_PROPERTY) {
        // set new name
        rewrite.replace(name, newName, null);
      } else if (location == Assignment.LEFT_HAND_SIDE_PROPERTY) {
        Assignment assignment = (Assignment) name.getParent();
        Expression expression = assignment.getRightHandSide();
        int exStart = expression.getStartPosition();
        int exEnd = exStart + expression.getLength();
        // collect all names that are used in assignments
        HashSet<SimpleName> overlapNames = new HashSet<SimpleName>();
        for (int j = 0; j < linkedNodes.length; j++) {
          SimpleName name2 = linkedNodes[j];
          if (name2 == null) {
            continue;
          }
          int name2Start = name2.getStartPosition();
          if (exStart <= name2Start && name2Start < exEnd) {
            overlapNames.add(name2);
          }
        }
        // prepare inverted expression
        SimpleNameRenameProvider provider =
            new SimpleNameRenameProvider() {
              public SimpleName getRenamed(SimpleName simpleName) {
                if (simpleName.resolveBinding() == variableBinding) {
                  renamedNames.add(simpleName);
                  return ast.newSimpleName(newIdentifier);
                }
                return null;
              }
            };
        Expression inversedExpression = getInversedExpression(rewrite, expression, provider);
        // if any name was not renamed during expression inverting, we can not already rename it, so
        // fail to create assist
        for (Iterator<SimpleName> iter = overlapNames.iterator(); iter.hasNext(); ) {
          Object o = iter.next();
          if (!renamedNames.contains(o)) {
            return false;
          }
        }
        // check operator and replace if needed
        Assignment.Operator operator = assignment.getOperator();
        if (operator == Assignment.Operator.BIT_AND_ASSIGN) {
          Assignment newAssignment = ast.newAssignment();
          newAssignment.setLeftHandSide(newName);
          newAssignment.setRightHandSide(inversedExpression);
          newAssignment.setOperator(Assignment.Operator.BIT_OR_ASSIGN);
          rewrite.replace(assignment, newAssignment, null);
        } else if (operator == Assignment.Operator.BIT_OR_ASSIGN) {
          Assignment newAssignment = ast.newAssignment();
          newAssignment.setLeftHandSide(newName);
          newAssignment.setRightHandSide(inversedExpression);
          newAssignment.setOperator(Assignment.Operator.BIT_AND_ASSIGN);
          rewrite.replace(assignment, newAssignment, null);
        } else {
          rewrite.replace(expression, inversedExpression, null);
          // set new name
          rewrite.replace(name, newName, null);
        }
      } else if (location == VariableDeclarationFragment.NAME_PROPERTY) {
        // replace initializer for variable
        VariableDeclarationFragment vdf = (VariableDeclarationFragment) name.getParent();
        Expression expression = vdf.getInitializer();
        if (expression != null) {
          rewrite.replace(expression, getInversedExpression(rewrite, expression), null);
        }
        // set new name
        rewrite.replace(name, newName, null);
      } else if (name.getParent() instanceof PrefixExpression
          && ((PrefixExpression) name.getParent()).getOperator() == PrefixExpression.Operator.NOT) {
        rewrite.replace(name.getParent(), newName, null);
      } else {
        PrefixExpression expression = ast.newPrefixExpression();
        expression.setOperator(PrefixExpression.Operator.NOT);
        expression.setOperand(newName);
        rewrite.replace(name, expression, null);
      }
    }
    // add correction proposal
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getPushNegationDownProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    PrefixExpression negationExpression = null;
    ParenthesizedExpression parenthesizedExpression = null;
    // check for case when cursor is on '!' before parentheses
    if (covering instanceof PrefixExpression) {
      PrefixExpression prefixExpression = (PrefixExpression) covering;
      if (prefixExpression.getOperator() == PrefixExpression.Operator.NOT
          && prefixExpression.getOperand() instanceof ParenthesizedExpression) {
        negationExpression = prefixExpression;
        parenthesizedExpression = (ParenthesizedExpression) prefixExpression.getOperand();
      }
    }
    // check for case when cursor is on parenthesized expression that is negated
    if (covering instanceof ParenthesizedExpression
        && covering.getParent() instanceof PrefixExpression
        && ((PrefixExpression) covering.getParent()).getOperator()
            == PrefixExpression.Operator.NOT) {
      negationExpression = (PrefixExpression) covering.getParent();
      parenthesizedExpression = (ParenthesizedExpression) covering;
    }
    if (negationExpression == null
        || (!(parenthesizedExpression.getExpression() instanceof InfixExpression)
            && !(parenthesizedExpression.getExpression() instanceof ConditionalExpression))) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    final AST ast = covering.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepared inverted expression
    Expression inversedExpression =
        getInversedExpression(rewrite, parenthesizedExpression.getExpression());
    // check, may be we should keep parentheses
    boolean keepParentheses = false;
    if (negationExpression.getParent() instanceof Expression) {
      int parentPrecedence =
          OperatorPrecedence.getExpressionPrecedence(((Expression) negationExpression.getParent()));
      int inversedExpressionPrecedence =
          OperatorPrecedence.getExpressionPrecedence(inversedExpression);
      keepParentheses = parentPrecedence > inversedExpressionPrecedence;
    }
    // replace negated expression with inverted one
    if (keepParentheses) {
      ParenthesizedExpression pe = ast.newParenthesizedExpression();
      pe.setExpression(inversedExpression);
      rewrite.replace(negationExpression, pe, null);
    } else {
      rewrite.replace(negationExpression, inversedExpression, null);
    }
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_pushNegationDown;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.PULL_NEGATION_DOWN,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static Expression getBooleanExpression(ASTNode node) {
    if (!(node instanceof Expression)) {
      return null;
    }

    // check if the node is a location where it can be negated
    StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
    if (locationInParent == QualifiedName.NAME_PROPERTY) {
      node = node.getParent();
      locationInParent = node.getLocationInParent();
    }
    while (locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY) {
      node = node.getParent();
      locationInParent = node.getLocationInParent();
    }
    Expression expression = (Expression) node;
    if (!isBoolean(expression)) {
      return null;
    }
    if (expression.getParent() instanceof InfixExpression) {
      return expression;
    }
    if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY
        || locationInParent == IfStatement.EXPRESSION_PROPERTY
        || locationInParent == WhileStatement.EXPRESSION_PROPERTY
        || locationInParent == DoStatement.EXPRESSION_PROPERTY
        || locationInParent == ReturnStatement.EXPRESSION_PROPERTY
        || locationInParent == ForStatement.EXPRESSION_PROPERTY
        || locationInParent == AssertStatement.EXPRESSION_PROPERTY
        || locationInParent == MethodInvocation.ARGUMENTS_PROPERTY
        || locationInParent == ConstructorInvocation.ARGUMENTS_PROPERTY
        || locationInParent == SuperMethodInvocation.ARGUMENTS_PROPERTY
        || locationInParent == EnumConstantDeclaration.ARGUMENTS_PROPERTY
        || locationInParent == SuperConstructorInvocation.ARGUMENTS_PROPERTY
        || locationInParent == ClassInstanceCreation.ARGUMENTS_PROPERTY
        || locationInParent == ConditionalExpression.EXPRESSION_PROPERTY
        || locationInParent == PrefixExpression.OPERAND_PROPERTY) {
      return expression;
    }
    return null;
  }

  private static boolean getPullNegationUpProposals(
      IInvocationContext context,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    if (coveredNodes.size() != 1) {
      return false;
    }
    //
    ASTNode fullyCoveredNode = coveredNodes.get(0);

    Expression expression = getBooleanExpression(fullyCoveredNode);
    if (expression == null
        || (!(expression instanceof InfixExpression)
            && !(expression instanceof ConditionalExpression))) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    AST ast = expression.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    // prepared inverted expression
    Expression inversedExpression = getInversedExpression(rewrite, expression);
    // prepare ParenthesizedExpression
    ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
    parenthesizedExpression.setExpression(inversedExpression);
    // prepare NOT prefix expression
    PrefixExpression prefixExpression = ast.newPrefixExpression();
    prefixExpression.setOperator(PrefixExpression.Operator.NOT);
    prefixExpression.setOperand(parenthesizedExpression);
    // replace old expression
    rewrite.replace(expression, prefixExpression, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_pullNegationUp;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.PULL_NEGATION_UP,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getJoinIfListInIfElseIfProposals(
      IInvocationContext context,
      ASTNode covering,
      ArrayList<ASTNode> coveredNodes,
      Collection<ICommandAccess> resultingCollections) {
    if (coveredNodes.isEmpty()) {
      return false;
    }
    // check that we have more than one covered statement
    if (coveredNodes.size() < 2) {
      return false;
    }
    // check that all selected nodes are 'if' statements with only 'then' statement
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      ASTNode node = iter.next();
      if (!(node instanceof IfStatement)) {
        return false;
      }
      IfStatement ifStatement = (IfStatement) node;
      if (ifStatement.getElseStatement() != null) {
        return false;
      }
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }
    //
    final AST ast = covering.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    //
    IfStatement firstIfStatement = (IfStatement) coveredNodes.get(0);
    IfStatement firstNewIfStatement = null;
    //
    IfStatement prevIfStatement = null;
    for (Iterator<ASTNode> iter = coveredNodes.iterator(); iter.hasNext(); ) {
      IfStatement ifStatement = (IfStatement) iter.next();
      // prepare new 'if' statement
      IfStatement newIfStatement = ast.newIfStatement();
      newIfStatement.setExpression(
          (Expression) rewrite.createMoveTarget(ifStatement.getExpression()));
      // prepare 'then' statement and convert into block if needed
      Statement thenStatement =
          (Statement) rewrite.createMoveTarget(ifStatement.getThenStatement());
      if (ifStatement.getThenStatement() instanceof IfStatement) {
        IfStatement ifBodyStatement = (IfStatement) ifStatement.getThenStatement();
        if (ifBodyStatement.getElseStatement() == null) {
          Block thenBlock = ast.newBlock();
          thenBlock.statements().add(thenStatement);
          thenStatement = thenBlock;
        }
      }
      newIfStatement.setThenStatement(thenStatement);
      //
      if (prevIfStatement != null) {
        prevIfStatement.setElseStatement(newIfStatement);
        rewrite.remove(ifStatement, null);
      } else {
        firstNewIfStatement = newIfStatement;
      }
      prevIfStatement = newIfStatement;
    }
    rewrite.replace(firstIfStatement, firstNewIfStatement, null);
    // add correction proposal
    String label = CorrectionMessages.AdvancedQuickAssistProcessor_joinIfSequence;
    Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.JOIN_IF_SEQUENCE,
            image);
    resultingCollections.add(proposal);
    return true;
  }

  private static boolean getConvertSwitchToIfProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections) {
    if (!(covering instanceof SwitchStatement)) {
      return false;
    }
    //  we could produce quick assist (if all 'case' statements end with 'break')
    if (resultingCollections == null) {
      return true;
    }
    if (!getConvertSwitchToIfProposals(context, covering, resultingCollections, false))
      return false;
    return getConvertSwitchToIfProposals(context, covering, resultingCollections, true);
  }

  private static boolean getConvertSwitchToIfProposals(
      IInvocationContext context,
      ASTNode covering,
      Collection<ICommandAccess> resultingCollections,
      boolean preserveNPE) {
    final AST ast = covering.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    final ImportRewrite importRewrite = StubUtility.createImportRewrite(context.getASTRoot(), true);
    //
    SwitchStatement switchStatement = (SwitchStatement) covering;
    ITypeBinding expressionType = switchStatement.getExpression().resolveTypeBinding();
    boolean isStringsInSwitch =
        expressionType != null
            && "java.lang.String".equals(expressionType.getQualifiedName()); // $NON-NLS-1$

    if (!isStringsInSwitch && preserveNPE) return false;

    IfStatement firstIfStatement = null;
    IfStatement currentIfStatement = null;
    Block currentBlock = null;
    boolean hasStopAsLastExecutableStatement = false;
    Block defaultBlock = null;
    Expression currentCondition = null;
    boolean defaultFound = false;

    ArrayList<Block> allBlocks = new ArrayList<Block>();
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(
            ASTResolving.findParentBodyDeclaration(covering), importRewrite);

    Expression switchExpression = switchStatement.getExpression();
    Name varName;
    VariableDeclarationStatement variableDeclarationStatement = null;
    if (switchExpression instanceof Name) {
      varName = (Name) switchExpression;
    } else {
      // Switch expression could have side effects, see bug 252040
      VariableDeclarationFragment variableDeclarationFragment =
          ast.newVariableDeclarationFragment();
      String[] varNames =
          StubUtility.getVariableNameSuggestions(
              NamingConventions.VK_LOCAL,
              context.getCompilationUnit().getJavaProject(),
              expressionType,
              switchExpression,
              null);
      varName = ast.newSimpleName(varNames[0]);
      variableDeclarationFragment.setName((SimpleName) varName);
      variableDeclarationFragment.setStructuralProperty(
          VariableDeclarationFragment.INITIALIZER_PROPERTY,
          rewrite.createCopyTarget(switchExpression));

      variableDeclarationStatement =
          ast.newVariableDeclarationStatement(variableDeclarationFragment);
      Type type = importRewrite.addImport(expressionType, ast, importRewriteContext);
      variableDeclarationStatement.setType(type);
    }

    for (Iterator<Statement> iter = switchStatement.statements().iterator(); iter.hasNext(); ) {
      Statement statement = iter.next();
      if (statement instanceof SwitchCase) {
        SwitchCase switchCase = (SwitchCase) statement;
        // special case: pass through
        if (currentBlock != null) {
          if (!hasStopAsLastExecutableStatement) {
            return false;
          }
          currentBlock = null;
        }

        if (defaultFound) {
          // This gets too complicated. We only support 'default' as last SwitchCase.
          return false;
        }
        if (switchCase.isDefault()) {
          defaultFound = true;
        }
        // prepare condition (is null for 'default')
        Expression switchCaseCondition =
            createSwitchCaseCondition(
                ast,
                rewrite,
                importRewrite,
                importRewriteContext,
                varName,
                switchCase,
                isStringsInSwitch,
                preserveNPE);
        if (currentCondition == null) {
          currentCondition = switchCaseCondition;
        } else {
          InfixExpression condition = ast.newInfixExpression();
          condition.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
          condition.setLeftOperand(currentCondition);
          if (switchCaseCondition == null) switchCaseCondition = ast.newBooleanLiteral(true);
          condition.setRightOperand(switchCaseCondition);
          currentCondition = condition;
        }
      } else {
        // ensure that current block exists as 'then' statement of 'if'
        if (currentBlock == null) {
          if (currentCondition != null) {
            IfStatement ifStatement;
            if (firstIfStatement == null) {
              firstIfStatement = ast.newIfStatement();
              ifStatement = firstIfStatement;
            } else {
              ifStatement = ast.newIfStatement();
              currentIfStatement.setElseStatement(ifStatement);
            }
            currentIfStatement = ifStatement;
            ifStatement.setExpression(currentCondition);
            currentCondition = null;
            currentBlock = ast.newBlock();
            ifStatement.setThenStatement(currentBlock);
            allBlocks.add(currentBlock);
          } else {
            // case for default:
            defaultBlock = ast.newBlock();
            currentBlock = defaultBlock;
            allBlocks.add(currentBlock);
            // delay adding of default block
          }
        }
        if (statement instanceof BreakStatement) {
          currentBlock = null;
        } else {
          // add current statement in current block

          hasStopAsLastExecutableStatement = hasStopAsLastExecutableStatement(statement);
          Statement copyStatement = copyStatementExceptBreak(ast, rewrite, statement);

          currentBlock.statements().add(copyStatement);
        }
      }
    }
    // check, may be we have delayed default block
    if (defaultBlock != null) {
      currentIfStatement.setElseStatement(defaultBlock);
    }
    // remove unnecessary blocks in blocks
    for (int i = 0; i < allBlocks.size(); i++) {
      Block block = allBlocks.get(i);
      List<Statement> statements = block.statements();
      if (statements.size() == 1 && statements.get(0) instanceof Block) {
        Block innerBlock = (Block) statements.remove(0);
        block.getParent().setStructuralProperty(block.getLocationInParent(), innerBlock);
      }
    }

    if (variableDeclarationStatement == null) {
      // replace 'switch' with single if-else-if statement
      rewrite.replace(switchStatement, firstIfStatement, null);
    } else {
      new StatementRewrite(rewrite, new ASTNode[] {switchStatement})
          .replace(new ASTNode[] {variableDeclarationStatement, firstIfStatement}, null);
    }

    // add correction proposal
    String source =
        ASTNodes.asString(switchExpression)
            .replaceAll("\r\n?|\n", " "); // $NON-NLS-1$ //$NON-NLS-2$
    String label =
        preserveNPE
            ? Messages.format(
                CorrectionMessages.AdvancedQuickAssistProcessor_convertSwitchToIf_preserveNPE,
                source)
            : CorrectionMessages.AdvancedQuickAssistProcessor_convertSwitchToIf;
    ASTRewriteCorrectionProposal proposal =
        new ASTRewriteCorrectionProposal(
            label,
            context.getCompilationUnit(),
            rewrite,
            IProposalRelevance.CONVERT_SWITCH_TO_IF_ELSE);
    proposal.setImportRewrite(importRewrite);
    resultingCollections.add(proposal);
    return true;
  }

  private static Expression createSwitchCaseCondition(
      AST ast,
      ASTRewrite rewrite,
      ImportRewrite importRewrite,
      ImportRewriteContext importRewriteContext,
      Name switchExpression,
      SwitchCase switchCase,
      boolean isStringsInSwitch,
      boolean preserveNPE) {
    Expression expression = switchCase.getExpression();
    if (expression == null) return null;

    if (isStringsInSwitch) {
      MethodInvocation methodInvocation = ast.newMethodInvocation();
      methodInvocation.setName(ast.newSimpleName("equals")); // $NON-NLS-1$
      if (preserveNPE) {
        methodInvocation.setExpression(
            (Expression)
                rewrite.createStringPlaceholder(
                    switchExpression.getFullyQualifiedName(), ASTNode.QUALIFIED_NAME));
        methodInvocation.arguments().add(rewrite.createCopyTarget(expression));
      } else {
        methodInvocation.setExpression((Expression) rewrite.createCopyTarget(expression));
        methodInvocation
            .arguments()
            .add(
                rewrite.createStringPlaceholder(
                    switchExpression.getFullyQualifiedName(), ASTNode.QUALIFIED_NAME));
      }
      return methodInvocation;
    } else {
      InfixExpression condition = ast.newInfixExpression();
      condition.setOperator(InfixExpression.Operator.EQUALS);
      condition.setLeftOperand(
          (Expression)
              rewrite.createStringPlaceholder(
                  switchExpression.getFullyQualifiedName(), ASTNode.QUALIFIED_NAME));

      Expression rightExpression = null;
      if (expression instanceof SimpleName
          && ((SimpleName) expression).resolveBinding() instanceof IVariableBinding) {
        IVariableBinding binding = (IVariableBinding) ((SimpleName) expression).resolveBinding();
        if (binding.isEnumConstant()) {
          String qualifiedName =
              importRewrite.addImport(binding.getDeclaringClass(), importRewriteContext)
                  + '.'
                  + binding.getName();
          rightExpression = ast.newName(qualifiedName);
        }
      }
      if (rightExpression == null) {
        rightExpression = (Expression) rewrite.createCopyTarget(expression);
      }
      condition.setRightOperand(rightExpression);
      return condition;
    }
  }

  private static boolean hasStopAsLastExecutableStatement(Statement lastStatement) {
    if (lastStatement instanceof ReturnStatement || lastStatement instanceof BreakStatement) {
      return true;
    }
    if (lastStatement instanceof Block) {
      Block block = (Block) lastStatement;
      lastStatement = (Statement) block.statements().get(block.statements().size() - 1);
      return hasStopAsLastExecutableStatement(lastStatement);
    }
    return false;
  }

  private static Statement copyStatementExceptBreak(AST ast, ASTRewrite rewrite, Statement source) {
    if (source instanceof Block) {
      Block block = (Block) source;
      Block newBlock = ast.newBlock();
      for (Iterator<Statement> iter = block.statements().iterator(); iter.hasNext(); ) {
        Statement statement = iter.next();
        if (statement instanceof BreakStatement) {
          continue;
        }
        newBlock.statements().add(copyStatementExceptBreak(ast, rewrite, statement));
      }
      return newBlock;
    }
    return (Statement) rewrite.createMoveTarget(source);
  }

  private static boolean getConvertIfElseToSwitchProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      ArrayList<ICommandAccess> resultingCollections) {
    if (!(coveringNode instanceof IfStatement)) {
      return false;
    }
    //  we could produce quick assist
    if (resultingCollections == null) {
      return true;
    }

    if (!getConvertIfElseToSwitchProposals(context, coveringNode, resultingCollections, true))
      return false;
    return getConvertIfElseToSwitchProposals(context, coveringNode, resultingCollections, false);
  }

  private static boolean getConvertIfElseToSwitchProposals(
      IInvocationContext context,
      ASTNode coveringNode,
      ArrayList<ICommandAccess> resultingCollections,
      boolean handleNullArg) {
    final AST ast = coveringNode.getAST();
    final ASTRewrite rewrite = ASTRewrite.create(ast);
    final ImportRewrite importRewrite = StubUtility.createImportRewrite(context.getASTRoot(), true);
    ImportRewriteContext importRewriteContext =
        new ContextSensitiveImportRewriteContext(
            ASTResolving.findParentBodyDeclaration(coveringNode), importRewrite);
    IfStatement ifStatement = (IfStatement) coveringNode;
    IfStatement currentIf = ifStatement;
    Statement currentStatement = ifStatement;
    Expression currentExpression = currentIf.getExpression();
    SwitchStatement switchStatement = ast.newSwitchStatement();
    Expression switchExpression = null;
    boolean executeDefaultOnNullExpression = false;
    Statement defaultStatement = null;

    while (currentStatement != null) {
      Expression expression = null;
      List<Expression> caseExpressions = new ArrayList<Expression>();
      if (currentIf != null) {
        while (currentExpression
            != null) { // loop for fall through cases - multiple expressions with || operator
          Expression leftOperand;
          Expression rightOperand;
          boolean isMethodInvocationCase = false;
          if (currentExpression instanceof MethodInvocation) {
            isMethodInvocationCase = true;
            if (!(((MethodInvocation) currentExpression).getName().getIdentifier())
                .equals("equals")) // $NON-NLS-1$
            return false;

            MethodInvocation invocation = (MethodInvocation) currentExpression;
            leftOperand = invocation.getExpression();
            if (leftOperand == null) return false;
            ITypeBinding leftBinding = leftOperand.resolveTypeBinding();
            if (leftBinding != null) {
              if (leftBinding.getQualifiedName().equals("java.lang.String")) { // $NON-NLS-1$
                if (!JavaModelUtil.is17OrHigher(context.getCompilationUnit().getJavaProject()))
                  return false;
              } else if (!leftBinding.isEnum()) {
                return false;
              }
            }

            List<Expression> arguments = invocation.arguments();
            if (arguments.size() != 1) return false;
            rightOperand = arguments.get(0);
            ITypeBinding rightBinding = leftOperand.resolveTypeBinding();
            if (rightBinding != null) {
              if (rightBinding.getQualifiedName().equals("java.lang.String")) { // $NON-NLS-1$
                if (!JavaModelUtil.is17OrHigher(context.getCompilationUnit().getJavaProject()))
                  return false;
              } else if (!rightBinding.isEnum()) {
                return false;
              }
            }

          } else if (currentExpression instanceof InfixExpression) {
            InfixExpression infixExpression = (InfixExpression) currentExpression;
            Operator operator = infixExpression.getOperator();
            if (!(operator.equals(InfixExpression.Operator.CONDITIONAL_OR)
                || operator.equals(InfixExpression.Operator.EQUALS))) return false;

            leftOperand = infixExpression.getLeftOperand();
            rightOperand = infixExpression.getRightOperand();

            if (operator.equals(InfixExpression.Operator.EQUALS)) {
              ITypeBinding typeBinding = leftOperand.resolveTypeBinding();
              if (typeBinding != null
                  && typeBinding.getQualifiedName().equals("java.lang.String")) { // $NON-NLS-1$
                return false; // don't propose quick assist when == is used to compare strings,
                // since switch will use equals()
              }
            } else if (operator.equals(InfixExpression.Operator.CONDITIONAL_OR)) {
              currentExpression = leftOperand;
              continue;
            }
          } else {
            return false;
          }

          if (leftOperand.resolveConstantExpressionValue() != null) {
            caseExpressions.add(leftOperand);
            expression = rightOperand;
            executeDefaultOnNullExpression |= isMethodInvocationCase;
          } else if (rightOperand.resolveConstantExpressionValue() != null) {
            caseExpressions.add(rightOperand);
            expression = leftOperand;
          } else if (leftOperand instanceof QualifiedName) {
            QualifiedName qualifiedName = (QualifiedName) leftOperand;
            IVariableBinding binding = (IVariableBinding) qualifiedName.resolveBinding();
            if (binding == null || !binding.isEnumConstant()) return false;
            importRewrite.addImport(binding.getDeclaringClass(), importRewriteContext);
            caseExpressions.add(qualifiedName.getName());
            expression = rightOperand;
            executeDefaultOnNullExpression |= isMethodInvocationCase;
          } else if (rightOperand instanceof QualifiedName) {
            QualifiedName qualifiedName = (QualifiedName) rightOperand;
            IVariableBinding binding = (IVariableBinding) qualifiedName.resolveBinding();
            if (binding == null || !binding.isEnumConstant()) return false;
            importRewrite.addImport(binding.getDeclaringClass(), importRewriteContext);
            caseExpressions.add(qualifiedName.getName());
            expression = leftOperand;
          } else {
            return false;
          }
          if (expression == null) { // paranoidal check: this condition should never be true
            return false;
          }

          if (currentExpression.getParent() instanceof InfixExpression) {
            currentExpression = getNextSiblingExpression(currentExpression);
          } else {
            currentExpression = null;
          }

          if (switchExpression == null) {
            switchExpression = expression;
          }

          if (!switchExpression.subtreeMatch(new ASTMatcher(), expression)) {
            return false;
          }
        }
      }

      Statement thenStatement;
      if (currentIf == null) {
        thenStatement = currentStatement; // currentStatement has the default else block
        defaultStatement = currentStatement;
      } else {
        thenStatement = currentIf.getThenStatement();
      }

      SwitchCase[] switchCaseStatements = createSwitchCaseStatements(ast, rewrite, caseExpressions);
      for (int i = 0; i < switchCaseStatements.length; i++) {
        switchStatement.statements().add(switchCaseStatements[i]);
      }
      boolean isBreakRequired = true;
      if (thenStatement instanceof Block) {
        Statement statement = null;
        for (Iterator<Statement> iter = ((Block) thenStatement).statements().iterator();
            iter.hasNext(); ) {
          statement = iter.next();
          switchStatement.statements().add(rewrite.createCopyTarget(statement));
        }
        if (statement instanceof ReturnStatement || statement instanceof ThrowStatement)
          isBreakRequired = false;
      } else {
        if (thenStatement instanceof ReturnStatement || thenStatement instanceof ThrowStatement)
          isBreakRequired = false;
        switchStatement.statements().add(rewrite.createCopyTarget(thenStatement));
      }
      if (isBreakRequired) switchStatement.statements().add(ast.newBreakStatement());

      // advance currentStatement to the next "else if" or "else":
      if (currentIf != null && currentIf.getElseStatement() != null) {
        Statement elseStatement = currentIf.getElseStatement();
        if (elseStatement instanceof IfStatement) {
          currentIf = (IfStatement) elseStatement;
          currentStatement = currentIf;
          currentExpression = currentIf.getExpression();
        } else {
          currentIf = null;
          currentStatement = elseStatement;
          currentExpression = null;
        }
      } else {
        currentStatement = null;
      }
    }

    if (switchExpression == null) return false;
    switchStatement.setExpression((Expression) rewrite.createCopyTarget(switchExpression));

    if (handleNullArg) {
      if (executeDefaultOnNullExpression) {
        IfStatement newIfStatement = ast.newIfStatement();

        InfixExpression infixExpression = ast.newInfixExpression();
        infixExpression.setLeftOperand((Expression) rewrite.createCopyTarget(switchExpression));
        infixExpression.setRightOperand(ast.newNullLiteral());
        infixExpression.setOperator(InfixExpression.Operator.EQUALS);
        newIfStatement.setExpression(infixExpression);

        if (defaultStatement == null) {
          Block block = ast.newBlock();
          newIfStatement.setThenStatement(block);
        } else if (defaultStatement instanceof Block) {
          Block block = ast.newBlock();
          for (Iterator<Statement> iter = ((Block) defaultStatement).statements().iterator();
              iter.hasNext(); ) {
            block.statements().add(rewrite.createCopyTarget(iter.next()));
          }
          newIfStatement.setThenStatement(block);
        } else {
          newIfStatement.setThenStatement((Statement) rewrite.createCopyTarget(defaultStatement));
        }
        Block block = ast.newBlock();
        block.statements().add(switchStatement);
        newIfStatement.setElseStatement(block);

        rewrite.replace(ifStatement, newIfStatement, null);

        String source =
            ASTNodes.asString(switchExpression)
                .replaceAll("\r\n?|\n", " "); // $NON-NLS-1$ //$NON-NLS-2$
        String label =
            Messages.format(
                CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch_handleNullArg,
                source);
        ASTRewriteCorrectionProposal proposal =
            new ASTRewriteCorrectionProposal(
                label,
                context.getCompilationUnit(),
                rewrite,
                IProposalRelevance.CONVERT_IF_ELSE_TO_SWITCH);
        proposal.setImportRewrite(importRewrite);
        resultingCollections.add(proposal);
      }
    } else {
      rewrite.replace(ifStatement, switchStatement, null);

      String label = CorrectionMessages.AdvancedQuickAssistProcessor_convertIfElseToSwitch;
      ASTRewriteCorrectionProposal proposal =
          new ASTRewriteCorrectionProposal(
              label,
              context.getCompilationUnit(),
              rewrite,
              IProposalRelevance.CONVERT_IF_ELSE_TO_SWITCH);
      proposal.setImportRewrite(importRewrite);
      resultingCollections.add(proposal);
    }
    return true;
  }

  private static Expression getNextSiblingExpression(Expression expression) {
    InfixExpression parentInfixExpression = (InfixExpression) expression.getParent();
    Expression sibiling;
    if (expression.equals(parentInfixExpression.getLeftOperand())) {
      sibiling = parentInfixExpression.getRightOperand();
    } else if (expression.equals(parentInfixExpression.getRightOperand())) {
      if (parentInfixExpression.getParent() instanceof InfixExpression)
        sibiling = getNextSiblingExpression(parentInfixExpression);
      else sibiling = null;
    } else {
      sibiling = null;
    }
    return sibiling;
  }

  private static SwitchCase[] createSwitchCaseStatements(
      AST ast, ASTRewrite rewrite, List<Expression> caseExpressions) {
    int len = (caseExpressions.size() == 0) ? 1 : caseExpressions.size();
    SwitchCase[] switchCaseStatements = new SwitchCase[len];
    if (caseExpressions.size() == 0) {
      switchCaseStatements[0] = ast.newSwitchCase();
      switchCaseStatements[0].setExpression(null);
    } else {
      for (int i = 0; i < caseExpressions.size(); i++) {
        ASTNode astNode = caseExpressions.get(i);
        switchCaseStatements[i] = ast.newSwitchCase();
        switchCaseStatements[i].setExpression((Expression) rewrite.createCopyTarget(astNode));
      }
    }
    return switchCaseStatements;
  }
}

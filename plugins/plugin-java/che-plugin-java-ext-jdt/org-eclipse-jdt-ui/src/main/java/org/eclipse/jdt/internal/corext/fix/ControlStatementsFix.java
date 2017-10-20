/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEditGroup;

public class ControlStatementsFix extends CompilationUnitRewriteOperationsFix {

  private static final class ControlStatementFinder extends GenericVisitor {

    private final List<CompilationUnitRewriteOperation> fResult;
    private final boolean fFindControlStatementsWithoutBlock;
    private final boolean fRemoveUnnecessaryBlocks;
    private final boolean fRemoveUnnecessaryBlocksOnlyWhenReturnOrThrow;

    public ControlStatementFinder(
        boolean findControlStatementsWithoutBlock,
        boolean removeUnnecessaryBlocks,
        boolean removeUnnecessaryBlocksOnlyWhenReturnOrThrow,
        List<CompilationUnitRewriteOperation> resultingCollection) {

      fFindControlStatementsWithoutBlock = findControlStatementsWithoutBlock;
      fRemoveUnnecessaryBlocks = removeUnnecessaryBlocks;
      fRemoveUnnecessaryBlocksOnlyWhenReturnOrThrow = removeUnnecessaryBlocksOnlyWhenReturnOrThrow;
      fResult = resultingCollection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
     */
    @Override
    public boolean visit(DoStatement node) {
      handle(node.getBody(), DoStatement.BODY_PROPERTY);

      return super.visit(node);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
     */
    @Override
    public boolean visit(ForStatement node) {
      handle(node.getBody(), ForStatement.BODY_PROPERTY);

      return super.visit(node);
    }

    /** {@inheritDoc} */
    @Override
    public boolean visit(EnhancedForStatement node) {
      handle(node.getBody(), EnhancedForStatement.BODY_PROPERTY);

      return super.visit(node);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
     */
    @Override
    public boolean visit(IfStatement statement) {
      handle(statement.getThenStatement(), IfStatement.THEN_STATEMENT_PROPERTY);

      Statement elseStatement = statement.getElseStatement();
      if (elseStatement != null && !(elseStatement instanceof IfStatement)) {
        handle(elseStatement, IfStatement.ELSE_STATEMENT_PROPERTY);
      }

      return super.visit(statement);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.corext.dom.GenericVisitor#visit(org.eclipse.jdt.core.dom.WhileStatement)
     */
    @Override
    public boolean visit(WhileStatement node) {
      handle(node.getBody(), WhileStatement.BODY_PROPERTY);

      return super.visit(node);
    }

    private void handle(Statement body, ChildPropertyDescriptor bodyProperty) {
      if ((body.getFlags() & ASTNode.RECOVERED) != 0) return;
      Statement parent = (Statement) body.getParent();
      if ((parent.getFlags() & ASTNode.RECOVERED) != 0) return;

      if (fRemoveUnnecessaryBlocksOnlyWhenReturnOrThrow) {
        if (!(body instanceof Block)) {
          if (body.getNodeType() != ASTNode.IF_STATEMENT
              && body.getNodeType() != ASTNode.RETURN_STATEMENT
              && body.getNodeType() != ASTNode.THROW_STATEMENT) {
            fResult.add(new AddBlockOperation(bodyProperty, body, parent));
          }
        } else {
          if (RemoveBlockOperation.satisfiesCleanUpPrecondition(parent, bodyProperty, true)) {
            fResult.add(new RemoveBlockOperation(parent, bodyProperty));
          }
        }
      } else if (fFindControlStatementsWithoutBlock) {
        if (!(body instanceof Block)) {
          fResult.add(new AddBlockOperation(bodyProperty, body, parent));
        }
      } else if (fRemoveUnnecessaryBlocks) {
        if (RemoveBlockOperation.satisfiesCleanUpPrecondition(parent, bodyProperty, false)) {
          fResult.add(new RemoveBlockOperation(parent, bodyProperty));
        }
      }
    }
  }

  private static class IfElseIterator {

    private IfStatement fCursor;

    public IfElseIterator(IfStatement item) {
      fCursor = findStart(item);
    }

    public IfStatement next() {
      if (!hasNext()) return null;

      IfStatement result = fCursor;

      if (fCursor.getElseStatement() instanceof IfStatement) {
        fCursor = (IfStatement) fCursor.getElseStatement();
      } else {
        fCursor = null;
      }

      return result;
    }

    public boolean hasNext() {
      return fCursor != null;
    }

    private IfStatement findStart(IfStatement item) {
      while (item.getLocationInParent() == IfStatement.ELSE_STATEMENT_PROPERTY) {
        item = (IfStatement) item.getParent();
      }
      return item;
    }
  }

  private static final class AddBlockOperation extends CompilationUnitRewriteOperation {

    private final ChildPropertyDescriptor fBodyProperty;
    private final Statement fBody;
    private final Statement fControlStatement;

    public AddBlockOperation(
        ChildPropertyDescriptor bodyProperty, Statement body, Statement controlStatement) {
      fBodyProperty = bodyProperty;
      fBody = body;
      fControlStatement = controlStatement;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {
      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      String label;
      if (fBodyProperty == IfStatement.THEN_STATEMENT_PROPERTY) {
        label = FixMessages.CodeStyleFix_ChangeIfToBlock_desription;
      } else if (fBodyProperty == IfStatement.ELSE_STATEMENT_PROPERTY) {
        label = FixMessages.CodeStyleFix_ChangeElseToBlock_description;
      } else {
        label = FixMessages.CodeStyleFix_ChangeControlToBlock_description;
      }

      TextEditGroup group = createTextEditGroup(label, cuRewrite);
      ASTNode moveTarget = rewrite.createMoveTarget(fBody);
      Block replacingBody = cuRewrite.getRoot().getAST().newBlock();
      replacingBody.statements().add(moveTarget);
      rewrite.set(fControlStatement, fBodyProperty, replacingBody, group);
    }
  }

  static class RemoveBlockOperation extends CompilationUnitRewriteOperation {

    private final Statement fStatement;
    private final ChildPropertyDescriptor fChild;

    public RemoveBlockOperation(Statement controlStatement, ChildPropertyDescriptor child) {
      fStatement = controlStatement;
      fChild = child;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {
      ASTRewrite rewrite = cuRewrite.getASTRewrite();

      Block block = (Block) fStatement.getStructuralProperty(fChild);
      Statement statement = (Statement) block.statements().get(0);
      Statement moveTarget = (Statement) rewrite.createMoveTarget(statement);

      TextEditGroup group =
          createTextEditGroup(
              FixMessages.ControlStatementsFix_removeBrackets_proposalDescription, cuRewrite);
      rewrite.set(fStatement, fChild, moveTarget, group);
    }

    public static boolean satisfiesCleanUpPrecondition(
        Statement controlStatement,
        ChildPropertyDescriptor childDescriptor,
        boolean onlyReturnAndThrows) {
      return satisfiesPrecondition(controlStatement, childDescriptor, onlyReturnAndThrows, true);
    }

    public static boolean satisfiesQuickAssistPrecondition(
        Statement controlStatement, ChildPropertyDescriptor childDescriptor) {
      return satisfiesPrecondition(controlStatement, childDescriptor, false, false);
    }

    // Can the block around child with childDescriptor of controlStatement be removed?
    private static boolean satisfiesPrecondition(
        Statement controlStatement,
        ChildPropertyDescriptor childDescriptor,
        boolean onlyReturnAndThrows,
        boolean cleanUpCheck) {
      Object child = controlStatement.getStructuralProperty(childDescriptor);

      if (!(child instanceof Block)) return false;

      Block block = (Block) child;
      List<Statement> list = block.statements();
      if (list.size() != 1) return false;

      ASTNode singleStatement = list.get(0);

      if (onlyReturnAndThrows)
        if (!(singleStatement instanceof ReturnStatement)
            && !(singleStatement instanceof ThrowStatement)) return false;

      if (controlStatement instanceof IfStatement) {
        // if (true) {
        //  while (true)
        // 	 if (false)
        //    ;
        // } else
        //   ;

        if (((IfStatement) controlStatement).getThenStatement() != child)
          return true; // can always remove blocks in else part

        IfStatement ifStatement = (IfStatement) controlStatement;
        if (ifStatement.getElseStatement() == null)
          return true; // can always remove if no else part

        return !hasUnblockedIf((Statement) singleStatement, onlyReturnAndThrows, cleanUpCheck);
      } else {
        // if (true)
        // while (true) {
        //  if (false)
        //   ;
        // }
        // else
        // ;
        if (!hasUnblockedIf((Statement) singleStatement, onlyReturnAndThrows, cleanUpCheck))
          return true;

        ASTNode currentChild = controlStatement;
        ASTNode parent = currentChild.getParent();
        while (true) {
          Statement body = null;
          if (parent instanceof IfStatement) {
            body = ((IfStatement) parent).getThenStatement();
            if (body == currentChild
                && ((IfStatement) parent).getElseStatement()
                    != null) // ->currentChild is an unblocked then part
            return false;
          } else if (parent instanceof WhileStatement) {
            body = ((WhileStatement) parent).getBody();
          } else if (parent instanceof DoStatement) {
            body = ((DoStatement) parent).getBody();
          } else if (parent instanceof ForStatement) {
            body = ((ForStatement) parent).getBody();
          } else if (parent instanceof EnhancedForStatement) {
            body = ((EnhancedForStatement) parent).getBody();
          } else {
            return true;
          }
          if (body != currentChild) // ->parents child is a block
          return true;

          currentChild = parent;
          parent = currentChild.getParent();
        }
      }
    }

    private static boolean hasUnblockedIf(
        Statement p, boolean onlyReturnAndThrows, boolean cleanUpCheck) {
      while (true) {
        if (p instanceof IfStatement) {
          return true;
        } else {

          ChildPropertyDescriptor childD = null;
          if (p instanceof WhileStatement) {
            childD = WhileStatement.BODY_PROPERTY;
          } else if (p instanceof ForStatement) {
            childD = ForStatement.BODY_PROPERTY;
          } else if (p instanceof EnhancedForStatement) {
            childD = EnhancedForStatement.BODY_PROPERTY;
          } else if (p instanceof DoStatement) {
            childD = DoStatement.BODY_PROPERTY;
          } else {
            return false;
          }
          Statement body = (Statement) p.getStructuralProperty(childD);
          if (body instanceof Block) {
            if (!cleanUpCheck) {
              return false;
            } else {
              if (!satisfiesPrecondition(p, childD, onlyReturnAndThrows, cleanUpCheck))
                return false;

              p = (Statement) ((Block) body).statements().get(0);
            }
          } else {
            p = body;
          }
        }
      }
    }
  }

  public static ControlStatementsFix[] createRemoveBlockFix(
      CompilationUnit compilationUnit, ASTNode node) {
    if (!(node instanceof Statement)) {
      return null;
    }
    Statement statement = (Statement) node;

    if (statement instanceof Block) {
      Block block = (Block) statement;
      if (block.statements().size() != 1) return null;

      ASTNode parent = block.getParent();
      if (!(parent instanceof Statement)) return null;

      statement = (Statement) parent;
    }

    if (statement instanceof IfStatement) {
      List<ControlStatementsFix> result = new ArrayList<ControlStatementsFix>();

      List<RemoveBlockOperation> removeAllList = new ArrayList<RemoveBlockOperation>();

      IfElseIterator iter = new IfElseIterator((IfStatement) statement);
      IfStatement item = null;
      while (iter.hasNext()) {
        item = iter.next();
        if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
            item, IfStatement.THEN_STATEMENT_PROPERTY)) {
          RemoveBlockOperation op =
              new RemoveBlockOperation(item, IfStatement.THEN_STATEMENT_PROPERTY);
          removeAllList.add(op);
          if (item == statement)
            result.add(
                new ControlStatementsFix(
                    FixMessages.ControlStatementsFix_removeIfBlock_proposalDescription,
                    compilationUnit,
                    new CompilationUnitRewriteOperation[] {op}));
        }
      }

      if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
          item, IfStatement.ELSE_STATEMENT_PROPERTY)) {
        RemoveBlockOperation op =
            new RemoveBlockOperation(item, IfStatement.ELSE_STATEMENT_PROPERTY);
        removeAllList.add(op);
        if (item == statement)
          result.add(
              new ControlStatementsFix(
                  FixMessages.ControlStatementsFix_removeElseBlock_proposalDescription,
                  compilationUnit,
                  new CompilationUnitRewriteOperation[] {op}));
      }

      if (removeAllList.size() > 1) {
        CompilationUnitRewriteOperation[] allConvert =
            removeAllList.toArray(new CompilationUnitRewriteOperation[removeAllList.size()]);
        result.add(
            new ControlStatementsFix(
                FixMessages.ControlStatementsFix_removeIfElseBlock_proposalDescription,
                compilationUnit,
                allConvert));
      }

      return result.toArray(new ControlStatementsFix[result.size()]);
    } else if (statement instanceof WhileStatement) {
      if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
          statement, WhileStatement.BODY_PROPERTY)) {
        RemoveBlockOperation op = new RemoveBlockOperation(statement, WhileStatement.BODY_PROPERTY);
        return new ControlStatementsFix[] {
          new ControlStatementsFix(
              FixMessages.ControlStatementsFix_removeBrackets_proposalDescription,
              compilationUnit,
              new CompilationUnitRewriteOperation[] {op})
        };
      }
    } else if (statement instanceof ForStatement) {
      if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
          statement, ForStatement.BODY_PROPERTY)) {
        RemoveBlockOperation op = new RemoveBlockOperation(statement, ForStatement.BODY_PROPERTY);
        return new ControlStatementsFix[] {
          new ControlStatementsFix(
              FixMessages.ControlStatementsFix_removeBrackets_proposalDescription,
              compilationUnit,
              new CompilationUnitRewriteOperation[] {op})
        };
      }
    } else if (statement instanceof EnhancedForStatement) {
      if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
          statement, EnhancedForStatement.BODY_PROPERTY)) {
        RemoveBlockOperation op =
            new RemoveBlockOperation(statement, EnhancedForStatement.BODY_PROPERTY);
        return new ControlStatementsFix[] {
          new ControlStatementsFix(
              FixMessages.ControlStatementsFix_removeBrackets_proposalDescription,
              compilationUnit,
              new CompilationUnitRewriteOperation[] {op})
        };
      }
    } else if (statement instanceof DoStatement) {
      if (RemoveBlockOperation.satisfiesQuickAssistPrecondition(
          statement, DoStatement.BODY_PROPERTY)) {
        RemoveBlockOperation op = new RemoveBlockOperation(statement, DoStatement.BODY_PROPERTY);
        return new ControlStatementsFix[] {
          new ControlStatementsFix(
              FixMessages.ControlStatementsFix_removeBrackets_proposalDescription,
              compilationUnit,
              new CompilationUnitRewriteOperation[] {op})
        };
      }
    }

    return null;
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      boolean convertSingleStatementToBlock,
      boolean removeUnnecessaryBlock,
      boolean removeUnnecessaryBlockContainingReturnOrThrow) {

    if (!convertSingleStatementToBlock
        && !removeUnnecessaryBlock
        && !removeUnnecessaryBlockContainingReturnOrThrow) return null;

    List<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();
    ControlStatementFinder finder =
        new ControlStatementFinder(
            convertSingleStatementToBlock,
            removeUnnecessaryBlock,
            removeUnnecessaryBlockContainingReturnOrThrow,
            operations);
    compilationUnit.accept(finder);

    if (operations.isEmpty()) return null;

    CompilationUnitRewriteOperation[] ops =
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
    return new ControlStatementsFix(
        FixMessages.ControlStatementsFix_change_name, compilationUnit, ops);
  }

  protected ControlStatementsFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations) {
    super(name, compilationUnit, fixRewriteOperations);
  }
}

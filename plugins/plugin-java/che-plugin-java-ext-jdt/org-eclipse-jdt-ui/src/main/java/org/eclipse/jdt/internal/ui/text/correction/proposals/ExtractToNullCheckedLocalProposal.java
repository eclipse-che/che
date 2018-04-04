/**
 * ***************************************************************************** Copyright (c) 2012,
 * 2013 GK Software AG and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Stephan Herrmann - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Fix for field related null-issues:
 *
 * <ol>
 *   <li>{@link IProblem#NullableFieldReference}
 *   <li>{@link IProblem#RequiredNonNullButProvidedSpecdNullable} <em>if relating to a field</em>
 *   <li>{@link IProblem#RequiredNonNullButProvidedUnknown} <em>if relating to a field</em>
 * </ol>
 *
 * Extract the field reference to a fresh local variable. Add a null check for that local variable
 * and move the dereference into the then-block of this null-check:
 *
 * <pre>
 * {@code @Nullable Exception e;}
 * void test() {
 *     e.printStackTrace();
 * }</pre>
 *
 * will be converted to:
 *
 * <pre>
 * {@code @Nullable Exception e;}
 * void test() {
 *     final Exception e2 = e;
 *     if (e2 != null) {
 *         e2.printStackTrace();
 *     } else {
 *         // TODO handle null value
 *     }
 * }</pre>
 *
 * <p>The <code>final</code> keyword is added to remind the user that writing to the local variable
 * has no effect on the original field.
 *
 * <p>Rrespects scoping if the problem occurs inside the initialization of a local variable (by
 * moving statements into the new then block).
 *
 * @since 3.9
 */
public class ExtractToNullCheckedLocalProposal extends LinkedCorrectionProposal {

  private static final String LOCAL_NAME_POSITION_GROUP = "localName"; // $NON-NLS-1$

  /** Protocol for rearranging the bits and pieces into a new code structure. */
  private abstract static class RearrangeStrategy {

    final Statement origStmt;
    final Block block;
    final TextEditGroup group;

    RearrangeStrategy(Statement origStmt, Block block, TextEditGroup group) {
      this.origStmt = origStmt;
      this.block = block;
      this.group = group;
    }

    /**
     * Step 1 of the protocol: insert the new local variable.
     *
     * @param localDecl new local variable initialized with the original expression
     */
    public abstract void insertLocalDecl(VariableDeclarationStatement localDecl);

    /**
     * Step 2 of the protocol: create a move target for repositioning the original enclosing
     * statement.
     *
     * @return a move target representing the original statement
     */
    public abstract Statement createMoveTargetForOrigStmt();

    /**
     * Step 3 of the protocol: integrate the new if statement into the existing structure.
     *
     * @param ifStmt the new if statement, fully created
     * @param thenBlock the then statement of the given if statement (is a block by construction)
     */
    public abstract void insertIfStatement(IfStatement ifStmt, Block thenBlock);

    public static RearrangeStrategy create(
        Statement origStmt, ASTRewrite rewrite, TextEditGroup group) {
      ASTNode parent = origStmt.getParent();
      if (parent instanceof Block) {
        Block block = (Block) parent;
        if (origStmt instanceof VariableDeclarationStatement)
          return new ModifyBlockWithLocalDecl(origStmt, block, rewrite, group);
        else return new ModifyBlock(origStmt, block, rewrite, group);
      } else {
        return new ReplaceStatement(origStmt, rewrite, group);
      }
    }

    /** Strategy implementation for modifying statement list of the parent block. */
    private static class ModifyBlock extends RearrangeStrategy {

      final ListRewrite blockRewrite;

      ModifyBlock(
          Statement origStmt, Block enclosingBlock, ASTRewrite rewrite, TextEditGroup group) {
        super(origStmt, enclosingBlock, group);
        // we're going to modify this block, create the rewrite for this task:
        this.blockRewrite = rewrite.getListRewrite(enclosingBlock, Block.STATEMENTS_PROPERTY);
      }

      @Override
      public void insertLocalDecl(VariableDeclarationStatement localDecl) {
        this.blockRewrite.insertBefore(localDecl, this.origStmt, this.group);
      }

      @Override
      public Statement createMoveTargetForOrigStmt() {
        return (Statement)
            this.blockRewrite.createMoveTarget(this.origStmt, this.origStmt, null, this.group);
      }

      @Override
      public void insertIfStatement(IfStatement ifStmt, Block thenBlock) {
        // inside a block replace old statement with wrapping if-statement
        this.blockRewrite.replace(this.origStmt, ifStmt, this.group);
      }
    }

    /** Variant that also respects scoping of an existing local variable declaration. */
    private static class ModifyBlockWithLocalDecl extends ModifyBlock {
      ModifyBlockWithLocalDecl(
          Statement origStmt, Block enclosingBlock, ASTRewrite rewrite, TextEditGroup group) {
        super(origStmt, enclosingBlock, rewrite, group);
      }

      @Override
      public void insertIfStatement(IfStatement ifStmt, Block thenBlock) {
        // when stmt declares a local variable (see RearrangeStrategy.create(..)) we need to move
        // all
        // subsequent statements into the then-block to ensure that the existing declared local is
        // visible:
        List<ASTNode> blockStmts = this.block.statements();
        int stmtIdx = blockStmts.indexOf(this.origStmt);
        int lastIdx = blockStmts.size() - 1;
        if (stmtIdx != -1 && stmtIdx < lastIdx) {
          thenBlock
              .statements()
              .add(
                  this.blockRewrite.createMoveTarget(
                      blockStmts.get(stmtIdx + 1), blockStmts.get(lastIdx), null, this.group));
        }
        super.insertIfStatement(ifStmt, thenBlock);
      }
    }

    /** Strategy implementation for replacing a single statement with a new block. */
    private static class ReplaceStatement extends RearrangeStrategy {

      final ASTRewrite rewrite;

      ReplaceStatement(Statement origStmt, ASTRewrite rewrite, TextEditGroup group) {
        // did not have a block, create one now to hold new statements:
        super(origStmt, rewrite.getAST().newBlock(), group);
        this.rewrite = rewrite;
      }

      @Override
      public void insertLocalDecl(VariableDeclarationStatement localDecl) {
        this.block.statements().add(localDecl);
      }

      @Override
      public Statement createMoveTargetForOrigStmt() {
        return (Statement) this.rewrite.createMoveTarget(this.origStmt); // group is unused
      }

      @Override
      public void insertIfStatement(IfStatement ifStmt, Block thenBlock) {
        // did not have a block: add if-statement to new block
        this.block.statements().add(ifStmt);
        // and replace the single statement with this block
        this.rewrite.replace(this.origStmt, this.block, this.group);
      }
    }
  }

  private SimpleName fieldReference;
  private CompilationUnit compilationUnit;
  private ASTNode enclosingMethod; // MethodDeclaration or Initializer

  public ExtractToNullCheckedLocalProposal(
      ICompilationUnit cu,
      CompilationUnit compilationUnit,
      SimpleName fieldReference,
      ASTNode enclosingMethod) {
    super(
        FixMessages.ExtractToNullCheckedLocalProposal_extractToCheckedLocal_proposalName,
        cu,
        null,
        100,
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
    this.compilationUnit = compilationUnit;
    this.fieldReference = fieldReference;
    this.enclosingMethod = enclosingMethod;
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {

    // infrastructure:
    AST ast = this.compilationUnit.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ImportRewrite imports = ImportRewrite.create(this.compilationUnit, true);
    TextEditGroup group =
        new TextEditGroup(
            FixMessages.ExtractToNullCheckedLocalProposal_extractCheckedLocal_editName);
    LinkedProposalPositionGroup localNameGroup =
        new LinkedProposalPositionGroup(LOCAL_NAME_POSITION_GROUP);
    getLinkedProposalModel().addPositionGroup(localNameGroup);

    // AST context:
    Statement origStmt = (Statement) ASTNodes.getParent(this.fieldReference, Statement.class);
    // determine suitable strategy for rearranging elements towards a new code structure:
    RearrangeStrategy rearrangeStrategy = RearrangeStrategy.create(origStmt, rewrite, group);

    Expression toReplace;
    ASTNode directParent = this.fieldReference.getParent();
    if (directParent instanceof FieldAccess) {
      toReplace = (Expression) directParent;
    } else if (directParent instanceof QualifiedName
        && this.fieldReference.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
      toReplace = (Expression) directParent;
    } else {
      toReplace = this.fieldReference;
    }

    // new local declaration initialized from the field reference
    VariableDeclarationFragment localFrag = ast.newVariableDeclarationFragment();
    VariableDeclarationStatement localDecl = ast.newVariableDeclarationStatement(localFrag);
    // ... type
    localDecl.setType(newType(toReplace.resolveTypeBinding(), ast, imports));
    localDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
    // ... name
    String localName =
        proposeLocalName(
            this.fieldReference, this.compilationUnit, getCompilationUnit().getJavaProject());
    localFrag.setName(ast.newSimpleName(localName));
    // ... initialization
    localFrag.setInitializer((Expression) ASTNode.copySubtree(ast, toReplace));

    rearrangeStrategy.insertLocalDecl(localDecl);

    // if statement:
    IfStatement ifStmt = ast.newIfStatement();

    // condition:
    InfixExpression nullCheck = ast.newInfixExpression();
    nullCheck.setLeftOperand(ast.newSimpleName(localName));
    nullCheck.setRightOperand(ast.newNullLiteral());
    nullCheck.setOperator(InfixExpression.Operator.NOT_EQUALS);
    ifStmt.setExpression(nullCheck);

    // then block: the original statement
    Block thenBlock = ast.newBlock();
    thenBlock.statements().add(rearrangeStrategy.createMoveTargetForOrigStmt());
    ifStmt.setThenStatement(thenBlock);
    // ... but with the field reference replaced by the new local:
    SimpleName dereferencedName = ast.newSimpleName(localName);
    rewrite.replace(toReplace, dereferencedName, group);

    // else block: a Todo comment
    Block elseBlock = ast.newBlock();
    String elseStatement =
        "// TODO "
            + FixMessages
                .ExtractToNullCheckedLocalProposal_todoHandleNullDescription; // $NON-NLS-1$
    if (origStmt instanceof ReturnStatement) {
      Type returnType =
          newType(((ReturnStatement) origStmt).getExpression().resolveTypeBinding(), ast, imports);
      ReturnStatement returnStatement = ast.newReturnStatement();
      returnStatement.setExpression(ASTNodeFactory.newDefaultExpression(ast, returnType, 0));
      elseStatement +=
          '\n'
              + ASTNodes.asFormattedString(
                  returnStatement,
                  0,
                  String.valueOf('\n'),
                  getCompilationUnit().getJavaProject().getOptions(true));
    }

    EmptyStatement todoNode =
        (EmptyStatement) rewrite.createStringPlaceholder(elseStatement, ASTNode.EMPTY_STATEMENT);
    elseBlock.statements().add(todoNode);
    ifStmt.setElseStatement(elseBlock);

    // link all three occurrences of the new local variable:
    addLinkedPosition(
        rewrite.track(localFrag.getName()), true /*first*/, LOCAL_NAME_POSITION_GROUP);
    addLinkedPosition(rewrite.track(nullCheck.getLeftOperand()), false, LOCAL_NAME_POSITION_GROUP);
    addLinkedPosition(rewrite.track(dereferencedName), false, LOCAL_NAME_POSITION_GROUP);

    rearrangeStrategy.insertIfStatement(ifStmt, thenBlock);

    return rewrite;
  }

  String proposeLocalName(SimpleName fieldName, CompilationUnit root, IJavaProject javaProject) {
    // don't propose names that are already in use:
    Collection<String> variableNames =
        new ScopeAnalyzer(root)
            .getUsedVariableNames(
                this.enclosingMethod.getStartPosition(), this.enclosingMethod.getLength());
    String[] names = new String[variableNames.size() + 1];
    variableNames.toArray(names);
    // don't propose the field name itself, either:
    String identifier = fieldName.getIdentifier();
    names[names.length - 1] = identifier;
    return StubUtility.getLocalNameSuggestions(javaProject, identifier, 0, names)[0];
  }

  /**
   * Create a fresh type reference
   *
   * @param typeBinding the type we want to refer to
   * @param ast AST for creating new nodes
   * @param imports use this for optimal type names
   * @return a fully features non-null type reference (can be parameterized and/or array).
   */
  public static Type newType(ITypeBinding typeBinding, AST ast, ImportRewrite imports) {
    // unwrap array type:
    int dimensions = typeBinding.getDimensions();
    if (dimensions > 0) typeBinding = typeBinding.getElementType();

    // unwrap parameterized type:
    ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
    typeBinding = typeBinding.getErasure();

    // create leaf type:
    Type elementType =
        (typeBinding.isPrimitive())
            ? ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()))
            : ast.newSimpleType(ast.newName(imports.addImport(typeBinding)));

    // re-wrap as parameterized type:
    if (typeArguments.length > 0) {
      ParameterizedType parameterizedType = ast.newParameterizedType(elementType);
      for (ITypeBinding typeArgument : typeArguments)
        parameterizedType.typeArguments().add(newType(typeArgument, ast, imports));
      elementType = parameterizedType;
    }

    // re-wrap as array type:
    if (dimensions > 0) return ast.newArrayType(elementType, dimensions);
    else return elementType;
  }
}

/**
 * ***************************************************************************** Copyright (c) 2014
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.NoCommentSourceRangeComputer;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.text.edits.TextEditGroup;

public class TypeParametersFix extends CompilationUnitRewriteOperationsFix {

  private static final class InsertTypeArgumentsVisitor extends ASTVisitor {

    private final ArrayList<ASTNode> fNodes;

    private InsertTypeArgumentsVisitor(ArrayList<ASTNode> nodes) {
      fNodes = nodes;
    }

    @Override
    public boolean visit(ParameterizedType createdType) {
      if (createdType == null || createdType.typeArguments().size() != 0) {
        return true;
      }

      ITypeBinding binding = createdType.resolveBinding();
      if (binding == null) {
        return true;
      }

      ITypeBinding[] typeArguments = binding.getTypeArguments();
      if (typeArguments.length == 0) {
        return true;
      }

      fNodes.add(createdType);
      return true;
    }
  }

  private static class InsertTypeArgumentsOperation extends CompilationUnitRewriteOperation {

    private final ParameterizedType[] fCreatedTypes;

    public InsertTypeArgumentsOperation(ParameterizedType[] parameterizedTypes) {
      fCreatedTypes = parameterizedTypes;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {
      TextEditGroup group =
          createTextEditGroup(
              FixMessages.TypeParametersFix_insert_inferred_type_arguments_description, cuRewrite);

      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      ImportRewrite importRewrite = cuRewrite.getImportRewrite();
      AST ast = cuRewrite.getRoot().getAST();

      for (int i = 0; i < fCreatedTypes.length; i++) {
        ParameterizedType createdType = fCreatedTypes[i];

        ITypeBinding[] typeArguments = createdType.resolveBinding().getTypeArguments();
        ContextSensitiveImportRewriteContext importContext =
            new ContextSensitiveImportRewriteContext(
                cuRewrite.getRoot(), createdType.getStartPosition(), importRewrite);

        ListRewrite argumentsRewrite =
            rewrite.getListRewrite(createdType, ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
        for (int j = 0; j < typeArguments.length; j++) {
          ITypeBinding typeArgument = typeArguments[j];
          Type argumentNode = importRewrite.addImport(typeArgument, ast, importContext);
          argumentsRewrite.insertLast(argumentNode, group);
        }
      }
    }
  }

  private static class RemoveTypeArgumentsOperation extends CompilationUnitRewriteOperation {

    private final ParameterizedType fParameterizedType;

    public RemoveTypeArgumentsOperation(ParameterizedType parameterizedType) {
      fParameterizedType = parameterizedType;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {
      TextEditGroup group =
          createTextEditGroup(
              FixMessages.TypeParametersFix_remove_redundant_type_arguments_description, cuRewrite);

      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      rewrite.setTargetSourceRangeComputer(new NoCommentSourceRangeComputer());

      ListRewrite listRewrite =
          rewrite.getListRewrite(fParameterizedType, ParameterizedType.TYPE_ARGUMENTS_PROPERTY);

      List<Type> typeArguments = fParameterizedType.typeArguments();
      for (Iterator<Type> iterator = typeArguments.iterator(); iterator.hasNext(); ) {
        listRewrite.remove(iterator.next(), group);
      }
    }
  }

  public static TypeParametersFix createInsertInferredTypeArgumentsFix(
      CompilationUnit compilationUnit, ParameterizedType node) {
    if (node == null) return null;

    final ArrayList<ASTNode> changedNodes = new ArrayList<ASTNode>();
    node.accept(new InsertTypeArgumentsVisitor(changedNodes));

    if (changedNodes.isEmpty()) return null;

    CompilationUnitRewriteOperation op =
        new InsertTypeArgumentsOperation(new ParameterizedType[] {node});
    return new TypeParametersFix(
        FixMessages.TypeParametersFix_insert_inferred_type_arguments_name,
        compilationUnit,
        new CompilationUnitRewriteOperation[] {op});
  }

  public static TypeParametersFix createRemoveRedundantTypeArgumentsFix(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    int id = problem.getProblemId();
    if (id == IProblem.RedundantSpecificationOfTypeArguments) {
      ParameterizedType parameterizedType = getParameterizedType(compilationUnit, problem);
      if (parameterizedType == null) return null;
      RemoveTypeArgumentsOperation operation = new RemoveTypeArgumentsOperation(parameterizedType);
      return new TypeParametersFix(
          FixMessages.TypeParametersFix_remove_redundant_type_arguments_name,
          compilationUnit,
          new CompilationUnitRewriteOperation[] {operation});
    }
    return null;
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      boolean insertInferredTypeArguments,
      boolean removeRedundantTypeArguments) {

    IProblem[] problems = compilationUnit.getProblems();
    IProblemLocation[] locations = new IProblemLocation[problems.length];
    for (int i = 0; i < problems.length; i++) {
      locations[i] = new ProblemLocation(problems[i]);
    }

    return createCleanUp(
        compilationUnit, locations, insertInferredTypeArguments, removeRedundantTypeArguments);
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      IProblemLocation[] problems,
      boolean insertInferredTypeArguments,
      boolean removeRedundantTypeArguments) {

    if (insertInferredTypeArguments) {
      final ArrayList<ASTNode> changedNodes = new ArrayList<ASTNode>();
      compilationUnit.accept(new InsertTypeArgumentsVisitor(changedNodes));

      if (changedNodes.isEmpty()) return null;

      CompilationUnitRewriteOperation op =
          new InsertTypeArgumentsOperation(
              changedNodes.toArray(new ParameterizedType[changedNodes.size()]));
      return new TypeParametersFix(
          FixMessages.TypeParametersFix_insert_inferred_type_arguments_name,
          compilationUnit,
          new CompilationUnitRewriteOperation[] {op});

    } else if (removeRedundantTypeArguments) {
      List<CompilationUnitRewriteOperation> result =
          new ArrayList<CompilationUnitRewriteOperation>();
      for (int i = 0; i < problems.length; i++) {
        IProblemLocation problem = problems[i];
        int id = problem.getProblemId();

        if (id == IProblem.RedundantSpecificationOfTypeArguments) {
          ParameterizedType parameterizedType = getParameterizedType(compilationUnit, problem);
          if (parameterizedType == null) return null;
          result.add(new RemoveTypeArgumentsOperation(parameterizedType));
        }
      }
      if (!result.isEmpty()) {
        return new TypeParametersFix(
            FixMessages.TypeParametersFix_remove_redundant_type_arguments_name,
            compilationUnit,
            result.toArray(new CompilationUnitRewriteOperation[result.size()]));
      }
    }
    return null;
  }

  private static ParameterizedType getParameterizedType(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    ASTNode selectedNode = problem.getCoveringNode(compilationUnit);
    if (selectedNode == null) return null;

    while (!(selectedNode instanceof ParameterizedType) && !(selectedNode instanceof Statement)) {
      selectedNode = selectedNode.getParent();
    }
    if (selectedNode instanceof ParameterizedType) {
      return (ParameterizedType) selectedNode;
    }
    return null;
  }

  protected TypeParametersFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations) {
    super(name, compilationUnit, fixRewriteOperations);
  }
}

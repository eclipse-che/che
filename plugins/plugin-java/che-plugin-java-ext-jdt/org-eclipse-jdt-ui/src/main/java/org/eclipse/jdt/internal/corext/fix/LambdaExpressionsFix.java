/**
 * ***************************************************************************** Copyright (c) 2013,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.HierarchicalASTVisitor;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.ImportRemover;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.TextEditGroup;

public class LambdaExpressionsFix extends CompilationUnitRewriteOperationsFix {

  private static final class FunctionalAnonymousClassesFinder extends ASTVisitor {

    private final ArrayList<ClassInstanceCreation> fNodes = new ArrayList<ClassInstanceCreation>();

    public static ArrayList<ClassInstanceCreation> perform(ASTNode node) {
      FunctionalAnonymousClassesFinder finder = new FunctionalAnonymousClassesFinder();
      node.accept(finder);
      return finder.fNodes;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
      if (isFunctionalAnonymous(node)) {
        fNodes.add(node);
      }
      return true;
    }
  }

  private static final class LambdaExpressionsFinder extends ASTVisitor {

    private final ArrayList<LambdaExpression> fNodes = new ArrayList<LambdaExpression>();

    public static ArrayList<LambdaExpression> perform(ASTNode node) {
      LambdaExpressionsFinder finder = new LambdaExpressionsFinder();
      node.accept(finder);
      return finder.fNodes;
    }

    @Override
    public boolean visit(LambdaExpression node) {
      ITypeBinding typeBinding = node.resolveTypeBinding();
      if (typeBinding != null && typeBinding.getFunctionalInterfaceMethod() != null) {
        fNodes.add(node);
      }
      return true;
    }
  }

  private static class AbortSearchException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  private static final class SuperThisReferenceFinder extends HierarchicalASTVisitor {

    private ITypeBinding fFunctionalInterface;
    private MethodDeclaration fMethodDeclaration;

    static boolean hasReference(MethodDeclaration node) {
      try {
        SuperThisReferenceFinder finder = new SuperThisReferenceFinder();
        ClassInstanceCreation cic = (ClassInstanceCreation) node.getParent().getParent();
        finder.fFunctionalInterface = cic.getType().resolveBinding();
        finder.fMethodDeclaration = node;
        node.accept(finder);
      } catch (AbortSearchException e) {
        return true;
      }
      return false;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(BodyDeclaration node) {
      return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
      return node == fMethodDeclaration;
    }

    @Override
    public boolean visit(ThisExpression node) {
      if (node.getQualifier() == null) throw new AbortSearchException();
      return true; // references to outer scope are harmless
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
      if (node.getQualifier() == null) {
        throw new AbortSearchException();
      } else {
        IBinding qualifierType = node.getQualifier().resolveBinding();
        if (qualifierType instanceof ITypeBinding && ((ITypeBinding) qualifierType).isInterface()) {
          throw new AbortSearchException(); // JLS8: new overloaded meaning of
          // 'interface'.super.'method'(..)
        }
      }
      return true; // references to outer scopes are harmless
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      throw new AbortSearchException();
    }

    @Override
    public boolean visit(MethodInvocation node) {
      IMethodBinding binding = node.resolveMethodBinding();
      if (binding != null
          && !JdtFlags.isStatic(binding)
          && node.getExpression() == null
          && Bindings.isSuperType(binding.getDeclaringClass(), fFunctionalInterface, false))
        throw new AbortSearchException();
      return true;
    }
  }

  private static class CreateLambdaOperation extends CompilationUnitRewriteOperation {

    private final List<ClassInstanceCreation> fExpressions;

    public CreateLambdaOperation(List<ClassInstanceCreation> expressions) {
      fExpressions = expressions;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {

      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      ImportRemover importRemover = cuRewrite.getImportRemover();
      AST ast = rewrite.getAST();

      HashMap<ClassInstanceCreation, HashSet<String>> cicToNewNames =
          new HashMap<ClassInstanceCreation, HashSet<String>>();
      for (int i = 0; i < fExpressions.size(); i++) {
        ClassInstanceCreation classInstanceCreation = fExpressions.get(i);
        TextEditGroup group =
            createTextEditGroup(
                FixMessages.LambdaExpressionsFix_convert_to_lambda_expression, cuRewrite);

        AnonymousClassDeclaration anonymTypeDecl =
            classInstanceCreation.getAnonymousClassDeclaration();
        List<BodyDeclaration> bodyDeclarations = anonymTypeDecl.bodyDeclarations();

        Object object = bodyDeclarations.get(0);
        if (!(object instanceof MethodDeclaration)) continue;
        MethodDeclaration methodDeclaration = (MethodDeclaration) object;
        HashSet<String> excludedNames = new HashSet<String>();
        if (i != 0) {
          for (ClassInstanceCreation convertedCic : fExpressions.subList(0, i)) {
            if (ASTNodes.isParent(classInstanceCreation, convertedCic)) {
              excludedNames.addAll(cicToNewNames.get(convertedCic));
            }
          }
        }
        HashSet<String> newNames =
            makeNamesUnique(excludedNames, methodDeclaration, rewrite, group);
        cicToNewNames.put(classInstanceCreation, new HashSet<String>(newNames));
        List<SingleVariableDeclaration> methodParameters = methodDeclaration.parameters();

        // use short form with inferred parameter types and without parentheses if possible
        LambdaExpression lambdaExpression = ast.newLambdaExpression();
        List<VariableDeclaration> lambdaParameters = lambdaExpression.parameters();
        lambdaExpression.setParentheses(methodParameters.size() != 1);
        for (SingleVariableDeclaration methodParameter : methodParameters) {
          VariableDeclarationFragment lambdaParameter = ast.newVariableDeclarationFragment();
          lambdaParameter.setName((SimpleName) rewrite.createCopyTarget(methodParameter.getName()));
          lambdaParameters.add(lambdaParameter);
        }

        Block body = methodDeclaration.getBody();
        List<Statement> statements = body.statements();
        ASTNode lambdaBody = body;
        if (statements.size() == 1) {
          // use short form with just an expression body if possible
          Statement statement = statements.get(0);
          if (statement instanceof ExpressionStatement) {
            lambdaBody = ((ExpressionStatement) statement).getExpression();
          } else if (statement instanceof ReturnStatement) {
            Expression returnExpression = ((ReturnStatement) statement).getExpression();
            if (returnExpression != null) {
              lambdaBody = returnExpression;
            }
          }
        }
        // TODO: Bug 421479: [1.8][clean up][quick assist] convert anonymous to lambda must consider
        // lost scope of interface
        //				lambdaBody.accept(new InterfaceAccessQualifier(rewrite,
        // classInstanceCreation.getType().resolveBinding())); //TODO: maybe
        // need a separate ASTRewrite and string placeholder

        lambdaExpression.setBody(rewrite.createCopyTarget(lambdaBody));
        Expression replacement = lambdaExpression;
        if (ASTNodes.isTargetAmbiguous(classInstanceCreation, lambdaParameters.isEmpty())) {
          CastExpression cast = ast.newCastExpression();
          cast.setExpression(lambdaExpression);
          ImportRewrite importRewrite = cuRewrite.getImportRewrite();
          ImportRewriteContext importRewriteContext =
              new ContextSensitiveImportRewriteContext(classInstanceCreation, importRewrite);
          Type castType =
              importRewrite.addImport(
                  classInstanceCreation.getType().resolveBinding(), ast, importRewriteContext);
          cast.setType(castType);
          importRemover.registerAddedImports(castType);
          replacement = cast;
        }
        rewrite.replace(classInstanceCreation, replacement, group);

        importRemover.registerRemovedNode(classInstanceCreation);
        importRemover.registerRetainedNode(lambdaBody);
      }
    }

    private HashSet<String> makeNamesUnique(
        HashSet<String> excludedNames,
        MethodDeclaration methodDeclaration,
        ASTRewrite rewrite,
        TextEditGroup group) {
      HashSet<String> newNames = new HashSet<String>();
      excludedNames.addAll(ASTNodes.getVisibleLocalVariablesInScope(methodDeclaration));
      List<SimpleName> simpleNamesInMethod = getNamesInMethod(methodDeclaration);
      List<String> namesInMethod = new ArrayList<String>();
      for (SimpleName name : simpleNamesInMethod) {
        namesInMethod.add(name.getIdentifier());
      }

      for (int i = 0; i < simpleNamesInMethod.size(); i++) {
        SimpleName name = simpleNamesInMethod.get(i);
        String identifier = namesInMethod.get(i);
        HashSet<String> allNamesToExclude = getNamesToExclude(excludedNames, namesInMethod, i);
        if (allNamesToExclude.contains(identifier)) {
          String newIdentifier = createName(identifier, allNamesToExclude);
          excludedNames.add(newIdentifier);
          newNames.add(newIdentifier);
          SimpleName[] references = LinkedNodeFinder.findByNode(name.getRoot(), name);
          for (SimpleName ref : references) {
            rewrite.set(ref, SimpleName.IDENTIFIER_PROPERTY, newIdentifier, group);
          }
        }
      }

      return newNames;
    }

    private HashSet<String> getNamesToExclude(
        HashSet<String> excludedNames, List<String> namesInMethod, int i) {
      HashSet<String> allNamesToExclude = new HashSet<String>(excludedNames);
      allNamesToExclude.addAll(namesInMethod.subList(0, i));
      allNamesToExclude.addAll(namesInMethod.subList(i + 1, namesInMethod.size()));
      return allNamesToExclude;
    }

    private List<SimpleName> getNamesInMethod(MethodDeclaration methodDeclaration) {
      class NamesCollector extends HierarchicalASTVisitor {
        private int fTypeCounter;

        private List<SimpleName> fNames = new ArrayList<SimpleName>();

        @Override
        public boolean visit(AbstractTypeDeclaration node) {
          if (fTypeCounter++ == 0) {
            fNames.add(node.getName());
          }
          return true;
        }

        @Override
        public void endVisit(AbstractTypeDeclaration node) {
          fTypeCounter--;
        }

        @Override
        public boolean visit(AnonymousClassDeclaration node) {
          fTypeCounter++;
          return true;
        }

        @Override
        public void endVisit(AnonymousClassDeclaration node) {
          fTypeCounter--;
        }

        @Override
        public boolean visit(VariableDeclaration node) {
          if (fTypeCounter == 0) fNames.add(node.getName());
          return true;
        }
      }

      NamesCollector namesCollector = new NamesCollector();
      methodDeclaration.accept(namesCollector);
      return namesCollector.fNames;
    }

    private String createName(String candidate, HashSet<String> excludedNames) {
      int i = 1;
      String result = candidate;
      while (excludedNames.contains(result)) {
        result = candidate + i++;
      }
      return result;
    }
  }

  private static class CreateAnonymousClassCreationOperation
      extends CompilationUnitRewriteOperation {

    private final List<LambdaExpression> fExpressions;

    public CreateAnonymousClassCreationOperation(List<LambdaExpression> changedNodes) {
      fExpressions = changedNodes;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {

      ASTRewrite rewrite = cuRewrite.getASTRewrite();
      AST ast = rewrite.getAST();

      for (Iterator<LambdaExpression> iterator = fExpressions.iterator(); iterator.hasNext(); ) {
        LambdaExpression lambdaExpression = iterator.next();
        TextEditGroup group =
            createTextEditGroup(
                FixMessages.LambdaExpressionsFix_convert_to_anonymous_class_creation, cuRewrite);

        ITypeBinding lambdaTypeBinding = lambdaExpression.resolveTypeBinding();
        IMethodBinding methodBinding = lambdaTypeBinding.getFunctionalInterfaceMethod();
        List<VariableDeclaration> parameters = lambdaExpression.parameters();
        String[] parameterNames = new String[parameters.size()];
        for (int i = 0; i < parameterNames.length; i++) {
          parameterNames[i] = parameters.get(i).getName().getIdentifier();
        }

        final CodeGenerationSettings settings =
            JavaPreferencesSettings.getCodeGenerationSettings(cuRewrite.getCu().getJavaProject());
        ImportRewrite importRewrite = cuRewrite.getImportRewrite();
        ImportRewriteContext importContext =
            new ContextSensitiveImportRewriteContext(lambdaExpression, importRewrite);

        MethodDeclaration methodDeclaration =
            StubUtility2.createImplementationStub(
                cuRewrite.getCu(),
                rewrite,
                importRewrite,
                importContext,
                methodBinding,
                parameterNames,
                lambdaTypeBinding.getName(),
                settings,
                false);

        Block block;
        ASTNode lambdaBody = lambdaExpression.getBody();
        if (lambdaBody instanceof Block) {
          block = (Block) rewrite.createCopyTarget(lambdaBody);
        } else {
          block = ast.newBlock();
          List<Statement> statements = block.statements();
          ITypeBinding returnType = methodBinding.getReturnType();
          Expression copyTarget = (Expression) rewrite.createCopyTarget(lambdaBody);
          if (Bindings.isVoidType(returnType)) {
            ExpressionStatement newExpressionStatement = ast.newExpressionStatement(copyTarget);
            statements.add(newExpressionStatement);
          } else {
            ReturnStatement returnStatement = ast.newReturnStatement();
            returnStatement.setExpression(copyTarget);
            statements.add(returnStatement);
          }
        }
        methodDeclaration.setBody(block);

        AnonymousClassDeclaration anonymousClassDeclaration = ast.newAnonymousClassDeclaration();
        List<BodyDeclaration> bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
        bodyDeclarations.add(methodDeclaration);

        Type creationType =
            ASTNodeFactory.newCreationType(ast, lambdaTypeBinding, importRewrite, importContext);

        ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
        classInstanceCreation.setType(creationType);
        classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);

        ASTNode toReplace = lambdaExpression;
        if (lambdaExpression.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY
            && lambdaTypeBinding.isEqualTo(
                ((CastExpression) lambdaExpression.getParent()).resolveTypeBinding())) {
          // remove cast to same type as the anonymous will use
          toReplace = lambdaExpression.getParent();
        }
        rewrite.replace(toReplace, classInstanceCreation, group);
      }
    }
  }

  public static LambdaExpressionsFix createConvertToLambdaFix(ClassInstanceCreation cic) {
    CompilationUnit root = (CompilationUnit) cic.getRoot();
    if (!JavaModelUtil.is18OrHigher(root.getJavaElement().getJavaProject())) return null;

    if (!LambdaExpressionsFix.isFunctionalAnonymous(cic)) return null;

    CreateLambdaOperation op = new CreateLambdaOperation(Collections.singletonList(cic));
    return new LambdaExpressionsFix(
        FixMessages.LambdaExpressionsFix_convert_to_lambda_expression,
        root,
        new CompilationUnitRewriteOperation[] {op});
  }

  public static IProposableFix createConvertToAnonymousClassCreationsFix(LambdaExpression lambda) {
    // offer the quick assist at pre 1.8 levels as well to get rid of the compilation error (TODO:
    // offer this as a quick fix in that
    // case)

    if (lambda.resolveTypeBinding() == null
        || lambda.resolveTypeBinding().getFunctionalInterfaceMethod() == null) return null;

    CreateAnonymousClassCreationOperation op =
        new CreateAnonymousClassCreationOperation(Collections.singletonList(lambda));
    CompilationUnit root = (CompilationUnit) lambda.getRoot();
    return new LambdaExpressionsFix(
        FixMessages.LambdaExpressionsFix_convert_to_anonymous_class_creation,
        root,
        new CompilationUnitRewriteOperation[] {op});
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit, boolean useLambda, boolean useAnonymous) {
    if (!JavaModelUtil.is18OrHigher(compilationUnit.getJavaElement().getJavaProject())) return null;

    if (useLambda) {
      ArrayList<ClassInstanceCreation> convertibleNodes =
          FunctionalAnonymousClassesFinder.perform(compilationUnit);
      if (convertibleNodes.isEmpty()) return null;

      CompilationUnitRewriteOperation op = new CreateLambdaOperation(convertibleNodes);
      return new LambdaExpressionsFix(
          FixMessages.LambdaExpressionsFix_convert_to_lambda_expression,
          compilationUnit,
          new CompilationUnitRewriteOperation[] {op});

    } else if (useAnonymous) {
      ArrayList<LambdaExpression> convertibleNodes =
          LambdaExpressionsFinder.perform(compilationUnit);
      if (convertibleNodes.isEmpty()) return null;

      CompilationUnitRewriteOperation op =
          new CreateAnonymousClassCreationOperation(convertibleNodes);
      return new LambdaExpressionsFix(
          FixMessages.LambdaExpressionsFix_convert_to_anonymous_class_creation,
          compilationUnit,
          new CompilationUnitRewriteOperation[] {op});
    }
    return null;
  }

  protected LambdaExpressionsFix(
      String name,
      CompilationUnit compilationUnit,
      CompilationUnitRewriteOperation[] fixRewriteOperations) {
    super(name, compilationUnit, fixRewriteOperations);
  }

  static boolean isFunctionalAnonymous(ClassInstanceCreation node) {
    ITypeBinding typeBinding = node.resolveTypeBinding();
    if (typeBinding == null) return false;
    ITypeBinding[] interfaces = typeBinding.getInterfaces();
    if (interfaces.length != 1) return false;
    if (interfaces[0].getFunctionalInterfaceMethod() == null) return false;

    AnonymousClassDeclaration anonymTypeDecl = node.getAnonymousClassDeclaration();
    if (anonymTypeDecl == null || anonymTypeDecl.resolveBinding() == null) return false;

    List<BodyDeclaration> bodyDeclarations = anonymTypeDecl.bodyDeclarations();
    // cannot convert if there are fields or additional methods
    if (bodyDeclarations.size() != 1) return false;
    BodyDeclaration bodyDeclaration = bodyDeclarations.get(0);
    if (!(bodyDeclaration instanceof MethodDeclaration)) return false;

    MethodDeclaration methodDecl = (MethodDeclaration) bodyDeclaration;
    IMethodBinding methodBinding = methodDecl.resolveBinding();

    if (methodBinding == null) return false;
    // generic lambda expressions are not allowed
    if (methodBinding.isGenericMethod()) return false;

    // lambda cannot refer to 'this'/'super' literals
    if (SuperThisReferenceFinder.hasReference(methodDecl)) return false;

    if (!isInTargetTypeContext(node)) return false;

    return true;
  }

  private static boolean isInTargetTypeContext(ClassInstanceCreation node) {
    ITypeBinding targetType = ASTNodes.getTargetType(node);
    return targetType != null && targetType.getFunctionalInterfaceMethod() != null;

    /*
    //TODO: probably incomplete, should reuse https://bugs.eclipse.org/bugs/show_bug.cgi?id=408966#c6
    StructuralPropertyDescriptor locationInParent= node.getLocationInParent();

    if (locationInParent == ReturnStatement.EXPRESSION_PROPERTY) {
    	MethodDeclaration methodDeclaration= ASTResolving.findParentMethodDeclaration(node);
    	if (methodDeclaration == null)
    		return false;
    	IMethodBinding methodBinding= methodDeclaration.resolveBinding();
    	if (methodBinding == null)
    		return false;
    	//TODO: could also cast to the CIC type instead of aborting...
    	return methodBinding.getReturnType().getFunctionalInterfaceMethod() != null;
    }

    //TODO: should also check whether variable is of a functional type
    return locationInParent == SingleVariableDeclaration.INITIALIZER_PROPERTY
    		|| locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY
    		|| locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY
    		|| locationInParent == ArrayInitializer.EXPRESSIONS_PROPERTY

    		|| locationInParent == MethodInvocation.ARGUMENTS_PROPERTY
    		|| locationInParent == SuperMethodInvocation.ARGUMENTS_PROPERTY
    		|| locationInParent == ConstructorInvocation.ARGUMENTS_PROPERTY
    		|| locationInParent == SuperConstructorInvocation.ARGUMENTS_PROPERTY
    		|| locationInParent == ClassInstanceCreation.ARGUMENTS_PROPERTY
    		|| locationInParent == EnumConstantDeclaration.ARGUMENTS_PROPERTY

    		|| locationInParent == LambdaExpression.BODY_PROPERTY
    		|| locationInParent == ConditionalExpression.THEN_EXPRESSION_PROPERTY
    		|| locationInParent == ConditionalExpression.ELSE_EXPRESSION_PROPERTY
    		|| locationInParent == CastExpression.EXPRESSION_PROPERTY;
    */
  }
}

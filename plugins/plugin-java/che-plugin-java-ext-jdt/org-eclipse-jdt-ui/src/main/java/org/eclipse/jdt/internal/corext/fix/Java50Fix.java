/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.generics.InferTypeArgumentsConstraintCreator;
import org.eclipse.jdt.internal.corext.refactoring.generics.InferTypeArgumentsConstraintsSolver;
import org.eclipse.jdt.internal.corext.refactoring.generics.InferTypeArgumentsRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.generics.InferTypeArgumentsTCModel;
import org.eclipse.jdt.internal.corext.refactoring.generics.InferTypeArgumentsUpdate;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Fix which introduce new language constructs to pre Java50 code. Requires a compiler level setting
 * of 5.0+ Supported: Add missing @Override annotation Add missing @Deprecated annotation Convert
 * for loop to enhanced for loop
 */
public class Java50Fix extends CompilationUnitRewriteOperationsFix {

  private static final String OVERRIDE = "Override"; // $NON-NLS-1$
  private static final String DEPRECATED = "Deprecated"; // $NON-NLS-1$

  private static class AnnotationRewriteOperation extends CompilationUnitRewriteOperation {
    private final BodyDeclaration fBodyDeclaration;
    private final String fAnnotation;

    public AnnotationRewriteOperation(BodyDeclaration bodyDeclaration, String annotation) {
      fBodyDeclaration = bodyDeclaration;
      fAnnotation = annotation;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel model)
        throws CoreException {
      AST ast = cuRewrite.getRoot().getAST();
      ListRewrite listRewrite =
          cuRewrite
              .getASTRewrite()
              .getListRewrite(fBodyDeclaration, fBodyDeclaration.getModifiersProperty());
      Annotation newAnnotation = ast.newMarkerAnnotation();
      newAnnotation.setTypeName(ast.newSimpleName(fAnnotation));
      TextEditGroup group =
          createTextEditGroup(
              Messages.format(
                  FixMessages.Java50Fix_AddMissingAnnotation_description,
                  BasicElementLabels.getJavaElementName(fAnnotation)),
              cuRewrite);
      listRewrite.insertFirst(newAnnotation, group);
    }
  }

  private static class AddTypeParametersOperation extends CompilationUnitRewriteOperation {

    private final SimpleType[] fTypes;

    public AddTypeParametersOperation(SimpleType[] types) {
      fTypes = types;
    }

    /** {@inheritDoc} */
    @Override
    public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel positionGroups)
        throws CoreException {
      InferTypeArgumentsTCModel model = new InferTypeArgumentsTCModel();
      InferTypeArgumentsConstraintCreator creator =
          new InferTypeArgumentsConstraintCreator(model, true);

      CompilationUnit root = cuRewrite.getRoot();
      root.accept(creator);

      InferTypeArgumentsConstraintsSolver solver = new InferTypeArgumentsConstraintsSolver(model);
      InferTypeArgumentsUpdate update = solver.solveConstraints(new NullProgressMonitor());
      solver = null; // free caches

      ParameterizedType[] nodes =
          InferTypeArgumentsRefactoring.inferArguments(fTypes, update, model, cuRewrite);
      if (nodes.length == 0) return;

      ASTRewrite astRewrite = cuRewrite.getASTRewrite();
      for (int i = 0; i < nodes.length; i++) {
        ParameterizedType type = nodes[i];
        List<Type> args = type.typeArguments();
        int j = 0;
        for (Iterator<Type> iter = args.iterator(); iter.hasNext(); ) {
          LinkedProposalPositionGroup group =
              new LinkedProposalPositionGroup("G" + i + "_" + j); // $NON-NLS-1$ //$NON-NLS-2$
          Type argType = iter.next();
          if (!positionGroups.hasLinkedPositions()) {
            group.addPosition(astRewrite.track(argType), true);
          } else {
            group.addPosition(astRewrite.track(argType), false);
          }
          positionGroups.addPositionGroup(group);
          j++;
        }
      }
      positionGroups.setEndPosition(astRewrite.track(nodes[0]));
    }
  }

  public static Java50Fix createAddOverrideAnnotationFix(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    if (!isMissingOverrideAnnotationProblem(problem.getProblemId())) return null;

    return createFix(
        compilationUnit, problem, OVERRIDE, FixMessages.Java50Fix_AddOverride_description);
  }

  public static boolean isMissingOverrideAnnotationInterfaceProblem(int id) {
    return id == IProblem.MissingOverrideAnnotationForInterfaceMethodImplementation;
  }

  public static boolean isMissingOverrideAnnotationProblem(int id) {
    return id == IProblem.MissingOverrideAnnotation
        || id == IProblem.MissingOverrideAnnotationForInterfaceMethodImplementation;
  }

  public static Java50Fix createAddDeprectatedAnnotation(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    if (!isMissingDeprecationProblem(problem.getProblemId())) return null;

    return createFix(
        compilationUnit, problem, DEPRECATED, FixMessages.Java50Fix_AddDeprecated_description);
  }

  public static boolean isMissingDeprecationProblem(int id) {
    return id == IProblem.FieldMissingDeprecatedAnnotation
        || id == IProblem.MethodMissingDeprecatedAnnotation
        || id == IProblem.TypeMissingDeprecatedAnnotation;
  }

  private static Java50Fix createFix(
      CompilationUnit compilationUnit, IProblemLocation problem, String annotation, String label) {
    ICompilationUnit cu = (ICompilationUnit) compilationUnit.getJavaElement();
    if (!JavaModelUtil.is50OrHigher(cu.getJavaProject())) return null;

    ASTNode selectedNode = problem.getCoveringNode(compilationUnit);
    if (selectedNode == null) return null;

    ASTNode declaringNode = getDeclaringNode(selectedNode);
    if (!(declaringNode instanceof BodyDeclaration)) return null;

    BodyDeclaration declaration = (BodyDeclaration) declaringNode;

    AnnotationRewriteOperation operation = new AnnotationRewriteOperation(declaration, annotation);

    return new Java50Fix(label, compilationUnit, new CompilationUnitRewriteOperation[] {operation});
  }

  public static Java50Fix createRawTypeReferenceFix(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    List<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();
    SimpleType node =
        createRawTypeReferenceOperations(
            compilationUnit, new IProblemLocation[] {problem}, operations);
    if (operations.size() == 0) return null;

    return new Java50Fix(
        Messages.format(
            FixMessages.Java50Fix_AddTypeArguments_description,
            BasicElementLabels.getJavaElementName(node.getName().getFullyQualifiedName())),
        compilationUnit,
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]));
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      boolean addOverrideAnnotation,
      boolean addOverrideInterfaceAnnotation,
      boolean addDeprecatedAnnotation,
      boolean rawTypeReference) {

    ICompilationUnit cu = (ICompilationUnit) compilationUnit.getJavaElement();
    if (!JavaModelUtil.is50OrHigher(cu.getJavaProject())) return null;

    if (!addOverrideAnnotation && !addDeprecatedAnnotation && !rawTypeReference) return null;

    List<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();

    IProblem[] problems = compilationUnit.getProblems();
    IProblemLocation[] locations = new IProblemLocation[problems.length];
    for (int i = 0; i < problems.length; i++) {
      locations[i] = new ProblemLocation(problems[i]);
    }

    if (addOverrideAnnotation)
      createAddOverrideAnnotationOperations(
          compilationUnit, addOverrideInterfaceAnnotation, locations, operations);

    if (addDeprecatedAnnotation)
      createAddDeprecatedAnnotationOperations(compilationUnit, locations, operations);

    if (rawTypeReference) createRawTypeReferenceOperations(compilationUnit, locations, operations);

    if (operations.size() == 0) return null;

    String fixName;
    if (rawTypeReference) {
      fixName = FixMessages.Java50Fix_add_type_parameters_change_name;
    } else {
      fixName = FixMessages.Java50Fix_add_annotations_change_name;
    }

    CompilationUnitRewriteOperation[] operationsArray =
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
    return new Java50Fix(fixName, compilationUnit, operationsArray);
  }

  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit,
      IProblemLocation[] problems,
      boolean addOverrideAnnotation,
      boolean addOverrideInterfaceAnnotation,
      boolean addDeprecatedAnnotation,
      boolean rawTypeReferences) {

    ICompilationUnit cu = (ICompilationUnit) compilationUnit.getJavaElement();
    if (!JavaModelUtil.is50OrHigher(cu.getJavaProject())) return null;

    if (!addOverrideAnnotation && !addDeprecatedAnnotation && !rawTypeReferences) return null;

    List<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();

    if (addOverrideAnnotation)
      createAddOverrideAnnotationOperations(
          compilationUnit, addOverrideInterfaceAnnotation, problems, operations);

    if (addDeprecatedAnnotation)
      createAddDeprecatedAnnotationOperations(compilationUnit, problems, operations);

    if (rawTypeReferences) createRawTypeReferenceOperations(compilationUnit, problems, operations);

    if (operations.size() == 0) return null;

    CompilationUnitRewriteOperation[] operationsArray =
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
    return new Java50Fix(
        FixMessages.Java50Fix_add_annotations_change_name, compilationUnit, operationsArray);
  }

  private static void createAddDeprecatedAnnotationOperations(
      CompilationUnit compilationUnit,
      IProblemLocation[] locations,
      List<CompilationUnitRewriteOperation> result) {
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation problem = locations[i];

      if (isMissingDeprecationProblem(problem.getProblemId())) {
        ASTNode selectedNode = problem.getCoveringNode(compilationUnit);
        if (selectedNode != null) {

          ASTNode declaringNode = getDeclaringNode(selectedNode);
          if (declaringNode instanceof BodyDeclaration) {
            BodyDeclaration declaration = (BodyDeclaration) declaringNode;
            AnnotationRewriteOperation operation =
                new AnnotationRewriteOperation(declaration, DEPRECATED);
            result.add(operation);
          }
        }
      }
    }
  }

  private static void createAddOverrideAnnotationOperations(
      CompilationUnit compilationUnit,
      boolean addOverrideInterfaceAnnotation,
      IProblemLocation[] locations,
      List<CompilationUnitRewriteOperation> result) {
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation problem = locations[i];
      int problemId = problem.getProblemId();

      if (isMissingOverrideAnnotationProblem(problemId)) {
        if (!isMissingOverrideAnnotationInterfaceProblem(problemId)
            || addOverrideInterfaceAnnotation) {
          ASTNode selectedNode = problem.getCoveringNode(compilationUnit);
          if (selectedNode != null) {

            ASTNode declaringNode = getDeclaringNode(selectedNode);
            if (declaringNode instanceof BodyDeclaration) {
              BodyDeclaration declaration = (BodyDeclaration) declaringNode;
              AnnotationRewriteOperation operation =
                  new AnnotationRewriteOperation(declaration, OVERRIDE);
              result.add(operation);
            }
          }
        }
      }
    }
  }

  private static SimpleType createRawTypeReferenceOperations(
      CompilationUnit compilationUnit,
      IProblemLocation[] locations,
      List<CompilationUnitRewriteOperation> operations) {
    if (hasFatalError(compilationUnit)) return null;

    List<SimpleType> result = new ArrayList<SimpleType>();
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation problem = locations[i];

      if (isRawTypeReferenceProblem(problem.getProblemId())) {
        ASTNode node = problem.getCoveredNode(compilationUnit);
        if (node instanceof ClassInstanceCreation) {
          Type rawReference =
              (Type) node.getStructuralProperty(ClassInstanceCreation.TYPE_PROPERTY);
          if (isRawTypeReference(rawReference)) {
            result.add((SimpleType) rawReference);
          }
        } else if (node instanceof SimpleName) {
          ASTNode rawReference = node.getParent();
          if (isRawTypeReference(rawReference)) {
            ASTNode parent = rawReference.getParent();
            if (!(parent instanceof ArrayType || parent instanceof ParameterizedType))
              result.add((SimpleType) rawReference);
          }
        } else if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;

          SimpleType rawReference = getRawReference(invocation, compilationUnit);
          if (rawReference != null) {
            result.add(rawReference);
          }
        }
      }
    }

    if (result.size() == 0) return null;

    SimpleType[] types = result.toArray(new SimpleType[result.size()]);
    operations.add(new AddTypeParametersOperation(types));
    return types[0];
  }

  private static boolean hasFatalError(CompilationUnit compilationUnit) {
    try {
      if (!((ICompilationUnit) compilationUnit.getJavaElement()).isStructureKnown()) return true;
    } catch (JavaModelException e) {
      JavaPlugin.log(e);
      return true;
    }

    IProblem[] problems = compilationUnit.getProblems();
    for (int i = 0; i < problems.length; i++) {
      if (problems[i].isError()) {
        if (!(problems[i] instanceof CategorizedProblem)) return true;

        CategorizedProblem categorizedProblem = (CategorizedProblem) problems[i];
        int categoryID = categorizedProblem.getCategoryID();

        if (categoryID == CategorizedProblem.CAT_BUILDPATH) return true;
        if (categoryID == CategorizedProblem.CAT_SYNTAX) return true;
        if (categoryID == CategorizedProblem.CAT_IMPORT) return true;
        if (categoryID == CategorizedProblem.CAT_TYPE) return true;
        if (categoryID == CategorizedProblem.CAT_MEMBER) return true;
        if (categoryID == CategorizedProblem.CAT_INTERNAL) return true;
      }
    }

    return false;
  }

  public static boolean isRawTypeReferenceProblem(int id) {
    switch (id) {
      case IProblem.UnsafeTypeConversion:
      case IProblem.UnsafeElementTypeConversion:
      case IProblem.RawTypeReference:
      case IProblem.UnsafeRawMethodInvocation:
        return true;
      default:
        return false;
    }
  }

  private static SimpleType getRawReference(
      MethodInvocation invocation, CompilationUnit compilationUnit) {
    Name name1 = (Name) invocation.getStructuralProperty(MethodInvocation.NAME_PROPERTY);
    if (name1 instanceof SimpleName) {
      SimpleType rawReference = getRawReference((SimpleName) name1, compilationUnit);
      if (rawReference != null) {
        return rawReference;
      }
    }

    Expression expr =
        (Expression) invocation.getStructuralProperty(MethodInvocation.EXPRESSION_PROPERTY);
    if (expr instanceof SimpleName) {
      SimpleType rawReference = getRawReference((SimpleName) expr, compilationUnit);
      if (rawReference != null) {
        return rawReference;
      }
    } else if (expr instanceof QualifiedName) {
      Name name = (Name) expr;
      while (name instanceof QualifiedName) {
        SimpleName simpleName =
            (SimpleName) name.getStructuralProperty(QualifiedName.NAME_PROPERTY);
        SimpleType rawReference = getRawReference(simpleName, compilationUnit);
        if (rawReference != null) {
          return rawReference;
        }
        name = (Name) name.getStructuralProperty(QualifiedName.QUALIFIER_PROPERTY);
      }
      if (name instanceof SimpleName) {
        SimpleType rawReference = getRawReference((SimpleName) name, compilationUnit);
        if (rawReference != null) {
          return rawReference;
        }
      }
    } else if (expr instanceof MethodInvocation) {
      SimpleType rawReference = getRawReference((MethodInvocation) expr, compilationUnit);
      if (rawReference != null) {
        return rawReference;
      }
    }
    return null;
  }

  private static SimpleType getRawReference(SimpleName name, CompilationUnit compilationUnit) {
    SimpleName[] names = LinkedNodeFinder.findByNode(compilationUnit, name);
    for (int j = 0; j < names.length; j++) {
      if (names[j].getParent() instanceof VariableDeclarationFragment) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) names[j].getParent();
        if (fragment.getParent() instanceof VariableDeclarationStatement) {
          VariableDeclarationStatement statement =
              (VariableDeclarationStatement) fragment.getParent();
          ASTNode result =
              (ASTNode) statement.getStructuralProperty(VariableDeclarationStatement.TYPE_PROPERTY);
          if (isRawTypeReference(result)) return (SimpleType) result;
        } else if (fragment.getParent() instanceof FieldDeclaration) {
          FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
          ASTNode result =
              (ASTNode) declaration.getStructuralProperty(FieldDeclaration.TYPE_PROPERTY);
          if (isRawTypeReference(result)) return (SimpleType) result;
        }
      } else if (names[j].getParent() instanceof SingleVariableDeclaration) {
        SingleVariableDeclaration declaration = (SingleVariableDeclaration) names[j].getParent();
        ASTNode result =
            (ASTNode) declaration.getStructuralProperty(SingleVariableDeclaration.TYPE_PROPERTY);
        if (isRawTypeReference(result)) return (SimpleType) result;
      } else if (names[j].getParent() instanceof MethodDeclaration) {
        MethodDeclaration methodDecl = (MethodDeclaration) names[j].getParent();
        ASTNode result =
            (ASTNode) methodDecl.getStructuralProperty(MethodDeclaration.RETURN_TYPE2_PROPERTY);
        if (isRawTypeReference(result)) return (SimpleType) result;
      }
    }
    return null;
  }

  private static boolean isRawTypeReference(ASTNode node) {
    if (!(node instanceof SimpleType)) return false;

    ITypeBinding typeBinding = ((SimpleType) node).resolveBinding();
    if (typeBinding == null) return false;

    ITypeBinding binding = typeBinding.getTypeDeclaration();
    if (binding == null) return false;

    ITypeBinding[] parameters = binding.getTypeParameters();
    if (parameters.length == 0) return false;

    return true;
  }

  private static ASTNode getDeclaringNode(ASTNode selectedNode) {
    ASTNode declaringNode = null;
    if (selectedNode instanceof MethodDeclaration) {
      declaringNode = selectedNode;
    } else if (selectedNode instanceof SimpleName) {
      StructuralPropertyDescriptor locationInParent = selectedNode.getLocationInParent();
      if (locationInParent == MethodDeclaration.NAME_PROPERTY
          || locationInParent == TypeDeclaration.NAME_PROPERTY) {
        declaringNode = selectedNode.getParent();
      } else if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY) {
        declaringNode = selectedNode.getParent().getParent();
      }
    }
    return declaringNode;
  }

  private Java50Fix(
      String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperation[] fixRewrites) {
    super(name, compilationUnit, fixRewrites);
  }
}

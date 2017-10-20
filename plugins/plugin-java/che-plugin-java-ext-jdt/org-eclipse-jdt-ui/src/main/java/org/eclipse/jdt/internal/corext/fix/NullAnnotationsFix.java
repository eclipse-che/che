/**
 * ***************************************************************************** Copyright (c) 2011,
 * 2013 GK Software AG and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Stephan Herrmann - [quick fix] Add quick fixes for null annotations -
 * https://bugs.eclipse.org/337977 IBM Corporation - bug fixes
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.fix.NullAnnotationsRewriteOperations.ChangeKind;
import org.eclipse.jdt.internal.corext.fix.NullAnnotationsRewriteOperations.RemoveRedundantAnnotationRewriteOperation;
import org.eclipse.jdt.internal.corext.fix.NullAnnotationsRewriteOperations.SignatureAnnotationRewriteOperation;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public class NullAnnotationsFix extends CompilationUnitRewriteOperationsFix {

  private CompilationUnit cu;

  public NullAnnotationsFix(
      String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperation[] operations) {
    super(name, compilationUnit, operations);
    this.cu = compilationUnit;
  }

  public CompilationUnit getCu() {
    return cu;
  }

  /* recognizes any simple name referring to a parameter binding */
  public static boolean isComplainingAboutArgument(ASTNode selectedNode) {
    if (!(selectedNode instanceof SimpleName)) return false;
    SimpleName nameNode = (SimpleName) selectedNode;
    IBinding binding = nameNode.resolveBinding();
    if (binding.getKind() == IBinding.VARIABLE && ((IVariableBinding) binding).isParameter())
      return true;
    VariableDeclaration argDecl =
        (VariableDeclaration) ASTNodes.getParent(selectedNode, VariableDeclaration.class);
    if (argDecl != null) binding = argDecl.resolveBinding();
    if (binding.getKind() == IBinding.VARIABLE && ((IVariableBinding) binding).isParameter())
      return true;
    return false;
  }

  /* recognizes the expression of a return statement and the return type of a method declaration. */
  public static boolean isComplainingAboutReturn(ASTNode selectedNode) {
    if (selectedNode.getParent().getNodeType() == ASTNode.RETURN_STATEMENT) return true;
    while (!(selectedNode instanceof Type)) {
      if (selectedNode == null) return false;
      selectedNode = selectedNode.getParent();
    }
    return selectedNode.getLocationInParent() == MethodDeclaration.RETURN_TYPE2_PROPERTY;
  }

  public static NullAnnotationsFix createNullAnnotationInSignatureFix(
      CompilationUnit compilationUnit,
      IProblemLocation problem,
      ChangeKind changeKind,
      boolean isArgumentProblem) {
    String nullableAnnotationName =
        getNullableAnnotationName(compilationUnit.getJavaElement(), false);
    String nonNullAnnotationName =
        getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
    String annotationToAdd = nullableAnnotationName;
    String annotationToRemove = nonNullAnnotationName;

    switch (problem.getProblemId()) {
      case IProblem.IllegalDefinitionToNonNullParameter:
      case IProblem.IllegalRedefinitionToNonNullParameter:
        // case ParameterLackingNullableAnnotation: // never proposed with modifyOverridden
        if (changeKind == ChangeKind.OVERRIDDEN) {
          annotationToAdd = nonNullAnnotationName;
          annotationToRemove = nullableAnnotationName;
        }
        break;
      case IProblem.ParameterLackingNonNullAnnotation:
      case IProblem.IllegalReturnNullityRedefinition:
        if (changeKind != ChangeKind.OVERRIDDEN) {
          annotationToAdd = nonNullAnnotationName;
          annotationToRemove = nullableAnnotationName;
        }
        break;
      case IProblem.RequiredNonNullButProvidedNull:
      case IProblem.RequiredNonNullButProvidedPotentialNull:
      case IProblem.RequiredNonNullButProvidedUnknown:
      case IProblem.RequiredNonNullButProvidedSpecdNullable:
        if (isArgumentProblem == (changeKind != ChangeKind.TARGET)) {
          annotationToAdd = nonNullAnnotationName;
          annotationToRemove = nullableAnnotationName;
        }
        break;
      case IProblem.ConflictingNullAnnotations:
      case IProblem.ConflictingInheritedNullAnnotations:
        if (changeKind == ChangeKind.INVERSE) {
          annotationToAdd = nonNullAnnotationName;
          annotationToRemove = nullableAnnotationName;
        }
        // all others propose to add @Nullable
    }

    // when performing one change at a time we can actually modify another CU than the current one:
    NullAnnotationsRewriteOperations.SignatureAnnotationRewriteOperation operation =
        NullAnnotationsRewriteOperations.createAddAnnotationOperation(
            compilationUnit,
            problem,
            annotationToAdd,
            annotationToRemove,
            null,
            false /*thisUnitOnly*/,
            true /*allowRemove*/,
            isArgumentProblem,
            changeKind);
    if (operation == null) return null;

    if (annotationToAdd == nonNullAnnotationName) {
      operation.fRemoveIfNonNullByDefault = true;
      operation.fNonNullByDefaultName =
          getNonNullByDefaultAnnotationName(compilationUnit.getJavaElement(), false);
    }
    return new NullAnnotationsFix(
        operation.getMessage(),
        operation.getCompilationUnit(), // note that this uses the findings from
        // createAddAnnotationOperation(..)
        new NullAnnotationsRewriteOperations.SignatureAnnotationRewriteOperation[] {operation});
  }

  public static NullAnnotationsFix createRemoveRedundantNullAnnotationsFix(
      CompilationUnit compilationUnit, IProblemLocation problem) {
    RemoveRedundantAnnotationRewriteOperation operation =
        new RemoveRedundantAnnotationRewriteOperation(compilationUnit, problem);
    return new NullAnnotationsFix(
        FixMessages.NullAnnotationsRewriteOperations_remove_redundant_nullness_annotation,
        compilationUnit,
        new RemoveRedundantAnnotationRewriteOperation[] {operation});
  }

  // Entry for NullAnnotationsCleanup:
  public static ICleanUpFix createCleanUp(
      CompilationUnit compilationUnit, IProblemLocation[] locations, int problemID) {
    ICompilationUnit cu = (ICompilationUnit) compilationUnit.getJavaElement();
    if (!JavaModelUtil.is50OrHigher(cu.getJavaProject())) return null;

    List<CompilationUnitRewriteOperation> operations =
        new ArrayList<CompilationUnitRewriteOperation>();
    if (locations == null) {
      org.eclipse.jdt.core.compiler.IProblem[] problems = compilationUnit.getProblems();
      locations = new IProblemLocation[problems.length];
      for (int i = 0; i < problems.length; i++) {
        if (problems[i].getID() == problemID) locations[i] = new ProblemLocation(problems[i]);
      }
    }

    createAddNullAnnotationOperations(compilationUnit, locations, operations);
    createRemoveRedundantNullAnnotationsOperations(compilationUnit, locations, operations);
    if (operations.size() == 0) return null;
    CompilationUnitRewriteOperation[] operationsArray =
        operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
    return new NullAnnotationsFix(
        FixMessages.NullAnnotationsFix_add_annotation_change_name,
        compilationUnit,
        operationsArray);
  }

  private static void createAddNullAnnotationOperations(
      CompilationUnit compilationUnit,
      IProblemLocation[] locations,
      List<CompilationUnitRewriteOperation> result) {
    String nullableAnnotationName =
        getNullableAnnotationName(compilationUnit.getJavaElement(), false);
    String nonNullAnnotationName =
        getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
    Set<String> handledPositions = new HashSet<String>();
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation problem = locations[i];
      if (problem == null) continue; // problem was filtered out by createCleanUp()
      boolean isArgumentProblem =
          isComplainingAboutArgument(problem.getCoveredNode(compilationUnit));
      String annotationToAdd = nullableAnnotationName;
      String annotationToRemove = nonNullAnnotationName;
      // cf. createNullAnnotationInSignatureFix() but changeKind is constantly LOCAL
      switch (problem.getProblemId()) {
        case IProblem.IllegalDefinitionToNonNullParameter:
        case IProblem.IllegalRedefinitionToNonNullParameter:
          break;
        case IProblem.ParameterLackingNonNullAnnotation:
        case IProblem.IllegalReturnNullityRedefinition:
          annotationToAdd = nonNullAnnotationName;
          annotationToRemove = nullableAnnotationName;
          break;
        case IProblem.RequiredNonNullButProvidedNull:
        case IProblem.RequiredNonNullButProvidedPotentialNull:
        case IProblem.RequiredNonNullButProvidedUnknown:
        case IProblem.RequiredNonNullButProvidedSpecdNullable:
          if (isArgumentProblem) {
            annotationToAdd = nonNullAnnotationName;
            annotationToRemove = nullableAnnotationName;
          }
          break;
          // all others propose to add @Nullable
      }
      // when performing multiple changes we can only modify the one CU that the CleanUp
      // infrastructure provides to the operation.
      SignatureAnnotationRewriteOperation fix =
          NullAnnotationsRewriteOperations.createAddAnnotationOperation(
              compilationUnit,
              problem,
              annotationToAdd,
              annotationToRemove,
              handledPositions,
              true /*thisUnitOnly*/,
              false /*allowRemove*/,
              isArgumentProblem,
              ChangeKind.LOCAL);
      if (fix != null) {
        if (annotationToAdd == nonNullAnnotationName) {
          fix.fRemoveIfNonNullByDefault = true;
          fix.fNonNullByDefaultName =
              getNonNullByDefaultAnnotationName(compilationUnit.getJavaElement(), false);
        }
        result.add(fix);
      }
    }
  }

  private static void createRemoveRedundantNullAnnotationsOperations(
      CompilationUnit compilationUnit,
      IProblemLocation[] locations,
      List<CompilationUnitRewriteOperation> result) {
    for (int i = 0; i < locations.length; i++) {
      IProblemLocation problem = locations[i];
      if (problem == null) continue; // problem was filtered out by createCleanUp()

      int problemId = problem.getProblemId();
      if (problemId == IProblem.RedundantNullAnnotation
          || problemId == IProblem.RedundantNullDefaultAnnotationPackage
          || problemId == IProblem.RedundantNullDefaultAnnotationType
          || problemId == IProblem.RedundantNullDefaultAnnotationMethod) {
        RemoveRedundantAnnotationRewriteOperation operation =
            new RemoveRedundantAnnotationRewriteOperation(compilationUnit, problem);
        result.add(operation);
      }
    }
  }

  //	private static boolean isMissingNullAnnotationProblem(int id) {
  //		return id == IProblem.RequiredNonNullButProvidedNull || id ==
  // IProblem.RequiredNonNullButProvidedPotentialNull || id ==
  // IProblem.IllegalReturnNullityRedefinition
  //				|| mayIndicateParameterNullcheck(id);
  //	}
  //
  //	private static boolean mayIndicateParameterNullcheck(int problemId) {
  //		return problemId == IProblem.NonNullLocalVariableComparisonYieldsFalse || problemId ==
  // IProblem.RedundantNullCheckOnNonNullLocalVariable;
  //	}

  /**
   * Tells whether an explicit null annotation exists on the given compilation unit.
   *
   * @param compilationUnit the compilation unit
   * @param offset the offset
   * @return <code>true</code> if the compilation unit has an explicit null annotation
   */
  public static boolean hasExplicitNullAnnotation(ICompilationUnit compilationUnit, int offset) {
    // FIXME(SH): check for existing annotations disabled due to lack of precision:
    //		      should distinguish what is actually annotated (return? param? which?)
    //		try {
    //			IJavaElement problemElement = compilationUnit.getElementAt(offset);
    //			if (problemElement.getElementType() == IJavaElement.METHOD) {
    //				IMethod method = (IMethod) problemElement;
    //				String nullable = getNullableAnnotationName(compilationUnit, true);
    //				String nonnull = getNonNullAnnotationName(compilationUnit, true);
    //				for (IAnnotation annotation : method.getAnnotations()) {
    //					if (   annotation.getElementName().equals(nonnull)
    //						|| annotation.getElementName().equals(nullable))
    //						return true;
    //				}
    //			}
    //		} catch (JavaModelException jme) {
    //			/* nop */
    //		}
    return false;
  }

  public static String getNullableAnnotationName(IJavaElement javaElement, boolean makeSimple) {
    return getAnnotationName(javaElement, makeSimple, JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME);
  }

  public static String getNonNullAnnotationName(IJavaElement javaElement, boolean makeSimple) {
    return getAnnotationName(javaElement, makeSimple, JavaCore.COMPILER_NONNULL_ANNOTATION_NAME);
  }

  public static String getNonNullByDefaultAnnotationName(
      IJavaElement javaElement, boolean makeSimple) {
    return getAnnotationName(
        javaElement, makeSimple, JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME);
  }

  private static String getAnnotationName(
      IJavaElement javaElement, boolean makeSimple, String annotation) {
    String qualifiedName = javaElement.getJavaProject().getOption(annotation, true);
    int lastDot;
    if (makeSimple && qualifiedName != null && (lastDot = qualifiedName.lastIndexOf('.')) != -1)
      return qualifiedName.substring(lastDot + 1);
    return qualifiedName;
  }
}

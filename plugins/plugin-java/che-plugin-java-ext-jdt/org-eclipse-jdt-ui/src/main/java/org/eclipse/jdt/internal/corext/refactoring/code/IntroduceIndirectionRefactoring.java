/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Nikolay Metchev
 * <nikolaymetchev@gmail.com> - [introduce indirection] ClassCastException when introducing
 * indirection on method in generic class - https://bugs.eclipse.org/395231
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceIndirectionDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.rename.RippleMethodFinder2;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor;
import org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor.IncomingMemberVisibilityAdjustment;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * This refactoring creates a wrapper around a certain method and redirects callers of the original
 * method to the newly created method (called "intermediary method").
 *
 * <p>If invoked on a method call, the user may select whether to only update the selected call or
 * all other calls as well. If invoked on a method declaration, the user may select whether to
 * update calls at all. An intermediary method will be created in both cases.
 *
 * <p>Creating indirections is possible for both source and binary methods. Select an invocation of
 * the method or the declaring method itself, for example in the outline view.
 *
 * <p>Note that in case of methods inside generic types, the parameters of the declaring type of the
 * selected method will be added to the method definition, rendering it generic as well.
 *
 * <p>If any of the calls cannot see the intermediary method due to visibility problems with
 * enclosing types of the intermediary method, visibility will be adjusted. If the intermediary
 * method is not able to see the target method, this refactoring will try to adjust the visibility
 * of the target method and enclosing types as well. However, the latter is only possible if the
 * target method is from source.
 *
 * @since 3.2
 */
public class IntroduceIndirectionRefactoring extends Refactoring {

  /** The compilation unit in which the user invoked this refactoring (if any) */
  private ICompilationUnit fSelectionCompilationUnit;
  /** The class file (with source) in which the user invoked this refactoring (if any) */
  private IClassFile fSelectionClassFile;
  /** The start of the user selection inside the selected compilation unit (if any) */
  private int fSelectionStart;
  /** The length of the user selection inside the selected compilation unit (if any) */
  private int fSelectionLength;
  /**
   * The selected MethodInvocation (if any). This field is used to update this particular invocation
   * in non-reference mode.
   */
  private MethodInvocation fSelectionMethodInvocation;

  // Intermediary information:

  /** The type in which to place the intermediary method */
  private IType fIntermediaryType;
  /** The binding of the intermediary type */
  private ITypeBinding fIntermediaryTypeBinding;
  /** The name of the intermediary method */
  private String fIntermediaryMethodName;
  /**
   * The type for the additional parameter for the intermediary. This type is determined from all
   * known references.
   */
  private ITypeBinding fIntermediaryFirstParameterType;

  // Target information:

  /** The originally selected target method (i.e., the one to be encapsulated) */
  private IMethod fTargetMethod;
  /** The binding of the originally selected target method */
  private IMethodBinding fTargetMethodBinding;

  // Other information:

  /** If true, all references to the target method are replaced with calls to the intermediary. */
  private boolean fUpdateReferences;
  /** CompilationUnitRewrites for all affected cus */
  private Map<ICompilationUnit, CompilationUnitRewrite> fRewrites;
  /** Text change manager (actually a CompilationUnitChange manager) which manages all changes. */
  private TextChangeManager fTextChangeManager;

  // Visibility

  /** The visibility adjustor */
  private MemberVisibilityAdjustor fAdjustor;
  /** Visibility adjustments for the intermediary */
  private Map<IMember, IncomingMemberVisibilityAdjustment> fIntermediaryAdjustments;

  private class NoOverrideProgressMonitor extends SubProgressMonitor {

    public NoOverrideProgressMonitor(IProgressMonitor monitor, int ticks) {
      super(monitor, ticks, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
    }

    @Override
    public void setTaskName(String name) {
      // do nothing
    }
  }

  // ********* CONSTRUCTORS AND CLASS CREATION ************

  public IntroduceIndirectionRefactoring(ICompilationUnit unit, int offset, int length) {
    fSelectionCompilationUnit = unit;
    initialize(offset, length);
  }

  public IntroduceIndirectionRefactoring(IClassFile file, int offset, int length) {
    fSelectionClassFile = file;
    initialize(offset, length);
  }

  public IntroduceIndirectionRefactoring(IMethod method) {
    fTargetMethod = method;
    initialize(0, 0);
  }

  public IntroduceIndirectionRefactoring(
      JavaRefactoringArguments arguments, RefactoringStatus status) {
    this((ICompilationUnit) null, 0, 0);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
  }

  private void initialize(int offset, int length) {
    fSelectionStart = offset;
    fSelectionLength = length;
    fUpdateReferences = true;
  }

  // ********* UI INTERACTION AND STARTUP OPTIONS ************

  @Override
  public String getName() {
    return RefactoringCoreMessages.IntroduceIndirectionRefactoring_introduce_indirection_name;
  }

  public IJavaProject getProject() {
    if (fSelectionCompilationUnit != null) return fSelectionCompilationUnit.getJavaProject();
    if (fSelectionClassFile != null) return fSelectionClassFile.getJavaProject();
    if (fTargetMethod != null) return fTargetMethod.getJavaProject();
    return null;
  }

  public IPackageFragment getInvocationPackage() {
    return fSelectionCompilationUnit != null
        ? (IPackageFragment) fSelectionCompilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT)
        : null;
  }

  public boolean canEnableUpdateReferences() {
    return true;
  }

  public void setEnableUpdateReferences(boolean updateReferences) {
    fUpdateReferences = updateReferences;
  }

  public RefactoringStatus setIntermediaryMethodName(String newMethodName) {
    Assert.isNotNull(newMethodName);
    fIntermediaryMethodName = newMethodName;
    IJavaElement context = fIntermediaryType != null ? fIntermediaryType : (IMember) fTargetMethod;
    RefactoringStatus stat = Checks.checkMethodName(newMethodName, context);
    stat.merge(checkOverloading());
    return stat;
  }

  private RefactoringStatus checkOverloading() {
    try {
      if (fIntermediaryType != null) {
        IMethod[] toCheck = fIntermediaryType.getMethods();
        for (int i = 0; i < toCheck.length; i++) {
          IMethod method = toCheck[i];
          if (method.getElementName().equals(fIntermediaryMethodName))
            return RefactoringStatus.createWarningStatus(
                Messages.format(
                    RefactoringCoreMessages
                        .IntroduceIndirectionRefactoring_duplicate_method_name_in_declaring_type_error,
                    BasicElementLabels.getJavaElementName(fIntermediaryMethodName)));
        }
      }
    } catch (JavaModelException e) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages
              .IntroduceIndirectionRefactoring_could_not_parse_declaring_type_error);
    }
    return new RefactoringStatus();
  }

  public String getIntermediaryMethodName() {
    return fIntermediaryMethodName;
  }

  /**
   * @param fullyQualifiedTypeName the fully qualified name of the intermediary method
   * @return status for type name. Use {@link #setIntermediaryMethodName(String)} to check for
   *     overridden methods.
   */
  public RefactoringStatus setIntermediaryTypeName(String fullyQualifiedTypeName) {
    IType target = null;

    try {
      if (fullyQualifiedTypeName.length() == 0)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_type_not_selected_error);

      // find type (now including secondaries)
      target = getProject().findType(fullyQualifiedTypeName, new NullProgressMonitor());
      if (target == null || !target.exists())
        return RefactoringStatus.createErrorStatus(
            Messages.format(
                RefactoringCoreMessages.IntroduceIndirectionRefactoring_type_does_not_exist_error,
                BasicElementLabels.getJavaElementName(fullyQualifiedTypeName)));
      if (target.isAnnotation())
        return RefactoringStatus.createErrorStatus(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_cannot_create_in_annotation);
      if (target.isInterface()
          && !(JavaModelUtil.is18OrHigher(target.getJavaProject())
              && JavaModelUtil.is18OrHigher(getProject())))
        return RefactoringStatus.createErrorStatus(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_cannot_create_on_interface);
    } catch (JavaModelException e) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_unable_determine_declaring_type);
    }

    if (target.isReadOnly())
      return RefactoringStatus.createErrorStatus(
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_cannot_create_in_readonly);

    if (target.isBinary())
      return RefactoringStatus.createErrorStatus(
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_cannot_create_in_binary);

    fIntermediaryType = target;

    return new RefactoringStatus();
  }

  /**
   * Returns the type name of the intermediary type, or the empty string if none has been set yet.
   *
   * @return the intermediary type name or the empty string
   */
  public String getIntermediaryTypeName() {
    return fIntermediaryType != null
        ? fIntermediaryType.getFullyQualifiedName('.')
        : ""; // $NON-NLS-1$
  }

  // ********** CONDITION CHECKING **********

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
      throws CoreException, OperationCanceledException {
    try {
      pm.beginTask(RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_activation, 1);
      fRewrites = new HashMap<ICompilationUnit, CompilationUnitRewrite>();

      // This refactoring has been invoked on
      // (1) a TextSelection inside an ICompilationUnit or inside an IClassFile (definitely with
      // source), or
      // (2) an IMethod inside a ICompilationUnit or inside an IClassFile (with or without source)

      if (fTargetMethod == null) {
        // (1) invoked on a text selection

        if (fSelectionStart == 0)
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_not_available_on_this_selection);

        // if a text selection exists, source is available.
        CompilationUnit selectionCURoot;
        ASTNode selectionNode;
        if (fSelectionCompilationUnit != null) {
          // compilation unit - could use CuRewrite later on
          selectionCURoot = getCachedCURewrite(fSelectionCompilationUnit).getRoot();
          selectionNode =
              getSelectedNode(
                  fSelectionCompilationUnit, selectionCURoot, fSelectionStart, fSelectionLength);
        } else {
          // binary class file - no cu rewrite
          ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
          parser.setResolveBindings(true);
          parser.setSource(fSelectionClassFile);
          selectionCURoot = (CompilationUnit) parser.createAST(null);
          selectionNode = getSelectedNode(null, selectionCURoot, fSelectionStart, fSelectionLength);
        }

        if (selectionNode == null)
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_not_available_on_this_selection);

        IMethodBinding targetMethodBinding = null;

        if (selectionNode.getNodeType() == ASTNode.METHOD_INVOCATION) {
          targetMethodBinding = ((MethodInvocation) selectionNode).resolveMethodBinding();
        } else if (selectionNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
          targetMethodBinding = ((MethodDeclaration) selectionNode).resolveBinding();
        } else if (selectionNode.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
          // Allow invocation on super methods calls. makes sense as other
          // calls or even only the declaration can be updated.
          targetMethodBinding = ((SuperMethodInvocation) selectionNode).resolveMethodBinding();
        } else {
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_not_available_on_this_selection);
        }
        fTargetMethodBinding = targetMethodBinding.getMethodDeclaration(); // resolve generics
        fTargetMethod = (IMethod) fTargetMethodBinding.getJavaElement();

        // allow single updating mode if an invocation was selected and the invocation can be
        // updated
        if (selectionNode instanceof MethodInvocation && fSelectionCompilationUnit != null)
          fSelectionMethodInvocation = (MethodInvocation) selectionNode;

      } else {
        // (2) invoked on an IMethod: Source may not be available

        if (fTargetMethod.getDeclaringType().isAnnotation())
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.IntroduceIndirectionRefactoring_not_available_on_annotation);

        if (fTargetMethod.getCompilationUnit() != null) {
          // source method
          CompilationUnit selectionCURoot =
              getCachedCURewrite(fTargetMethod.getCompilationUnit()).getRoot();
          MethodDeclaration declaration =
              ASTNodeSearchUtil.getMethodDeclarationNode(fTargetMethod, selectionCURoot);
          fTargetMethodBinding = declaration.resolveBinding().getMethodDeclaration();
        } else {
          // binary method - no CURewrite available (and none needed as we cannot update the method
          // anyway)
          ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
          parser.setProject(fTargetMethod.getJavaProject());
          IBinding[] bindings = parser.createBindings(new IJavaElement[] {fTargetMethod}, null);
          fTargetMethodBinding = ((IMethodBinding) bindings[0]).getMethodDeclaration();
        }
      }

      if (fTargetMethod == null
          || fTargetMethodBinding == null
          || (!RefactoringAvailabilityTester.isIntroduceIndirectionAvailable(fTargetMethod)))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages
                .IntroduceIndirectionRefactoring_not_available_on_this_selection);

      if (fTargetMethod.getDeclaringType().isLocal()
          || fTargetMethod.getDeclaringType().isAnonymous())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages
                .IntroduceIndirectionRefactoring_not_available_for_local_or_anonymous_types);

      if (fTargetMethod.isConstructor())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_not_available_for_constructors);

      if (fIntermediaryMethodName == null) fIntermediaryMethodName = fTargetMethod.getElementName();

      if (fIntermediaryType == null) {
        if (fSelectionCompilationUnit != null && !fSelectionCompilationUnit.isReadOnly())
          fIntermediaryType = getEnclosingInitialSelectionMember().getDeclaringType();
        else if (!fTargetMethod.isBinary() && !fTargetMethod.isReadOnly())
          fIntermediaryType = fTargetMethod.getDeclaringType();
      }

      return new RefactoringStatus();
    } finally {
      pm.done();
    }
  }

  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
      throws CoreException, OperationCanceledException {

    RefactoringStatus result = new RefactoringStatus();
    fTextChangeManager = new TextChangeManager();
    fIntermediaryFirstParameterType = null;
    fIntermediaryTypeBinding = null;
    for (Iterator<CompilationUnitRewrite> iter = fRewrites.values().iterator(); iter.hasNext(); )
      iter.next().clearASTAndImportRewrites();

    int startupTicks = 5;
    int hierarchyTicks = 5;
    int visibilityTicks = 5;
    int referenceTicks = fUpdateReferences ? 30 : 5;
    int creationTicks = 5;

    pm.beginTask(
        "",
        startupTicks
            + hierarchyTicks
            + visibilityTicks
            + referenceTicks
            + creationTicks); // $NON-NLS-1$
    pm.setTaskName(RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_conditions);

    result.merge(Checks.checkMethodName(fIntermediaryMethodName, fIntermediaryType));
    if (result.hasFatalError()) return result;

    if (fIntermediaryType == null)
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages
              .IntroduceIndirectionRefactoring_cannot_run_without_intermediary_type);

    // intermediary type is already non binary/non-enum
    CompilationUnitRewrite imRewrite = getCachedCURewrite(fIntermediaryType.getCompilationUnit());
    fIntermediaryTypeBinding = typeToBinding(fIntermediaryType, imRewrite.getRoot());

    fAdjustor = new MemberVisibilityAdjustor(fIntermediaryType, fIntermediaryType);
    fIntermediaryAdjustments = new HashMap<IMember, IncomingMemberVisibilityAdjustment>();

    // check static method in non-static nested type
    if (fIntermediaryTypeBinding.isNested()
        && !Modifier.isStatic(fIntermediaryTypeBinding.getModifiers()))
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_cannot_create_in_nested_nonstatic,
          JavaStatusContext.create(fIntermediaryType));

    pm.worked(startupTicks);
    if (pm.isCanceled()) throw new OperationCanceledException();

    if (fUpdateReferences) {
      pm.setTaskName(
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_conditions
              + " "
              + RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_looking_for_references); // $NON-NLS-1$
      result.merge(updateReferences(new NoOverrideProgressMonitor(pm, referenceTicks)));
      pm.setTaskName(RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_conditions);
    } else {
      // only update the declaration and/or a selected method invocation
      if (fSelectionMethodInvocation != null) {
        fIntermediaryFirstParameterType = getExpressionType(fSelectionMethodInvocation);
        final IMember enclosing = getEnclosingInitialSelectionMember();
        // create an edit for this particular call
        result.merge(
            updateMethodInvocation(
                fSelectionMethodInvocation,
                enclosing,
                getCachedCURewrite(fSelectionCompilationUnit)));

        if (!isRewriteKept(fSelectionCompilationUnit))
          createChangeAndDiscardRewrite(fSelectionCompilationUnit);

        // does call see the intermediary method?
        // => increase visibility of the type of the intermediary method.
        result.merge(
            adjustVisibility(
                fIntermediaryType,
                enclosing.getDeclaringType(),
                new NoOverrideProgressMonitor(pm, 0)));
      }
      pm.worked(referenceTicks);
    }

    if (pm.isCanceled()) throw new OperationCanceledException();

    if (fIntermediaryFirstParameterType == null)
      fIntermediaryFirstParameterType = fTargetMethodBinding.getDeclaringClass();

    // The target type and method may have changed - update them

    IType actualTargetType = (IType) fIntermediaryFirstParameterType.getJavaElement();
    if (!fTargetMethod.getDeclaringType().equals(actualTargetType)) {
      IMethod actualTargetMethod =
          new MethodOverrideTester(actualTargetType, actualTargetType.newSupertypeHierarchy(null))
              .findOverriddenMethodInHierarchy(actualTargetType, fTargetMethod);
      fTargetMethod = actualTargetMethod;
      fTargetMethodBinding =
          findMethodBindingInHierarchy(fIntermediaryFirstParameterType, actualTargetMethod);
      Assert.isNotNull(fTargetMethodBinding);
    }

    result.merge(checkCanCreateIntermediaryMethod());
    createIntermediaryMethod();
    pm.worked(creationTicks);

    pm.setTaskName(
        RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_conditions
            + " "
            + RefactoringCoreMessages
                .IntroduceIndirectionRefactoring_adjusting_visibility); // $NON-NLS-1$
    result.merge(updateTargetVisibility(new NoOverrideProgressMonitor(pm, 0)));
    result.merge(updateIntermediaryVisibility(new NoOverrideProgressMonitor(pm, 0)));
    pm.worked(visibilityTicks);
    pm.setTaskName(RefactoringCoreMessages.IntroduceIndirectionRefactoring_checking_conditions);

    createChangeAndDiscardRewrite(fIntermediaryType.getCompilationUnit());

    result.merge(Checks.validateModifiesFiles(getAllFilesToModify(), getValidationContext()));
    pm.done();

    return result;
  }

  private RefactoringStatus updateTargetVisibility(IProgressMonitor monitor)
      throws JavaModelException, CoreException {

    RefactoringStatus result = new RefactoringStatus();

    // Adjust the visibility of the method and of the referenced type. Note that
    // the target method may not be in the target type; and in this case, the type
    // of the target method does not need a visibility adjustment.

    // This method is called after all other changes have been
    // created. Changes induced by this method will be attached to those changes.

    result.merge(
        adjustVisibility(
            (IType) fIntermediaryFirstParameterType.getJavaElement(), fIntermediaryType, monitor));
    if (result.hasError()) return result; // binary

    ModifierKeyword neededVisibility = getNeededVisibility(fTargetMethod, fIntermediaryType);
    if (neededVisibility != null) {

      result.merge(adjustVisibility(fTargetMethod, neededVisibility, monitor));
      if (result.hasError()) return result; // binary

      // Need to adjust the overridden methods of the target method.
      ITypeHierarchy hierarchy = fTargetMethod.getDeclaringType().newTypeHierarchy(null);
      MethodOverrideTester tester =
          new MethodOverrideTester(fTargetMethod.getDeclaringType(), hierarchy);
      IType[] subtypes = hierarchy.getAllSubtypes(fTargetMethod.getDeclaringType());
      for (int i = 0; i < subtypes.length; i++) {
        IMethod method = tester.findOverridingMethodInType(subtypes[i], fTargetMethod);
        if (method != null && method.exists()) {
          result.merge(adjustVisibility(method, neededVisibility, monitor));
          if (monitor.isCanceled()) throw new OperationCanceledException();

          if (result.hasError()) return result; // binary
        }
      }
    }

    return result;
  }

  private RefactoringStatus updateIntermediaryVisibility(NoOverrideProgressMonitor monitor)
      throws JavaModelException {
    return rewriteVisibility(fIntermediaryAdjustments, fRewrites, monitor);
  }

  private RefactoringStatus updateReferences(IProgressMonitor monitor) throws CoreException {

    RefactoringStatus result = new RefactoringStatus();

    monitor.beginTask("", 90); // $NON-NLS-1$

    if (monitor.isCanceled()) throw new OperationCanceledException();

    IMethod[] ripple =
        RippleMethodFinder2.getRelatedMethods(
            fTargetMethod, false, new NoOverrideProgressMonitor(monitor, 10), null);

    if (monitor.isCanceled()) throw new OperationCanceledException();

    SearchResultGroup[] references =
        Checks.excludeCompilationUnits(
            getReferences(ripple, new NoOverrideProgressMonitor(monitor, 10), result), result);

    if (result.hasFatalError()) return result;

    result.merge(Checks.checkCompileErrorsInAffectedFiles(references));

    if (monitor.isCanceled()) throw new OperationCanceledException();

    int ticksPerCU = references.length == 0 ? 0 : 70 / references.length;

    for (int i = 0; i < references.length; i++) {
      SearchResultGroup group = references[i];
      SearchMatch[] searchResults = group.getSearchResults();
      CompilationUnitRewrite currentCURewrite = getCachedCURewrite(group.getCompilationUnit());

      for (int j = 0; j < searchResults.length; j++) {

        SearchMatch match = searchResults[j];
        if (match.isInsideDocComment()) continue;

        IMember enclosingMember = (IMember) match.getElement();
        ASTNode target =
            getSelectedNode(
                group.getCompilationUnit(),
                currentCURewrite.getRoot(),
                match.getOffset(),
                match.getLength());

        if (target instanceof SuperMethodInvocation) {
          // Cannot retarget calls to super - add a warning
          result.merge(
              createWarningAboutCall(
                  enclosingMember,
                  target,
                  RefactoringCoreMessages
                      .IntroduceIndirectionRefactoring_call_warning_super_keyword));
          continue;
        }

        Assert.isTrue(
            target instanceof MethodInvocation,
            "Element of call should be a MethodInvocation."); // $NON-NLS-1$

        MethodInvocation invocation = (MethodInvocation) target;
        ITypeBinding typeBinding = getExpressionType(invocation);

        if (fIntermediaryFirstParameterType == null) {
          // no highest type yet
          fIntermediaryFirstParameterType = typeBinding.getTypeDeclaration();
        } else {
          // check if current type is higher
          result.merge(findCommonParent(typeBinding.getTypeDeclaration()));
        }

        if (result.hasFatalError()) return result;

        // create an edit for this particular call
        result.merge(updateMethodInvocation(invocation, enclosingMember, currentCURewrite));

        // does call see the intermediary method?
        // => increase visibility of the type of the intermediary method.
        result.merge(
            adjustVisibility(
                fIntermediaryType,
                enclosingMember.getDeclaringType(),
                new NoOverrideProgressMonitor(monitor, 0)));

        if (monitor.isCanceled()) throw new OperationCanceledException();
      }

      if (!isRewriteKept(group.getCompilationUnit()))
        createChangeAndDiscardRewrite(group.getCompilationUnit());

      monitor.worked(ticksPerCU);
    }

    monitor.done();
    return result;
  }

  private RefactoringStatus findCommonParent(ITypeBinding typeBinding) {

    RefactoringStatus status = new RefactoringStatus();

    ITypeBinding highest = fIntermediaryFirstParameterType;
    ITypeBinding current = typeBinding;

    if (current.equals(highest) || Bindings.isSuperType(highest, current))
      // current is the same as highest or highest is already a supertype of current in the same
      // hierarchy => no change
      return status;

    // find lowest common supertype with the method
    // search in bottom-up order
    ITypeBinding[] currentAndSupers = getTypeAndAllSuperTypes(current);
    ITypeBinding[] highestAndSupers = getTypeAndAllSuperTypes(highest);

    ITypeBinding foundBinding = null;
    for (int i1 = 0; i1 < currentAndSupers.length; i1++) {
      for (int i2 = 0; i2 < highestAndSupers.length; i2++) {
        if (highestAndSupers[i2].isEqualTo(currentAndSupers[i1])
            && (Bindings.findMethodInHierarchy(
                    highestAndSupers[i2],
                    fTargetMethodBinding.getName(),
                    fTargetMethodBinding.getParameterTypes())
                != null)) {
          foundBinding = highestAndSupers[i2];
          break;
        }
      }
      if (foundBinding != null) break;
    }

    if (foundBinding != null) {
      fIntermediaryFirstParameterType = foundBinding;
    } else {
      String type1 =
          BasicElementLabels.getJavaElementName(fIntermediaryFirstParameterType.getQualifiedName());
      String type2 = BasicElementLabels.getJavaElementName(current.getQualifiedName());
      status.addFatalError(
          Messages.format(
              RefactoringCoreMessages.IntroduceIndirectionRefactoring_open_hierarchy_error,
              new String[] {type1, type2}));
    }

    return status;
  }

  // ******************** CHANGE CREATION ***********************

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    final Map<String, String> arguments = new HashMap<String, String>();
    String project = null;
    IJavaProject javaProject = fTargetMethod.getJavaProject();
    if (javaProject != null) project = javaProject.getElementName();
    int flags =
        JavaRefactoringDescriptor.JAR_MIGRATION
            | JavaRefactoringDescriptor.JAR_REFACTORING
            | RefactoringDescriptor.STRUCTURAL_CHANGE
            | RefactoringDescriptor.MULTI_CHANGE;
    final IType declaring = fTargetMethod.getDeclaringType();
    try {
      if (declaring.isLocal() || declaring.isAnonymous())
        flags |= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
    } catch (JavaModelException exception) {
      JavaPlugin.log(exception);
    }
    final String description =
        Messages.format(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_descriptor_description_short,
            BasicElementLabels.getJavaElementName(fTargetMethod.getElementName()));
    final String header =
        Messages.format(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_descriptor_description,
            new String[] {
              JavaElementLabels.getTextLabel(fTargetMethod, JavaElementLabels.ALL_FULLY_QUALIFIED),
              JavaElementLabels.getTextLabel(declaring, JavaElementLabels.ALL_FULLY_QUALIFIED)
            });
    final JDTRefactoringDescriptorComment comment =
        new JDTRefactoringDescriptorComment(project, this, header);
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_original_pattern,
            JavaElementLabels.getTextLabel(fTargetMethod, JavaElementLabels.ALL_FULLY_QUALIFIED)));
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_method_pattern,
            BasicElementLabels.getJavaElementName(fIntermediaryMethodName)));
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.IntroduceIndirectionRefactoring_declaring_pattern,
            JavaElementLabels.getTextLabel(
                fIntermediaryType, JavaElementLabels.ALL_FULLY_QUALIFIED)));
    if (fUpdateReferences)
      comment.addSetting(RefactoringCoreMessages.JavaRefactoringDescriptor_update_references);
    final IntroduceIndirectionDescriptor descriptor =
        RefactoringSignatureDescriptorFactory.createIntroduceIndirectionDescriptor(
            project, description, comment.asString(), arguments, flags);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
        JavaRefactoringDescriptorUtil.elementToHandle(project, fTargetMethod));
    arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fIntermediaryMethodName);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1,
        JavaRefactoringDescriptorUtil.elementToHandle(project, fIntermediaryType));
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES,
        Boolean.valueOf(fUpdateReferences).toString());
    return new DynamicValidationRefactoringChange(
        descriptor,
        RefactoringCoreMessages.IntroduceIndirectionRefactoring_introduce_indirection,
        fTextChangeManager.getAllChanges());
  }

  // ******************* CREATE INTERMEDIARY **********************

  /**
   * Checks whether the target method can be created. Note that this can only be done after
   * fDelegateParameterType has been initialized.
   *
   * @return resulting status
   * @throws JavaModelException should not happen
   */
  private RefactoringStatus checkCanCreateIntermediaryMethod() throws JavaModelException {
    // check if method already exists:
    List<ITypeBinding> parameterBindings = new ArrayList<ITypeBinding>();
    if (!isStaticTarget()) parameterBindings.add(fIntermediaryFirstParameterType);
    parameterBindings.addAll(Arrays.asList(fTargetMethodBinding.getParameterTypes()));
    return Checks.checkMethodInType(
        fIntermediaryTypeBinding,
        fIntermediaryMethodName,
        parameterBindings.toArray(new ITypeBinding[parameterBindings.size()]));
  }

  private void createIntermediaryMethod() throws CoreException {

    CompilationUnitRewrite imRewrite = getCachedCURewrite(fIntermediaryType.getCompilationUnit());
    AST ast = imRewrite.getAST();
    MethodDeclaration intermediary = ast.newMethodDeclaration();

    // Intermediary class is non-anonymous
    AbstractTypeDeclaration type =
        (AbstractTypeDeclaration) typeToDeclaration(fIntermediaryType, imRewrite.getRoot());

    // Name
    intermediary.setName(ast.newSimpleName(fIntermediaryMethodName));

    // Flags
    List<IExtendedModifier> modifiers = intermediary.modifiers();
    if (!fIntermediaryType.isInterface()) {
      modifiers.add(imRewrite.getAST().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    }
    modifiers.add(imRewrite.getAST().newModifier(ModifierKeyword.STATIC_KEYWORD));

    // Parameters
    String targetParameterName =
        StubUtility.suggestArgumentName(
            getProject(),
            fIntermediaryFirstParameterType.getName(),
            fTargetMethod.getParameterNames());

    ImportRewriteContext context =
        new ContextSensitiveImportRewriteContext(type, imRewrite.getImportRewrite());
    if (!isStaticTarget()) {
      // Add first param
      SingleVariableDeclaration parameter = imRewrite.getAST().newSingleVariableDeclaration();
      Type t =
          imRewrite
              .getImportRewrite()
              .addImport(fIntermediaryFirstParameterType, imRewrite.getAST(), context);
      if (fIntermediaryFirstParameterType.isGenericType()) {
        ParameterizedType parameterized = imRewrite.getAST().newParameterizedType(t);
        ITypeBinding[] typeParameters = fIntermediaryFirstParameterType.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++)
          parameterized
              .typeArguments()
              .add(imRewrite.getImportRewrite().addImport(typeParameters[i], imRewrite.getAST()));
        t = parameterized;
      }
      parameter.setType(t);
      parameter.setName(imRewrite.getAST().newSimpleName(targetParameterName));
      intermediary.parameters().add(parameter);
    }
    // Add other params
    copyArguments(intermediary, imRewrite);

    // Add type parameters of declaring type (and enclosing types)
    if (!isStaticTarget() && fIntermediaryFirstParameterType.isGenericType())
      addTypeParameters(imRewrite, intermediary.typeParameters(), fIntermediaryFirstParameterType);

    // Add type params of method
    copyTypeParameters(intermediary, imRewrite);

    // Return type
    intermediary.setReturnType2(
        imRewrite.getImportRewrite().addImport(fTargetMethodBinding.getReturnType(), ast, context));

    // Exceptions
    copyExceptions(intermediary, imRewrite);

    // Body
    MethodInvocation invocation = imRewrite.getAST().newMethodInvocation();
    invocation.setName(imRewrite.getAST().newSimpleName(fTargetMethod.getElementName()));
    if (isStaticTarget()) {
      Type importedType =
          imRewrite
              .getImportRewrite()
              .addImport(fTargetMethodBinding.getDeclaringClass(), ast, context);
      invocation.setExpression(ASTNodeFactory.newName(ast, ASTNodes.asString(importedType)));
    } else {
      invocation.setExpression(imRewrite.getAST().newSimpleName(targetParameterName));
    }
    copyInvocationParameters(invocation, ast);
    Statement call = encapsulateInvocation(intermediary, invocation);

    final Block body = imRewrite.getAST().newBlock();
    body.statements().add(call);
    intermediary.setBody(body);

    // method comment
    ICompilationUnit targetCU = imRewrite.getCu();
    if (StubUtility.doAddComments(targetCU.getJavaProject())) {
      String comment =
          CodeGeneration.getMethodComment(
              targetCU,
              getIntermediaryTypeName(),
              intermediary,
              null,
              StubUtility.getLineDelimiterUsed(targetCU));
      if (comment != null) {
        Javadoc javadoc =
            (Javadoc) imRewrite.getASTRewrite().createStringPlaceholder(comment, ASTNode.JAVADOC);
        intermediary.setJavadoc(javadoc);
      }
    }

    // Add the completed method to the intermediary type:
    ChildListPropertyDescriptor typeBodyDeclarationsProperty =
        typeToBodyDeclarationProperty(fIntermediaryType, imRewrite.getRoot());

    ListRewrite bodyDeclarationsListRewrite =
        imRewrite.getASTRewrite().getListRewrite(type, typeBodyDeclarationsProperty);
    bodyDeclarationsListRewrite.insertAt(
        intermediary,
        ASTNodes.getInsertionIndex(intermediary, type.bodyDeclarations()),
        imRewrite.createGroupDescription(
            RefactoringCoreMessages
                .IntroduceIndirectionRefactoring_group_description_create_new_method));
  }

  private void addTypeParameters(
      CompilationUnitRewrite imRewrite, List<TypeParameter> list, ITypeBinding parent) {

    ITypeBinding enclosing = parent.getDeclaringClass();
    if (enclosing != null) addTypeParameters(imRewrite, list, enclosing);

    ITypeBinding[] typeParameters = parent.getTypeParameters();
    for (int i = 0; i < typeParameters.length; i++) {
      TypeParameter ntp = imRewrite.getAST().newTypeParameter();
      ntp.setName(imRewrite.getAST().newSimpleName(typeParameters[i].getName()));
      ITypeBinding[] bounds = typeParameters[i].getTypeBounds();
      for (int j = 0; j < bounds.length; j++)
        if (!"java.lang.Object".equals(bounds[j].getQualifiedName())) // $NON-NLS-1$
        ntp.typeBounds().add(imRewrite.getImportRewrite().addImport(bounds[j], imRewrite.getAST()));
      list.add(ntp);
    }
  }

  private Statement encapsulateInvocation(
      MethodDeclaration declaration, MethodInvocation invocation) {
    final Type type = declaration.getReturnType2();

    if (type == null
        || (type instanceof PrimitiveType
            && PrimitiveType.VOID.equals(((PrimitiveType) type).getPrimitiveTypeCode())))
      return invocation.getAST().newExpressionStatement(invocation);

    ReturnStatement statement = invocation.getAST().newReturnStatement();
    statement.setExpression(invocation);
    return statement;
  }

  private void copyInvocationParameters(MethodInvocation invocation, AST ast)
      throws JavaModelException {
    String[] names = fTargetMethod.getParameterNames();
    for (int i = 0; i < names.length; i++) invocation.arguments().add(ast.newSimpleName(names[i]));
  }

  private void copyArguments(MethodDeclaration intermediary, CompilationUnitRewrite rew)
      throws JavaModelException {
    String[] names = fTargetMethod.getParameterNames();
    ITypeBinding[] types = fTargetMethodBinding.getParameterTypes();
    for (int i = 0; i < names.length; i++) {
      ITypeBinding typeBinding = types[i];
      SingleVariableDeclaration newElement = rew.getAST().newSingleVariableDeclaration();
      newElement.setName(rew.getAST().newSimpleName(names[i]));

      if (i == (names.length - 1) && fTargetMethodBinding.isVarargs()) {
        newElement.setVarargs(true);
        if (typeBinding.isArray()) typeBinding = typeBinding.getComponentType();
      }

      newElement.setType(rew.getImportRewrite().addImport(typeBinding, rew.getAST()));
      intermediary.parameters().add(newElement);
    }
  }

  private void copyTypeParameters(MethodDeclaration intermediary, CompilationUnitRewrite rew) {
    ITypeBinding[] typeParameters = fTargetMethodBinding.getTypeParameters();
    for (int i = 0; i < typeParameters.length; i++) {
      ITypeBinding current = typeParameters[i];

      TypeParameter parameter = rew.getAST().newTypeParameter();
      parameter.setName(rew.getAST().newSimpleName(current.getName()));
      ITypeBinding[] bounds = current.getTypeBounds();
      for (int j = 0; j < bounds.length; j++)
        if (!"java.lang.Object".equals(bounds[j].getQualifiedName())) // $NON-NLS-1$
        parameter.typeBounds().add(rew.getImportRewrite().addImport(bounds[j], rew.getAST()));

      intermediary.typeParameters().add(parameter);
    }
  }

  private void copyExceptions(MethodDeclaration intermediary, CompilationUnitRewrite imRewrite) {
    ITypeBinding[] exceptionTypes = fTargetMethodBinding.getExceptionTypes();
    for (int i = 0; i < exceptionTypes.length; i++) {
      Type exceptionType =
          imRewrite.getImportRewrite().addImport(exceptionTypes[i], imRewrite.getAST());
      intermediary.thrownExceptionTypes().add(exceptionType);
    }
  }

  // ******************* UPDATE CALLS **********************

  private RefactoringStatus updateMethodInvocation(
      MethodInvocation originalInvocation, IMember enclosing, CompilationUnitRewrite unitRewriter)
      throws JavaModelException {

    RefactoringStatus status = new RefactoringStatus();

    // If the method invocation utilizes type arguments, skip this
    // call as the new target method may have additional parameters
    if (originalInvocation.typeArguments().size() > 0)
      return createWarningAboutCall(
          enclosing,
          originalInvocation,
          RefactoringCoreMessages.IntroduceIndirectionRefactoring_call_warning_type_arguments);

    MethodInvocation newInvocation = unitRewriter.getAST().newMethodInvocation();
    List<Expression> newInvocationArgs = newInvocation.arguments();
    List<Expression> originalInvocationArgs = originalInvocation.arguments();

    // static call => always use a qualifier
    String qualifier = unitRewriter.getImportRewrite().addImport(fIntermediaryTypeBinding);
    newInvocation.setExpression(ASTNodeFactory.newName(unitRewriter.getAST(), qualifier));
    newInvocation.setName(unitRewriter.getAST().newSimpleName(getIntermediaryMethodName()));

    final Expression expression = originalInvocation.getExpression();

    if (!isStaticTarget()) {
      // Add the expression as the first parameter
      if (expression == null) {
        // There is no expression for this call. Use a (possibly qualified) "this" expression.
        ThisExpression expr = unitRewriter.getAST().newThisExpression();
        RefactoringStatus qualifierStatus =
            qualifyThisExpression(expr, originalInvocation, enclosing, unitRewriter);
        status.merge(qualifierStatus);
        if (qualifierStatus.hasEntries())
          // warning means don't include this invocation
          return status;
        newInvocationArgs.add(expr);
      } else {
        Expression expressionAsParam =
            (Expression) unitRewriter.getASTRewrite().createMoveTarget(expression);
        newInvocationArgs.add(expressionAsParam);
      }
    } else {
      if (expression != null) {
        // Check if expression is the type name. If not, there may
        // be side effects (e.g. inside methods) -> don't update
        if (!(expression instanceof Name) || ASTNodes.getTypeBinding((Name) expression) == null)
          return createWarningAboutCall(
              enclosing,
              originalInvocation,
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_call_warning_static_expression_access);
      }
    }

    for (int i = 0; i < originalInvocationArgs.size(); i++) {
      Expression originalInvocationArg = originalInvocationArgs.get(i);
      Expression movedArg =
          (Expression) unitRewriter.getASTRewrite().createMoveTarget(originalInvocationArg);
      newInvocationArgs.add(movedArg);
    }

    unitRewriter
        .getASTRewrite()
        .replace(
            originalInvocation,
            newInvocation,
            unitRewriter.createGroupDescription(
                RefactoringCoreMessages
                    .IntroduceIndirectionRefactoring_group_description_replace_call));

    return status;
  }

  /**
   * Attempts to qualify a "this" expression for a method invocation with an appropriate qualifier.
   * The invoked method is analyzed according to the following specs:
   *
   * <p>'this' must be qualified iff method is declared in an enclosing type or a supertype of an
   * enclosing type
   *
   * <p>1) The method is declared somewhere outside of the cu of the invocation 1a) inside a
   * supertype of the current type 1b) inside a supertype of an enclosing type 2) The method is
   * declared inside of the cu of the invocation 2a) inside the type of the invocation 2b) outside
   * the type of the invocation
   *
   * <p>In case of 1a) and 2b), qualify with the enclosing type.
   *
   * @param expr a {@link ThisExpression}
   * @param originalInvocation the original method invocation
   * @param enclosing the enclosing member of the original method invocation
   * @param unitRewriter the rewrite
   * @return resulting status
   */
  private RefactoringStatus qualifyThisExpression(
      ThisExpression expr,
      MethodInvocation originalInvocation,
      IMember enclosing,
      CompilationUnitRewrite unitRewriter) {

    RefactoringStatus status = new RefactoringStatus();

    IMethodBinding methodBinding = originalInvocation.resolveMethodBinding();
    MethodDeclaration methodDeclaration =
        (MethodDeclaration) ASTNodes.findDeclaration(methodBinding, originalInvocation.getRoot());

    ITypeBinding currentTypeBinding = null;
    if (methodDeclaration != null) {
      // Case 1) : Declaring type is inside this cu => use its name if it's declared in an enclosing
      // type
      if (ASTNodes.isParent(originalInvocation, methodDeclaration.getParent()))
        currentTypeBinding = methodBinding.getDeclaringClass();
      else currentTypeBinding = ASTNodes.getEnclosingType(originalInvocation);
    } else {
      // Case 2) : Declaring type is outside of this cu => find subclass in this cu
      ASTNode currentTypeDeclaration = getEnclosingTypeDeclaration(originalInvocation);
      currentTypeBinding = ASTNodes.getEnclosingType(currentTypeDeclaration);
      while (currentTypeDeclaration != null
          && (Bindings.findMethodInHierarchy(
                  currentTypeBinding, methodBinding.getName(), methodBinding.getParameterTypes())
              == null)) {
        currentTypeDeclaration = getEnclosingTypeDeclaration(currentTypeDeclaration.getParent());
        currentTypeBinding = ASTNodes.getEnclosingType(currentTypeDeclaration);
      }
    }

    if (currentTypeBinding == null) {
      status.merge(
          createWarningAboutCall(
              enclosing,
              originalInvocation,
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_call_warning_declaring_type_not_found));
      return status;
    }

    currentTypeBinding = currentTypeBinding.getTypeDeclaration();

    ITypeBinding typeOfCall = ASTNodes.getEnclosingType(originalInvocation);
    if (!typeOfCall.equals(currentTypeBinding)) {
      if (currentTypeBinding.isAnonymous()) {
        // Cannot qualify, see bug 115277
        status.merge(
            createWarningAboutCall(
                enclosing,
                originalInvocation,
                RefactoringCoreMessages
                    .IntroduceIndirectionRefactoring_call_warning_anonymous_cannot_qualify));
      } else {
        expr.setQualifier(unitRewriter.getAST().newSimpleName(currentTypeBinding.getName()));
      }
    } else {
      // do not qualify, only use "this.".
    }

    return status;
  }

  // ********* SMALL HELPERS ********************

  /*
   * Helper method for finding an IMethod inside a binding hierarchy
   */
  private IMethodBinding findMethodBindingInHierarchy(
      ITypeBinding currentTypeBinding, IMethod methodDeclaration) {
    IMethodBinding[] bindings = currentTypeBinding.getDeclaredMethods();
    for (int i = 0; i < bindings.length; i++)
      if (methodDeclaration.equals(bindings[i].getJavaElement())) return bindings[i];

    ITypeBinding superClass = currentTypeBinding.getSuperclass();
    if (superClass != null) {
      IMethodBinding b = findMethodBindingInHierarchy(superClass, methodDeclaration);
      if (b != null) return b;
    }
    ITypeBinding[] interfaces = currentTypeBinding.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      IMethodBinding b = findMethodBindingInHierarchy(interfaces[i], methodDeclaration);
      if (b != null) return b;
    }
    return null;
  }

  /*
   * Helper method for retrieving a *bottom-up* list of super type bindings
   */
  private ITypeBinding[] getTypeAndAllSuperTypes(ITypeBinding type) {
    List<ITypeBinding> result = new ArrayList<ITypeBinding>();
    collectSuperTypes(type, result);
    return result.toArray(new ITypeBinding[result.size()]);
  }

  private void collectSuperTypes(ITypeBinding curr, List<ITypeBinding> list) {
    if (list.add(curr.getTypeDeclaration())) {
      ITypeBinding[] interfaces = curr.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        collectSuperTypes(interfaces[i], list);
      }
      ITypeBinding superClass = curr.getSuperclass();
      if (superClass != null) {
        collectSuperTypes(superClass, list);
      }
    }
  }

  private CompilationUnitRewrite getCachedCURewrite(ICompilationUnit unit) {
    CompilationUnitRewrite rewrite = fRewrites.get(unit);
    if (rewrite == null && fSelectionMethodInvocation != null) {
      CompilationUnit cuNode = ASTResolving.findParentCompilationUnit(fSelectionMethodInvocation);
      if (cuNode != null && cuNode.getJavaElement().equals(unit)) {
        rewrite = new CompilationUnitRewrite(unit, cuNode);
        fRewrites.put(unit, rewrite);
      }
    }
    if (rewrite == null) {
      rewrite = new CompilationUnitRewrite(unit);
      fRewrites.put(unit, rewrite);
    }
    return rewrite;
  }

  private boolean isRewriteKept(ICompilationUnit compilationUnit) {
    return fIntermediaryType.getCompilationUnit().equals(compilationUnit);
  }

  private void createChangeAndDiscardRewrite(ICompilationUnit compilationUnit)
      throws CoreException {
    CompilationUnitRewrite rewrite = fRewrites.get(compilationUnit);
    if (rewrite != null) {
      fTextChangeManager.manage(compilationUnit, rewrite.createChange(true));
      fRewrites.remove(compilationUnit);
    }
  }

  private SearchResultGroup[] getReferences(
      IMethod[] methods, IProgressMonitor pm, RefactoringStatus status) throws CoreException {
    SearchPattern pattern =
        RefactoringSearchEngine.createOrPattern(methods, IJavaSearchConstants.REFERENCES);
    IJavaSearchScope scope = RefactoringScopeFactory.create(fIntermediaryType, false);
    return RefactoringSearchEngine.search(pattern, scope, pm, status);
  }

  private ITypeBinding typeToBinding(IType type, CompilationUnit root) throws JavaModelException {
    ASTNode typeNode = typeToDeclaration(type, root);
    if (type.isAnonymous()) {
      return ((AnonymousClassDeclaration) typeNode).resolveBinding();
    } else {
      return ((AbstractTypeDeclaration) typeNode).resolveBinding();
    }
  }

  private ASTNode typeToDeclaration(IType type, CompilationUnit root) throws JavaModelException {
    Name intermediateName = (Name) NodeFinder.perform(root, type.getNameRange());
    if (type.isAnonymous()) {
      return ASTNodes.getParent(intermediateName, AnonymousClassDeclaration.class);
    } else {
      return ASTNodes.getParent(intermediateName, AbstractTypeDeclaration.class);
    }
  }

  private ASTNode getEnclosingTypeDeclaration(ASTNode node) {
    while (node != null) {
      if (node instanceof AbstractTypeDeclaration) {
        return node;
      } else if (node instanceof AnonymousClassDeclaration) {
        return node;
      }
      node = node.getParent();
    }
    return null;
  }

  private ChildListPropertyDescriptor typeToBodyDeclarationProperty(
      IType type, CompilationUnit root) throws JavaModelException {
    ASTNode typeDeclaration = typeToDeclaration(type, root);
    if (typeDeclaration instanceof AbstractTypeDeclaration)
      return ((AbstractTypeDeclaration) typeDeclaration).getBodyDeclarationsProperty();
    else if (typeDeclaration instanceof AnonymousClassDeclaration)
      return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;

    Assert.isTrue(false);
    return null;
  }

  private RefactoringStatus createWarningAboutCall(
      IMember enclosing, ASTNode concreteNode, String message) {
    String name = JavaElementLabels.getElementLabel(enclosing, JavaElementLabels.ALL_DEFAULT);
    String container =
        JavaElementLabels.getElementLabel(
            enclosing.getDeclaringType(), JavaElementLabels.ALL_FULLY_QUALIFIED);
    return RefactoringStatus.createWarningStatus(
        Messages.format(message, new String[] {name, container}),
        JavaStatusContext.create(enclosing.getCompilationUnit(), concreteNode));
  }

  private ITypeBinding getExpressionType(MethodInvocation invocation) {
    Expression expression = invocation.getExpression();
    ITypeBinding typeBinding = null;
    if (expression == null) {
      typeBinding = invocation.resolveMethodBinding().getDeclaringClass();
    } else {
      typeBinding = expression.resolveTypeBinding();
    }
    if (typeBinding != null && typeBinding.isTypeVariable()) {
      ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
      if (typeBounds.length > 0) {
        for (ITypeBinding typeBound : typeBounds) {
          ITypeBinding expressionType = getExpressionType(invocation, typeBound);
          if (expressionType != null) {
            return expressionType;
          }
        }
        typeBinding = typeBounds[0].getTypeDeclaration();
      } else {
        typeBinding = invocation.getAST().resolveWellKnownType("java.lang.Object"); // $NON-NLS-1$
      }
    }
    Assert.isNotNull(
        typeBinding, "Type binding of target expression may not be null"); // $NON-NLS-1$
    return typeBinding;
  }

  private ITypeBinding getExpressionType(
      final MethodInvocation invocation, ITypeBinding typeBinding) {
    if (typeBinding == null) return null;
    for (IMethodBinding iMethodBinding : typeBinding.getDeclaredMethods()) {
      if (invocation.resolveMethodBinding() == iMethodBinding)
        return typeBinding.getTypeDeclaration();
    }
    ITypeBinding expressionType = getExpressionType(invocation, typeBinding.getSuperclass());
    if (expressionType != null) {
      return expressionType;
    }
    for (ITypeBinding interfaceBinding : typeBinding.getInterfaces()) {
      expressionType = getExpressionType(invocation, interfaceBinding);
      if (expressionType != null) {
        return expressionType;
      }
    }
    return null;
  }

  private IFile[] getAllFilesToModify() {
    List<ICompilationUnit> cus = new ArrayList<ICompilationUnit>();
    cus.addAll(Arrays.asList(fTextChangeManager.getAllCompilationUnits()));
    return ResourceUtil.getFiles(cus.toArray(new ICompilationUnit[cus.size()]));
  }

  private boolean isStaticTarget() throws JavaModelException {
    return Flags.isStatic(fTargetMethod.getFlags());
  }

  private IMember getEnclosingInitialSelectionMember() throws JavaModelException {
    return (IMember) fSelectionCompilationUnit.getElementAt(fSelectionStart);
  }

  private static ASTNode getSelectedNode(
      ICompilationUnit unit, CompilationUnit root, int offset, int length) {
    ASTNode node = null;
    try {
      if (unit != null) node = checkNode(NodeFinder.perform(root, offset, length, unit));
      else node = checkNode(NodeFinder.perform(root, offset, length));
    } catch (JavaModelException e) {
      // Do nothing
    }
    if (node != null) return node;
    return checkNode(NodeFinder.perform(root, offset, length));
  }

  private static ASTNode checkNode(ASTNode node) {
    if (node == null) return null;
    if (node.getNodeType() == ASTNode.SIMPLE_NAME) {
      node = node.getParent();
    } else if (node.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
      node = ((ExpressionStatement) node).getExpression();
    }
    switch (node.getNodeType()) {
      case ASTNode.METHOD_INVOCATION:
      case ASTNode.METHOD_DECLARATION:
      case ASTNode.SUPER_METHOD_INVOCATION:
        return node;
    }
    return null;
  }

  // ***************** VISIBILITY ********************

  private ModifierKeyword getNeededVisibility(IMember whoToAdjust, IMember fromWhereToLook)
      throws JavaModelException {
    return fAdjustor.getVisibilityThreshold(
        fromWhereToLook, whoToAdjust, new NullProgressMonitor());
  }

  private RefactoringStatus adjustVisibility(
      IMember whoToAdjust, IMember fromWhereToLook, IProgressMonitor monitor) throws CoreException {
    return adjustVisibility(
        whoToAdjust, getNeededVisibility(whoToAdjust, fromWhereToLook), true, monitor);
  }

  private RefactoringStatus adjustVisibility(
      IMember whoToAdjust, ModifierKeyword neededVisibility, IProgressMonitor monitor)
      throws CoreException {
    return adjustVisibility(whoToAdjust, neededVisibility, false, monitor);
  }

  private RefactoringStatus adjustVisibility(
      IMember whoToAdjust,
      ModifierKeyword neededVisibility,
      boolean alsoIncreaseEnclosing,
      IProgressMonitor monitor)
      throws CoreException {

    Map<IMember, IncomingMemberVisibilityAdjustment> adjustments;
    if (isRewriteKept(whoToAdjust.getCompilationUnit())) adjustments = fIntermediaryAdjustments;
    else adjustments = new HashMap<IMember, IncomingMemberVisibilityAdjustment>();

    int existingAdjustments = adjustments.size();
    addAdjustment(whoToAdjust, neededVisibility, adjustments);

    if (alsoIncreaseEnclosing)
      while (whoToAdjust.getDeclaringType() != null) {
        whoToAdjust = whoToAdjust.getDeclaringType();
        addAdjustment(whoToAdjust, neededVisibility, adjustments);
      }

    boolean hasNewAdjustments = (adjustments.size() - existingAdjustments) > 0;
    if (hasNewAdjustments && ((whoToAdjust.isReadOnly() || whoToAdjust.isBinary())))
      return RefactoringStatus.createErrorStatus(
          Messages.format(
              RefactoringCoreMessages
                  .IntroduceIndirectionRefactoring_cannot_update_binary_target_visibility,
              new String[] {
                JavaElementLabels.getElementLabel(whoToAdjust, JavaElementLabels.ALL_DEFAULT)
              }),
          JavaStatusContext.create(whoToAdjust));

    RefactoringStatus status = new RefactoringStatus();

    // Don't create a rewrite if it is not necessary
    if (!hasNewAdjustments) return status;

    try {
      monitor.beginTask(RefactoringCoreMessages.MemberVisibilityAdjustor_adjusting, 2);
      Map<ICompilationUnit, CompilationUnitRewrite> rewrites;
      if (!isRewriteKept(whoToAdjust.getCompilationUnit())) {
        CompilationUnitRewrite rewrite =
            new CompilationUnitRewrite(whoToAdjust.getCompilationUnit());
        rewrite.setResolveBindings(false);
        rewrites = new HashMap<ICompilationUnit, CompilationUnitRewrite>();
        rewrites.put(whoToAdjust.getCompilationUnit(), rewrite);
        status.merge(
            rewriteVisibility(
                adjustments,
                rewrites,
                new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
        rewrite.attachChange(
            (CompilationUnitChange) fTextChangeManager.get(whoToAdjust.getCompilationUnit()),
            true,
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      }
    } finally {
      monitor.done();
    }
    return status;
  }

  private RefactoringStatus rewriteVisibility(
      Map<IMember, IncomingMemberVisibilityAdjustment> adjustments,
      Map<ICompilationUnit, CompilationUnitRewrite> rewrites,
      IProgressMonitor monitor)
      throws JavaModelException {
    RefactoringStatus status = new RefactoringStatus();
    fAdjustor.setRewrites(rewrites);
    fAdjustor.setAdjustments(adjustments);
    fAdjustor.setStatus(status);
    fAdjustor.rewriteVisibility(monitor);
    return status;
  }

  private void addAdjustment(
      IMember whoToAdjust,
      ModifierKeyword neededVisibility,
      Map<IMember, IncomingMemberVisibilityAdjustment> adjustments)
      throws JavaModelException {
    ModifierKeyword currentVisibility =
        ModifierKeyword.fromFlagValue(JdtFlags.getVisibilityCode(whoToAdjust));
    if (MemberVisibilityAdjustor.hasLowerVisibility(currentVisibility, neededVisibility)
        && MemberVisibilityAdjustor.needsVisibilityAdjustments(
            whoToAdjust, neededVisibility, adjustments))
      adjustments.put(
          whoToAdjust,
          new MemberVisibilityAdjustor.IncomingMemberVisibilityAdjustment(
              whoToAdjust,
              neededVisibility,
              RefactoringStatus.createWarningStatus(
                  Messages.format(
                      MemberVisibilityAdjustor.getMessage(whoToAdjust),
                      new String[] {
                        MemberVisibilityAdjustor.getLabel(whoToAdjust),
                        MemberVisibilityAdjustor.getLabel(neededVisibility)
                      }),
                  JavaStatusContext.create(whoToAdjust))));
  }

  private RefactoringStatus initialize(JavaRefactoringArguments arguments) {
    String handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
    if (handle != null) {
      final IJavaElement element =
          JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
      if (element == null || !element.exists() || element.getElementType() != IJavaElement.METHOD)
        return JavaRefactoringDescriptorUtil.createInputFatalStatus(
            element, getName(), IJavaRefactorings.INTRODUCE_INDIRECTION);
      else fTargetMethod = (IMethod) element;
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1);
    if (handle != null) {
      final IJavaElement element =
          JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
      if (element == null || !element.exists() || element.getElementType() != IJavaElement.TYPE)
        return JavaRefactoringDescriptorUtil.createInputFatalStatus(
            element, getName(), IJavaRefactorings.INTRODUCE_INDIRECTION);
      else fIntermediaryType = (IType) element;
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1));
    final String references =
        arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES);
    if (references != null) {
      fUpdateReferences = Boolean.valueOf(references).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES));
    final String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
    if (name != null && !"".equals(name)) // $NON-NLS-1$
    return setIntermediaryMethodName(name);
    else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
  }
}

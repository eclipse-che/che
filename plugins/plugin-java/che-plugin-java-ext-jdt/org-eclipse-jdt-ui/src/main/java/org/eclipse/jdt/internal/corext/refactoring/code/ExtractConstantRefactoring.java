/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractConstantDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.Corext;
import org.eclipse.jdt.internal.corext.SourceRangeFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.dom.fragments.ASTFragmentFactory;
import org.eclipse.jdt.internal.corext.dom.fragments.IASTFragment;
import org.eclipse.jdt.internal.corext.dom.fragments.IExpressionFragment;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStringStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatusCodes;
import org.eclipse.jdt.internal.corext.refactoring.rename.RefactoringAnalyzeUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.text.edits.TextEditGroup;

public class ExtractConstantRefactoring extends Refactoring {

  private static final String ATTRIBUTE_REPLACE = "replace"; // $NON-NLS-1$
  private static final String ATTRIBUTE_QUALIFY = "qualify"; // $NON-NLS-1$
  private static final String ATTRIBUTE_VISIBILITY = "visibility"; // $NON-NLS-1$

  private static final String MODIFIER = "static final"; // $NON-NLS-1$

  private static final String KEY_NAME = "name"; // $NON-NLS-1$
  private static final String KEY_TYPE = "type"; // $NON-NLS-1$

  private CompilationUnitRewrite fCuRewrite;
  private int fSelectionStart;
  private int fSelectionLength;
  private ICompilationUnit fCu;

  private IExpressionFragment fSelectedExpression;
  private Type fConstantTypeCache;
  private boolean fReplaceAllOccurrences = true; // default value
  private boolean fQualifyReferencesWithDeclaringClassName = false; // default value

  private String fVisibility = JdtFlags.VISIBILITY_STRING_PRIVATE; // default value
  private boolean fTargetIsInterface = false;
  private String fConstantName;
  private String[] fExcludedVariableNames;

  private boolean fSelectionAllStaticFinal;
  private boolean fAllStaticFinalCheckPerformed = false;

  // Constant Declaration Location
  private BodyDeclaration fToInsertAfter;
  private boolean fInsertFirst;

  private CompilationUnitChange fChange;
  private String[] fGuessedConstNames;

  private LinkedProposalModel fLinkedProposalModel;
  private boolean fCheckResultForCompileProblems;

  /**
   * Creates a new extract constant refactoring
   *
   * @param unit the compilation unit, or <code>null</code> if invoked by scripting
   * @param selectionStart start
   * @param selectionLength length
   */
  public ExtractConstantRefactoring(
      ICompilationUnit unit, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCu = unit;
    fCuRewrite = null;
    fLinkedProposalModel = null;
    fConstantName = ""; // $NON-NLS-1$
    fCheckResultForCompileProblems = true;
  }

  public ExtractConstantRefactoring(
      CompilationUnit astRoot, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    Assert.isTrue(astRoot.getTypeRoot() instanceof ICompilationUnit);

    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCu = (ICompilationUnit) astRoot.getTypeRoot();
    fCuRewrite = new CompilationUnitRewrite(fCu, astRoot);
    fLinkedProposalModel = null;
    fConstantName = ""; // $NON-NLS-1$
    fCheckResultForCompileProblems = true;
  }

  public ExtractConstantRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
    this((ICompilationUnit) null, 0, 0);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
  }

  public void setCheckResultForCompileProblems(boolean checkResultForCompileProblems) {
    fCheckResultForCompileProblems = checkResultForCompileProblems;
  }

  public void setLinkedProposalModel(LinkedProposalModel linkedProposalModel) {
    fLinkedProposalModel = linkedProposalModel;
  }

  @Override
  public String getName() {
    return RefactoringCoreMessages.ExtractConstantRefactoring_name;
  }

  public boolean replaceAllOccurrences() {
    return fReplaceAllOccurrences;
  }

  public void setReplaceAllOccurrences(boolean replaceAllOccurrences) {
    fReplaceAllOccurrences = replaceAllOccurrences;
  }

  public void setVisibility(String am) {
    Assert.isTrue(
        am == JdtFlags.VISIBILITY_STRING_PRIVATE
            || am == JdtFlags.VISIBILITY_STRING_PROTECTED
            || am == JdtFlags.VISIBILITY_STRING_PACKAGE
            || am == JdtFlags.VISIBILITY_STRING_PUBLIC);
    fVisibility = am;
  }

  public String getVisibility() {
    return fVisibility;
  }

  public boolean getTargetIsInterface() {
    return fTargetIsInterface;
  }

  public boolean qualifyReferencesWithDeclaringClassName() {
    return fQualifyReferencesWithDeclaringClassName;
  }

  public void setQualifyReferencesWithDeclaringClassName(boolean qualify) {
    fQualifyReferencesWithDeclaringClassName = qualify;
  }

  public String guessConstantName() {
    String[] proposals = guessConstantNames();
    if (proposals.length > 0) return proposals[0];
    else return fConstantName;
  }

  /**
   * @return proposed variable names (may be empty, but not null). The first proposal should be used
   *     as "best guess" (if it exists).
   */
  public String[] guessConstantNames() {
    if (fGuessedConstNames == null) {
      try {
        Expression expression = getSelectedExpression().getAssociatedExpression();
        if (expression != null) {
          ITypeBinding binding = guessBindingForReference(expression);
          fGuessedConstNames =
              StubUtility.getVariableNameSuggestions(
                  NamingConventions.VK_STATIC_FINAL_FIELD,
                  fCu.getJavaProject(),
                  binding,
                  expression,
                  Arrays.asList(getExcludedVariableNames()));
        }
      } catch (JavaModelException e) {
      }
      if (fGuessedConstNames == null) fGuessedConstNames = new String[0];
    }
    return fGuessedConstNames;
  }

  private String[] getExcludedVariableNames() {
    if (fExcludedVariableNames == null) {
      try {
        IExpressionFragment expr = getSelectedExpression();
        Collection<String> takenNames =
            new ScopeAnalyzer(fCuRewrite.getRoot())
                .getUsedVariableNames(expr.getStartPosition(), expr.getLength());
        fExcludedVariableNames = takenNames.toArray(new String[takenNames.size()]);
      } catch (JavaModelException e) {
        fExcludedVariableNames = new String[0];
      }
    }
    return fExcludedVariableNames;
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask("", 7); // $NON-NLS-1$

      RefactoringStatus result = Checks.validateEdit(fCu, getValidationContext());
      if (result.hasFatalError()) return result;
      pm.worked(1);

      if (fCuRewrite == null) {
        CompilationUnit cuNode =
            RefactoringASTParser.parseWithASTProvider(fCu, true, new SubProgressMonitor(pm, 3));
        fCuRewrite = new CompilationUnitRewrite(fCu, cuNode);
      } else {
        pm.worked(3);
      }
      result.merge(checkSelection(new SubProgressMonitor(pm, 3)));

      if (result.hasFatalError()) return result;

      if (isLiteralNodeSelected()) fReplaceAllOccurrences = false;

      if (isInTypeDeclarationAnnotation(getSelectedExpression().getAssociatedNode())) {
        fVisibility = JdtFlags.VISIBILITY_STRING_PACKAGE;
      }

      ITypeBinding targetType = getContainingTypeBinding();
      if (targetType.isInterface()) {
        fTargetIsInterface = true;
        fVisibility = JdtFlags.VISIBILITY_STRING_PUBLIC;
      }

      return result;
    } finally {
      pm.done();
    }
  }

  public boolean selectionAllStaticFinal() {
    Assert.isTrue(fAllStaticFinalCheckPerformed);
    return fSelectionAllStaticFinal;
  }

  private void checkAllStaticFinal() throws JavaModelException {
    fSelectionAllStaticFinal = ConstantChecks.isStaticFinalConstant(getSelectedExpression());
    fAllStaticFinalCheckPerformed = true;
  }

  private RefactoringStatus checkSelection(IProgressMonitor pm) throws JavaModelException {
    try {
      pm.beginTask("", 2); // $NON-NLS-1$

      IExpressionFragment selectedExpression = getSelectedExpression();

      if (selectedExpression == null) {
        String message = RefactoringCoreMessages.ExtractConstantRefactoring_select_expression;
        return CodeRefactoringUtil.checkMethodSyntaxErrors(
            fSelectionStart, fSelectionLength, fCuRewrite.getRoot(), message);
      }
      pm.worked(1);

      RefactoringStatus result = new RefactoringStatus();
      result.merge(checkExpression());
      if (result.hasFatalError()) return result;
      pm.worked(1);

      return result;
    } finally {
      pm.done();
    }
  }

  private RefactoringStatus checkExpressionBinding() throws JavaModelException {
    return checkExpressionFragmentIsRValue();
  }

  private RefactoringStatus checkExpressionFragmentIsRValue() throws JavaModelException {
    /* Moved this functionality to Checks, to allow sharing with
    ExtractTempRefactoring, others */
    switch (Checks.checkExpressionIsRValue(getSelectedExpression().getAssociatedExpression())) {
      case Checks.NOT_RVALUE_MISC:
        return RefactoringStatus.createStatus(
            RefactoringStatus.FATAL,
            RefactoringCoreMessages.ExtractConstantRefactoring_select_expression,
            null,
            Corext.getPluginId(),
            RefactoringStatusCodes.EXPRESSION_NOT_RVALUE,
            null);
      case Checks.NOT_RVALUE_VOID:
        return RefactoringStatus.createStatus(
            RefactoringStatus.FATAL,
            RefactoringCoreMessages.ExtractConstantRefactoring_no_void,
            null,
            Corext.getPluginId(),
            RefactoringStatusCodes.EXPRESSION_NOT_RVALUE_VOID,
            null);

      case Checks.IS_RVALUE_GUESSED:
      case Checks.IS_RVALUE:
        return new RefactoringStatus();
      default:
        Assert.isTrue(false);
        return null;
    }
  }

  private ITypeBinding guessBindingForReference(Expression expression) {
    ITypeBinding binding = expression.resolveTypeBinding();
    if (binding == null) {
      binding = ASTResolving.guessBindingForReference(expression);
    }
    return binding;
  }

  //	 !!! -- same as in ExtractTempRefactoring
  private boolean isLiteralNodeSelected() throws JavaModelException {
    IExpressionFragment fragment = getSelectedExpression();
    if (fragment == null) return false;
    Expression expression = fragment.getAssociatedExpression();
    if (expression == null) return false;
    switch (expression.getNodeType()) {
      case ASTNode.BOOLEAN_LITERAL:
      case ASTNode.CHARACTER_LITERAL:
      case ASTNode.NULL_LITERAL:
      case ASTNode.NUMBER_LITERAL:
        return true;

      default:
        return false;
    }
  }

  private RefactoringStatus checkExpression() throws JavaModelException {
    RefactoringStatus result = new RefactoringStatus();
    result.merge(checkExpressionBinding());
    if (result.hasFatalError()) return result;
    checkAllStaticFinal();

    IExpressionFragment selectedExpression = getSelectedExpression();
    Expression associatedExpression = selectedExpression.getAssociatedExpression();
    if (associatedExpression instanceof NullLiteral)
      result.merge(
          RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ExtractConstantRefactoring_null_literals));
    else if (!ConstantChecks.isLoadTimeConstant(selectedExpression))
      result.merge(
          RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ExtractConstantRefactoring_not_load_time_constant));
    else if (associatedExpression instanceof SimpleName) {
      if (associatedExpression.getParent() instanceof QualifiedName
              && associatedExpression.getLocationInParent() == QualifiedName.NAME_PROPERTY
          || associatedExpression.getParent() instanceof FieldAccess
              && associatedExpression.getLocationInParent() == FieldAccess.NAME_PROPERTY)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractConstantRefactoring_select_expression);
    }

    return result;
  }

  public void setConstantName(String newName) {
    Assert.isNotNull(newName);
    fConstantName = newName;
  }

  public String getConstantName() {
    return fConstantName;
  }

  /**
   * This method performs checks on the constant name which are quick enough to be performed every
   * time the ui input component contents are changed.
   *
   * @return return the resulting status
   * @throws JavaModelException thrown when the operation could not be executed
   */
  public RefactoringStatus checkConstantNameOnChange() throws JavaModelException {
    if (Arrays.asList(getExcludedVariableNames()).contains(fConstantName))
      return RefactoringStatus.createErrorStatus(
          Messages.format(
              RefactoringCoreMessages.ExtractConstantRefactoring_another_variable,
              BasicElementLabels.getJavaElementName(getConstantName())));
    return Checks.checkConstantName(fConstantName, fCu);
  }

  // !! similar to ExtractTempRefactoring equivalent
  public String getConstantSignaturePreview() throws JavaModelException {
    String space = " "; // $NON-NLS-1$
    return getVisibility()
        + space
        + MODIFIER
        + space
        + getConstantTypeName()
        + space
        + fConstantName;
  }

  public CompilationUnitChange createTextChange(IProgressMonitor pm) throws CoreException {
    createConstantDeclaration();
    replaceExpressionsWithConstant();
    return fCuRewrite.createChange(
        RefactoringCoreMessages.ExtractConstantRefactoring_change_name, true, pm);
  }

  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    pm.beginTask(RefactoringCoreMessages.ExtractConstantRefactoring_checking_preconditions, 2);

    /* Note: some checks are performed on change of input widget
     * values. (e.g. see ExtractConstantRefactoring.checkConstantNameOnChange())
     */

    // TODO: possibly add more checking for name conflicts that might
    //      lead to a change in behavior

    try {
      RefactoringStatus result = new RefactoringStatus();

      createConstantDeclaration();
      replaceExpressionsWithConstant();
      fChange =
          fCuRewrite.createChange(
              RefactoringCoreMessages.ExtractConstantRefactoring_change_name,
              true,
              new SubProgressMonitor(pm, 1));

      if (fCheckResultForCompileProblems) {
        checkSource(new SubProgressMonitor(pm, 1), result);
      }
      return result;
    } finally {
      fConstantTypeCache = null;
      fCuRewrite.clearASTAndImportRewrites();
      pm.done();
    }
  }

  private void checkSource(SubProgressMonitor monitor, RefactoringStatus result)
      throws CoreException {
    String newCuSource = fChange.getPreviewContent(new NullProgressMonitor());
    CompilationUnit newCUNode =
        new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL)
            .parse(newCuSource, fCu, true, true, monitor);

    IProblem[] newProblems =
        RefactoringAnalyzeUtil.getIntroducedCompileProblems(newCUNode, fCuRewrite.getRoot());
    for (int i = 0; i < newProblems.length; i++) {
      IProblem problem = newProblems[i];
      if (problem.isError())
        result.addEntry(
            new RefactoringStatusEntry(
                (problem.isError() ? RefactoringStatus.ERROR : RefactoringStatus.WARNING),
                problem.getMessage(),
                new JavaStringStatusContext(newCuSource, SourceRangeFactory.create(problem))));
    }
  }

  private void createConstantDeclaration() throws CoreException {
    Type type = getConstantType();

    IExpressionFragment fragment = getSelectedExpression();
    Expression initializer =
        getSelectedExpression().createCopyTarget(fCuRewrite.getASTRewrite(), true);

    AST ast = fCuRewrite.getAST();
    VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
    variableDeclarationFragment.setName(ast.newSimpleName(fConstantName));
    variableDeclarationFragment.setInitializer(initializer);

    FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
    fieldDeclaration.setType(type);
    Modifier.ModifierKeyword accessModifier = Modifier.ModifierKeyword.toKeyword(fVisibility);
    if (accessModifier != null) fieldDeclaration.modifiers().add(ast.newModifier(accessModifier));
    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));

    boolean createComments =
        JavaPreferencesSettings.getCodeGenerationSettings(fCu.getJavaProject()).createComments;
    if (createComments) {
      String comment =
          CodeGeneration.getFieldComment(
              fCu, getConstantTypeName(), fConstantName, StubUtility.getLineDelimiterUsed(fCu));
      if (comment != null && comment.length() > 0) {
        Javadoc doc =
            (Javadoc) fCuRewrite.getASTRewrite().createStringPlaceholder(comment, ASTNode.JAVADOC);
        fieldDeclaration.setJavadoc(doc);
      }
    }

    AbstractTypeDeclaration parent = getContainingTypeDeclarationNode();
    ListRewrite listRewrite =
        fCuRewrite.getASTRewrite().getListRewrite(parent, parent.getBodyDeclarationsProperty());
    TextEditGroup msg =
        fCuRewrite.createGroupDescription(
            RefactoringCoreMessages.ExtractConstantRefactoring_declare_constant);
    if (insertFirst()) {
      listRewrite.insertFirst(fieldDeclaration, msg);
    } else {
      listRewrite.insertAfter(fieldDeclaration, getNodeToInsertConstantDeclarationAfter(), msg);
    }

    if (fLinkedProposalModel != null) {
      ASTRewrite rewrite = fCuRewrite.getASTRewrite();
      LinkedProposalPositionGroup nameGroup = fLinkedProposalModel.getPositionGroup(KEY_NAME, true);
      nameGroup.addPosition(rewrite.track(variableDeclarationFragment.getName()), true);

      String[] nameSuggestions = guessConstantNames();
      if (nameSuggestions.length > 0 && !nameSuggestions[0].equals(fConstantName)) {
        nameGroup.addProposal(fConstantName, null, nameSuggestions.length + 1);
      }
      for (int i = 0; i < nameSuggestions.length; i++) {
        nameGroup.addProposal(nameSuggestions[i], null, nameSuggestions.length - i);
      }

      LinkedProposalPositionGroup typeGroup = fLinkedProposalModel.getPositionGroup(KEY_TYPE, true);
      typeGroup.addPosition(rewrite.track(type), true);

      ITypeBinding typeBinding = guessBindingForReference(fragment.getAssociatedExpression());
      if (typeBinding != null) {
        ITypeBinding[] relaxingTypes = ASTResolving.getNarrowingTypes(ast, typeBinding);
        for (int i = 0; i < relaxingTypes.length; i++) {
          typeGroup.addProposal(relaxingTypes[i], fCuRewrite.getCu(), relaxingTypes.length - i);
        }
      }
      boolean isInterface =
          parent.resolveBinding() != null && parent.resolveBinding().isInterface();
      ModifierCorrectionSubProcessor.installLinkedVisibilityProposals(
          fLinkedProposalModel, rewrite, fieldDeclaration.modifiers(), isInterface);
    }
  }

  private Type getConstantType() throws JavaModelException {
    if (fConstantTypeCache == null) {
      IExpressionFragment fragment = getSelectedExpression();
      ITypeBinding typeBinding = guessBindingForReference(fragment.getAssociatedExpression());
      AST ast = fCuRewrite.getAST();
      typeBinding = Bindings.normalizeForDeclarationUse(typeBinding, ast);
      ImportRewrite importRewrite = fCuRewrite.getImportRewrite();
      ImportRewriteContext context =
          new ContextSensitiveImportRewriteContext(
              fCuRewrite.getRoot(), fSelectionStart, importRewrite);
      fConstantTypeCache = importRewrite.addImport(typeBinding, ast, context);
    }
    return fConstantTypeCache;
  }

  @Override
  public Change createChange(IProgressMonitor monitor) throws CoreException {
    ExtractConstantDescriptor descriptor = createRefactoringDescriptor();
    fChange.setDescriptor(new RefactoringChangeDescriptor(descriptor));
    return fChange;
  }

  private ExtractConstantDescriptor createRefactoringDescriptor() {
    final Map<String, String> arguments = new HashMap<String, String>();
    String project = null;
    IJavaProject javaProject = fCu.getJavaProject();
    if (javaProject != null) project = javaProject.getElementName();
    int flags =
        JavaRefactoringDescriptor.JAR_REFACTORING | JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
    if (JdtFlags.getVisibilityCode(fVisibility) != Modifier.PRIVATE)
      flags |= RefactoringDescriptor.STRUCTURAL_CHANGE;

    final String expression = ASTNodes.asString(fSelectedExpression.getAssociatedExpression());
    final String description =
        Messages.format(
            RefactoringCoreMessages.ExtractConstantRefactoring_descriptor_description_short,
            BasicElementLabels.getJavaElementName(fConstantName));
    final String header =
        Messages.format(
            RefactoringCoreMessages.ExtractConstantRefactoring_descriptor_description,
            new String[] {
              BasicElementLabels.getJavaElementName(fConstantName),
              BasicElementLabels.getJavaCodeString(expression)
            });
    final JDTRefactoringDescriptorComment comment =
        new JDTRefactoringDescriptorComment(project, this, header);
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ExtractConstantRefactoring_constant_name_pattern,
            BasicElementLabels.getJavaElementName(fConstantName)));
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ExtractConstantRefactoring_constant_expression_pattern,
            BasicElementLabels.getJavaCodeString(expression)));
    String visibility = fVisibility;
    if ("".equals(visibility)) // $NON-NLS-1$
    visibility = RefactoringCoreMessages.ExtractConstantRefactoring_default_visibility;
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ExtractConstantRefactoring_visibility_pattern, visibility));
    if (fReplaceAllOccurrences)
      comment.addSetting(RefactoringCoreMessages.ExtractConstantRefactoring_replace_occurrences);
    if (fQualifyReferencesWithDeclaringClassName)
      comment.addSetting(RefactoringCoreMessages.ExtractConstantRefactoring_qualify_references);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
        JavaRefactoringDescriptorUtil.elementToHandle(project, fCu));
    arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fConstantName);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION,
        new Integer(fSelectionStart).toString()
            + " "
            + new Integer(fSelectionLength).toString()); // $NON-NLS-1$
    arguments.put(ATTRIBUTE_REPLACE, Boolean.valueOf(fReplaceAllOccurrences).toString());
    arguments.put(
        ATTRIBUTE_QUALIFY, Boolean.valueOf(fQualifyReferencesWithDeclaringClassName).toString());
    arguments.put(
        ATTRIBUTE_VISIBILITY, new Integer(JdtFlags.getVisibilityCode(fVisibility)).toString());

    ExtractConstantDescriptor descriptor =
        RefactoringSignatureDescriptorFactory.createExtractConstantDescriptor(
            project, description, comment.asString(), arguments, flags);
    return descriptor;
  }

  private void replaceExpressionsWithConstant() throws JavaModelException {
    ASTRewrite astRewrite = fCuRewrite.getASTRewrite();
    AST ast = astRewrite.getAST();

    IASTFragment[] fragmentsToReplace = getFragmentsToReplace();
    for (int i = 0; i < fragmentsToReplace.length; i++) {
      IASTFragment fragment = fragmentsToReplace[i];
      ASTNode node = fragment.getAssociatedNode();
      boolean inTypeDeclarationAnnotation = isInTypeDeclarationAnnotation(node);
      if (inTypeDeclarationAnnotation && JdtFlags.VISIBILITY_STRING_PRIVATE == getVisibility())
        continue;

      SimpleName ref = ast.newSimpleName(fConstantName);
      Name replacement = ref;
      boolean qualifyReference = qualifyReferencesWithDeclaringClassName();
      if (!qualifyReference) {
        qualifyReference = inTypeDeclarationAnnotation;
      }
      if (qualifyReference) {
        replacement =
            ast.newQualifiedName(ast.newSimpleName(getContainingTypeBinding().getName()), ref);
      }
      TextEditGroup description =
          fCuRewrite.createGroupDescription(
              RefactoringCoreMessages.ExtractConstantRefactoring_replace);

      fragment.replace(astRewrite, replacement, description);
      if (fLinkedProposalModel != null)
        fLinkedProposalModel
            .getPositionGroup(KEY_NAME, true)
            .addPosition(astRewrite.track(ref), false);
    }
  }

  private boolean isInTypeDeclarationAnnotation(ASTNode node) throws JavaModelException {
    ASTNode enclosingAnnotation = ASTNodes.getParent(node, Annotation.class);
    return enclosingAnnotation != null
        && enclosingAnnotation.getParent() == getContainingTypeDeclarationNode();
  }

  private void computeConstantDeclarationLocation() throws JavaModelException {
    if (isDeclarationLocationComputed()) return;

    BodyDeclaration lastStaticDependency = null;
    Iterator<BodyDeclaration> decls =
        getContainingTypeDeclarationNode().bodyDeclarations().iterator();

    while (decls.hasNext()) {
      BodyDeclaration decl = decls.next();

      int modifiers;
      if (decl instanceof FieldDeclaration) modifiers = ((FieldDeclaration) decl).getModifiers();
      else if (decl instanceof Initializer) modifiers = ((Initializer) decl).getModifiers();
      else {
        continue; /* this declaration is not a field declaration
                  or initializer, so the placement of the constant
                  declaration relative to it does not matter */
      }

      if (Modifier.isStatic(modifiers) && depends(getSelectedExpression(), decl))
        lastStaticDependency = decl;
    }

    if (lastStaticDependency == null) fInsertFirst = true;
    else fToInsertAfter = lastStaticDependency;
  }

  /* bd is a static field declaration or static initializer */
  private static boolean depends(IExpressionFragment selected, BodyDeclaration bd) {
    /* We currently consider selected to depend on bd only if db includes a declaration
     * of a static field on which selected depends.
     *
     * A more accurate strategy might be to also check if bd contains (or is) a
     * static initializer containing code which changes the value of a static field on
     * which selected depends.  However, if a static is written to multiple times within
     * during class initialization, it is difficult to predict which value should be used.
     * This would depend on which value is used by expressions instances for which the new
     * constant will be substituted, and there may be many of these; in each, the
     * static field in question may have taken on a different value (if some of these uses
     * occur within static initializers).
     */

    if (bd instanceof FieldDeclaration) {
      FieldDeclaration fieldDecl = (FieldDeclaration) bd;
      for (Iterator<VariableDeclarationFragment> fragments = fieldDecl.fragments().iterator();
          fragments.hasNext(); ) {
        VariableDeclarationFragment fragment = fragments.next();
        SimpleName staticFieldName = fragment.getName();
        if (selected.getSubFragmentsMatching(
                    ASTFragmentFactory.createFragmentForFullSubtree(staticFieldName))
                .length
            != 0) return true;
      }
    }
    return false;
  }

  private boolean isDeclarationLocationComputed() {
    return fInsertFirst == true || fToInsertAfter != null;
  }

  private boolean insertFirst() throws JavaModelException {
    if (!isDeclarationLocationComputed()) computeConstantDeclarationLocation();
    return fInsertFirst;
  }

  private BodyDeclaration getNodeToInsertConstantDeclarationAfter() throws JavaModelException {
    if (!isDeclarationLocationComputed()) computeConstantDeclarationLocation();
    return fToInsertAfter;
  }

  private String getConstantTypeName() throws JavaModelException {
    return ASTNodes.asString(getConstantType());
  }

  private static boolean isStaticFieldOrStaticInitializer(BodyDeclaration node) {
    if (node instanceof MethodDeclaration || node instanceof AbstractTypeDeclaration) return false;

    int modifiers;
    if (node instanceof FieldDeclaration) {
      modifiers = ((FieldDeclaration) node).getModifiers();
    } else if (node instanceof Initializer) {
      modifiers = ((Initializer) node).getModifiers();
    } else {
      Assert.isTrue(false);
      return false;
    }

    if (!Modifier.isStatic(modifiers)) return false;

    return true;
  }

  /*
   * Elements returned by next() are BodyDeclaration or Annotation instances.
   */
  private Iterator<ASTNode> getReplacementScope() throws JavaModelException {
    boolean declPredecessorReached = false;

    Collection<ASTNode> scope = new ArrayList<ASTNode>();

    AbstractTypeDeclaration containingType = getContainingTypeDeclarationNode();
    if (containingType instanceof EnumDeclaration) {
      // replace in all enum constants bodies
      EnumDeclaration enumDeclaration = (EnumDeclaration) containingType;
      scope.addAll(enumDeclaration.enumConstants());
    }

    for (Iterator<IExtendedModifier> iter = containingType.modifiers().iterator();
        iter.hasNext(); ) {
      IExtendedModifier modifier = iter.next();
      if (modifier instanceof Annotation) {
        scope.add((ASTNode) modifier);
      }
    }

    for (Iterator<BodyDeclaration> bodyDeclarations = containingType.bodyDeclarations().iterator();
        bodyDeclarations.hasNext(); ) {
      BodyDeclaration bodyDeclaration = bodyDeclarations.next();

      if (bodyDeclaration == getNodeToInsertConstantDeclarationAfter())
        declPredecessorReached = true;

      if (insertFirst()
          || declPredecessorReached
          || !isStaticFieldOrStaticInitializer(bodyDeclaration)) scope.add(bodyDeclaration);
    }
    return scope.iterator();
  }

  private IASTFragment[] getFragmentsToReplace() throws JavaModelException {
    List<IASTFragment> toReplace = new ArrayList<IASTFragment>();
    if (fReplaceAllOccurrences) {
      Iterator<ASTNode> replacementScope = getReplacementScope();
      while (replacementScope.hasNext()) {
        ASTNode scope = replacementScope.next();
        IASTFragment[] allMatches =
            ASTFragmentFactory.createFragmentForFullSubtree(scope)
                .getSubFragmentsMatching(getSelectedExpression());
        IASTFragment[] replaceableMatches = retainOnlyReplacableMatches(allMatches);
        for (int i = 0; i < replaceableMatches.length; i++) toReplace.add(replaceableMatches[i]);
      }
    } else if (canReplace(getSelectedExpression())) toReplace.add(getSelectedExpression());
    return toReplace.toArray(new IASTFragment[toReplace.size()]);
  }

  // !! - like one in ExtractTempRefactoring
  private static IASTFragment[] retainOnlyReplacableMatches(IASTFragment[] allMatches) {
    List<IASTFragment> result = new ArrayList<IASTFragment>(allMatches.length);
    for (int i = 0; i < allMatches.length; i++) {
      if (canReplace(allMatches[i])) result.add(allMatches[i]);
    }
    return result.toArray(new IASTFragment[result.size()]);
  }

  // !! - like one in ExtractTempRefactoring
  private static boolean canReplace(IASTFragment fragment) {
    ASTNode node = fragment.getAssociatedNode();
    ASTNode parent = node.getParent();
    if (parent instanceof VariableDeclarationFragment) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) parent;
      if (node.equals(vdf.getName())) return false;
    }
    if (parent instanceof ExpressionStatement) return false;
    if (parent instanceof SwitchCase) {
      if (node instanceof Name) {
        Name name = (Name) node;
        ITypeBinding typeBinding = name.resolveTypeBinding();
        if (typeBinding != null) {
          return !typeBinding.isEnum();
        }
      }
    }
    return true;
  }

  private IExpressionFragment getSelectedExpression() throws JavaModelException {
    if (fSelectedExpression != null) return fSelectedExpression;

    IASTFragment selectedFragment =
        ASTFragmentFactory.createFragmentForSourceRange(
            new SourceRange(fSelectionStart, fSelectionLength), fCuRewrite.getRoot(), fCu);

    if (selectedFragment instanceof IExpressionFragment
        && !Checks.isInsideJavadoc(selectedFragment.getAssociatedNode())) {
      fSelectedExpression = (IExpressionFragment) selectedFragment;
    }

    if (fSelectedExpression != null
        && Checks.isEnumCase(fSelectedExpression.getAssociatedExpression().getParent())) {
      fSelectedExpression = null;
    }

    return fSelectedExpression;
  }

  /**
   * Returns the type to which the new constant will be added to. It is the first non-anonymous
   * parent.
   *
   * @return the type to add the new constant to
   * @throws JavaModelException shouldn't happen
   */
  private AbstractTypeDeclaration getContainingTypeDeclarationNode() throws JavaModelException {
    AbstractTypeDeclaration result =
        (AbstractTypeDeclaration)
            ASTNodes.getParent(
                getSelectedExpression().getAssociatedNode(), AbstractTypeDeclaration.class);
    Assert.isNotNull(result);
    return result;
  }

  private ITypeBinding getContainingTypeBinding() throws JavaModelException {
    ITypeBinding result = getContainingTypeDeclarationNode().resolveBinding();
    Assert.isNotNull(result);
    return result;
  }

  private RefactoringStatus initialize(JavaRefactoringArguments arguments) {
    final String selection =
        arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION);
    if (selection != null) {
      int offset = -1;
      int length = -1;
      final StringTokenizer tokenizer = new StringTokenizer(selection);
      if (tokenizer.hasMoreTokens()) offset = Integer.valueOf(tokenizer.nextToken()).intValue();
      if (tokenizer.hasMoreTokens()) length = Integer.valueOf(tokenizer.nextToken()).intValue();
      if (offset >= 0 && length >= 0) {
        fSelectionStart = offset;
        fSelectionLength = length;
      } else
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_illegal_argument,
                new Object[] {selection, JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION}));
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION));
    final String handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
    if (handle != null) {
      final IJavaElement element =
          JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
      if (element == null
          || !element.exists()
          || element.getElementType() != IJavaElement.COMPILATION_UNIT)
        return JavaRefactoringDescriptorUtil.createInputFatalStatus(
            element, getName(), IJavaRefactorings.EXTRACT_CONSTANT);
      else fCu = (ICompilationUnit) element;
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    final String visibility = arguments.getAttribute(ATTRIBUTE_VISIBILITY);
    if (visibility != null && !"".equals(visibility)) { // $NON-NLS-1$
      int flag = 0;
      try {
        flag = Integer.parseInt(visibility);
      } catch (NumberFormatException exception) {
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_VISIBILITY));
      }
      fVisibility = JdtFlags.getVisibilityString(flag);
    }
    final String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
    if (name != null && !"".equals(name)) // $NON-NLS-1$
    fConstantName = name;
    else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
    final String replace = arguments.getAttribute(ATTRIBUTE_REPLACE);
    if (replace != null) {
      fReplaceAllOccurrences = Boolean.valueOf(replace).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_REPLACE));
    final String declareFinal = arguments.getAttribute(ATTRIBUTE_QUALIFY);
    if (declareFinal != null) {
      fQualifyReferencesWithDeclaringClassName = Boolean.valueOf(declareFinal).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_QUALIFY));
    return new RefactoringStatus();
  }
}

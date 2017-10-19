/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Nikolay Metchev
 * <nikolaymetchev@gmail.com> - [extract local] Extract to local variable not replacing multiple
 * occurrences in same statement - https://bugs.eclipse.org/406347
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractLocalDescriptor;
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
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStringStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatusCodes;
import org.eclipse.jdt.internal.corext.refactoring.rename.RefactoringAnalyzeUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.NoCommentSourceRangeComputer;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.text.edits.TextEditGroup;

/** Extract Local Variable (from selected expression inside method or initializer). */
public class ExtractTempRefactoring extends Refactoring {

  private static final String ATTRIBUTE_REPLACE = "replace"; // $NON-NLS-1$
  private static final String ATTRIBUTE_FINAL = "final"; // $NON-NLS-1$

  private static final class ForStatementChecker extends ASTVisitor {

    private final Collection<IVariableBinding> fForInitializerVariables;

    private boolean fReferringToForVariable = false;

    public ForStatementChecker(Collection<IVariableBinding> forInitializerVariables) {
      Assert.isNotNull(forInitializerVariables);
      fForInitializerVariables = forInitializerVariables;
    }

    public boolean isReferringToForVariable() {
      return fReferringToForVariable;
    }

    @Override
    public boolean visit(SimpleName node) {
      IBinding binding = node.resolveBinding();
      if (binding != null && fForInitializerVariables.contains(binding)) {
        fReferringToForVariable = true;
      }
      return false;
    }
  }

  private static boolean allArraysEqual(Object[][] arrays, int position) {
    Object element = arrays[0][position];
    for (int i = 0; i < arrays.length; i++) {
      Object[] array = arrays[i];
      if (!element.equals(array[position])) return false;
    }
    return true;
  }

  private static boolean canReplace(IASTFragment fragment) {
    ASTNode node = fragment.getAssociatedNode();
    ASTNode parent = node.getParent();
    if (parent instanceof VariableDeclarationFragment) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) parent;
      if (node.equals(vdf.getName())) return false;
    }
    if (isMethodParameter(node)) return false;
    if (isThrowableInCatchBlock(node)) return false;
    if (parent instanceof ExpressionStatement) return false;
    if (isLeftValue(node)) return false;
    if (isReferringToLocalVariableFromFor((Expression) node)) return false;
    if (isUsedInForInitializerOrUpdater((Expression) node)) return false;
    if (parent instanceof SwitchCase) return false;
    return true;
  }

  private static Object[] getArrayPrefix(Object[] array, int prefixLength) {
    Assert.isTrue(prefixLength <= array.length);
    Assert.isTrue(prefixLength >= 0);
    Object[] prefix = new Object[prefixLength];
    for (int i = 0; i < prefix.length; i++) {
      prefix[i] = array[i];
    }
    return prefix;
  }

  // return List<IVariableBinding>
  private static List<IVariableBinding> getForInitializedVariables(
      VariableDeclarationExpression variableDeclarations) {
    List<IVariableBinding> forInitializerVariables = new ArrayList<IVariableBinding>(1);
    for (Iterator<VariableDeclarationFragment> iter = variableDeclarations.fragments().iterator();
        iter.hasNext(); ) {
      VariableDeclarationFragment fragment = iter.next();
      IVariableBinding binding = fragment.resolveBinding();
      if (binding != null) forInitializerVariables.add(binding);
    }
    return forInitializerVariables;
  }

  private static Object[] getLongestArrayPrefix(Object[][] arrays) {
    int length = -1;
    if (arrays.length == 0) return new Object[0];
    int minArrayLength = arrays[0].length;
    for (int i = 1; i < arrays.length; i++)
      minArrayLength = Math.min(minArrayLength, arrays[i].length);

    for (int i = 0; i < minArrayLength; i++) {
      if (!allArraysEqual(arrays, i)) break;
      length++;
    }
    if (length == -1) return new Object[0];
    return getArrayPrefix(arrays[0], length + 1);
  }

  private static ASTNode[] getParents(ASTNode node) {
    ASTNode current = node;
    List<ASTNode> parents = new ArrayList<ASTNode>();
    do {
      parents.add(current.getParent());
      current = current.getParent();
    } while (current.getParent() != null);
    Collections.reverse(parents);
    return parents.toArray(new ASTNode[parents.size()]);
  }

  private static boolean isLeftValue(ASTNode node) {
    ASTNode parent = node.getParent();
    if (parent instanceof Assignment) {
      Assignment assignment = (Assignment) parent;
      if (assignment.getLeftHandSide() == node) return true;
    }
    if (parent instanceof PostfixExpression) return true;
    if (parent instanceof PrefixExpression) {
      PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
      if (op.equals(PrefixExpression.Operator.DECREMENT)) return true;
      if (op.equals(PrefixExpression.Operator.INCREMENT)) return true;
      return false;
    }
    return false;
  }

  private static boolean isMethodParameter(ASTNode node) {
    return (node instanceof SimpleName)
        && (node.getParent() instanceof SingleVariableDeclaration)
        && (node.getParent().getParent() instanceof MethodDeclaration);
  }

  private static boolean isReferringToLocalVariableFromFor(Expression expression) {
    ASTNode current = expression;
    ASTNode parent = current.getParent();
    while (parent != null && !(parent instanceof BodyDeclaration)) {
      if (parent instanceof ForStatement) {
        ForStatement forStmt = (ForStatement) parent;
        if (forStmt.initializers().contains(current)
            || forStmt.updaters().contains(current)
            || forStmt.getExpression() == current) {
          List<Expression> initializers = forStmt.initializers();
          if (initializers.size() == 1
              && initializers.get(0) instanceof VariableDeclarationExpression) {
            List<IVariableBinding> forInitializerVariables =
                getForInitializedVariables((VariableDeclarationExpression) initializers.get(0));
            ForStatementChecker checker = new ForStatementChecker(forInitializerVariables);
            expression.accept(checker);
            if (checker.isReferringToForVariable()) return true;
          }
        }
      }
      current = parent;
      parent = current.getParent();
    }
    return false;
  }

  private static boolean isThrowableInCatchBlock(ASTNode node) {
    return (node instanceof SimpleName)
        && (node.getParent() instanceof SingleVariableDeclaration)
        && (node.getParent().getParent() instanceof CatchClause);
  }

  private static boolean isUsedInForInitializerOrUpdater(Expression expression) {
    ASTNode parent = expression.getParent();
    if (parent instanceof ForStatement) {
      ForStatement forStmt = (ForStatement) parent;
      return forStmt.initializers().contains(expression) || forStmt.updaters().contains(expression);
    }
    return false;
  }

  private static IASTFragment[] retainOnlyReplacableMatches(IASTFragment[] allMatches) {
    List<IASTFragment> result = new ArrayList<IASTFragment>(allMatches.length);
    for (int i = 0; i < allMatches.length; i++) {
      if (canReplace(allMatches[i])) result.add(allMatches[i]);
    }
    return result.toArray(new IASTFragment[result.size()]);
  }

  private CompilationUnit fCompilationUnitNode;

  private CompilationUnitRewrite fCURewrite;

  private ICompilationUnit fCu;

  private boolean fDeclareFinal;

  private String[] fExcludedVariableNames;

  private boolean fReplaceAllOccurrences;

  // caches:
  private IExpressionFragment fSelectedExpression;

  private int fSelectionLength;

  private int fSelectionStart;

  private String fTempName;
  private String[] fGuessedTempNames;

  private boolean fCheckResultForCompileProblems;

  private CompilationUnitChange fChange;

  private LinkedProposalModel fLinkedProposalModel;

  private static final String KEY_NAME = "name"; // $NON-NLS-1$
  private static final String KEY_TYPE = "type"; // $NON-NLS-1$

  /**
   * Creates a new extract temp refactoring
   *
   * @param unit the compilation unit, or <code>null</code> if invoked by scripting
   * @param selectionStart start of selection
   * @param selectionLength length of selection
   */
  public ExtractTempRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCu = unit;
    fCompilationUnitNode = null;

    fReplaceAllOccurrences = true; // default
    fDeclareFinal = false; // default
    fTempName = ""; // $NON-NLS-1$

    fLinkedProposalModel = null;
    fCheckResultForCompileProblems = true;
  }

  public ExtractTempRefactoring(CompilationUnit astRoot, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    Assert.isTrue(astRoot.getTypeRoot() instanceof ICompilationUnit);

    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCu = (ICompilationUnit) astRoot.getTypeRoot();
    fCompilationUnitNode = astRoot;

    fReplaceAllOccurrences = true; // default
    fDeclareFinal = false; // default
    fTempName = ""; // $NON-NLS-1$

    fLinkedProposalModel = null;
    fCheckResultForCompileProblems = true;
  }

  public ExtractTempRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
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

  private void addReplaceExpressionWithTemp() throws JavaModelException {
    IASTFragment[] fragmentsToReplace = retainOnlyReplacableMatches(getMatchingFragments());
    // TODO: should not have to prune duplicates here...
    ASTRewrite rewrite = fCURewrite.getASTRewrite();
    HashSet<IASTFragment> seen = new HashSet<IASTFragment>();
    for (int i = 0; i < fragmentsToReplace.length; i++) {
      IASTFragment fragment = fragmentsToReplace[i];
      if (!seen.add(fragment)) continue;
      SimpleName tempName = fCURewrite.getAST().newSimpleName(fTempName);
      TextEditGroup description =
          fCURewrite.createGroupDescription(RefactoringCoreMessages.ExtractTempRefactoring_replace);

      fragment.replace(rewrite, tempName, description);
      if (fLinkedProposalModel != null)
        fLinkedProposalModel
            .getPositionGroup(KEY_NAME, true)
            .addPosition(rewrite.track(tempName), false);
    }
  }

  private RefactoringStatus checkExpression() throws JavaModelException {
    Expression selectedExpression = getSelectedExpression().getAssociatedExpression();
    if (selectedExpression != null) {
      final ASTNode parent = selectedExpression.getParent();
      if (selectedExpression instanceof NullLiteral) {
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_null_literals);
      } else if (selectedExpression instanceof ArrayInitializer) {
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_array_initializer);
      } else if (selectedExpression instanceof Assignment) {
        if (parent instanceof Expression && !(parent instanceof ParenthesizedExpression))
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ExtractTempRefactoring_assignment);
        else return null;
      } else if (selectedExpression instanceof SimpleName) {
        if ((((SimpleName) selectedExpression)).isDeclaration())
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ExtractTempRefactoring_names_in_declarations);
        if (parent instanceof QualifiedName
                && selectedExpression.getLocationInParent() == QualifiedName.NAME_PROPERTY
            || parent instanceof FieldAccess
                && selectedExpression.getLocationInParent() == FieldAccess.NAME_PROPERTY)
          return RefactoringStatus.createFatalErrorStatus(
              RefactoringCoreMessages.ExtractTempRefactoring_select_expression);
      } else if (selectedExpression instanceof VariableDeclarationExpression
          && parent instanceof TryStatement) {
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_resource_in_try_with_resources);
      }
    }

    return null;
  }

  // !! Same as in ExtractConstantRefactoring
  private RefactoringStatus checkExpressionFragmentIsRValue() throws JavaModelException {
    switch (Checks.checkExpressionIsRValue(getSelectedExpression().getAssociatedExpression())) {
      case Checks.NOT_RVALUE_MISC:
        return RefactoringStatus.createStatus(
            RefactoringStatus.FATAL,
            RefactoringCoreMessages.ExtractTempRefactoring_select_expression,
            null,
            Corext.getPluginId(),
            RefactoringStatusCodes.EXPRESSION_NOT_RVALUE,
            null);
      case Checks.NOT_RVALUE_VOID:
        return RefactoringStatus.createStatus(
            RefactoringStatus.FATAL,
            RefactoringCoreMessages.ExtractTempRefactoring_no_void,
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

  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 4);

      fCURewrite = new CompilationUnitRewrite(fCu, fCompilationUnitNode);
      fCURewrite.getASTRewrite().setTargetSourceRangeComputer(new NoCommentSourceRangeComputer());

      doCreateChange(new SubProgressMonitor(pm, 2));

      fChange =
          fCURewrite.createChange(
              RefactoringCoreMessages.ExtractTempRefactoring_change_name,
              true,
              new SubProgressMonitor(pm, 1));

      RefactoringStatus result = new RefactoringStatus();
      if (Arrays.asList(getExcludedVariableNames()).contains(fTempName))
        result.addWarning(
            Messages.format(
                RefactoringCoreMessages.ExtractTempRefactoring_another_variable,
                BasicElementLabels.getJavaElementName(fTempName)));

      result.merge(checkMatchingFragments());

      fChange.setKeepPreviewEdits(true);

      if (fCheckResultForCompileProblems) {
        checkNewSource(new SubProgressMonitor(pm, 1), result);
      }

      return result;
    } finally {
      pm.done();
    }
  }

  private final ExtractLocalDescriptor createRefactoringDescriptor() {
    final Map<String, String> arguments = new HashMap<String, String>();
    String project = null;
    IJavaProject javaProject = fCu.getJavaProject();
    if (javaProject != null) project = javaProject.getElementName();
    final String description =
        Messages.format(
            RefactoringCoreMessages.ExtractTempRefactoring_descriptor_description_short,
            BasicElementLabels.getJavaElementName(fTempName));
    final String expression = ASTNodes.asString(fSelectedExpression.getAssociatedExpression());
    final String header =
        Messages.format(
            RefactoringCoreMessages.ExtractTempRefactoring_descriptor_description,
            new String[] {
              BasicElementLabels.getJavaElementName(fTempName),
              BasicElementLabels.getJavaCodeString(expression)
            });
    final JDTRefactoringDescriptorComment comment =
        new JDTRefactoringDescriptorComment(project, this, header);
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ExtractTempRefactoring_name_pattern,
            BasicElementLabels.getJavaElementName(fTempName)));
    final BodyDeclaration decl =
        (BodyDeclaration)
            ASTNodes.getParent(
                fSelectedExpression.getAssociatedExpression(), BodyDeclaration.class);
    if (decl instanceof MethodDeclaration) {
      final IMethodBinding method = ((MethodDeclaration) decl).resolveBinding();
      final String label =
          method != null
              ? BindingLabelProvider.getBindingLabel(method, JavaElementLabels.ALL_FULLY_QUALIFIED)
              : BasicElementLabels.getJavaElementName(
                  '{' + JavaElementLabels.ELLIPSIS_STRING + '}');
      comment.addSetting(
          Messages.format(
              RefactoringCoreMessages.ExtractTempRefactoring_destination_pattern, label));
    }
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ExtractTempRefactoring_expression_pattern,
            BasicElementLabels.getJavaCodeString(expression)));
    if (fReplaceAllOccurrences)
      comment.addSetting(RefactoringCoreMessages.ExtractTempRefactoring_replace_occurrences);
    if (fDeclareFinal)
      comment.addSetting(RefactoringCoreMessages.ExtractTempRefactoring_declare_final);
    final ExtractLocalDescriptor descriptor =
        RefactoringSignatureDescriptorFactory.createExtractLocalDescriptor(
            project, description, comment.asString(), arguments, RefactoringDescriptor.NONE);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
        JavaRefactoringDescriptorUtil.elementToHandle(project, fCu));
    arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fTempName);
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION,
        new Integer(fSelectionStart).toString()
            + " "
            + new Integer(fSelectionLength).toString()); // $NON-NLS-1$
    arguments.put(ATTRIBUTE_REPLACE, Boolean.valueOf(fReplaceAllOccurrences).toString());
    arguments.put(ATTRIBUTE_FINAL, Boolean.valueOf(fDeclareFinal).toString());
    return descriptor;
  }

  private void doCreateChange(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 1);
      try {
        createTempDeclaration();
      } catch (CoreException exception) {
        JavaPlugin.log(exception);
      }
      addReplaceExpressionWithTemp();
    } finally {
      pm.done();
    }
  }

  private void checkNewSource(SubProgressMonitor monitor, RefactoringStatus result)
      throws CoreException {
    String newCuSource = fChange.getPreviewContent(new NullProgressMonitor());
    CompilationUnit newCUNode =
        new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL)
            .parse(newCuSource, fCu, true, true, monitor);
    IProblem[] newProblems =
        RefactoringAnalyzeUtil.getIntroducedCompileProblems(newCUNode, fCompilationUnitNode);
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

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask("", 6); // $NON-NLS-1$

      RefactoringStatus result =
          Checks.validateModifiesFiles(
              ResourceUtil.getFiles(new ICompilationUnit[] {fCu}), getValidationContext());
      if (result.hasFatalError()) return result;

      if (fCompilationUnitNode == null) {
        fCompilationUnitNode =
            RefactoringASTParser.parseWithASTProvider(fCu, true, new SubProgressMonitor(pm, 3));
      } else {
        pm.worked(3);
      }

      result.merge(checkSelection(new SubProgressMonitor(pm, 3)));
      if (!result.hasFatalError() && isLiteralNodeSelected()) fReplaceAllOccurrences = false;
      return result;

    } finally {
      pm.done();
    }
  }

  private RefactoringStatus checkMatchingFragments() throws JavaModelException {
    RefactoringStatus result = new RefactoringStatus();
    IASTFragment[] matchingFragments = getMatchingFragments();
    for (int i = 0; i < matchingFragments.length; i++) {
      ASTNode node = matchingFragments[i].getAssociatedNode();
      if (isLeftValue(node) && !isReferringToLocalVariableFromFor((Expression) node)) {
        String msg = RefactoringCoreMessages.ExtractTempRefactoring_assigned_to;
        result.addWarning(msg, JavaStatusContext.create(fCu, node));
      }
    }
    return result;
  }

  private RefactoringStatus checkSelection(IProgressMonitor pm) throws JavaModelException {
    try {
      pm.beginTask("", 8); // $NON-NLS-1$

      IExpressionFragment selectedExpression = getSelectedExpression();

      if (selectedExpression == null) {
        String message = RefactoringCoreMessages.ExtractTempRefactoring_select_expression;
        return CodeRefactoringUtil.checkMethodSyntaxErrors(
            fSelectionStart, fSelectionLength, fCompilationUnitNode, message);
      }
      pm.worked(1);

      if (isUsedInExplicitConstructorCall())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_explicit_constructor);
      pm.worked(1);

      ASTNode associatedNode = selectedExpression.getAssociatedNode();
      if (getEnclosingBodyNode() == null
          || ASTNodes.getParent(associatedNode, Annotation.class) != null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_expr_in_method_or_initializer);
      pm.worked(1);

      if (associatedNode instanceof Name
          && associatedNode.getParent() instanceof ClassInstanceCreation
          && associatedNode.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_name_in_new);
      pm.worked(1);

      RefactoringStatus result = new RefactoringStatus();
      result.merge(checkExpression());
      if (result.hasFatalError()) return result;
      pm.worked(1);

      result.merge(checkExpressionFragmentIsRValue());
      if (result.hasFatalError()) return result;
      pm.worked(1);

      if (isUsedInForInitializerOrUpdater(getSelectedExpression().getAssociatedExpression()))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_for_initializer_updater);
      pm.worked(1);

      if (isReferringToLocalVariableFromFor(getSelectedExpression().getAssociatedExpression()))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ExtractTempRefactoring_refers_to_for_variable);
      pm.worked(1);

      return result;
    } finally {
      pm.done();
    }
  }

  public RefactoringStatus checkTempName(String newName) {
    RefactoringStatus status = Checks.checkTempName(newName, fCu);
    if (Arrays.asList(getExcludedVariableNames()).contains(newName))
      status.addWarning(
          Messages.format(
              RefactoringCoreMessages.ExtractTempRefactoring_another_variable,
              BasicElementLabels.getJavaElementName(newName)));
    return status;
  }

  private void createAndInsertTempDeclaration() throws CoreException {
    Expression initializer =
        getSelectedExpression().createCopyTarget(fCURewrite.getASTRewrite(), true);
    VariableDeclarationStatement vds = createTempDeclaration(initializer);

    if ((!fReplaceAllOccurrences)
        || (retainOnlyReplacableMatches(getMatchingFragments()).length <= 1)) {
      insertAt(getSelectedExpression().getAssociatedNode(), vds);
      return;
    }

    ASTNode[] firstReplaceNodeParents =
        getParents(getFirstReplacedExpression().getAssociatedNode());
    ASTNode[] commonPath = findDeepestCommonSuperNodePathForReplacedNodes();
    Assert.isTrue(commonPath.length <= firstReplaceNodeParents.length);

    ASTNode deepestCommonParent = firstReplaceNodeParents[commonPath.length - 1];
    if (deepestCommonParent instanceof Block)
      insertAt(firstReplaceNodeParents[commonPath.length], vds);
    else insertAt(deepestCommonParent, vds);
  }

  private VariableDeclarationStatement createTempDeclaration(Expression initializer)
      throws CoreException {
    AST ast = fCURewrite.getAST();

    VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
    vdf.setName(ast.newSimpleName(fTempName));
    vdf.setInitializer(initializer);

    VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
    if (fDeclareFinal) {
      vds.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
    }
    vds.setType(createTempType());

    if (fLinkedProposalModel != null) {
      ASTRewrite rewrite = fCURewrite.getASTRewrite();
      LinkedProposalPositionGroup nameGroup = fLinkedProposalModel.getPositionGroup(KEY_NAME, true);
      nameGroup.addPosition(rewrite.track(vdf.getName()), true);

      String[] nameSuggestions = guessTempNames();
      if (nameSuggestions.length > 0 && !nameSuggestions[0].equals(fTempName)) {
        nameGroup.addProposal(fTempName, null, nameSuggestions.length + 1);
      }
      for (int i = 0; i < nameSuggestions.length; i++) {
        nameGroup.addProposal(nameSuggestions[i], null, nameSuggestions.length - i);
      }
    }
    return vds;
  }

  private void insertAt(ASTNode target, Statement declaration) {
    ASTRewrite rewrite = fCURewrite.getASTRewrite();
    TextEditGroup groupDescription =
        fCURewrite.createGroupDescription(
            RefactoringCoreMessages.ExtractTempRefactoring_declare_local_variable);

    ASTNode parent = target.getParent();
    StructuralPropertyDescriptor locationInParent = target.getLocationInParent();
    while (locationInParent != Block.STATEMENTS_PROPERTY
        && locationInParent != SwitchStatement.STATEMENTS_PROPERTY) {
      if (locationInParent == IfStatement.THEN_STATEMENT_PROPERTY
          || locationInParent == IfStatement.ELSE_STATEMENT_PROPERTY
          || locationInParent == ForStatement.BODY_PROPERTY
          || locationInParent == EnhancedForStatement.BODY_PROPERTY
          || locationInParent == DoStatement.BODY_PROPERTY
          || locationInParent == WhileStatement.BODY_PROPERTY) {
        // create intermediate block if target was the body property of a control statement:
        Block replacement = rewrite.getAST().newBlock();
        ListRewrite replacementRewrite =
            rewrite.getListRewrite(replacement, Block.STATEMENTS_PROPERTY);
        replacementRewrite.insertFirst(declaration, null);
        replacementRewrite.insertLast(rewrite.createMoveTarget(target), null);
        rewrite.replace(target, replacement, groupDescription);
        return;
      }
      target = parent;
      parent = parent.getParent();
      locationInParent = target.getLocationInParent();
    }
    ListRewrite listRewrite =
        rewrite.getListRewrite(parent, (ChildListPropertyDescriptor) locationInParent);
    listRewrite.insertBefore(declaration, target, groupDescription);
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.ExtractTempRefactoring_checking_preconditions, 1);

      ExtractLocalDescriptor descriptor = createRefactoringDescriptor();
      fChange.setDescriptor(new RefactoringChangeDescriptor(descriptor));
      return fChange;
    } finally {
      pm.done();
    }
  }

  private void createTempDeclaration() throws CoreException {
    if (shouldReplaceSelectedExpressionWithTempDeclaration())
      replaceSelectedExpressionWithTempDeclaration();
    else createAndInsertTempDeclaration();
  }

  public boolean declareFinal() {
    return fDeclareFinal;
  }

  private ASTNode[] findDeepestCommonSuperNodePathForReplacedNodes() throws JavaModelException {
    ASTNode[] matchNodes = getMatchNodes();

    ASTNode[][] matchingNodesParents = new ASTNode[matchNodes.length][];
    for (int i = 0; i < matchNodes.length; i++) {
      matchingNodesParents[i] = getParents(matchNodes[i]);
    }
    List<Object> l = Arrays.asList(getLongestArrayPrefix(matchingNodesParents));
    return l.toArray(new ASTNode[l.size()]);
  }

  private Block getEnclosingBodyNode() throws JavaModelException {
    ASTNode node = getSelectedExpression().getAssociatedNode();

    // expression must be in a method or initializer body
    // make sure it is not in method or parameter annotation
    StructuralPropertyDescriptor location = null;
    while (node != null && !(node instanceof BodyDeclaration)) {
      location = node.getLocationInParent();
      node = node.getParent();
    }
    if (location == MethodDeclaration.BODY_PROPERTY || location == Initializer.BODY_PROPERTY) {
      return (Block) node.getStructuralProperty(location);
    }
    return null;
  }

  private String[] getExcludedVariableNames() {
    if (fExcludedVariableNames == null) {
      try {
        IBinding[] bindings =
            new ScopeAnalyzer(fCompilationUnitNode)
                .getDeclarationsInScope(
                    getSelectedExpression().getStartPosition(),
                    ScopeAnalyzer.VARIABLES | ScopeAnalyzer.CHECK_VISIBILITY);
        fExcludedVariableNames = new String[bindings.length];
        for (int i = 0; i < bindings.length; i++) {
          fExcludedVariableNames[i] = bindings[i].getName();
        }
      } catch (JavaModelException e) {
        fExcludedVariableNames = new String[0];
      }
    }
    return fExcludedVariableNames;
  }

  private IExpressionFragment getFirstReplacedExpression() throws JavaModelException {
    if (!fReplaceAllOccurrences) return getSelectedExpression();
    IASTFragment[] nodesToReplace = retainOnlyReplacableMatches(getMatchingFragments());
    if (nodesToReplace.length == 0) return getSelectedExpression();
    Comparator<IASTFragment> comparator =
        new Comparator<IASTFragment>() {

          public int compare(IASTFragment o1, IASTFragment o2) {
            return o1.getStartPosition() - o2.getStartPosition();
          }
        };
    Arrays.sort(nodesToReplace, comparator);
    return (IExpressionFragment) nodesToReplace[0];
  }

  private IASTFragment[] getMatchingFragments() throws JavaModelException {
    if (fReplaceAllOccurrences) {
      IASTFragment[] allMatches =
          ASTFragmentFactory.createFragmentForFullSubtree(getEnclosingBodyNode())
              .getSubFragmentsMatching(getSelectedExpression());
      return allMatches;
    } else return new IASTFragment[] {getSelectedExpression()};
  }

  private ASTNode[] getMatchNodes() throws JavaModelException {
    IASTFragment[] matches = retainOnlyReplacableMatches(getMatchingFragments());
    ASTNode[] result = new ASTNode[matches.length];
    for (int i = 0; i < matches.length; i++) result[i] = matches[i].getAssociatedNode();
    return result;
  }

  @Override
  public String getName() {
    return RefactoringCoreMessages.ExtractTempRefactoring_name;
  }

  private IExpressionFragment getSelectedExpression() throws JavaModelException {
    if (fSelectedExpression != null) return fSelectedExpression;
    IASTFragment selectedFragment =
        ASTFragmentFactory.createFragmentForSourceRange(
            new SourceRange(fSelectionStart, fSelectionLength), fCompilationUnitNode, fCu);

    if (selectedFragment instanceof IExpressionFragment
        && !Checks.isInsideJavadoc(selectedFragment.getAssociatedNode())) {
      fSelectedExpression = (IExpressionFragment) selectedFragment;
    } else if (selectedFragment != null) {
      if (selectedFragment.getAssociatedNode() instanceof ExpressionStatement) {
        ExpressionStatement exprStatement =
            (ExpressionStatement) selectedFragment.getAssociatedNode();
        Expression expression = exprStatement.getExpression();
        fSelectedExpression =
            (IExpressionFragment) ASTFragmentFactory.createFragmentForFullSubtree(expression);
      } else if (selectedFragment.getAssociatedNode() instanceof Assignment) {
        Assignment assignment = (Assignment) selectedFragment.getAssociatedNode();
        fSelectedExpression =
            (IExpressionFragment) ASTFragmentFactory.createFragmentForFullSubtree(assignment);
      }
    }

    if (fSelectedExpression != null
        && Checks.isEnumCase(fSelectedExpression.getAssociatedExpression().getParent())) {
      fSelectedExpression = null;
    }

    return fSelectedExpression;
  }

  private Type createTempType() throws CoreException {
    Expression expression = getSelectedExpression().getAssociatedExpression();

    Type resultingType = null;
    ITypeBinding typeBinding = expression.resolveTypeBinding();

    ASTRewrite rewrite = fCURewrite.getASTRewrite();
    AST ast = rewrite.getAST();

    if (expression instanceof ClassInstanceCreation
        && (typeBinding == null || typeBinding.getTypeArguments().length == 0)) {
      resultingType =
          (Type) rewrite.createCopyTarget(((ClassInstanceCreation) expression).getType());
    } else if (expression instanceof CastExpression) {
      resultingType = (Type) rewrite.createCopyTarget(((CastExpression) expression).getType());
    } else {
      if (typeBinding == null) {
        typeBinding = ASTResolving.guessBindingForReference(expression);
      }
      if (typeBinding != null) {
        typeBinding = Bindings.normalizeForDeclarationUse(typeBinding, ast);
        ImportRewrite importRewrite = fCURewrite.getImportRewrite();
        ImportRewriteContext context =
            new ContextSensitiveImportRewriteContext(expression, importRewrite);
        resultingType = importRewrite.addImport(typeBinding, ast, context);
      } else {
        resultingType = ast.newSimpleType(ast.newSimpleName("Object")); // $NON-NLS-1$
      }
    }
    if (fLinkedProposalModel != null) {
      LinkedProposalPositionGroup typeGroup = fLinkedProposalModel.getPositionGroup(KEY_TYPE, true);
      typeGroup.addPosition(rewrite.track(resultingType), false);
      if (typeBinding != null) {
        ITypeBinding[] relaxingTypes = ASTResolving.getNarrowingTypes(ast, typeBinding);
        for (int i = 0; i < relaxingTypes.length; i++) {
          typeGroup.addProposal(relaxingTypes[i], fCURewrite.getCu(), relaxingTypes.length - i);
        }
      }
    }
    return resultingType;
  }

  public String guessTempName() {
    String[] proposals = guessTempNames();
    if (proposals.length == 0) return fTempName;
    else return proposals[0];
  }

  /**
   * @return proposed variable names (may be empty, but not null). The first proposal should be used
   *     as "best guess" (if it exists).
   */
  public String[] guessTempNames() {
    if (fGuessedTempNames == null) {
      try {
        Expression expression = getSelectedExpression().getAssociatedExpression();
        if (expression != null) {
          ITypeBinding binding = guessBindingForReference(expression);
          fGuessedTempNames =
              StubUtility.getVariableNameSuggestions(
                  NamingConventions.VK_LOCAL,
                  fCu.getJavaProject(),
                  binding,
                  expression,
                  Arrays.asList(getExcludedVariableNames()));
        }
      } catch (JavaModelException e) {
      }
      if (fGuessedTempNames == null) fGuessedTempNames = new String[0];
    }
    return fGuessedTempNames;
  }

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

  private boolean isUsedInExplicitConstructorCall() throws JavaModelException {
    Expression selectedExpression = getSelectedExpression().getAssociatedExpression();
    if (ASTNodes.getParent(selectedExpression, ConstructorInvocation.class) != null) return true;
    if (ASTNodes.getParent(selectedExpression, SuperConstructorInvocation.class) != null)
      return true;
    return false;
  }

  public boolean replaceAllOccurrences() {
    return fReplaceAllOccurrences;
  }

  private void replaceSelectedExpressionWithTempDeclaration() throws CoreException {
    ASTRewrite rewrite = fCURewrite.getASTRewrite();
    Expression selectedExpression =
        getSelectedExpression().getAssociatedExpression(); // whole expression selected

    Expression initializer = (Expression) rewrite.createMoveTarget(selectedExpression);
    ASTNode replacement =
        createTempDeclaration(initializer); // creates a VariableDeclarationStatement

    ExpressionStatement parent = (ExpressionStatement) selectedExpression.getParent();
    if (ASTNodes.isControlStatementBody(parent.getLocationInParent())) {
      Block block = rewrite.getAST().newBlock();
      block.statements().add(replacement);
      replacement = block;
    }
    rewrite.replace(
        parent,
        replacement,
        fCURewrite.createGroupDescription(
            RefactoringCoreMessages.ExtractTempRefactoring_declare_local_variable));
  }

  public void setDeclareFinal(boolean declareFinal) {
    fDeclareFinal = declareFinal;
  }

  public void setReplaceAllOccurrences(boolean replaceAllOccurrences) {
    fReplaceAllOccurrences = replaceAllOccurrences;
  }

  public void setTempName(String newName) {
    fTempName = newName;
  }

  private boolean shouldReplaceSelectedExpressionWithTempDeclaration() throws JavaModelException {
    IExpressionFragment selectedFragment = getSelectedExpression();
    return selectedFragment.getAssociatedNode().getParent() instanceof ExpressionStatement
        && selectedFragment.matches(
            ASTFragmentFactory.createFragmentForFullSubtree(selectedFragment.getAssociatedNode()));
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
            element, getName(), IJavaRefactorings.EXTRACT_LOCAL_VARIABLE);
      else fCu = (ICompilationUnit) element;
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    final String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
    if (name != null && !"".equals(name)) // $NON-NLS-1$
    fTempName = name;
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
    final String declareFinal = arguments.getAttribute(ATTRIBUTE_FINAL);
    if (declareFinal != null) {
      fDeclareFinal = Boolean.valueOf(declareFinal).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_FINAL));
    return new RefactoringStatus();
  }
}

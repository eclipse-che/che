/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.InlineLocalVariableDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.NecessaryParenthesesChecker;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempDeclarationFinder;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempOccurrenceAnalyzer;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TightSourceRangeComputer;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class InlineTempRefactoring extends Refactoring {

  private int fSelectionStart;
  private int fSelectionLength;
  private ICompilationUnit fCu;

  // the following fields are set after the construction
  private VariableDeclaration fVariableDeclaration;
  private SimpleName[] fReferences;
  private CompilationUnit fASTRoot;

  /**
   * Creates a new inline constant refactoring.
   *
   * @param unit the compilation unit, or <code>null</code> if invoked by scripting
   * @param node compilation unit node, or <code>null</code>
   * @param selectionStart start
   * @param selectionLength length
   */
  public InlineTempRefactoring(
      ICompilationUnit unit, CompilationUnit node, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCu = unit;

    fASTRoot = node;
    fVariableDeclaration = null;
  }

  /**
   * Creates a new inline constant refactoring.
   *
   * @param unit the compilation unit, or <code>null</code> if invoked by scripting
   * @param selectionStart start
   * @param selectionLength length
   */
  public InlineTempRefactoring(ICompilationUnit unit, int selectionStart, int selectionLength) {
    this(unit, null, selectionStart, selectionLength);
  }

  public InlineTempRefactoring(VariableDeclaration decl) {
    fVariableDeclaration = decl;
    ASTNode astRoot = decl.getRoot();
    Assert.isTrue(astRoot instanceof CompilationUnit);
    fASTRoot = (CompilationUnit) astRoot;
    Assert.isTrue(fASTRoot.getJavaElement() instanceof ICompilationUnit);

    fSelectionStart = decl.getStartPosition();
    fSelectionLength = decl.getLength();
    fCu = (ICompilationUnit) fASTRoot.getJavaElement();
  }

  public InlineTempRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
    this(null, null, 0, 0);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
  }

  public RefactoringStatus checkIfTempSelected() {
    VariableDeclaration decl = getVariableDeclaration();
    if (decl == null) {
      return CodeRefactoringUtil.checkMethodSyntaxErrors(
          fSelectionStart,
          fSelectionLength,
          getASTRoot(),
          RefactoringCoreMessages.InlineTempRefactoring_select_temp);
    }
    if (decl.getParent() instanceof FieldDeclaration) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTemRefactoring_error_message_fieldsCannotBeInlined);
    }
    return new RefactoringStatus();
  }

  private CompilationUnit getASTRoot() {
    if (fASTRoot == null) {
      fASTRoot = RefactoringASTParser.parseWithASTProvider(fCu, true, null);
    }
    return fASTRoot;
  }

  public VariableDeclaration getVariableDeclaration() {
    if (fVariableDeclaration == null) {
      fVariableDeclaration =
          TempDeclarationFinder.findTempDeclaration(
              getASTRoot(), fSelectionStart, fSelectionLength);
    }
    return fVariableDeclaration;
  }

  /*
   * @see IRefactoring#getName()
   */
  @Override
  public String getName() {
    return RefactoringCoreMessages.InlineTempRefactoring_name;
  }

  /*
   * @see Refactoring#checkActivation(IProgressMonitor)
   */
  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask("", 1); // $NON-NLS-1$

      RefactoringStatus result =
          Checks.validateModifiesFiles(
              ResourceUtil.getFiles(new ICompilationUnit[] {fCu}), getValidationContext());
      if (result.hasFatalError()) return result;

      VariableDeclaration declaration = getVariableDeclaration();

      result.merge(checkSelection(declaration));
      if (result.hasFatalError()) return result;

      result.merge(checkInitializer(declaration));
      return result;
    } finally {
      pm.done();
    }
  }

  private RefactoringStatus checkInitializer(VariableDeclaration decl) {
    if (decl.getInitializer().getNodeType() == ASTNode.NULL_LITERAL)
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTemRefactoring_error_message_nulLiteralsCannotBeInlined);
    return null;
  }

  private RefactoringStatus checkSelection(VariableDeclaration decl) {
    ASTNode parent = decl.getParent();
    if (parent instanceof MethodDeclaration) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTempRefactoring_method_parameter);
    }

    if (parent instanceof CatchClause) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTempRefactoring_exceptions_declared);
    }

    if (parent instanceof VariableDeclarationExpression
        && parent.getLocationInParent() == ForStatement.INITIALIZERS_PROPERTY) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTempRefactoring_for_initializers);
    }

    if (parent instanceof VariableDeclarationExpression
        && parent.getLocationInParent() == TryStatement.RESOURCES_PROPERTY) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.InlineTempRefactoring_resource_in_try_with_resources);
    }

    if (decl.getInitializer() == null) {
      String message =
          Messages.format(
              RefactoringCoreMessages.InlineTempRefactoring_not_initialized,
              BasicElementLabels.getJavaElementName(decl.getName().getIdentifier()));
      return RefactoringStatus.createFatalErrorStatus(message);
    }

    return checkAssignments(decl);
  }

  private RefactoringStatus checkAssignments(VariableDeclaration decl) {
    TempAssignmentFinder assignmentFinder = new TempAssignmentFinder(decl);
    getASTRoot().accept(assignmentFinder);
    if (!assignmentFinder.hasAssignments()) return new RefactoringStatus();
    ASTNode firstAssignment = assignmentFinder.getFirstAssignment();
    int start = firstAssignment.getStartPosition();
    int length = firstAssignment.getLength();
    ISourceRange range = new SourceRange(start, length);
    RefactoringStatusContext context = JavaStatusContext.create(fCu, range);
    String message =
        Messages.format(
            RefactoringCoreMessages.InlineTempRefactoring_assigned_more_once,
            BasicElementLabels.getJavaElementName(decl.getName().getIdentifier()));
    return RefactoringStatus.createFatalErrorStatus(message, context);
  }

  /*
   * @see Refactoring#checkInput(IProgressMonitor)
   */
  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask("", 1); // $NON-NLS-1$
      return new RefactoringStatus();
    } finally {
      pm.done();
    }
  }

  // ----- changes

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.InlineTempRefactoring_preview, 2);
      final Map<String, String> arguments = new HashMap<String, String>();
      String project = null;
      IJavaProject javaProject = fCu.getJavaProject();
      if (javaProject != null) project = javaProject.getElementName();

      final IVariableBinding binding = getVariableDeclaration().resolveBinding();
      String text = null;
      final IMethodBinding method = binding.getDeclaringMethod();
      if (method != null)
        text = BindingLabelProvider.getBindingLabel(method, JavaElementLabels.ALL_FULLY_QUALIFIED);
      else
        text = BasicElementLabels.getJavaElementName('{' + JavaElementLabels.ELLIPSIS_STRING + '}');
      final String description =
          Messages.format(
              RefactoringCoreMessages.InlineTempRefactoring_descriptor_description_short,
              BasicElementLabels.getJavaElementName(binding.getName()));
      final String header =
          Messages.format(
              RefactoringCoreMessages.InlineTempRefactoring_descriptor_description,
              new String[] {
                BindingLabelProvider.getBindingLabel(
                    binding, JavaElementLabels.ALL_FULLY_QUALIFIED),
                text
              });
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      comment.addSetting(
          Messages.format(
              RefactoringCoreMessages.InlineTempRefactoring_original_pattern,
              BindingLabelProvider.getBindingLabel(
                  binding, JavaElementLabels.ALL_FULLY_QUALIFIED)));
      final InlineLocalVariableDescriptor descriptor =
          RefactoringSignatureDescriptorFactory.createInlineLocalVariableDescriptor(
              project, description, comment.asString(), arguments, RefactoringDescriptor.NONE);
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
          JavaRefactoringDescriptorUtil.elementToHandle(project, fCu));
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION,
          String.valueOf(fSelectionStart) + ' ' + String.valueOf(fSelectionLength));

      CompilationUnitRewrite cuRewrite = new CompilationUnitRewrite(fCu, fASTRoot);

      inlineTemp(cuRewrite);
      removeTemp(cuRewrite);

      final CompilationUnitChange result =
          cuRewrite.createChange(
              RefactoringCoreMessages.InlineTempRefactoring_inline,
              false,
              new SubProgressMonitor(pm, 1));
      result.setDescriptor(new RefactoringChangeDescriptor(descriptor));
      return result;
    } finally {
      pm.done();
    }
  }

  private void inlineTemp(CompilationUnitRewrite cuRewrite) throws JavaModelException {
    SimpleName[] references = getReferences();

    TextEditGroup groupDesc =
        cuRewrite.createGroupDescription(
            RefactoringCoreMessages.InlineTempRefactoring_inline_edit_name);
    ASTRewrite rewrite = cuRewrite.getASTRewrite();

    for (int i = 0; i < references.length; i++) {
      SimpleName curr = references[i];
      ASTNode initializerCopy = getInitializerSource(cuRewrite, curr);
      rewrite.replace(curr, initializerCopy, groupDesc);
    }
  }

  private void removeTemp(CompilationUnitRewrite cuRewrite) {
    VariableDeclaration variableDeclaration = getVariableDeclaration();
    TextEditGroup groupDesc =
        cuRewrite.createGroupDescription(
            RefactoringCoreMessages.InlineTempRefactoring_remove_edit_name);
    ASTNode parent = variableDeclaration.getParent();
    ASTRewrite rewrite = cuRewrite.getASTRewrite();
    TightSourceRangeComputer sourceRangeComputer = new TightSourceRangeComputer();
    rewrite.setTargetSourceRangeComputer(sourceRangeComputer);
    if (parent instanceof VariableDeclarationStatement
        && ((VariableDeclarationStatement) parent).fragments().size() == 1) {
      sourceRangeComputer.addTightSourceNode(parent);
      rewrite.remove(parent, groupDesc);
    } else {
      sourceRangeComputer.addTightSourceNode(variableDeclaration);
      rewrite.remove(variableDeclaration, groupDesc);
    }
  }

  private Expression getInitializerSource(CompilationUnitRewrite rewrite, SimpleName reference)
      throws JavaModelException {
    Expression copy = getModifiedInitializerSource(rewrite, reference);
    if (NecessaryParenthesesChecker.needsParentheses(
        copy, reference.getParent(), reference.getLocationInParent())) {
      ParenthesizedExpression parentExpr = rewrite.getAST().newParenthesizedExpression();
      parentExpr.setExpression(copy);
      return parentExpr;
    }
    return copy;
  }

  private Expression getModifiedInitializerSource(
      CompilationUnitRewrite rewrite, SimpleName reference) throws JavaModelException {
    VariableDeclaration varDecl = getVariableDeclaration();
    Expression initializer = varDecl.getInitializer();

    ASTNode referenceContext = reference.getParent();
    if (Invocations.isResolvedTypeInferredFromExpectedType(initializer)) {
      if (!(referenceContext instanceof VariableDeclarationFragment
          || referenceContext instanceof SingleVariableDeclaration
          || referenceContext instanceof Assignment)) {
        ITypeBinding[] typeArguments = Invocations.getInferredTypeArguments(initializer);
        if (typeArguments != null) {
          String newSource = createParameterizedInvocation(initializer, typeArguments, rewrite);
          return (Expression)
              rewrite.getASTRewrite().createStringPlaceholder(newSource, initializer.getNodeType());
        }
      }
    }

    Expression copy = (Expression) rewrite.getASTRewrite().createCopyTarget(initializer);
    AST ast = rewrite.getAST();
    if (NecessaryParenthesesChecker.needsParentheses(
        initializer, reference.getParent(), reference.getLocationInParent())) {
      ParenthesizedExpression parenthesized = ast.newParenthesizedExpression();
      parenthesized.setExpression(copy);
      copy = parenthesized;
    }

    ITypeBinding explicitCast = ASTNodes.getExplicitCast(initializer, reference);
    if (explicitCast != null) {
      CastExpression cast = ast.newCastExpression();
      if (NecessaryParenthesesChecker.needsParentheses(
          copy, cast, CastExpression.EXPRESSION_PROPERTY)) {
        ParenthesizedExpression parenthesized = ast.newParenthesizedExpression();
        parenthesized.setExpression(copy);
        copy = parenthesized;
      }
      cast.setExpression(copy);
      ImportRewriteContext context =
          new ContextSensitiveImportRewriteContext(reference, rewrite.getImportRewrite());
      cast.setType(rewrite.getImportRewrite().addImport(explicitCast, ast, context));
      copy = cast;

    } else if (initializer instanceof ArrayInitializer && ASTNodes.getDimensions(varDecl) > 0) {
      ArrayType newType = (ArrayType) ASTNodeFactory.newType(ast, varDecl);

      ArrayCreation newArrayCreation = ast.newArrayCreation();
      newArrayCreation.setType(newType);
      newArrayCreation.setInitializer((ArrayInitializer) copy);
      return newArrayCreation;
    }
    return copy;
  }

  private String createParameterizedInvocation(
      Expression invocation, ITypeBinding[] typeArguments, CompilationUnitRewrite cuRewrite)
      throws JavaModelException {
    ASTRewrite rewrite = ASTRewrite.create(invocation.getAST());
    ListRewrite typeArgsRewrite = Invocations.getInferredTypeArgumentsRewrite(rewrite, invocation);

    for (int i = 0; i < typeArguments.length; i++) {
      Type typeArgumentNode =
          cuRewrite.getImportRewrite().addImport(typeArguments[i], cuRewrite.getAST());
      typeArgsRewrite.insertLast(typeArgumentNode, null);
    }

    if (invocation instanceof MethodInvocation) {
      MethodInvocation methodInvocation = (MethodInvocation) invocation;
      Expression expression = methodInvocation.getExpression();
      if (expression == null) {
        IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
        if (methodBinding != null && Modifier.isStatic(methodBinding.getModifiers())) {
          expression =
              cuRewrite
                  .getAST()
                  .newName(
                      cuRewrite
                          .getImportRewrite()
                          .addImport(methodBinding.getDeclaringClass().getTypeDeclaration()));
        } else {
          expression = invocation.getAST().newThisExpression();
        }
        rewrite.set(invocation, MethodInvocation.EXPRESSION_PROPERTY, expression, null);
      }
    }

    IDocument document = new Document(fCu.getBuffer().getContents());
    final RangeMarker marker =
        new RangeMarker(invocation.getStartPosition(), invocation.getLength());
    IJavaProject project = fCu.getJavaProject();
    TextEdit[] rewriteEdits =
        rewrite.rewriteAST(document, project.getOptions(true)).removeChildren();
    marker.addChildren(rewriteEdits);
    try {
      marker.apply(document, TextEdit.UPDATE_REGIONS);
      String rewrittenInitializer = document.get(marker.getOffset(), marker.getLength());
      IRegion region = document.getLineInformation(document.getLineOfOffset(marker.getOffset()));
      int oldIndent =
          Strings.computeIndentUnits(document.get(region.getOffset(), region.getLength()), project);
      return Strings.changeIndent(
          rewrittenInitializer,
          oldIndent,
          project,
          "",
          TextUtilities.getDefaultLineDelimiter(document)); // $NON-NLS-1$
    } catch (MalformedTreeException e) {
      JavaPlugin.log(e);
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
    // fallback:
    return fCu.getBuffer().getText(invocation.getStartPosition(), invocation.getLength());
  }

  public SimpleName[] getReferences() {
    if (fReferences != null) return fReferences;
    TempOccurrenceAnalyzer analyzer = new TempOccurrenceAnalyzer(getVariableDeclaration(), false);
    analyzer.perform();
    fReferences = analyzer.getReferenceNodes();
    return fReferences;
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
            element, getName(), IJavaRefactorings.INLINE_LOCAL_VARIABLE);
      else {
        fCu = (ICompilationUnit) element;
        if (checkIfTempSelected().hasFatalError())
          return JavaRefactoringDescriptorUtil.createInputFatalStatus(
              element, getName(), IJavaRefactorings.INLINE_LOCAL_VARIABLE);
      }
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    return new RefactoringStatus();
  }
}

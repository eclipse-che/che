/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.refactoring.binary.StubCreator;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

public class ReplaceInvocationsRefactoring extends Refactoring {

  private static final String ID_REPLACE_INVOCATIONS =
      "org.eclipse.jdt.ui.replace.invocations"; // $NON-NLS-1$
  private static final String ATTRIBUTE_MODE = "mode"; // $NON-NLS-1$

  public static class Mode {
    private Mode() {}

    public static final Mode REPLACE_ALL = new Mode();
    public static final Mode REPLACE_SINGLE = new Mode();
  }

  private final ITypeRoot fSelectionTypeRoot;
  /** only set if initial mode is REPLACE_SINGLE */
  private final int fSelectionStart;
  /** only set if initial mode is REPLACE_SINGLE */
  private final int fSelectionLength;

  private ASTNode fSelectionNode;
  /** only set after checkInitialConditions */
  private IMethod fMethod;

  private String fBody;
  private String[] fParameterNames;

  private SourceProvider fSourceProvider; // resolved once in checkInitialConditions
  private TargetProvider fTargetProvider; // flexible

  private TextChangeManager fChangeManager;
  private IMethodBinding fMethodBinding;

  public ReplaceInvocationsRefactoring(ITypeRoot typeRoot, int offset, int length) {
    fSelectionTypeRoot = typeRoot;
    fSelectionStart = offset;
    fSelectionLength = length;
  }

  public ReplaceInvocationsRefactoring(IMethod method) {
    fMethod = method;
    fSelectionTypeRoot = method.getTypeRoot();
    fSelectionStart = -1;
    fSelectionLength = -1;
  }

  public ReplaceInvocationsRefactoring(
      JavaRefactoringArguments arguments, RefactoringStatus status) {
    this(null, 0, 0);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
  }

  @Override
  public String getName() {
    return RefactoringCoreMessages.ReplaceInvocationsRefactoring_name;
  }

  /*
   * Only to be called after {@link #checkInitialConditions(IProgressMonitor)}
   */
  public boolean canReplaceSingle() {
    return fSelectionNode instanceof MethodInvocation;
  }

  /*
   * Only to be called after {@link #checkInitialConditions(IProgressMonitor)}
   */
  public RefactoringStatus setCurrentMode(Mode mode) throws JavaModelException {
    if (fTargetProvider.isSingle() == (mode == Mode.REPLACE_SINGLE)) return new RefactoringStatus();
    Assert.isTrue(canReplaceSingle());
    if (mode == Mode.REPLACE_SINGLE) {
      fTargetProvider =
          TargetProvider.create(
              (ICompilationUnit) fSelectionTypeRoot, (MethodInvocation) fSelectionNode);
    } else {
      fTargetProvider = TargetProvider.create(fSourceProvider.getDeclaration());
    }
    return fTargetProvider.checkActivation();
  }

  public void setBody(String body, String[] parameterNames) {
    // TODO: validate parameter name count and body
    fBody = body;
    fParameterNames = parameterNames;
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    // TargetProvider must get an untampered AST with original invocation node
    // SourceProvider must get a tweaked AST with method body / parameter names replaced

    RefactoringStatus result = new RefactoringStatus();

    if (fMethod == null) {
      if (!(fSelectionTypeRoot instanceof ICompilationUnit))
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReplaceInvocationsRefactoring_cannot_replace_in_binary);

      ICompilationUnit cu = (ICompilationUnit) fSelectionTypeRoot;
      CompilationUnit root = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(cu, true);
      fSelectionNode = getTargetNode(cu, root, fSelectionStart, fSelectionLength);
      if (fSelectionNode == null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.ReplaceInvocationsRefactoring_select_method_to_apply);

      if (fSelectionNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) fSelectionNode;
        fTargetProvider = TargetProvider.create(methodDeclaration);
        fMethodBinding = methodDeclaration.resolveBinding();
      } else {
        MethodInvocation methodInvocation = (MethodInvocation) fSelectionNode;
        fTargetProvider = TargetProvider.create(cu, methodInvocation);
        fMethodBinding = methodInvocation.resolveMethodBinding();
      }
      if (fMethodBinding == null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.InlineMethodRefactoring_error_noMethodDeclaration);
      fMethod = (IMethod) fMethodBinding.getJavaElement();

    } else {
      ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
      parser.setProject(fMethod.getJavaProject());
      IBinding[] bindings = parser.createBindings(new IJavaElement[] {fMethod}, null);
      fMethodBinding = (IMethodBinding) bindings[0];
      if (fMethodBinding == null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.InlineMethodRefactoring_error_noMethodDeclaration);

      fTargetProvider = TargetProvider.create(fMethodBinding);
    }

    result.merge(fTargetProvider.checkActivation());
    return result;
  }

  private SourceProvider resolveSourceProvider(
      IMethodBinding methodBinding, RefactoringStatus status) throws JavaModelException {
    final IMethod method = (IMethod) methodBinding.getJavaElement();

    ITypeRoot typeRoot;
    IDocument source;
    CompilationUnit methodDeclarationAstRoot;

    ICompilationUnit methodCu = (method).getCompilationUnit();
    if (methodCu != null) {
      typeRoot = methodCu;
      ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
      parser.setSource(methodCu);
      parser.setFocalPosition(method.getNameRange().getOffset());
      CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
      MethodDeclaration methodDecl =
          (MethodDeclaration)
              NodeFinder.perform(compilationUnit, method.getNameRange()).getParent();
      AST ast = compilationUnit.getAST();
      ASTRewrite rewrite = ASTRewrite.create(ast);
      Block newBody = ast.newBlock();
      newBody.statements().add(rewrite.createStringPlaceholder(fBody, ASTNode.EMPTY_STATEMENT));
      rewrite.replace(methodDecl.getBody(), newBody, null);
      List<SingleVariableDeclaration> parameters = methodDecl.parameters();
      for (int i = 0; i < parameters.size(); i++) {
        SingleVariableDeclaration parameter = parameters.get(i);
        rewrite.set(parameter.getName(), SimpleName.IDENTIFIER_PROPERTY, fParameterNames[i], null);
      }
      TextEdit textEdit = rewrite.rewriteAST();
      Document document = new Document(methodCu.getBuffer().getContents());
      try {
        textEdit.apply(document);
      } catch (MalformedTreeException e) {
        JavaPlugin.log(e);
      } catch (BadLocationException e) {
        JavaPlugin.log(e);
      }
      source = document;

      methodDeclarationAstRoot =
          new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL)
              .parse(source.get(), methodCu, true, true, null);

    } else {
      IClassFile classFile = method.getClassFile();
      // TODO: use source if available?
      StubCreator stubCreator =
          new StubCreator(true) {
            @Override
            protected void appendMethodBody(IMethod currentMethod) throws JavaModelException {
              if (currentMethod.equals(method)) {
                fBuffer.append(fBody);
              } else {
                super.appendMethodBody(currentMethod);
              }
            }
            /*
             * @see org.eclipse.jdt.internal.corext.refactoring.binary.StubCreator#appendMethodParameterName(org.eclipse.jdt.core.IMethod, int)
             */
            @Override
            protected void appendMethodParameterName(IMethod currentMethod, int index) {
              if (currentMethod.equals(method)) {
                fBuffer.append(fParameterNames[index]);
              } else {
                super.appendMethodParameterName(currentMethod, index);
              }
            }
          };

      String stub = stubCreator.createStub(classFile.getType(), null);
      source = new Document(stub);
      methodDeclarationAstRoot =
          new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL)
              .parse(stub, classFile, true, true, null);
      typeRoot = classFile;
    }
    ASTNode node = methodDeclarationAstRoot.findDeclaringNode(methodBinding.getKey());
    if (node instanceof MethodDeclaration) {
      return new SourceProvider(typeRoot, source, (MethodDeclaration) node);
    } else {
      status.addFatalError(
          RefactoringCoreMessages.ReplaceInvocationsRefactoring_cannot_find_method_declaration);
      return null;
    }
  }

  /*
   * @return an invocation or declaration node
   */
  private static ASTNode getTargetNode(
      ICompilationUnit unit, CompilationUnit root, int offset, int length) {
    ASTNode node = null;
    try {
      node = checkNode(NodeFinder.perform(root, offset, length, unit));
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
      case ASTNode.METHOD_DECLARATION:
      case ASTNode.METHOD_INVOCATION:
        // not yet...
        //			case ASTNode.SUPER_METHOD_INVOCATION:
        //			case ASTNode.CONSTRUCTOR_INVOCATION:
        return node;
    }
    return null;
  }

  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    pm.beginTask("", 20); // $NON-NLS-1$
    fChangeManager = new TextChangeManager();
    RefactoringStatus result = new RefactoringStatus();

    fSourceProvider = resolveSourceProvider(fMethodBinding, result);
    if (result.hasFatalError()) return result;

    result.merge(fSourceProvider.checkActivation());
    if (result.hasFatalError()) return result;

    fSourceProvider.initialize();
    fTargetProvider.initialize();

    pm.setTaskName(RefactoringCoreMessages.InlineMethodRefactoring_searching);
    RefactoringStatus searchStatus = new RefactoringStatus();
    String binaryRefsDescription =
        Messages.format(
            RefactoringCoreMessages.ReferencesInBinaryContext_ref_in_binaries_description,
            BasicElementLabels.getJavaElementName(fSourceProvider.getMethodName()));
    ReferencesInBinaryContext binaryRefs = new ReferencesInBinaryContext(binaryRefsDescription);
    ICompilationUnit[] units =
        fTargetProvider.getAffectedCompilationUnits(
            searchStatus, binaryRefs, new SubProgressMonitor(pm, 1));
    binaryRefs.addErrorIfNecessary(searchStatus);

    if (searchStatus.hasFatalError()) {
      result.merge(searchStatus);
      return result;
    }
    IFile[] filesToBeModified = getFilesToBeModified(units);
    result.merge(Checks.validateModifiesFiles(filesToBeModified, getValidationContext()));
    if (result.hasFatalError()) return result;
    result.merge(
        ResourceChangeChecker.checkFilesToBeChanged(
            filesToBeModified, new SubProgressMonitor(pm, 1)));
    checkOverridden(result, new SubProgressMonitor(pm, 4));
    IProgressMonitor sub = new SubProgressMonitor(pm, 15);
    sub.beginTask("", units.length * 3); // $NON-NLS-1$
    for (int c = 0; c < units.length; c++) {
      ICompilationUnit unit = units[c];
      sub.subTask(
          Messages.format(
              RefactoringCoreMessages.InlineMethodRefactoring_processing,
              BasicElementLabels.getFileName(unit)));
      CallInliner inliner = null;
      try {
        boolean added = false;
        MultiTextEdit root = new MultiTextEdit();
        CompilationUnitChange change = (CompilationUnitChange) fChangeManager.get(unit);
        change.setEdit(root);
        BodyDeclaration[] bodies =
            fTargetProvider.getAffectedBodyDeclarations(unit, new SubProgressMonitor(pm, 1));
        if (bodies.length == 0) continue;
        inliner = new CallInliner(unit, (CompilationUnit) bodies[0].getRoot(), fSourceProvider);
        for (int b = 0; b < bodies.length; b++) {
          BodyDeclaration body = bodies[b];
          inliner.initialize(body);
          RefactoringStatus nestedInvocations = new RefactoringStatus();
          ASTNode[] invocations =
              removeNestedCalls(
                  nestedInvocations,
                  unit,
                  fTargetProvider.getInvocations(body, new SubProgressMonitor(sub, 2)));
          for (int i = 0; i < invocations.length; i++) {
            ASTNode invocation = invocations[i];
            result.merge(inliner.initialize(invocation, fTargetProvider.getStatusSeverity()));
            if (result.hasFatalError()) break;
            if (result.getSeverity() < fTargetProvider.getStatusSeverity()) {
              added = true;
              TextEditGroup group =
                  new TextEditGroup(RefactoringCoreMessages.InlineMethodRefactoring_edit_inline);
              change.addTextEditGroup(group);
              result.merge(inliner.perform(group));
            }
          }
          // do this after we have inlined the method calls. We still want
          // to generate the modifications.
          result.merge(nestedInvocations);
        }
        if (!added) {
          fChangeManager.remove(unit);
        } else {
          root.addChild(inliner.getModifications());
          ImportRewrite rewrite = inliner.getImportEdit();
          if (rewrite.hasRecordedChanges()) {
            TextEdit edit = rewrite.rewriteImports(null);
            if (edit instanceof MultiTextEdit
                ? ((MultiTextEdit) edit).getChildrenSize() > 0
                : true) {
              root.addChild(edit);
              change.addTextEditGroup(
                  new TextEditGroup(
                      RefactoringCoreMessages.InlineMethodRefactoring_edit_import,
                      new TextEdit[] {edit}));
            }
          }
        }
      } finally {
        if (inliner != null) inliner.dispose();
      }
      sub.worked(1);
      if (sub.isCanceled()) throw new OperationCanceledException();
    }
    result.merge(searchStatus);
    sub.done();
    pm.done();
    return result;
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    // TODO: update for fSelectionStart == -1
    final Map<String, String> arguments = new HashMap<String, String>();
    String project = null;
    IJavaProject javaProject = fSelectionTypeRoot.getJavaProject();
    if (javaProject != null) project = javaProject.getElementName();
    final IMethodBinding binding = fSourceProvider.getDeclaration().resolveBinding();
    int flags =
        RefactoringDescriptor.STRUCTURAL_CHANGE
            | JavaRefactoringDescriptor.JAR_REFACTORING
            | JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
    if (!Modifier.isPrivate(binding.getModifiers())) flags |= RefactoringDescriptor.MULTI_CHANGE;
    final String description =
        Messages.format(
            RefactoringCoreMessages.ReplaceInvocationsRefactoring_descriptor_description_short,
            BasicElementLabels.getJavaElementName(binding.getName()));
    final String header =
        Messages.format(
            RefactoringCoreMessages.ReplaceInvocationsRefactoring_descriptor_description,
            new String[] {
              BindingLabelProvider.getBindingLabel(binding, JavaElementLabels.ALL_FULLY_QUALIFIED),
              BindingLabelProvider.getBindingLabel(
                  binding.getDeclaringClass(), JavaElementLabels.ALL_FULLY_QUALIFIED)
            });
    final JDTRefactoringDescriptorComment comment =
        new JDTRefactoringDescriptorComment(project, this, header);
    comment.addSetting(
        Messages.format(
            RefactoringCoreMessages.ReplaceInvocationsRefactoring_original_pattern,
            BindingLabelProvider.getBindingLabel(binding, JavaElementLabels.ALL_FULLY_QUALIFIED)));
    if (!fTargetProvider.isSingle())
      comment.addSetting(RefactoringCoreMessages.ReplaceInvocationsRefactoring_replace_references);
    final JavaRefactoringDescriptor descriptor =
        new JavaRefactoringDescriptor(
            ID_REPLACE_INVOCATIONS,
            project,
            description,
            comment.asString(),
            arguments,
            flags) {}; // REVIEW Unregistered ID!
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
        JavaRefactoringDescriptorUtil.elementToHandle(project, fSelectionTypeRoot));
    arguments.put(
        JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION,
        new Integer(fSelectionStart).toString()
            + " "
            + new Integer(fSelectionLength).toString()); // $NON-NLS-1$
    arguments.put(ATTRIBUTE_MODE, new Integer(fTargetProvider.isSingle() ? 0 : 1).toString());
    return new DynamicValidationRefactoringChange(
        descriptor,
        RefactoringCoreMessages.ReplaceInvocationsRefactoring_change_name,
        fChangeManager.getAllChanges());
  }

  private IFile[] getFilesToBeModified(ICompilationUnit[] units) {
    List<IFile> result = new ArrayList<IFile>(units.length + 1);
    IFile file;
    for (int i = 0; i < units.length; i++) {
      file = getFile(units[i]);
      if (file != null) result.add(file);
    }
    return result.toArray(new IFile[result.size()]);
  }

  private IFile getFile(ICompilationUnit unit) {
    unit = unit.getPrimary();
    IResource resource = unit.getResource();
    if (resource != null && resource.getType() == IResource.FILE) return (IFile) resource;
    return null;
  }

  private void checkOverridden(RefactoringStatus status, IProgressMonitor pm)
      throws JavaModelException {
    pm.beginTask("", 9); // $NON-NLS-1$
    pm.setTaskName(RefactoringCoreMessages.InlineMethodRefactoring_checking_overridden);
    MethodDeclaration decl = fSourceProvider.getDeclaration();
    IMethod method = (IMethod) decl.resolveBinding().getJavaElement();
    if (method == null || Flags.isPrivate(method.getFlags())) {
      pm.worked(8);
      return;
    }
    IType type = method.getDeclaringType();
    ITypeHierarchy hierarchy = type.newTypeHierarchy(new SubProgressMonitor(pm, 6));
    checkSubTypes(status, method, hierarchy.getAllSubtypes(type), new SubProgressMonitor(pm, 1));
    checkSuperClasses(
        status, method, hierarchy.getAllSuperclasses(type), new SubProgressMonitor(pm, 1));
    checkSuperInterfaces(
        status, method, hierarchy.getAllSuperInterfaces(type), new SubProgressMonitor(pm, 1));
    pm.setTaskName(""); // $NON-NLS-1$
  }

  private void checkSubTypes(
      RefactoringStatus result, IMethod method, IType[] types, IProgressMonitor pm) {
    checkTypes(
        result,
        method,
        types,
        RefactoringCoreMessages.InlineMethodRefactoring_checking_overridden_error,
        pm);
  }

  private void checkSuperClasses(
      RefactoringStatus result, IMethod method, IType[] types, IProgressMonitor pm) {
    checkTypes(
        result,
        method,
        types,
        RefactoringCoreMessages.InlineMethodRefactoring_checking_overrides_error,
        pm);
  }

  private void checkSuperInterfaces(
      RefactoringStatus result, IMethod method, IType[] types, IProgressMonitor pm) {
    checkTypes(
        result,
        method,
        types,
        RefactoringCoreMessages.InlineMethodRefactoring_checking_implements_error,
        pm);
  }

  private void checkTypes(
      RefactoringStatus result, IMethod method, IType[] types, String key, IProgressMonitor pm) {
    pm.beginTask("", types.length); // $NON-NLS-1$
    for (int i = 0; i < types.length; i++) {
      pm.worked(1);
      IMethod[] overridden = types[i].findMethods(method);
      if (overridden != null && overridden.length > 0) {
        result.addError(
            Messages.format(key, BasicElementLabels.getJavaElementName(types[i].getElementName())),
            JavaStatusContext.create(overridden[0]));
      }
    }
  }

  private ASTNode[] removeNestedCalls(
      RefactoringStatus status, ICompilationUnit unit, ASTNode[] invocations) {
    if (invocations.length <= 1) return invocations;
    ASTNode[] parents = new ASTNode[invocations.length];
    for (int i = 0; i < invocations.length; i++) {
      parents[i] = invocations[i].getParent();
    }
    for (int i = 0; i < invocations.length; i++) {
      removeNestedCalls(status, unit, parents, invocations, i);
    }
    List<ASTNode> result = new ArrayList<ASTNode>();
    for (int i = 0; i < invocations.length; i++) {
      if (invocations[i] != null) result.add(invocations[i]);
    }
    return result.toArray(new ASTNode[result.size()]);
  }

  private void removeNestedCalls(
      RefactoringStatus status,
      ICompilationUnit unit,
      ASTNode[] parents,
      ASTNode[] invocations,
      int index) {
    ASTNode invocation = invocations[index];
    for (int i = 0; i < parents.length; i++) {
      ASTNode parent = parents[i];
      while (parent != null) {
        if (parent == invocation) {
          status.addError(
              RefactoringCoreMessages.InlineMethodRefactoring_nestedInvocation,
              JavaStatusContext.create(unit, parent));
          invocations[index] = null;
        }
        parent = parent.getParent();
      }
    }
  }

  private RefactoringStatus initialize(JavaRefactoringArguments arguments) {
    final String value = arguments.getAttribute(ATTRIBUTE_MODE);
    if (value != null && !"".equals(value)) { // $NON-NLS-1$
      int mode = 0;
      try {
        mode = Integer.parseInt(value);
      } catch (NumberFormatException exception) {
        return RefactoringStatus.createFatalErrorStatus(
            Messages.format(
                RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
                ATTRIBUTE_MODE));
      }
      try {
        setCurrentMode(mode == 1 ? Mode.REPLACE_ALL : Mode.REPLACE_SINGLE);
      } catch (JavaModelException exception) {
        return RefactoringStatus.createFatalErrorStatus(exception.getLocalizedMessage());
      }
    }
    return new RefactoringStatus();
  }

  public IMethod getMethod() {
    return fMethod;
  }
}

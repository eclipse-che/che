/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Samrat Dhillon
 * <samrat.dhillon@gmail.com> - [introduce factory] Introduce Factory on an abstract class adds a
 * statement to create an instance of that class -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395016
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceFactoryDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine2;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.ASTCreator;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.MethodsSourcePositionComparator;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.BindingLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Refactoring class that permits the substitution of a factory method for direct calls to a given
 * constructor.
 *
 * @author rfuhrer
 */
public class IntroduceFactoryRefactoring extends Refactoring {

  private static final String ATTRIBUTE_PROTECT = "protect"; // $NON-NLS-1$

  /**
   * The handle for the compilation unit holding the selection that was passed into this
   * refactoring.
   */
  private ICompilationUnit fCUHandle;

  /**
   * The AST for the compilation unit holding the selection that was passed into this refactoring.
   */
  private CompilationUnit fCU;

  /** Handle for compilation unit in which the factory method/class/interface will be generated. */
  private ICompilationUnit fFactoryUnitHandle;

  /**
   * The start of the original textual selection in effect when this refactoring was initiated. If
   * the refactoring was initiated from a structured selection (e.g. from the outline view), then
   * this refers to the textual selection that corresponds to the structured selection item.
   */
  private int fSelectionStart;

  /**
   * The length of the original textual selection in effect when this refactoring was initiated. If
   * the refactoring was initiated from a structured selection (e.g. from the outline view), then
   * this refers to the textual selection that corresponds to the structured selection item.
   */
  private int fSelectionLength;

  /** The AST node corresponding to the user's textual selection. */
  private ASTNode fSelectedNode;

  /** The method binding for the selected constructor. */
  private IMethodBinding fCtorBinding;

  /** <code>TypeDeclaration</code> for class containing the constructor to be encapsulated. */
  private AbstractTypeDeclaration fCtorOwningClass;

  /** The name to be given to the generated factory method. */
  private String fNewMethodName = null;

  /**
   * An array of <code>SearchResultGroup</code>'s of all call sites that refer to the constructor
   * signature in question.
   */
  private SearchResultGroup[] fAllCallsTo;

  /** The class that will own the factory method/class/interface. */
  private AbstractTypeDeclaration fFactoryOwningClass;

  /** The newly-generated factory method. */
  private MethodDeclaration fFactoryMethod = null;

  /**
   * An array containing the names of the constructor's formal arguments, if available, otherwise
   * "arg1" ... "argN".
   */
  private String[] fFormalArgNames = null;

  /**
   * An array of <code>ITypeBinding</code>'s that describes the types of the constructor arguments,
   * in order.
   */
  private ITypeBinding[] fArgTypes;

  /** True iff the given constructor has a varargs signature. */
  private boolean fCtorIsVarArgs;

  /** If true, change the visibility of the constructor to protected to better encapsulate it. */
  private boolean fProtectConstructor = true;

  /**
   * An <code>ImportRewrite</code> that manages imports needed to satisfy newly-introduced type
   * references in the <code>ICompilationUnit</code> currently being rewritten during <code>
   * createChange()</code>.
   */
  private ImportRewrite fImportRewriter;

  /**
   * True iff there are call sites for the constructor to be encapsulated located in binary classes.
   */
  private boolean fCallSitesInBinaryUnits;

  /** <code>CompilationUnit</code> in which the factory is to be created. */
  private CompilationUnit fFactoryCU;

  /**
   * The fully qualified name of the factory class. This is only used if invoked from a refactoring
   * script.
   */
  private String fFactoryClassName;

  private int fConstructorVisibility = Modifier.PRIVATE;

  /**
   * Creates a new <code>IntroduceFactoryRefactoring</code> with the given selection on the given
   * compilation unit.
   *
   * @param cu the <code>ICompilationUnit</code> in which the user selection was made, or <code>null
   *     </code> if invoked from scripting
   * @param selectionStart the start of the textual selection in <code>cu</code>
   * @param selectionLength the length of the textual selection in <code>cu</code>
   */
  public IntroduceFactoryRefactoring(ICompilationUnit cu, int selectionStart, int selectionLength) {
    Assert.isTrue(selectionStart >= 0);
    Assert.isTrue(selectionLength >= 0);
    fSelectionStart = selectionStart;
    fSelectionLength = selectionLength;
    fCUHandle = cu;
    if (cu != null) initialize();
  }

  public IntroduceFactoryRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
    this(null, 0, 0);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
  }

  private void initialize() {
    fCU = ASTCreator.createAST(fCUHandle, null);
  }

  /**
   * Finds and returns the <code>ASTNode</code> for the given source text selection, if it is an
   * entire constructor call or the class name portion of a constructor call or constructor
   * declaration, or null otherwise.
   *
   * @param unit The compilation unit in which the selection was made
   * @param offset The textual offset of the start of the selection
   * @param length The length of the selection in characters
   * @return ClassInstanceCreation or MethodDeclaration
   */
  private ASTNode getTargetNode(ICompilationUnit unit, int offset, int length) {
    ASTNode node = ASTNodes.getNormalizedNode(NodeFinder.perform(fCU, offset, length));
    if (node.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) return node;
    if (node.getNodeType() == ASTNode.METHOD_DECLARATION
        && ((MethodDeclaration) node).isConstructor()) return node;
    // we have some sub node. Make sure its the right child of the parent
    StructuralPropertyDescriptor location = node.getLocationInParent();
    ASTNode parent = node.getParent();
    if (location == ClassInstanceCreation.TYPE_PROPERTY) {
      return parent;
    } else if (location == MethodDeclaration.NAME_PROPERTY
        && ((MethodDeclaration) parent).isConstructor()) {
      return parent;
    }
    return null;
  }

  /**
   * Determines what kind of AST node was selected, and returns an error status if the kind of node
   * is inappropriate for this refactoring.
   *
   * @param pm
   * @return a RefactoringStatus indicating whether the selection is valid
   * @throws JavaModelException
   */
  private RefactoringStatus checkSelection(IProgressMonitor pm) throws JavaModelException {
    try {
      pm.beginTask(RefactoringCoreMessages.IntroduceFactory_examiningSelection, 2);

      fSelectedNode = getTargetNode(fCUHandle, fSelectionStart, fSelectionLength);

      if (fSelectedNode == null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_notAConstructorInvocation);

      // getTargetNode() must return either a ClassInstanceCreation or a
      // constructor MethodDeclaration; nothing else.
      if (fSelectedNode instanceof ClassInstanceCreation) {
        ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) fSelectedNode;
        fCtorBinding = classInstanceCreation.resolveConstructorBinding();
      } else if (fSelectedNode instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) fSelectedNode;
        fCtorBinding = methodDeclaration.resolveBinding();
      }

      if (fCtorBinding == null)
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_unableToResolveConstructorBinding);

      // If this constructor is of a generic type, get the generic version,
      // not some instantiation thereof.
      fCtorBinding = fCtorBinding.getMethodDeclaration();

      pm.worked(1);

      // We don't handle constructors of nested types at the moment
      if (fCtorBinding.getDeclaringClass().isNested())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_unsupportedNestedTypes);

      ITypeBinding ctorType = fCtorBinding.getDeclaringClass();
      IType ctorOwningType = (IType) ctorType.getJavaElement();

      if (ctorOwningType.isBinary())
        // Can't modify binary CU; don't know what CU to put factory method
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_constructorInBinaryClass);
      if (ctorOwningType.isEnum())
        // Doesn't make sense to encapsulate enum constructors
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_constructorInEnum);

      // Put the generated factory method inside the type that owns the constructor
      fFactoryUnitHandle = ctorOwningType.getCompilationUnit();
      fFactoryCU = getASTFor(fFactoryUnitHandle);

      Name ctorOwnerName = (Name) NodeFinder.perform(fFactoryCU, ctorOwningType.getNameRange());

      fCtorOwningClass =
          (AbstractTypeDeclaration)
              ASTNodes.getParent(ctorOwnerName, AbstractTypeDeclaration.class);
      fFactoryOwningClass = fCtorOwningClass;

      pm.worked(1);

      if (fNewMethodName == null)
        return setNewMethodName("create" + fCtorBinding.getName()); // $NON-NLS-1$
      else return new RefactoringStatus();
    } finally {
      pm.done();
    }
  }

  /*
   * @see org.eclipse.jdt.internal.corext.refactoring.base.Refactoring#checkActivation(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.IntroduceFactory_checkingActivation, 1);

      if (!fCUHandle.isStructureKnown())
        return RefactoringStatus.createFatalErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_syntaxError);

      return checkSelection(new SubProgressMonitor(pm, 1));
    } finally {
      pm.done();
    }
  }

  /**
   * @param searchHits
   * @return the set of compilation units that will be affected by this particular invocation of
   *     this refactoring. This in general includes the class containing the constructor in
   *     question, as well as all call sites to the constructor.
   */
  private ICompilationUnit[] collectAffectedUnits(SearchResultGroup[] searchHits) {
    Collection<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
    boolean hitInFactoryClass = false;

    for (int i = 0; i < searchHits.length; i++) {
      SearchResultGroup rg = searchHits[i];
      ICompilationUnit icu = rg.getCompilationUnit();

      result.add(icu);
      if (icu.equals(fFactoryUnitHandle)) hitInFactoryClass = true;
    }
    if (!hitInFactoryClass) result.add(fFactoryUnitHandle);
    return result.toArray(new ICompilationUnit[result.size()]);
  }

  /**
   * @param ctor
   * @param methodBinding
   * @return a <code>SearchPattern</code> that finds all calls to the constructor identified by the
   *     argument <code>methodBinding</code>.
   */
  private SearchPattern createSearchPattern(IMethod ctor, IMethodBinding methodBinding) {
    Assert.isNotNull(
        methodBinding, RefactoringCoreMessages.IntroduceFactory_noBindingForSelectedConstructor);

    if (ctor != null)
      return SearchPattern.createPattern(
          ctor, IJavaSearchConstants.REFERENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
    else { // perhaps a synthetic method? (but apparently not always... hmmm...)
      // Can't find an IMethod for this method, so build a string pattern instead
      StringBuffer buf = new StringBuffer();

      buf.append(methodBinding.getDeclaringClass().getQualifiedName()).append("("); // $NON-NLS-1$
      for (int i = 0; i < fArgTypes.length; i++) {
        if (i != 0) buf.append(","); // $NON-NLS-1$
        buf.append(fArgTypes[i].getQualifiedName());
      }
      buf.append(")"); // $NON-NLS-1$
      return SearchPattern.createPattern(
          buf.toString(),
          IJavaSearchConstants.CONSTRUCTOR,
          IJavaSearchConstants.REFERENCES,
          SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
    }
  }

  private IJavaSearchScope createSearchScope(IMethod ctor, IMethodBinding binding)
      throws JavaModelException {
    if (ctor != null) {
      return RefactoringScopeFactory.create(ctor);
    } else {
      ITypeBinding type = Bindings.getTopLevelType(binding.getDeclaringClass());
      return RefactoringScopeFactory.create(type.getJavaElement());
    }
  }

  /**
   * @param groups
   * @return an array of <code>SearchResultGroup</code>'s like the argument, but omitting those
   *     groups that have no corresponding compilation unit (i.e. are binary and therefore can't be
   *     modified).
   */
  private SearchResultGroup[] excludeBinaryUnits(SearchResultGroup[] groups) {
    Collection<SearchResultGroup> result = new ArrayList<SearchResultGroup>();

    for (int i = 0; i < groups.length; i++) {
      SearchResultGroup rg = groups[i];
      ICompilationUnit unit = rg.getCompilationUnit();

      if (unit != null) // ignore hits within a binary unit
      result.add(rg);
      else fCallSitesInBinaryUnits = true;
    }
    return result.toArray(new SearchResultGroup[result.size()]);
  }

  /**
   * Search for all calls to the given <code>IMethodBinding</code> in the project that contains the
   * compilation unit <code>fCUHandle</code>.
   *
   * @param methodBinding
   * @param pm
   * @param status
   * @return an array of <code>SearchResultGroup</code>'s that identify the search matches
   * @throws JavaModelException
   */
  private SearchResultGroup[] searchForCallsTo(
      IMethodBinding methodBinding, IProgressMonitor pm, RefactoringStatus status)
      throws JavaModelException {
    IMethod method = (IMethod) methodBinding.getJavaElement();
    final RefactoringSearchEngine2 engine =
        new RefactoringSearchEngine2(createSearchPattern(method, methodBinding));
    engine.setFiltering(true, true);
    engine.setScope(createSearchScope(method, methodBinding));
    engine.setStatus(status);
    engine.searchPattern(new SubProgressMonitor(pm, 1));
    return (SearchResultGroup[]) engine.getResults();
  }

  /**
   * Returns an array of <code>SearchResultGroup</code>'s containing all method calls in the Java
   * project that invoke the constructor identified by the given <code>IMethodBinding</code>
   *
   * @param ctorBinding an <code>IMethodBinding</code> identifying a particular constructor
   *     signature to search for
   * @param pm an <code>IProgressMonitor</code> to use during this potentially lengthy operation
   * @param status
   * @return an array of <code>SearchResultGroup</code>'s identifying all calls to the given
   *     constructor signature
   * @throws JavaModelException
   */
  private SearchResultGroup[] findAllCallsTo(
      IMethodBinding ctorBinding, IProgressMonitor pm, RefactoringStatus status)
      throws JavaModelException {
    SearchResultGroup[] groups = excludeBinaryUnits(searchForCallsTo(ctorBinding, pm, status));

    return groups;
  }

  private IType findNonPrimaryType(
      String fullyQualifiedName, IProgressMonitor pm, RefactoringStatus status)
      throws JavaModelException {
    SearchPattern p =
        SearchPattern.createPattern(
            fullyQualifiedName,
            IJavaSearchConstants.TYPE,
            IJavaSearchConstants.DECLARATIONS,
            SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
    final RefactoringSearchEngine2 engine = new RefactoringSearchEngine2(p);

    engine.setFiltering(true, true);
    engine.setScope(RefactoringScopeFactory.create(fCtorBinding.getJavaElement().getJavaProject()));
    engine.setStatus(status);
    engine.searchPattern(new SubProgressMonitor(pm, 1));

    SearchResultGroup[] groups = (SearchResultGroup[]) engine.getResults();

    if (groups.length != 0) {
      for (int i = 0; i < groups.length; i++) {
        SearchMatch[] matches = groups[i].getSearchResults();
        for (int j = 0; j < matches.length; j++) {
          if (matches[j].getAccuracy() == SearchMatch.A_ACCURATE)
            return (IType) matches[j].getElement();
        }
      }
    }
    return null;
  }

  /*
   * @see org.eclipse.jdt.internal.corext.refactoring.base.Refactoring#checkInput(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.IntroduceFactory_checking_preconditions, 1);
      RefactoringStatus result = new RefactoringStatus();

      if (fFactoryClassName != null) result.merge(setFactoryClass(fFactoryClassName));
      if (result.hasFatalError()) return result;
      fArgTypes = fCtorBinding.getParameterTypes();
      fCtorIsVarArgs = fCtorBinding.isVarargs();
      fAllCallsTo = findAllCallsTo(fCtorBinding, pm, result);
      fFormalArgNames = findCtorArgNames();

      ICompilationUnit[] affectedFiles = collectAffectedUnits(fAllCallsTo);
      result.merge(
          Checks.validateModifiesFiles(
              ResourceUtil.getFiles(affectedFiles), getValidationContext()));

      if (fCallSitesInBinaryUnits)
        result.merge(
            RefactoringStatus.createWarningStatus(
                RefactoringCoreMessages.IntroduceFactory_callSitesInBinaryClass));

      if (Modifier.isAbstract(fCtorBinding.getDeclaringClass().getModifiers())) {
        result.merge(
            RefactoringStatus.createWarningStatus(
                RefactoringCoreMessages.IntroduceFactory_abstractClass));
      }

      return result;
    } finally {
      pm.done();
    }
  }

  /**
   * @return an array containing the argument names for the constructor identified by <code>
   *     fCtorBinding</code>, if available, or default names if unavailable (e.g. if the constructor
   *     resides in a binary unit).
   */
  private String[] findCtorArgNames() {
    int numArgs = fCtorBinding.getParameterTypes().length;
    String[] names = new String[numArgs];

    CompilationUnit ctorUnit =
        (CompilationUnit) ASTNodes.getParent(fCtorOwningClass, CompilationUnit.class);
    MethodDeclaration ctorDecl =
        (MethodDeclaration) ctorUnit.findDeclaringNode(fCtorBinding.getKey());

    if (ctorDecl != null) {
      List<SingleVariableDeclaration> formalArgs = ctorDecl.parameters();
      int i = 0;

      for (Iterator<SingleVariableDeclaration> iter = formalArgs.iterator(); iter.hasNext(); i++) {
        SingleVariableDeclaration svd = iter.next();

        names[i] = svd.getName().getIdentifier();
      }
      return names;
    }

    // Have no way of getting the formal argument names; just fake it.
    for (int i = 0; i < numArgs; i++) names[i] = "arg" + (i + 1); // $NON-NLS-1$

    return names;
  }

  /**
   * Creates and returns a new MethodDeclaration that represents the factory method to be used in
   * place of direct calls to the constructor in question.
   *
   * @param ast An AST used as a factory for various AST nodes
   * @param ctorBinding binding for the constructor being wrapped
   * @param unitRewriter the ASTRewrite to be used
   * @return the new method declaration
   * @throws CoreException if an exception occurs while accessing its corresponding resource
   */
  private MethodDeclaration createFactoryMethod(
      AST ast, IMethodBinding ctorBinding, ASTRewrite unitRewriter) throws CoreException {
    MethodDeclaration newMethod = ast.newMethodDeclaration();
    SimpleName newMethodName = ast.newSimpleName(fNewMethodName);
    ClassInstanceCreation newCtorCall = ast.newClassInstanceCreation();
    ReturnStatement ret = ast.newReturnStatement();
    Block body = ast.newBlock();
    List<Statement> stmts = body.statements();
    String retTypeName = ctorBinding.getName();

    createFactoryMethodSignature(ast, newMethod);

    newMethod.setName(newMethodName);
    newMethod.setBody(body);

    ITypeBinding declaringClass = fCtorBinding.getDeclaringClass();
    ITypeBinding[] ctorOwnerTypeParameters = declaringClass.getTypeParameters();

    setMethodReturnType(newMethod, retTypeName, ctorOwnerTypeParameters, ast);

    newMethod
        .modifiers()
        .addAll(ASTNodeFactory.newModifiers(ast, Modifier.STATIC | Modifier.PUBLIC));

    setCtorTypeArguments(newCtorCall, retTypeName, ctorOwnerTypeParameters, ast);

    createFactoryMethodConstructorArgs(ast, newCtorCall);

    if (Modifier.isAbstract(declaringClass.getModifiers())) {
      AnonymousClassDeclaration decl = ast.newAnonymousClassDeclaration();
      IMethodBinding[] unimplementedMethods = getUnimplementedMethods(declaringClass);
      CodeGenerationSettings settings =
          JavaPreferencesSettings.getCodeGenerationSettings(fCUHandle.getJavaProject());
      ImportRewriteContext context =
          new ContextSensitiveImportRewriteContext(
              fFactoryCU, decl.getStartPosition(), fImportRewriter);
      for (int i = 0; i < unimplementedMethods.length; i++) {
        IMethodBinding unImplementedMethod = unimplementedMethods[i];
        MethodDeclaration newMethodDecl =
            StubUtility2.createImplementationStub(
                fCUHandle,
                unitRewriter,
                fImportRewriter,
                context,
                unImplementedMethod,
                unImplementedMethod.getDeclaringClass().getName(),
                settings,
                false);
        decl.bodyDeclarations().add(newMethodDecl);
      }
      newCtorCall.setAnonymousClassDeclaration(decl);
    }

    ret.setExpression(newCtorCall);
    stmts.add(ret);

    return newMethod;
  }

  private IMethodBinding[] getUnimplementedMethods(ITypeBinding binding) {
    IMethodBinding[] unimplementedMethods = StubUtility2.getUnimplementedMethods(binding, true);
    Arrays.sort(unimplementedMethods, new MethodsSourcePositionComparator(binding));
    return unimplementedMethods;
  }

  /**
   * Sets the type being instantiated in the given constructor call, including specifying any
   * necessary type arguments.
   *
   * @param newCtorCall the constructor call to modify
   * @param ctorTypeName the simple name of the type being instantiated
   * @param ctorOwnerTypeParameters the formal type parameters of the type being instantiated
   * @param ast utility object used to create AST nodes
   */
  private void setCtorTypeArguments(
      ClassInstanceCreation newCtorCall,
      String ctorTypeName,
      ITypeBinding[] ctorOwnerTypeParameters,
      AST ast) {
    if (ctorOwnerTypeParameters.length == 0) // easy, just a simple type
    newCtorCall.setType(ASTNodeFactory.newType(ast, ctorTypeName));
    else {
      Type baseType = ast.newSimpleType(ast.newSimpleName(ctorTypeName));
      ParameterizedType newInstantiatedType = ast.newParameterizedType(baseType);
      List<Type> newInstTypeArgs = newInstantiatedType.typeArguments();

      for (int i = 0; i < ctorOwnerTypeParameters.length; i++) {
        Type typeArg = ASTNodeFactory.newType(ast, ctorOwnerTypeParameters[i].getName());

        newInstTypeArgs.add(typeArg);
      }
      newCtorCall.setType(newInstantiatedType);
    }
  }

  /**
   * Sets the return type of the factory method, including any necessary type arguments. E.g., for
   * constructor <code>Foo()</code> in <code>Foo&lt;T&gt;</code>, the factory method defines a
   * method type parameter <code>&lt;T&gt;</code> and returns a <code>Foo&lt;T&gt;</code>.
   *
   * @param newMethod the method whose return type is to be set
   * @param retTypeName the simple name of the return type (without type parameters)
   * @param ctorOwnerTypeParameters the formal type parameters of the type that the factory method
   *     instantiates (whose constructor is being encapsulated)
   * @param ast utility object used to create AST nodes
   */
  private void setMethodReturnType(
      MethodDeclaration newMethod,
      String retTypeName,
      ITypeBinding[] ctorOwnerTypeParameters,
      AST ast) {
    if (ctorOwnerTypeParameters.length == 0)
      newMethod.setReturnType2(ast.newSimpleType(ast.newSimpleName(retTypeName)));
    else {
      Type baseType = ast.newSimpleType(ast.newSimpleName(retTypeName));
      ParameterizedType newRetType = ast.newParameterizedType(baseType);
      List<Type> newRetTypeArgs = newRetType.typeArguments();

      for (int i = 0; i < ctorOwnerTypeParameters.length; i++) {
        Type retTypeArg = ASTNodeFactory.newType(ast, ctorOwnerTypeParameters[i].getName());

        newRetTypeArgs.add(retTypeArg);
      }
      newMethod.setReturnType2(newRetType);
    }
  }

  /**
   * Creates and adds the necessary argument declarations to the given factory method.<br>
   * An argument is needed for each original constructor argument for which the evaluation of the
   * actual arguments across all calls was not able to be pushed inside the factory method (e.g.
   * arguments with side-effects, references to fields if the factory method is to be static or
   * reside in a factory class, or arguments that varied across the set of constructor calls).<br>
   * <code>fArgTypes</code> identifies such arguments by a <code>null</code> value.
   *
   * @param ast utility object used to create AST nodes
   * @param newMethod the <code>MethodDeclaration</code> for the factory method
   */
  private void createFactoryMethodSignature(AST ast, MethodDeclaration newMethod) {
    List<SingleVariableDeclaration> argDecls = newMethod.parameters();

    for (int i = 0; i < fArgTypes.length; i++) {
      SingleVariableDeclaration argDecl = ast.newSingleVariableDeclaration();
      Type argType;

      if (i == (fArgTypes.length - 1) && fCtorIsVarArgs) {
        // The trailing varargs arg has an extra array dimension, compared to
        // what we need to pass to setType()...
        argType =
            typeNodeForTypeBinding(
                fArgTypes[i].getElementType(), fArgTypes[i].getDimensions() - 1, ast);
        argDecl.setVarargs(true);
      } else argType = typeNodeForTypeBinding(fArgTypes[i], 0, ast);

      argDecl.setName(ast.newSimpleName(fFormalArgNames[i]));
      argDecl.setType(argType);
      argDecls.add(argDecl);
    }

    ITypeBinding[] ctorExcepts = fCtorBinding.getExceptionTypes();
    List<Type> exceptions = newMethod.thrownExceptionTypes();

    for (int i = 0; i < ctorExcepts.length; i++) {
      exceptions.add(fImportRewriter.addImport(ctorExcepts[i], ast));
    }

    copyTypeParameters(ast, newMethod);
  }

  /**
   * Copies the constructor's parent type's type parameters, if any, as method type parameters of
   * the new static factory method. (Recall that static methods can't refer to type arguments of the
   * enclosing class, since they have no instance to serve as a context.)<br>
   * Makes sure to copy the bounds from the owning type, to ensure that the return type of the
   * factory method satisfies the bounds of the type being instantiated.<br>
   * E.g., for ctor Foo() in the type Foo<T extends Number>, be sure that the factory method is
   * declared as<br>
   * <code>static <T extends Number> Foo<T> createFoo()</code><br>
   * and not simply<br>
   * <code>static <T> Foo<T> createFoo()</code><br>
   * or the compiler will bark.
   *
   * @param ast utility object needed to create ASTNode's for the new method
   * @param newMethod the method onto which to copy the type parameters
   */
  private void copyTypeParameters(AST ast, MethodDeclaration newMethod) {
    ITypeBinding[] ctorOwnerTypeParms = fCtorBinding.getDeclaringClass().getTypeParameters();
    List<TypeParameter> factoryMethodTypeParms = newMethod.typeParameters();
    for (int i = 0; i < ctorOwnerTypeParms.length; i++) {
      TypeParameter newParm = ast.newTypeParameter();
      ITypeBinding[] parmTypeBounds = ctorOwnerTypeParms[i].getTypeBounds();
      List<Type> newParmBounds = newParm.typeBounds();

      newParm.setName(ast.newSimpleName(ctorOwnerTypeParms[i].getName()));
      for (int b = 0; b < parmTypeBounds.length; b++) {
        if (parmTypeBounds[b].isClass() && parmTypeBounds[b].getSuperclass() == null) continue;

        Type newBound = fImportRewriter.addImport(parmTypeBounds[b], ast);

        newParmBounds.add(newBound);
      }
      factoryMethodTypeParms.add(newParm);
    }
  }

  /**
   * @param argType
   * @param extraDims number of extra array dimensions to add to the resulting type
   * @param ast
   * @return a Type that describes the given ITypeBinding. If the binding refers to an object type,
   *     use the import rewriter to determine whether the reference requires a new import, or
   *     instead needs to be qualified.<br>
   *     Like ASTNodeFactory.newType(), but for the handling of imports.
   */
  private Type typeNodeForTypeBinding(ITypeBinding argType, int extraDims, AST ast) {
    if (extraDims > 0) {
      return ast.newArrayType(typeNodeForTypeBinding(argType, 0, ast), extraDims);

    } else if (argType.isArray()) {
      Type elementType = typeNodeForTypeBinding(argType.getElementType(), extraDims, ast);
      return ast.newArrayType(elementType, argType.getDimensions());

    } else {
      return fImportRewriter.addImport(argType, ast);
    }
  }

  /**
   * Create the list of actual arguments to the constructor call that is encapsulated inside the
   * factory method, and associate the arguments with the given constructor call object.
   *
   * @param ast utility object used to create AST nodes
   * @param newCtorCall the newly-generated constructor call to be wrapped inside the factory method
   */
  private void createFactoryMethodConstructorArgs(AST ast, ClassInstanceCreation newCtorCall) {
    List<Expression> argList = newCtorCall.arguments();

    for (int i = 0; i < fArgTypes.length; i++) {
      ASTNode ctorArg = ast.newSimpleName(fFormalArgNames[i]);

      argList.add((Expression) ctorArg);
    }
  }

  /**
   * Updates the constructor call.
   *
   * @param ctorCall the ClassInstanceCreation to be marked as replaced
   * @param unitRewriter the AST rewriter
   * @param gd the edit group to use
   */
  private void rewriteFactoryMethodCall(
      ClassInstanceCreation ctorCall, ASTRewrite unitRewriter, TextEditGroup gd) {
    AST ast = unitRewriter.getAST();
    MethodInvocation factoryMethodCall = ast.newMethodInvocation();

    ASTNode ctorCallParent = ctorCall.getParent();
    StructuralPropertyDescriptor ctorCallLocation = ctorCall.getLocationInParent();
    if (ctorCallLocation instanceof ChildListPropertyDescriptor) {
      ListRewrite ctorCallParentListRewrite =
          unitRewriter.getListRewrite(
              ctorCallParent, (ChildListPropertyDescriptor) ctorCallLocation);
      int index = ctorCallParentListRewrite.getOriginalList().indexOf(ctorCall);
      ctorCall = (ClassInstanceCreation) ctorCallParentListRewrite.getRewrittenList().get(index);
    } else {
      ctorCall = (ClassInstanceCreation) unitRewriter.get(ctorCallParent, ctorCallLocation);
    }

    ListRewrite actualFactoryArgs =
        unitRewriter.getListRewrite(factoryMethodCall, MethodInvocation.ARGUMENTS_PROPERTY);
    ListRewrite actualCtorArgs =
        unitRewriter.getListRewrite(ctorCall, ClassInstanceCreation.ARGUMENTS_PROPERTY);

    // Need to use a qualified name for the factory method if we're not
    // in the context of the class holding the factory.
    AbstractTypeDeclaration callOwner =
        (AbstractTypeDeclaration) ASTNodes.getParent(ctorCall, AbstractTypeDeclaration.class);
    ITypeBinding callOwnerBinding = callOwner.resolveBinding();

    if (callOwnerBinding == null
        || !Bindings.equals(callOwner.resolveBinding(), fFactoryOwningClass.resolveBinding())) {
      String qualifier = fImportRewriter.addImport(fFactoryOwningClass.resolveBinding());
      factoryMethodCall.setExpression(ASTNodeFactory.newName(ast, qualifier));
    }

    factoryMethodCall.setName(ast.newSimpleName(fNewMethodName));

    List<Expression> actualCtorArgsList = actualCtorArgs.getRewrittenList();
    for (int i = 0; i < actualCtorArgsList.size(); i++) {
      Expression actualCtorArg = actualCtorArgsList.get(i);

      ASTNode movedArg;
      if (ASTNodes.isExistingNode(actualCtorArg)) {
        movedArg = unitRewriter.createMoveTarget(actualCtorArg);
      } else {
        unitRewriter.remove(actualCtorArg, null);
        movedArg = actualCtorArg;
      }

      actualFactoryArgs.insertLast(movedArg, gd);
    }

    unitRewriter.replace(ctorCall, factoryMethodCall, gd);
  }

  /**
   * @param unit
   * @return true iff the given <code>ICompilationUnit</code> is the unit containing the original
   *     constructor
   */
  private boolean isConstructorUnit(ICompilationUnit unit) {
    return unit.equals(ASTCreator.getCu(fCtorOwningClass));
  }

  /**
   * @return true iff we should actually change the original constructor's visibility to <code>
   *     protected</code>. This takes into account the user- requested mode and whether the
   *     constructor's compilation unit is in source form.
   */
  private boolean shouldProtectConstructor() {
    return fProtectConstructor && fCtorOwningClass != null;
  }

  /**
   * Creates and adds the necessary change to make the constructor method protected.
   *
   * @param unitAST
   * @param unitRewriter
   * @param declGD
   * @return false iff the constructor didn't exist (i.e. was implicit)
   */
  private boolean protectConstructor(
      CompilationUnit unitAST, ASTRewrite unitRewriter, TextEditGroup declGD) {
    MethodDeclaration constructor =
        (MethodDeclaration) unitAST.findDeclaringNode(fCtorBinding.getKey());

    // No need to rewrite the modifiers if the visibility is what we already want it to be.
    if (constructor == null || (JdtFlags.getVisibilityCode(constructor)) == fConstructorVisibility)
      return false;
    ModifierRewrite.create(unitRewriter, constructor).setVisibility(fConstructorVisibility, declGD);
    return true;
  }

  /**
   * Add all changes necessary on the <code>ICompilationUnit</code> in the given <code>
   * SearchResultGroup</code> to implement the refactoring transformation to the given <code>
   * CompilationUnitChange</code>.
   *
   * @param rg the <code>SearchResultGroup</code> for which changes should be created
   * @param unitHandle
   * @param unitChange the CompilationUnitChange object for the compilation unit in question
   * @return <code>true</code> iff a change has been added
   * @throws CoreException
   */
  private boolean addAllChangesFor(
      SearchResultGroup rg, ICompilationUnit unitHandle, CompilationUnitChange unitChange)
      throws CoreException {
    //		ICompilationUnit	unitHandle= rg.getCompilationUnit();
    Assert.isTrue(rg == null || rg.getCompilationUnit() == unitHandle);
    CompilationUnit unit = getASTFor(unitHandle);
    ASTRewrite unitRewriter = ASTRewrite.create(unit.getAST());
    MultiTextEdit root = new MultiTextEdit();
    boolean someChange = false;

    unitChange.setEdit(root);
    fImportRewriter = StubUtility.createImportRewrite(unit, true);

    // First create the factory method
    if (unitHandle.equals(fFactoryUnitHandle)) {
      TextEditGroup factoryGD =
          new TextEditGroup(RefactoringCoreMessages.IntroduceFactory_addFactoryMethod);

      createFactoryChange(unitRewriter, unit, factoryGD);
      unitChange.addTextEditGroup(factoryGD);
      someChange = true;
    }

    // Now rewrite all the constructor calls to use the factory method
    if (rg != null)
      if (replaceConstructorCalls(rg, unit, unitRewriter, unitChange)) someChange = true;

    // Finally, make the constructor private, if requested.
    if (shouldProtectConstructor() && isConstructorUnit(unitHandle)) {
      TextEditGroup declGD =
          new TextEditGroup(RefactoringCoreMessages.IntroduceFactory_protectConstructor);

      if (protectConstructor(unit, unitRewriter, declGD)) {
        unitChange.addTextEditGroup(declGD);
        someChange = true;
      }
    }

    if (someChange) {
      root.addChild(unitRewriter.rewriteAST());
      root.addChild(fImportRewriter.rewriteImports(null));
    }

    return someChange;
  }

  /**
   * @param unitHandle
   * @return an AST for the given compilation unit handle.<br>
   *     If this is the unit containing the selection or the unit in which the factory is to reside,
   *     checks the appropriate field (<code>fCU</code> or <code>fFactoryCU</code>, respectively)
   *     and initializes the field with a new AST only if not already done.
   */
  private CompilationUnit getASTFor(ICompilationUnit unitHandle) {
    if (unitHandle.equals(fCUHandle)) { // is this the unit containing the selection?
      if (fCU == null) {
        fCU = ASTCreator.createAST(unitHandle, null);
        if (fCU.equals(fFactoryUnitHandle)) // if selection unit and factory unit are the same...
        fFactoryCU = fCU; // ...make sure the factory unit gets initialized
      }
      return fCU;
    } else if (unitHandle.equals(fFactoryUnitHandle)) { // is this the "factory unit"?
      if (fFactoryCU == null) fFactoryCU = ASTCreator.createAST(unitHandle, null);
      return fFactoryCU;
    } else return ASTCreator.createAST(unitHandle, null);
  }

  /**
   * Use the given <code>ASTRewrite</code> to replace direct calls to the constructor with calls to
   * the newly-created factory method.
   *
   * @param rg the <code>SearchResultGroup</code> indicating all of the constructor references
   * @param unit the <code>CompilationUnit</code> to be rewritten
   * @param unitRewriter the rewriter
   * @param unitChange the compilation unit change
   * @throws CoreException
   * @return true iff at least one constructor call site was rewritten.
   */
  private boolean replaceConstructorCalls(
      SearchResultGroup rg,
      CompilationUnit unit,
      ASTRewrite unitRewriter,
      CompilationUnitChange unitChange)
      throws CoreException {
    Assert.isTrue(ASTCreator.getCu(unit).equals(rg.getCompilationUnit()));
    SearchMatch[] hits = rg.getSearchResults();
    Arrays.sort(
        hits,
        new Comparator<SearchMatch>() {
          /**
           * Sort by descending offset, such that nested constructor calls are processed first. This
           * is necessary, since they can only be moved into the factory method invocation after
           * they have been rewritten.
           */
          public int compare(SearchMatch m1, SearchMatch m2) {
            return m2.getOffset() - m1.getOffset();
          }
        });

    boolean someCallPatched = false;

    for (int i = 0; i < hits.length; i++) {
      ASTNode ctrCall = getCtorCallAt(hits[i].getOffset(), hits[i].getLength(), unit);

      if (ctrCall instanceof ClassInstanceCreation) {
        TextEditGroup gd = new TextEditGroup(RefactoringCoreMessages.IntroduceFactory_replaceCalls);

        rewriteFactoryMethodCall((ClassInstanceCreation) ctrCall, unitRewriter, gd);
        unitChange.addTextEditGroup(gd);
        someCallPatched = true;
      } else if (ctrCall instanceof MethodRef) {
        TextEditGroup gd =
            new TextEditGroup(
                RefactoringCoreMessages.IntroduceFactoryRefactoring_replaceJavadocReference);

        rewriteJavadocReference((MethodRef) ctrCall, unitRewriter, gd);
        unitChange.addTextEditGroup(gd);
        someCallPatched = true;
      }
    }
    return someCallPatched;
  }

  private void rewriteJavadocReference(
      MethodRef javadocRef, ASTRewrite unitRewriter, TextEditGroup gd) {
    AST ast = unitRewriter.getAST();
    unitRewriter.replace(javadocRef.getName(), ast.newSimpleName(fNewMethodName), gd);
  }

  /**
   * Look "in the vicinity" of the given range to find the <code>ClassInstanceCreation</code> node
   * that this search hit identified. Necessary because the <code>SearchEngine</code> doesn't always
   * cough up text extents that <code>NodeFinder.perform()</code> agrees with.
   *
   * @param start
   * @param length
   * @param unitAST
   * @return return a {@link ClassInstanceCreation} or a {@link MethodRef} or <code>null</code> if
   *     this is really a constructor->constructor call (e.g. "this(...)")
   * @throws CoreException
   */
  private ASTNode getCtorCallAt(int start, int length, CompilationUnit unitAST)
      throws CoreException {
    ICompilationUnit unitHandle = ASTCreator.getCu(unitAST);
    ASTNode node = NodeFinder.perform(unitAST, start, length);

    if (node == null)
      throw new CoreException(
          JavaUIStatus.createError(
              IStatus.ERROR,
              Messages.format(
                  RefactoringCoreMessages.IntroduceFactory_noASTNodeForConstructorSearchHit,
                  new Object[] {
                    Integer.toString(start),
                    Integer.toString(start + length),
                    BasicElementLabels.getJavaCodeString(
                        unitHandle.getSource().substring(start, start + length)),
                    BasicElementLabels.getFileName(unitHandle)
                  }),
              null));

    if (node instanceof ClassInstanceCreation) {
      if (((ClassInstanceCreation) node).getAnonymousClassDeclaration() != null) {
        // Cannot replace anonymous inner class, see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=250660
        fConstructorVisibility = Modifier.PROTECTED;
        return null;
      }
      return node;
    } else if (node instanceof VariableDeclaration) {
      Expression init = ((VariableDeclaration) node).getInitializer();

      if (init instanceof ClassInstanceCreation) {
        return init;
      } else if (init != null)
        throw new CoreException(
            JavaUIStatus.createError(
                IStatus.ERROR,
                Messages.format(
                    RefactoringCoreMessages.IntroduceFactory_unexpectedInitializerNodeType,
                    new Object[] {
                      BasicElementLabels.getJavaCodeString(init.toString()),
                      BasicElementLabels.getFileName(unitHandle)
                    }),
                null));
      else
        throw new CoreException(
            JavaUIStatus.createError(
                IStatus.ERROR,
                Messages.format(
                    RefactoringCoreMessages
                        .IntroduceFactory_noConstructorCallNodeInsideFoundVarbleDecl,
                    BasicElementLabels.getJavaCodeString(node.toString())),
                null));
    } else if (node instanceof ConstructorInvocation) {
      // This is a call we can bypass; it's from one constructor flavor
      // to another flavor on the same class.
      return null;
    } else if (node instanceof SuperConstructorInvocation) {
      // This is a call we can bypass; it's from one constructor flavor
      // to another flavor on the same class.
      fConstructorVisibility = Modifier.PROTECTED;
      return null;
    } else if (node instanceof ExpressionStatement) {
      Expression expr = ((ExpressionStatement) node).getExpression();

      if (expr instanceof ClassInstanceCreation) return expr;
      else
        throw new CoreException(
            JavaUIStatus.createError(
                IStatus.ERROR,
                Messages.format(
                    RefactoringCoreMessages
                        .IntroduceFactory_unexpectedASTNodeTypeForConstructorSearchHit,
                    new Object[] {
                      BasicElementLabels.getJavaCodeString(expr.toString()),
                      BasicElementLabels.getFileName(unitHandle)
                    }),
                null));
    } else if (node instanceof SimpleName
        && (node.getParent() instanceof MethodDeclaration
            || node.getParent() instanceof AbstractTypeDeclaration)) {
      // We seem to have been given a hit for an implicit call to the base-class constructor.
      // Do nothing with this (implicit) call, but have to make sure we make the derived class
      // doesn't lose access to the base-class constructor (so make it 'protected', not 'private').
      fConstructorVisibility = Modifier.PROTECTED;
      return null;
    } else if (node instanceof MethodRef) {
      return node;
    } else
      throw new CoreException(
          JavaUIStatus.createError(
              IStatus.ERROR,
              Messages.format(
                  RefactoringCoreMessages
                      .IntroduceFactory_unexpectedASTNodeTypeForConstructorSearchHit,
                  new Object[] {
                    BasicElementLabels.getJavaElementName(
                        node.getClass().getName() + "('" + node.toString() + "')"),
                    BasicElementLabels.getFileName(unitHandle)
                  }),
              // $NON-NLS-1$ //$NON-NLS-2$
              null));
  }

  /**
   * Perform the AST rewriting necessary on the given <code>CompilationUnit</code> to create the
   * factory method. The method will reside on the type identified by <code>fFactoryOwningClass
   * </code>.
   *
   * @param unitRewriter the ASTRewrite to be used
   * @param unit the <code>CompilationUnit</code> where factory method will be created
   * @param gd the <code>GroupDescription</code> to associate with the changes made
   * @throws CoreException if an exception occurs while accessing its corresponding resource
   */
  private void createFactoryChange(ASTRewrite unitRewriter, CompilationUnit unit, TextEditGroup gd)
      throws CoreException {
    // ================================================================================
    // First add the factory itself (method, class, and interface as needed/directed by user)
    AST ast = unit.getAST();

    fFactoryMethod = createFactoryMethod(ast, fCtorBinding, unitRewriter);

    AbstractTypeDeclaration factoryOwner =
        (AbstractTypeDeclaration)
            unit.findDeclaringNode(fFactoryOwningClass.resolveBinding().getKey());
    fImportRewriter.addImport(fCtorOwningClass.resolveBinding());

    int idx = ASTNodes.getInsertionIndex(fFactoryMethod, factoryOwner.bodyDeclarations());

    if (idx < 0) idx = 0; // Guard against bug in getInsertionIndex()
    unitRewriter
        .getListRewrite(factoryOwner, factoryOwner.getBodyDeclarationsProperty())
        .insertAt(fFactoryMethod, idx, gd);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Refactoring#createChange(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException {
    try {
      pm.beginTask(RefactoringCoreMessages.IntroduceFactory_createChanges, fAllCallsTo.length);
      final ITypeBinding binding = fFactoryOwningClass.resolveBinding();
      final Map<String, String> arguments = new HashMap<String, String>();
      String project = null;
      IJavaProject javaProject = fCUHandle.getJavaProject();
      if (javaProject != null) project = javaProject.getElementName();
      int flags =
          JavaRefactoringDescriptor.JAR_MIGRATION
              | JavaRefactoringDescriptor.JAR_REFACTORING
              | RefactoringDescriptor.STRUCTURAL_CHANGE
              | RefactoringDescriptor.MULTI_CHANGE;
      if (binding.isNested() && !binding.isMember())
        flags |= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
      final String description =
          Messages.format(
              RefactoringCoreMessages.IntroduceFactoryRefactoring_descriptor_description_short,
              BasicElementLabels.getJavaElementName(fCtorOwningClass.getName().getIdentifier()));
      final String header =
          Messages.format(
              RefactoringCoreMessages.IntroduceFactory_descriptor_description,
              new String[] {
                BasicElementLabels.getJavaElementName(fNewMethodName),
                BindingLabelProvider.getBindingLabel(
                    binding, JavaElementLabels.ALL_FULLY_QUALIFIED),
                BindingLabelProvider.getBindingLabel(
                    fCtorBinding, JavaElementLabels.ALL_FULLY_QUALIFIED)
              });
      final JDTRefactoringDescriptorComment comment =
          new JDTRefactoringDescriptorComment(project, this, header);
      comment.addSetting(
          Messages.format(
              RefactoringCoreMessages.IntroduceFactoryRefactoring_original_pattern,
              BindingLabelProvider.getBindingLabel(
                  fCtorBinding, JavaElementLabels.ALL_FULLY_QUALIFIED)));
      comment.addSetting(
          Messages.format(
              RefactoringCoreMessages.IntroduceFactoryRefactoring_factory_pattern,
              BasicElementLabels.getJavaElementName(fNewMethodName)));
      comment.addSetting(
          Messages.format(
              RefactoringCoreMessages.IntroduceFactoryRefactoring_owner_pattern,
              BindingLabelProvider.getBindingLabel(
                  binding, JavaElementLabels.ALL_FULLY_QUALIFIED)));
      if (fProtectConstructor)
        comment.addSetting(RefactoringCoreMessages.IntroduceFactoryRefactoring_declare_private);
      final IntroduceFactoryDescriptor descriptor =
          RefactoringSignatureDescriptorFactory.createIntroduceFactoryDescriptor(
              project, description, comment.asString(), arguments, flags);
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT,
          JavaRefactoringDescriptorUtil.elementToHandle(project, fCUHandle));
      arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fNewMethodName);
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1,
          JavaRefactoringDescriptorUtil.elementToHandle(project, binding.getJavaElement()));
      arguments.put(
          JavaRefactoringDescriptorUtil.ATTRIBUTE_SELECTION,
          new Integer(fSelectionStart).toString()
              + " "
              + new Integer(fSelectionLength).toString()); // $NON-NLS-1$
      arguments.put(ATTRIBUTE_PROTECT, Boolean.valueOf(fProtectConstructor).toString());
      final DynamicValidationStateChange result =
          new DynamicValidationRefactoringChange(
              descriptor, RefactoringCoreMessages.IntroduceFactory_name);
      boolean hitInFactoryClass = false;
      boolean hitInCtorClass = false;
      for (int i = 0; i < fAllCallsTo.length; i++) {
        SearchResultGroup rg = fAllCallsTo[i];
        ICompilationUnit unitHandle = rg.getCompilationUnit();
        CompilationUnitChange cuChange = new CompilationUnitChange(getName(), unitHandle);

        if (addAllChangesFor(rg, unitHandle, cuChange)) result.add(cuChange);

        if (unitHandle.equals(fFactoryUnitHandle)) hitInFactoryClass = true;
        if (unitHandle.equals(ASTCreator.getCu(fCtorOwningClass))) hitInCtorClass = true;

        pm.worked(1);
        if (pm.isCanceled()) throw new OperationCanceledException();
      }
      if (!hitInFactoryClass) { // Handle factory class if no search hits there
        CompilationUnitChange cuChange = new CompilationUnitChange(getName(), fFactoryUnitHandle);
        addAllChangesFor(null, fFactoryUnitHandle, cuChange);
        result.add(cuChange);
      }
      if (!hitInCtorClass
          && !fFactoryUnitHandle.equals(
              ASTCreator.getCu(
                  fCtorOwningClass))) { // Handle constructor-owning class if no search hits there
        CompilationUnitChange cuChange =
            new CompilationUnitChange(getName(), ASTCreator.getCu(fCtorOwningClass));
        addAllChangesFor(null, ASTCreator.getCu(fCtorOwningClass), cuChange);
        result.add(cuChange);
      }
      return result;
    } finally {
      pm.done();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
   */
  @Override
  public String getName() {
    return RefactoringCoreMessages.IntroduceFactory_name;
  }

  /**
   * Returns the name to be used for the generated factory method.
   *
   * @return the new method name
   */
  public String getNewMethodName() {
    return fNewMethodName;
  }

  /**
   * Sets the name to be used for the generated factory method.<br>
   * Returns a <code>RefactoringStatus</code> that indicates whether the given name is valid for the
   * new factory method.
   *
   * @param newMethodName the name to be used for the generated factory method
   * @return the resulting status
   */
  public RefactoringStatus setNewMethodName(String newMethodName) {
    Assert.isNotNull(newMethodName);
    fNewMethodName = newMethodName;

    RefactoringStatus stat = Checks.checkMethodName(newMethodName, fCUHandle);

    stat.merge(isUniqueMethodName(newMethodName));

    return stat;
  }

  /**
   * @param methodName
   * @return a <code>RefactoringStatus</code> that identifies whether the the name <code>
   *     newMethodName</code> is available to use as the name of the new factory method within the
   *     factory-owner class (either a to-be- created factory class or the constructor-owning class,
   *     depending on the user options).
   */
  private RefactoringStatus isUniqueMethodName(String methodName) {
    ITypeBinding declaringClass = fCtorBinding.getDeclaringClass();
    if (Bindings.findMethodInType(declaringClass, methodName, fCtorBinding.getParameterTypes())
        != null) {
      String format =
          Messages.format(
              RefactoringCoreMessages.IntroduceFactory_duplicateMethodName,
              BasicElementLabels.getJavaElementName(methodName));
      return RefactoringStatus.createErrorStatus(format);
    }
    return new RefactoringStatus();
  }

  /**
   * Returns true iff the selected constructor can be protected.
   *
   * @return return <code>true</code> if the constructor can be made protected
   */
  public boolean canProtectConstructor() {
    return !fCtorBinding.isSynthetic()
        && fFactoryCU.findDeclaringNode(fCtorBinding.getKey()) != null;
  }

  /**
   * If the argument is true, change the visibility of the constructor to <code>protected</code>,
   * thereby encapsulating it.
   *
   * @param protectConstructor
   */
  public void setProtectConstructor(boolean protectConstructor) {
    fProtectConstructor = protectConstructor;
  }

  /**
   * Returns the project on behalf of which this refactoring was invoked.
   *
   * @return returns the Java project
   */
  public IJavaProject getProject() {
    return fCUHandle.getJavaProject();
  }

  /**
   * Sets the class on which the generated factory method is to be placed.
   *
   * @param fullyQualifiedTypeName an <code>IType</code> referring to an existing class
   * @return return the resulting status
   */
  public RefactoringStatus setFactoryClass(String fullyQualifiedTypeName) {
    IType factoryType;

    try {
      factoryType = findFactoryClass(fullyQualifiedTypeName);
      if (factoryType == null)
        return RefactoringStatus.createErrorStatus(
            Messages.format(
                RefactoringCoreMessages.IntroduceFactory_noSuchClass,
                BasicElementLabels.getJavaElementName(fullyQualifiedTypeName)));

      if (factoryType.isAnnotation())
        return RefactoringStatus.createErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_cantPutFactoryMethodOnAnnotation);
      if (factoryType.isInterface())
        return RefactoringStatus.createErrorStatus(
            RefactoringCoreMessages.IntroduceFactory_cantPutFactoryMethodOnInterface);
    } catch (JavaModelException e) {
      return RefactoringStatus.createFatalErrorStatus(
          RefactoringCoreMessages.IntroduceFactory_cantCheckForInterface);
    }

    ICompilationUnit factoryUnitHandle = factoryType.getCompilationUnit();

    if (factoryType.isBinary())
      return RefactoringStatus.createErrorStatus(
          RefactoringCoreMessages.IntroduceFactory_cantPutFactoryInBinaryClass);
    else {
      try {
        if (!fFactoryUnitHandle.equals(factoryUnitHandle)) {
          fFactoryCU = getASTFor(factoryUnitHandle);
          fFactoryUnitHandle = factoryUnitHandle;
        }
        fFactoryOwningClass =
            (AbstractTypeDeclaration)
                ASTNodes.getParent(
                    NodeFinder.perform(fFactoryCU, factoryType.getNameRange()),
                    AbstractTypeDeclaration.class);

        String factoryPkg = factoryType.getPackageFragment().getElementName();
        String ctorPkg = fCtorOwningClass.resolveBinding().getPackage().getName();

        if (!factoryPkg.equals(ctorPkg)) fConstructorVisibility = Modifier.PUBLIC;
        else if (fFactoryOwningClass != fCtorOwningClass)
          fConstructorVisibility = 0; // No such thing as Modifier.PACKAGE...

        if (fFactoryOwningClass != fCtorOwningClass)
          fConstructorVisibility = 0; // No such thing as Modifier.PACKAGE...

      } catch (JavaModelException e) {
        return RefactoringStatus.createFatalErrorStatus(e.getMessage());
      }
      return new RefactoringStatus();
    }
  }

  /**
   * Finds the factory class associated with the fully qualified name.
   *
   * @param fullyQualifiedTypeName the fully qualified type name
   * @return the factory class, or <code>null</code> if not found
   * @throws JavaModelException if an error occurs while finding the factory class
   */
  private IType findFactoryClass(String fullyQualifiedTypeName) throws JavaModelException {
    IType factoryType = getProject().findType(fullyQualifiedTypeName);
    if (factoryType == null) // presumably a non-primary type; try the search engine
    factoryType =
          findNonPrimaryType(
              fullyQualifiedTypeName, new NullProgressMonitor(), new RefactoringStatus());
    return factoryType;
  }

  /**
   * Returns the name of the class on which the generated factory method is to be placed.
   *
   * @return return the factory class name
   */
  public String getFactoryClassName() {
    return fFactoryOwningClass.resolveBinding().getQualifiedName();
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
    String handle = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
    if (handle != null) {
      final IJavaElement element =
          JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), handle, false);
      if (element == null
          || !element.exists()
          || element.getElementType() != IJavaElement.COMPILATION_UNIT)
        return JavaRefactoringDescriptorUtil.createInputFatalStatus(
            element, getName(), IJavaRefactorings.INTRODUCE_FACTORY);
      else {
        fCUHandle = (ICompilationUnit) element;
        initialize();
      }
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
            element, getName(), IJavaRefactorings.INTRODUCE_FACTORY);
      else {
        final IType type = (IType) element;
        fFactoryClassName = type.getFullyQualifiedName();
      }
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    final String name = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
    if (name != null && !"".equals(name)) // $NON-NLS-1$
    fNewMethodName = name;
    else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
    final String protect = arguments.getAttribute(ATTRIBUTE_PROTECT);
    if (protect != null) {
      fProtectConstructor = Boolean.valueOf(protect).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_PROTECT));
    return new RefactoringStatus();
  }
}

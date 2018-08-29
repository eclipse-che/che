/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Nikolay Metchev
 * <nikolaymetchev@gmail.com> - Import static (Ctrl+Shift+M) creates imports for private methods -
 * https://bugs.eclipse.org/409594
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.codemanipulation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.SimilarElementsRequestor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Add imports to a compilation unit. The input is an array of full qualified type names. No
 * elimination of unnecessary imports is done (use ImportStructure for this). Duplicates are not
 * added. If the compilation unit is open in an editor, be sure to pass over its working copy.
 */
public class AddImportsOperation implements IWorkspaceRunnable {

  public static interface IChooseImportQuery {
    /**
     * Selects an import from a list of choices.
     *
     * @param openChoices Array of found types
     * @param containerName Name type to be imported
     * @return Returns <code>null</code> to cancel the operation, or the selected imports.
     */
    TypeNameMatch chooseImport(TypeNameMatch[] openChoices, String containerName);
  }

  private ICompilationUnit fCompilationUnit;
  private final int fSelectionOffset;
  private final int fSelectionLength;
  private final IChooseImportQuery fQuery;
  private IStatus fStatus;
  private boolean fDoSave;
  private final boolean fApply;
  private MultiTextEdit fResultingEdit;

  /**
   * Generate import statements for the passed java elements Elements must be of type IType (->
   * single import) or IPackageFragment (on-demand-import). Other JavaElements are ignored
   *
   * @param cu The compilation unit
   * @param selectionOffset Start of the current text selection
   * @param selectionLength End of the current text selection
   * @param query Query element to be used for UI interaction or <code>null</code> to not select
   *     anything when multiple possibilities are available
   * @param save If set, the result will be saved
   */
  public AddImportsOperation(
      ICompilationUnit cu,
      int selectionOffset,
      int selectionLength,
      IChooseImportQuery query,
      boolean save) {
    this(cu, selectionOffset, selectionLength, query, save, true);
  }

  /**
   * Generate import statements for the passed java elements Elements must be of type IType (->
   * single import) or IPackageFragment (on-demand-import). Other JavaElements are ignored
   *
   * @param cu The compilation unit
   * @param selectionOffset Start of the current text selection
   * @param selectionLength End of the current text selection
   * @param query Query element to be used for UI interaction or <code>null</code> to not select
   *     anything when multiple possibilities are available
   * @param save If set, the result will be saved
   * @param apply If set, the resulting edit will be applied
   */
  public AddImportsOperation(
      ICompilationUnit cu,
      int selectionOffset,
      int selectionLength,
      IChooseImportQuery query,
      boolean save,
      boolean apply) {
    super();
    Assert.isNotNull(cu);

    fCompilationUnit = cu;
    fSelectionOffset = selectionOffset;
    fSelectionLength = selectionLength;
    fQuery = query;
    fStatus = Status.OK_STATUS;
    fDoSave = save;
    fApply = apply;
  }

  /** @return Returns the status. */
  public IStatus getStatus() {
    return fStatus;
  }

  /**
   * Runs the operation.
   *
   * @param monitor The progress monitor
   * @throws CoreException if accessing the CU or rewritting the imports fails
   * @throws OperationCanceledException Runtime error thrown when operation is canceled.
   */
  public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    try {
      monitor.beginTask(CodeGenerationMessages.AddImportsOperation_description, 4);

      CompilationUnit astRoot =
          SharedASTProvider.getAST(
              fCompilationUnit, SharedASTProvider.WAIT_YES, new SubProgressMonitor(monitor, 1));
      if (astRoot == null) throw new OperationCanceledException();

      ImportRewrite importRewrite = StubUtility.createImportRewrite(astRoot, true);

      MultiTextEdit res = new MultiTextEdit();

      TextEdit edit =
          evaluateEdits(
              astRoot,
              importRewrite,
              fSelectionOffset,
              fSelectionLength,
              new SubProgressMonitor(monitor, 1));
      if (edit == null) {
        return;
      }
      res.addChild(edit);

      TextEdit importsEdit = importRewrite.rewriteImports(new SubProgressMonitor(monitor, 1));
      res.addChild(importsEdit);

      fResultingEdit = res;

      if (fApply) {
        JavaModelUtil.applyEdit(fCompilationUnit, res, fDoSave, new SubProgressMonitor(monitor, 1));
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Returns the resulting edit
   *
   * @return the resulting edit
   */
  public MultiTextEdit getResultingEdit() {
    return fResultingEdit;
  }

  private TextEdit evaluateEdits(
      CompilationUnit root,
      ImportRewrite importRewrite,
      int offset,
      int length,
      IProgressMonitor monitor)
      throws JavaModelException {
    SimpleName nameNode = null;
    if (root != null) { // got an AST
      ASTNode node = NodeFinder.perform(root, offset, length);
      if (node instanceof MarkerAnnotation) {
        node = ((Annotation) node).getTypeName();
      }
      if (node instanceof QualifiedName) {
        nameNode = ((QualifiedName) node).getName();
      } else if (node instanceof SimpleName) {
        nameNode = (SimpleName) node;
      }
    }

    String name, simpleName, containerName;
    int qualifierStart;
    int simpleNameStart;

    if (nameNode != null) {
      simpleName = nameNode.getIdentifier();
      simpleNameStart = nameNode.getStartPosition();
      if (nameNode.getLocationInParent() == QualifiedName.NAME_PROPERTY) {
        Name qualifier = ((QualifiedName) nameNode.getParent()).getQualifier();
        containerName = qualifier.getFullyQualifiedName();
        name = JavaModelUtil.concatenateName(containerName, simpleName);
        qualifierStart = qualifier.getStartPosition();
      } else if (nameNode.getLocationInParent() == NameQualifiedType.NAME_PROPERTY) {
        NameQualifiedType nameQualifiedType = (NameQualifiedType) nameNode.getParent();
        Name qualifier = nameQualifiedType.getQualifier();
        containerName = qualifier.getFullyQualifiedName();
        name = JavaModelUtil.concatenateName(containerName, simpleName);
        qualifierStart = qualifier.getStartPosition();
        List<Annotation> annotations = nameQualifiedType.annotations();
        if (!annotations.isEmpty()) { // don't remove annotations
          simpleNameStart = annotations.get(0).getStartPosition();
        }
      } else if (nameNode.getLocationInParent() == MethodInvocation.NAME_PROPERTY) {
        ASTNode qualifier = ((MethodInvocation) nameNode.getParent()).getExpression();
        if (qualifier instanceof Name) {
          containerName = ASTNodes.asString(qualifier);
          name = JavaModelUtil.concatenateName(containerName, simpleName);
          qualifierStart = qualifier.getStartPosition();
        } else {
          return null;
        }
      } else {
        containerName = ""; // $NON-NLS-1$
        name = simpleName;
        qualifierStart = simpleNameStart;
      }

      IBinding binding = nameNode.resolveBinding();
      if (binding != null && !binding.isRecovered()) {
        if (binding instanceof ITypeBinding) {
          ITypeBinding typeBinding = ((ITypeBinding) binding).getTypeDeclaration();
          String qualifiedBindingName = typeBinding.getQualifiedName();
          if (containerName.length() > 0 && !qualifiedBindingName.equals(name)) {
            return null;
          }

          ImportRewriteContext context =
              new ContextSensitiveImportRewriteContext(root, qualifierStart, importRewrite);
          String res = importRewrite.addImport(typeBinding, context);
          if (containerName.length() > 0 && !res.equals(simpleName)) {
            // adding import failed
            fStatus =
                JavaUIStatus.createError(
                    IStatus.ERROR,
                    CodeGenerationMessages.AddImportsOperation_error_importclash,
                    null);
            return null;
          }
          if (containerName.length() == 0 && res.equals(simpleName)) {
            // no change necessary
            return null;
          }
          return new ReplaceEdit(qualifierStart, simpleNameStart - qualifierStart, new String());
        } else if (JavaModelUtil.is50OrHigher(fCompilationUnit.getJavaProject())
            && (binding instanceof IVariableBinding || binding instanceof IMethodBinding)) {
          boolean isField = binding instanceof IVariableBinding;
          ITypeBinding declaringClass =
              isField
                  ? ((IVariableBinding) binding).getDeclaringClass()
                  : ((IMethodBinding) binding).getDeclaringClass();
          if (declaringClass == null) {
            return null; // variableBinding.getDeclaringClass() is null for array.length
          }
          if (Modifier.isStatic(binding.getModifiers())) {
            if (containerName.length() > 0) {
              if (containerName.equals(declaringClass.getName())
                  || containerName.equals(declaringClass.getQualifiedName())) {
                ASTNode node = nameNode.getParent();
                boolean isDirectlyAccessible = false;
                while (node != null) {
                  if (isTypeDeclarationSubTypeCompatible(node, declaringClass)) {
                    isDirectlyAccessible = true;
                    break;
                  }
                  node = node.getParent();
                }
                if (!isDirectlyAccessible) {
                  if (Modifier.isPrivate(declaringClass.getModifiers())) {
                    fStatus =
                        JavaUIStatus.createError(
                            IStatus.ERROR,
                            Messages.format(
                                CodeGenerationMessages.AddImportsOperation_error_not_visible_class,
                                BasicElementLabels.getJavaElementName(declaringClass.getName())),
                            null);
                    return null;
                  }
                  String res =
                      importRewrite.addStaticImport(
                          declaringClass.getQualifiedName(), binding.getName(), isField);
                  if (!res.equals(simpleName)) {
                    // adding import failed
                    return null;
                  }
                }
                return new ReplaceEdit(
                    qualifierStart, simpleNameStart - qualifierStart, ""); // $NON-NLS-1$
              }
            }
          }
          return null; // no static imports for packages
        } else {
          return null;
        }
      }
      if (binding != null && binding.getKind() != IBinding.TYPE) {
        // recovered binding
        return null;
      }

    } else {
      IBuffer buffer = fCompilationUnit.getBuffer();

      qualifierStart = getNameStart(buffer, offset);
      int nameEnd = getNameEnd(buffer, offset + length);
      int len = nameEnd - qualifierStart;
      name = buffer.getText(qualifierStart, len).trim();
      if (name.length() == 0) {
        return null;
      }

      simpleName = Signature.getSimpleName(name);
      containerName = Signature.getQualifier(name);

      IJavaProject javaProject = fCompilationUnit.getJavaProject();
      if (simpleName.length() == 0
          || JavaConventionsUtil.validateJavaTypeName(simpleName, javaProject)
              .matches(IStatus.ERROR)
          || (containerName.length() > 0
              && JavaConventionsUtil.validateJavaTypeName(containerName, javaProject)
                  .matches(IStatus.ERROR))) {
        fStatus =
            JavaUIStatus.createError(
                IStatus.ERROR,
                CodeGenerationMessages.AddImportsOperation_error_invalid_selection,
                null);
        return null;
      }

      simpleNameStart = getSimpleNameStart(buffer, qualifierStart, containerName);

      int res =
          importRewrite
              .getDefaultImportRewriteContext()
              .findInContext(containerName, simpleName, ImportRewriteContext.KIND_TYPE);
      if (res == ImportRewriteContext.RES_NAME_CONFLICT) {
        fStatus =
            JavaUIStatus.createError(
                IStatus.ERROR, CodeGenerationMessages.AddImportsOperation_error_importclash, null);
        return null;
      } else if (res == ImportRewriteContext.RES_NAME_FOUND) {
        return new ReplaceEdit(qualifierStart, simpleNameStart - qualifierStart, ""); // $NON-NLS-1$
      }
    }
    IJavaSearchScope searchScope =
        SearchEngine.createJavaSearchScope(new IJavaElement[] {fCompilationUnit.getJavaProject()});

    TypeNameMatch[] types =
        findAllTypes(simpleName, searchScope, nameNode, new SubProgressMonitor(monitor, 1));
    if (types.length == 0) {
      fStatus =
          JavaUIStatus.createError(
              IStatus.ERROR,
              Messages.format(
                  CodeGenerationMessages.AddImportsOperation_error_notresolved_message,
                  BasicElementLabels.getJavaElementName(simpleName)),
              null);
      return null;
    }

    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }
    TypeNameMatch chosen;
    if (types.length > 1 && fQuery != null) {
      chosen = fQuery.chooseImport(types, containerName);
      if (chosen == null) {
        throw new OperationCanceledException();
      }
    } else {
      chosen = types[0];
    }
    ImportRewriteContext context =
        root == null
            ? null
            : new ContextSensitiveImportRewriteContext(root, simpleNameStart, importRewrite);
    importRewrite.addImport(chosen.getFullyQualifiedName(), context);
    return new ReplaceEdit(qualifierStart, simpleNameStart - qualifierStart, ""); // $NON-NLS-1$
  }

  private boolean isTypeDeclarationSubTypeCompatible(
      ASTNode typeDeclaration, ITypeBinding supertype) {
    if (typeDeclaration instanceof AbstractTypeDeclaration) {
      ITypeBinding binding = ((AbstractTypeDeclaration) typeDeclaration).resolveBinding();
      return binding != null && binding.isSubTypeCompatible(supertype);
    } else if (typeDeclaration instanceof AnonymousClassDeclaration) {
      ITypeBinding binding = ((AnonymousClassDeclaration) typeDeclaration).resolveBinding();
      return binding != null && binding.isSubTypeCompatible(supertype);
    } else {
      return false;
    }
  }

  private int getNameStart(IBuffer buffer, int pos) {
    while (pos > 0) {
      char ch = buffer.getChar(pos - 1);
      if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
        return pos;
      }
      pos--;
    }
    return pos;
  }

  private int getNameEnd(IBuffer doc, int pos) {
    if (pos > 0) {
      if (Character.isWhitespace(doc.getChar(pos - 1))) {
        return pos;
      }
    }
    int len = doc.getLength();
    while (pos < len) {
      char ch = doc.getChar(pos);
      if (!Character.isJavaIdentifierPart(ch)) {
        return pos;
      }
      pos++;
    }
    return pos;
  }

  private int getSimpleNameStart(IBuffer buffer, int nameStart, String containerName) {
    int containerLen = containerName.length();
    int docLen = buffer.getLength();
    if (containerLen > 0 && nameStart + containerLen + 1 < docLen) {
      for (int k = 0; k < containerLen; k++) {
        if (buffer.getChar(nameStart + k) != containerName.charAt(k)) {
          return nameStart;
        }
      }
      if (buffer.getChar(nameStart + containerLen) == '.') {
        return nameStart + containerLen + 1;
      }
    }
    return nameStart;
  }

  private int getSearchForConstant(int typeKinds) {
    final int CLASSES = SimilarElementsRequestor.CLASSES;
    final int INTERFACES = SimilarElementsRequestor.INTERFACES;
    final int ENUMS = SimilarElementsRequestor.ENUMS;
    final int ANNOTATIONS = SimilarElementsRequestor.ANNOTATIONS;

    switch (typeKinds & (CLASSES | INTERFACES | ENUMS | ANNOTATIONS)) {
      case CLASSES:
        return IJavaSearchConstants.CLASS;
      case INTERFACES:
        return IJavaSearchConstants.INTERFACE;
      case ENUMS:
        return IJavaSearchConstants.ENUM;
      case ANNOTATIONS:
        return IJavaSearchConstants.ANNOTATION_TYPE;
      case CLASSES | INTERFACES:
        return IJavaSearchConstants.CLASS_AND_INTERFACE;
      case CLASSES | ENUMS:
        return IJavaSearchConstants.CLASS_AND_ENUM;
      default:
        return IJavaSearchConstants.TYPE;
    }
  }

  /*
   * Finds a type by the simple name.
   */
  private TypeNameMatch[] findAllTypes(
      String simpleTypeName,
      IJavaSearchScope searchScope,
      SimpleName nameNode,
      IProgressMonitor monitor)
      throws JavaModelException {
    boolean is50OrHigher = JavaModelUtil.is50OrHigher(fCompilationUnit.getJavaProject());

    int typeKinds = SimilarElementsRequestor.ALL_TYPES;
    if (nameNode != null) {
      typeKinds = ASTResolving.getPossibleTypeKinds(nameNode, is50OrHigher);
    }

    ArrayList<TypeNameMatch> typeInfos = new ArrayList<TypeNameMatch>();
    TypeNameMatchCollector requestor = new TypeNameMatchCollector(typeInfos);
    int matchMode = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
    new SearchEngine()
        .searchAllTypeNames(
            null,
            matchMode,
            simpleTypeName.toCharArray(),
            matchMode,
            getSearchForConstant(typeKinds),
            searchScope,
            requestor,
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            monitor);

    ArrayList<TypeNameMatch> typeRefsFound = new ArrayList<TypeNameMatch>(typeInfos.size());
    for (int i = 0, len = typeInfos.size(); i < len; i++) {
      TypeNameMatch curr = typeInfos.get(i);
      if (curr.getPackageName().length() > 0) { // do not suggest imports from the default package
        if (isOfKind(curr, typeKinds, is50OrHigher) && isVisible(curr)) {
          typeRefsFound.add(curr);
        }
      }
    }
    return typeRefsFound.toArray(new TypeNameMatch[typeRefsFound.size()]);
  }

  private boolean isOfKind(TypeNameMatch curr, int typeKinds, boolean is50OrHigher) {
    int flags = curr.getModifiers();
    if (Flags.isAnnotation(flags)) {
      return is50OrHigher && (typeKinds & SimilarElementsRequestor.ANNOTATIONS) != 0;
    }
    if (Flags.isEnum(flags)) {
      return is50OrHigher && (typeKinds & SimilarElementsRequestor.ENUMS) != 0;
    }
    if (Flags.isInterface(flags)) {
      return (typeKinds & SimilarElementsRequestor.INTERFACES) != 0;
    }
    return (typeKinds & SimilarElementsRequestor.CLASSES) != 0;
  }

  private boolean isVisible(TypeNameMatch curr) {
    int flags = curr.getModifiers();
    if (Flags.isPrivate(flags)) {
      return false;
    }
    if (Flags.isPublic(flags) || Flags.isProtected(flags)) {
      return true;
    }
    return curr.getPackageName().equals(fCompilationUnit.getParent().getElementName());
  }

  /** @return Returns the scheduling rule for this operation */
  public ISchedulingRule getScheduleRule() {
    return fCompilationUnit.getJavaProject().getResource();
  }
}

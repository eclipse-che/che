/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
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
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.delegates.DelegateCreator;
import org.eclipse.jdt.internal.corext.refactoring.delegates.DelegateMethodCreator;
import org.eclipse.jdt.internal.corext.refactoring.participants.JavaProcessors;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IDelegateUpdating;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.CollectionsUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.refactoring.IRefactoringProcessorIds;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.text.edits.ReplaceEdit;

public abstract class RenameMethodProcessor extends JavaRenameProcessor
    implements IReferenceUpdating, IDelegateUpdating {

  private static final String ATTRIBUTE_DELEGATE = "delegate"; // $NON-NLS-1$
  private static final String ATTRIBUTE_DEPRECATE = "deprecate"; // $NON-NLS-1$

  private SearchResultGroup[] fOccurrences;
  private boolean fUpdateReferences;
  private IMethod fMethod;
  private Set<IMethod> fMethodsToRename;
  private TextChangeManager fChangeManager;
  private WorkingCopyOwner fWorkingCopyOwner;
  private boolean fIsComposite;
  private GroupCategorySet fCategorySet;
  private boolean fDelegateUpdating;
  private boolean fDelegateDeprecation;
  protected boolean fInitialized = false;

  /**
   * Creates a new rename method processor.
   *
   * @param method the method, or <code>null</code> if invoked by scripting
   */
  protected RenameMethodProcessor(IMethod method) {
    this(method, new TextChangeManager(true), null);
    fIsComposite = false;
  }
  /**
   * Creates a new rename method processor.
   *
   * <p>This constructor is only invoked by <code>RenameTypeProcessor</code>.
   *
   * @param method the method
   * @param manager the change manager
   * @param categorySet the group category set
   */
  protected RenameMethodProcessor(
      IMethod method, TextChangeManager manager, GroupCategorySet categorySet) {
    initialize(method);
    fChangeManager = manager;
    fCategorySet = categorySet;
    fDelegateUpdating = false;
    fDelegateDeprecation = true;
    fIsComposite = true;
  }

  protected void initialize(IMethod method) {
    fMethod = method;
    if (!fInitialized) {
      if (method != null) setNewElementName(method.getElementName());
      fUpdateReferences = true;
      initializeWorkingCopyOwner();
    }
  }

  protected void initializeWorkingCopyOwner() {
    fWorkingCopyOwner =
        new WorkingCopyOwner() {
          /*must subclass*/
        };
  }

  protected void setData(RenameMethodProcessor other) {
    fUpdateReferences = other.fUpdateReferences;
    setNewElementName(other.getNewElementName());
  }

  @Override
  public String getIdentifier() {
    return IRefactoringProcessorIds.RENAME_METHOD_PROCESSOR;
  }

  @Override
  public boolean isApplicable() throws CoreException {
    return RefactoringAvailabilityTester.isRenameAvailable(fMethod);
  }

  @Override
  public String getProcessorName() {
    return RefactoringCoreMessages.RenameMethodRefactoring_name;
  }

  @Override
  protected String[] getAffectedProjectNatures() throws CoreException {
    return JavaProcessors.computeAffectedNatures(fMethod);
  }

  @Override
  public Object[] getElements() {
    return new Object[] {fMethod};
  }

  @Override
  protected RenameModifications computeRenameModifications() throws CoreException {
    RenameModifications result = new RenameModifications();
    RenameArguments args = new RenameArguments(getNewElementName(), getUpdateReferences());
    for (Iterator<IMethod> iter = fMethodsToRename.iterator(); iter.hasNext(); ) {
      IMethod method = iter.next();
      result.rename(method, args);
    }
    return result;
  }

  @Override
  protected IFile[] getChangedFiles() throws CoreException {
    return ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits());
  }

  @Override
  public int getSaveMode() {
    return RefactoringSaveHelper.SAVE_REFACTORING;
  }

  // ---- INameUpdating -------------------------------------

  public final String getCurrentElementName() {
    return fMethod.getElementName();
  }

  public final RefactoringStatus checkNewElementName(String newName) {
    Assert.isNotNull(newName, "new name"); // $NON-NLS-1$

    RefactoringStatus status =
        Checks.checkName(newName, JavaConventionsUtil.validateMethodName(newName, fMethod));
    if (status.isOK() && !Checks.startsWithLowerCase(newName))
      status =
          RefactoringStatus.createWarningStatus(
              fIsComposite
                  ? Messages.format(
                      RefactoringCoreMessages.Checks_method_names_lowercase2,
                      new String[] {
                        BasicElementLabels.getJavaElementName(newName), getDeclaringTypeLabel()
                      })
                  : RefactoringCoreMessages.Checks_method_names_lowercase);

    if (Checks.isAlreadyNamed(fMethod, newName))
      status.addFatalError(
          fIsComposite
              ? Messages.format(
                  RefactoringCoreMessages.RenameMethodRefactoring_same_name2,
                  new String[] {
                    BasicElementLabels.getJavaElementName(newName), getDeclaringTypeLabel()
                  })
              : RefactoringCoreMessages.RenameMethodRefactoring_same_name,
          JavaStatusContext.create(fMethod));
    return status;
  }

  private String getDeclaringTypeLabel() {
    return JavaElementLabels.getElementLabel(
        fMethod.getDeclaringType(), JavaElementLabels.ALL_DEFAULT);
  }

  public Object getNewElement() {
    return fMethod.getDeclaringType().getMethod(getNewElementName(), fMethod.getParameterTypes());
  }

  public final IMethod getMethod() {
    return fMethod;
  }

  private void initializeMethodsToRename(IProgressMonitor pm, ReferencesInBinaryContext binaryRefs)
      throws CoreException {
    if (fMethodsToRename == null) {
      IMethod[] rippleMethods =
          RippleMethodFinder2.getRelatedMethods(getMethod(), binaryRefs, pm, null);
      fMethodsToRename = new HashSet<IMethod>();
      for (IMethod method : rippleMethods) {
        if (!method.isLambdaMethod()) {
          fMethodsToRename.add(method);
        }
      }
    }
  }

  protected void setMethodsToRename(IMethod[] methods) {
    fMethodsToRename = new HashSet<IMethod>(Arrays.asList(methods));
  }

  protected Set<IMethod> getMethodsToRename() {
    return fMethodsToRename;
  }

  // ---- IReferenceUpdating -----------------------------------

  public final void setUpdateReferences(boolean update) {
    fUpdateReferences = update;
  }

  public boolean getUpdateReferences() {
    return fUpdateReferences;
  }

  // ------------------- IDelegateUpdating ----------------------

  public boolean canEnableDelegateUpdating() {
    return true;
  }

  public boolean getDelegateUpdating() {
    return fDelegateUpdating;
  }

  public void setDelegateUpdating(boolean updating) {
    fDelegateUpdating = updating;
  }

  public boolean getDeprecateDelegates() {
    return fDelegateDeprecation;
  }

  public void setDeprecateDelegates(boolean deprecate) {
    fDelegateDeprecation = deprecate;
  }

  // ----------- preconditions ------------------

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    if (!fMethod.exists()) {
      String message =
          Messages.format(
              RefactoringCoreMessages.RenameMethodRefactoring_deleted,
              BasicElementLabels.getFileName(fMethod.getCompilationUnit()));
      return RefactoringStatus.createFatalErrorStatus(message);
    }

    RefactoringStatus result = Checks.checkAvailability(fMethod);
    if (result.hasFatalError()) return result;
    result.merge(Checks.checkIfCuBroken(fMethod));
    if (JdtFlags.isNative(fMethod))
      result.addError(RefactoringCoreMessages.RenameMethodRefactoring_no_native);
    return result;
  }

  @Override
  protected RefactoringStatus doCheckFinalConditions(
      IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
    try {
      RefactoringStatus result = new RefactoringStatus();
      pm.beginTask("", 9); // $NON-NLS-1$
      // TODO workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=40367
      if (!Checks.isAvailable(fMethod)) {
        result.addFatalError(
            RefactoringCoreMessages.RenameMethodProcessor_is_binary,
            JavaStatusContext.create(fMethod));
        return result;
      }
      result.merge(Checks.checkIfCuBroken(fMethod));
      if (result.hasFatalError()) return result;
      pm.setTaskName(
          RefactoringCoreMessages.RenameMethodRefactoring_taskName_checkingPreconditions);
      result.merge(checkNewElementName(getNewElementName()));
      if (result.hasFatalError()) return result;

      boolean mustAnalyzeShadowing;
      IMethod[] newNameMethods =
          searchForDeclarationsOfClashingMethods(new SubProgressMonitor(pm, 1));
      if (newNameMethods.length == 0) {
        mustAnalyzeShadowing = false;
        pm.worked(1);
      } else {
        IType[] outerTypes =
            searchForOuterTypesOfReferences(newNameMethods, new SubProgressMonitor(pm, 1));
        if (outerTypes.length > 0) {
          // There exists a reference to a clashing method, where the reference is in a nested type.
          // That nested type could be a type in a ripple method's hierarchy, which could
          // cause the reference to bind to the new ripple method instead of to
          // its old binding (a method of an enclosing scope).
          // -> Getting *more* references than before -> Semantics not preserved.
          // Examples: RenameVirtualMethodInClassTests#testFail39() and #testFail41()
          // TODO: could pass declaringTypes to the RippleMethodFinder and check whether
          // a hierarchy contains one of outerTypes (or an outer type of an outerType, recursively).
          mustAnalyzeShadowing = true;

        } else {
          boolean hasOldRefsInInnerTypes = true;
          // TODO: to implement this optimization:
          // - move search for references to before this check.
          // - collect references in inner types.
          // - for each reference, check for all supertypes and their enclosing types
          // (recursively), whether they declare a rippleMethod
          if (hasOldRefsInInnerTypes) {
            // There exists a reference to a ripple method in a nested type
            // of a type in the hierarchy of any ripple method.
            // When that reference is renamed, and one of the supertypes of the
            // nested type declared a method matching the new name, then
            // the renamed reference will bind to the method in its supertype,
            // since inherited methods bind stronger than methods from enclosing scopes.
            // Getting *less* references than before -> Semantics not preserved.
            // Examples: RenamePrivateMethodTests#testFail2(), RenamePrivateMethodTests#testFail5()
            mustAnalyzeShadowing = true;
          } else {
            mustAnalyzeShadowing = false;
          }
        }
      }

      String binaryRefsDescription =
          Messages.format(
              RefactoringCoreMessages.ReferencesInBinaryContext_ref_in_binaries_description,
              BasicElementLabels.getJavaElementName(getCurrentElementName()));
      ReferencesInBinaryContext binaryRefs = new ReferencesInBinaryContext(binaryRefsDescription);

      initializeMethodsToRename(new SubProgressMonitor(pm, 1), binaryRefs);
      pm.setTaskName(
          RefactoringCoreMessages.RenameMethodRefactoring_taskName_searchingForReferences);
      fOccurrences = getOccurrences(new SubProgressMonitor(pm, 3), result, binaryRefs);
      binaryRefs.addErrorIfNecessary(result);

      pm.setTaskName(
          RefactoringCoreMessages.RenameMethodRefactoring_taskName_checkingPreconditions);

      if (fUpdateReferences) result.merge(checkRelatedMethods());

      result.merge(analyzeCompilationUnits()); // removes CUs with syntax errors
      pm.worked(1);

      if (result.hasFatalError()) return result;

      createChanges(new SubProgressMonitor(pm, 1), result);
      if (fUpdateReferences & mustAnalyzeShadowing)
        result.merge(analyzeRenameChanges(new SubProgressMonitor(pm, 1)));
      else pm.worked(1);

      return result;
    } finally {
      pm.done();
    }
  }

  private IType[] searchForOuterTypesOfReferences(IMethod[] newNameMethods, IProgressMonitor pm)
      throws CoreException {
    final Set<IType> outerTypesOfReferences = new HashSet<IType>();
    SearchPattern pattern =
        RefactoringSearchEngine.createOrPattern(newNameMethods, IJavaSearchConstants.REFERENCES);
    IJavaSearchScope scope = createRefactoringScope(getMethod());
    SearchRequestor requestor =
        new SearchRequestor() {
          @Override
          public void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object element = match.getElement();
            if (!(element instanceof IMember))
              return; // e.g. an IImportDeclaration for a static method import
            IMember member = (IMember) element;
            IType declaring = member.getDeclaringType();
            if (declaring == null) return;
            IType outer = declaring.getDeclaringType();
            if (outer != null) outerTypesOfReferences.add(declaring);
          }
        };
    new SearchEngine()
        .search(pattern, SearchUtils.getDefaultSearchParticipants(), scope, requestor, pm);
    return outerTypesOfReferences.toArray(new IType[outerTypesOfReferences.size()]);
  }

  private IMethod[] searchForDeclarationsOfClashingMethods(IProgressMonitor pm)
      throws CoreException {
    final List<IMethod> results = new ArrayList<IMethod>();
    SearchPattern pattern = createNewMethodPattern();
    IJavaSearchScope scope = RefactoringScopeFactory.create(getMethod().getJavaProject());
    SearchRequestor requestor =
        new SearchRequestor() {
          @Override
          public void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object method = match.getElement();
            if (method
                instanceof
                IMethod) // check for bug 90138: [refactoring] [rename] Renaming method throws
              // internal exception
              results.add((IMethod) method);
            else
              JavaPlugin.logErrorMessage(
                  "Unexpected element in search match: " + match.toString()); // $NON-NLS-1$
          }
        };
    new SearchEngine()
        .search(pattern, SearchUtils.getDefaultSearchParticipants(), scope, requestor, pm);
    return results.toArray(new IMethod[results.size()]);
  }

  private SearchPattern createNewMethodPattern() {
    StringBuffer stringPattern = new StringBuffer(getNewElementName()).append('(');
    int paramCount = getMethod().getNumberOfParameters();
    for (int i = 0; i < paramCount; i++) {
      if (i > 0) stringPattern.append(',');
      stringPattern.append('*');
    }
    stringPattern.append(')');

    return SearchPattern.createPattern(
        stringPattern.toString(),
        IJavaSearchConstants.METHOD,
        IJavaSearchConstants.DECLARATIONS,
        SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
  }

  protected final IJavaSearchScope createRefactoringScope() throws CoreException {
    return createRefactoringScope(fMethod);
  }
  // TODO: shouldn't scope take all ripple methods into account?
  protected static final IJavaSearchScope createRefactoringScope(IMethod method)
      throws CoreException {
    return RefactoringScopeFactory.create(method, true, false);
  }

  private SearchPattern createOccurrenceSearchPattern() {
    HashSet<IMethod> methods = new HashSet<IMethod>(fMethodsToRename);
    methods.add(fMethod);
    IMethod[] ms = methods.toArray(new IMethod[methods.size()]);
    return RefactoringSearchEngine.createOrPattern(ms, IJavaSearchConstants.ALL_OCCURRENCES);
  }

  protected SearchResultGroup[] getOccurrences() {
    return fOccurrences;
  }

  private SearchResultGroup[] getOccurrences(
      IProgressMonitor pm, RefactoringStatus status, ReferencesInBinaryContext binaryRefs)
      throws CoreException {
    SearchPattern pattern = createOccurrenceSearchPattern();
    return RefactoringSearchEngine.search(
        pattern,
        createRefactoringScope(),
        new MethodOccurenceCollector(getMethod().getElementName(), binaryRefs),
        pm,
        status);
  }

  private RefactoringStatus checkRelatedMethods() throws CoreException {
    RefactoringStatus result = new RefactoringStatus();
    for (Iterator<IMethod> iter = fMethodsToRename.iterator(); iter.hasNext(); ) {
      IMethod method = iter.next();

      result.merge(
          Checks.checkIfConstructorName(
              method, getNewElementName(), method.getDeclaringType().getElementName()));

      String[] msgData =
          new String[] {
            BasicElementLabels.getJavaElementName(method.getElementName()),
            BasicElementLabels.getJavaElementName(
                method.getDeclaringType().getFullyQualifiedName('.'))
          };
      if (!method.exists()) {
        result.addFatalError(
            Messages.format(RefactoringCoreMessages.RenameMethodRefactoring_not_in_model, msgData));
        continue;
      }
      if (method.isBinary())
        result.addFatalError(
            Messages.format(RefactoringCoreMessages.RenameMethodRefactoring_no_binary, msgData));
      if (method.isReadOnly())
        result.addFatalError(
            Messages.format(RefactoringCoreMessages.RenameMethodRefactoring_no_read_only, msgData));
      if (JdtFlags.isNative(method))
        result.addError(
            Messages.format(RefactoringCoreMessages.RenameMethodRefactoring_no_native_1, msgData));
    }
    return result;
  }

  private RefactoringStatus analyzeCompilationUnits() throws CoreException {
    if (fOccurrences.length == 0) return null;

    RefactoringStatus result = new RefactoringStatus();
    fOccurrences = Checks.excludeCompilationUnits(fOccurrences, result);
    if (result.hasFatalError()) return result;

    result.merge(Checks.checkCompileErrorsInAffectedFiles(fOccurrences));

    return result;
  }

  // -------

  private RefactoringStatus analyzeRenameChanges(IProgressMonitor pm) throws CoreException {
    ICompilationUnit[] newDeclarationWCs = null;
    try {
      pm.beginTask("", 4); // $NON-NLS-1$
      RefactoringStatus result = new RefactoringStatus();
      ICompilationUnit[] declarationCUs = getDeclarationCUs();
      newDeclarationWCs =
          RenameAnalyzeUtil.createNewWorkingCopies(
              declarationCUs, fChangeManager, fWorkingCopyOwner, new SubProgressMonitor(pm, 1));

      IMethod[] wcOldMethods = new IMethod[fMethodsToRename.size()];
      IMethod[] wcNewMethods = new IMethod[fMethodsToRename.size()];
      int i = 0;
      for (Iterator<IMethod> iter = fMethodsToRename.iterator(); iter.hasNext(); i++) {
        IMethod method = iter.next();
        ICompilationUnit newCu =
            RenameAnalyzeUtil.findWorkingCopyForCu(newDeclarationWCs, method.getCompilationUnit());
        IType typeWc =
            (IType) JavaModelUtil.findInCompilationUnit(newCu, method.getDeclaringType());
        if (typeWc == null) {
          // should not happen
          i--;
          wcOldMethods =
              CollectionsUtil.toArray(
                  Arrays.asList(wcOldMethods).subList(0, wcOldMethods.length - 1), IMethod.class);
          wcNewMethods =
              CollectionsUtil.toArray(
                  Arrays.asList(wcNewMethods).subList(0, wcNewMethods.length - 1), IMethod.class);
          continue;
        }
        wcOldMethods[i] = getMethodInWorkingCopy(method, getCurrentElementName(), typeWc);
        wcNewMethods[i] = getMethodInWorkingCopy(method, getNewElementName(), typeWc);
      }

      //			SearchResultGroup[] newOccurrences= findNewOccurrences(newMethods, newDeclarationWCs, new
      // SubProgressMonitor(pm, 3));
      SearchResultGroup[] newOccurrences =
          batchFindNewOccurrences(
              wcNewMethods, wcOldMethods, newDeclarationWCs, new SubProgressMonitor(pm, 3), result);

      result.merge(
          RenameAnalyzeUtil.analyzeRenameChanges2(
              fChangeManager, fOccurrences, newOccurrences, getNewElementName()));
      return result;
    } finally {
      pm.done();
      if (newDeclarationWCs != null) {
        for (int i = 0; i < newDeclarationWCs.length; i++) {
          newDeclarationWCs[i].discardWorkingCopy();
        }
      }
    }
  }

  // Lower memory footprint than batchFindNewOccurrences. Not used because it is too slow.
  // Final solution is maybe to do searches in chunks of ~ 50 CUs.
  //	private SearchResultGroup[] findNewOccurrences(IMethod[] newMethods, ICompilationUnit[]
  // newDeclarationWCs, IProgressMonitor pm) throws CoreException {
  //		pm.beginTask("", fOccurrences.length * 2); //$NON-NLS-1$
  //
  //		SearchPattern refsPattern= RefactoringSearchEngine.createOrPattern(newMethods,
  // IJavaSearchConstants.REFERENCES);
  //		SearchParticipant[] searchParticipants= SearchUtils.getDefaultSearchParticipants();
  //		IJavaSearchScope scope= RefactoringScopeFactory.create(newMethods);
  //		MethodOccurenceCollector requestor= new MethodOccurenceCollector(getNewElementName());
  //		SearchEngine searchEngine= new SearchEngine(fWorkingCopyOwner);
  //
  //		//TODO: should process only references
  //		for (int j= 0; j < fOccurrences.length; j++) { //should be getReferences()
  //			//cut memory peak by holding only one reference CU at a time in memory
  //			ICompilationUnit originalCu= fOccurrences[j].getCompilationUnit();
  //			ICompilationUnit newWc= null;
  //			try {
  //				ICompilationUnit wc= RenameAnalyzeUtil.findWorkingCopyForCu(newDeclarationWCs, originalCu);
  //				if (wc == null) {
  //					newWc= RenameAnalyzeUtil.createNewWorkingCopy(originalCu, fChangeManager,
  // fWorkingCopyOwner,
  //							new SubProgressMonitor(pm, 1));
  //				}
  //				searchEngine.search(refsPattern, searchParticipants, scope,	requestor, new
  // SubProgressMonitor(pm, 1));
  //			} finally {
  //				if (newWc != null)
  //					newWc.discardWorkingCopy();
  //			}
  //		}
  //		SearchResultGroup[] newResults=
  // RefactoringSearchEngine.groupByResource(requestor.getResults());
  //		pm.done();
  //		return newResults;
  //	}

  private SearchResultGroup[] batchFindNewOccurrences(
      IMethod[] wcNewMethods,
      final IMethod[] wcOldMethods,
      ICompilationUnit[] newDeclarationWCs,
      IProgressMonitor pm,
      RefactoringStatus status)
      throws CoreException {
    pm.beginTask("", 2); // $NON-NLS-1$

    SearchPattern refsPattern =
        RefactoringSearchEngine.createOrPattern(wcNewMethods, IJavaSearchConstants.REFERENCES);
    SearchParticipant[] searchParticipants = SearchUtils.getDefaultSearchParticipants();
    IJavaSearchScope scope = RefactoringScopeFactory.create(wcNewMethods);

    MethodOccurenceCollector requestor;
    if (getDelegateUpdating()) {
      // There will be two new matches inside the delegate(s) (the invocation
      // and the javadoc) which are OK and must not be reported.
      // Note that except these ocurrences, the delegate bodies are empty
      // (as they were created this way).
      requestor =
          new MethodOccurenceCollector(getNewElementName()) {
            @Override
            public void acceptSearchMatch(ICompilationUnit unit, SearchMatch match)
                throws CoreException {
              for (int i = 0; i < wcOldMethods.length; i++)
                if (wcOldMethods[i].equals(match.getElement())) return;
              super.acceptSearchMatch(unit, match);
            }
          };
    } else requestor = new MethodOccurenceCollector(getNewElementName());

    SearchEngine searchEngine = new SearchEngine(fWorkingCopyOwner);

    ArrayList<ICompilationUnit> needWCs = new ArrayList<ICompilationUnit>();
    HashSet<ICompilationUnit> declaringCUs =
        new HashSet<ICompilationUnit>(newDeclarationWCs.length);
    for (int i = 0; i < newDeclarationWCs.length; i++)
      declaringCUs.add(newDeclarationWCs[i].getPrimary());
    for (int i = 0; i < fOccurrences.length; i++) {
      ICompilationUnit cu = fOccurrences[i].getCompilationUnit();
      if (!declaringCUs.contains(cu)) needWCs.add(cu);
    }
    ICompilationUnit[] otherWCs = null;
    try {
      otherWCs =
          RenameAnalyzeUtil.createNewWorkingCopies(
              needWCs.toArray(new ICompilationUnit[needWCs.size()]),
              fChangeManager,
              fWorkingCopyOwner,
              new SubProgressMonitor(pm, 1));
      searchEngine.search(
          refsPattern, searchParticipants, scope, requestor, new SubProgressMonitor(pm, 1));
    } finally {
      pm.done();
      if (otherWCs != null) {
        for (int i = 0; i < otherWCs.length; i++) {
          otherWCs[i].discardWorkingCopy();
        }
      }
    }
    SearchResultGroup[] newResults =
        RefactoringSearchEngine.groupByCu(requestor.getResults(), status);
    return newResults;
  }

  private ICompilationUnit[] getDeclarationCUs() {
    Set<ICompilationUnit> cus = new HashSet<ICompilationUnit>();
    for (Iterator<IMethod> iter = fMethodsToRename.iterator(); iter.hasNext(); ) {
      IMethod method = iter.next();
      cus.add(method.getCompilationUnit());
    }
    return cus.toArray(new ICompilationUnit[cus.size()]);
  }

  private IMethod getMethodInWorkingCopy(IMethod method, String elementName, IType typeWc) {
    String[] paramTypeSignatures = method.getParameterTypes();
    return typeWc.getMethod(elementName, paramTypeSignatures);
  }

  // -------
  private static IMethod[] classesDeclareMethodName(
      ITypeHierarchy hier, List<IType> classes, IMethod method, String newName)
      throws CoreException {
    Set<IMethod> result = new HashSet<IMethod>();
    IType type = method.getDeclaringType();
    List<IType> subtypes = Arrays.asList(hier.getAllSubtypes(type));

    int parameterCount = method.getParameterTypes().length;
    boolean isMethodPrivate = JdtFlags.isPrivate(method);

    for (Iterator<IType> iter = classes.iterator(); iter.hasNext(); ) {
      IType clazz = iter.next();
      IMethod[] methods = clazz.getMethods();
      boolean isSubclass = subtypes.contains(clazz);
      for (int j = 0; j < methods.length; j++) {
        IMethod foundMethod =
            Checks.findMethod(newName, parameterCount, false, new IMethod[] {methods[j]});
        if (foundMethod == null) continue;
        if (isSubclass || type.equals(clazz)) result.add(foundMethod);
        else if ((!isMethodPrivate) && (!JdtFlags.isPrivate(methods[j]))) result.add(foundMethod);
      }
    }
    return result.toArray(new IMethod[result.size()]);
  }

  static final IMethod[] hierarchyDeclaresMethodName(
      IProgressMonitor pm, ITypeHierarchy hierarchy, IMethod method, String newName)
      throws CoreException {
    try {
      Set<IMethod> result = new HashSet<IMethod>();
      IType type = method.getDeclaringType();
      IMethod foundMethod =
          Checks.findMethod(newName, method.getParameterTypes().length, false, type);
      if (foundMethod != null) result.add(foundMethod);
      IMethod[] foundInHierarchyClasses =
          classesDeclareMethodName(
              hierarchy, Arrays.asList(hierarchy.getAllClasses()), method, newName);
      if (foundInHierarchyClasses != null) result.addAll(Arrays.asList(foundInHierarchyClasses));
      IType[] implementingClasses = hierarchy.getImplementingClasses(type);
      IMethod[] foundInImplementingClasses =
          classesDeclareMethodName(hierarchy, Arrays.asList(implementingClasses), method, newName);
      if (foundInImplementingClasses != null)
        result.addAll(Arrays.asList(foundInImplementingClasses));
      return result.toArray(new IMethod[result.size()]);
    } finally {
      if (pm != null) {
        pm.done();
      }
    }
  }

  @Override
  public Change createChange(IProgressMonitor monitor) throws CoreException {
    try {
      final TextChange[] changes = fChangeManager.getAllChanges();
      final List<TextChange> list = new ArrayList<TextChange>(changes.length);
      list.addAll(Arrays.asList(changes));
      String project = null;
      IJavaProject javaProject = fMethod.getJavaProject();
      if (javaProject != null) project = javaProject.getElementName();
      int flags =
          JavaRefactoringDescriptor.JAR_MIGRATION
              | JavaRefactoringDescriptor.JAR_REFACTORING
              | RefactoringDescriptor.STRUCTURAL_CHANGE;
      try {
        if (!Flags.isPrivate(fMethod.getFlags())) flags |= RefactoringDescriptor.MULTI_CHANGE;
      } catch (JavaModelException exception) {
        JavaPlugin.log(exception);
      }
      final IType declaring = fMethod.getDeclaringType();
      try {
        if (declaring.isAnonymous() || declaring.isLocal())
          flags |= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
      } catch (JavaModelException exception) {
        JavaPlugin.log(exception);
      }
      final String description =
          Messages.format(
              RefactoringCoreMessages.RenameMethodProcessor_descriptor_description_short,
              BasicElementLabels.getJavaElementName(fMethod.getElementName()));
      final String header =
          Messages.format(
              RefactoringCoreMessages.RenameMethodProcessor_descriptor_description,
              new String[] {
                JavaElementLabels.getTextLabel(fMethod, JavaElementLabels.ALL_FULLY_QUALIFIED),
                BasicElementLabels.getJavaElementName(getNewElementName())
              });
      final String comment = new JDTRefactoringDescriptorComment(project, this, header).asString();
      final RenameJavaElementDescriptor descriptor =
          RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(
              IJavaRefactorings.RENAME_METHOD);
      descriptor.setProject(project);
      descriptor.setDescription(description);
      descriptor.setComment(comment);
      descriptor.setFlags(flags);
      descriptor.setJavaElement(fMethod);
      descriptor.setNewName(getNewElementName());
      descriptor.setUpdateReferences(fUpdateReferences);
      descriptor.setKeepOriginal(fDelegateUpdating);
      descriptor.setDeprecateDelegate(fDelegateDeprecation);
      return new DynamicValidationRefactoringChange(
          descriptor,
          RefactoringCoreMessages.RenameMethodProcessor_change_name,
          list.toArray(new Change[list.size()]));
    } finally {
      monitor.done();
    }
  }

  private TextChangeManager createChanges(IProgressMonitor pm, RefactoringStatus status)
      throws CoreException {
    if (!fIsComposite) fChangeManager.clear();
    addOccurrences(fChangeManager, pm, status);
    return fChangeManager;
  }

  /**
   * Add occurrences
   *
   * @param manager the text change manager
   * @param pm the progress monitor
   * @param status the status
   * @throws CoreException if change creation failed
   */
  protected void addOccurrences(
      TextChangeManager manager, IProgressMonitor pm, RefactoringStatus status)
      throws CoreException /*thrown in subtype*/ {
    pm.beginTask("", fOccurrences.length); // $NON-NLS-1$
    for (int i = 0; i < fOccurrences.length; i++) {
      ICompilationUnit cu = fOccurrences[i].getCompilationUnit();
      if (cu == null) continue;

      SearchMatch[] results = fOccurrences[i].getSearchResults();

      // Split matches into declaration and non-declaration matches

      List<SearchMatch> declarationsInThisCu = new ArrayList<SearchMatch>();
      List<SearchMatch> referencesInThisCu = new ArrayList<SearchMatch>();

      for (int j = 0; j < results.length; j++) {
        if (results[j] instanceof MethodDeclarationMatch) declarationsInThisCu.add(results[j]);
        else referencesInThisCu.add(results[j]);
      }

      // First, handle the declarations
      if (declarationsInThisCu.size() > 0) {

        if (fDelegateUpdating) {
          // Update with delegates
          CompilationUnitRewrite rewrite = new CompilationUnitRewrite(cu);
          rewrite.setResolveBindings(true);

          for (Iterator<SearchMatch> iter = declarationsInThisCu.iterator(); iter.hasNext(); ) {
            SearchMatch element = iter.next();
            MethodDeclaration method =
                ASTNodeSearchUtil.getMethodDeclarationNode(
                    (IMethod) element.getElement(), rewrite.getRoot());
            DelegateCreator creator = new DelegateMethodCreator();
            creator.setDeclareDeprecated(fDelegateDeprecation);
            creator.setDeclaration(method);
            creator.setSourceRewrite(rewrite);
            creator.setNewElementName(getNewElementName());
            creator.prepareDelegate();
            creator.createEdit();
          }
          // Need to handle all delegates first as this
          // creates a completely new change object.
          TextChange changeForThisCu = rewrite.createChange(true);
          changeForThisCu.setKeepPreviewEdits(true);
          manager.manage(cu, changeForThisCu);
        }

        // Update the normal methods
        for (Iterator<SearchMatch> iter = declarationsInThisCu.iterator(); iter.hasNext(); ) {
          SearchMatch element = iter.next();
          simpleUpdate(element, cu, manager.get(cu));
        }
      }

      // Second, handle references
      if (fUpdateReferences) {
        for (Iterator<SearchMatch> iter = referencesInThisCu.iterator(); iter.hasNext(); ) {
          SearchMatch element = iter.next();
          simpleUpdate(element, cu, manager.get(cu));
        }
      }

      pm.worked(1);
      if (pm.isCanceled()) throw new OperationCanceledException();
    }
    pm.done();
  }

  private void simpleUpdate(SearchMatch element, ICompilationUnit cu, TextChange textChange) {
    String editName = RefactoringCoreMessages.RenameMethodRefactoring_update_occurrence;
    ReplaceEdit replaceEdit = createReplaceEdit(element, cu);
    addTextEdit(textChange, editName, replaceEdit);
  }

  protected final ReplaceEdit createReplaceEdit(SearchMatch searchResult, ICompilationUnit cu) {
    if (searchResult.isImplicit()) { // handle Annotation Element references, see bug 94062
      StringBuffer sb = new StringBuffer(getNewElementName());
      if (JavaCore.INSERT.equals(
          cu.getJavaProject()
              .getOption(
                  DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR,
                  true))) sb.append(' ');
      sb.append('=');
      if (JavaCore.INSERT.equals(
          cu.getJavaProject()
              .getOption(
                  DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR,
                  true))) sb.append(' ');
      return new ReplaceEdit(searchResult.getOffset(), 0, sb.toString());
    } else {
      return new ReplaceEdit(
          searchResult.getOffset(), searchResult.getLength(), getNewElementName());
    }
  }

  /**
   * Initializes the refactoring from scripting arguments. Used by {@link
   * RenameVirtualMethodProcessor} and {@link RenameNonVirtualMethodProcessor}
   *
   * @param extended the arguments
   * @return the resulting status
   */
  protected final RefactoringStatus initialize(JavaRefactoringArguments extended) {
    fInitialized = true;
    final String handle = extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
    if (handle != null) {
      final IJavaElement element =
          JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
      final String refactoring = getProcessorName();
      if (element instanceof IMethod) {
        final IMethod method = (IMethod) element;
        final IType declaring = method.getDeclaringType();
        if (declaring != null && declaring.exists()) {
          final IMethod[] methods = declaring.findMethods(method);
          if (methods != null && methods.length == 1 && methods[0] != null) {
            if (!methods[0].exists())
              return JavaRefactoringDescriptorUtil.createInputFatalStatus(
                  methods[0], refactoring, IJavaRefactorings.RENAME_METHOD);
            fMethod = methods[0];
            initializeWorkingCopyOwner();
          } else
            return JavaRefactoringDescriptorUtil.createInputFatalStatus(
                null, refactoring, IJavaRefactorings.RENAME_METHOD);
        } else
          return JavaRefactoringDescriptorUtil.createInputFatalStatus(
              element, refactoring, IJavaRefactorings.RENAME_METHOD);
      } else
        return JavaRefactoringDescriptorUtil.createInputFatalStatus(
            element, refactoring, IJavaRefactorings.RENAME_METHOD);
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
    final String name = extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME);
    if (name != null && !"".equals(name)) // $NON-NLS-1$
    setNewElementName(name);
    else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
    final String references =
        extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES);
    if (references != null) {
      fUpdateReferences = Boolean.valueOf(references).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              JavaRefactoringDescriptorUtil.ATTRIBUTE_REFERENCES));
    final String delegate = extended.getAttribute(ATTRIBUTE_DELEGATE);
    if (delegate != null) {
      fDelegateUpdating = Boolean.valueOf(delegate).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_DELEGATE));
    final String deprecate = extended.getAttribute(ATTRIBUTE_DEPRECATE);
    if (deprecate != null) {
      fDelegateDeprecation = Boolean.valueOf(deprecate).booleanValue();
    } else
      return RefactoringStatus.createFatalErrorStatus(
          Messages.format(
              RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
              ATTRIBUTE_DEPRECATE));
    return new RefactoringStatus();
  }

  protected void addTextEdit(TextChange change, String editName, ReplaceEdit replaceEdit) {
    if (fIsComposite)
      TextChangeCompatibility.addTextEdit(change, editName, replaceEdit, fCategorySet);
    else TextChangeCompatibility.addTextEdit(change, editName, replaceEdit);
  }
}

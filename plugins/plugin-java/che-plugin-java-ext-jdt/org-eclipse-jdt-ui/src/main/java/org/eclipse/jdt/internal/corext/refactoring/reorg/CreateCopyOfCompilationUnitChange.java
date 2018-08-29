/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.IRefactoringSearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine2;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.nls.changes.CreateTextFileChange;
import org.eclipse.jdt.internal.corext.refactoring.rename.TypeOccurrenceCollector;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.JavaElementResourceMapping;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.text.edits.ReplaceEdit;

public final class CreateCopyOfCompilationUnitChange extends CreateTextFileChange {

  private static TextChangeManager createChangeManager(
      IProgressMonitor monitor, ICompilationUnit copy, String newName) throws CoreException {
    TextChangeManager manager = new TextChangeManager();
    SearchResultGroup refs = getReferences(copy, monitor);
    if (refs == null) return manager;
    if (refs.getCompilationUnit() == null) return manager;

    String name = RefactoringCoreMessages.CopyRefactoring_update_ref;
    SearchMatch[] results = refs.getSearchResults();
    for (int j = 0; j < results.length; j++) {
      SearchMatch searchResult = results[j];
      if (searchResult.getAccuracy() == SearchMatch.A_INACCURATE) continue;
      int offset = searchResult.getOffset();
      int length = searchResult.getLength();
      TextChangeCompatibility.addTextEdit(
          manager.get(copy), name, new ReplaceEdit(offset, length, newName));
    }
    return manager;
  }

  private static SearchPattern createSearchPattern(IType type) throws JavaModelException {
    SearchPattern pattern =
        SearchPattern.createPattern(
            type, IJavaSearchConstants.ALL_OCCURRENCES, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
    IMethod[] constructors = JavaElementUtil.getAllConstructors(type);
    if (constructors.length == 0) return pattern;
    SearchPattern constructorDeclarationPattern =
        RefactoringSearchEngine.createOrPattern(constructors, IJavaSearchConstants.DECLARATIONS);
    return SearchPattern.createOrPattern(pattern, constructorDeclarationPattern);
  }

  private static String getCopiedFileSource(
      IProgressMonitor monitor, ICompilationUnit unit, String newTypeName) throws CoreException {
    ICompilationUnit copy = unit.getPrimary().getWorkingCopy(null);
    try {
      TextChangeManager manager = createChangeManager(monitor, copy, newTypeName);
      String result = manager.get(copy).getPreviewContent(new NullProgressMonitor());
      return result;
    } finally {
      copy.discardWorkingCopy();
    }
  }

  private static SearchResultGroup getReferences(
      final ICompilationUnit copy, IProgressMonitor monitor) throws JavaModelException {
    final ICompilationUnit[] copies = new ICompilationUnit[] {copy};
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(copies);
    final IType type = copy.findPrimaryType();
    if (type == null) return null;
    SearchPattern pattern = createSearchPattern(type);
    final RefactoringSearchEngine2 engine = new RefactoringSearchEngine2(pattern);
    engine.setScope(scope);
    engine.setWorkingCopies(copies);
    engine.setRequestor(
        new IRefactoringSearchRequestor() {
          TypeOccurrenceCollector fTypeOccurrenceCollector = new TypeOccurrenceCollector(type);

          public SearchMatch acceptSearchMatch(SearchMatch match) {
            try {
              return fTypeOccurrenceCollector.acceptSearchMatch2(copy, match);
            } catch (CoreException e) {
              JavaPlugin.log(e);
              return null;
            }
          }
        });

    engine.searchPattern(monitor);
    final Object[] results = engine.getResults();
    // Assert.isTrue(results.length <= 1);
    // just 1 file or none, but inaccurate matches can play bad here (see
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=106127)
    for (int index = 0; index < results.length; index++) {
      SearchResultGroup group = (SearchResultGroup) results[index];
      if (group.getCompilationUnit().equals(copy)) return group;
    }
    return null;
  }

  private final INewNameQuery fNameQuery;

  private final ICompilationUnit fOldCu;

  public CreateCopyOfCompilationUnitChange(
      IPath path, String source, ICompilationUnit oldCu, INewNameQuery nameQuery) {
    super(path, source, null, "java"); // $NON-NLS-1$
    fOldCu = oldCu;
    fNameQuery = nameQuery;
    setEncoding(oldCu);
  }

  @Override
  public String getName() {
    String cuName = BasicElementLabels.getResourceName(fOldCu.getElementName());
    String cuContainerName = BasicElementLabels.getPathLabel(fOldCu.getParent().getPath(), false);
    return Messages.format(
        RefactoringCoreMessages.CreateCopyOfCompilationUnitChange_create_copy,
        new String[] {cuName, cuContainerName});
  }

  @Override
  protected IFile getOldFile(IProgressMonitor monitor) throws OperationCanceledException {
    try {
      monitor.beginTask("", 12); // $NON-NLS-1$
      String oldSource = super.getSource();
      IPath oldPath = super.getPath();
      String newTypeName = fNameQuery.getNewName();
      try {
        String newSource =
            getCopiedFileSource(new SubProgressMonitor(monitor, 9), fOldCu, newTypeName);
        setSource(newSource);
        setPath(
            fOldCu
                .getResource()
                .getParent()
                .getFullPath()
                .append(JavaModelUtil.getRenamedCUName(fOldCu, newTypeName)));
        return super.getOldFile(new SubProgressMonitor(monitor, 1));
      } catch (CoreException e) {
        setSource(oldSource);
        setPath(oldPath);
        return super.getOldFile(new SubProgressMonitor(monitor, 2));
      }
    } finally {
      monitor.done();
    }
  }

  private void markAsExecuted(ICompilationUnit unit, ResourceMapping mapping) {
    ReorgExecutionLog log = (ReorgExecutionLog) getAdapter(ReorgExecutionLog.class);
    if (log != null) {
      log.markAsProcessed(unit);
      log.markAsProcessed(mapping);
    }
  }

  @Override
  public Change perform(IProgressMonitor monitor) throws CoreException {
    ResourceMapping mapping = JavaElementResourceMapping.create(fOldCu);
    final Change result = super.perform(monitor);
    markAsExecuted(fOldCu, mapping);
    return result;
  }

  private void setEncoding(ICompilationUnit unit) {
    IResource resource = unit.getResource();
    // no file so the encoding is taken from the target
    if (!(resource instanceof IFile)) return;
    IFile file = (IFile) resource;
    try {
      String encoding = file.getCharset(false);
      if (encoding != null) {
        setEncoding(encoding, true);
      } else {
        encoding = file.getCharset(true);
        if (encoding != null) {
          setEncoding(encoding, false);
        }
      }
    } catch (CoreException e) {
      // Take encoding from target
    }
  }
}

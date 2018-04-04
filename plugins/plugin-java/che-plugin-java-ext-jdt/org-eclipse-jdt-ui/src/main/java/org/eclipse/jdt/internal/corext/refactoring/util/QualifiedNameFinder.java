/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.ui.util.PatternConstructor;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.text.edits.ReplaceEdit;

public class QualifiedNameFinder {

  private static final GroupCategorySet QUALIFIED_NAMES =
      new GroupCategorySet(
          new GroupCategory(
              "org.eclipse.jdt.internal.corext.qualifiedNames", // $NON-NLS-1$
              RefactoringCoreMessages.QualifiedNameFinder_qualifiedNames_name,
              RefactoringCoreMessages.QualifiedNameFinder_qualifiedNames_description));

  private static class ResultCollector extends TextSearchRequestor {

    private String fNewValue;
    private QualifiedNameSearchResult fResult;

    public ResultCollector(QualifiedNameSearchResult result, String newValue) {
      fResult = result;
      fNewValue = newValue;
    }

    @Override
    public boolean acceptFile(IFile file) throws CoreException {
      IJavaElement element = JavaCore.create(file);
      if ((element != null && element.exists())) return false;

      // Only touch text files (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=114153 ):
      if (!FileBuffers.getTextFileBufferManager().isTextFileLocation(file.getFullPath(), false))
        return false;

      IPath path = file.getProjectRelativePath();
      String segment = path.segment(0);
      if (segment != null
          && (segment.startsWith(".refactorings")
              || segment.startsWith(".deprecations"))) // $NON-NLS-1$
        // $NON-NLS-2$
        return false;

      return true;
    }

    @Override
    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
      int start = matchAccess.getMatchOffset();
      int length = matchAccess.getMatchLength();

      // skip embedded FQNs (bug 130764):
      if (start > 0) {
        char before = matchAccess.getFileContentChar(start - 1);
        if (before == '.' || Character.isJavaIdentifierPart(before)) return true;
      }
      int fileContentLength = matchAccess.getFileContentLength();
      int end = start + length;
      if (end < fileContentLength) {
        char after = matchAccess.getFileContentChar(end);
        if (Character.isJavaIdentifierPart(after)) return true;
      }

      IFile file = matchAccess.getFile();
      TextChange change = fResult.getChange(file);
      TextChangeCompatibility.addTextEdit(
          change,
          RefactoringCoreMessages.QualifiedNameFinder_update_name,
          new ReplaceEdit(start, length, fNewValue),
          QUALIFIED_NAMES);

      return true;
    }
  }

  public QualifiedNameFinder() {}

  public static void process(
      QualifiedNameSearchResult result,
      String pattern,
      String newValue,
      String filePatterns,
      IProject root,
      IProgressMonitor monitor) {
    Assert.isNotNull(pattern);
    Assert.isNotNull(newValue);
    Assert.isNotNull(root);

    if (monitor == null) monitor = new NullProgressMonitor();

    if (filePatterns == null || filePatterns.length() == 0) {
      // Eat progress.
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.worked(1);
      return;
    }

    ResultCollector collector = new ResultCollector(result, newValue);
    TextSearchEngine engine = TextSearchEngine.create();
    Pattern searchPattern = PatternConstructor.createPattern(pattern, true, false);

    engine.search(createScope(filePatterns, root), collector, searchPattern, monitor);
  }

  private static TextSearchScope createScope(String filePatterns, IProject root) {
    HashSet<IProject> res = new HashSet<IProject>();
    res.add(root);
    addReferencingProjects(root, res);
    IResource[] resArr = res.toArray(new IResource[res.size()]);
    Pattern filePattern = getFilePattern(filePatterns);

    return TextSearchScope.newSearchScope(resArr, filePattern, false);
  }

  private static Pattern getFilePattern(String filePatterns) {
    StringTokenizer tokenizer = new StringTokenizer(filePatterns, ","); // $NON-NLS-1$
    String[] filePatternArray = new String[tokenizer.countTokens()];
    int i = 0;
    while (tokenizer.hasMoreTokens()) {
      filePatternArray[i++] = tokenizer.nextToken().trim();
    }
    return PatternConstructor.createPattern(filePatternArray, true, false);
  }

  private static void addReferencingProjects(IProject root, Set<IProject> res) {
    IProject[] projects = root.getReferencingProjects();
    for (int i = 0; i < projects.length; i++) {
      IProject project = projects[i];
      if (res.add(project)) {
        addReferencingProjects(project, res);
      }
    }
  }
}

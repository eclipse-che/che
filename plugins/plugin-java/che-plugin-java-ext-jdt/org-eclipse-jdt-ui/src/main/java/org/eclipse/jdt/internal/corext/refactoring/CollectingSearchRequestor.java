/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;

/**
 * Collects the results returned by a <code>SearchEngine</code>. If a {@link
 * ReferencesInBinaryContext} is passed, matches that are inside a binary element are not collected
 * (but added to the context if they are accurate).
 */
public class CollectingSearchRequestor extends SearchRequestor {
  private final ArrayList<SearchMatch> fFound;
  private final ReferencesInBinaryContext fBinaryRefs;

  public CollectingSearchRequestor() {
    this(null);
  }

  public CollectingSearchRequestor(ReferencesInBinaryContext binaryRefs) {
    fFound = new ArrayList<SearchMatch>();
    fBinaryRefs = binaryRefs;
  }

  /**
   * The default implementation calls {@link #collectMatch(SearchMatch)} for all matches that make
   * it through {@link #filterMatch(SearchMatch)}.
   *
   * @param match the found match
   * @throws CoreException
   * @see
   *     org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
   */
  @Override
  public void acceptSearchMatch(SearchMatch match) throws CoreException {
    if (!filterMatch(match)) collectMatch(match);
  }

  public void collectMatch(SearchMatch match) {
    fFound.add(match);
  }

  /**
   * Returns whether the given match should be filtered out. The default implementation filters out
   * matches in binaries iff {@link #CollectingSearchRequestor(ReferencesInBinaryContext)} has been
   * called with a non-<code>null</code> argument. Accurate binary matches are added to the {@link
   * ReferencesInBinaryContext}.
   *
   * @param match the match to test
   * @return <code>true</code> iff the given match should <em>not</em> be collected
   * @throws CoreException
   */
  public boolean filterMatch(SearchMatch match) throws CoreException {
    if (fBinaryRefs == null) return false;

    if (match.getAccuracy() == SearchMatch.A_ACCURATE && isBinaryElement(match.getElement())) {
      // binary classpaths are often incomplete -> avoiding false positives from inaccurate matches
      fBinaryRefs.add(match);
      return true;
    }

    return false;
  }

  private static boolean isBinaryElement(Object element) throws JavaModelException {
    if (element instanceof IMember) {
      return ((IMember) element).isBinary();

    } else if (element instanceof ICompilationUnit) {
      return true;

    } else if (element instanceof IClassFile) {
      return false;

    } else if (element instanceof IPackageFragment) {
      return isBinaryElement(((IPackageFragment) element).getParent());

    } else if (element instanceof IPackageFragmentRoot) {
      return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_BINARY;
    }
    return false;
  }

  /** @return a List of {@link SearchMatch}es (not sorted) */
  public List<SearchMatch> getResults() {
    return fFound;
  }
}

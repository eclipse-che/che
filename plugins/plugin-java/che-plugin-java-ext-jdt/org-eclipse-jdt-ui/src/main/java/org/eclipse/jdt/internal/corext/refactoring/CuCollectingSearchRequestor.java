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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.util.SearchUtils;

/**
 * Collects the results returned by a <code>SearchEngine</code>. Only collects matches in CUs ands
 * offers a scanner to trim match ranges. If a {@link ReferencesInBinaryContext} is passed, matches
 * that are inside a binary element are not collected (but added to the context if they are
 * accurate).
 */
public class CuCollectingSearchRequestor extends CollectingSearchRequestor {

  private IJavaProject fProjectCache;
  private IScanner fScannerCache;

  public CuCollectingSearchRequestor() {
    this(null);
  }

  public CuCollectingSearchRequestor(ReferencesInBinaryContext binaryRefs) {
    super(binaryRefs);
  }

  protected IScanner getScanner(ICompilationUnit unit) {
    IJavaProject project = unit.getJavaProject();
    if (project.equals(fProjectCache)) return fScannerCache;

    fProjectCache = project;
    String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
    String complianceLevel = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
    fScannerCache = ToolFactory.createScanner(false, false, false, sourceLevel, complianceLevel);
    return fScannerCache;
  }

  /**
   * This is an internal method. Do not call from subclasses! Use {@link #collectMatch(SearchMatch)}
   * instead.
   *
   * @param match
   * @throws CoreException
   * @deprecated
   */
  @Override
  public final void acceptSearchMatch(SearchMatch match) throws CoreException {
    if (filterMatch(match)) return;

    ICompilationUnit unit = SearchUtils.getCompilationUnit(match);
    if (unit != null) {
      acceptSearchMatch(unit, match);
    }
  }

  /**
   * Handles the given match in the given compilation unit. The default implementation accepts all
   * matches. Subclasses can override and call {@link #collectMatch(SearchMatch)} to collect
   * matches.
   *
   * @param unit the enclosing CU of the match, never <code>null</code>
   * @param match the match
   * @throws CoreException if something bad happens
   */
  protected void acceptSearchMatch(ICompilationUnit unit, SearchMatch match) throws CoreException {
    collectMatch(match);
  }

  @Override
  public void endReporting() {
    fProjectCache = null;
    fScannerCache = null;
  }
}

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

/**
 * Helper class to use the search engine in refactorings.
 *
 * <p>TODO: - is inefficient: uses sets instead of lists, creates useless intermediate collections -
 * destroys locality by doing multiple passes over result sets instead of processing results in a
 * pipeline - does not allow users to modify search matches - generates boilerplate error messages
 * and has no way to configure them
 *
 * @since 3.1
 */
public final class RefactoringSearchEngine2 {

  /** Default implementation of a search requestor */
  private static class DefaultSearchRequestor implements IRefactoringSearchRequestor {

    public final SearchMatch acceptSearchMatch(final SearchMatch match) {
      return match;
    }
  }

  /** Search requestor which only collects compilation units */
  private class RefactoringCompilationUnitCollector extends RefactoringSearchCollector {

    /** The collected compilation units */
    private final Set<ICompilationUnit> fCollectedUnits = new HashSet<ICompilationUnit>();

    /** The inaccurate matches */
    private final Set<SearchMatch> fInaccurateMatches = new HashSet<SearchMatch>();

    @Override
    public final void acceptSearchMatch(final SearchMatch match) throws CoreException {
      final SearchMatch accepted = fRequestor.acceptSearchMatch(match);
      if (accepted != null) {
        final IResource resource = accepted.getResource();
        if (!resource.equals(fLastResource)) {
          final IJavaElement element = JavaCore.create(resource);
          if (element instanceof ICompilationUnit) fCollectedUnits.add((ICompilationUnit) element);
        }
        if (fInaccurate
            && accepted.getAccuracy() == SearchMatch.A_INACCURATE
            && !fInaccurateMatches.contains(accepted)) {
          fStatus.addEntry(
              fSeverity,
              Messages.format(
                  RefactoringCoreMessages.RefactoringSearchEngine_inaccurate_match,
                  BasicElementLabels.getResourceName(accepted.getResource())),
              null,
              null,
              RefactoringStatusEntry.NO_CODE);
          fInaccurateMatches.add(accepted);
        }
      }
    }

    @Override
    public final void clearResults() {
      super.clearResults();
      fCollectedUnits.clear();
      fInaccurateMatches.clear();
    }

    @Override
    public final Collection<IResource> getBinaryResources() {
      return Collections.emptySet();
    }

    @Override
    public final Collection<ICompilationUnit> getCollectedMatches() {
      return fCollectedUnits;
    }

    @Override
    public final Collection<SearchMatch> getInaccurateMatches() {
      return fInaccurateMatches;
    }
  }

  private abstract class RefactoringSearchCollector extends SearchRequestor {

    protected IResource fLastResource = null;

    public void clearResults() {
      fLastResource = null;
    }

    public abstract Collection<IResource> getBinaryResources();

    public abstract Collection<?> getCollectedMatches();

    public abstract Collection<SearchMatch> getInaccurateMatches();
  }

  /** Search requestor which collects every search match */
  private class RefactoringSearchMatchCollector extends RefactoringSearchCollector {

    /** The binary resources */
    private final Set<IResource> fBinaryResources = new HashSet<IResource>();

    /** The collected matches */
    private final List<SearchMatch> fCollectedMatches = new ArrayList<SearchMatch>();

    /** The inaccurate matches */
    private final Set<SearchMatch> fInaccurateMatches = new HashSet<SearchMatch>();

    @Override
    public final void acceptSearchMatch(final SearchMatch match) throws CoreException {
      final SearchMatch accepted = fRequestor.acceptSearchMatch(match);
      if (accepted != null) {
        fCollectedMatches.add(accepted);
        final IResource resource = accepted.getResource();
        if (!resource.equals(fLastResource)) {
          if (fBinary) {
            final IJavaElement element = JavaCore.create(resource);
            if (!(element instanceof ICompilationUnit)) {
              final IProject project = resource.getProject();
              if (!fGrouping)
                fStatus.addEntry(
                    fSeverity,
                    Messages.format(
                        RefactoringCoreMessages.RefactoringSearchEngine_binary_match_ungrouped,
                        BasicElementLabels.getResourceName(project)),
                    null,
                    null,
                    RefactoringStatusEntry.NO_CODE);
              else if (!fBinaryResources.contains(resource))
                fStatus.addEntry(
                    fSeverity,
                    Messages.format(
                        RefactoringCoreMessages.RefactoringSearchEngine_binary_match_grouped,
                        BasicElementLabels.getResourceName(project)),
                    null,
                    null,
                    RefactoringStatusEntry.NO_CODE);
              fBinaryResources.add(resource);
            }
          }
          if (fInaccurate
              && accepted.getAccuracy() == SearchMatch.A_INACCURATE
              && !fInaccurateMatches.contains(accepted)) {
            fStatus.addEntry(
                fSeverity,
                Messages.format(
                    RefactoringCoreMessages.RefactoringSearchEngine_inaccurate_match,
                    BasicElementLabels.getResourceName(resource)),
                null,
                null,
                RefactoringStatusEntry.NO_CODE);
            fInaccurateMatches.add(accepted);
          }
        }
      }
    }

    @Override
    public final void clearResults() {
      super.clearResults();
      fCollectedMatches.clear();
      fInaccurateMatches.clear();
      fBinaryResources.clear();
    }

    @Override
    public final Collection<IResource> getBinaryResources() {
      return fBinaryResources;
    }

    @Override
    public final Collection<SearchMatch> getCollectedMatches() {
      return fCollectedMatches;
    }

    @Override
    public final Collection<SearchMatch> getInaccurateMatches() {
      return fInaccurateMatches;
    }
  }

  /** The compilation unit granularity */
  public static final int GRANULARITY_COMPILATION_UNIT = 2;

  /** The search match granularity */
  public static final int GRANULARITY_SEARCH_MATCH = 1;

  /** Should binary matches be filtered? */
  private boolean fBinary = false;

  /** The refactoring search collector */
  private RefactoringSearchCollector fCollector = null;

  /** The search granularity */
  private int fGranularity = GRANULARITY_SEARCH_MATCH;

  /** Should the matches be grouped by resource? */
  private boolean fGrouping = true;

  /** Should inaccurate matches be filtered? */
  private boolean fInaccurate = true;

  /** The working copy owner, or <code>null</code> */
  private WorkingCopyOwner fOwner = null;

  /** The search pattern, or <code>null</code> */
  private SearchPattern fPattern = null;

  /** The search requestor */
  private IRefactoringSearchRequestor fRequestor = new DefaultSearchRequestor();

  /** The search scope */
  private IJavaSearchScope fScope = SearchEngine.createWorkspaceScope();

  /** The severity */
  private int fSeverity = RefactoringStatus.WARNING;

  /** The search status */
  private RefactoringStatus fStatus = new RefactoringStatus();

  /** The working copies */
  private ICompilationUnit[] fWorkingCopies = {};

  /** Creates a new refactoring search engine. */
  public RefactoringSearchEngine2() {
    // Do nothing
  }

  /**
   * Creates a new refactoring search engine.
   *
   * @param pattern the search pattern
   */
  public RefactoringSearchEngine2(final SearchPattern pattern) {
    Assert.isNotNull(pattern);
    fPattern = pattern;
  }

  /**
   * Clears all results found so far, and sets resets the status to {@link RefactoringStatus#OK}.
   */
  public final void clearResults() {
    getCollector().clearResults();
    fStatus = new RefactoringStatus();
  }

  /**
   * Returns the affected compilation units of the previous search queries.
   *
   * <p>In order to retrieve the compilation units, grouping by resource must have been enabled
   * before searching.
   *
   * @return the compilation units of the previous queries
   */
  public final ICompilationUnit[] getAffectedCompilationUnits() {
    if (fGranularity == GRANULARITY_COMPILATION_UNIT) {
      final Collection<?> collection = getCollector().getCollectedMatches();
      final ICompilationUnit[] units = new ICompilationUnit[collection.size()];
      int index = 0;
      for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext(); index++)
        units[index] = (ICompilationUnit) iterator.next();
      return units;
    } else {
      final SearchResultGroup[] groups = getGroupedMatches();
      final ICompilationUnit[] units = new ICompilationUnit[groups.length];
      for (int index = 0; index < groups.length; index++)
        units[index] = groups[index].getCompilationUnit();
      return units;
    }
  }

  /**
   * Returns the affected java projects of the previous search queries.
   *
   * <p>In order to retrieve the java projects, grouping by resource must have been enabled before
   * searching.
   *
   * @return the java projects of the previous queries (element type: <code>
   *     &lt;IJavaProject, Collection&lt;SearchResultGroup&gt;&gt;</code> if granularity is {@link
   *     #GRANULARITY_SEARCH_MATCH} or <code>
   *     &lt;IJavaProject, Collection&lt;ICompilationUnit&gt;&gt;</code> if it is {@link
   *     #GRANULARITY_COMPILATION_UNIT}).
   */
  public final Map<IJavaProject, ? extends Set<?>> getAffectedProjects() {
    IJavaProject project = null;
    ICompilationUnit unit = null;
    if (fGranularity == GRANULARITY_COMPILATION_UNIT) {
      final Map<IJavaProject, Set<ICompilationUnit>> map =
          new HashMap<IJavaProject, Set<ICompilationUnit>>();
      final ICompilationUnit[] units = getAffectedCompilationUnits();
      for (int index = 0; index < units.length; index++) {
        unit = units[index];
        project = unit.getJavaProject();
        if (project != null) {
          Set<ICompilationUnit> set = map.get(project);
          if (set == null) {
            set = new HashSet<ICompilationUnit>();
            map.put(project, set);
          }
          set.add(unit);
        }
      }
      return map;
    } else {
      final Map<IJavaProject, Set<SearchResultGroup>> map =
          new HashMap<IJavaProject, Set<SearchResultGroup>>();
      final SearchResultGroup[] groups = getGroupedMatches();
      SearchResultGroup group = null;
      for (int index = 0; index < groups.length; index++) {
        group = groups[index];
        unit = group.getCompilationUnit();
        if (unit != null) {
          project = unit.getJavaProject();
          if (project != null) {
            Set<SearchResultGroup> set = map.get(project);
            if (set == null) {
              set = new HashSet<SearchResultGroup>();
              map.put(project, set);
            }
            set.add(group);
          }
        }
      }
      return map;
    }
  }

  /**
   * Returns the refactoring search collector.
   *
   * @return the refactoring search collector
   */
  private RefactoringSearchCollector getCollector() {
    if (fCollector == null) {
      if (fGranularity == GRANULARITY_COMPILATION_UNIT)
        fCollector = new RefactoringCompilationUnitCollector();
      else if (fGranularity == GRANULARITY_SEARCH_MATCH)
        fCollector = new RefactoringSearchMatchCollector();
      else Assert.isTrue(false);
    }
    return fCollector;
  }

  /**
   * Returns the found search matches in grouped by their containing resource.
   *
   * @return the found search matches
   */
  private SearchResultGroup[] getGroupedMatches() {
    final Map<IResource, List<SearchMatch>> grouped = new HashMap<IResource, List<SearchMatch>>();
    List<SearchMatch> matches = null;
    IResource resource = null;
    SearchMatch match = null;
    for (final Iterator<?> iterator = getSearchMatches().iterator(); iterator.hasNext(); ) {
      match = (SearchMatch) iterator.next();
      resource = match.getResource();
      if (!grouped.containsKey(resource)) grouped.put(resource, new ArrayList<SearchMatch>(4));
      matches = grouped.get(resource);
      matches.add(match);
    }
    if (fBinary) {
      final Collection<IResource> collection = getCollector().getBinaryResources();
      for (final Iterator<IResource> iterator = grouped.keySet().iterator(); iterator.hasNext(); ) {
        resource = iterator.next();
        if (collection.contains(resource)) iterator.remove();
      }
    }
    final SearchResultGroup[] result = new SearchResultGroup[grouped.keySet().size()];
    int index = 0;
    for (final Iterator<IResource> iterator = grouped.keySet().iterator(); iterator.hasNext(); ) {
      resource = iterator.next();
      matches = grouped.get(resource);
      result[index++] =
          new SearchResultGroup(resource, matches.toArray(new SearchMatch[matches.size()]));
    }
    return result;
  }

  /**
   * Returns the search pattern currently used for searching.
   *
   * @return the search pattern
   */
  public final SearchPattern getPattern() {
    return fPattern;
  }

  /**
   * Returns the results of the previous search queries.
   *
   * <p>The result depends on the following conditions:
   *
   * <ul>
   *   <li>If the search granularity is {@link #GRANULARITY_COMPILATION_UNIT}, the results are
   *       elements of type {@link ICompilationUnit} .
   *   <li>If grouping by resource is enabled, the results are elements of type {@link
   *       SearchResultGroup}, otherwise the elements are of type {@link SearchMatch}.
   * </ul>
   *
   * @return the results of the previous queries
   */
  public final Object[] getResults() {
    if (fGranularity == GRANULARITY_COMPILATION_UNIT) return getAffectedCompilationUnits();
    else {
      if (fGrouping) return getGroupedMatches();
      else return getUngroupedMatches();
    }
  }

  /**
   * Returns the search matches filtered by their accuracy.
   *
   * @return the filtered search matches
   */
  private Collection<?> getSearchMatches() {
    Collection<?> results = null;
    if (fInaccurate) {
      results = new LinkedList<Object>(getCollector().getCollectedMatches());
      final Collection<SearchMatch> collection = getCollector().getInaccurateMatches();
      SearchMatch match = null;
      for (final Iterator<?> iterator = results.iterator(); iterator.hasNext(); ) {
        match = (SearchMatch) iterator.next();
        if (collection.contains(match)) iterator.remove();
      }
    } else results = getCollector().getCollectedMatches();
    return results;
  }

  /**
   * Returns the refactoring status of this search engine.
   *
   * @return the refactoring status
   */
  public final RefactoringStatus getStatus() {
    return fStatus;
  }

  /**
   * Returns the found search matches in no particular order.
   *
   * @return the found search matches
   */
  private SearchMatch[] getUngroupedMatches() {
    Collection<?> results = null;
    if (fBinary) {
      results = new LinkedList<Object>(getSearchMatches());
      final Collection<IResource> collection = getCollector().getBinaryResources();
      SearchMatch match = null;
      for (final Iterator<?> iterator = results.iterator(); iterator.hasNext(); ) {
        match = (SearchMatch) iterator.next();
        if (collection.contains(match.getResource())) iterator.remove();
      }
    } else results = getSearchMatches();
    final SearchMatch[] matches = new SearchMatch[results.size()];
    results.toArray(matches);
    return matches;
  }

  /**
   * Performs the search according to the specified pattern.
   *
   * @param monitor the progress monitor, or <code>null</code>
   * @throws JavaModelException if an error occurs during search
   */
  public final void searchPattern(IProgressMonitor monitor) throws JavaModelException {
    Assert.isNotNull(fPattern);
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(RefactoringCoreMessages.RefactoringSearchEngine_searching_occurrences);
      try {
        SearchEngine engine = null;
        if (fOwner != null) engine = new SearchEngine(fOwner);
        else engine = new SearchEngine(fWorkingCopies);
        engine.search(
            fPattern,
            SearchUtils.getDefaultSearchParticipants(),
            fScope,
            getCollector(),
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      } catch (CoreException exception) {
        throw new JavaModelException(exception);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Performs the search of referenced fields.
   *
   * @param element the java element whose referenced fields have to be found
   * @param monitor the progress monitor, or <code>null</code>
   * @throws JavaModelException if an error occurs during search
   */
  public final void searchReferencedFields(final IJavaElement element, IProgressMonitor monitor)
      throws JavaModelException {
    Assert.isNotNull(element);
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(
          RefactoringCoreMessages.RefactoringSearchEngine_searching_referenced_fields);
      try {
        SearchEngine engine = null;
        if (fOwner != null) engine = new SearchEngine(fOwner);
        else engine = new SearchEngine(fWorkingCopies);
        engine.searchDeclarationsOfAccessedFields(
            element,
            getCollector(),
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      } catch (CoreException exception) {
        throw new JavaModelException(exception);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Performs the search of referenced methods.
   *
   * @param element the java element whose referenced methods have to be found
   * @param monitor the progress monitor, or <code>null</code>
   * @throws JavaModelException if an error occurs during search
   */
  public final void searchReferencedMethods(final IJavaElement element, IProgressMonitor monitor)
      throws JavaModelException {
    Assert.isNotNull(element);
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(
          RefactoringCoreMessages.RefactoringSearchEngine_searching_referenced_methods);
      try {
        SearchEngine engine = null;
        if (fOwner != null) engine = new SearchEngine(fOwner);
        else engine = new SearchEngine(fWorkingCopies);
        engine.searchDeclarationsOfSentMessages(
            element,
            getCollector(),
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      } catch (CoreException exception) {
        throw new JavaModelException(exception);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Performs the search of referenced types.
   *
   * @param element the java element whose referenced types have to be found
   * @param monitor the progress monitor, or <code>null</code>
   * @throws JavaModelException if an error occurs during search
   */
  public final void searchReferencedTypes(final IJavaElement element, IProgressMonitor monitor)
      throws JavaModelException {
    Assert.isNotNull(element);
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      monitor.beginTask("", 1); // $NON-NLS-1$
      monitor.setTaskName(
          RefactoringCoreMessages.RefactoringSearchEngine_searching_referenced_types);
      try {
        SearchEngine engine = null;
        if (fOwner != null) engine = new SearchEngine(fOwner);
        else engine = new SearchEngine(fWorkingCopies);
        engine.searchDeclarationsOfReferencedTypes(
            element,
            getCollector(),
            new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
      } catch (CoreException exception) {
        throw new JavaModelException(exception);
      }
    } finally {
      monitor.done();
    }
  }

  /**
   * Determines how search matches are filtered.
   *
   * <p>This method must be called before start searching. The default is to filter inaccurate
   * matches only.
   *
   * @param inaccurate <code>true</code> to filter inaccurate matches, <code>false</code> otherwise
   * @param binary <code>true</code> to filter binary matches, <code>false</code> otherwise
   */
  public final void setFiltering(final boolean inaccurate, final boolean binary) {
    fInaccurate = inaccurate;
    fBinary = binary;
  }

  /**
   * Sets the granularity to use during the searches.
   *
   * <p>This method must be called before start searching. The default is a granularity of {@link
   * #GRANULARITY_SEARCH_MATCH}.
   *
   * @param granularity The granularity to use. Must be one of the <code>GRANULARITY_XXX</code>
   *     constants.
   */
  public final void setGranularity(final int granularity) {
    Assert.isTrue(
        granularity == GRANULARITY_COMPILATION_UNIT || granularity == GRANULARITY_SEARCH_MATCH);
    fGranularity = granularity;
  }

  /**
   * Sets the working copies to take precedence during the searches.
   *
   * <p>This method must be called before start searching. The default is to use no working copies
   *
   * @param copies the working copies to use
   */
  public final void setWorkingCopies(final ICompilationUnit[] copies) {
    Assert.isNotNull(copies);
    fWorkingCopies = new ICompilationUnit[copies.length];
    System.arraycopy(copies, 0, fWorkingCopies, 0, copies.length);
  }

  /**
   * Determines how search matches are grouped.
   *
   * <p>This method must be called before start searching. The default is to group by containing
   * resource.
   *
   * @param grouping <code>true</code> to group matches by their containing resource, <code>false
   *     </code> otherwise
   */
  public final void setGrouping(final boolean grouping) {
    fGrouping = grouping;
  }

  /**
   * Sets the disjunction of search patterns to be used during search.
   *
   * <p>This method must be called before {@link
   * RefactoringSearchEngine2#searchPattern(IProgressMonitor)}
   *
   * @param first the first search pattern to set
   * @param second the second search pattern to set
   */
  public final void setOrPattern(final SearchPattern first, final SearchPattern second) {
    Assert.isNotNull(first);
    Assert.isNotNull(second);
    fPattern = SearchPattern.createOrPattern(first, second);
  }

  /**
   * Sets the working copy owner to use during search.
   *
   * <p>This method must be called before start searching. The default is to use no working copy
   * owner.
   *
   * @param owner the working copy owner to use, or <code>null</code> to use none
   */
  public final void setOwner(final WorkingCopyOwner owner) {
    fOwner = owner;
  }

  /**
   * Sets the search pattern to be used during search.
   *
   * <p>This method must be called before {@link
   * RefactoringSearchEngine2#searchPattern(IProgressMonitor)}
   *
   * @param elements the set of elements
   * @param limitTo determines the nature of the expected matches. This is a combination of {@link
   *     org.eclipse.jdt.core.search .IJavaSearchConstants}.
   */
  public final void setPattern(final IJavaElement[] elements, final int limitTo) {
    Assert.isNotNull(elements);
    Assert.isTrue(elements.length > 0);
    SearchPattern pattern =
        SearchPattern.createPattern(elements[0], limitTo, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE);
    IJavaElement element = null;
    for (int index = 1; index < elements.length; index++) {
      element = elements[index];
      pattern =
          SearchPattern.createOrPattern(
              pattern,
              SearchPattern.createPattern(
                  element, limitTo, SearchUtils.GENERICS_AGNOSTIC_MATCH_RULE));
    }
    setPattern(pattern);
  }

  /**
   * Sets the search pattern to be used during search.
   *
   * <p>This method must be called before {@link
   * RefactoringSearchEngine2#searchPattern(IProgressMonitor)}
   *
   * @param pattern the search pattern to set
   */
  public final void setPattern(final SearchPattern pattern) {
    Assert.isNotNull(pattern);
    fPattern = pattern;
  }

  /**
   * Sets the search requestor for this search engine.
   *
   * <p>This method must be called before start searching. The default is a non-filtering search
   * requestor.
   *
   * @param requestor the search requestor to set
   */
  public final void setRequestor(final IRefactoringSearchRequestor requestor) {
    Assert.isNotNull(requestor);
    fRequestor = requestor;
  }

  /**
   * Sets the search scope for this search engine.
   *
   * <p>This method must be called before start searching. The default is the entire workspace as
   * search scope.
   *
   * @param scope the search scope to set
   */
  public final void setScope(final IJavaSearchScope scope) {
    Assert.isNotNull(scope);
    fScope = scope;
  }

  /**
   * Sets the severity of the generated status entries.
   *
   * <p>This method must be called before start searching. The default is a severity of {@link
   * RefactoringStatus#OK}.
   *
   * @param severity the severity to set
   */
  public final void setSeverity(final int severity) {
    Assert.isTrue(
        severity == RefactoringStatus.WARNING
            || severity == RefactoringStatus.INFO
            || severity == RefactoringStatus.FATAL
            || severity == RefactoringStatus.ERROR);
    fSeverity = severity;
  }

  /**
   * Sets the refactoring status for this search engine.
   *
   * <p>This method must be called before start searching. The default is an empty status with
   * status {@link RefactoringStatus#OK}.
   *
   * @param status the refactoring status to set
   */
  public final void setStatus(final RefactoringStatus status) {
    Assert.isNotNull(status);
    fStatus = status;
  }
}

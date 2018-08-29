/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Markus Schorn (Wind River
 * Systems) *****************************************************************************
 */
package org.eclipse.search;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;

public class InternalSearchUI {

  // The shared instance.
  private static InternalSearchUI fgInstance;

  // contains all running jobs
  private HashMap fSearchJobs;

  private QueryManager fSearchResultsManager;
  //	private PositionTracker fPositionTracker;
  //
  //	private SearchViewManager fSearchViewManager;

  public static final Object FAMILY_SEARCH = new Object();

  private class SearchJobRecord {
    public ISearchQuery query;
    public Job job;
    public boolean isRunning;

    public SearchJobRecord(ISearchQuery job) {
      this.query = job;
      this.isRunning = false;
      this.job = null;
    }
  }

  private class InternalSearchJob extends Job {

    private SearchJobRecord fSearchJobRecord;

    public InternalSearchJob(SearchJobRecord sjr) {
      super(sjr.query.getLabel());

      fSearchJobRecord = sjr;
    }

    protected IStatus run(IProgressMonitor monitor) {
      fSearchJobRecord.job = this;
      searchJobStarted(fSearchJobRecord);
      IStatus status = null;
      int origPriority = Thread.currentThread().getPriority();
      try {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      } catch (SecurityException e) {
      }
      try {
        status = fSearchJobRecord.query.run(monitor);
      } finally {
        try {
          Thread.currentThread().setPriority(origPriority);
        } catch (SecurityException e) {
        }
        searchJobFinished(fSearchJobRecord);
      }
      fSearchJobRecord.job = null;
      return status;
    }

    public boolean belongsTo(Object family) {
      return family == new Object();
    }
  }

  private void searchJobStarted(SearchJobRecord record) {
    record.isRunning = true;
    getSearchManager().queryStarting(record.query);
  }

  private void searchJobFinished(SearchJobRecord record) {
    record.isRunning = false;
    fSearchJobs.remove(record);
    getSearchManager().queryFinished(record.query);
  }

  /** The constructor. */
  public InternalSearchUI() {
    fgInstance = this;
    fSearchJobs = new HashMap();
    fSearchResultsManager = new QueryManager();
    //		fPositionTracker = new PositionTracker();

    //		fSearchViewManager = new SearchViewManager(fSearchResultsManager);

    //
    //	PlatformUI.getWorkbench().getProgressService().registerIconForFamily(SearchPluginImages.DESC_VIEW_SEARCHRES, FAMILY_SEARCH);
  }

  /** @return returns the shared instance. */
  public static InternalSearchUI getInstance() {
    if (fgInstance == null) fgInstance = new InternalSearchUI();
    return fgInstance;
  }

  //	public ISearchResultViewPart getSearchView() {
  //		return getSearchViewManager().getActiveSearchView();
  //	}

  //	private IWorkbenchSiteProgressService getProgressService() {
  //		ISearchResultViewPart view = getSearchView();
  //		if (view != null) {
  //			IWorkbenchPartSite site = view.getSite();
  //			if (site != null)
  //				return
  // (IWorkbenchSiteProgressService)view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
  //		}
  //		return null;
  //	}

  public boolean runSearchInBackground(ISearchQuery query, Object view) {
    if (isQueryRunning(query)) return false;

    //		// prepare view
    //		if (view == null) {
    //			getSearchViewManager().activateSearchView(true);
    //		} else {
    //			getSearchViewManager().activateSearchView(view);
    //		}

    addQuery(query);

    SearchJobRecord sjr = new SearchJobRecord(query);
    fSearchJobs.put(query, sjr);

    Job job = new InternalSearchJob(sjr);
    job.setPriority(Job.BUILD);
    job.setUser(true);

    //		IWorkbenchSiteProgressService service= getProgressService();
    //		if (service != null) {
    //			service.schedule(job, 0, true);
    //		} else {
    job.schedule();
    //		}

    return true;
  }

  public boolean isQueryRunning(ISearchQuery query) {
    SearchJobRecord sjr = (SearchJobRecord) fSearchJobs.get(query);
    return sjr != null && sjr.isRunning;
  }

  public IStatus runSearchInForeground(
      IRunnableContext context, final ISearchQuery query, Object view) {
    if (isQueryRunning(query)) {
      return Status.CANCEL_STATUS;
    }

    //		// prepare view
    //		if (view == null) {
    //			getSearchViewManager().activateSearchView(true);
    //		} else {
    //			getSearchViewManager().activateSearchView(view);
    //		}

    addQuery(query);

    SearchJobRecord sjr = new SearchJobRecord(query);
    fSearchJobs.put(query, sjr);
    //
    //		if (context == null)
    //			context= new ProgressMonitorDialog(null);

    return doRunSearchInForeground(sjr, context);
  }

  /**
   * Run a runnable. Convert all thrown exceptions to either InterruptedException or
   * InvocationTargetException
   */
  private static void runInCurrentThread(
      IRunnableWithProgress runnable, IProgressMonitor progressMonitor)
      throws InterruptedException, InvocationTargetException {
    try {
      if (runnable != null) {
        runnable.run(progressMonitor);
      }
    } catch (InvocationTargetException e) {
      throw e;
    } catch (InterruptedException e) {
      throw e;
    } catch (OperationCanceledException e) {
      throw new InterruptedException();
    } catch (ThreadDeath e) {
      // Make sure to propagate ThreadDeath, or threads will never fully
      // terminate
      throw e;
    } catch (RuntimeException e) {
      throw new InvocationTargetException(e);
    } catch (Error e) {
      throw new InvocationTargetException(e);
    }
  }

  private IStatus doRunSearchInForeground(final SearchJobRecord rec, IRunnableContext context) {
    try {
      runInCurrentThread(
          new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
              searchJobStarted(rec);
              try {
                IStatus status = rec.query.run(monitor);
                if (status.matches(IStatus.CANCEL)) {
                  throw new InterruptedException();
                }
                if (!status.isOK()) {
                  throw new InvocationTargetException(new CoreException(status));
                }
              } catch (OperationCanceledException e) {
                throw new InterruptedException();
              } finally {
                searchJobFinished(rec);
              }
            }
          },
          new NullProgressMonitor());
    } catch (InvocationTargetException e) {
      Throwable innerException = e.getTargetException();
      if (innerException instanceof CoreException) {
        return ((CoreException) innerException).getStatus();
      }
      return new Status(
          IStatus.ERROR,
          "SearchPlugin.getID()",
          0,
          "An unexpected exception occurred during search",
          innerException);
    } catch (InterruptedException e) {
      return Status.CANCEL_STATUS;
    }
    return Status.OK_STATUS;
  }

  public static void shutdown() {
    InternalSearchUI instance = fgInstance;
    if (instance != null) instance.doShutdown();
  }

  private void doShutdown() {
    Iterator jobRecs = fSearchJobs.values().iterator();
    while (jobRecs.hasNext()) {
      SearchJobRecord element = (SearchJobRecord) jobRecs.next();
      if (element.job != null) element.job.cancel();
    }
    //		fPositionTracker.dispose();

    //		fSearchViewManager.dispose(fSearchResultsManager);

  }

  public void cancelSearch(ISearchQuery job) {
    SearchJobRecord rec = (SearchJobRecord) fSearchJobs.get(job);
    if (rec != null && rec.job != null) rec.job.cancel();
  }

  public QueryManager getSearchManager() {
    return fSearchResultsManager;
  }

  //	public SearchViewManager getSearchViewManager() {
  //		return fSearchViewManager;
  //	}
  //
  //	public PositionTracker getPositionTracker() {
  //		return fPositionTracker;
  //	}

  public void addQueryListener(IQueryListener l) {
    getSearchManager().addQueryListener(l);
  }

  public ISearchQuery[] getQueries() {
    return getSearchManager().getQueries();
  }

  public void removeQueryListener(IQueryListener l) {
    getSearchManager().removeQueryListener(l);
  }

  public void removeQuery(ISearchQuery query) {
    if (query == null) {
      throw new IllegalArgumentException();
    }
    cancelSearch(query);
    getSearchManager().removeQuery(query);
    fSearchJobs.remove(query);
  }

  public void addQuery(ISearchQuery query) {
    if (query == null) {
      throw new IllegalArgumentException();
    }
    establishHistoryLimit();
    getSearchManager().addQuery(query);
  }

  private void establishHistoryLimit() {
    //		int historyLimit= SearchPreferencePage.getHistoryLimit();
    //		QueryManager searchManager= getSearchManager();
    //		if (historyLimit >= searchManager.getSize()) {
    //			return;
    //		}
    //		int numberQueriesNotShown= 0;
    //		SearchViewManager searchViewManager= getSearchViewManager();
    //		ISearchQuery[] queries= searchManager.getQueries();
    //		for (int i= 0; i < queries.length; i++) {
    //			ISearchQuery query= queries[i];
    //			if (!searchViewManager.isShown(query)) {
    //				if (++numberQueriesNotShown >= historyLimit) {
    //					removeQuery(query);
    //				}
    //			}
    //		}
  }

  public void removeAllQueries() {
    for (Iterator queries = fSearchJobs.keySet().iterator(); queries.hasNext(); ) {
      ISearchQuery query = (ISearchQuery) queries.next();
      cancelSearch(query);
    }
    fSearchJobs.clear();
    getSearchManager().removeAll();
  }

  //	public void showSearchResult(SearchView searchView, ISearchResult result, boolean openInNew) {
  //		if (openInNew)
  //			searchView= (SearchView)getSearchViewManager().activateSearchView(true, openInNew);
  //		showSearchResult(searchView, result);
  //	}
  //
  //	private void showSearchResult(SearchView searchView, ISearchResult result) {
  //		getSearchManager().touch(result.getQuery());
  //		searchView.showSearchResult(result);
  //	}

}

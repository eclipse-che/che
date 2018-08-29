/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * An operation which potentially makes changes to the workspace. All resource modification should
 * be performed using this operation. The primary consequence of using this operation is that events
 * which typically occur as a result of workspace changes (such as the firing of resource deltas,
 * performance of autobuilds, etc.) are generally deferred until the outermost operation has
 * successfully completed. The platform may still decide to broadcast periodic resource change
 * notifications during the scope of the operation if the operation runs for a long time or another
 * thread modifies the workspace concurrently.
 *
 * <p>If a scheduling rule is provided, the operation will obtain that scheduling rule for the
 * duration of its <code>execute</code> method. If no scheduling rule is provided, the operation
 * will obtain a scheduling rule that locks the entire workspace for the duration of the operation.
 *
 * <p>Subclasses must implement <code>execute</code> to do the work of the operation.
 *
 * @see ISchedulingRule
 * @see org.eclipse.core.resources.IWorkspace#run(IWorkspaceRunnable, IProgressMonitor)
 */
public abstract
class WorkspaceModifyOperation /*implements IRunnableWithProgress, IThreadListener*/ {
  private ISchedulingRule rule;

  /** Creates a new operation. */
  protected WorkspaceModifyOperation() {
    this(ResourcesPlugin.getWorkspace().getRoot());
  }

  /**
   * Creates a new operation that will run using the provided scheduling rule.
   *
   * @param rule The ISchedulingRule to use or <code>null</code>.
   * @since 3.0
   */
  protected WorkspaceModifyOperation(ISchedulingRule rule) {
    this.rule = rule;
  }

  /**
   * Performs the steps that are to be treated as a single logical workspace change.
   *
   * <p>Subclasses must implement this method.
   *
   * @param monitor the progress monitor to use to display progress and field user requests to
   *     cancel
   * @exception CoreException if the operation fails due to a CoreException
   * @exception InvocationTargetException if the operation fails due to an exception other than
   *     CoreException
   * @exception InterruptedException if the operation detects a request to cancel, using <code>
   *     IProgressMonitor.isCanceled()</code>, it should exit by throwing <code>InterruptedException
   *     </code>. It is also possible to throw <code>OperationCanceledException</code>, which gets
   *     mapped to <code>InterruptedException</code> by the <code>run</code> method.
   */
  protected abstract void execute(IProgressMonitor monitor)
      throws CoreException, InvocationTargetException, InterruptedException;

  /**
   * The <code>WorkspaceModifyOperation</code> implementation of this <code>IRunnableWithProgress
   * </code> method initiates a batch of changes by invoking the <code>execute</code> method as a
   * workspace runnable (<code>IWorkspaceRunnable</code>).
   */
  public final synchronized void run(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {
    final InvocationTargetException[] iteHolder = new InvocationTargetException[1];
    try {
      IWorkspaceRunnable workspaceRunnable =
          new IWorkspaceRunnable() {
            public void run(IProgressMonitor pm) throws CoreException {
              try {
                execute(pm);
              } catch (InvocationTargetException e) {
                // Pass it outside the workspace runnable
                iteHolder[0] = e;
              } catch (InterruptedException e) {
                // Re-throw as OperationCanceledException, which will be
                // caught and re-thrown as InterruptedException below.
                throw new OperationCanceledException(e.getMessage());
              }
              // CoreException and OperationCanceledException are propagated
            }
          };
      ResourcesPlugin.getWorkspace().run(workspaceRunnable, rule, IResource.NONE, monitor);
    } catch (CoreException e) {
      throw new InvocationTargetException(e);
    } catch (OperationCanceledException e) {
      throw new InterruptedException(e.getMessage());
    }
    // Re-throw the InvocationTargetException, if any occurred
    if (iteHolder[0] != null) {
      throw iteHolder[0];
    }
  }

  /* (non-Javadoc)
   * @see IThreadListener#threadChange(Thread);
   * @since 3.2
   */
  public void threadChange(Thread thread) {
    // we must make sure we aren't transferring control away from a thread that
    // already owns a scheduling rule because this is deadlock prone (bug 105491)
    if (rule == null) {
      return;
    }
    Job currentJob = Job.getJobManager().currentJob();
    if (currentJob == null) {
      return;
    }
    ISchedulingRule currentRule = currentJob.getRule();
    if (currentRule == null) {
      return;
    }
    throw new IllegalStateException(
        "Cannot fork a thread from a thread owning a rule"); // $NON-NLS-1$
  }

  /**
   * The scheduling rule. Should not be modified.
   *
   * @return the scheduling rule, or <code>null</code>.
   * @since 3.4
   */
  public ISchedulingRule getRule() {
    return rule;
  }
}

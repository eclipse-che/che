/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ltk.internal.core.refactoring.NotCancelableProgressMonitor;

/**
 * Operation that, when run, performs a {@link Change} object. The operation can be created in two
 * different ways: with a given change or with a {@link CreateChangeOperation}. If created the
 * second way the given create change operation will be used to create the actual change to perform.
 *
 * <p>If the change has been performed successfully (e.g. {@link #changeExecuted()} returns <code>
 * true</code>) then the operation has called {@link Change#dispose()} as well to clear-up internal
 * state in the change object. If it hasn't been executed the change, the change is still intact and
 * the client is responsible to dispose the change object.
 *
 * <p>If an undo change has been provided by the change to execute then the operation calls {@link
 * Change#initializeValidationData(IProgressMonitor)} to initialize the undo change's validation
 * data.
 *
 * <p>If an undo manager has been set via the method {@link #setUndoManager(IUndoManager, String)}
 * then the undo object, if any has been provided, will be pushed onto the manager's undo stack.
 *
 * <p>The operation should be executed via the run method offered by <code>IWorkspace</code> to
 * achieve proper delta batching.
 *
 * <p>Note: this class is not intended to be extended outside of the refactoring framework.
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PerformChangeOperation implements IWorkspaceRunnable {

  private Change fChange;
  private CreateChangeOperation fCreateChangeOperation;
  private RefactoringStatus fValidationStatus;

  private Change fUndoChange;
  private String fUndoName;
  private IUndoManager fUndoManager;

  private boolean fChangeExecuted;
  private boolean fChangeExecutionFailed;
  private ISchedulingRule fSchedulingRule;

  /**
   * Creates a new perform change operation instance for the given change.
   *
   * @param change the change to be applied to the workbench
   */
  public PerformChangeOperation(Change change) {
    Assert.isNotNull(change);
    fChange = change;
    fSchedulingRule = ResourcesPlugin.getWorkspace().getRoot();
  }

  /**
   * Creates a new <code>PerformChangeOperation</code> for the given {@link CreateChangeOperation}.
   * The create change operation is used to create the actual change to execute.
   *
   * @param op the <code>CreateChangeOperation</code> used to create the actual change object
   */
  public PerformChangeOperation(CreateChangeOperation op) {
    Assert.isNotNull(op);
    fCreateChangeOperation = op;
    fSchedulingRule = ResourcesPlugin.getWorkspace().getRoot();
  }

  /**
   * Returns <code>true</code> if the change execution failed.
   *
   * @return <code>true</code> if the change execution failed; <code>false</code> otherwise
   */
  public boolean changeExecutionFailed() {
    return fChangeExecutionFailed;
  }

  /**
   * Returns <code>true</code> if the change has been executed. Otherwise <code>
   * false</code> is returned.
   *
   * @return <code>true</code> if the change has been executed, otherwise <code>false</code>
   */
  public boolean changeExecuted() {
    return fChangeExecuted;
  }

  /**
   * Returns the status of the condition checking. Returns <code>null</code> if no condition
   * checking has been requested.
   *
   * @return the status of the condition checking
   */
  public RefactoringStatus getConditionCheckingStatus() {
    if (fCreateChangeOperation != null) return fCreateChangeOperation.getConditionCheckingStatus();
    return null;
  }

  /**
   * Returns the change used by this operation. This is either the change passed to the constructor
   * or the one create by the <code>CreateChangeOperation</code>. Method returns <code>null</code>
   * if the create operation did not create a corresponding change or hasn't been executed yet.
   *
   * @return the change used by this operation or <code>null</code> if no change has been created
   */
  public Change getChange() {
    return fChange;
  }

  /**
   * Returns the undo change of the change performed by this operation. Returns <code>null</code> if
   * the change hasn't been performed yet or if the change doesn't provide a undo.
   *
   * @return the undo change of the performed change or <code>null</code>
   */
  public Change getUndoChange() {
    return fUndoChange;
  }

  /**
   * Returns the refactoring status returned from the call <code>IChange#isValid()</code>. Returns
   * <code>null</code> if the change has not been executed.
   *
   * @return the change's validation status
   */
  public RefactoringStatus getValidationStatus() {
    return fValidationStatus;
  }

  /**
   * Sets the undo manager. If the executed change provides an undo change, then the undo change is
   * pushed onto this manager.
   *
   * @param manager the undo manager to use or <code>null</code> if no undo recording is desired
   * @param undoName the name used to present the undo change on the undo stack. Must be a
   *     human-readable string. Must not be <code>null</code> if manager is unequal <code>null
   *     </code>
   */
  public void setUndoManager(IUndoManager manager, String undoName) {
    if (manager != null) {
      Assert.isNotNull(undoName);
    }
    fUndoManager = manager;
    fUndoName = undoName;
  }

  /**
   * Sets the scheduling rule used to execute this operation. If not set then the workspace root is
   * used. The supplied Change must be able to be performed in the provided scheduling rule.
   *
   * @param rule the rule to use, or <code>null</code> to use no scheduling rule
   * @since 3.3
   */
  public void setSchedulingRule(ISchedulingRule rule) {
    fSchedulingRule = rule;
  }

  /** {@inheritDoc} */
  public void run(IProgressMonitor pm) throws CoreException {
    if (pm == null) pm = new NullProgressMonitor();
    try {
      fChangeExecuted = false;
      if (createChange()) {
        pm.beginTask("", 4); // $NON-NLS-1$
        pm.subTask(""); // $NON-NLS-1$
        fCreateChangeOperation.run(new SubProgressMonitor(pm, 3));
        // Check for cancellation before executing the change, since canceling
        // during change execution is not supported
        // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=187265 ):
        if (pm.isCanceled()) throw new OperationCanceledException();

        fChange = fCreateChangeOperation.getChange();
        if (fChange != null) {
          executeChange(new SubProgressMonitor(pm, 1));
        } else {
          pm.worked(1);
        }
      } else {
        executeChange(pm);
      }
    } finally {
      pm.done();
    }
  }

  /**
   * Actually executes the change.
   *
   * @param pm a progress monitor to report progress
   * @throws CoreException if an unexpected error occurs during change execution
   */
  protected void executeChange(IProgressMonitor pm) throws CoreException {
    fChangeExecuted = false;
    if (!fChange.isEnabled()) return;
    IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          public void run(IProgressMonitor monitor) throws CoreException {
            boolean undoInitialized = false;
            try {
              monitor.beginTask("", 10); // $NON-NLS-1$
              fValidationStatus = fChange.isValid(new SubProgressMonitor(monitor, 1));
              if (fValidationStatus.hasFatalError()) return;
              boolean aboutToPerformChangeCalled = false;
              try {
                if (fUndoManager != null) {
                  ResourcesPlugin.getWorkspace().checkpoint(false);
                  fUndoManager.aboutToPerformChange(fChange);
                  aboutToPerformChangeCalled = true;
                }
                fChangeExecutionFailed = true;
                fUndoChange = fChange.perform(new SubProgressMonitor(monitor, 9));
                fChangeExecutionFailed = false;
                fChangeExecuted = true;
              } finally {
                if (fUndoManager != null) {
                  ResourcesPlugin.getWorkspace().checkpoint(false);
                  if (aboutToPerformChangeCalled)
                    fUndoManager.changePerformed(fChange, !fChangeExecutionFailed);
                }
              }
              fChange.dispose();
              if (fUndoChange != null) {
                fUndoChange.initializeValidationData(
                    new NotCancelableProgressMonitor(new SubProgressMonitor(monitor, 1)));
                undoInitialized = true;
              }
              if (fUndoManager != null) {
                if (fUndoChange != null) {
                  fUndoManager.addUndo(fUndoName, fUndoChange);
                } else {
                  fUndoManager.flush();
                }
              }
            } catch (CoreException e) {
              if (fUndoManager != null) fUndoManager.flush();
              if (fUndoChange != null && undoInitialized) {
                Change ch = fUndoChange;
                fUndoChange = null;
                ch.dispose();
              }
              fUndoChange = null;
              throw e;
            } catch (RuntimeException e) {
              if (fUndoManager != null) fUndoManager.flush();
              if (fUndoChange != null && undoInitialized) {
                Change ch = fUndoChange;
                fUndoChange = null;
                ch.dispose();
              }
              fUndoChange = null;
              throw e;
            } finally {
              monitor.done();
            }
          }
        };
    ResourcesPlugin.getWorkspace().run(runnable, fSchedulingRule, IWorkspace.AVOID_UPDATE, pm);
  }

  private boolean createChange() {
    return fCreateChangeOperation != null;
  }
}

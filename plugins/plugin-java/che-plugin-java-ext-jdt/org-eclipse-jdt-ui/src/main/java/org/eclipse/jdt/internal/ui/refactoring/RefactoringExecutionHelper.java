/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A helper class to execute a refactoring. The class takes care of pushing the undo change onto the
 * undo stack and folding editor edits into one editor undo object.
 */
public class RefactoringExecutionHelper {

  private final Refactoring fRefactoring;
  private final int fStopSeverity;
  private final int fSaveMode;

  private PerformChangeOperation fPerformChangeOperation;

  private class Operation implements IWorkspaceRunnable {
    public Change fChange;
    public PerformChangeOperation fPerformChangeOperation;
    public RefactoringStatus allConditions;
    private final boolean fForked;
    private final boolean fForkChangeExecution;

    public Operation(boolean forked, boolean forkChangeExecution) {
      fForked = forked;
      fForkChangeExecution = forkChangeExecution;
    }

    public void run(IProgressMonitor pm) throws CoreException {
      try {
        pm.beginTask("", fForked && !fForkChangeExecution ? 7 : 11); // $NON-NLS-1$
        pm.subTask(""); // $NON-NLS-1$

        final RefactoringStatus status =
            fRefactoring.checkAllConditions(
                new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
        if (status.getSeverity() >= fStopSeverity) {
          //					final boolean[] canceled = {false};
          //					if (fForked) {
          //						fParent.getDisplay().syncExec(new Runnable() {
          //							public void run() {
          //								canceled[0] = showStatusDialog(status);
          //							}
          //						});
          //					} else {
          //						canceled[0] = showStatusDialog(status);
          //					}
          //					if (canceled[0]) {
          allConditions = status;
          throw new OperationCanceledException();
          //					}
        }

        fChange =
            fRefactoring.createChange(
                new SubProgressMonitor(pm, 2, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
        fChange.initializeValidationData(
            new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

        fPerformChangeOperation =
            new PerformChangeOperation(
                fChange); // RefactoringUI.createUIAwareChangeOperation(fChange);
        fPerformChangeOperation.setUndoManager(
            RefactoringCore.getUndoManager(), fRefactoring.getName());
        if (fRefactoring instanceof IScheduledRefactoring)
          fPerformChangeOperation.setSchedulingRule(
              ((IScheduledRefactoring) fRefactoring).getSchedulingRule());

        if (!fForked || fForkChangeExecution)
          fPerformChangeOperation.run(
              new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      } finally {
        pm.done();
      }
    }

    //		/**
    //		 * @param status the status to show
    //		 * @return <code>true</code> iff the operation should be cancelled
    //		 */
    //		private boolean showStatusDialog(RefactoringStatus status) {
    //			Dialog dialog = RefactoringUI.createRefactoringStatusDialog(status, fParent,
    // fRefactoring.getName(), false);
    //			return dialog.open() == IDialogConstants.CANCEL_ID;
    //		}
  }

  /**
   * Creates a new refactoring execution helper.
   *
   * @param refactoring the refactoring
   * @param stopSeverity a refactoring status constant from {@link RefactoringStatus}
   * @param saveMode a save mode from {@link RefactoringSaveHelper}
   */
  public RefactoringExecutionHelper(Refactoring refactoring, int stopSeverity, int saveMode) {
    super();
    Assert.isNotNull(refactoring);
    //		Assert.isNotNull(parent);
    //		Assert.isNotNull(context);
    fRefactoring = refactoring;
    fStopSeverity = stopSeverity;
    //		fParent= parent;
    //		fExecContext= context;
    fSaveMode = saveMode;
  }

  /**
   * Must be called in the UI thread.
   *
   * @param fork if set, the operation will be forked
   * @param cancelable if set, the operation will be cancelable
   * @throws InterruptedException thrown when the operation is cancelled
   * @throws InvocationTargetException thrown when the operation failed to execute
   */
  public RefactoringStatus perform(boolean fork, boolean cancelable)
      throws InterruptedException, InvocationTargetException, CoreException {
    return perform(fork, false, cancelable);
  }

  /**
   * Must be called in the UI thread.<br>
   * <strong>Use {@link #perform(boolean, boolean)} unless you know exactly what you are
   * doing!</strong>
   *
   * @param fork if set, the operation will be forked
   * @param forkChangeExecution if the change should not be executed in the UI thread: This may not
   *     work in any case
   * @param cancelable if set, the operation will be cancelable
   * @throws InterruptedException thrown when the operation is cancelled
   * @throws InvocationTargetException thrown when the operation failed to execute
   */
  public RefactoringStatus perform(boolean fork, boolean forkChangeExecution, boolean cancelable)
      throws InterruptedException, InvocationTargetException, CoreException {
    //		Assert.isTrue(Display.getCurrent() != null);
    final IJobManager manager = Job.getJobManager();
    final ISchedulingRule rule;
    if (fRefactoring instanceof IScheduledRefactoring) {
      rule = ((IScheduledRefactoring) fRefactoring).getSchedulingRule();
    } else {
      rule = ResourcesPlugin.getWorkspace().getRoot();
    }
    Operation op = null;
    try {
      //			try {
      //				Runnable r= new Runnable() {
      //					public void run() {
      //						manager.beginRule(rule, null);
      //					}
      //				};
      ////				BusyIndicator.showWhile(fParent.getDisplay(), r);
      //			} catch (OperationCanceledException e) {
      //				throw new InterruptedException(e.getMessage());
      //			}

      //			RefactoringSaveHelper saveHelper= new RefactoringSaveHelper(fSaveMode);
      //			if (!saveHelper.saveEditors(fParent))
      //				throw new InterruptedException();

      try {
        op = new Operation(fork, forkChangeExecution);
        op.run(new NullProgressMonitor());
        fPerformChangeOperation = op.fPerformChangeOperation;
        //			fRefactoring.setValidationContext(fParent);
        if (op.fPerformChangeOperation != null) {
          ResourcesPlugin.getWorkspace().run(op.fPerformChangeOperation, new NullProgressMonitor());
        }
        //				if (fork && !forkChangeExecution && op.fPerformChangeOperation != null)
        //					fExecContext.run(false, false, new
        // WorkbenchRunnableAdapter(op.fPerformChangeOperation, rule, true));

        if (op.fPerformChangeOperation != null) {
          RefactoringStatus validationStatus = op.fPerformChangeOperation.getValidationStatus();
          if (validationStatus != null /*&& validationStatus.hasFatalError()*/) {
            //						MessageDialog.openError(fParent, fRefactoring.getName(),
            //								Messages.format(
            //										RefactoringMessages.RefactoringExecutionHelper_cannot_execute,
            //										validationStatus.getMessageMatchingSeverity(RefactoringStatus.FATAL)));
            //						throw new InterruptedException();
            return validationStatus;
          }
        }
      } catch (OperationCanceledException e) {
        if (op != null) {
          if (op.allConditions != null) {
            return op.allConditions;
          }
        }
        throw new InterruptedException(e.getMessage());
      } finally {
        //				saveHelper.triggerIncrementalBuild();
      }
    } finally {
      //			manager.endRule(rule);
      fRefactoring.setValidationContext(null);
    }
    return new RefactoringStatus();
  }

  public PerformChangeOperation getfPerformChangeOperation() {
    return fPerformChangeOperation;
  }
}

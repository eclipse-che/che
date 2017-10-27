/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class UndoableOperation2ChangeAdapter
    implements IUndoableOperation, IAdvancedUndoableOperation {

  private String fLabel;
  private String fDescription;
  private Change fExecuteChange;
  private Change fUndoChange;
  private Change fRedoChange;
  private Change fActiveChange;

  private ChangeDescriptor fChangeDescriptor;

  private List fContexts = new ArrayList();

  private static class ContextAdapter implements IAdaptable {
    private IAdaptable fInfoAdapter;
    private String fTitle;

    public ContextAdapter(IAdaptable infoAdapter, String title) {
      fInfoAdapter = infoAdapter;
      fTitle = title;
    }

    public Object getAdapter(Class adapter) {
      if (String.class.equals(adapter)) return fTitle;
      return fInfoAdapter.getAdapter(adapter);
    }
  }

  private static class ExecuteResult {
    boolean changeExecuted;
    Change reverseChange;
    RefactoringStatus validationStatus;

    public ExecuteResult() {
      validationStatus = new RefactoringStatus();
    }
  }

  public UndoableOperation2ChangeAdapter(Change change) {
    fExecuteChange = change;
    fActiveChange = change;
    fChangeDescriptor =
        change.getDescriptor(); // remember it now: can't access anymore from executed change
  }

  public void setUndoChange(Change undoChange) {
    fUndoChange = undoChange;
    fActiveChange = fUndoChange;
    fExecuteChange = null;
    fRedoChange = null;
  }

  public Change getChange() {
    return fActiveChange;
  }

  public ChangeDescriptor getChangeDescriptor() {
    return fChangeDescriptor;
  }

  public void setChangeDescriptor(ChangeDescriptor descriptor) {
    fChangeDescriptor = descriptor;
  }

  public void setLabel(String label) {
    fLabel = label;
  }

  public String getLabel() {
    if (fLabel != null) return fLabel;
    return fActiveChange.getName();
  }

  public String getDescription() {
    if (fDescription != null) return fDescription;
    return fActiveChange.getName();
  }

  public Object[] getAffectedObjects() {
    if (fActiveChange == null) return null;
    return fActiveChange.getAffectedObjects();
  }

  public void addContext(IUndoContext context) {
    if (!fContexts.contains(context)) fContexts.add(context);
  }

  public boolean hasContext(IUndoContext context) {
    if (context == null) return false;
    for (int i = 0; i < fContexts.size(); i++) {
      IUndoContext otherContext = (IUndoContext) fContexts.get(i);
      // have to check both ways because one context may be more general in
      // its matching rules than another.
      if (context.matches(otherContext) || otherContext.matches(context)) return true;
    }
    return false;
  }

  public void removeContext(IUndoContext context) {
    fContexts.remove(context);
  }

  public IUndoContext[] getContexts() {
    return (IUndoContext[]) fContexts.toArray(new IUndoContext[fContexts.size()]);
  }

  public boolean canExecute() {
    return fExecuteChange != null;
  }

  public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      ExecuteResult result =
          executeChange(getQuery(info, RefactoringCoreMessages.Refactoring_execute_label), monitor);
      if (!result.changeExecuted) {
        return createStatus(result);
      }
      fUndoChange = result.reverseChange;
      fActiveChange = fUndoChange;
      fExecuteChange = null;
      return Status.OK_STATUS;
    } catch (CoreException e) {
      throw new ExecutionException(e.getStatus().getMessage(), e);
    }
  }

  public boolean canUndo() {
    return fUndoChange != null;
  }

  public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      ExecuteResult result =
          executeChange(getQuery(info, RefactoringCoreMessages.Refactoring_undo_label), monitor);
      if (!result.changeExecuted) {
        fUndoChange = null;
        fRedoChange = null;
        clearActiveChange();
        return createStatus(result);
      }
      fRedoChange = result.reverseChange;
      fActiveChange = fRedoChange;
      fUndoChange = null;
      return Status.OK_STATUS;
    } catch (CoreException e) {
      throw new ExecutionException(e.getStatus().getMessage(), e);
    }
  }

  public IStatus computeUndoableStatus(IProgressMonitor monitor) throws ExecutionException {
    if (fUndoChange == null)
      return new Status(
          IStatus.ERROR,
          RefactoringCorePlugin.getPluginId(),
          IStatus.ERROR,
          RefactoringCoreMessages.UndoableOperation2ChangeAdapter_no_undo_available,
          null);
    try {
      if (monitor == null) monitor = new NullProgressMonitor();
      RefactoringStatus status = fUndoChange.isValid(monitor);
      if (status.hasFatalError()) {
        // The operation can no longer be undo.
        fUndoChange = null;
        clearActiveChange();
        return asStatus(status);
      } else {
        // return OK in all other cases. This by passes the dialog shown
        // in the operation approver and allows refactoring to show its
        // own dialog again inside the runnable.
        return Status.OK_STATUS;
      }
    } catch (CoreException e) {
      throw new ExecutionException(e.getStatus().getMessage(), e);
    }
  }

  public boolean canRedo() {
    return fRedoChange != null;
  }

  public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
    if (monitor == null) monitor = new NullProgressMonitor();
    try {
      ExecuteResult result =
          executeChange(getQuery(info, RefactoringCoreMessages.Refactoring_redo_label), monitor);
      if (!result.changeExecuted) {
        fUndoChange = null;
        fRedoChange = null;
        clearActiveChange();
        return createStatus(result);
      }
      fUndoChange = result.reverseChange;
      fActiveChange = fUndoChange;
      fRedoChange = null;
      return Status.OK_STATUS;
    } catch (CoreException e) {
      throw new ExecutionException(e.getStatus().getMessage(), e);
    }
  }

  public IStatus computeRedoableStatus(IProgressMonitor monitor) throws ExecutionException {
    if (fRedoChange == null)
      return new Status(
          IStatus.ERROR,
          RefactoringCorePlugin.getPluginId(),
          IStatus.ERROR,
          RefactoringCoreMessages.UndoableOperation2ChangeAdapter_no_redo_available,
          null);
    try {
      if (monitor == null) monitor = new NullProgressMonitor();
      RefactoringStatus status = fRedoChange.isValid(monitor);
      if (status.hasFatalError()) {
        // The operation can no longer be redone.
        fRedoChange = null;
        clearActiveChange();
        return asStatus(status);
      } else {
        // return OK in all other cases. This by passes the dialog shown
        // in the operation approver and allows refactoring to show its
        // own dialog again inside the runnable.
        return Status.OK_STATUS;
      }
    } catch (CoreException e) {
      throw new ExecutionException(e.getStatus().getMessage(), e);
    }
  }

  public void aboutToNotify(OperationHistoryEvent event) {
    switch (event.getEventType()) {
      case OperationHistoryEvent.ABOUT_TO_EXECUTE:
      case OperationHistoryEvent.ABOUT_TO_UNDO:
      case OperationHistoryEvent.ABOUT_TO_REDO:
      case OperationHistoryEvent.DONE:
      case OperationHistoryEvent.UNDONE:
      case OperationHistoryEvent.REDONE:
      case OperationHistoryEvent.OPERATION_NOT_OK:
        ResourcesPlugin.getWorkspace().checkpoint(false);
        break;
    }
  }

  public void dispose() {
    // the active change could be cleared.
    if (fActiveChange != null) fActiveChange.dispose();
  }

  private ExecuteResult executeChange(final IValidationCheckResultQuery query, IProgressMonitor pm)
      throws CoreException {
    final ExecuteResult result = new ExecuteResult();
    if (fActiveChange == null || !fActiveChange.isEnabled()) return result;
    IWorkspaceRunnable runnable =
        new IWorkspaceRunnable() {
          public void run(IProgressMonitor monitor) throws CoreException {
            boolean reverseIsInitialized = false;
            try {
              monitor.beginTask("", 11); // $NON-NLS-1$
              result.validationStatus = fActiveChange.isValid(new SubProgressMonitor(monitor, 2));
              if (result.validationStatus.hasFatalError()) {
                query.stopped(result.validationStatus);
                // no need to dispose here. The framework disposes
                // the undo since it couldn't be executed.
                return;
              }
              if (!result.validationStatus.isOK() && !query.proceed(result.validationStatus)) {
                return;
              }
              try {
                result.reverseChange = fActiveChange.perform(new SubProgressMonitor(monitor, 9));
                result.changeExecuted = true;
              } finally {
                ResourcesPlugin.getWorkspace().checkpoint(false);
              }
              fActiveChange.dispose();
              if (result.reverseChange != null) {
                result.reverseChange.initializeValidationData(
                    new NotCancelableProgressMonitor(new SubProgressMonitor(monitor, 1)));
                reverseIsInitialized = true;
              }
            } catch (CoreException e) {
              Change ch = result.reverseChange;
              result.reverseChange = null;
              if (ch != null && reverseIsInitialized) {
                ch.dispose();
              }
              throw e;
            } catch (RuntimeException e) {
              Change ch = result.reverseChange;
              result.reverseChange = null;
              if (ch != null && reverseIsInitialized) {
                ch.dispose();
              }
              throw e;
            } finally {
              monitor.done();
            }
          }
        };
    ResourcesPlugin.getWorkspace().run(runnable, pm);
    return result;
  }

  private IStatus createStatus(ExecuteResult result) {
    if (!result.validationStatus.isOK()) {
      return result.validationStatus.getEntryWithHighestSeverity().toStatus();
    } else {
      return new Status(
          IStatus.ERROR,
          RefactoringCorePlugin.getPluginId(),
          IStatus.ERROR,
          RefactoringCoreMessages.UndoableOperation2ChangeAdapter_error_message,
          null);
    }
  }

  private IStatus asStatus(RefactoringStatus status) {
    if (status.isOK()) {
      return Status.OK_STATUS;
    } else {
      return status.getEntryWithHighestSeverity().toStatus();
    }
  }

  private IValidationCheckResultQuery getQuery(IAdaptable info, String title) {
    if (info == null) return RefactoringCore.getQueryFactory().create(null);
    IValidationCheckResultQuery result =
        (IValidationCheckResultQuery) info.getAdapter(IValidationCheckResultQuery.class);
    if (result != null) return result;
    ContextAdapter context = new ContextAdapter(info, title);
    return RefactoringCore.getQueryFactory().create(context);
  }

  private void clearActiveChange() {
    if (fLabel == null) {
      fLabel = fActiveChange.getName();
    }
    if (fDescription == null) {
      fDescription = fActiveChange.getName();
    }
    fActiveChange.dispose();
    fActiveChange = null;
  }
}

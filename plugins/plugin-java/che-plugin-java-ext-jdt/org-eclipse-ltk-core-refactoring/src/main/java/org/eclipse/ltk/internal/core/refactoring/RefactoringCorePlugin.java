/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Oakland Software (Francis
 * Upton) <francisu@ieee.org> - Fix for Bug 63149 [ltk] allow changes to be executed after the
 * 'main' change during an undo [refactoring]
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistorySerializer;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefactoringCorePlugin extends Plugin {
  private static final Logger LOG = LoggerFactory.getLogger(RefactoringCorePlugin.class);
  private static RefactoringCorePlugin fgDefault;
  private static IUndoManager fgUndoManager = null;

  private static IUndoContext fRefactoringUndoContext;

  private IRefactoringHistoryListener fRefactoringHistoryListener = null;

  public RefactoringCorePlugin() {
    fgDefault = this;
  }

  public static RefactoringCorePlugin getDefault() {
    return fgDefault;
  }

  public static String getPluginId() {
    return RefactoringCore.ID_PLUGIN;
  }

  public static IUndoContext getUndoContext() {
    if (fRefactoringUndoContext == null) {
      fRefactoringUndoContext = new RefactoringUndoContext();
      IUndoContext workspaceContext =
          (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
      if (workspaceContext instanceof ObjectUndoContext) {
        ((ObjectUndoContext) workspaceContext).addMatch(fRefactoringUndoContext);
      }
      IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
      operationHistory.setLimit(fRefactoringUndoContext, 5);
    }
    return fRefactoringUndoContext;
  }

  public static void log(IStatus status) {
    //        getDefault().getLog().log(status);
    LOG.error(status.getMessage(), status.getException());
  }

  public static void log(Throwable t) {
    IStatus status =
        new Status(
            IStatus.ERROR,
            getPluginId(),
            IRefactoringCoreStatusCodes.INTERNAL_ERROR,
            RefactoringCoreMessages.RefactoringCorePlugin_internal_error,
            t);
    log(status);
  }

  public static void logRemovedListener(Throwable t) {
    IStatus status =
        new Status(
            IStatus.ERROR,
            getPluginId(),
            IRefactoringCoreStatusCodes.INTERNAL_ERROR,
            RefactoringCoreMessages.RefactoringCorePlugin_listener_removed,
            t);
    log(status);
  }

  public static void logRemovedParticipant(ParticipantDescriptor descriptor, Throwable t) {
    IStatus status =
        new Status(
            IStatus.ERROR,
            getPluginId(),
            IRefactoringCoreStatusCodes.PARTICIPANT_DISABLED,
            Messages.format(
                RefactoringCoreMessages.RefactoringCorePlugin_participant_removed,
                descriptor.getId()),
            t);
    log(status);
  }

  public static void logErrorMessage(String message) {
    log(
        new Status(
            IStatus.ERROR,
            getPluginId(),
            IRefactoringCoreStatusCodes.INTERNAL_ERROR,
            message,
            null));
  }

  public static IUndoManager getUndoManager() {
    if (fgUndoManager == null) fgUndoManager = createUndoManager();
    return fgUndoManager;
  }

  public void start(BundleContext context) throws Exception {
    super.start(context);
    RefactoringContributionManager.getInstance().connect();
    final RefactoringHistoryService service = RefactoringHistoryService.getInstance();
    service.connect();
    fRefactoringHistoryListener = new RefactoringHistorySerializer();
    service.addHistoryListener(fRefactoringHistoryListener);
  }

  public void stop(BundleContext context) throws Exception {
    if (fRefactoringUndoContext != null) {
      IUndoContext workspaceContext =
          (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
      if (workspaceContext instanceof ObjectUndoContext) {
        ((ObjectUndoContext) workspaceContext).removeMatch(fRefactoringUndoContext);
      }
    }
    if (fgUndoManager != null) fgUndoManager.shutdown();
    final RefactoringHistoryService service = RefactoringHistoryService.getInstance();
    service.disconnect();
    if (fRefactoringHistoryListener != null)
      service.removeHistoryListener(fRefactoringHistoryListener);
    RefactoringContributionManager.getInstance().disconnect();
    super.stop(context);
  }

  /**
   * Creates a new empty undo manager.
   *
   * @return a new undo manager
   */
  private static IUndoManager createUndoManager() {
    return new UndoManager2();
  }
}

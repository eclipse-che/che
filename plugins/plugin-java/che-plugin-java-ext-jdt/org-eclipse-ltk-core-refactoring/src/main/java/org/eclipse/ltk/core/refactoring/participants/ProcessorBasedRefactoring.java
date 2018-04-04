/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Oakland Software (Francis
 * Upton) <francisu@ieee.org> - Fix for Bug 63149 [ltk] allow changes to be executed after the
 * 'main' change during an undo [refactoring]
 * *****************************************************************************
 */
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * An base implementation for refactorings that are split into one refactoring processor and 0..n
 * participants.
 *
 * <p>This class can be subclassed by clients wishing to provide a special refactoring which uses a
 * processor/participant architecture.
 *
 * <p>Since 3.4, this class is non abstract and can be instantiated. {@link #getProcessor()} will
 * return the processor passed in {@link #ProcessorBasedRefactoring(RefactoringProcessor)} or the
 * processor set by {@link #setProcessor(RefactoringProcessor)}.
 *
 * @since 3.0
 */
public class ProcessorBasedRefactoring extends Refactoring {

  private static final String PERF_CHECK_CONDITIONS =
      "org.eclipse.ltk.core.refactoring/perf/participants/checkConditions"; // $NON-NLS-1$
  private static final String PERF_CREATE_CHANGES =
      "org.eclipse.ltk.core.refactoring/perf/participants/createChanges"; // $NON-NLS-1$

  private RefactoringProcessor fProcessor;

  private List /*<RefactoringParticipant>*/ fParticipants;

  private List /*<RefactoringParticipant>*/ fPreChangeParticipants; // can be null

  private Map /*<Object, TextChange>*/ fTextChangeMap;

  private static final List /*<RefactoringParticipant>*/ EMPTY_PARTICIPANTS =
      Collections.EMPTY_LIST;

  private static class ProcessorChange extends CompositeChange {
    private Map /*<Change, RefactoringParticipant>*/ fParticipantMap;
    private List /*<RefactoringParticipant>*/ fPreChangeParticipants; // can be null

    public ProcessorChange(String name) {
      super(name);
      markAsSynthetic();
    }

    public void setParticipantMap(Map /*<Change, RefactoringParticipant>*/ map) {
      fParticipantMap = map;
    }

    public void setPreChangeParticipants(List /*<RefactoringParticipant>*/ list) {
      fPreChangeParticipants = list;
    }

    protected void internalHandleException(Change change, Throwable e) {
      if (e instanceof OperationCanceledException) return;

      RefactoringParticipant participant = (RefactoringParticipant) fParticipantMap.get(change);
      if (participant != null) {
        disableParticipant(participant, e);
      } else if (fPreChangeParticipants != null) {
        // The main refactoring, get rid of any participants with pre changes
        IStatus status =
            new Status(
                IStatus.ERROR,
                RefactoringCorePlugin.getPluginId(),
                IRefactoringCoreStatusCodes.REFACTORING_EXCEPTION_DISABLED_PARTICIPANTS,
                RefactoringCoreMessages.ProcessorBasedRefactoring_prechange_participants_removed,
                e);
        ResourcesPlugin.log(status);
        Iterator it = fPreChangeParticipants.iterator();
        while (it.hasNext()) {
          participant = (RefactoringParticipant) it.next();
          disableParticipant(participant, null);
        }
      }
    }

    protected boolean internalContinueOnCancel() {
      return true;
    }

    protected boolean internalProcessOnCancel(Change change) {
      RefactoringParticipant participant = (RefactoringParticipant) fParticipantMap.get(change);
      if (participant == null) return false;
      return participant.getDescriptor().processOnCancel();
    }
  }

  /**
   * Creates a new processor based refactoring. Clients must override {@link #getProcessor()} to
   * return a processor or set the processor with {@link #setProcessor(RefactoringProcessor)}.
   *
   * @deprecated use {@link #ProcessorBasedRefactoring(RefactoringProcessor)} instead
   */
  protected ProcessorBasedRefactoring() {}

  /**
   * Creates a new processor based refactoring.
   *
   * @param processor the refactoring's main processor
   * @since 3.4 public, was added in 3.1 as protected method
   */
  public ProcessorBasedRefactoring(RefactoringProcessor processor) {
    setProcessor(processor);
  }

  /**
   * Return the processor associated with this refactoring. The method must not return <code>null
   * </code>. Implementors can override this method to return the processor to be used by this
   * refactoring. Since 3.4, this method returns the processor passed in {@link
   * #ProcessorBasedRefactoring(RefactoringProcessor)} or by {@link
   * #setProcessor(RefactoringProcessor)}.
   *
   * @return the processor associated with this refactoring
   */
  public RefactoringProcessor getProcessor() {
    return fProcessor;
  }

  /**
   * Sets the processor associated with this refactoring. The processor must not be <code>null
   * </code>.
   *
   * @param processor the processor associated with this refactoring
   * @since 3.4
   */
  public void setProcessor(RefactoringProcessor processor) {
    processor.setRefactoring(this);
    fProcessor = processor;
  }

  /**
   * Checks whether the refactoring is applicable to the elements to be refactored or not.
   *
   * <p>This default implementation forwards the call to the refactoring processor.
   *
   * @return <code>true</code> if the refactoring is applicable to the elements; otherwise <code>
   *     false</code> is returned.
   * @throws CoreException if the test fails
   */
  public final boolean isApplicable() throws CoreException {
    return getProcessor().isApplicable();
  }

  /** {@inheritDoc} */
  public String getName() {
    return getProcessor().getProcessorName();
  }

  /** {@inheritDoc} */
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    if (pm == null) pm = new NullProgressMonitor();
    RefactoringStatus result = new RefactoringStatus();
    pm.beginTask("", 10); // $NON-NLS-1$
    pm.setTaskName(RefactoringCoreMessages.ProcessorBasedRefactoring_initial_conditions);

    result.merge(getProcessor().checkInitialConditions(new SubProgressMonitor(pm, 8)));
    if (result.hasFatalError()) {
      pm.done();
      return result;
    }
    pm.done();
    return result;
  }

  /** {@inheritDoc} */
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
    if (pm == null) pm = new NullProgressMonitor();
    RefactoringStatus result = new RefactoringStatus();
    CheckConditionsContext context = createCheckConditionsContext();

    pm.beginTask("", 9); // $NON-NLS-1$
    pm.setTaskName(RefactoringCoreMessages.ProcessorBasedRefactoring_final_conditions);

    result.merge(getProcessor().checkFinalConditions(new SubProgressMonitor(pm, 5), context));
    if (result.hasFatalError()) {
      pm.done();
      return result;
    }
    if (pm.isCanceled()) throw new OperationCanceledException();

    SharableParticipants sharableParticipants =
        new SharableParticipants(); // must not be shared when checkFinalConditions is called again
    RefactoringParticipant[] loadedParticipants =
        getProcessor().loadParticipants(result, sharableParticipants);
    if (loadedParticipants == null || loadedParticipants.length == 0) {
      fParticipants = EMPTY_PARTICIPANTS;
    } else {
      fParticipants = new ArrayList();
      for (int i = 0; i < loadedParticipants.length; i++) {
        fParticipants.add(loadedParticipants[i]);
      }
    }
    if (result.hasFatalError()) {
      pm.done();
      return result;
    }
    IProgressMonitor sm = new SubProgressMonitor(pm, 2);

    sm.beginTask("", fParticipants.size()); // $NON-NLS-1$
    for (Iterator iter = fParticipants.iterator(); iter.hasNext() && !result.hasFatalError(); ) {

      RefactoringParticipant participant = (RefactoringParticipant) iter.next();

      final PerformanceStats stats =
          PerformanceStats.getStats(
              PERF_CHECK_CONDITIONS, getName() + ", " + participant.getName()); // $NON-NLS-1$
      stats.startRun();

      try {
        result.merge(participant.checkConditions(new SubProgressMonitor(sm, 1), context));
      } catch (OperationCanceledException e) {
        throw e;
      } catch (RuntimeException e) {
        // remove the participant so that it will be ignored during change execution.
        RefactoringCorePlugin.log(e);
        result.merge(
            RefactoringStatus.createErrorStatus(
                Messages.format(
                    RefactoringCoreMessages
                        .ProcessorBasedRefactoring_check_condition_participant_failed,
                    participant.getName())));
        iter.remove();
      }

      stats.endRun();

      if (sm.isCanceled()) throw new OperationCanceledException();
    }
    sm.done();
    if (result.hasFatalError()) {
      pm.done();
      return result;
    }
    result.merge(context.check(new SubProgressMonitor(pm, 1)));
    pm.done();
    return result;
  }

  /** {@inheritDoc} */
  public Change createChange(IProgressMonitor pm) throws CoreException {
    if (pm == null) pm = new NullProgressMonitor();
    pm.beginTask("", fParticipants.size() + 3); // $NON-NLS-1$
    pm.setTaskName(RefactoringCoreMessages.ProcessorBasedRefactoring_create_change);
    Change processorChange = getProcessor().createChange(new SubProgressMonitor(pm, 1));
    if (pm.isCanceled()) throw new OperationCanceledException();

    fTextChangeMap = new HashMap();
    addToTextChangeMap(processorChange);

    List /*<Change>*/ changes = new ArrayList();
    List /*<Change>*/ preChanges = new ArrayList();
    Map /*<Change, RefactoringParticipant>*/ participantMap = new HashMap();
    for (Iterator iter = fParticipants.iterator(); iter.hasNext(); ) {
      final RefactoringParticipant participant = (RefactoringParticipant) iter.next();

      try {
        final PerformanceStats stats =
            PerformanceStats.getStats(
                PERF_CREATE_CHANGES, getName() + ", " + participant.getName()); // $NON-NLS-1$
        stats.startRun();

        Change preChange = participant.createPreChange(new SubProgressMonitor(pm, 1));
        Change change = participant.createChange(new SubProgressMonitor(pm, 1));

        stats.endRun();

        if (preChange != null) {
          if (fPreChangeParticipants == null) fPreChangeParticipants = new ArrayList();
          fPreChangeParticipants.add(participant);
          preChanges.add(preChange);
          participantMap.put(preChange, participant);
          addToTextChangeMap(preChange);
        }

        if (change != null) {
          changes.add(change);
          participantMap.put(change, participant);
          addToTextChangeMap(change);
        }

      } catch (CoreException e) {
        disableParticipant(participant, e);
        throw e;
      } catch (OperationCanceledException e) {
        throw e;
      } catch (RuntimeException e) {
        disableParticipant(participant, e);
        throw e;
      }
      if (pm.isCanceled()) throw new OperationCanceledException();
    }

    fTextChangeMap = null;

    Change postChange =
        getProcessor()
            .postCreateChange(
                (Change[]) changes.toArray(new Change[changes.size()]),
                new SubProgressMonitor(pm, 1));

    ProcessorChange result = new ProcessorChange(getName());
    result.addAll((Change[]) preChanges.toArray(new Change[preChanges.size()]));
    result.add(processorChange);
    result.addAll((Change[]) changes.toArray(new Change[changes.size()]));
    result.setParticipantMap(participantMap);
    result.setPreChangeParticipants(fPreChangeParticipants);
    if (postChange != null) result.add(postChange);
    return result;
  }

  /**
   * Returns the text change for the given element or <code>null</code> if a text change doesn't
   * exist. This method only returns a valid result during change creation. Outside of change
   * creation always <code>null</code> is returned.
   *
   * @param element the element to be modified for which a text change is requested
   * @return the text change or <code>null</code> if no text change exists for the element
   * @since 3.1
   */
  public TextChange getTextChange(Object element) {
    if (fTextChangeMap == null) return null;
    return (TextChange) fTextChangeMap.get(element);
  }

  /**
   * Adapts the refactoring to the given type. The adapter is resolved as follows:
   *
   * <ol>
   *   <li>the refactoring itself is checked whether it is an instance of the requested type.
   *   <li>its processor is checked whether it is an instance of the requested type.
   *   <li>the request is delegated to the super class.
   * </ol>
   *
   * @param clazz the adapter class to look up
   * @return the requested adapter or <code>null</code>if no adapter exists.
   */
  public Object getAdapter(Class clazz) {
    if (clazz.isInstance(this)) return this;
    if (clazz.isInstance(getProcessor())) return getProcessor();
    return super.getAdapter(clazz);
  }

  /* non java-doc
   * for debugging only
   */
  public String toString() {
    return getName();
  }

  // ---- Helper methods ---------------------------------------------------------------------

  private CheckConditionsContext createCheckConditionsContext() throws CoreException {
    CheckConditionsContext result = new CheckConditionsContext();
    result.add(new ValidateEditChecker(getValidationContext()));
    result.add(new ResourceChangeChecker());
    return result;
  }

  private static void disableParticipant(final RefactoringParticipant participant, Throwable e) {
    ParticipantDescriptor descriptor = participant.getDescriptor();
    descriptor.disable();
    RefactoringCorePlugin.logRemovedParticipant(descriptor, e);
  }

  private void addToTextChangeMap(Change change) {
    if (change instanceof TextChange) {
      Object element = ((TextChange) change).getModifiedElement();
      if (element != null) {
        fTextChangeMap.put(element, change);
      }
      // check if we have a subclass of TextFileChange. If so also put the change
      // under the file resource into the hash table if possible.
      if (change instanceof TextFileChange && !change.getClass().equals(TextFileChange.class)) {
        IFile file = ((TextFileChange) change).getFile();
        fTextChangeMap.put(file, change);
      }
    } else if (change instanceof CompositeChange) {
      Change[] children = ((CompositeChange) change).getChildren();
      for (int i = 0; i < children.length; i++) {
        addToTextChangeMap(children[i]);
      }
    }
  }
}

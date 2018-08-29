/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ltk.core.refactoring.participants.CheRefactoringParticipantsRegistry;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.ParticipantDescriptor;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * A {@link ParticipantExtensionPoint} is used to manage contributions of participants.
 *
 * <p>This class is not intended to be subclassed by clients.
 *
 * @since 3.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ParticipantExtensionPoint {

  private String fParticipantID;
  private List fParticipants;
  private Class fParticipantClass;
  private String fPluginId;

  /**
   * Creates a {@link ParticipantExtensionPoint}.
   *
   * @param pluginId the ID of the plug-in defining the extension point
   * @param participantId the name of the extension point
   * @param clazz the type of the class that contributors must provide
   */
  public ParticipantExtensionPoint(String pluginId, String participantId, Class clazz) {
    Assert.isNotNull(participantId);
    fParticipantID = participantId;
    fParticipantClass = clazz;
    fPluginId = pluginId;
  }

  /**
   * Returns all participants for a given element.
   *
   * @param status a refactoring status to report status if problems occurred while loading the
   *     participants
   * @param processor the processor that will own the participants
   * @param element the element to be copied or a corresponding descriptor
   * @param arguments the arguments for the participants
   * @param filter a participant filter to exclude certain participants, or <code>null</code> if no
   *     filtering is desired
   * @param affectedNatures an array of project natures affected by the refactoring
   * @param shared a list of shared participants
   * @return an array of participants
   */
  public RefactoringParticipant[] getParticipants(
      RefactoringStatus status,
      RefactoringProcessor processor,
      Object element,
      RefactoringArguments arguments,
      IParticipantDescriptorFilter filter,
      String[] affectedNatures,
      SharableParticipants shared) {
    if (fParticipants == null) init();

    EvaluationContext evalContext = createEvaluationContext(processor, element, affectedNatures);
    List result = new ArrayList();
    for (Iterator iter = fParticipants.iterator(); iter.hasNext(); ) {
      ParticipantDescriptor descriptor = (ParticipantDescriptor) iter.next();
      if (!descriptor.isEnabled()) {
        iter.remove();
      } else {
        try {
          RefactoringStatus filterStatus = new RefactoringStatus();
          if (descriptor.matches(evalContext, filter, filterStatus)) {
            RefactoringParticipant participant = shared.get(descriptor);
            if (participant != null) {
              ((ISharableParticipant) participant).addElement(element, arguments);
            } else {
              participant = descriptor.createParticipant();
              if (fParticipantClass.isInstance(participant)) {
                if (participant.initialize(processor, element, arguments)) {
                  participant.setDescriptor(descriptor);
                  result.add(participant);
                  if (participant instanceof ISharableParticipant)
                    shared.put(descriptor, participant);
                }
              } else {
                status.addError(
                    Messages.format(
                        RefactoringCoreMessages.ParticipantExtensionPoint_participant_removed,
                        descriptor.getName()));
                RefactoringCorePlugin.logErrorMessage(
                    Messages.format(
                        RefactoringCoreMessages.ParticipantExtensionPoint_wrong_type,
                        new String[] {descriptor.getName(), fParticipantClass.getName()}));
                iter.remove();
              }
            }
          } else {
            status.merge(filterStatus);
          }
        } catch (CoreException e) {
          logMalfunctioningParticipant(status, descriptor, e);
          iter.remove();
        } catch (RuntimeException e) {
          logMalfunctioningParticipant(status, descriptor, e);
          iter.remove();
        }
      }
    }

    return (RefactoringParticipant[]) result.toArray(new RefactoringParticipant[result.size()]);
  }

  private void logMalfunctioningParticipant(
      RefactoringStatus status, ParticipantDescriptor descriptor, Throwable e) {
    status.addError(
        Messages.format(
            RefactoringCoreMessages.ParticipantExtensionPoint_participant_removed,
            descriptor.getName()));
    RefactoringCorePlugin.logRemovedParticipant(descriptor, e);
  }

  private void init() {
    //		IExtensionRegistry registry= Platform.getExtensionRegistry();
    //		IConfigurationElement[] ces= new
    // IConfigurationElement[0];//registry.getConfigurationElementsFor(fPluginId, fParticipantID);
    Set<Class<? extends RefactoringParticipant>> ces =
        CheRefactoringParticipantsRegistry.getParticipantsFor(fParticipantID);
    fParticipants = new ArrayList(ces.size());
    for (Class<? extends RefactoringParticipant> clazz : ces) {
      ParticipantDescriptor descriptor = new ParticipantDescriptor(clazz);
      IStatus status = descriptor.checkSyntax();
      switch (status.getSeverity()) {
        case IStatus.ERROR:
          RefactoringCorePlugin.log(status);
          break;
        case IStatus.WARNING:
        case IStatus.INFO:
          RefactoringCorePlugin.log(status);
          fParticipants.add(descriptor);
          break;
        default:
          fParticipants.add(descriptor);
      }
    }
  }

  // ---- Helper methods ------------------------------------------------------------------

  private static EvaluationContext createEvaluationContext(
      RefactoringProcessor processor, Object element, String[] affectedNatures) {
    EvaluationContext result = new EvaluationContext(null, element);
    result.setAllowPluginActivation(true);
    result.addVariable("element", element); // $NON-NLS-1$
    result.addVariable("affectedNatures", Arrays.asList(affectedNatures)); // $NON-NLS-1$
    result.addVariable("processorIdentifier", processor.getIdentifier()); // $NON-NLS-1$
    return result;
  }
}

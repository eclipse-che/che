/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.refactoring.participants;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IParticipantDescriptorFilter;
import org.eclipse.ltk.core.refactoring.participants.ParticipantExtensionPoint;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

/**
 * Facade to access participants to the participant extension points
 * provided by the org.eclipse.jdt.core.manipulation plug-in.
 * <p>
 * Note: this class is not intended to be extended or instantiated by clients.
 * </p>
 *
 * @since 1.2
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JavaParticipantManager {

	private final static String PLUGIN_ID= "org.eclipse.jdt.core.manipulation"; //$NON-NLS-1$

	private JavaParticipantManager() {
		// no instance
	}

	//---- Change method signature participants ----------------------------------------------------------------

	private static final String METHOD_SIGNATURE_PARTICIPANT_EXT_POINT= "changeMethodSignatureParticipants"; //$NON-NLS-1$

	private static ParticipantExtensionPoint fgMethodSignatureInstance =
			new ParticipantExtensionPoint(PLUGIN_ID, METHOD_SIGNATURE_PARTICIPANT_EXT_POINT, ChangeMethodSignatureParticipant.class);

	/**
	 * Loads the change method signature participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param method the method to be changed
	 * @param arguments the change method signature arguments describing the change
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of change method signature participants
	 */
	public static ChangeMethodSignatureParticipant[] loadChangeMethodSignatureParticipants(RefactoringStatus status,
																						   RefactoringProcessor processor, IMethod method,
																						   ChangeMethodSignatureArguments arguments,
																						   IParticipantDescriptorFilter filter,
																						   String[] affectedNatures,
																						   SharableParticipants shared) {
		RefactoringParticipant[] participants =
				fgMethodSignatureInstance.getParticipants(status, processor, method, arguments, filter, affectedNatures, shared);
		ChangeMethodSignatureParticipant[] result = new ChangeMethodSignatureParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

}
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Facade to access the rename, move, delete, create and copy participant
 * extension point provided by the org.eclipse.ltk.core.refactoring plug-in.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ParticipantManager {

	private ParticipantManager() {
		// no instance
	}

	//---- Rename participants ----------------------------------------------------------------

	private static final String RENAME_PARTICIPANT_EXT_POINT= "renameParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgRenameInstance=
		new ParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), RENAME_PARTICIPANT_EXT_POINT, RenameParticipant.class);

	/**
	 * Loads the rename participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be renamed
	 * @param arguments the rename arguments describing the rename
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of rename participants
	 */
	public static RenameParticipant[] loadRenameParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, RenameArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		return loadRenameParticipants(status, processor, element, arguments, null, affectedNatures, shared);
	}

	/**
	 * Loads the rename participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be renamed
	 * @param arguments the rename arguments describing the rename
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of rename participants
	 *
	 * @since 3.2
	 */
	public static RenameParticipant[] loadRenameParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, RenameArguments arguments, IParticipantDescriptorFilter filter, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgRenameInstance.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		RenameParticipant[] result= new RenameParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Move participants ----------------------------------------------------------------

	private static final String MOVE_PARTICIPANT_EXT_POINT= "moveParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgMoveExtensions=
		new ParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), MOVE_PARTICIPANT_EXT_POINT, MoveParticipant.class);

	/**
	 * Loads the move participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be moved
	 * @param arguments the move arguments describing the move
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of move participants
	 */
	public static MoveParticipant[] loadMoveParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, MoveArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		return loadMoveParticipants(status, processor, element, arguments, null, affectedNatures, shared);
	}

	/**
	 * Loads the move participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be moved
	 * @param arguments the move arguments describing the move
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of move participants
	 *
	 * @since 3.2
	 */
	public static MoveParticipant[] loadMoveParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, MoveArguments arguments, IParticipantDescriptorFilter filter, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgMoveExtensions.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		MoveParticipant[] result= new MoveParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Delete participants ----------------------------------------------------------------

	private static final String DELETE_PARTICIPANT_EXT_POINT= "deleteParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgDeleteInstance=
		new ParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), DELETE_PARTICIPANT_EXT_POINT, DeleteParticipant.class);

	/**
	 * Loads the delete participants for the given element.
	 * @param status a refactoring status to report status if problems occurred while
     *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be deleted
	 * @param arguments the delete arguments describing the delete
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of delete participants
	 */
	public static DeleteParticipant[] loadDeleteParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, DeleteArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		return loadDeleteParticipants(status, processor, element, arguments, null, affectedNatures, shared);
	}

	/**
	 * Loads the delete participants for the given element.
	 * @param status a refactoring status to report status if problems occurred while
     *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be deleted
	 * @param arguments the delete arguments describing the delete
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of delete participants
	 *
	 * @since 3.2
	 */
	public static DeleteParticipant[] loadDeleteParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, DeleteArguments arguments, IParticipantDescriptorFilter filter, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgDeleteInstance.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		DeleteParticipant[] result= new DeleteParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Create participants ----------------------------------------------------------------

	private static final String CREATE_PARTICIPANT_EXT_POINT= "createParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCreateInstance=
		new ParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), CREATE_PARTICIPANT_EXT_POINT, CreateParticipant.class);

	/**
	 * Loads the create participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be created or a corresponding descriptor
	 * @param arguments the create arguments describing the create
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of create participants
	 */
	public static CreateParticipant[] loadCreateParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CreateArguments arguments, String affectedNatures[], SharableParticipants shared) {
		return loadCreateParticipants(status, processor, element, arguments, null, affectedNatures, shared);
	}

	/**
	 * Loads the create participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be created or a corresponding descriptor
	 * @param arguments the create arguments describing the create
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of create participants
	 *
	 * @since 3.2
	 */
	public static CreateParticipant[] loadCreateParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CreateArguments arguments, IParticipantDescriptorFilter filter, String affectedNatures[], SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCreateInstance.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		CreateParticipant[] result= new CreateParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Copy participants ----------------------------------------------------------------

	private static final String COPY_PARTICIPANT_EXT_POINT= "copyParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCopyInstance=
		new ParticipantExtensionPoint(RefactoringCorePlugin.getPluginId(), COPY_PARTICIPANT_EXT_POINT, CopyParticipant.class);

	/**
	 * Loads the copy participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be copied or a corresponding descriptor
	 * @param arguments the copy arguments describing the copy operation
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of copy participants
	 *
	 * @since 3.1
	 */
	public static CopyParticipant[] loadCopyParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CopyArguments arguments, String affectedNatures[], SharableParticipants shared) {
		return loadCopyParticipants(status, processor, element, arguments, null, affectedNatures, shared);
	}

	/**
	 * Loads the copy participants for the given element.
	 *
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be copied or a corresponding descriptor
	 * @param arguments the copy arguments describing the copy operation
	 * @param filter a participant filter to exclude certain participants, or <code>null</code>
	 *  if no filtering is desired
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 *
	 * @return an array of copy participants
	 *
	 * @since 3.2
	 */
	public static CopyParticipant[] loadCopyParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CopyArguments arguments, IParticipantDescriptorFilter filter, String affectedNatures[], SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCopyInstance.getParticipants(status, processor, element, arguments, filter, affectedNatures, shared);
		CopyParticipant[] result= new CopyParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
}
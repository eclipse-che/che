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
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.ltk.core.refactoring.participants.CreateArguments;
import org.eclipse.ltk.core.refactoring.participants.CreateParticipant;
import org.eclipse.ltk.core.refactoring.participants.DeleteArguments;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

/**
 * A data structure to collect resource modifications.
 *
 * @since 3.4
 */
public class ResourceModifications {

	private List/*<IResource>*/ fCreate;
	private List/*<IResource>*/ fDelete;

	private List/*<IResource>*/ fMove;
	private List/*<MoveArguments>*/ fMoveArguments;

	private List/*<IResource>*/ fRename;
	private List/*<RenameArguments>*/ fRenameArguments;

	private List/*<IResource>*/ fCopy;
	private List/*<CopyArguments>*/ fCopyArguments;

	private int fIgnoreCount;
	private List/*<DeltaDescription>*/ fDeltaDescriptions;

	public static abstract class DeltaDescription {
		protected IResource fResource;
		public DeltaDescription(IResource resource) {
			fResource= resource;
		}
		public abstract void buildDelta(IResourceChangeDescriptionFactory builder);
		public abstract IPath getDestinationPath();

	}
	public static class DeleteDescription extends DeltaDescription {
		public DeleteDescription(IResource resource) {
			super(resource);
		}
		public void buildDelta(IResourceChangeDescriptionFactory builder) {
			builder.delete(fResource);
		}
		public IPath getDestinationPath() {
			return null;
		}
	}
	public static class ChangedDescription extends DeltaDescription {
		public ChangedDescription(IFile resource) {
			super(resource);
		}
		public void buildDelta(IResourceChangeDescriptionFactory builder) {
			builder.change((IFile)fResource);
		}
		public IPath getDestinationPath() {
			return null;
		}
	}
	public static class CreateDescription extends DeltaDescription {
		public CreateDescription(IResource resource) {
			super(resource);
		}
		public void buildDelta(IResourceChangeDescriptionFactory builder) {
			builder.create(fResource);
		}
		public IPath getDestinationPath() {
			return fResource.getFullPath();
		}
	}
	public static class MoveDescription extends DeltaDescription {
		private IPath fDestination;
		public MoveDescription(IResource resource, IPath destination) {
			super(resource);
			fDestination= destination;
		}
		public void buildDelta(IResourceChangeDescriptionFactory builder) {
			builder.move(fResource, fDestination);
		}
		public IPath getDestinationPath() {
			return fDestination;
		}
	}
	public static class CopyDescription extends DeltaDescription {
		private IPath fDestination;
		public CopyDescription(IResource resource, IPath destination) {
			super(resource);
			fDestination= destination;
		}
		public void buildDelta(IResourceChangeDescriptionFactory builder) {
			builder.copy(fResource, fDestination);
		}
		public IPath getDestinationPath() {
			return fDestination;
		}
	}

	/**
	 * Adds the given file to the list of changed files.
	 *
	 * @param file the changed file
	 */
	public void addChanged(IFile file) {
		if (fIgnoreCount == 0) {
			internalAdd(new ChangedDescription(file));
		}
	}

	/**
	 * Adds the given resource to the list of resources
	 * to be created.
	 *
	 * @param create the resource to be add to the list of
	 *  resources to be created
	 */
	public void addCreate(IResource create) {
		if (fCreate == null)
			fCreate= new ArrayList(2);
		fCreate.add(create);
		if (fIgnoreCount == 0) {
			internalAdd(new CreateDescription(create));
		}
	}

	/**
	 * Adds the given resource to the list of resources
	 * to be deleted.
	 *
	 * @param delete the resource to be deleted
	 */
	public void addDelete(IResource delete) {
		if (fDelete == null)
			fDelete= new ArrayList(2);
		fDelete.add(delete);
		if (fIgnoreCount == 0) {
			internalAdd(new DeleteDescription(delete));
		}
	}

	/**
	 * Adds the given resource to the list of resources
	 * to be moved.
	 *
	 * @param move the resource to be moved
	 * @param arguments the move arguments
	 */
	public void addMove(IResource move, MoveArguments arguments) {
		if (fMove == null) {
			fMove= new ArrayList(2);
			fMoveArguments= new ArrayList(2);
		}
		fMove.add(move);
		fMoveArguments.add(arguments);
		if (fIgnoreCount == 0) {
			IPath destination= ((IResource)arguments.getDestination()).getFullPath().append(move.getName());
			internalAdd(new MoveDescription(move, destination));
		}
	}

	/**
	 * Adds the given resource to the list of resources
	 * to be copied.
	 *
	 * @param copy the resource to be copied
	 * @param arguments the copy arguments
	 */
	public void addCopy(IResource copy, CopyArguments arguments) {
		if (fCopy == null) {
			fCopy= new ArrayList(2);
			fCopyArguments= new ArrayList(2);
		}
		fCopy.add(copy);
		fCopyArguments.add(arguments);
		addCopyDelta(copy, arguments);
	}

	/**
	 * Adds the given resource to the list of renamed
	 * resources.
	 *
	 * @param rename the resource to be renamed
	 * @param arguments the arguments of the rename
	 */
	public void addRename(IResource rename, RenameArguments arguments) {
		Assert.isNotNull(rename);
		Assert.isNotNull(arguments);
		if (fRename == null) {
			fRename= new ArrayList(2);
			fRenameArguments= new ArrayList(2);
		}
		fRename.add(rename);
		fRenameArguments.add(arguments);
		if (fIgnoreCount == 0) {
			IPath newPath= rename.getFullPath().removeLastSegments(1).append(arguments.getNewName());
			internalAdd(new MoveDescription(rename, newPath));
		}
	}

	public RefactoringParticipant[] getParticipants(RefactoringStatus status, RefactoringProcessor processor, String[] natures, SharableParticipants shared) {
		List result= new ArrayList(5);
		if (fDelete != null) {
			DeleteArguments arguments= new DeleteArguments();
			for (Iterator iter= fDelete.iterator(); iter.hasNext();) {
				DeleteParticipant[] deletes= ParticipantManager.loadDeleteParticipants(status,
					processor, iter.next(),
					arguments, natures, shared);
				result.addAll(Arrays.asList(deletes));
			}
		}
		if (fCreate != null) {
			CreateArguments arguments= new CreateArguments();
			for (Iterator iter= fCreate.iterator(); iter.hasNext();) {
				CreateParticipant[] creates= ParticipantManager.loadCreateParticipants(status,
					processor, iter.next(),
					arguments, natures, shared);
				result.addAll(Arrays.asList(creates));
			}
		}
		if (fMove != null) {
			for (int i= 0; i < fMove.size(); i++) {
				Object element= fMove.get(i);
				MoveArguments arguments= (MoveArguments)fMoveArguments.get(i);
				MoveParticipant[] moves= ParticipantManager.loadMoveParticipants(status,
					processor, element,
					arguments, natures, shared);
				result.addAll(Arrays.asList(moves));

			}
		}
		if (fCopy != null) {
			for (int i= 0; i < fCopy.size(); i++) {
				Object element= fCopy.get(i);
				CopyArguments arguments= (CopyArguments)fCopyArguments.get(i);
				CopyParticipant[] copies= ParticipantManager.loadCopyParticipants(status,
					processor, element,
					arguments, natures, shared);
				result.addAll(Arrays.asList(copies));
			}
		}
		if (fRename != null) {
			for (int i= 0; i < fRename.size(); i++) {
				Object resource= fRename.get(i);
				RenameArguments arguments= (RenameArguments) fRenameArguments.get(i);
				RenameParticipant[] renames= ParticipantManager.loadRenameParticipants(status,
					processor, resource,
					arguments, natures, shared);
				result.addAll(Arrays.asList(renames));
			}
		}
		return (RefactoringParticipant[])result.toArray(new RefactoringParticipant[result.size()]);
	}

	public void ignoreForDelta() {
		fIgnoreCount++;
	}

	public void trackForDelta() {
		fIgnoreCount--;
	}

	public void addDelta(DeltaDescription description) {
		if (fIgnoreCount > 0)
			return;
		internalAdd(description);
	}

	public void addCopyDelta(IResource copy, CopyArguments arguments) {
		if (fIgnoreCount == 0) {
			IPath destination= ((IResource)arguments.getDestination()).getFullPath().append(copy.getName());
			internalAdd(new CopyDescription(copy, destination));
		}
	}

	/**
	 * Checks if the resource will exist in the future based on
	 * the recorded resource modifications.
	 *
	 * @param resource the resource to check
	 * @return whether the resource will exist or not
	 */
	public boolean willExist(IResource resource) {
		if (fDeltaDescriptions == null)
			return false;
		IPath fullPath= resource.getFullPath();
		for (Iterator iter= fDeltaDescriptions.iterator(); iter.hasNext();) {
			DeltaDescription delta= (DeltaDescription) iter.next();
			if (fullPath.equals(delta.getDestinationPath()))
				return true;
		}
		return false;
	}

	public void buildDelta(IResourceChangeDescriptionFactory builder) {
		if (fDeltaDescriptions == null)
			return;
		for (Iterator iter= fDeltaDescriptions.iterator(); iter.hasNext();) {
			((DeltaDescription) iter.next()).buildDelta(builder);
		}
	}

	public static void buildMoveDelta(IResourceChangeDescriptionFactory builder, IResource resource, RenameArguments args) {
		IPath newPath= resource.getFullPath().removeLastSegments(1).append(args.getNewName());
		builder.move(resource, newPath);
	}

	public static void buildMoveDelta(IResourceChangeDescriptionFactory builder, IResource resource, MoveArguments args) {
		IPath destination= ((IResource)args.getDestination()).getFullPath().append(resource.getName());
		builder.move(resource, destination);
	}

	public static void buildCopyDelta(IResourceChangeDescriptionFactory builder, IResource resource, CopyArguments args) {
		IPath destination= ((IResource)args.getDestination()).getFullPath().append(resource.getName());
		builder.copy(resource, destination);
	}

	private void internalAdd(DeltaDescription description) {
		if (fDeltaDescriptions == null)
			fDeltaDescriptions= new ArrayList();
		fDeltaDescriptions.add(description);
	}
}

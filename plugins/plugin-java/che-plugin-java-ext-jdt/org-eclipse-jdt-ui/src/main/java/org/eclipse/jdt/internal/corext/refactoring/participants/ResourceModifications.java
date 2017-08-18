/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.participants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
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
 * @since 3.0
 */
public class ResourceModifications {

  private List<IResource> fCreate;
  private List<IResource> fDelete;

  private List<IResource> fMove;
  private List<MoveArguments> fMoveArguments;

  private List<IResource> fRename;
  private List<RenameArguments> fRenameArguments;

  private List<IResource> fCopy;
  private List<CopyArguments> fCopyArguments;

  private int fIgnoreCount;
  private List<DeltaDescription> fDeltaDescriptions;

  public abstract static class DeltaDescription {
    protected IResource fResource;

    public DeltaDescription(IResource resource) {
      fResource = resource;
    }

    public abstract void buildDelta(IResourceChangeDescriptionFactory builder);

    public abstract IPath getDestinationPath();
  }

  public static class DeleteDescription extends DeltaDescription {
    public DeleteDescription(IResource resource) {
      super(resource);
    }

    @Override
    public void buildDelta(IResourceChangeDescriptionFactory builder) {
      builder.delete(fResource);
    }

    @Override
    public IPath getDestinationPath() {
      return null;
    }
  }

  public static class ChangedDescription extends DeltaDescription {
    public ChangedDescription(IFile resource) {
      super(resource);
    }

    @Override
    public void buildDelta(IResourceChangeDescriptionFactory builder) {
      builder.change((IFile) fResource);
    }

    @Override
    public IPath getDestinationPath() {
      return null;
    }
  }

  public static class CreateDescription extends DeltaDescription {
    public CreateDescription(IResource resource) {
      super(resource);
    }

    @Override
    public void buildDelta(IResourceChangeDescriptionFactory builder) {
      builder.create(fResource);
    }

    @Override
    public IPath getDestinationPath() {
      return fResource.getFullPath();
    }
  }

  public static class MoveDescription extends DeltaDescription {
    private IPath fDestination;

    public MoveDescription(IResource resource, IPath destination) {
      super(resource);
      fDestination = destination;
    }

    @Override
    public void buildDelta(IResourceChangeDescriptionFactory builder) {
      IResource existing = ResourcesPlugin.getWorkspace().getRoot().findMember(fDestination);
      if (existing != null && !existing.equals(fResource)) {
        builder.delete(existing);
      }
      builder.move(fResource, fDestination);
    }

    @Override
    public IPath getDestinationPath() {
      return fDestination;
    }
  }

  public static class CopyDescription extends DeltaDescription {
    private IPath fDestination;

    public CopyDescription(IResource resource, IPath destination) {
      super(resource);
      fDestination = destination;
    }

    @Override
    public void buildDelta(IResourceChangeDescriptionFactory builder) {
      IResource existing = ResourcesPlugin.getWorkspace().getRoot().findMember(fDestination);
      if (existing != null && !existing.equals(fResource)) {
        builder.delete(existing);
      }
      builder.copy(fResource, fDestination);
    }

    @Override
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
   * Adds the given resource to the list of resources to be created.
   *
   * @param create the resource to be add to the list of resources to be created
   */
  public void addCreate(IResource create) {
    if (fCreate == null) fCreate = new ArrayList<IResource>(2);
    fCreate.add(create);
    if (fIgnoreCount == 0) {
      internalAdd(new CreateDescription(create));
    }
  }

  /**
   * Adds the given resource to the list of resources to be deleted.
   *
   * @param delete the resource to be deleted
   */
  public void addDelete(IResource delete) {
    if (fDelete == null) fDelete = new ArrayList<IResource>(2);
    fDelete.add(delete);
    if (fIgnoreCount == 0) {
      internalAdd(new DeleteDescription(delete));
    }
  }

  /**
   * Adds the given resource to the list of resources to be moved.
   *
   * @param move the resource to be moved
   * @param arguments the move arguments
   */
  public void addMove(IResource move, MoveArguments arguments) {
    if (fMove == null) {
      fMove = new ArrayList<IResource>(2);
      fMoveArguments = new ArrayList<MoveArguments>(2);
    }
    fMove.add(move);
    fMoveArguments.add(arguments);
    if (fIgnoreCount == 0) {
      IPath destination =
          ((IResource) arguments.getDestination()).getFullPath().append(move.getName());
      internalAdd(new MoveDescription(move, destination));
    }
  }

  /**
   * Adds the given resource to the list of resources to be copied.
   *
   * @param copy the resource to be copied
   * @param arguments the copy arguments
   */
  public void addCopy(IResource copy, CopyArguments arguments) {
    if (fCopy == null) {
      fCopy = new ArrayList<IResource>(2);
      fCopyArguments = new ArrayList<CopyArguments>(2);
    }
    fCopy.add(copy);
    fCopyArguments.add(arguments);
    addCopyDelta(copy, arguments);
  }

  /**
   * Adds the given resource to the list of renamed resources.
   *
   * @param rename the resource to be renamed
   * @param arguments the arguments of the rename
   */
  public void addRename(IResource rename, RenameArguments arguments) {
    Assert.isNotNull(rename);
    Assert.isNotNull(arguments);
    if (fRename == null) {
      fRename = new ArrayList<IResource>(2);
      fRenameArguments = new ArrayList<RenameArguments>(2);
    }
    fRename.add(rename);
    fRenameArguments.add(arguments);
    if (fIgnoreCount == 0) {
      IPath newPath = rename.getFullPath().removeLastSegments(1).append(arguments.getNewName());
      internalAdd(new MoveDescription(rename, newPath));
    }
  }

  public RefactoringParticipant[] getParticipants(
      RefactoringStatus status,
      RefactoringProcessor processor,
      String[] natures,
      SharableParticipants shared) {
    List<RefactoringParticipant> result = new ArrayList<RefactoringParticipant>(5);
    if (fDelete != null) {
      DeleteArguments arguments = new DeleteArguments();
      for (Iterator<IResource> iter = fDelete.iterator(); iter.hasNext(); ) {
        DeleteParticipant[] deletes =
            ParticipantManager.loadDeleteParticipants(
                status, processor, iter.next(), arguments, natures, shared);
        result.addAll(Arrays.asList(deletes));
      }
    }
    if (fCreate != null) {
      CreateArguments arguments = new CreateArguments();
      for (Iterator<IResource> iter = fCreate.iterator(); iter.hasNext(); ) {
        CreateParticipant[] creates =
            ParticipantManager.loadCreateParticipants(
                status, processor, iter.next(), arguments, natures, shared);
        result.addAll(Arrays.asList(creates));
      }
    }
    if (fMove != null) {
      for (int i = 0; i < fMove.size(); i++) {
        Object element = fMove.get(i);
        MoveArguments arguments = fMoveArguments.get(i);
        MoveParticipant[] moves =
            ParticipantManager.loadMoveParticipants(
                status, processor, element, arguments, natures, shared);
        result.addAll(Arrays.asList(moves));
      }
    }
    if (fCopy != null) {
      for (int i = 0; i < fCopy.size(); i++) {
        Object element = fCopy.get(i);
        CopyArguments arguments = fCopyArguments.get(i);
        CopyParticipant[] copies =
            ParticipantManager.loadCopyParticipants(
                status, processor, element, arguments, natures, shared);
        result.addAll(Arrays.asList(copies));
      }
    }
    if (fRename != null) {
      for (int i = 0; i < fRename.size(); i++) {
        Object resource = fRename.get(i);
        RenameArguments arguments = fRenameArguments.get(i);
        RenameParticipant[] renames =
            ParticipantManager.loadRenameParticipants(
                status, processor, resource, arguments, natures, shared);
        result.addAll(Arrays.asList(renames));
      }
    }
    return result.toArray(new RefactoringParticipant[result.size()]);
  }

  public void ignoreForDelta() {
    fIgnoreCount++;
  }

  public void trackForDelta() {
    fIgnoreCount--;
  }

  public void addDelta(DeltaDescription description) {
    if (fIgnoreCount > 0) return;
    internalAdd(description);
  }

  public void addCopyDelta(IResource copy, CopyArguments arguments) {
    if (fIgnoreCount == 0) {
      IPath destination =
          ((IResource) arguments.getDestination()).getFullPath().append(copy.getName());
      internalAdd(new CopyDescription(copy, destination));
    }
  }

  /**
   * Checks if the resource will exist in the future based on the recorded resource modifications.
   *
   * @param resource the resource to check
   * @return whether the resource will exist or not
   */
  public boolean willExist(IResource resource) {
    if (fDeltaDescriptions == null) return false;
    IPath fullPath = resource.getFullPath();
    for (Iterator<DeltaDescription> iter = fDeltaDescriptions.iterator(); iter.hasNext(); ) {
      DeltaDescription delta = iter.next();
      if (fullPath.equals(delta.getDestinationPath())) return true;
    }
    return false;
  }

  public void buildDelta(IResourceChangeDescriptionFactory builder) {
    if (fDeltaDescriptions == null) return;
    for (Iterator<DeltaDescription> iter = fDeltaDescriptions.iterator(); iter.hasNext(); ) {
      iter.next().buildDelta(builder);
    }
  }

  public static void buildMoveDelta(
      IResourceChangeDescriptionFactory builder, IResource resource, RenameArguments args) {
    IPath newPath = resource.getFullPath().removeLastSegments(1).append(args.getNewName());
    new MoveDescription(resource, newPath).buildDelta(builder);
  }

  public static void buildMoveDelta(
      IResourceChangeDescriptionFactory builder, IResource resource, MoveArguments args) {
    IPath destination =
        ((IResource) args.getDestination()).getFullPath().append(resource.getName());
    new MoveDescription(resource, destination).buildDelta(builder);
  }

  public static void buildCopyDelta(
      IResourceChangeDescriptionFactory builder, IResource resource, CopyArguments args) {
    IPath destination =
        ((IResource) args.getDestination()).getFullPath().append(resource.getName());
    new CopyDescription(resource, destination).buildDelta(builder);
  }

  private void internalAdd(DeltaDescription description) {
    if (fDeltaDescriptions == null) fDeltaDescriptions = new ArrayList<DeltaDescription>();
    fDeltaDescriptions.add(description);
  }
}

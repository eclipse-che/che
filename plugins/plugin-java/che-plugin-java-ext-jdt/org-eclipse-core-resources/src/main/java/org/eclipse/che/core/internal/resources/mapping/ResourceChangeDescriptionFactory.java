/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.internal.resources.mapping;

import org.eclipse.che.core.internal.resources.Workspace;
import org.eclipse.che.core.internal.utils.Policy;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/** Factory for creating a resource delta that describes a proposed change. */
public class ResourceChangeDescriptionFactory implements IResourceChangeDescriptionFactory {

  private ProposedResourceDelta root =
      new ProposedResourceDelta(ResourcesPlugin.getWorkspace().getRoot());

  /**
   * Creates and a delta representing a deleted resource, and adds it to the provided parent delta.
   *
   * @param parentDelta The parent of the deletion delta to create
   * @param resource The deleted resource to create a delta for
   */
  private ProposedResourceDelta buildDeleteDelta(
      ProposedResourceDelta parentDelta, IResource resource) {
    // start with the existing delta for this resource, if any, to preserve other flags
    ProposedResourceDelta delta = parentDelta.getChild(resource.getName());
    if (delta == null) {
      delta = new ProposedResourceDelta(resource);
      parentDelta.add(delta);
    }
    delta.setKind(IResourceDelta.REMOVED);
    if (resource.getType() == IResource.FILE) return delta;
    // recurse to build deletion deltas for children
    try {
      IResource[] members = ((IContainer) resource).members();
      int childCount = members.length;
      if (childCount > 0) {
        ProposedResourceDelta[] childDeltas = new ProposedResourceDelta[childCount];
        for (int i = 0; i < childCount; i++) childDeltas[i] = buildDeleteDelta(delta, members[i]);
      }
    } catch (CoreException e) {
      // don't need to create deletion deltas for children of inaccessible resources
    }
    return delta;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#change(org.eclipse.core.resources.IFile)
   */
  public void change(IFile file) {
    ProposedResourceDelta delta = getDelta(file);
    if (delta.getKind() == 0) delta.setKind(IResourceDelta.CHANGED);
    // the CONTENT flag only applies to the changed and moved from cases
    if (delta.getKind() == IResourceDelta.CHANGED
        || (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0
        || (delta.getFlags() & IResourceDelta.COPIED_FROM) != 0)
      delta.addFlags(IResourceDelta.CONTENT);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#close(org.eclipse.core.resources.IProject)
   */
  public void close(IProject project) {
    delete(project);
    ProposedResourceDelta delta = getDelta(project);
    delta.addFlags(IResourceDelta.OPEN);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#copy(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IPath)
   */
  public void copy(IResource resource, IPath destination) {
    moveOrCopyDeep(resource, destination, false /* copy */);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory#create(org.eclipse.core.resources.IResource)
   */
  public void create(IResource resource) {
    getDelta(resource).setKind(IResourceDelta.ADDED);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#delete(org.eclipse.core.resources.IResource)
   */
  public void delete(IResource resource) {
    if (resource.getType() == IResource.ROOT) {
      // the root itself cannot be deleted, so create deletions for each project
      IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
      for (int i = 0; i < projects.length; i++) buildDeleteDelta(root, projects[i]);
    } else {
      buildDeleteDelta(getDelta(resource.getParent()), resource);
    }
  }

  private void fail(CoreException e) {
    Policy.log(
        e.getStatus().getSeverity(),
        "An internal error occurred while accumulating a change description.",
        e); // $NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#getDelta()
   */
  public IResourceDelta getDelta() {
    return root;
  }

  private ProposedResourceDelta getDelta(IResource resource) {
    ProposedResourceDelta delta = (ProposedResourceDelta) root.findMember(resource.getFullPath());
    if (delta != null) {
      return delta;
    }
    IWorkspace workspace = ResourcesPlugin.getWorkspace();

    IResource iResource = resource.getParent();
    if (iResource == null) {
      iResource =
          ((Workspace) workspace)
              .newResource(resource.getFullPath().removeLastSegments(1), IResource.FOLDER);
    }
    ProposedResourceDelta parent = getDelta(iResource);
    delta = new ProposedResourceDelta(resource);
    parent.add(delta);
    return delta;
  }

  /*
   * Return the resource at the destination path that corresponds to the source resource
   * @param source the source resource
   * @param sourcePrefix the path of the root of the move or copy
   * @param destinationPrefix the path of the destination the root was copied to
   * @return the destination resource
   */
  protected IResource getDestinationResource(
      IResource source, IPath sourcePrefix, IPath destinationPrefix) {
    IPath relativePath = source.getFullPath().removeFirstSegments(sourcePrefix.segmentCount());
    IPath destinationPath = destinationPrefix.append(relativePath);
    IResource destination;
    IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
    switch (source.getType()) {
      case IResource.FILE:
        destination = wsRoot.getFile(destinationPath);
        break;
      case IResource.FOLDER:
        destination = wsRoot.getFolder(destinationPath);
        break;
      case IResource.PROJECT:
        destination = wsRoot.getProject(destinationPath.segment(0));
        break;
      default:
        // Shouldn't happen
        destination = null;
    }
    return destination;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.mapping.IProposedResourceDeltaFactory#move(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IPath)
   */
  public void move(IResource resource, IPath destination) {
    moveOrCopyDeep(resource, destination, true /* move */);
  }

  /**
   * Builds the delta representing a single resource being moved or copied.
   *
   * @param resource The resource being moved
   * @param sourcePrefix The root of the sub-tree being moved
   * @param destinationPrefix The root of the destination sub-tree
   * @param move <code>true</code> for a move, <code>false</code> for a copy
   * @return Whether to move or copy the child
   */
  boolean moveOrCopy(
      IResource resource,
      final IPath sourcePrefix,
      final IPath destinationPrefix,
      final boolean move) {
    ProposedResourceDelta sourceDelta = getDelta(resource);
    if (sourceDelta.getKind() == IResourceDelta.REMOVED) {
      // There is already a removed delta here so there
      // is nothing to move/copy
      return false;
    }
    IResource destinationResource =
        getDestinationResource(resource, sourcePrefix, destinationPrefix);
    ProposedResourceDelta destinationDelta = getDelta(destinationResource);
    if ((destinationDelta.getKind() & (IResourceDelta.ADDED | IResourceDelta.CHANGED)) > 0) {
      // There is already a resource at the destination
      // TODO: What do we do
      return false;
    }
    // First, create the delta for the source
    IPath fromPath = resource.getFullPath();
    boolean wasAdded = false;
    final int sourceFlags = sourceDelta.getFlags();
    if (move) {
      // We transfer the source flags to the destination
      if (sourceDelta.getKind() == IResourceDelta.ADDED) {
        if ((sourceFlags & IResourceDelta.MOVED_FROM) != 0) {
          // The resource was moved from somewhere else so
          // we need to transfer the path to the new location
          fromPath = sourceDelta.getMovedFromPath();
          sourceDelta.setMovedFromPath(null);
        }
        // The source was added and then moved so we'll
        // make it an add at the destination
        sourceDelta.setKind(0);
        wasAdded = true;
      } else {
        // We reset the status to be a remove/move_to
        sourceDelta.setKind(IResourceDelta.REMOVED);
        sourceDelta.setFlags(IResourceDelta.MOVED_TO);
        sourceDelta.setMovedToPath(
            destinationPrefix.append(fromPath.removeFirstSegments(sourcePrefix.segmentCount())));
      }
    }
    // Next, create the delta for the destination
    if (destinationDelta.getKind() == IResourceDelta.REMOVED) {
      // The destination was removed and is being re-added
      destinationDelta.setKind(IResourceDelta.CHANGED);
      destinationDelta.addFlags(IResourceDelta.REPLACED);
    } else {
      destinationDelta.setKind(IResourceDelta.ADDED);
    }
    if (!wasAdded || !fromPath.equals(resource.getFullPath())) {
      // The source wasn't added so it is a move/copy
      destinationDelta.addFlags(move ? IResourceDelta.MOVED_FROM : IResourceDelta.COPIED_FROM);
      destinationDelta.setMovedFromPath(fromPath);
      // Apply the source flags
      if (move) destinationDelta.addFlags(sourceFlags);
    }

    return true;
  }

  /**
   * Helper method that generate a move or copy delta for a sub-tree of resources being moved or
   * copied.
   */
  private void moveOrCopyDeep(IResource resource, IPath destination, final boolean move) {
    final IPath sourcePrefix = resource.getFullPath();
    final IPath destinationPrefix = destination;
    try {
      // build delta for the entire sub-tree if available
      if (resource.isAccessible()) {
        resource.accept(
            new IResourceVisitor() {
              public boolean visit(IResource child) {
                return moveOrCopy(child, sourcePrefix, destinationPrefix, move);
              }
            });
      } else {
        // just build a delta for the single resource
        moveOrCopy(resource, sourcePrefix, destination, move);
      }
    } catch (CoreException e) {
      fail(e);
    }
  }
}

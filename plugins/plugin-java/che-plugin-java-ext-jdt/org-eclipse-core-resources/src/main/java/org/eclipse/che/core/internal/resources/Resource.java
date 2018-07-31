/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.internal.resources;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.core.internal.utils.Policy;
import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.WrappedRuntimeException;
import org.eclipse.core.internal.watson.IPathRequestor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;

/** @author Evgen Vidolob */
public abstract class Resource implements IResource, IPathRequestor, ICoreConstants {
  /* package */ IPath path;
  /* package */ Workspace workspace;

  protected Resource(IPath path, Workspace workspace) {
    this.path = path.removeTrailingSeparator();
    this.workspace = workspace;
  }

  @Override
  public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
    accept(visitor, IResource.DEPTH_INFINITE, memberFlags);
  }

  @Override
  public void accept(final IResourceProxyVisitor visitor, final int depth, final int memberFlags)
      throws CoreException {
    java.io.File file = workspace.getFile(getFullPath());
    int maxDepth = depth == IResource.DEPTH_INFINITE ? Integer.MAX_VALUE : depth;
    try {
      final ResourceProxy resourceProxy = new ResourceProxy();
      final int workspacePath = workspace.getAbsoluteWorkspacePath().length();
      Files.walkFileTree(
          file.toPath(),
          Collections.<FileVisitOption>emptySet(),
          maxDepth,
          new FileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                throws IOException {
              FileVisitResult result = FileVisitResult.CONTINUE;
              try {
                String string = file.toString();
                IPath path = new Path(string.substring(workspacePath));
                resourceProxy.info = workspace.getResourceInfo(path);
                resourceProxy.fullPath = path;

                boolean shouldContinue = true;
                switch (depth) {
                  case DEPTH_ZERO:
                    shouldContinue = false;
                    break;
                  case DEPTH_ONE:
                    shouldContinue = !Resource.this.path.equals(path.removeLastSegments(1));
                    break;
                  case DEPTH_INFINITE:
                    shouldContinue = true;
                    break;
                }
                boolean visit = visitor.visit(resourceProxy) && shouldContinue;
                result = visit ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
              } catch (CoreException e) {
                throw new WrappedRuntimeException(e);
              } finally {
                resourceProxy.reset();
              }
              return result;
            }

            @Override
            public FileVisitResult visitFileFailed(java.nio.file.Path file, IOException exc)
                throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc)
                throws IOException {
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new CoreException(
          new Status(IStatus.ERROR, ResourcesPlugin.getPluginId(), e.getMessage(), e));
    }
  }

  @Override
  public void accept(IResourceVisitor visitor) throws CoreException {
    accept(visitor, IResource.DEPTH_INFINITE, 0);
  }

  @Override
  public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms)
      throws CoreException {
    accept(visitor, depth, includePhantoms ? IContainer.INCLUDE_PHANTOMS : 0);
  }

  @Override
  public void accept(final IResourceVisitor visitor, int depth, int memberFlags)
      throws CoreException {
    // use the fast visitor if visiting to infinite depth
    if (depth == IResource.DEPTH_INFINITE) {
      accept(
          new IResourceProxyVisitor() {
            public boolean visit(IResourceProxy proxy) throws CoreException {
              return visitor.visit(proxy.requestResource());
            }
          },
          memberFlags);
      return;
    }
    // it is invalid to call accept on a phantom when INCLUDE_PHANTOMS is not specified
    final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
    ResourceInfo info = getResourceInfo(includePhantoms, false);
    int flags = getFlags(info);
    if ((memberFlags & IContainer.DO_NOT_CHECK_EXISTENCE) == 0) checkAccessible(flags);

    // check that this resource matches the member flags
    if (!isMember(flags, memberFlags)) return;
    // visit this resource
    if (!visitor.visit(this) || depth == DEPTH_ZERO) return;
    // get the info again because it might have been changed by the visitor
    info = getResourceInfo(includePhantoms, false);
    if (info == null) return;
    // thread safety: (cache the type to avoid changes -- we might not be inside an operation)
    int type = info.getType();
    if (type == FILE) return;
    // if we had a gender change we need to fix up the resource before asking for its members
    IContainer resource =
        getType() != type
            ? (IContainer) workspace.newResource(getFullPath(), type)
            : (IContainer) this;
    IResource[] members = resource.members(memberFlags);
    for (int i = 0; i < members.length; i++)
      members[i].accept(visitor, DEPTH_ZERO, memberFlags | IContainer.DO_NOT_CHECK_EXISTENCE);
  }

  public int getFlags(ResourceInfo info) {
    //        return (info == null) ? NULL_FLAG : info.getFlags();
    return NULL_FLAG;
  }

  /**
   * Returns whether a resource should be included in a traversal based on the provided member
   * flags.
   *
   * @param flags The resource info flags
   * @param memberFlags The member flag mask
   * @return Whether the resource is included
   */
  protected boolean isMember(int flags, int memberFlags) {
    int excludeMask = 0;
    if ((memberFlags & IContainer.INCLUDE_PHANTOMS) == 0) excludeMask |= M_PHANTOM;
    if ((memberFlags & IContainer.INCLUDE_HIDDEN) == 0) excludeMask |= M_HIDDEN;
    if ((memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) == 0)
      excludeMask |= M_TEAM_PRIVATE_MEMBER;
    if ((memberFlags & IContainer.EXCLUDE_DERIVED) != 0) excludeMask |= M_DERIVED;
    // the resource is a matching member if it matches none of the exclude flags
    return flags != NULL_FLAG && (flags & excludeMask) == 0;
  }

  @Override
  public boolean contains(ISchedulingRule rule) {
    if (this == rule) return true;
    // must allow notifications to nest in all resource rules
    if (rule.getClass().equals(WorkManager.NotifyRule.class)) return true;
    if (rule instanceof MultiRule) {
      MultiRule multi = (MultiRule) rule;
      ISchedulingRule[] children = multi.getChildren();
      for (int i = 0; i < children.length; i++) if (!contains(children[i])) return false;
      return true;
    }
    if (!(rule instanceof IResource)) return false;
    IResource resource = (IResource) rule;
    if (!workspace.equals(resource.getWorkspace())) return false;
    return path.isPrefixOf(resource.getFullPath());
  }

  @Override
  public void clearHistory(IProgressMonitor monitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(IPath destination, boolean force, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(IPath destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(IProjectDescription destDesc, boolean force, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copy(IProjectDescription destDesc, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IResourceProxy createProxy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IMarker createMarker(String type) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
    delete(force ? IResource.FORCE : IResource.NONE, monitor);
  }

  @Override
  public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      //            String message = NLS.bind(Messages.resources_deleting, getFullPath());
      //            monitor.beginTask("", Policy.totalWork * 1000); //$NON-NLS-1$
      //            monitor.subTask(message);
      final ISchedulingRule rule = workspace.getRuleFactory().deleteRule(this);
      try {
        workspace.prepareOperation(rule, monitor);
        // if there is no resource then there is nothing to delete so just return
        if (!exists()) return;
        workspace.beginOperation(true);
        //                broadcastPreDeleteEvent();

        // when a project is being deleted, flush the build order in case there is a problem
        //                if (this.getType() == IResource.PROJECT)
        //                    workspace.flushBuildOrder();

        //                final IFileStore originalStore = getStore();
        //                boolean wasLinked = isLinked();
        //                message = Messages.resources_deleteProblem;
        //                MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES,
        // IResourceStatus.FAILED_DELETE_LOCAL, message, null);
        WorkManager workManager = workspace.getWorkManager();
        //                ResourceTree tree = new ResourceTree(workspace.getFileSystemManager(),
        // workManager.getLock(), status, updateFlags);
        int depth = 0;
        try {
          depth = workManager.beginUnprotected();
          workspace.delete(this);
          //                    unprotectedDelete(tree, updateFlags, monitor);
        } finally {
          workManager.endUnprotected(depth);
        }
        if (getType() == ROOT) {
          //                    // need to clear out the root info
          //                    workspace.getMarkerManager().removeMarkers(this,
          // IResource.DEPTH_ZERO);
          //                    getPropertyManager().deleteProperties(this, IResource.DEPTH_ZERO);
          //                    getResourceInfo(false, false).clearSessionProperties();
        }
        // Invalidate the tree for further use by clients.
        //                tree.makeInvalid();
        //                if (!tree.getStatus().isOK())
        //                    throw new ResourceException(tree.getStatus());
        // update any aliases of this resource
        // note that deletion of a linked resource cannot affect other resources
        //                if (!wasLinked)
        //                    workspace.getAliasManager().updateAliases(this, originalStore,
        // IResource.DEPTH_INFINITE, monitor);
        //                if (getType() == PROJECT) {
        //                     make sure the rule factory is cleared on project deletion
        //                    ((Rules) workspace.getRuleFactory()).setRuleFactory((IProject) this,
        // null);
        //                     make sure project deletion is remembered
        //                    workspace.getSaveManager().requestSnapshot();
        //                }
      } catch (OperationCanceledException e) {
        workspace.getWorkManager().operationCanceled();
        throw e;
      } finally {
        workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork * 1000));
      }
    } finally {
      monitor.done();
    }
  }

  @Override
  public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists() {
    return workspace.getFile(path).exists();
  }

  @Override
  public IMarker findMarker(long id) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth)
      throws CoreException {
    //        throw new UnsupportedOperationException();
    // TODO
    return new IMarker[0];
  }

  @Override
  public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getFileExtension() {
    String name = getName();
    int index = name.lastIndexOf('.');
    if (index == -1) return null;
    if (index == (name.length() - 1)) return ""; // $NON-NLS-1$
    return name.substring(index + 1);
  }

  @Override
  public IPath getFullPath() {
    return path;
  }

  @Override
  public long getLocalTimeStamp() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPath getLocation() {
    return new Path(workspace.getAbsoluteWorkspacePath() + path.toOSString());
  }

  @Override
  public URI getLocationURI() {
    IProject project = getProject();
    if (project != null && !project.exists()) return null;
    return workspace.getFile(getFullPath()).toURI();
  }

  @Override
  public IMarker getMarker(long id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getModificationStamp() {
    return 0;
  }

  @Override
  public String getName() {
    return path.lastSegment();
  }

  @Override
  public IContainer getParent() {
    int segments = path.segmentCount();
    // zero and one segments handled by subclasses
    if (segments < 2) Assert.isLegal(false, path.toString());
    if (segments == 2) return workspace.getRoot().getProject(path.segment(0));
    IPath parentPath = this.path.removeLastSegments(1);
    ResourceInfo resourceInfo = workspace.getResourceInfo(parentPath);
    if (resourceInfo == null) {
      return null;
    }
    return (IContainer) workspace.newResource(parentPath, resourceInfo.getType());
  }

  @Override
  public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPersistentProperty(QualifiedName qualifiedName) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProject getProject() {
    if (this instanceof IProject) {
      return (IProject) this;
    }
    final IProject[] projects = workspace.getRoot().getProjects();
    // here we try to found project by path of resources. We will get all projects and select
    // longest path of project that
    // start with path of resource.
    final Optional<IProject> max =
        Arrays.stream(projects)
            .filter(iProject -> path.toOSString().startsWith(iProject.getFullPath().toOSString()))
            .max(
                (o1, o2) -> {
                  if (o1.getFullPath().toOSString().length()
                      > o2.getFullPath().toOSString().length()) {
                    return 1;
                  }
                  if (o2.getFullPath().toOSString().length()
                      > o1.getFullPath().toOSString().length()) {
                    return -1;
                  }
                  return 0;
                });
    if (max.isPresent()) {
      return max.get();
    } else {
      return null;
    }
  }

  @Override
  public IPath getProjectRelativePath() {
    return getFullPath().removeFirstSegments(getProject().getFullPath().segmentCount());
  }

  @Override
  public IPath getRawLocation() {
    //        if (isLinked())
    //            return FileUtil.toPath(((Project)
    // getProject()).internalGetDescription().getLinkLocationURI(getProjectRelativePath()));
    //        return getLocation();
    throw new UnsupportedOperationException();
  }

  @Override
  public URI getRawLocationURI() {
    //        if (isLinked())
    //            return ((Project)
    // getProject()).internalGetDescription().getLinkLocationURI(getProjectRelativePath());
    //        return getLocationURI();
    throw new UnsupportedOperationException();
  }

  @Override
  public ResourceAttributes getResourceAttributes() {
    //        throw new UnsupportedOperationException();
    return new ResourceAttributes();
  }

  @Override
  public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getSessionProperty(QualifiedName qualifiedName) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public abstract int getType();

  public IWorkspace getWorkspace() {
    return workspace;
  }

  @Override
  public int hashCode() {
    // the container may be null if the identified resource
    // does not exist so don't bother with it in the hash
    return getFullPath().hashCode();
  }

  /* (non-Javadoc)
   * @see IResource#isAccessible()
   */
  public boolean isAccessible() {
    return exists();
  }

  @Override
  public boolean isDerived() {
    return isDerived(IResource.NONE);
  }

  @Override
  public boolean isDerived(int options) {
    //        throw new UnsupportedOperationException();
    return true;
  }

  @Override
  public boolean isHidden() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isHidden(int options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLinked() {
    return isLinked(NONE);
  }

  @Override
  public boolean isLocal(int depth) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVirtual() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLinked(int options) {
    //        throw new UnsupportedOperationException();
    return false;
  }

  @Override
  public boolean isPhantom() {
    //        throw new UnsupportedOperationException();
    return false;
  }

  @Override
  public boolean isReadOnly() {
    //        throw new UnsupportedOperationException();
    return false;
  }

  @Override
  public boolean isSynchronized(int depth) {
    //        throw new UnsupportedOperationException();
    return true;
  }

  @Override
  public boolean isTeamPrivateMember() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isTeamPrivateMember(int options) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(IPath destination, boolean force, IProgressMonitor monitor)
      throws CoreException {
    move(destination, force ? IResource.FORCE : IResource.NONE, monitor);
  }

  @Override
  public void move(IPath destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      String message = NLS.bind(Messages.resources_moving, getFullPath());
      monitor.beginTask(message, Policy.totalWork);
      Policy.checkCanceled(monitor);
      destination = makePathAbsolute(destination);
      //            checkValidPath(destination, getType(), false);
      Resource destResource = workspace.newResource(destination, getType());
      final ISchedulingRule rule = workspace.getRuleFactory().moveRule(this, destResource);
      WorkManager workManager = workspace.getWorkManager();
      try {
        workspace.prepareOperation(rule, monitor);
        workspace.beginOperation(true);
        int depth = 0;
        try {
          depth = workManager.beginUnprotected();
          unprotectedMove(destResource, updateFlags, monitor);
        } finally {
          workManager.endUnprotected(depth);
        }
      } finally {
        workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
      }
    } finally {
      monitor.done();
    }
  }

  protected IPath makePathAbsolute(IPath target) {
    if (target.isAbsolute()) return target;
    return getParent().getFullPath().append(target);
  }

  /**
   * Calls the move/delete hook to perform the move. Since this method calls client code, it is run
   * "unprotected", so the workspace lock is not held. Returns true if resources were actually
   * moved, and false otherwise.
   */
  private boolean unprotectedMove(
      final IResource destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException, ResourceException {
    //        IMoveDeleteHook hook = workspace.getMoveDeleteHook();
    switch (getType()) {
      case IResource.FILE:
        //                if (!hook.moveFile(tree, (IFile) this, (IFile) destination, updateFlags,
        // Policy.subMonitorFor(monitor, Policy.opWork / 2)))
        workspace.standardMoveFile(
            (IFile) this,
            (IFile) destination,
            updateFlags,
            Policy.subMonitorFor(monitor, Policy.opWork));
        break;
      case IResource.FOLDER:
        //                if (!hook.moveFolder(tree, (IFolder) this, (IFolder) destination,
        // updateFlags, Policy.subMonitorFor(monitor, Policy.opWork / 2)))
        workspace.standardMoveFolder(
            (IFolder) this,
            (IFolder) destination,
            updateFlags,
            Policy.subMonitorFor(monitor, Policy.opWork));
        break;
      case IResource.PROJECT:
        IProject project = (IProject) this;
        // if there is no change in name, there is nothing to do so return.
        if (getName().equals(destination.getName())) return false;
        IProjectDescription description = project.getDescription();
        description.setName(destination.getName());
        //                if (!hook.moveProject(tree, project, description, updateFlags,
        // Policy.subMonitorFor(monitor, Policy.opWork / 2)))
        workspace.standardMoveProject(
            project, description, updateFlags, Policy.subMonitorFor(monitor, Policy.opWork));
        break;
      case IResource.ROOT:
        String msg = Messages.resources_moveRoot;
        throw new ResourceException(
            new ResourceStatus(IResourceStatus.INVALID_VALUE, getFullPath(), msg));
    }
    return true;
  }

  @Override
  public void move(
      IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
    //        throw new UnsupportedOperationException();
  }

  @Override
  public void revertModificationStamp(long value) throws CoreException {
    //        throw new UnsupportedOperationException();
  }

  @Override
  public IPath requestPath() {
    return getFullPath();
  }

  @Override
  public String requestName() {
    return getName();
  }

  @Override
  public void setDerived(boolean isDerived) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHidden(boolean isHidden) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long setLocalTimeStamp(long value) throws CoreException {
    //        throw new UnsupportedOperationException();
    return value;
  }

  @Override
  public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setReadOnly(boolean readonly) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
    throw new UnsupportedOperationException();
  }

  public String getTypeString() {
    switch (getType()) {
      case FILE:
        return "L"; // $NON-NLS-1$
      case FOLDER:
        return "F"; // $NON-NLS-1$
      case PROJECT:
        return "P"; // $NON-NLS-1$
      case ROOT:
        return "R"; // $NON-NLS-1$
    }
    return ""; // $NON-NLS-1$
  }

  @Override
  public Object getAdapter(Class aClass) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see IProject#delete(boolean, boolean, IProgressMonitor)
   * @see IWorkspaceRoot#delete(boolean, boolean, IProgressMonitor)
   * N.B. This is not an IResource method!
   */
  public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
    delete(updateFlags, monitor);
  }

  @Override
  public boolean isConflicting(ISchedulingRule rule) {
    if (this == rule) return true;
    // must not schedule at same time as notification
    if (rule.getClass().equals(WorkManager.NotifyRule.class)) return true;
    if (rule instanceof MultiRule) {
      MultiRule multi = (MultiRule) rule;
      ISchedulingRule[] children = multi.getChildren();
      for (int i = 0; i < children.length; i++) if (isConflicting(children[i])) return true;
      return false;
    }
    if (!(rule instanceof IResource)) return false;
    IResource resource = (IResource) rule;
    if (!workspace.equals(resource.getWorkspace())) return false;
    IPath otherPath = resource.getFullPath();
    return path.isPrefixOf(otherPath) || otherPath.isPrefixOf(path);
  }

  @Override
  public void touch(IProgressMonitor iProgressMonitor) throws CoreException {
    // do nothing
    // todo
  }

  /* (non-Javadoc)
   * @see IResource#getPathVariableManager()
   */
  public IPathVariableManager getPathVariableManager() {
    //        if (getProject() == null)
    //            return workspace.getPathVariableManager();
    //        return new ProjectPathVariableManager(this);
    throw new UnsupportedOperationException();
  }
  /* (non-Javadoc)
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return getTypeString() + getFullPath().toString();
  }

  /* (non-Javadoc)
   * @see IResource#equals(Object)
   */
  @Override
  public boolean equals(Object target) {
    if (this == target) return true;
    if (!(target instanceof Resource)) return false;
    Resource resource = (Resource) target;
    return getType() == resource.getType()
        && path.equals(resource.path)
        && workspace.equals(resource.workspace);
  }

  /* (non-Javadoc)
   * @see IFile#move(IPath, boolean, boolean, IProgressMonitor)
   * @see IFolder#move(IPath, boolean, boolean, IProgressMonitor)
   */
  public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException {
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= keepHistory ? IResource.KEEP_HISTORY : IResource.NONE;
    move(destination, updateFlags, monitor);
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IFolder#createLink(IPath, int, IProgressMonitor)
   * @see org.eclipse.core.resources.IFile#createLink(IPath, int, IProgressMonitor)
   */
  public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    //        Assert.isNotNull(localLocation);
    //        createLink(URIUtil.toURI(localLocation), updateFlags, monitor);
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IFolder#createLink(URI, int, IProgressMonitor)
   * @see org.eclipse.core.resources.IFile#createLink(URI, int, IProgressMonitor)
   */
  public void createLink(URI localLocation, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the resource info. Returns null if the resource doesn't exist. If the phantom flag is
   * true, phantom resources are considered. If the mutable flag is true, a mutable info is
   * returned.
   */
  public ResourceInfo getResourceInfo(boolean phantom, boolean mutable) {
    return workspace.getResourceInfo(getFullPath());
  }

  public void checkAccessible(int flags) throws CoreException {
    checkExists(flags, true);
  }

  /**
   * Checks that this resource exists. If checkType is true, the type of this resource and the one
   * in the tree must match.
   *
   * @exception CoreException if this resource does not exist
   */
  public void checkExists(int flags, boolean checkType) throws CoreException {
    if (!exists(flags, checkType)) {
      String message = NLS.bind(Messages.resources_mustExist, getFullPath());
      throw new ResourceException(IResourceStatus.RESOURCE_NOT_FOUND, getFullPath(), message, null);
    }
  }

  public boolean exists(int flags, boolean checkType) {
    //        return flags != NULL_FLAG && !(checkType && ResourceInfo.getType(flags) != getType());
    return true;
  }
}

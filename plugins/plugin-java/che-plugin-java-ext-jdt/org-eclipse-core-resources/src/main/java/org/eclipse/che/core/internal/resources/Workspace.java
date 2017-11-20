/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.internal.resources;

import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

import com.google.inject.Provider;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.core.internal.utils.Policy;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.resources.OS;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.resources.WorkspaceDescription;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.TeamHook;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class Workspace implements IWorkspace {

  public static final boolean caseSensitive =
      new java.io.File("a").compareTo(new java.io.File("A")) != 0;
  // $NON-NLS-1$ //$NON-NLS-2$
  private static final Logger LOG = LoggerFactory.getLogger(Workspace.class);
  protected final IWorkspaceRoot defaultRoot = new WorkspaceRoot(Path.ROOT, this);
  private final Provider<ProjectManager> projectManager;
  private final Provider<PathTransformer> pathTransformerProvider;
  private final Provider<FsManager> fsManagerProvider;
  /**
   * Work manager should never be accessed directly because accessor asserts that workspace is still
   * open.
   */
  protected WorkManager _workManager;
  /** The currently installed team hook. */
  protected TeamHook teamHook = null;

  private String wsPath;
  /**
   * Scheduling rule factory. This field is null if the factory has not been used yet. The accessor
   * method should be used rather than accessing this field directly.
   */
  private IResourceRuleFactory ruleFactory;

  private IUndoContext undoContext = new UndoContext();

  public Workspace(
      String path,
      Provider<ProjectManager> projectManager,
      Provider<PathTransformer> pathTransformerProvider,
      Provider<FsManager> fsManagerProvider) {
    this.wsPath = path;
    this.projectManager = projectManager;
    this.pathTransformerProvider = pathTransformerProvider;
    this.fsManagerProvider = fsManagerProvider;
    _workManager = new WorkManager(this);
    _workManager.startup(null);
    _workManager.postWorkspaceStartup();
  }

  public static WorkspaceDescription defaultWorkspaceDescription() {
    return new WorkspaceDescription("Workspace"); // $NON-NLS-1$
  }

  private static boolean deleteDirectory(java.io.File directory) {
    if (directory.exists()) {
      java.io.File[] files = directory.listFiles();
      if (null != files) {
        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            deleteDirectory(files[i]);
          } else {
            files[i].delete();
          }
        }
      }
    }
    return (directory.delete());
  }

  public String getAbsoluteWorkspacePath() {
    return pathTransformerProvider.get().transform(ROOT).toString();
  }

  public Resource newResource(IPath path, int type) {
    String message;
    switch (type) {
      case IResource.FOLDER:
        if (path.segmentCount() < ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH) {
          message =
              "Path must include project and resource name: " + path.toString(); // $NON-NLS-1$
          Assert.isLegal(false, message);
        }
        return new Folder(path.makeAbsolute(), this);
      case IResource.FILE:
        if (path.segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
          message =
              "Path must include project and resource name: " + path.toString(); // $NON-NLS-1$
          Assert.isLegal(false, message);
        }
        return new File(path.makeAbsolute(), this);
      case IResource.PROJECT:
        return (Resource) getRoot().getProject(path.toOSString());
      case IResource.ROOT:
        return (Resource) getRoot();
    }
    Assert.isLegal(false);
    // will never get here because of assertion.
    return null;
  }

  @Override
  public void addResourceChangeListener(IResourceChangeListener listener) {
    // TODO
    //        throw new UnsupportedOperationException();
  }

  @Override
  public void addResourceChangeListener(IResourceChangeListener listener, int eventMask) {
    //        throw new UnsupportedOperationException();
    // TODO
  }

  @Override
  public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant iSaveParticipant)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ISavedState addSaveParticipant(String s, ISaveParticipant iSaveParticipant)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build(int i, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void build(
      IBuildConfiguration[] iBuildConfigurations,
      int i,
      boolean b,
      IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void checkpoint(boolean b) {
    //        throw new UnsupportedOperationException();
  }

  @Override
  public IProject[][] computePrerequisiteOrder(IProject[] iProjects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ProjectOrder computeProjectOrder(IProject[] iProjects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus copy(
      IResource[] iResources, IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus copy(IResource[] iResources, IPath iPath, int i, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor)
      throws CoreException {
    int updateFlags = force ? IResource.FORCE : IResource.NONE;
    updateFlags |= IResource.KEEP_HISTORY;
    return delete(resources, updateFlags, monitor);
  }

  @Override
  public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      int opWork = Math.max(resources.length, 1);
      int totalWork = Policy.totalWork * opWork / Policy.opWork;
      String message = Messages.resources_deleting_0;
      monitor.beginTask(message, totalWork);
      message = Messages.resources_deleteProblem;
      MultiStatus result =
          new MultiStatus(
              ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
      if (resources.length == 0) {
        return result;
      }
      resources = resources.clone(); // to avoid concurrent changes to this array
      try {
        prepareOperation(getRoot(), monitor);
        beginOperation(true);
        for (int i = 0; i < resources.length; i++) {
          Policy.checkCanceled(monitor);
          Resource resource = (Resource) resources[i];
          if (resource == null) {
            monitor.worked(1);
            continue;
          }
          try {
            resource.delete(updateFlags, Policy.subMonitorFor(monitor, 1));
          } catch (CoreException e) {
            // Don't really care about the exception unless the resource is still around.
            ResourceInfo info = resource.getResourceInfo(false, false);
            if (resource.exists(resource.getFlags(info), false)) {
              message = NLS.bind(Messages.resources_couldnotDelete, resource.getFullPath());
              result.merge(
                  new org.eclipse.core.internal.resources.ResourceStatus(
                      IResourceStatus.FAILED_DELETE_LOCAL, resource.getFullPath(), message));
              result.merge(e.getStatus());
            }
          }
        }
        if (result.matches(IStatus.ERROR)) {
          throw new ResourceException(result);
        }
        return result;
      } catch (OperationCanceledException e) {
        getWorkManager().operationCanceled();
        throw e;
      } finally {
        endOperation(getRoot(), true, Policy.subMonitorFor(monitor, totalWork - opWork));
      }
    } finally {
      monitor.done();
    }
  }

  @Override
  public void deleteMarkers(IMarker[] iMarkers) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void forgetSavedTree(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFilterMatcherDescriptor[] getFilterMatcherDescriptors() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFilterMatcherDescriptor getFilterMatcherDescriptor(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProjectNatureDescriptor[] getNatureDescriptors() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProjectNatureDescriptor getNatureDescriptor(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<IProject, IProject[]> getDanglingReferences() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IWorkspaceDescription getDescription() {
    WorkspaceDescription workingCopy = defaultWorkspaceDescription();
    //        description.copyTo(workingCopy);
    return workingCopy;
  }

  @Override
  public void setDescription(IWorkspaceDescription iWorkspaceDescription) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IWorkspaceRoot getRoot() {
    return defaultRoot;
  }

  @Override
  public IResourceRuleFactory getRuleFactory() {
    // note that the rule factory is created lazily because it
    // requires loading the teamHook extension
    if (ruleFactory == null) {
      ruleFactory = new Rules(this);
    }
    return ruleFactory;
  }

  @Override
  public ISynchronizer getSynchronizer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAutoBuilding() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isTreeLocked() {
    // todo
    return false;
  }

  @Override
  public IProjectDescription loadProjectDescription(IPath iPath) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProjectDescription loadProjectDescription(InputStream inputStream) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus move(
      IResource[] iResources, IPath iPath, boolean b, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus move(IResource[] iResources, IPath iPath, int i, IProgressMonitor iProgressMonitor)
      throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IBuildConfiguration newBuildConfig(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProjectDescription newProjectDescription(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeResourceChangeListener(IResourceChangeListener listener) {
    //        throw new UnsupportedOperationException();
    // TODO
  }

  @Override
  public void removeSaveParticipant(Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeSaveParticipant(String s) {
    throw new UnsupportedOperationException();
  }

  /**
   * Called before checking the pre-conditions of an operation. Optionally supply a scheduling rule
   * to determine when the operation is safe to run. If a scheduling rule is supplied, this method
   * will block until it is safe to run.
   *
   * @param rule the scheduling rule that describes what this operation intends to modify.
   */
  public void prepareOperation(ISchedulingRule rule, IProgressMonitor monitor)
      throws CoreException {
    try {
      // make sure autobuild is not running if it conflicts with this operation
      ISchedulingRule buildRule = getRuleFactory().buildRule();
      //            if (rule != null && buildRule != null && (rule.isConflicting(buildRule) ||
      // buildRule.isConflicting(rule)))
      //                buildManager.interrupt();
    } finally {
      getWorkManager().checkIn(rule, monitor);
    }
    //        if (!isOpen()) {
    //            String message = Messages.resources_workspaceClosed;
    //            throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message,
    // null);
    //        }
  }

  /**
   * We should not have direct references to this field. All references should go through this
   * method.
   */
  public WorkManager getWorkManager() throws CoreException {
    if (_workManager == null) {
      String message = Messages.resources_shutdown;
      throw new ResourceException(
          new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message));
    }
    return _workManager;
  }

  @Override
  public void run(
      IWorkspaceRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor)
      throws CoreException {
    monitor = Policy.monitorFor(monitor);
    try {
      monitor.beginTask("", Policy.totalWork); // $NON-NLS-1$
      int depth = -1;
      boolean avoidNotification = (options & IWorkspace.AVOID_UPDATE) != 0;
      try {
        prepareOperation(rule, monitor);
        beginOperation(true);
        //                if (avoidNotification)
        //                    avoidNotification = notificationManager.beginAvoidNotify();
        depth = getWorkManager().beginUnprotected();
        action.run(
            Policy.subMonitorFor(
                monitor, Policy.opWork, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
      } catch (OperationCanceledException e) {
        getWorkManager().operationCanceled();
        throw e;
      } finally {
        //                if (avoidNotification)
        //                    notificationManager.endAvoidNotify();
        if (depth >= 0) {
          getWorkManager().endUnprotected(depth);
        }
        endOperation(rule, false, Policy.subMonitorFor(monitor, Policy.endOpWork));
      }
    } finally {
      monitor.done();
    }
  }

  public void beginOperation(boolean createNewTree) throws CoreException {
    WorkManager workManager = getWorkManager();
    workManager.incrementNestedOperations();
    if (!workManager.isBalanced()) {
      Assert.isTrue(false, "Operation was not prepared."); // $NON-NLS-1$
    }
    //        if (workManager.getPreparedOperationDepth() > 1) {
    //            if (createNewTree && tree.isImmutable())
    //                newWorkingTree();
    //            return;
    //        }
    //        // stash the current tree as the basis for this operation.
    //        operationTree = tree;
    //        if (createNewTree && tree.isImmutable())
    //            newWorkingTree();
  }

  /**
   * End an operation (group of resource changes). Notify interested parties that resource changes
   * have taken place. All registered resource change listeners are notified. If autobuilding is
   * enabled, a build is run.
   */
  public void endOperation(ISchedulingRule rule, boolean build, IProgressMonitor monitor)
      throws CoreException {
    WorkManager workManager = getWorkManager();
    // don't do any end operation work if we failed to check in
    if (workManager.checkInFailed(rule)) {
      return;
    }
    // This is done in a try finally to ensure that we always decrement the operation count
    // and release the workspace lock.  This must be done at the end because snapshot
    // and "hasChanges" comparison have to happen without interference from other threads.
    boolean hasTreeChanges = false;
    boolean depthOne = false;
    try {
      workManager.setBuild(build);
      // if we are not exiting a top level operation then just decrement the count and return
      depthOne = workManager.getPreparedOperationDepth() == 1;
      //            if (!(notificationManager.shouldNotify() || depthOne)) {
      //                notificationManager.requestNotify();
      //                return;
      //            }
      // do the following in a try/finally to ensure that the operation tree is nulled at the end
      // as we are completing a top level operation.
      try {
        //                notificationManager.beginNotify();
        // check for a programming error on using beginOperation/endOperation
        Assert.isTrue(
            workManager.getPreparedOperationDepth() > 0,
            "Mismatched begin/endOperation"); // $NON-NLS-1$

        // At this time we need to re-balance the nested operations. It is necessary because
        // build() and snapshot() should not fail if they are called.
        workManager.rebalanceNestedOperations();

        // find out if any operation has potentially modified the tree
        //                hasTreeChanges = workManager.shouldBuild();
        // double check if the tree has actually changed
        //                if (hasTreeChanges)
        //                    hasTreeChanges = operationTree != null && ElementTree.hasChanges(tree,
        // operationTree, ResourceComparator
        // .getBuildComparator(), true);
        //                broadcastPostChange();
        //                // Request a snapshot if we are sufficiently out of date.
        //                saveManager.snapshotIfNeeded(hasTreeChanges);
      } finally {
        //                // make sure the tree is immutable if we are ending a top-level operation.
        //                if (depthOne) {
        //                    tree.immutable();
        //                    operationTree = null;
        //                } else
        //                    newWorkingTree();
      }
    } finally {
      workManager.checkOut(rule);
    }
    //        if (depthOne)
    //            buildManager.endTopLevel(hasTreeChanges);
  }

  @Override
  public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
    run(action, defaultRoot, IWorkspace.AVOID_UPDATE, monitor);
  }

  @Override
  public IStatus save(boolean b, IProgressMonitor iProgressMonitor) throws CoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] sortNatureSet(String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateEdit(IFile[] iFiles, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateFiltered(IResource iResource) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateLinkLocation(IResource iResource, IPath iPath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateLinkLocationURI(IResource iResource, URI uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateName(String segment, int type) {
    String message;
    /* segment must not be null */
    if (segment == null) {
      message = Messages.resources_nameNull;
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    // cannot be an empty string
    if (segment.length() == 0) {
      message = Messages.resources_nameEmpty;
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    /* test invalid characters */
    char[] chars = OS.INVALID_RESOURCE_CHARACTERS;
    for (int i = 0; i < chars.length; i++) {
      if (segment.indexOf(chars[i]) != -1) {
        message = NLS.bind(Messages.resources_invalidCharInName, String.valueOf(chars[i]), segment);
        return new org.eclipse.core.internal.resources.ResourceStatus(
            IResourceStatus.INVALID_VALUE, null, message);
      }
    }

    /* test invalid OS names */
    if (!OS.isNameValid(segment)) {
      message = NLS.bind(Messages.resources_invalidName, segment);
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }
    return Status.OK_STATUS;
  }

  @Override
  public IStatus validateNatureSet(String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validatePath(String path, int type) {
    /* path must not be null */
    if (path == null) {
      String message = Messages.resources_pathNull;
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }
    return validatePath(Path.fromOSString(path), type, false);
  }

  /**
   * Validates that the given workspace path is valid for the given type. If <code>lastSegmentOnly
   * </code> is true, it is assumed that all segments except the last one have previously been
   * validated. This is an optimization for validating a leaf resource when it is known that the
   * parent exists (and thus its parent path must already be valid).
   */
  public IStatus validatePath(IPath path, int type, boolean lastSegmentOnly) {
    String message;

    /* path must not be null */
    if (path == null) {
      message = Messages.resources_pathNull;
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    /* path must not have a device separator */
    if (path.getDevice() != null) {
      message =
          NLS.bind(
              Messages.resources_invalidCharInPath, String.valueOf(IPath.DEVICE_SEPARATOR), path);
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    /* path must not be the root path */
    if (path.isRoot()) {
      message = Messages.resources_invalidRoot;
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    /* path must be absolute */
    if (!path.isAbsolute()) {
      message = NLS.bind(Messages.resources_mustBeAbsolute, path);
      return new org.eclipse.core.internal.resources.ResourceStatus(
          IResourceStatus.INVALID_VALUE, null, message);
    }

    /* validate segments */
    int numberOfSegments = path.segmentCount();
    if ((type & IResource.PROJECT) != 0) {
      if (numberOfSegments == ICoreConstants.PROJECT_SEGMENT_LENGTH) {
        return validateName(path.segment(0), IResource.PROJECT);
      } else if (type == IResource.PROJECT) {
        message = NLS.bind(Messages.resources_projectPath, path);
        return new org.eclipse.core.internal.resources.ResourceStatus(
            IResourceStatus.INVALID_VALUE, null, message);
      }
    }
    if ((type & (IResource.FILE | IResource.FOLDER)) != 0) {
      if (numberOfSegments < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
        message = NLS.bind(Messages.resources_resourcePath, path);
        return new org.eclipse.core.internal.resources.ResourceStatus(
            IResourceStatus.INVALID_VALUE, null, message);
      }
      int fileFolderType = type &= ~IResource.PROJECT;
      int segmentCount = path.segmentCount();
      if (lastSegmentOnly) {
        return validateName(path.segment(segmentCount - 1), fileFolderType);
      }
      IStatus status = validateName(path.segment(0), IResource.PROJECT);
      if (!status.isOK()) {
        return status;
      }
      // ignore first segment (the project)
      for (int i = 1; i < segmentCount; i++) {
        status = validateName(path.segment(i), fileFolderType);
        if (!status.isOK()) {
          return status;
        }
      }
      return Status.OK_STATUS;
    }
    message = NLS.bind(Messages.resources_invalidPath, path);
    return new org.eclipse.core.internal.resources.ResourceStatus(
        IResourceStatus.INVALID_VALUE, null, message);
  }

  @Override
  public IStatus validateProjectLocation(IProject iProject, IPath iPath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IStatus validateProjectLocationURI(IProject iProject, URI uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IPathVariableManager getPathVariableManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAdapter(Class aClass) {
    if (aClass == IUndoContext.class) {
      return undoContext;
    }
    throw new UnsupportedOperationException();
  }

  public java.io.File getFile(IPath path) {
    return new java.io.File(wsPath, path.toOSString());
  }

  public ResourceInfo getResourceInfo(IPath path) {
    String wsPath = absolutize(path.toOSString());
    return fsManagerProvider.get().exists(wsPath) ? newElement(getType(wsPath)) : null;
  }

  private int getType(String wsPath) {
    if (fsManagerProvider.get().existsAsFile(wsPath)) {
      return IResource.FILE;
    } else {
      if (projectManager.get().isRegistered(wsPath)) {
        return IResource.PROJECT;
      } else {
        return IResource.FOLDER;
      }
    }
  }

  /** Create and return a new tree element of the given type. */
  protected ResourceInfo newElement(int type) {
    ResourceInfo result = null;
    switch (type) {
      case IResource.FILE:
      case IResource.FOLDER:
        result = new ResourceInfo(type);
        break;
      case IResource.PROJECT:
        result = new ResourceInfo(type);
        break;
      case IResource.ROOT:
        result = new ResourceInfo(type);
        break;
    }

    return result;
  }

  public IResource[] getChildren(IPath path) {

    String parentWsPath = absolutize(path.toOSString());
    if (fsManagerProvider.get().existsAsDir(parentWsPath)) {
      List<String> allChildrenWsPaths =
          new ArrayList<>(fsManagerProvider.get().getAllChildrenWsPaths(parentWsPath));
      if (!allChildrenWsPaths.isEmpty()) {
        IResource[] resources = new IResource[allChildrenWsPaths.size()];
        for (int i = 0; i < allChildrenWsPaths.size(); i++) {
          String childWsPath = allChildrenWsPaths.get(i);
          IPath iPath = new Path(childWsPath);
          resources[i] = newResource(iPath, getType(childWsPath));
        }
        resources =
            Arrays.stream(resources)
                .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .toArray(IResource[]::new);
        return resources;
      }
    }
    return ICoreConstants.EMPTY_RESOURCE_ARRAY;
  }

  public void createResource(IResource resource, int updateFlags) throws CoreException {
    try {
      IPath path = resource.getFullPath();
      switch (resource.getType()) {
        case IResource.FILE:
          String newName = path.lastSegment();
          String childWsPath = absolutize(path.removeLastSegments(1).toOSString());

          if (!fsManagerProvider.get().exists(childWsPath)) {
            throw new NotFoundException(
                "Can't find parent folder: " + path.removeLastSegments(1).toOSString());
          }
          String newFileWsPath = resolve(childWsPath, newName);
          fsManagerProvider.get().createFile(newFileWsPath);
          break;
        case IResource.FOLDER:
          String directoryWsPath = absolutize(path.toOSString());
          fsManagerProvider.get().createDir(directoryWsPath);
          break;
        case IResource.PROJECT:
          ProjectConfigImpl projectConfig = new ProjectConfigImpl();
          projectConfig.setPath(resource.getName());
          projectConfig.setName(resource.getName());
          projectConfig.setType(BaseProjectType.ID);
          projectManager.get().create(projectConfig, new HashMap<>());
          break;
        default:
          throw new UnsupportedOperationException();
      }
    } catch (ForbiddenException
        | ConflictException
        | ServerException
        | NotFoundException
        | BadRequestException e) {
      throw new CoreException(new Status(0, ResourcesPlugin.getPluginId(), e.getMessage(), e));
    }
  }

  public void setFileContent(File file, InputStream content) {
    try {
      String fileWsPath = absolutize(file.getFullPath().toOSString());
      if (fsManagerProvider.get().existsAsFile(fileWsPath)) {
        fsManagerProvider.get().update(fileWsPath, content);
      }
    } catch (ServerException | NotFoundException | ConflictException e) {
      ResourcesPlugin.log(e);
    }
  }

  public TeamHook getTeamHook() {
    // default to use Core's implementation
    // create anonymous subclass because TeamHook is abstract
    if (teamHook == null) {
      teamHook =
          new TeamHook() {
            // empty
          };
    }
    return teamHook;
  }

  public void delete(Resource resource) {
    try {
      projectManager.get().delete(resource.getFullPath().toOSString());
    } catch (ServerException | ForbiddenException | ConflictException | NotFoundException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  void write(
      File file, InputStream content, int updateFlags, boolean append, IProgressMonitor monitor)
      throws CoreException {
    try {
      String fileWsPath = absolutize(file.getFullPath().toOSString());
      if (!fsManagerProvider.get().existsAsFile(fileWsPath)) {
        fsManagerProvider.get().createFile(fileWsPath, content);
      } else {
        fsManagerProvider.get().update(fileWsPath, content);
      }
    } catch (ConflictException | ServerException | NotFoundException e) {
      throw new CoreException(new Status(0, "", e.getMessage(), e));
    }
  }

  public void standardMoveFile(
      IFile file, IFile destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    String srcWsPath = absolutize(file.getFullPath().toOSString());
    String dstDirectoryWsPath =
        absolutize(destination.getFullPath().removeLastSegments(1).toOSString());
    String dstWsPath = resolve(dstDirectoryWsPath, destination.getName());

    try {
      fsManagerProvider.get().move(srcWsPath, dstWsPath);
    } catch (NotFoundException | ConflictException | ServerException e) {
      throw new CoreException(new Status(0, "", e.getMessage(), e));
    }
  }

  public void standardMoveFolder(
      IFolder folder, IFolder destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException {
    String srcWsPath = absolutize(folder.getFullPath().toOSString());
    String dstParentWsPath =
        absolutize(destination.getFullPath().removeLastSegments(1).toOSString());
    String dstWsPath = resolve(dstParentWsPath, destination.getName());

    try {
      fsManagerProvider.get().move(srcWsPath, dstWsPath);
    } catch (NotFoundException | ConflictException | ServerException e) {
      throw new CoreException(new Status(0, "", e.getMessage(), e));
    }
  }

  public void standardMoveProject(
      IProject project,
      IProjectDescription description,
      int updateFlags,
      IProgressMonitor monitor) {
    throw new UnsupportedOperationException("standardMoveProject");
  }

  public void addLifecycleListener(org.eclipse.core.internal.resources.Rules rules) {}

  /** Returns project manager associated with this workspace */
  public ProjectManager getProjectRegistry() {
    return projectManager.get();
  }
}

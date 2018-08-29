/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources.team;

import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.resources.InternalTeamHook;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A general hook class for operations that team providers may be interested in participating in.
 * Implementors of the hook should provide a concrete subclass, and override any methods they are
 * interested in.
 *
 * <p>This class is intended to be subclassed by the team component in conjunction with the <code>
 * org.eclipse.core.resources.teamHook</code> standard extension point. Individual team providers
 * may also subclass this class. It is not intended to be subclassed by other clients. The methods
 * defined on this class are called from within the implementations of workspace API methods and
 * must not be invoked directly by clients.
 *
 * @since 2.1
 */
public abstract class TeamHook extends InternalTeamHook {
  /**
   * The default resource scheduling rule factory. This factory can be used for projects that the
   * team hook methods do not participate in.
   *
   * @see #getRuleFactory(IProject)
   * @see #setRuleFactory(IProject, IResourceRuleFactory)
   * @since 3.0
   */
  protected final IResourceRuleFactory defaultFactory = new ResourceRuleFactory();

  /**
   * Creates a new team hook. Default constructor for use by subclasses and the resources plug-in
   * only.
   */
  protected TeamHook() {
    super();
  }

  /**
   * Returns the resource scheduling rule factory that should be used when workspace operations are
   * invoked on resources in that project. The workspace will ask the team hook this question only
   * once per project, per session. The workspace will assume the returned result is valid for the
   * rest of that session, unless the rule is changed by calling <code>setRuleFactory</code>.
   *
   * <p>This method must not return <code>null</code>. If no special rules are required by the team
   * hook for the given project, the value of the <code>defaultFactory</code> field should be
   * returned.
   *
   * <p>This default implementation always returns the value of the <code>defaultFactory</code>
   * field. Subclasses may override and provide a subclass of <code>ResourceRuleFactory</code>.
   *
   * @param project the project to return scheduling rules for
   * @return the resource scheduling rules for a project
   * @see #setRuleFactory(IProject, IResourceRuleFactory)
   * @see ResourceRuleFactory
   * @since 3.0
   */
  public IResourceRuleFactory getRuleFactory(IProject project) {
    return defaultFactory;
  }

  /**
   * Sets the resource scheduling rule factory to use for resource modifications in the given
   * project. This method only needs to be called if the factory has changed since the initial call
   * to <code>getRuleFactory</code> for the given project
   *
   * <p>The supplied factory must not be <code>null</code>. If no special rules are required by the
   * team hook for the given project, the value of the <code>defaultFactory</code> field should be
   * used.
   *
   * <p>Note that the new rule factory will only take effect for resource changing operations that
   * begin after this method completes. Care should be taken to avoid calling this method during the
   * invocation of any resource changing operation (in any thread). The best time to change rule
   * factories is during resource change notification when the workspace is locked for modification.
   *
   * @param project the project to change the resource rule factory for
   * @param factory the new resource rule factory
   * @see #getRuleFactory(IProject)
   * @see IResourceRuleFactory
   * @since 3.0
   */
  @Override
  protected final void setRuleFactory(IProject project, IResourceRuleFactory factory) {
    super.setRuleFactory(project, factory);
  }

  /**
   * Validates whether a particular attempt at link creation is allowed. This gives team providers
   * an opportunity to hook into the beginning of the implementation of <code>IFile.createLink
   * </code>.
   *
   * <p>The implementation of this method runs "below" the resources API and is therefore very
   * restricted in what resource API method it can call. The list of useable methods includes most
   * resource operations that read but do not update the resource tree; resource operations that
   * modify resources and trigger deltas must not be called from within the dynamic scope of the
   * invocation of this method.
   *
   * <p>This method should be overridden by subclasses that want to control what links are created.
   * The default implementation of this method allows all links to be created.
   *
   * @param file the file to be linked
   * @param updateFlags bit-wise or of update flag constants (only ALLOW_MISSING_LOCAL is relevant
   *     here)
   * @param location a file system path where the file should be linked
   * @return a status object with code <code>IStatus.OK</code> if linking is allowed, otherwise a
   *     status object with severity <code>IStatus.ERROR</code> indicating why the creation is not
   *     allowed.
   * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
   */
  public IStatus validateCreateLink(IFile file, int updateFlags, IPath location) {
    return Status.OK_STATUS;
  }

  /**
   * Validates whether a particular attempt at link creation is allowed. This gives team providers
   * an opportunity to hook into the beginning of the implementation of {@link IFile#createLink(URI,
   * int, IProgressMonitor) }
   *
   * <p>The implementation of this method runs "below" the resources API and is therefore very
   * restricted in what resource API method it can call. The list of useable methods includes most
   * resource operations that read but do not update the resource tree; resource operations that
   * modify resources and trigger deltas must not be called from within the dynamic scope of the
   * invocation of this method.
   *
   * <p>This method should be overridden by subclasses that want to control what links are created.
   * The default implementation of this method allows all links to be created.
   *
   * @param file the file to be linked
   * @param updateFlags bit-wise or of update flag constants (only ALLOW_MISSING_LOCAL is relevant
   *     here)
   * @param location a file system URI where the file should be linked
   * @return a status object with code <code>IStatus.OK</code> if linking is allowed, otherwise a
   *     status object with severity <code>IStatus.ERROR</code> indicating why the creation is not
   *     allowed.
   * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
   * @since 3.2
   */
  public IStatus validateCreateLink(IFile file, int updateFlags, URI location) {
    // forward to old method to ensure old hooks get a chance to validate in the local case
    if (EFS.SCHEME_FILE.equals(location.getScheme()))
      return validateCreateLink(file, updateFlags, URIUtil.toPath(location));
    return Status.OK_STATUS;
  }

  /**
   * Validates whether a particular attempt at link creation is allowed. This gives team providers
   * an opportunity to hook into the beginning of the implementation of <code>IFolder.createLink
   * </code>.
   *
   * <p>The implementation of this method runs "below" the resources API and is therefore very
   * restricted in what resource API method it can call. The list of useable methods includes most
   * resource operations that read but do not update the resource tree; resource operations that
   * modify resources and trigger deltas must not be called from within the dynamic scope of the
   * invocation of this method.
   *
   * <p>This method should be overridden by subclasses that want to control what links are created.
   * The default implementation of this method allows all links to be created.
   *
   * @param folder the file to be linked
   * @param updateFlags bit-wise or of update flag constants (only ALLOW_MISSING_LOCAL is relevant
   *     here)
   * @param location a file system path where the folder should be linked
   * @return a status object with code <code>IStatus.OK</code> if linking is allowed, otherwise a
   *     status object with severity <code>IStatus.ERROR</code> indicating why the creation is not
   *     allowed.
   * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
   */
  public IStatus validateCreateLink(IFolder folder, int updateFlags, IPath location) {
    return Status.OK_STATUS;
  }

  /**
   * Validates whether a particular attempt at link creation is allowed. This gives team providers
   * an opportunity to hook into the beginning of the implementation of {@link
   * IFolder#createLink(URI, int, IProgressMonitor)}
   *
   * <p>The implementation of this method runs "below" the resources API and is therefore very
   * restricted in what resource API method it can call. The list of useable methods includes most
   * resource operations that read but do not update the resource tree; resource operations that
   * modify resources and trigger deltas must not be called from within the dynamic scope of the
   * invocation of this method.
   *
   * <p>This method should be overridden by subclasses that want to control what links are created.
   * The default implementation of this method allows all links to be created.
   *
   * @param folder the file to be linked
   * @param updateFlags bit-wise or of update flag constants (only ALLOW_MISSING_LOCAL is relevant
   *     here)
   * @param location a file system path where the folder should be linked
   * @return a status object with code <code>IStatus.OK</code> if linking is allowed, otherwise a
   *     status object with severity <code>IStatus.ERROR</code> indicating why the creation is not
   *     allowed.
   * @see org.eclipse.core.resources.IResource#ALLOW_MISSING_LOCAL
   * @since 3.2
   */
  public IStatus validateCreateLink(IFolder folder, int updateFlags, URI location) {
    // forward to old method to ensure old hooks get a chance to validate in the local case
    if (EFS.SCHEME_FILE.equals(location.getScheme()))
      return validateCreateLink(folder, updateFlags, URIUtil.toPath(location));
    return Status.OK_STATUS;
  }
}

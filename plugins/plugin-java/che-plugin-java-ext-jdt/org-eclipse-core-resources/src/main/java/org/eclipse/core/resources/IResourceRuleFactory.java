/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM - Initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A resource rule factory returns scheduling rules for API methods that modify the workspace. These
 * rules can be used when creating jobs or other operations that perform a series of modifications
 * on the workspace. This allows clients to implement two phase commit semantics, where all
 * necessary rules are obtained prior to executing a long running operation.
 *
 * <p>Note that simple use of the workspace APIs does not require use of scheduling rules. All
 * workspace API methods that modify the workspace will automatically obtain any scheduling rules
 * needed to perform the modification. However, if you are aggregating a set of changes to the
 * workspace using <code>WorkspaceJob</code> or <code>IWorkspaceRunnable</code> you can use
 * scheduling rules to lock a portion of the workspace for the duration of the job or runnable. If
 * you provide a non-null scheduling rule, a runtime exception will occur if you try to modify a
 * portion of the workspace that is not covered by the rule for the runnable or job.
 *
 * <p>If more than one rule is needed, they can be aggregated using the <code>MultiRule.combine
 * </code> method. Simplifying a group of rules does not change the set of resources that are
 * covered, but can improve job scheduling performance.
 *
 * <p>Note that <code>null</code> is a valid scheduling rule (indicating that no resources need to
 * be locked), and thus all methods in this class may return <code>null</code>.
 *
 * @see WorkspaceJob
 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int,
 *     org.eclipse.core.runtime.IProgressMonitor)
 * @see org.eclipse.core.runtime.jobs.MultiRule#combine(ISchedulingRule, ISchedulingRule)
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceRuleFactory {
  /**
   * Returns the scheduling rule that is required for creating a project, folder, or file.
   *
   * @param resource the resource being created
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule createRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for building a project or the entire workspace.
   *
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule buildRule();

  /**
   * Returns the scheduling rule that is required for changing the charset setting for a file or the
   * default charset setting for a container.
   *
   * @param resource the resource for which the charset will be changed
   * @return a scheduling rule, or <code>null</code>
   * @since 3.1
   */
  public ISchedulingRule charsetRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for changing the derived flag on a resource.
   *
   * @param resource the resource for which the derived flag will be changed
   * @return a scheduling rule, or <code>null</code>
   * @since 3.6
   */
  public ISchedulingRule derivedRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for copying a resource.
   *
   * @param source the source of the copy
   * @param destination the destination of the copy
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule copyRule(IResource source, IResource destination);

  /**
   * Returns the scheduling rule that is required for deleting a resource.
   *
   * @param resource the resource to be deleted
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule deleteRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for creating, modifying, or deleting markers on a
   * resource.
   *
   * @param resource the resource owning the marker to be modified
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule markerRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for modifying a resource. For files, modification
   * includes setting and appending contents. For projects, modification includes opening or closing
   * the project, or setting the project description using the {@link IResource#AVOID_NATURE_CONFIG}
   * flag. For all resources <code>touch</code> is considered to be a modification.
   *
   * @param resource the resource being modified
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule modifyRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for moving a resource.
   *
   * @param source the source of the move
   * @param destination the destination of the move
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule moveRule(IResource source, IResource destination);

  /**
   * Returns the scheduling rule that is required for performing <code>refreshLocal</code> on a
   * resource.
   *
   * @param resource the resource to refresh
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule refreshRule(IResource resource);

  /**
   * Returns the scheduling rule that is required for a <code>validateEdit</code>
   *
   * @param resources the resources to be validated
   * @return a scheduling rule, or <code>null</code>
   */
  public ISchedulingRule validateEditRule(IResource[] resources);
}

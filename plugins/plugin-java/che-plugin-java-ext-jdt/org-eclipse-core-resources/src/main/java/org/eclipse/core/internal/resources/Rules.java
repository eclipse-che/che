/**
 * ***************************************************************************** Copyright (c)
 * 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.che.core.internal.resources.Workspace;
import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.resources.team.TeamHook;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * Class for calculating scheduling rules for resource changing operations. This factory delegates
 * to the TeamHook to obtain an appropriate factory for the resource that the operation is proposing
 * to modify.
 */
public class Rules implements IResourceRuleFactory, ILifecycleListener {
  private final ResourceRuleFactory defaultFactory = new ResourceRuleFactory() {
        /**/
      };
  /** Map of project names to the factory for that project. */
  private final Map<String, IResourceRuleFactory> projectsToRules =
      Collections.synchronizedMap(new HashMap<String, IResourceRuleFactory>());

  private final TeamHook teamHook;
  private final IWorkspaceRoot root;

  /**
   * Creates a new scheduling rule factory for the given workspace
   *
   * @param workspace
   */
  Rules(Workspace workspace) {
    this.root = workspace.getRoot();
    this.teamHook = workspace.getTeamHook();
    workspace.addLifecycleListener(this);
  }

  /** Obtains the scheduling rule from the appropriate factory for a build operation. */
  public ISchedulingRule buildRule() {
    // team hook currently cannot change this rule
    return root;
  }

  /** Obtains the scheduling rule from the appropriate factories for a copy operation. */
  public ISchedulingRule copyRule(IResource source, IResource destination) {
    if (source.getType() == IResource.ROOT || destination.getType() == IResource.ROOT) return root;
    // source is not modified, destination is created
    return factoryFor(destination).copyRule(source, destination);
  }

  /** Obtains the scheduling rule from the appropriate factory for a create operation. */
  public ISchedulingRule createRule(IResource resource) {
    if (resource.getType() == IResource.ROOT) return root;
    return factoryFor(resource).createRule(resource);
  }

  /** Obtains the scheduling rule from the appropriate factory for a delete operation. */
  public ISchedulingRule deleteRule(IResource resource) {
    if (resource.getType() == IResource.ROOT) return root;
    return factoryFor(resource).deleteRule(resource);
  }

  /** Returns the scheduling rule factory for the given resource */
  private IResourceRuleFactory factoryFor(IResource destination) {
    IResourceRuleFactory fac = projectsToRules.get(destination.getFullPath().segment(0));
    if (fac == null) {
      // use the default factory if the project is not yet accessible
      if (!destination.getProject().isAccessible()) return defaultFactory;
      // ask the team hook to supply one
      fac = teamHook.getRuleFactory(destination.getProject());
      projectsToRules.put(destination.getFullPath().segment(0), fac);
    }
    return fac;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.internal.events.ILifecycleListener#handleEvent(org.eclipse.core.internal.events.LifecycleEvent)
   */
  public void handleEvent(LifecycleEvent event) {
    // clear resource rule factory for projects that are about to be closed
    // or deleted. It is ok to do this during a PRE event because the rule
    // has already been obtained at this point.
    switch (event.kind) {
      case LifecycleEvent.PRE_PROJECT_CLOSE:
      case LifecycleEvent.PRE_PROJECT_DELETE:
      case LifecycleEvent.PRE_PROJECT_MOVE:
        setRuleFactory((IProject) event.resource, null);
    }
  }

  /** Obtains the scheduling rule from the appropriate factory for a charset change operation. */
  public ISchedulingRule charsetRule(IResource resource) {
    if (resource.getType() == IResource.ROOT) return null;
    return factoryFor(resource).charsetRule(resource);
  }

  /**
   * Obtains the scheduling rule from the appropriate factory for a derived flag change operation.
   */
  public ISchedulingRule derivedRule(IResource resource) {
    // team hook currently cannot change this rule
    return null;
  }

  /** Obtains the scheduling rule from the appropriate factory for a marker change operation. */
  public ISchedulingRule markerRule(IResource resource) {
    // team hook currently cannot change this rule
    return null;
  }

  /** Obtains the scheduling rule from the appropriate factory for a modify operation. */
  public ISchedulingRule modifyRule(IResource resource) {
    if (resource.getType() == IResource.ROOT) return root;
    return factoryFor(resource).modifyRule(resource);
  }

  /** Obtains the scheduling rule from the appropriate factories for a move operation. */
  public ISchedulingRule moveRule(IResource source, IResource destination) {
    if (source.getType() == IResource.ROOT || destination.getType() == IResource.ROOT) return root;
    // treat a move across projects as a create on the destination and a delete on the source
    if (!source.getFullPath().segment(0).equals(destination.getFullPath().segment(0)))
      return MultiRule.combine(
          modifyRule(source.getProject()), modifyRule(destination.getProject()));
    return factoryFor(source).moveRule(source, destination);
  }

  /** Obtains the scheduling rule from the appropriate factory for a refresh operation. */
  public ISchedulingRule refreshRule(IResource resource) {
    if (resource.getType() == IResource.ROOT) return root;
    return factoryFor(resource).refreshRule(resource);
  }

  /* (non-javadoc)
   * Implements TeamHook#setRuleFactory
   */
  void setRuleFactory(IProject project, IResourceRuleFactory factory) {
    if (factory == null) projectsToRules.remove(project.getName());
    else projectsToRules.put(project.getName(), factory);
  }

  /** Combines rules for each parameter to validateEdit from the corresponding rule factories. */
  public ISchedulingRule validateEditRule(IResource[] resources) {
    if (resources.length == 0) return null;
    // optimize rule for single file
    if (resources.length == 1) {
      if (resources[0].getType() == IResource.ROOT) return root;
      return factoryFor(resources[0]).validateEditRule(resources);
    }
    // gather rules for each resource from appropriate factory
    HashSet<ISchedulingRule> rules = new HashSet<ISchedulingRule>();
    IResource[] oneResource = new IResource[1];
    for (int i = 0; i < resources.length; i++) {
      if (resources[i].getType() == IResource.ROOT) return root;
      oneResource[0] = resources[i];
      ISchedulingRule rule = factoryFor(resources[i]).validateEditRule(oneResource);
      if (rule != null) rules.add(rule);
    }
    if (rules.isEmpty()) return null;
    if (rules.size() == 1) return rules.iterator().next();
    ISchedulingRule[] ruleArray = rules.toArray(new ISchedulingRule[rules.size()]);
    return new MultiRule(ruleArray);
  }
}

/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM - Initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.resources;

import org.eclipse.che.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.TeamHook;

/**
 * The internal abstract superclass of all TeamHook implementations. This superclass provides access
 * to internal non-API methods that are not available from the API package. Plugin developers should
 * not subclass this class.
 *
 * @see TeamHook
 */
public class InternalTeamHook {
  /* (non-Javadoc)
   * Internal implementation of TeamHook#setRulesFor(IProject,IResourceRuleFactory)
   */
  protected void setRuleFactory(IProject project, IResourceRuleFactory factory) {
    Workspace workspace = ((Workspace) project.getWorkspace());
    ((Rules) workspace.getRuleFactory()).setRuleFactory(project, factory);
  }
}

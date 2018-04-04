/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching.environments;

import org.eclipse.che.jdt.core.launching.IVMInstallType;
import org.eclipse.che.jdt.core.launching.LibraryLocation;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Contributes access rules for an execution environment. Contributed with an execution environments
 * extension.
 *
 * <p>Clients contributing an access rule participant may implement this interface.
 *
 * @since 3.3
 */
public interface IAccessRuleParticipant {

  /**
   * Returns a collection of access rules to be applied to the specified VM libraries and execution
   * environment in the context of the given project. An array of access rules is returned for each
   * library specified by <code>libraries</code>, possibly empty.
   *
   * @param environment the environment that access rules are requested for
   * @param vm the vm that access rules are requested for
   * @param libraries the libraries that access rules are requested for
   * @param project the project the access rules are requested for or <code>null</code> if none
   * @return a collection of arrays of access rules - one array per library, possibly empty
   * @since 3.3
   */
  public IAccessRule[][] getAccessRules(
      IExecutionEnvironment environment,
      IVMInstallType vm,
      LibraryLocation[] libraries,
      IJavaProject project);
}

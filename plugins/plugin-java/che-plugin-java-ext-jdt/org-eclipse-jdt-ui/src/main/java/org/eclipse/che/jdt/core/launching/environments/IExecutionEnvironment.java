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

import java.util.Map;
import java.util.Properties;
import org.eclipse.che.jdt.core.launching.IVMInstallType;
import org.eclipse.che.jdt.core.launching.LibraryLocation;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;

/**
 * An execution environment describes capabilities of a Java runtime environment (<code>IVMInstall
 * </code>).
 *
 * <p>An execution environment is contributed in plug-in XML via the <code>
 * org.eclipse.jdt.launching.executionEnvironments</code> extension point.
 *
 * <p>Clients contributing execution environments may provide and implement execution environment
 * analyzer delegates.
 *
 * @since 3.2
 * @see IExecutionEnvironmentAnalyzerDelegate
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IExecutionEnvironment {

  /**
   * Returns a unique identifier for this execution environment. Corresponds to the <code>id</code>
   * attribute in plug-in XML.
   *
   * @return unique identifier of this execution environment
   */
  public String getId();

  /**
   * Returns a brief human-readable description of this environment.
   *
   * @return brief human-readable description of this environment.
   */
  public String getDescription();

  //	/**
  //	 * Returns a collection of VM installs compatible with this environment,
  //	 * possibly empty.
  //	 *
  //	 * @return a collection of VM installs compatible with this environment,
  //	 *  possibly empty.
  //	 */
  //	public IVMInstall[] getCompatibleVMs();
  //
  //	/**
  //	 * Returns whether the specified VM install is strictly compatible with
  //	 * this environment. Returns <code>true</code> to indicate the VM install
  //	 * is strictly compatible with this environment and <code>false</code> to indicate
  //	 * the VM install represents a superset of this environment.
  //	 *
  //	 * @param vm VM install
  //	 * @return whether the VM install is strictly compatible with this environment
  //	 */
  //	public boolean isStrictlyCompatible(IVMInstall vm);
  //
  //	/**
  //	 * Returns the VM that is used by default for this execution environment,
  //	 * or <code>null</code> if none.
  //	 *
  //	 * @return default VM for this environment or <code>null</code> if none
  //	 */
  //	public IVMInstall getDefaultVM();
  //
  //	/**
  //	 * Sets the VM to use by default for this execution environment.
  //	 *
  //	 * @param vm VM to use by default for this execution environment,
  //	 *  or <code>null</code> to clear the default setting
  //	 * @exception IllegalArgumentException if the given VM is not compatible with
  //	 *  this environment
  //	 */
  //	public void setDefaultVM(IVMInstall vm);

  /**
   * Returns a collection of access rules to be applied to the specified VM libraries for this
   * execution environment in the context of the given project. An array of access rules is returned
   * for each library specified by <code>libraries</code>, possibly empty.
   *
   * <p>Access rules for an execution environment are defined by access rule participants
   * contributed in a <code>org.eclipse.jdt.launching.executionEnvironments</code> extension.
   *
   * @param vm the VM that access rules are requested for
   * @param libraries the libraries that access rules are requested for
   * @param project the project the access rules are requested for or <code>null</code> if none
   * @return a collection of arrays of access rules - one array per library
   * @since 3.3
   */
  public IAccessRule[][] getAccessRules(
      IVMInstallType vm, LibraryLocation[] libraries, IJavaProject project);

  /**
   * Returns the OSGi profile properties associated with this execution environment or <code>null
   * </code> if none. Profile properties specify attributes such as {@link
   * org.osgi.framework.Constants#FRAMEWORK_SYSTEMPACKAGES}. Profile properties can be optionally
   * contributed with an execution environment extension.
   *
   * @return associated profile properties or <code>null</code> if none
   * @since 3.5
   */
  public Properties getProfileProperties();

  /**
   * Returns a collection of execution environments that are subsets of this environment.
   *
   * @return a collection of execution environments that are subsets of this environment
   * @since 3.5
   */
  public IExecutionEnvironment[] getSubEnvironments();

  /**
   * Returns a map of Eclipse Java compiler options specified as default settings to use when
   * building with this profile, or <code>null</code> if unspecified.
   *
   * @return a map of Eclipse Java compiler options associated with this profile or <code>null
   *     </code>
   * @since 3.5
   */
  public Map<String, String> getComplianceOptions();
}

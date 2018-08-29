/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Broadcom Corporation - build
 * configurations and references
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import java.net.URI;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A project description contains the meta-data required to define a project. In effect, a project
 * description is a project's "content".
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProjectDescription {
  /**
   * Constant that denotes the name of the project description file (value <code>".project"</code>).
   * The handle of a project's description file is <code>project.getFile(DESCRIPTION_FILE_NAME)
   * </code>. The project description file is located in the root of the project's content area.
   *
   * @since 2.0
   */
  public static final String DESCRIPTION_FILE_NAME = ".project"; // $NON-NLS-1$

  /**
   * Returns the build configurations referenced by the specified configuration for the described
   * project.
   *
   * <p>These references are persisted by the workspace in a private location outside the project
   * description file, and as such will not be shared when a project is exported or persisted in a
   * repository. As such clients are always responsible for setting these references when a project
   * is created or recreated.
   *
   * <p>The referenced build configurations need not exist in the workspace. The result will not
   * contain duplicates. The order of the references is preserved from the call to {@link
   * #setBuildConfigReferences(String, IBuildConfiguration[])}. Returns an empty array if the
   * provided config doesn't dynamically reference any other build configurations, or the given
   * config does not exist in this description.
   *
   * @param configName the configuration in the described project to get the references for
   * @return a list of dynamic build configurations
   * @see #setBuildConfigReferences(String, IBuildConfiguration[])
   * @since 3.7
   */
  public IBuildConfiguration[] getBuildConfigReferences(String configName);

  /**
   * Returns the list of build commands to run when building the described project. The commands are
   * listed in the order in which they are to be run.
   *
   * @return the list of build commands for the described project
   */
  public ICommand[] getBuildSpec();

  /**
   * Returns the descriptive comment for the described project.
   *
   * @return the comment for the described project
   */
  public String getComment();

  /**
   * Returns the dynamic project references for the described project. Dynamic project references
   * can be used instead of simple project references in cases where the reference information is
   * computed dynamically be a third party. These references are persisted by the workspace in a
   * private location outside the project description file, and as such will not be shared when a
   * project is exported or persisted in a repository. A client using project references is always
   * responsible for setting these references when a project is created or recreated.
   *
   * <p>The returned projects need not exist in the workspace. The result will not contain
   * duplicates. Returns an empty array if there are no dynamic project references on this
   * description.
   *
   * @see #getBuildConfigReferences(String)
   * @see #getReferencedProjects()
   * @see #setDynamicReferences(IProject[])
   * @return a list of projects
   * @since 3.0
   */
  public IProject[] getDynamicReferences();

  /**
   * Returns the local file system location for the described project. The path will be either an
   * absolute file system path, or a relative path whose first segment is the name of a workspace
   * path variable. <code>null</code> is returned if the default location should be used. This
   * method will return <code>null</code> if this project is not located in the local file system.
   *
   * @return the location for the described project or <code>null</code>
   * @deprecated Since 3.2, project locations are not necessarily in the local file system. The more
   *     general {@link #getLocationURI()} method should be used instead.
   */
  @Deprecated
  public IPath getLocation();

  /**
   * Returns the location URI for the described project. <code>null</code> is returned if the
   * default location should be used.
   *
   * @return the location for the described project or <code>null</code>
   * @since 3.2
   * @see #setLocationURI(URI)
   */
  public URI getLocationURI();

  /**
   * Returns the name of the described project.
   *
   * @return the name of the described project
   */
  public String getName();

  /**
   * Returns the list of natures associated with the described project. Returns an empty array if
   * there are no natures on this description.
   *
   * @return the list of natures for the described project
   * @see #setNatureIds(String[])
   */
  public String[] getNatureIds();

  /**
   * Returns the projects referenced by the described project. These references are persisted in the
   * project description file (&quot;.project&quot;) and as such will be shared whenever the project
   * is exported to another workspace. For references that are likely to change from one workspace
   * to another, dynamic references should be used instead.
   *
   * <p>The projects need not exist in the workspace. The result will not contain duplicates.
   * Returns an empty array if there are no referenced projects on this description.
   *
   * @see #getDynamicReferences()
   * @see #getBuildConfigReferences(String)
   * @return a list of projects
   */
  public IProject[] getReferencedProjects();

  /**
   * Returns whether the project nature specified by the given nature extension id has been added to
   * the described project.
   *
   * @param natureId the nature extension identifier
   * @return <code>true</code> if the described project has the given nature
   */
  public boolean hasNature(String natureId);

  /**
   * Returns a new build command.
   *
   * <p>Note that the new command does not become part of this project description's build spec
   * until it is installed via the <code>setBuildSpec</code> method.
   *
   * @return a new command
   * @see #setBuildSpec(ICommand[])
   */
  public ICommand newCommand();

  /**
   * Sets the active configuration for the described project.
   *
   * <p>If a configuration with the specified name does not exist in the project then the first
   * configuration in the project is treated as the active configuration.
   *
   * @param configName the configuration to set as the active or default
   * @since 3.7
   */
  public void setActiveBuildConfig(String configName);

  /**
   * Sets the build configurations for the described project.
   *
   * <p>The passed in names must all be non-null. Before they are set, duplicates are removed.
   *
   * <p>All projects have one default build configuration, and it is impossible to configure the
   * project to have no build configurations. If the input is null or an empty list, the current
   * configurations are removed, and a default build configuration is (re-)added.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @param configNames the configurations to set for the described project
   * @see IProject#getActiveBuildConfig()
   * @see IProject#getBuildConfigs()
   * @see IProjectDescription#setActiveBuildConfig(String)
   * @since 3.7
   */
  public void setBuildConfigs(String[] configNames);

  /**
   * Sets the build configurations referenced by the specified configuration.
   *
   * <p>The configuration to which references are being added needs to exist in this description,
   * but the referenced projects and build configurations need not exist. A reference with <code>
   * null</code> configuration name is resolved to the active build configuration on use. Duplicates
   * will be removed. The order of the referenced build configurations is preserved. If the given
   * configuration does not exist in this description then this has no effect.
   *
   * <p>References at the build configuration level take precedence over references at the project
   * level.
   *
   * <p>Like dynamic references, these build configuration references are persisted as part of
   * workspace metadata.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @see #getBuildConfigReferences(String)
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @param configName the configuration in the described project to set the references for
   * @param references list of build configuration references
   * @since 3.7
   */
  public void setBuildConfigReferences(String configName, IBuildConfiguration[] references);

  /**
   * Sets the list of build command to run when building the described project.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @param buildSpec the array of build commands to run
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @see #getBuildSpec()
   * @see #newCommand()
   */
  public void setBuildSpec(ICommand[] buildSpec);

  /**
   * Sets the comment for the described project.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @param comment the comment for the described project
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @see #getComment()
   */
  public void setComment(String comment);

  /**
   * Sets the dynamic project references for the described project. The projects need not exist in
   * the workspace. Duplicates will be removed.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @see #getDynamicReferences()
   * @see #setBuildConfigReferences(String, IBuildConfiguration[])
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @param projects list of projects
   * @since 3.0
   */
  public void setDynamicReferences(IProject[] projects);

  /**
   * Sets the local file system location for the described project. The path must be either an
   * absolute file system path, or a relative path whose first segment is the name of a defined
   * workspace path variable. If <code>null</code> is specified, the default location is used.
   *
   * <p>Setting the location on a description for a project which already exists has no effect; the
   * new project location is ignored when the description is set on the already existing project.
   * This method is intended for use on descriptions for new projects or for destination projects
   * for <code>copy</code> and <code>move</code>.
   *
   * <p>This operation maps the root folder of the project to the exact location provided. For
   * example, if the location for project named "P" is set to the path c:\my_plugins\Project1, the
   * file resource at workspace path /P/index.html would be stored in the local file system at
   * c:\my_plugins\Project1\index.html.
   *
   * @param location the location for the described project or <code>null</code>
   * @see #getLocation()
   */
  public void setLocation(IPath location);

  /**
   * Sets the location for the described project. If <code>null</code> is specified, the default
   * location is used.
   *
   * <p>Setting the location on a description for a project which already exists has no effect; the
   * new project location is ignored when the description is set on the already existing project.
   * This method is intended for use on descriptions for new projects or for destination projects
   * for <code>copy</code> and <code>move</code>.
   *
   * <p>This operation maps the root folder of the project to the exact location provided. For
   * example, if the location for project named "P" is set to the URI file://c:/my_plugins/Project1,
   * the file resource at workspace path /P/index.html would be stored in the local file system at
   * file://c:/my_plugins/Project1/index.html.
   *
   * @param location the location for the described project or <code>null</code>
   * @see #getLocationURI()
   * @see IWorkspace#validateProjectLocationURI(IProject, URI)
   * @since 3.2
   */
  public void setLocationURI(URI location);

  /**
   * Sets the name of the described project.
   *
   * <p>Setting the name on a description and then setting the description on the project has no
   * effect; the new name is ignored.
   *
   * <p>Creating a new project with a description name which doesn't match the project handle name
   * results in the description name being ignored; the project will be created using the name in
   * the handle.
   *
   * @param projectName the name of the described project
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @see #getName()
   */
  public void setName(String projectName);

  /**
   * Sets the list of natures associated with the described project. A project created with this
   * description will have these natures added to it in the given order.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @param natures the list of natures
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @see #getNatureIds()
   */
  public void setNatureIds(String[] natures);

  /**
   * Sets the referenced projects, ignoring any duplicates. The order of projects is preserved. The
   * projects need not exist in the workspace.
   *
   * <p>Users must call {@link IProject#setDescription(IProjectDescription, int, IProgressMonitor)}
   * before changes made to this description take effect.
   *
   * @param projects a list of projects
   * @see IProject#setDescription(IProjectDescription, int, IProgressMonitor)
   * @see #setBuildConfigReferences(String, IBuildConfiguration[])
   * @see #getReferencedProjects()
   */
  public void setReferencedProjects(IProject[] projects);
}

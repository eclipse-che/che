/**
 * ***************************************************************************** Copyright (c)
 * 2012-2018 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.core.internal.resources.Workspace;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class ResourcesPlugin {

  /**
   * Unique identifier constant (value <code>"org.eclipse.core.resources"</code>) for the standard
   * Resources plug-in.
   */
  public static final String PI_RESOURCES = "org.eclipse.core.resources"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring whether the workspace performs auto- refresh. Auto-refresh
   * installs a file-system listener, or performs periodic file-system polling to actively discover
   * changes in the resource hierarchy.
   *
   * @since 3.0
   */
  public static final String PREF_AUTO_REFRESH = "refresh.enabled"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring whether out-of-sync resources are automatically
   * asynchronously refreshed, when discovered to be out-of-sync by the workspace.
   *
   * <p>This preference suppresses out-of-sync CoreException for some read methods, including:
   * {@link IFile#getContents()} & {@link IFile#getContentDescription()}.
   *
   * <p>In the future the workspace may enable other lightweight auto-refresh mechanisms when this
   * preference is true. (The existing {@link ResourcesPlugin#PREF_AUTO_REFRESH} will continue to
   * enable filesystem hooks and the existing polling based monitor.) See the discussion:
   * https://bugs.eclipse.org/303517
   *
   * @since 3.7
   */
  public static final String PREF_LIGHTWEIGHT_AUTO_REFRESH =
      "refresh.lightweight.enabled"; // $NON-NLS-1$
  /**
   * Name of a preference indicating the encoding to use when reading text files in the workspace.
   * The value is a string, and may be the default empty string, indicating that the file system
   * encoding should be used instead. The file system encoding can be retrieved using <code>
   * System.getProperty("file.encoding")</code>. There is also a convenience method <code>
   * getEncoding</code> which returns the value of this preference, or the file system encoding if
   * this preference is not set.
   *
   * <p>Note that there is no guarantee that the value is a supported encoding. Callers should be
   * prepared to handle <code>UnsupportedEncodingException</code> where this encoding is used.
   *
   * @see #getEncoding()
   * @see java.io.UnsupportedEncodingException
   */
  public static final String PREF_ENCODING = "encoding"; // $NON-NLS-1$

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesPlugin.class);
  /**
   * Common prefix for workspace preference names.
   *
   * @since 2.1
   */
  private static final String PREF_DESCRIPTION_PREFIX = "description."; // $NON-NLS-1$

  /**
   * Name of a preference for configuring whether the workspace performs auto- builds.
   *
   * @see IWorkspaceDescription#isAutoBuilding()
   * @see IWorkspaceDescription#setAutoBuilding(boolean)
   * @since 2.1
   */
  public static final String PREF_AUTO_BUILDING =
      PREF_DESCRIPTION_PREFIX + "autobuilding"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the maximum number of times that the workspace should
   * rebuild when builders affect projects that have already been built.
   *
   * @see IWorkspaceDescription#getMaxBuildIterations()
   * @see IWorkspaceDescription#setMaxBuildIterations(int)
   * @since 2.1
   */
  public static final String PREF_MAX_BUILD_ITERATIONS =
      PREF_DESCRIPTION_PREFIX + "maxbuilditerations"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring whether to apply the specified history size policy.
   *
   * @see IWorkspaceDescription#isApplyFileStatePolicy()
   * @see IWorkspaceDescription#setApplyFileStatePolicy(boolean)
   * @since 3.6
   */
  public static final String PREF_APPLY_FILE_STATE_POLICY =
      PREF_DESCRIPTION_PREFIX + "applyfilestatepolicy"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the maximum number of milliseconds a file state should be
   * kept in the local history
   *
   * @see IWorkspaceDescription#getFileStateLongevity()
   * @see IWorkspaceDescription#setFileStateLongevity(long)
   * @since 2.1
   */
  public static final String PREF_FILE_STATE_LONGEVITY =
      PREF_DESCRIPTION_PREFIX + "filestatelongevity"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the maximum number of states per file that can be stored
   * in the local history.
   *
   * @see IWorkspaceDescription#getMaxFileStates()
   * @see IWorkspaceDescription#setMaxFileStates(int)
   * @since 2.1
   */
  public static final String PREF_MAX_FILE_STATES =
      PREF_DESCRIPTION_PREFIX + "maxfilestates"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the maximum permitted size of a file to be stored in the
   * local history
   *
   * @see IWorkspaceDescription#getMaxFileStateSize()
   * @see IWorkspaceDescription#setMaxFileStateSize(long)
   * @since 2.1
   */
  public static final String PREF_MAX_FILE_STATE_SIZE =
      PREF_DESCRIPTION_PREFIX + "maxfilestatesize"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the amount of time in milliseconds between automatic
   * workspace snapshots
   *
   * @see IWorkspaceDescription#getSnapshotInterval()
   * @see IWorkspaceDescription#setSnapshotInterval(long)
   * @since 2.1
   */
  public static final String PREF_SNAPSHOT_INTERVAL =
      PREF_DESCRIPTION_PREFIX + "snapshotinterval"; // $NON-NLS-1$

  /**
   * Name of a preference for turning off support for linked resources. When this preference is set
   * to "true", attempting to create linked resources will fail.
   *
   * @since 2.1
   */
  public static final String PREF_DISABLE_LINKING =
      PREF_DESCRIPTION_PREFIX + "disableLinking"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring the order projects in the workspace are built.
   *
   * @see IWorkspaceDescription#getBuildOrder()
   * @see IWorkspaceDescription#setBuildOrder(String[])
   * @since 2.1
   */
  public static final String PREF_BUILD_ORDER =
      PREF_DESCRIPTION_PREFIX + "buildorder"; // $NON-NLS-1$
  /**
   * Name of a preference for configuring whether to use the workspace's default order for building
   * projects.
   *
   * @since 2.1
   */
  public static final String PREF_DEFAULT_BUILD_ORDER =
      PREF_DESCRIPTION_PREFIX + "defaultbuildorder"; // $NON-NLS-1$

  /**
   * The workspace managed by the single instance of this plug-in runtime class, or <code>null
   * </code> is there is none.
   */
  private static Workspace workspace = null;

  private static String indexPath;
  private static String workspacePath;
  private static String pluginId;

  @Inject
  public ResourcesPlugin(
      @Named("che.jdt.workspace.index.dir") String indexPath,
      RootDirPathProvider pathProvider,
      Provider<ProjectManager> projectManagerProvider,
      Provider<PathTransformer> pathTransformerProvider,
      Provider<FsManager> fsManagerProvider) {
    ResourcesPlugin.indexPath = indexPath;
    ResourcesPlugin.workspacePath = pathProvider.get();
    pluginId = "cheWsPlugin";
    EFS.setWsPath(workspacePath);
    workspace =
        new Workspace(
            workspacePath, projectManagerProvider, pathTransformerProvider, fsManagerProvider);
  }

  public static String getPathToWorkspace() {
    return workspacePath;
  }

  public static String getIndexPath() {
    return indexPath;
  }

  public static String getPluginId() {
    return pluginId;
  }

  /**
   * Returns the workspace. The workspace is not accessible after the resources plug-in has
   * shutdown.
   *
   * @return the workspace that was created by the single instance of this plug-in class.
   */
  public static IWorkspace getWorkspace() {
    if (workspace == null) {
      throw new IllegalStateException(Messages.resources_workspaceClosed);
    }
    return workspace;
  }

  public static void log(Exception e) {
    LOG.error(e.getMessage(), e);
  }

  public static void log(IStatus status) {
    LOG.error(status.getMessage(), status.getException());
  }

  /**
   * Returns the encoding to use when reading text files in the workspace. This is the value of the
   * <code>PREF_ENCODING</code> preference, or the file system encoding (<code>
   * System.getProperty("file.encoding")</code>) if the preference is not set.
   *
   * <p>Note that this method does not check whether the result is a supported encoding. Callers
   * should be prepared to handle <code>UnsupportedEncodingException</code> where this encoding is
   * used.
   *
   * @return the encoding to use when reading text files in the workspace
   * @see java.io.UnsupportedEncodingException
   */
  public static String getEncoding() {
    String enc = null; // getPlugin().getPluginPreferences().getString(PREF_ENCODING);
    //        if (enc == null || enc.length() == 0) {
    enc = System.getProperty("file.encoding"); // $NON-NLS-1$
    //        }
    return enc;
  }

  @PostConstruct
  public void start() {}
}

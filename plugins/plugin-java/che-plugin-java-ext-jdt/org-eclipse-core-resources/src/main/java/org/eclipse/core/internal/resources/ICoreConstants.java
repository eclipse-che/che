/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Serge Beauchamp (Freescale
 * Semiconductor) - [229633] Group Support Broadcom Corporation - build configurations
 * *****************************************************************************
 */
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.QualifiedName;

public interface ICoreConstants {

  // Standard resource properties
  /** map of builders to their last built state. */
  public static final QualifiedName K_BUILD_LIST =
      new QualifiedName(ResourcesPlugin.PI_RESOURCES, "BuildMap"); // $NON-NLS-1$

  /** Command line argument indicating a workspace refresh on startup is requested. */
  public static final String REFRESH_ON_STARTUP = "-refresh"; // $NON-NLS-1$

  // resource info constants
  static final long I_NULL_SYNC_INFO = -1;

  // Useful flag masks for resource info states
  static final int M_OPEN = 0x1;
  static final int M_LOCAL_EXISTS = 0x2;
  static final int M_PHANTOM = 0x8;
  static final int M_USED = 0x10;
  static final int M_TYPE = 0xF00;
  static final int M_TYPE_START = 8;
  static final int M_MARKERS_SNAP_DIRTY = 0x1000;
  static final int M_SYNCINFO_SNAP_DIRTY = 0x2000;
  /**
   * Marks this resource as derived.
   *
   * @since 2.0
   */
  static final int M_DERIVED = 0x4000;
  /**
   * Marks this resource as a team-private member of its container.
   *
   * @since 2.0
   */
  static final int M_TEAM_PRIVATE_MEMBER = 0x8000;
  /**
   * Marks this resource as a hidden resource.
   *
   * @since 3.4
   */
  static final int M_HIDDEN = 0x200000;

  /**
   * Marks this resource as a linked resource.
   *
   * @since 2.1
   */
  static final int M_LINK = 0x10000;
  /**
   * Marks this resource as virtual.
   *
   * @since 3.6
   */
  static final int M_VIRTUAL = 0x80000;
  /**
   * The file has no content description.
   *
   * @since 3.0
   */
  static final int M_NO_CONTENT_DESCRIPTION = 0x20000;
  /**
   * The file has a default content description.
   *
   * @since 3.0
   */
  static final int M_DEFAULT_CONTENT_DESCRIPTION = 0x40000;

  /**
   * Marks this resource as having undiscovered children
   *
   * @since 3.1
   */
  static final int M_CHILDREN_UNKNOWN = 0x100000;

  /**
   * Set of flags that should be cleared when the contents for a file change.
   *
   * @since 3.0
   */
  static final int M_CONTENT_CACHE = M_NO_CONTENT_DESCRIPTION | M_DEFAULT_CONTENT_DESCRIPTION;

  static final int NULL_FLAG = -1;

  /**
   * A private preference stored in a preference node to indicate the preference version that is
   * used. This version identifier is used to handle preference migration when old preferences are
   * loaded.
   */
  public static final String PREF_VERSION_KEY = "version"; // $NON-NLS-1$

  /**
   * A private preference stored in a preference node to indicate the preference version that is
   * used. This version identifier is used to handle preference migration when old preferences are
   * loaded.
   */
  public static final String PREF_VERSION = "1"; // $NON-NLS-1$

  // Internal status codes
  // Information Only [00-24]
  // Warnings [25-74]
  public static final int CRASH_DETECTED = 10035;

  // Errors [75-99]

  public static final int PROJECT_SEGMENT_LENGTH = 1;
  public static final int MINIMUM_FOLDER_SEGMENT_LENGTH = 2;
  public static final int MINIMUM_FILE_SEGMENT_LENGTH = 2;

  public static final int WORKSPACE_TREE_VERSION_1 = 67305985;
  public static final int WORKSPACE_TREE_VERSION_2 = 67305986;

  // helper constants for empty structures
  public static final IBuildConfiguration[] EMPTY_BUILD_CONFIG_ARRAY = new IBuildConfiguration[0];
  public static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
  public static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
  public static final IFileState[] EMPTY_FILE_STATES = new IFileState[0];
}

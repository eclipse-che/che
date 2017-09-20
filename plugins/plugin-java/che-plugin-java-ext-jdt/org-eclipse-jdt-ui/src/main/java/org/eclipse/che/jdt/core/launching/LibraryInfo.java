/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

/** Stores the boot path and extension directories associated with a VM. */
public class LibraryInfo {

  private String fVersion;
  private String[] fBootpath;
  private String[] fExtensionDirs;
  private String[] fEndorsedDirs;

  public LibraryInfo(String version, String[] bootpath, String[] extDirs, String[] endDirs) {
    fVersion = version;
    fBootpath = bootpath;
    fExtensionDirs = extDirs;
    fEndorsedDirs = endDirs;
  }

  /**
   * Returns the version of this VM install.
   *
   * @return version
   */
  public String getVersion() {
    return fVersion;
  }

  /**
   * Returns a collection of extension directory paths for this VM install.
   *
   * @return a collection of absolute paths
   */
  public String[] getExtensionDirs() {
    return fExtensionDirs;
  }

  /**
   * Returns a collection of bootpath entries for this VM install.
   *
   * @return a collection of absolute paths
   */
  public String[] getBootpath() {
    return fBootpath;
  }

  /**
   * Returns a collection of endorsed directory paths for this VM install.
   *
   * @return a collection of absolute paths
   */
  public String[] getEndorsedDirs() {
    return fEndorsedDirs;
  }
}

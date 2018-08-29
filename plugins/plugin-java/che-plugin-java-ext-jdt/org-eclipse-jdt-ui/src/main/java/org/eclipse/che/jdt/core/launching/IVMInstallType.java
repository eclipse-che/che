/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2007 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

import java.io.File;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents a particular type of VM for which there may be any number of VM installations. An
 * example of a VM type is the standard JRE which might have instances corresponding to different
 * installed versions such as JRE 1.2.2 and JRE 1.3.
 *
 * <p>This interface is intended to be implemented by clients that contribute to the <code>
 * "org.eclipse.jdt.launching.vmInstallTypes"</code> extension point.
 */
public interface IVMInstallType {
  //	/**
  //	 * Creates a new instance of this VM Install type.
  //	 * The newly created IVMInstall is managed by this IVMInstallType.
  //	 *
  //	 * @param	id	An id String that must be unique within this IVMInstallType.
  //	 *
  //	 * @return the newly created VM instance
  //	 *
  //	 * @throws	IllegalArgumentException	If the id exists already.
  //	 */
  //	IVMInstall createVMInstall(String id);
  //	/**
  //	 * Finds the VM with the given id.
  //	 *
  //	 * @param id the VM id
  //	 * @return a VM instance, or <code>null</code> if not found
  //	 */
  //	IVMInstall findVMInstall(String id);
  //	/**
  //	 * Finds the VM with the given name.
  //	 *
  //	 * @param name the VM name
  //	 * @return a VM instance, or <code>null</code> if not found
  //	 * @since 2.0
  //	 */
  //	IVMInstall findVMInstallByName(String name);
  //
  //	/**
  //	 * Remove the VM associated with the given id from the set of VMs managed by
  //	 * this VM type. Has no effect if a VM with the given id is not currently managed
  //	 * by this type.
  //	 * A VM install that is disposed may not be used anymore.
  //	 *
  //	 * @param id the id of the VM to be disposed.
  //	 */
  //	void disposeVMInstall(String id);
  //	/**
  //	 * Returns all VM instances managed by this VM type.
  //	 *
  //	 * @return the list of VM instances managed by this VM type
  //	 */
  //	IVMInstall[] getVMInstalls();

  /**
   * Returns the display name of this VM type.
   *
   * @return the name of this IVMInstallType
   */
  String getName();

  /**
   * Returns the globally unique id of this VM type. Clients are responsible for providing a unique
   * id.
   *
   * @return the id of this IVMInstallType
   */
  String getId();

  /**
   * Validates the given location of a VM installation.
   *
   * <p>For example, an implementation might check whether the VM executable is present.
   *
   * @param installLocation the root directory of a potential installation for this type of VM
   * @return a status object describing whether the install location is valid
   */
  IStatus validateInstallLocation(File installLocation);

  /**
   * Tries to detect an installed VM that matches this VM install type. Typically, this method will
   * detect the VM installation the Eclipse platform runs on. Implementers should return <code>null
   * </code> if they can't assure that a given vm install matches this IVMInstallType.
   *
   * @return The location of an VM installation that can be used with this VM install type, or
   *     <code>null</code> if unable to locate an installed VM.
   */
  File detectInstallLocation();

  /**
   * Returns a collection of <code>LibraryLocation</code>s that represent the default system
   * libraries of this VM install type, if a VM was installed at the given <code>installLocation
   * </code>. The returned <code>LibraryLocation</code>s may not exist if the <code>installLocation
   * </code> is not a valid install location.
   *
   * @param installLocation home location
   * @return default library locations based on the given <code>installLocation</code>.
   * @see LibraryLocation
   * @see IVMInstallType#validateInstallLocation(java.io.File)
   * @since 2.0
   */
  LibraryLocation[] getDefaultLibraryLocations(File installLocation);
}

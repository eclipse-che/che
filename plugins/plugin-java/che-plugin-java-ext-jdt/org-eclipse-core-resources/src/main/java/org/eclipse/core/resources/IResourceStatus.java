/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.*;

/**
 * Represents status related to resources in the Resources plug-in and
 * defines the relevant status code constants.
 * Status objects created by the Resources plug-in bear its unique id
 * (<code>ResourcesPlugin.PI_RESOURCES</code>) and one of
 * these status codes.
 *
 * @see org.eclipse.core.runtime.IStatus
 * @see ResourcesPlugin#PI_RESOURCES
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IResourceStatus extends IStatus {

	/*
	 * Status code definitions
	 */

	// General constants [0-98]
	// Information Only [0-32]
	// Warnings [33-65]
	/** Status code constant (value 35) indicating that a given 
	 * nature set does not satisfy its constraints.
	 * Severity: warning. Category: general.
	 */
	public static final int INVALID_NATURE_SET = 35;

	// Errors [66-98]

	/** Status code constant (value 75) indicating that a builder failed.
	 * Severity: error. Category: general.
	 */
	public static final int BUILD_FAILED = 75;

	/** Status code constant (value 76) indicating that an operation failed.
	 * Severity: error. Category: general.
	 */
	public static final int OPERATION_FAILED = 76;

	/** Status code constant (value 77) indicating an invalid value.
	 * Severity: error. Category: general.
	 */
	public static final int INVALID_VALUE = 77;

	// Local file system constants [200-298]
	// Information Only [200-232]

	// Warnings [233-265]

	/** Status code constant (value 234) indicating that a project
	 * description file (.project), was missing but it has been repaired.
	 * Severity: warning. Category: local file system.
	 */
	public static final int MISSING_DESCRIPTION_REPAIRED = 234;

	/** Status code constant (value 235) indicating the local file system location
	 * for a resource overlaps the location of another resource.
	 * Severity: warning. Category: local file system.
	 */
	public static final int OVERLAPPING_LOCATION = 235;

	// Errors [266-298]

	/** Status code constant (value 268) indicating a resource unexpectedly 
	 * exists on the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int EXISTS_LOCAL = 268;

	/** Status code constant (value 269) indicating a resource unexpectedly 
	 * does not exist on the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int NOT_FOUND_LOCAL = 269;

	/** Status code constant (value 270) indicating the local file system location for
	 * a resource could not be computed. 
	 * Severity: error. Category: local file system.
	 */
	public static final int NO_LOCATION_LOCAL = 270;

	/** Status code constant (value 271) indicating an error occurred while
	 * reading part of a resource from the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int FAILED_READ_LOCAL = 271;

	/** Status code constant (value 272) indicating an error occurred while
	 * writing part of a resource to the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int FAILED_WRITE_LOCAL = 272;

	/** Status code constant (value 273) indicating an error occurred while
	 * deleting a resource from the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int FAILED_DELETE_LOCAL = 273;

	/** Status code constant (value 274) indicating the workspace view of
	 * the resource differs from that of the local file system.  The requested
	 * operation has been aborted to prevent the possible loss of data.
	 * Severity: error. Category: local file system.
	 */
	public static final int OUT_OF_SYNC_LOCAL = 274;

	/** Status code constant (value 275) indicating this file system is not case
	 * sensitive and a resource that differs only in case unexpectedly exists on 
	 * the local file system.
	 * Severity: error. Category: local file system.
	 */
	public static final int CASE_VARIANT_EXISTS = 275;

	/** Status code constant (value 276) indicating a file exists in the
	 * file system but is not of the expected type (file instead of directory,
	 * or vice-versa).
	 * Severity: error. Category: local file system.
	 */
	public static final int WRONG_TYPE_LOCAL = 276;

	/** Status code constant (value 277) indicating that the parent
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: local file system.
	 * @since 2.1
	 */
	public static final int PARENT_READ_ONLY = 277;

	/** Status code constant (value 278) indicating a file exists in the
	 * file system but its name is not a valid resource name.
	 * Severity: error. Category: local file system.
	 */
	public static final int INVALID_RESOURCE_NAME = 278;

	/** Status code constant (value 279) indicating that the 
	 * file in the file system is marked as read-only.
	 * Severity: error. Category: local file system.
	 * @since 3.0
	 */
	public static final int READ_ONLY_LOCAL = 279;

	// Workspace constants [300-398]
	// Information Only [300-332]

	// Warnings [333-365]

	/** Status code constant (value 333) indicating that a workspace path 
	 * variable unexpectedly does not exist.
	 *  Severity: warning. Category: workspace.
	 * @since 2.1
	 */
	public static final int VARIABLE_NOT_DEFINED_WARNING = 333;

	// Errors [366-398]

	/** Status code constant (value 366) indicating a resource exists in the
	 * workspace but is not of the expected type.
	 * Severity: error. Category: workspace.
	 */
	public static final int RESOURCE_WRONG_TYPE = 366;

	/** Status code constant (value 367) indicating a resource unexpectedly 
	 * exists in the workspace.
	 * Severity: error. Category: workspace.
	 */
	public static final int RESOURCE_EXISTS = 367;

	/** Status code constant (value 368) indicating a resource unexpectedly 
	 * does not exist in the workspace.
	 * Severity: error. Category: workspace.
	 */
	public static final int RESOURCE_NOT_FOUND = 368;

	/** Status code constant (value 369) indicating a resource unexpectedly 
	 * does not have content local to the workspace.
	 * Severity: error. Category: workspace.
	 */
	public static final int RESOURCE_NOT_LOCAL = 369;

	/** Status code constant (value 370) indicating a workspace
	 * is unexpectedly closed.
	 * Severity: error. Category: workspace.
	 */
	public static final int WORKSPACE_NOT_OPEN = 370;

	/** Status code constant (value 372) indicating a project is
	 * unexpectedly closed.
	 * Severity: error. Category: workspace.
	 */
	public static final int PROJECT_NOT_OPEN = 372;

	/** Status code constant (value 374) indicating that the path
	 * of a resource being created is occupied by an existing resource
	 * of a different type.
	 * Severity: error. Category: workspace.
	 */
	public static final int PATH_OCCUPIED = 374;

	/** Status code constant (value 375) indicating that the sync partner
	 * is not registered with the workspace synchronizer.
	 * Severity: error. Category: workspace.
	 */
	public static final int PARTNER_NOT_REGISTERED = 375;

	/** Status code constant (value 376) indicating a marker unexpectedly 
	 * does not exist in the workspace tree.
	 * Severity: error. Category: workspace.
	 */
	public static final int MARKER_NOT_FOUND = 376;

	/** Status code constant (value 377) indicating a resource is 
	 * unexpectedly not a linked resource.
	 * Severity: error. Category: workspace.
	 * @since 2.1
	 */
	public static final int RESOURCE_NOT_LINKED = 377;

	/** Status code constant (value 378) indicating that linking is
	 * not permitted on a certain project.
	 * Severity: error. Category: workspace.
	 * @since 2.1
	 */
	public static final int LINKING_NOT_ALLOWED = 378;

	/** Status code constant (value 379) indicating that a workspace path 
	 * variable unexpectedly does not exist.
	 *  Severity: error. Category: workspace.
	 * @since 2.1
	 */
	public static final int VARIABLE_NOT_DEFINED = 379;

	/** Status code constant (value 380) indicating that an attempt was made to modify 
	 * the workspace while it was locked.  Resource changes are disallowed
	 * during certain types of resource change event notification. 
	 * Severity: error. Category: workspace.
	 * @see IResourceChangeEvent
	 * @since 2.1
	 */
	public static final int WORKSPACE_LOCKED = 380;
	
	/** Status code constant (value 381) indicating that a problem occurred while
	 * retrieving the content description for a resource.
	 * Severity: error. Category: workspace.
	 * @see IFile#getContentDescription
	 * @since 3.0 
	 */
	public static final int FAILED_DESCRIBING_CONTENTS = 381;	

	/** Status code constant (value 382) indicating that a problem occurred while
	 * setting the charset for a resource.
	 * Severity: error. Category: workspace.
	 * @see IContainer#setDefaultCharset(String, IProgressMonitor)
	 * @see IFile#setCharset(String, IProgressMonitor)
	 * @since 3.0 
	 */
	public static final int FAILED_SETTING_CHARSET = 382;
	
	/** Status code constant (value 383) indicating that a problem occurred while
	 * getting the charset for a resource.
	 * Severity: error. Category: workspace.
	 * @since 3.0 
	 */
	public static final int FAILED_GETTING_CHARSET = 383;	

	/** Status code constant (value 384) indicating a build configuration with
	 * the specified ID unexpectedly does not exist.
	 * Severity: error. Category: workspace.
	 * @since 3.7
	 */
	public static final int BUILD_CONFIGURATION_NOT_FOUND = 384;

	// Internal constants [500-598]
	// Information Only [500-532]

	// Warnings [533-565]

	// Errors [566-598]

	/** Status code constant (value 566) indicating an error internal to the
	 * platform has occurred.
	 * Severity: error. Category: internal.
	 */
	public static final int INTERNAL_ERROR = 566;

	/** Status code constant (value 567) indicating the platform could not read
	 * some of its metadata.
	 * Severity: error. Category: internal.
	 */
	public static final int FAILED_READ_METADATA = 567;

	/** Status code constant (value 568) indicating the platform could not write
	 * some of its metadata.
	 * Severity: error. Category: internal.
	 */
	public static final int FAILED_WRITE_METADATA = 568;

	/** Status code constant (value 569) indicating the platform could not delete
	 * some of its metadata.
	 * Severity: error. Category: internal.
	 */
	public static final int FAILED_DELETE_METADATA = 569;

	/**
	 * Returns the path of the resource associated with this status.
	 *
	 * @return the path of the resource related to this status
	 */
	public IPath getPath();
}

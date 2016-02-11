/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Markers are a general mechanism for associating notes and meta-data with
 * resources.
 * <p>
 * Markers themselves are handles in the same way as <code>IResources</code>
 * are handles.  Instances of <code>IMarker</code> do not hold the attributes
 * themselves but rather uniquely refer to the attribute container.  As such,
 * their state may change underneath the handle with no warning to the holder
 * of the handle.
 * </p>
 * The Resources plug-in provides a general framework for 
 * defining and manipulating markers and provides several standard marker types.
 * </p>
 * <p>
 * Each marker has:<ul>
 * <li> a type string, specifying its type (e.g. 
 *		<code>"org.eclipse.core.resources.taskmarker"</code>), </li>
 * <li> an identifier which is unique (relative to a particular resource)</li>
 * </ul>
 * Specific types of markers may carry additional information.
 * </p>
 * <p>
 * The resources plug-in defines five standard types:
 * <ul>
 * <li><code>org.eclipse.core.resources.marker</code></li>
 * <li><code>org.eclipse.core.resources.taskmarker</code></li>
 * <li><code>org.eclipse.core.resources.problemmarker</code></li>
 * <li><code>org.eclipse.core.resources.bookmark</code></li>
 * <li><code>org.eclipse.core.resources.textmarker</code></li>
 * </ul>
 * The plug-in also provides an extension point (
 * <code>org.eclipse.core.resources.markers</code>) into which other
 * plug-ins can install marker type declaration extensions.
 * </p>
 * <p>
 * Marker types are declared within a multiple inheritance type system.
 * New markers are defined in the <code>plugin.xml</code> file of the
 * declaring plug-in.  A valid declaration contains elements as defined by
 * the extension point DTD:
 * <ul>
 * <li><i>type</i> - the unique name of the marker type</li>
 * <li><i>super</i> - the list of marker types of which this marker is to be considered a sub-type</li>
 * <li><i>attributes</i> - the list of standard attributes which may be present on this type of marker</li>
 * <li><i>persistent</i> - whether markers of this type should be persisted by the platform</li>
 * </li>
 * </p>
 * <p>All markers declared as <code>persistent</code> are saved when the
 * workspace is saved, except those explicitly set as transient (the
 * <code>TRANSIENT</code> attribute is set as <code>true</code>). A plug-in
 * which defines a persistent marker is not directly involved in saving and
 * restoring the marker. Markers are not under version and configuration
 * management, and cannot be shared via VCM repositories.
 * </p>
 * <p>
 * Markers implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IMarker extends IAdaptable {

	/*====================================================================
	 * Marker types:
	 *====================================================================*/

	/** 
	 * Base marker type. 
	 *
	 * @see #getType()
	 */
	public static final String MARKER = ResourcesPlugin.PI_RESOURCES + ".marker"; //$NON-NLS-1$

	/** 
	 * Task marker type. 
	 *
	 * @see #getType()
	 */
	public static final String TASK = ResourcesPlugin.PI_RESOURCES + ".taskmarker"; //$NON-NLS-1$

	/** 
	 * Problem marker type. 
	 *
	 * @see #getType()
	 */
	public static final String PROBLEM = ResourcesPlugin.PI_RESOURCES + ".problemmarker"; //$NON-NLS-1$

	/** 
	 * Text marker type. 
	 *
	 * @see #getType()
	 */
	public static final String TEXT = ResourcesPlugin.PI_RESOURCES + ".textmarker"; //$NON-NLS-1$

	/** 
	 * Bookmark marker type. 
	 *
	 * @see #getType()
	 */
	public static final String BOOKMARK = ResourcesPlugin.PI_RESOURCES + ".bookmark"; //$NON-NLS-1$

	/*====================================================================
	 * Marker attributes:
	 *====================================================================*/

	/** 
	 * Severity marker attribute.  A number from the set of error, warning and info
	 * severities defined by the platform.
	 *
	 * @see #SEVERITY_ERROR
	 * @see #SEVERITY_WARNING
	 * @see #SEVERITY_INFO
	 * @see #getAttribute(String, int)
	 */
	public static final String SEVERITY = "severity"; //$NON-NLS-1$

	/** 
	 * Message marker attribute.  A localized string describing the nature
	 * of the marker (e.g., a name for a bookmark or task).  The content
	 * and form of this attribute is not specified or interpreted by the platform.
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String MESSAGE = "message"; //$NON-NLS-1$

	/** 
	 * Location marker attribute.  The location is a human-readable (localized) string which
	 * can be used to distinguish between markers on a resource.  As such it 
	 * should be concise and aimed at users.  The content and 
	 * form of this attribute is not specified or interpreted by the platform.
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String LOCATION = "location"; //$NON-NLS-1$

	/** 
	 * Priority marker attribute.  A number from the set of high, normal and low 
	 * priorities defined by the platform.
	 * 
	 * @see #PRIORITY_HIGH
	 * @see #PRIORITY_NORMAL
	 * @see #PRIORITY_LOW
	 * @see #getAttribute(String, int)
	 */
	public static final String PRIORITY = "priority"; //$NON-NLS-1$

	/** 
	 * Done marker attribute.  A boolean value indicating whether 
	 * the marker (e.g., a task) is considered done.  
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String DONE = "done"; //$NON-NLS-1$

	/** 
	 * Character start marker attribute.  An integer value indicating where a text
	 * marker starts.  This attribute is zero-relative and inclusive.
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String CHAR_START = "charStart"; //$NON-NLS-1$

	/** 
	 * Character end marker attribute.  An integer value indicating where a text
	 * marker ends.  This attribute is zero-relative and exclusive.
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String CHAR_END = "charEnd"; //$NON-NLS-1$

	/** 
	 * Line number marker attribute.  An integer value indicating the line number
	 * for a text marker.  This attribute is 1-relative.
	 *
	 * @see #getAttribute(String, String)
	 */
	public static final String LINE_NUMBER = "lineNumber"; //$NON-NLS-1$

	/** 
	 * Transient marker attribute.  A boolean value indicating whether the
	 * marker (e. g., a task) is considered transient even if its type is
	 * declared as persistent. 
	 *
	 * @see #getAttribute(String, String)
	 * @since 2.1 
	 */
	public static final String TRANSIENT = "transient"; //$NON-NLS-1$

	/** 
	 * User editable marker attribute.  A boolean value indicating whether a
	 * user should be able to manually change the marker (e.g. a task). The
	 * default is <code>true</code>. Note that the value of this attribute
	 * is to be used by the UI as a suggestion and its value will NOT be
	 * interpreted by Core in any manner and will not be enforced by Core 
	 * when performing any operations on markers.
	 *
	 * @see #getAttribute(String, String)
	 * @since 2.1 
	 */
	public static final String USER_EDITABLE = "userEditable"; //$NON-NLS-1$

	/**
	 * Source id attribute.  A string attribute that can be used by tools that
	 * generate markers to indicate the source of the marker. Use of this attribute is
	 * optional and its format or existence is not enforced.  This attribute is
	 * intended to improve serviceability by providing a value that product support
	 * personnel or automated tools can use to determine appropriate help and
	 * resolutions for markers.
	 * 
	 * @see #getAttribute(String, String)
	 * @since 3.3
	 */
	public static final String SOURCE_ID = "sourceId"; //$NON-NLS-1$

	/*====================================================================
	 * Marker attributes values:
	 *====================================================================*/

	/** 
	 * High priority constant (value 2).
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int PRIORITY_HIGH = 2;

	/** 
	 * Normal priority constant (value 1).
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int PRIORITY_NORMAL = 1;

	/** 
	 * Low priority constant (value 0).
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int PRIORITY_LOW = 0;

	/** 
	 * Error severity constant (value 2) indicating an error state.
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int SEVERITY_ERROR = 2;

	/** 
	 * Warning severity constant (value 1) indicating a warning.
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int SEVERITY_WARNING = 1;

	/** 
	 * Info severity constant (value 0) indicating information only.
	 *
	 * @see #getAttribute(String, int)
	 */
	public static final int SEVERITY_INFO = 0;

	/**
	 * Deletes this marker from its associated resource.  This method has no
	 * effect if this marker does not exist.
	 *
	 * @exception CoreException if this marker could not be deleted. Reasons include:
	 * <ul>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void delete() throws CoreException;

	/**
	 * Tests this marker for equality with the given object.
	 * Two markers are equal if their id and resource are both equal.
	 * 
	 * @param object the other object
	 * @return an indication of whether the objects are equal
	 */
	public boolean equals(Object object);

	/**
	 * Returns whether this marker exists in the workspace.  A marker
	 * exists if its resource exists and has a marker with the marker's id.
	 *
	 * @return <code>true</code> if this marker exists, otherwise
	 *    <code>false</code>
	 */
	public boolean exists();

	/**
	 * Returns the attribute with the given name.  The result is an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 * Returns <code>null</code> if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @return the value, or <code>null</code> if the attribute is undefined.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	public Object getAttribute(String attributeName) throws CoreException;

	/**
	 * Returns the integer-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 * or the marker does not exist or is not an integer value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public int getAttribute(String attributeName, int defaultValue);

	/**
	 * Returns the string-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a string value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public String getAttribute(String attributeName, String defaultValue);

	/**
	 * Returns the boolean-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a boolean value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue);

	/**
	 * Returns a map with all the attributes for the marker.
	 * If the marker has no attributes then <code>null</code> is returned.
	 *
	 * @return a map of attribute keys and values (key type : <code>String</code> 
	 *		value type : <code>String</code>, <code>Integer</code>, or 
	 *		<code>Boolean</code>) or <code>null</code>.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	public Map<String,Object> getAttributes() throws CoreException;

	/**
	 * Returns the attributes with the given names.  The result is an an array 
	 * whose elements correspond to the elements of the given attribute name
	 * array.  Each element is <code>null</code> or an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 *
	 * @param attributeNames the names of the attributes
	 * @return the values of the given attributes.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	public Object[] getAttributes(String[] attributeNames) throws CoreException;

	/**
	 * Returns the time at which this marker was created.
	 *
	 * @return the difference, measured in milliseconds, between the time at which
	 *    this marker was created and midnight, January 1, 1970 UTC, or <code>0L</code>
	 *    if the creation time is not known (this can occur in workspaces created using v2.0 or earlier).
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 * @since 2.1
	 */
	public long getCreationTime() throws CoreException;

	/**
	 * Returns the id of the marker.  The id of a marker is unique
	 * relative to the resource with which the marker is associated.
	 * Marker ids are not globally unique.
	 *
	 * @return the id of the marker
	 * @see IResource#findMarker(long)
	 */
	public long getId();

	/**
	 * Returns the resource with which this marker is associated. 
	 *
	 * @return the resource with which this marker is associated
	 */
	public IResource getResource();

	/**
	 * Returns the type of this marker. The returned marker type will not be 
	 * <code>null</code>.
	 *
	 * @return the type of this marker
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	public String getType() throws CoreException;

	/**
	 * Returns whether the type of this marker is considered to be a sub-type of
	 * the given marker type. 
	 *
	 * @return boolean <code>true</code>if the marker's type
	 *		is the same as (or a sub-type of) the given type.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * </ul>
	 */
	public boolean isSubtypeOf(String superType) throws CoreException;

	/**
	 * Sets the integer-valued attribute with the given name.  
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void setAttribute(String attributeName, int value) throws CoreException;

	/**
	 * Sets the attribute with the given name.  The value must be <code>null</code> or 
	 * an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * If the value is <code>null</code>, the attribute is considered to be undefined.
	 * 
	 * <p>
	 * The attribute value cannot be <code>String</code> 
	 * whose UTF encoding exceeds 65535 bytes. On persistent
	 * markers this limit is enforced by an assertion.
	 * </p>
	 * 
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 * 
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void setAttribute(String attributeName, Object value) throws CoreException;

	/**
	 * Sets the boolean-valued attribute with the given name.  
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void setAttribute(String attributeName, boolean value) throws CoreException;

	/**
	 * Sets the given attribute key-value pairs on this marker.
	 * The values must be <code>null</code> or an instance of 
	 * one of the following classes: <code>String</code>, 
	 * <code>Integer</code>, or <code>Boolean</code>.
	 * If a value is <code>null</code>, the new value of the 
	 * attribute is considered to be undefined.
	 * 
	 * <p>
	 * The values of the attributes cannot be <code>String</code> 
	 * whose UTF encoding exceeds 65535 bytes. On persistent markers
	 * this limit is enforced by an assertion. 
	 * </p>
	 * 
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributeNames an array of attribute names
	 * @param values an array of attribute values
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void setAttributes(String[] attributeNames, Object[] values) throws CoreException;

	/**
	 * Sets the attributes for this marker to be the ones contained in the
	 * given table. The values must be an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * Attributes previously set on the marker but not included in the given map
	 * are considered to be removals. Setting the given map to be <code>null</code>
	 * is equivalent to removing all marker attributes.
	 * 
	 * <p>
	 * The values of the attributes cannot be <code>String</code> 
	 * whose UTF encoding exceeds 65535 bytes. On persistent markers
	 * this limit is enforced by an assertion. 
	 * </p>
	 * 
	 * <p>
	 * This method changes resources; these changes will be reported
	 * in a subsequent resource change event, including an indication 
	 * that this marker has been modified.
	 * </p>
	 *
	 * @param attributes a map of attribute names to attribute values 
	 *		(key type : <code>String</code> value type : <code>String</code>, 
	 *		<code>Integer</code>, or <code>Boolean</code>) or <code>null</code>
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This marker does not exist.</li>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @see IResourceRuleFactory#markerRule(IResource)
	 */
	public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException;
}

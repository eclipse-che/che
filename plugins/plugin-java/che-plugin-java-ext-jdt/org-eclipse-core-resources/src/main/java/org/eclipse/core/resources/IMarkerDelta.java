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

import java.util.Map;

/**
 * A marker delta describes the change to a single marker.
 * A marker can either be added, removed or changed.
 * Marker deltas give access to the state of the marker as it
 * was (in the case of deletions and changes) before the modifying
 * operation occurred.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IMarkerDelta {
	/**
	 * Returns the object attribute with the given name.  The result is an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 * Returns <code>null</code> if the attribute is undefined.
	 * The set of valid attribute names is defined elsewhere.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 * @param attributeName the name of the attribute
	 * @return the value, or <code>null</code> if the attribute is undefined.
	 */
	public Object getAttribute(String attributeName);

	/**
	 * Returns the integer-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or is not an integer value.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if the attribute does not exist
	 * @return the value or the default value if the attribute is undefined.
	 */
	public int getAttribute(String attributeName, int defaultValue);

	/**
	 * Returns the string-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined or
	 * is not a string value.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if the attribute does not exist
	 * @return the value or the default value if the attribute is undefined.
	 */
	public String getAttribute(String attributeName, String defaultValue);

	/**
	 * Returns the boolean-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or is not a boolean value.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if the attribute does not exist
	 * @return the value or the default value if the attribute is undefined.
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue);

	/**
	 * Returns a Map with all the attributes for the marker.  The result is a Map
	 * whose keys are attributes names and whose values are attribute values.
	 * Each value an instance of one of the following classes: <code>String</code>, 
	 * <code>Integer</code>, or <code>Boolean</code>. If the marker has no
	 * attributes then <code>null</code> is returned.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @return a map of attribute keys and values (key type : <code>String</code> 
	 *		value type : <code>String</code>, <code>Integer</code>, or 
	 *		<code>Boolean</code>) or <code>null</code>.
	 */
	public Map<String, Object> getAttributes();

	/**
	 * Returns the attributes with the given names.  The result is an array 
	 * whose elements correspond to the elements of the given attribute name
	 * array.  Each element is <code>null</code> or an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @param attributeNames the names of the attributes
	 * @return the values of the given attributes.
	 */
	public Object[] getAttributes(String[] attributeNames);

	/**
	 * Returns the id of the marker.  The id of a marker is unique
	 * relative to the resource with which the marker is associated.
	 * Marker ids are not globally unique.
	 *
	 * @return the id of the marker
	 */
	public long getId();

	/**
	 * Returns the kind of this marker delta: 
	 * one of <code>IResourceDelta.ADDED</code>, 
	 * <code>IResourceDelta.REMOVED</code>, or <code>IResourceDelta.CHANGED</code>.
	 *
	 * @return the kind of marker delta
	 * @see IResourceDelta#ADDED
	 * @see IResourceDelta#REMOVED
	 * @see IResourceDelta#CHANGED
	 */
	public int getKind();

	/**
	 * Returns the marker described by this change.
	 * If kind is <code>IResourceDelta.REMOVED</code>, then this is the old marker,
	 * otherwise this is the new marker.  Note that if the marker was deleted,
	 * the value returned cannot be used to access attributes.
	 *
	 * @return the marker
	 */
	public IMarker getMarker();

	/**
	 * Returns the resource with which this marker is associated. 
	 *
	 * @return the resource
	 */
	public IResource getResource();

	/**
	 * Returns the type of this marker.
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @return the type of this marker
	 */
	public String getType();

	/**
	 * Returns whether the type of this marker is considered to be a sub-type of
	 * the given marker type. 
	 * <p>
	 * If kind is <code>IResourceDelta.ADDED</code>, then the information is 
	 * from the new marker, otherwise it is from the old marker.
	 * </p>
	 *
	 * @return boolean <code>true</code>if the marker's type
	 *		is the same as (or a sub-type of) the given type.
	 */
	public boolean isSubtypeOf(String superType);
}

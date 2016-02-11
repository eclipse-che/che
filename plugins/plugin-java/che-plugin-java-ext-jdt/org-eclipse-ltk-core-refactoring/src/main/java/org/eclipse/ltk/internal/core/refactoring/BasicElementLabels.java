/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.osgi.util.TextProcessor;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;


/**
 * A label provider for basic elements like paths. The label provider will make sure that the labels are correctly
 * shown in RTL environments.
 *
 * @since 3.4
 */
public class BasicElementLabels {

	/**
	 * User-readable string for separating post qualified names (e.g. " - ").
	 */
	public final static String CONCAT_STRING= RefactoringCoreMessages.BasicElementLabels_concat_string;
	
	private BasicElementLabels(){}

	/**
	 * Adds special marks so that that the given string is readable in a BIDI environment.
	 *
	 * @param string the string
	 * @param delimiters the additional delimiters
	 * @return the processed styled string
	 */
	private static String markLTR(String string, String delimiters) {
		return TextProcessor.process(string, delimiters);
	}

	/**
	 * Returns the label of a path.
	 *
	 * @param path the path
	 * @param isOSPath if <code>true</code>, the path represents an OS path, if <code>false</code> it is a workspace path.
	 * @return the label of the path to be used in the UI.
	 */
	public static String getPathLabel(IPath path, boolean isOSPath) {
		String label;
		if (isOSPath) {
			label= path.toOSString();
		} else {
			label= path.makeRelative().toString();
		}
		return markLTR(label, "/\\:."); //$NON-NLS-1$
	}

	/**
	 * Returns the label for a file pattern like '*.java'
	 *
	 * @param name the pattern
	 * @return the label of the pattern.
	 */
	public static String getFilePattern(String name) {
		return markLTR(name, "*.?/\\:."); //$NON-NLS-1$
	}

	/**
	 * Returns the label for a URL, URI or URL part. Example is 'http://www.x.xom/s.html#1'
	 *
	 * @param name the URL string
	 * @return the label of the URL.
	 */
	public static String getURLPart(String name) {
		return markLTR(name, ":@?-#/\\:."); //$NON-NLS-1$
	}

	/**
	 * Returns a label for a resource name.
	 *
	 * @param resource the resource
	 * @return the label of the resource name.
	 */
	public static String getResourceName(IResource resource) {
		return markLTR(resource.getName(), ":."); //$NON-NLS-1$
	}

	/**
	 * Returns a label for a resource name.
	 *
	 * @param resourceName the resource name
	 * @return the label of the resource name.
	 */
	public static String getResourceName(String resourceName) {
		return markLTR(resourceName, ":."); //$NON-NLS-1$
	}

	/**
	 * Returns a label for a version name. Example is '1.4.1'
	 *
	 * @param name the version string
	 * @return the version label
	 */
	public static String getVersionName(String name) {
		return markLTR(name, ":.");  //$NON-NLS-1$
	}
}

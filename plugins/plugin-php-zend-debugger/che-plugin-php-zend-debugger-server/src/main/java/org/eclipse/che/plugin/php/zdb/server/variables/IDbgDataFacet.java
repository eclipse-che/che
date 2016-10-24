/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.php.zdb.server.variables;

/**
 * Common interface for debug variables that might be described with the use of
 * additional facets that provides information about some useful meta-data like
 * variable kind, visibility, accessibility, etc.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDbgDataFacet {

	/**
	 * Variable facets.
	 */
	public enum Facet {

		/**
		 * Facet indicating that corresponding element is "this" variable.
		 */
		KIND_THIS,
		/**
		 * Facet indicating that corresponding element is super global variable.
		 */
		KIND_SUPER_GLOBAL,
		/**
		 * Facet indicating that corresponding element is local variable.
		 */
		KIND_LOCAL,
		/**
		 * Facet indicating that corresponding element is object member.
		 */
		KIND_OBJECT_MEMBER,
		/**
		 * Facet indicating that corresponding element is array member.
		 */
		KIND_ARRAY_MEMBER,
		/**
		 * Facet indicating that corresponding element is resource.
		 */
		KIND_RESOURCE,
		/**
		 * Facet indicating that corresponding element has public visibility.
		 */
		MOD_PUBLIC,
		/**
		 * Facet indicating that corresponding element has protected visibility.
		 */
		MOD_PROTECTED,
		/**
		 * Facet indicating that corresponding element has private visibility.
		 */
		MOD_PRIVATE,
		/**
		 * Facet indicating that corresponding element has static accessibility.
		 */
		MOD_STATIC,
		/**
		 * Facet indicating that corresponding element is 'virtual class'.
		 */
		VIRTUAL_CLASS,
		/**
		 * Facet indicating that corresponding element is 'virtual partition'.
		 */
		VIRTUAL_PARTITION,
		/**
		 * Facet indicating that corresponding element is an element length.
		 */
		VIRTUAL_LENGTH,
		/**
		 * Facet indicating that corresponding element is uninitialized.
		 */
		VIRTUAL_UNINIT,
		/**
		 * Facet indicating that corresponding element is 'virtual array' member.
		 */
		VIRTUAL_ARRAY_MEMBER;

	}

	/**
	 * Checks if variable has given facet.
	 * 
	 * @param facet
	 * @return <code>true</code> if variable has given facet, <code>false</code>
	 *         otherwise
	 */
	public boolean hasFacet(Facet facet);

	/**
	 * Adds facet(s) to the variable description.
	 * 
	 * @param facets
	 */
	public void addFacets(Facet... facets);

}

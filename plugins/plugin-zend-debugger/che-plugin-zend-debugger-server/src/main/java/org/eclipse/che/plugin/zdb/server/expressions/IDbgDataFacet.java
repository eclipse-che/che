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
package org.eclipse.che.plugin.zdb.server.expressions;

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

        KIND_THIS, KIND_SUPER_GLOBAL, KIND_LOCAL, KIND_OBJECT_MEMBER, KIND_ARRAY_MEMBER, KIND_RESOURCE, MOD_PUBLIC, MOD_PROTECTED, MOD_PRIVATE, MOD_STATIC, VIRTUAL_CLASS;

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

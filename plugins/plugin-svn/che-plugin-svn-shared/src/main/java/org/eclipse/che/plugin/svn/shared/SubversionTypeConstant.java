/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.shared;

/**
 * Client/server shared class for the subversion mixin type constant.
 */
public final class SubversionTypeConstant {

    private SubversionTypeConstant() {
    }

    public static final String SUBVERSION_MIXIN_TYPE = "subversion";

    /**
     * Attribute name for the respoitory URL.
     */
    public static final String SUBVERSION_ATTRIBUTE_REPOSITORY_URL = "svn.repository.url";
}

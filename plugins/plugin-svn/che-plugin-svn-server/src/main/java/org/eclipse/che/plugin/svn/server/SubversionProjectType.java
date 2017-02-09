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
package org.eclipse.che.plugin.svn.server;

import org.eclipse.che.api.project.server.type.TransientMixin;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.che.plugin.svn.shared.SubversionTypeConstant;

/**
 * Extension of {@link TransientMixin} for Subversion projects.
 */
@Singleton
public class SubversionProjectType extends TransientMixin {

    @Inject
    public SubversionProjectType(final SubversionValueProviderFactory factory) {
        super(SubversionTypeConstant.SUBVERSION_MIXIN_TYPE, "Subversion");

        addVariableDefinition(SubversionTypeConstant.SUBVERSION_ATTRIBUTE_REPOSITORY_URL, "SVN repository URL", true, factory);
    }
}

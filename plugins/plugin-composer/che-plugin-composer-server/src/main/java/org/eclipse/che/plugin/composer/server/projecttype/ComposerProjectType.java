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
package org.eclipse.che.plugin.composer.server.projecttype;

import com.google.inject.Inject;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.composer.shared.Constants.PACKAGE;
import static org.eclipse.che.plugin.php.shared.Constants.PHP_PROJECT_TYPE_ID;

/**
 * Composer project type.
 * 
 * @author Kaloyan Raev
 */
public class ComposerProjectType extends ProjectTypeDef {
    @Inject
    public ComposerProjectType(ComposerValueProviderFactory valueProviderFactory) {
        super(COMPOSER_PROJECT_TYPE_ID, "Composer", true, false, true);

        addVariableDefinition(PACKAGE, "Package name", true, valueProviderFactory);

        addParent(PHP_PROJECT_TYPE_ID);
    }
}

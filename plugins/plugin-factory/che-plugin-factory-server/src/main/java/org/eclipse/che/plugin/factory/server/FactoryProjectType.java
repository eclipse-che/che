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
package org.eclipse.che.plugin.factory.server;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.factory.shared.Constants;

import javax.inject.Singleton;


/**
 * Factory project type mixin.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class FactoryProjectType extends ProjectTypeDef {
    public FactoryProjectType() {
        super(Constants.FACTORY_PROJECT_TYPE_ID, Constants.FACTORY_PROJECT_TYPE_DISPLAY_NAME, false, true);
        addVariableDefinition(Constants.FACTORY_ID_ATTRIBUTE_NAME, "Factory flag", false);
    }
}

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
package org.eclipse.che.plugin.web.typescript;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.web.shared.Constants;

/**
 * TypeScript project type definition
 */
public class TypeScriptProjectType extends ProjectTypeDef {

    public TypeScriptProjectType(){
        super(Constants.TS_PROJECT_TYPE_ID, "TypeScript project", true, false, true);
        addConstantDefinition(Constants.LANGUAGE, "language", Constants.TS_LANG);
    }
}

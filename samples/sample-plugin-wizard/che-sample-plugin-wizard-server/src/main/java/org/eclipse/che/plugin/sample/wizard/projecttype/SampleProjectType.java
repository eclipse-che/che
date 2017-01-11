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
package org.eclipse.che.plugin.sample.wizard.projecttype;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import static org.eclipse.che.plugin.sample.wizard.shared.Constants.COMPILER_VERSION_ATRIBUTE;
import static org.eclipse.che.plugin.sample.wizard.shared.Constants.C_LANG;
import static org.eclipse.che.plugin.sample.wizard.shared.Constants.X_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.sample.wizard.shared.Constants.LANGUAGE;


/**
 * C wizard type
 * @author Vitalii Parfonov
 */
public class SampleProjectType extends ProjectTypeDef {
    @Inject
    public SampleProjectType() {
        super(X_PROJECT_TYPE_ID, "Sample Project Type", true, false, true);
        addConstantDefinition(LANGUAGE, "language", C_LANG);
        addVariableDefinition(COMPILER_VERSION_ATRIBUTE, "GCC compiler version", false);
    }
}

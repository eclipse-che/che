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
package org.eclipse.che.plugin.cpp.projecttype;

import com.google.inject.Inject;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_LANG;
import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.cpp.shared.Constants.LANGUAGE;

/**
 * C++ project type
 * @author Vitalii Parfonov
 */
public class CppProjectType extends ProjectTypeDef {
    @Inject
    public CppProjectType() {
        super(CPP_PROJECT_TYPE_ID, "C++", true, false, true);
        addConstantDefinition(LANGUAGE, "language", CPP_LANG);
    }
}

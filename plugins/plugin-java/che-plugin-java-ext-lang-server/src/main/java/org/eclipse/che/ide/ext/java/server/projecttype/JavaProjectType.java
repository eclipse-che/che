/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.server.projecttype;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.ide.ext.java.shared.Constants;

import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;

/**
 * Bare Java project type
 * @author gazarenkov
 * @author Dmitry Shnurenko
 */
public class JavaProjectType extends ProjectTypeDef {
    @Inject
    public JavaProjectType(JavaPropertiesValueProviderFactory jpFactory) {
        super("java", "Java", true, false);
        addConstantDefinition(Constants.LANGUAGE, "language", "java");
        addVariableDefinition(Constants.LANGUAGE_VERSION, "java version", true, jpFactory);
        addVariableDefinition(CONTAINS_JAVA_FILES, "contains java files", true, jpFactory);
    }
}

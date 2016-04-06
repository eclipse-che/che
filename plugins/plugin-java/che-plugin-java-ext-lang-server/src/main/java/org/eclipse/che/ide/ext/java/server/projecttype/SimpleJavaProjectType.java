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
import com.google.inject.Singleton;

import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

import static org.eclipse.che.ide.ext.java.shared.Constants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.SIMPLE_JAVA_PROJECT_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.SIMPLE_JAVA_PROJECT_NAME;
import static org.eclipse.che.ide.ext.java.shared.Constants.DEFAULT_SOURCE_FOLDER;

/**
 * Project type for simple java projects.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SimpleJavaProjectType extends ProjectTypeDef {
    @Inject
    public SimpleJavaProjectType() {
        super(SIMPLE_JAVA_PROJECT_ID, SIMPLE_JAVA_PROJECT_NAME, true, false, true);

        addConstantDefinition(DEFAULT_SOURCE_FOLDER, "", new AttributeValue(DEFAULT_SOURCE_FOLDER_VALUE));

        addParent(JAVA_ID);
    }
}

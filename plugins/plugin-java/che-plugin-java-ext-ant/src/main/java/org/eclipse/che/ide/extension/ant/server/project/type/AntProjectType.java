/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.server.project.type;

import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaProjectType;
import org.eclipse.che.ide.extension.ant.shared.AntAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.eclipse.che.ide.api.project.type.RunnerCategory.JAVA;

/**
 * @author Vladyslav Zhukovskii
 * @author Dmitry Shnurenko
 */
@Singleton
public class AntProjectType extends ProjectType {
    private static final Logger LOG = LoggerFactory.getLogger(AntProjectType.class);

    /** Create instance of {@link AntProjectType}. */
    @Inject
    public AntProjectType(AntValueProviderFactory antValueProviderFactory,
                          JavaProjectType javaProjectType) {
        super(AntAttributes.ANT_ID, AntAttributes.ANT_NAME, true, false);
        addParent(javaProjectType);
        setDefaultBuilder("ant");
        addVariableDefinition(AntAttributes.SOURCE_FOLDER, "", true, antValueProviderFactory);
        addVariableDefinition(AntAttributes.TEST_SOURCE_FOLDER, "", true, antValueProviderFactory);
        addRunnerCategories(Arrays.asList(JAVA.toString()));
    }
}

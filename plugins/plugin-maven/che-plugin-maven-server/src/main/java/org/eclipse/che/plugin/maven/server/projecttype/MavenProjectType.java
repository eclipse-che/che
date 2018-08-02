/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;

import com.google.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenProjectType extends ProjectTypeDef {

  @Inject
  public MavenProjectType(MavenValueProviderFactory mavenValueProviderFactory) {

    super(MavenAttributes.MAVEN_ID, MavenAttributes.MAVEN_NAME, true, false, true);

    addVariableDefinition(SOURCE_FOLDER, "source folder", true, mavenValueProviderFactory);
    addVariableDefinition(OUTPUT_FOLDER, "output folder", false, mavenValueProviderFactory);

    addVariableDefinition(MavenAttributes.GROUP_ID, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.ARTIFACT_ID, "", true, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.VERSION, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.PARENT_VERSION, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.PARENT_ARTIFACT_ID, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.PARENT_GROUP_ID, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.PACKAGING, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.TEST_SOURCE_FOLDER, "", false, mavenValueProviderFactory);
    addVariableDefinition(MavenAttributes.RESOURCE_FOLDER, "", false, mavenValueProviderFactory);

    addParent(JAVA_ID);
  }
}

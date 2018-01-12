/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.ide.ext.java.shared.Constants;

/**
 * Bare Java project type.
 *
 * @author gazarenkov
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class JavaProjectType extends ProjectTypeDef {
  @Inject
  public JavaProjectType(JavaValueProviderFactory jpFactory) {
    super("java", "Java", true, false);
    addConstantDefinition(Constants.LANGUAGE, "language", "java");
    addVariableDefinition(Constants.LANGUAGE_VERSION, "java version", true, jpFactory);
    addVariableDefinition(CONTAINS_JAVA_FILES, "contains java files", true, jpFactory);
    addVariableDefinition(SOURCE_FOLDER, "java source folder", true, jpFactory);
    addVariableDefinition(OUTPUT_FOLDER, "java output folder", false, jpFactory);
  }
}

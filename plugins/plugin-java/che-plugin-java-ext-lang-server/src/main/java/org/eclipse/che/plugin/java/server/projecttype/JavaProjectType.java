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
package org.eclipse.che.plugin.java.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;

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
  }
}

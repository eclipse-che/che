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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.JAVAC_PROJECT_NAME;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.tika.mime.MediaType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.search.server.excludes.MediaTypesExcludeMatcher;

/**
 * Project type for plain java projects.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class PlainJavaProjectType extends ProjectTypeDef {
  @Inject
  public PlainJavaProjectType(
      PlainJavaValueProviderFactory valueProviderFactory,
      MediaTypesExcludeMatcher mediaTypesExcludeMatcher) {
    super(JAVAC, JAVAC_PROJECT_NAME, true, false, true);

    setValueProviderFactory(SOURCE_FOLDER, valueProviderFactory);
    setValueProviderFactory(OUTPUT_FOLDER, valueProviderFactory);

    addVariableDefinition(LIBRARY_FOLDER, "java library folder", false);

    addParent(JAVA_ID);

    mediaTypesExcludeMatcher.addExcludedMediaType(new MediaType("application", "java-vm"));
    mediaTypesExcludeMatcher.addExcludedMediaType(new MediaType("application", "java-archive"));
  }
}

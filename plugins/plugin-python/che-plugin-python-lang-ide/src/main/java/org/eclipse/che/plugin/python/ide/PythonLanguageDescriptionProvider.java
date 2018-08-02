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
package org.eclipse.che.plugin.python.ide;

import static java.util.Arrays.asList;

import javax.inject.Provider;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.plugin.python.shared.ProjectAttributes;

public class PythonLanguageDescriptionProvider implements Provider<LanguageDescription> {
  private static final String[] EXTENSIONS = new String[] {ProjectAttributes.PYTHON_EXT};
  private static final String MIME_TYPE = "text/x-python";

  @Override
  public LanguageDescription get() {
    LanguageDescription description = new LanguageDescription();
    description.setFileExtensions(asList(EXTENSIONS));
    description.setLanguageId(ProjectAttributes.PYTHON_ID);
    description.setMimeType(MIME_TYPE);

    return description;
  }
}

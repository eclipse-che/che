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
package org.eclipse.che.plugin.yaml.ide;

import static java.util.Arrays.asList;

import javax.inject.Provider;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

public class YamlDescriptionProvider implements Provider<LanguageDescription> {
  private static final String LANGUAGE_ID = "yaml";
  private static final String[] EXTENSIONS = new String[] {"yaml", "yml"};
  private static final String MIME_TYPE = "text/yaml";

  @Override
  public LanguageDescription get() {
    LanguageDescription description = new LanguageDescription();
    description.setFileExtensions(asList(EXTENSIONS));
    description.setLanguageId(LANGUAGE_ID);
    description.setMimeType(MIME_TYPE);

    return description;
  }
}

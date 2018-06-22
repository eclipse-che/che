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
package org.eclipse.che.plugin.ceylon.ide;

import static java.util.Arrays.asList;

import javax.inject.Provider;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.plugin.ceylon.shared.Constants;

public class CeylonLanguageDescriptionProvider implements Provider<LanguageDescription> {
  private static final String[] EXTENSIONS = new String[] {Constants.CEYLON_EXT};
  private static final String MIME_TYPE = "text/x-ceylon";

  @Override
  public LanguageDescription get() {
    LanguageDescription description = new LanguageDescription();
    description.setFileExtensions(asList(EXTENSIONS));
    description.setLanguageId(Constants.LANGUAGE_ID);
    description.setMimeType(MIME_TYPE);
    description.setHighlightingConfiguration(CeylonResources.INSTANCE.syntax().getText());

    return description;
  }
}

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
package org.eclipse.che.plugin.maven.client;

import com.google.common.collect.ImmutableList;
import javax.inject.Provider;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

public class MavenLanguageDescriptionProvider implements Provider<LanguageDescription> {

  @Override
  public LanguageDescription get() {
    LanguageDescription description = new LanguageDescription();
    description.setLanguageId("pom");
    description.setMimeType("application/pom");
    description.setFileNames(ImmutableList.of("pom.xml"));
    return description;
  }
}

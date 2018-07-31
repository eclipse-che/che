/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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

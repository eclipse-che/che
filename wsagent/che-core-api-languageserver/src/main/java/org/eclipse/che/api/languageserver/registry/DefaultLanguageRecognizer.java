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
package org.eclipse.che.api.languageserver.registry;

import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

/**
 * Language recognizer based on default language set. Custom language descriptions created that are
 * injected by guice are taken into account prior to default language set. Language recognition is
 * based on file name or file extension, so may be improved further in future.
 */
@Singleton
public class DefaultLanguageRecognizer implements LanguageRecognizer {

  private final Set<LanguageDescription> languages;
  private final DefaultLanguages defaultLanguages;

  @Inject
  public DefaultLanguageRecognizer(
      Set<LanguageDescription> languages, DefaultLanguages defaultLanguages) {
    this.languages = languages;
    this.defaultLanguages = defaultLanguages;
  }

  @Override
  public LanguageDescription recognizeByPath(String wsPath) {
    Name name = new Name(wsPath);
    Extension extension = new Extension(wsPath);

    for (LanguageDescription language : languages) {
      if (name.matches(language) || extension.matches(language)) {
        return language;
      }
    }

    for (LanguageDescription language : defaultLanguages.getAll()) {
      if (name.matches(language) || extension.matches(language)) {
        return language;
      }
    }

    return UNIDENTIFIED;
  }

  @Override
  public LanguageDescription recognizeById(String id) {
    for (LanguageDescription language : languages) {
      if (language.getLanguageId().equals(id)) {
        return language;
      }
    }

    for (LanguageDescription language : defaultLanguages.getAll()) {
      if (language.getLanguageId().equals(id)) {
        return language;
      }
    }

    return UNIDENTIFIED;
  }

  private static class Name {
    private final String name;

    private Name(String wsPath) {
      String nameAndExtension = nameOf(wsPath);
      int lastDotIndex = nameAndExtension.lastIndexOf('.');

      this.name = lastDotIndex < 0 ? nameAndExtension : nameAndExtension.substring(0, lastDotIndex);
    }

    private boolean matches(LanguageDescription languageDescription) {
      return languageDescription.getFileNames().contains(name);
    }
  }

  private static class Extension {
    private final String extension;

    private Extension(String wsPath) {
      String nameAndExtension = nameOf(wsPath);
      int lastDotIndex = nameAndExtension.lastIndexOf('.');

      this.extension = lastDotIndex < 0 ? "" : nameAndExtension.substring(lastDotIndex + 1);
    }

    private boolean matches(LanguageDescription languageDescription) {
      return languageDescription.getFileExtensions().contains(extension);
    }
  }
}

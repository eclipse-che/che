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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.Collections;
import java.util.List;
import org.eclipse.lsp4j.ServerCapabilities;

/**
 * Initialize result per project and language
 *
 * @author Anatoliy Bazko
 * @author Thomas Mäder
 */
public class ExtendedInitializeResult {

  private String project;
  private ServerCapabilities capabilities;
  private List<? extends LanguageDescription> supportedLanguages;

  public ExtendedInitializeResult(
      String project,
      ServerCapabilities serverCapabilities,
      LanguageDescription languageDescription) {
    this.project = project;
    this.capabilities = serverCapabilities;
    this.supportedLanguages = Collections.singletonList(languageDescription);
  }

  public ExtendedInitializeResult() {}

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public ServerCapabilities getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(ServerCapabilities capabilities) {
    this.capabilities = capabilities;
  }

  public List<? extends LanguageDescription> getSupportedLanguages() {
    return supportedLanguages;
  }

  public void setSupportedLanguages(List<? extends LanguageDescription> supportedLanguages) {
    this.supportedLanguages = supportedLanguages;
  }
}

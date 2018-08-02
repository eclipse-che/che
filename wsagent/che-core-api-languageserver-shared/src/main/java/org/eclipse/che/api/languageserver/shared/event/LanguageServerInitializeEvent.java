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
package org.eclipse.che.api.languageserver.shared.event;

import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ServerCapabilities;

/** @author Anatoliy Bazko */
public class LanguageServerInitializeEvent {

  private String projectPath;
  private LanguageDescription supportedLanguages;
  private ServerCapabilities serverCapabilities;

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public LanguageDescription getSupportedLanguages() {
    return supportedLanguages;
  }

  public void setSupportedLanguages(LanguageDescription supportedLanguages) {
    this.supportedLanguages = supportedLanguages;
  }

  public ServerCapabilities getServerCapabilities() {
    return serverCapabilities;
  }

  public void setServerCapabilities(ServerCapabilities serverCapabilities) {
    this.serverCapabilities = serverCapabilities;
  }
}

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
package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.che.dto.shared.DTO;

/**
 * Language regular expression definition is mostly used to match a workspace path to a language
 * server instance. It is also may be used in some way to configure client file type registry.
 */
@DTO
public class LanguageRegex {
  private String languageId;
  private String namePattern;

  public String getLanguageId() {
    return languageId;
  }

  public void setLanguageId(String languageId) {
    this.languageId = languageId;
  }

  public String getNamePattern() {
    return namePattern;
  }

  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
  }
}

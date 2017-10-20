/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.shared.model;

public class FileContentParameters {
  private String languagesServerId;
  private String uri;

  public FileContentParameters() {}

  public FileContentParameters(String languagesServerId, String uri) {
    this.languagesServerId = languagesServerId;
    this.uri = uri;
  }

  public String getLanguagesServerId() {
    return languagesServerId;
  }

  public void setLanguagesServerId(String languagesServerId) {
    this.languagesServerId = languagesServerId;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}

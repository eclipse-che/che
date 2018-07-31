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

import org.eclipse.lsp4j.PublishDiagnosticsParams;

/**
 * Extends diagnostics notification with a server id, to keep diagnostics in different name spaces
 * with a different update cycle
 *
 * @author Thomas Mäder
 */
public class ExtendedPublishDiagnosticsParams {
  private PublishDiagnosticsParams params;
  private String languageServerId;

  public ExtendedPublishDiagnosticsParams() {}

  public ExtendedPublishDiagnosticsParams(String serverId, PublishDiagnosticsParams diagnostics) {
    this.languageServerId = serverId;
    this.params = diagnostics;
  }

  public PublishDiagnosticsParams getParams() {
    return params;
  }

  public void setParams(PublishDiagnosticsParams params) {
    this.params = params;
  }

  public String getLanguageServerId() {
    return languageServerId;
  }

  public void setLanguageServerId(String languageServerId) {
    this.languageServerId = languageServerId;
  }
}

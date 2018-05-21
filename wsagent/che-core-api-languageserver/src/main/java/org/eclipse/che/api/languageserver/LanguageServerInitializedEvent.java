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
package org.eclipse.che.api.languageserver;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Event is published when language server instance is properly initialized and ready.
 *
 * @author Dmytro Kulieshov
 */
@EventOrigin("languageServer")
public class LanguageServerInitializedEvent {
  private final String id;

  private final LanguageServer languageServer;

  LanguageServerInitializedEvent(String id, LanguageServer languageServer) {
    this.id = id;
    this.languageServer = languageServer;
  }

  /**
   * Get initialized language server id
   *
   * @return language server id
   */
  public String getId() {
    return id;
  }

  /**
   * Get initialized language server instance
   *
   * @return language server instance
   */
  public LanguageServer getLanguageServer() {
    return languageServer;
  }
}

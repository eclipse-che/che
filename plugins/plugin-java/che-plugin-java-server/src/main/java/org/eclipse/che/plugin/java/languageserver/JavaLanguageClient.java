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
package org.eclipse.che.plugin.java.languageserver;

import java.util.concurrent.CompletableFuture;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;
import org.eclipse.che.jdt.ls.extension.api.dto.StatusReport;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface JavaLanguageClient {
  /**
   * The show message notification is sent from a server to a client to ask the client to display a
   * particular message in the user interface.
   */
  @JsonNotification("language/status")
  void sendStatusReport(StatusReport report);

  /**
   * The actionable notification is sent from a server to a client to ask The show message
   * notification is sent from a server to a client to ask the the client to display a particular
   * message in the user interface, and possible client to display a particular message in the user
   * interface.
   */
  @JsonNotification("language/progressReport")
  void sendProgressReport(ProgressReport report);

  /** Execute custom command requests sent by jdt.ls */
  @JsonRequest("workspace/executeClientCommand")
  CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params);

  /** Handles custom notification sent by jdt.ls */
  @JsonNotification("workspace/notify")
  void sendNotification(ExecuteCommandParams params);
}

package org.eclipse.che.plugin.java.languageserver;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public interface JavaLanguageClient {
  /**
   * The show message notification is sent from a server to a client to ask the client to display a
   * particular message in the user interface.
   */
  @JsonNotification("language/status")
  void sendStatusReport(StatusReport report);
}

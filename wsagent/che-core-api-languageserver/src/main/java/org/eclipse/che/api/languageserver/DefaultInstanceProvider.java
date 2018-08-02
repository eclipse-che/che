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
package org.eclipse.che.api.languageserver;

import static org.eclipse.lsp4j.jsonrpc.Launcher.createLauncher;

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.che.api.languageserver.LanguageServerConfig.InstanceProvider;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default way it is expected to construct language server instance
 *
 * @author Dmytro Kulieshov
 */
public class DefaultInstanceProvider implements InstanceProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultInstanceProvider.class);

  private DefaultInstanceProvider() {
    LOG.debug("Constructing default instance provider");
  }

  public static InstanceProvider getInstance() {
    return InstanceHolder.instance;
  }

  @Override
  public LanguageServer get(LanguageClient client, InputStream in, OutputStream out) {
    Launcher<LanguageServer> launcher = createLauncher(client, LanguageServer.class, in, out);
    LOG.debug("Created launcher for language server");
    launcher.startListening();
    LOG.debug("Started listening");
    LanguageServer remoteProxy = launcher.getRemoteProxy();
    LOG.debug("Got remote proxy");
    return remoteProxy;
  }

  private static class InstanceHolder {
    private static final InstanceProvider instance = new DefaultInstanceProvider();
  }
}

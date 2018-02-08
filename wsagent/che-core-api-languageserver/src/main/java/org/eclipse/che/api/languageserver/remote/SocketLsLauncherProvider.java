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
package org.eclipse.che.api.languageserver.remote;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;

/** Provides socket based language server launchers */
@Singleton
class SocketLsLauncherProvider implements RemoteLsLauncherProvider {
  private static final Logger LOG = getLogger(SocketLsLauncherProvider.class);

  private final LsConfigurationDetector lsConfigurationDetector;
  private final LsConfigurationExtractor lsConfigurationExtractor;

  private final Map<String, LanguageServerLauncher> lslRegistry = new ConcurrentHashMap<>();

  @Inject
  public SocketLsLauncherProvider(
      LsConfigurationDetector lsConfigurationDetector,
      LsConfigurationExtractor lsConfigurationExtractor) {
    this.lsConfigurationDetector = lsConfigurationDetector;
    this.lsConfigurationExtractor = lsConfigurationExtractor;
  }

  @Override
  public Set<LanguageServerLauncher> getAll(Workspace workspace) {
    Runtime runtime = workspace.getRuntime();
    if (runtime == null) {
      return emptySet();
    }

    for (Map.Entry<String, ? extends Machine> machineEntry : runtime.getMachines().entrySet()) {
      String machineName = machineEntry.getKey();
      Machine machine = machineEntry.getValue();
      Map<String, ? extends Server> servers = machine.getServers();

      for (Map.Entry<String, ? extends Server> serverEntry : servers.entrySet()) {
        String serverName = serverEntry.getKey();
        Server server = serverEntry.getValue();
        String serverUrl = server.getUrl();
        Map<String, String> serverAttributes = server.getAttributes();

        if (lslRegistry.keySet().contains(machineName + serverName)) {
          continue;
        }

        if (!lsConfigurationDetector.isDetected(serverAttributes)) {
          continue;
        }

        LanguageServerDescription description = lsConfigurationExtractor.extract(serverAttributes);

        try {
          URI uri = new URI(serverUrl);
          String host = uri.getHost();
          int port = uri.getPort();

          SocketLanguageServerLauncher launcher =
              new SocketLanguageServerLauncher(description, host, port);
          lslRegistry.put(machineName + serverName, launcher);
        } catch (URISyntaxException e) {
          LOG.error("Can't parse server url: {}", serverUrl, e);
        }
      }
    }

    return unmodifiableSet(new HashSet<>(lslRegistry.values()));
  }

  private static final class SocketLanguageServerLauncher implements LanguageServerLauncher {

    private final LanguageServerDescription languageServerDescription;
    private final String host;
    private final int port;

    SocketLanguageServerLauncher(
        LanguageServerDescription languageServerDescription, String host, int port) {
      this.languageServerDescription = languageServerDescription;
      this.host = host;
      this.port = port;
    }

    @Override
    public LanguageServer launch(String projectPath, LanguageClient client)
        throws LanguageServerException {
      try {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        Launcher<LanguageServer> launcher =
            Launcher.createLauncher(client, LanguageServer.class, inputStream, outputStream);

        launcher.startListening();
        return launcher.getRemoteProxy();
      } catch (IOException e) {
        throw new LanguageServerException(
            "Can't launch language server for project: " + projectPath, e);
      }
    }

    @Override
    public boolean isLocal() {
      return false;
    }

    @Override
    public LanguageServerDescription getDescription() {
      return languageServerDescription;
    }

    @Override
    public boolean isAbleToLaunch() {
      return host != null && languageServerDescription != null;
    }
  }
}

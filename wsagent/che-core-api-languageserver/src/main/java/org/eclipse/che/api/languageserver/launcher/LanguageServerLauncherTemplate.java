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
package org.eclipse.che.api.languageserver.launcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
public abstract class LanguageServerLauncherTemplate implements LanguageServerLauncher {

  private LaunchingStrategy launchingStrategy;

  public LanguageServerLauncherTemplate() {
    this.launchingStrategy = createLauncherStrategy();
  }

  protected LaunchingStrategy createLauncherStrategy() {
    return PerWorkspaceLaunchingStrategy.INSTANCE;
  }

  private static Logger LOGGER = LoggerFactory.getLogger(LanguageServerLauncherTemplate.class);

  @Override
  public final LanguageServer launch(String fileUri, LanguageClient client)
      throws LanguageServerException {
    Process languageServerProcess = startLanguageServerProcess(fileUri);
    waitCheckProcess(languageServerProcess);
    return connectToLanguageServer(languageServerProcess, client);
  }

  /**
   * Temporary solution, in future need to provide some service that can watch for LS process and
   * notify user in case in some reason it stopped. For now we just check it once start it before
   * connect to it. Ask with delay in 5 seconds this delay chose empirical in normal state should be
   * enough for start or fail process. If after 5 seconds process not alive we notify client about
   * problem.
   *
   * @param languageServerProcess
   * @throws LanguageServerException
   */
  private void waitCheckProcess(Process languageServerProcess) throws LanguageServerException {
    long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);

    try {
      while (!languageServerProcess.isAlive() && System.currentTimeMillis() < endTime) {
        TimeUnit.MILLISECONDS.sleep(10);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (!languageServerProcess.isAlive()) {
      final String error;
      try {
        error = IoUtil.readStream(languageServerProcess.getErrorStream());
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
        throw new LanguageServerException("Can't start language server process");
      }
      LOGGER.error("Can't start language server process. Got error: {}", error);
      throw new LanguageServerException(error);
    }
  }

  @Override
  public LaunchingStrategy getLaunchingStrategy() {
    return launchingStrategy;
  }

  protected abstract Process startLanguageServerProcess(String fileUri)
      throws LanguageServerException;

  protected abstract LanguageServer connectToLanguageServer(
      Process languageServerProcess, LanguageClient client) throws LanguageServerException;
}

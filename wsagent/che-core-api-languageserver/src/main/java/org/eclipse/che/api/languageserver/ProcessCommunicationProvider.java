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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation provides communication to a process that runs a language server. Besides that
 * it also lazily starts the language server process.
 *
 * @author Dmytro Kulieshov
 */
public class ProcessCommunicationProvider implements CommunicationProvider {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessCommunicationProvider.class);

  private final ProcessBuilder processBuilder;
  private final String languageServerName;
  private final StatusChecker statusChecker;

  private Process process;

  public ProcessCommunicationProvider(ProcessBuilder processBuilder, String languageServerName) {
    this.processBuilder = processBuilder;
    this.languageServerName = languageServerName;

    this.statusChecker =
        new StatusChecker() {
          @Override
          public boolean isAlive() {
            return process != null && process.isAlive();
          }

          @Override
          public String getCause() {
            if (process == null) {
              return "Process is NULL";
            }

            if (process.isAlive()) {
              return null;
            }

            try {
              return IOUtils.toString(process.getErrorStream());
            } catch (IOException e) {
              return e.getMessage();
            }
          }
        };
  }

  @Override
  public InputStream getInputStream() throws LanguageServerException {
    startProcessLazily();

    return process.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws LanguageServerException {
    startProcessLazily();

    return process.getOutputStream();
  }

  @Override
  public StatusChecker getStatusChecker() throws LanguageServerException {
    startProcessLazily();

    return statusChecker;
  }

  private void startProcessLazily() throws LanguageServerException {
    LOG.debug("Starting process lazily");
    if (process == null) {
      LOG.debug("Process is not started, starting.");
      try {
        process = processBuilder.start();
      } catch (IOException e) {
        String error = String.format("Can't start process for '%s'", languageServerName);
        LOG.error(error, e);
        throw new LanguageServerException(error, e);
      }
    } else {
      LOG.debug("Process is started, skipping.");
    }
  }
}

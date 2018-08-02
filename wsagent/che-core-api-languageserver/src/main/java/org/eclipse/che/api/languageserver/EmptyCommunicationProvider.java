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

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.everrest.core.tools.EmptyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Empty communication provider must be used in cases where there is no need in a communication
 * between language server instance and language server server in a default way (i.e. using
 * input/output streams). The example of of such use case is a custom language server instance
 * provider and the language server server that runs in the same virtual machine and does not use
 * input/output streams for communication.
 *
 * @author Dmytro Kulieshov
 */
public class EmptyCommunicationProvider implements CommunicationProvider {
  private static final Logger LOG = LoggerFactory.getLogger(EmptyCommunicationProvider.class);

  private InputStream inputStream;
  private OutputStream outputStream;
  private StatusChecker statusChecker;

  private EmptyCommunicationProvider() {
    LOG.debug("Constructing empty communication provider");
  }

  public static CommunicationProvider getInstance() {
    return InstanceHolder.instance;
  }

  @Override
  public InputStream getInputStream() {
    if (inputStream == null) {
      LOG.debug("Input stream is null, initializing with empty input stream");
      inputStream = new EmptyInputStream();
    }
    return inputStream;
  }

  @Override
  public OutputStream getOutputStream() {
    if (outputStream == null) {
      LOG.debug("Output stream is null, initializing with null output stream");
      outputStream = ByteStreams.nullOutputStream();
    }
    return outputStream;
  }

  @Override
  public StatusChecker getStatusChecker() {
    if (statusChecker == null) {
      LOG.debug("Status checker is null, initializing with default status checker");
      statusChecker =
          new StatusChecker() {
            @Override
            public boolean isAlive() {
              return true;
            }

            @Override
            public String getCause() {
              return null;
            }
          };
    }
    return statusChecker;
  }

  private static class InstanceHolder {
    private static final CommunicationProvider instance = new EmptyCommunicationProvider();
  }
}

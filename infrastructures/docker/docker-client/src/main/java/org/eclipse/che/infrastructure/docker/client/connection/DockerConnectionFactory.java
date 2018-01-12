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
package org.eclipse.che.infrastructure.docker.client.connection;

import com.google.inject.Inject;
import java.net.URI;
import javax.inject.Named;
import org.eclipse.che.infrastructure.docker.client.DockerCertificates;
import org.eclipse.che.infrastructure.docker.client.DockerConnectorConfiguration;

/**
 * Factory for connections to docker API.
 *
 * <p>Detects connection implementation by checking docker daemon URI.
 *
 * @author Alexander Garagatyi
 */
public class DockerConnectionFactory {
  public static final String CONNECTION_TIMEOUT_MS_PROPERTY =
      "che.docker.tcp_connection_timeout_ms";
  public static final String CONNECTION_READ_TIMEOUT_MS_PROPERTY =
      "che.docker.tcp_connection_read_timeout_ms";

  @Inject(optional = true)
  @Named(CONNECTION_TIMEOUT_MS_PROPERTY)
  private int connectionTimeoutMs = 60000;

  @Inject(optional = true)
  @Named(CONNECTION_READ_TIMEOUT_MS_PROPERTY)
  private int connectionReadTimeoutMs = 60000;

  private final DockerCertificates dockerCertificates;

  @Inject
  public DockerConnectionFactory(DockerConnectorConfiguration connectorConfiguration) {
    this.dockerCertificates = connectorConfiguration.getDockerCertificates();
  }

  public DockerConnection openConnection(URI dockerDaemonUri) {
    if (DockerConnectorConfiguration.isUnixSocketUri(dockerDaemonUri)) {
      return new UnixSocketConnection(dockerDaemonUri.getPath());
    } else {
      return new TcpConnection(
          dockerDaemonUri, dockerCertificates, connectionTimeoutMs, connectionReadTimeoutMs);
    }
  }
}

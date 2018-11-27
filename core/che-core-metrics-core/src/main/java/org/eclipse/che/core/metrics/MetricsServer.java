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
package org.eclipse.che.core.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Bind on 8087 port http endpoint with prometheus metrics. */
@Singleton
public class MetricsServer {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsServer.class);

  private HTTPServer server;
  private final CollectorRegistry collectorRegistry;
  private final Integer metricsPort;

  @Inject
  public MetricsServer(
      CollectorRegistry collectorRegistry, @Named("che.metrics.port") Integer metricsPort) {
    this.collectorRegistry = collectorRegistry;
    this.metricsPort = metricsPort;
  }

  public void startServer() throws IOException {
    this.server = new HTTPServer(new InetSocketAddress(metricsPort), collectorRegistry, true);
    LOG.info("Metrics server started at port {} successfully ", metricsPort);
  }

  @PreDestroy
  public void stopServer() {
    if (server != null) {
      server.stop();
      LOG.info("Metrics server suspended at port {} successfully ", metricsPort);
    }
  }
}

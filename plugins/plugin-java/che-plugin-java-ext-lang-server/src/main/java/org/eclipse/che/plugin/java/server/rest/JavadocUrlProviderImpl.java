/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.JavadocUrlProvider;

/**
 * Default implementation of the {@link JavadocUrlProvider}. Use <code>wsagent.endpoint</code>
 * constant and {@link UriBuilder} to build URL to {@link JavadocService#get(String, String)}
 * service
 */
@Singleton
public class JavadocUrlProviderImpl implements JavadocUrlProvider {

  private final String agentEndpoint;

  @Inject
  public JavadocUrlProviderImpl(@Named("wsagent.endpoint") String agentEndpoint) {
    this.agentEndpoint = agentEndpoint;
  }

  @Override
  public String getJavadocUrl(String projectPath) {
    return UriBuilder.fromUri(agentEndpoint)
            .queryParam("projectpath", projectPath)
            .path(JavadocService.class)
            .path(JavadocService.class, "get")
            .build()
            .toString()
        + "&handle=";
  }
}

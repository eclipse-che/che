/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.proxy;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds proxy configuration to {@link DockerContainerConfig}.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ProxySettingsProvisioner implements ContainerSystemSettingsProvisioner {
  private static final String HTTPS_PROXY = "https_proxy";
  private static final String HTTP_PROXY = "http_proxy";
  private static final String NO_PROXY = "no_proxy";

  // note that values are the same for now
  private final Map<String, String> proxyEnvVars;
  private final Map<String, String> buildArgs;

  @Inject
  public ProxySettingsProvisioner(
      @Named("che.workspace.https_proxy") String httpsProxy,
      @Named("che.workspace.http_proxy") String httpProxy,
      @Named("che.workspace.no_proxy") String noProxy) {
    proxyEnvVars = new HashMap<>();
    buildArgs = new HashMap<>();
    if (!httpsProxy.isEmpty()) {
      proxyEnvVars.put(HTTPS_PROXY, httpsProxy);
      buildArgs.put(HTTPS_PROXY, httpsProxy);
    }
    if (!httpProxy.isEmpty()) {
      proxyEnvVars.put(HTTP_PROXY, httpProxy);
      buildArgs.put(HTTP_PROXY, httpProxy);
    }
    if (!noProxy.isEmpty()) {
      proxyEnvVars.put(NO_PROXY, noProxy);
      buildArgs.put(NO_PROXY, noProxy);
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getEnvironment().putAll(proxyEnvVars);
      if (containerConfig.getBuild() != null) {
        containerConfig.getBuild().getArgs().putAll(buildArgs);
      }
    }
  }
}
